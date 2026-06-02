package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AdRepository
import com.example.data.repository.UserRepository
import com.example.data.repository.WithdrawalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class EarnPulseViewModel(application: Application) : AndroidViewModel(application) {
    val db = AppDatabase.getDatabase(application)
    
    // Repositories
    val userRepository = UserRepository(
        context = application,
        userDao = db.userDao(),
        balanceDao = db.balanceDao(),
        referralDao = db.referralDao()
    )
    val adRepository = AdRepository(
        userDao = db.userDao(),
        balanceDao = db.balanceDao(),
        earningDao = db.earningDao(),
        adSessionDao = db.adSessionDao(),
        dailyStatsDao = db.dailyStatsDao(),
        referralDao = db.referralDao(),
        goalDao = db.goalDao(),
        notificationDao = db.notificationDao(),
        fraudDao = db.fraudDao(),
        userRepository = userRepository
    )
    val withdrawalRepository = WithdrawalRepository(
        userDao = db.userDao(),
        balanceDao = db.balanceDao(),
        withdrawalDao = db.withdrawalDao(),
        payMethodDao = db.payMethodDao(),
        notificationDao = db.notificationDao()
    )

    // AUTH USER STATE
    val currentUser: StateFlow<UserProfile?> = userRepository.currentUser

    // BALANCE & STATS REACTIVE STATE
    val currentBalance: StateFlow<Balance?> = currentUser.flatMapLatest { user ->
        if (user != null) db.balanceDao().getBalance(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentDailyStats: StateFlow<UserDailyStats?> = currentUser.flatMapLatest { user ->
        if (user != null) adRepository.observeDailyStats(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val myEarnings: StateFlow<List<Earning>> = currentUser.flatMapLatest { user ->
        if (user != null) db.earningDao().getEarningsByUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myWithdrawals: StateFlow<List<Withdrawal>> = currentUser.flatMapLatest { user ->
        if (user != null) withdrawalRepository.getWithdrawals(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myPaymentMethods: StateFlow<List<PayMethod>> = currentUser.flatMapLatest { user ->
        if (user != null) withdrawalRepository.getPaymentMethods(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val myNotifications: StateFlow<List<Notification>> = currentUser.flatMapLatest { user ->
        if (user != null) db.notificationDao().getNotificationsByUser(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) db.notificationDao().getUnreadCount(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val leaderboardUsers: StateFlow<List<UserProfile>> = userRepository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val referralCount: StateFlow<Int> = currentUser.flatMapLatest { user ->
        if (user != null) userRepository.getReferralCount(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // AD GAME STATE
    private val _activeAdSession = MutableStateFlow<AdSession?>(null)
    val activeAdSession = _activeAdSession.asStateFlow()

    private val _adProgress = MutableStateFlow(0f) 
    val adProgress = _adProgress.asStateFlow()

    private val _adTimeRemaining = MutableStateFlow(30)
    val adTimeRemaining = _adTimeRemaining.asStateFlow()

    private val _isWatchingAd = MutableStateFlow(false)
    val isWatchingAd = _isWatchingAd.asStateFlow()

    private val _adVerificationResult = MutableStateFlow<String?>(null)
    val adVerificationResult = _adVerificationResult.asStateFlow()

    // ADMIN STATE
    val adminAllUsers: StateFlow<List<UserProfile>> = db.userDao().getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminAllWithdrawals: StateFlow<List<Withdrawal>> = withdrawalRepository.getAllWithdrawals()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminFraudLogs: StateFlow<List<FraudFlag>> = db.fraudDao().getAllFraudFlags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val adminCountUsers: StateFlow<Int> = db.userDao().getUserCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // UI Status Actions
    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _withdrawalMsg = MutableStateFlow<String?>(null)
    val withdrawalMsg = _withdrawalMsg.asStateFlow()

    init {
        // Seed Admin Configuration parameters
        viewModelScope.launch(Dispatchers.IO) {
            if (db.adminSettingDao().getSetting("min_withdrawal") == null) {
                db.adminSettingDao().insertSetting(AdminSetting("min_withdrawal", "5.0"))
            }
        }
    }

    // AUTH ACTIONS
    fun register(fullName: String, username: String, email: String, password: String, referredBy: String?, onSuccess: () -> Unit) {
        _authError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.register(fullName, username, email, password, referredBy)
            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    _authError.value = result.exceptionOrNull()?.message ?: "Unknown registration failure"
                }
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        _authError.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val result = userRepository.login(email, password)
            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    onSuccess()
                } else {
                    _authError.value = result.exceptionOrNull()?.message ?: "Incorrect email or password."
                }
            }
        }
    }

    fun clearAuthError() {
        _authError.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            userRepository.logout()
            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // PROFILE PIN SETUP
    fun updateWithDrawalPin(pin: String, onSuccess: () -> Unit) {
        val current = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val updated = current.copy(withdrawalPin = pin)
            userRepository.updateUserProfile(updated)
            launch(Dispatchers.Main) {
                onSuccess()
            }
        }
    }

    // AD SESSIONS ACTIONS
    fun startAdWatchSession(adUnitId: String, onFail: (String) -> Unit) {
        val user = currentUser.value ?: return
        _adVerificationResult.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val result = adRepository.startAdSession(user.id, "VIDEO_30S", adUnitId, "FINGERPRINT_MOBILE_DEMO", "192.168.1.100")
            launch(Dispatchers.Main) {
                if (result.isSuccess) {
                    _activeAdSession.value = result.getOrNull()
                    _adProgress.value = 0f
                    _adTimeRemaining.value = 30
                    _isWatchingAd.value = true
                } else {
                    onFail(result.exceptionOrNull()?.message ?: "Check quota conditions.")
                }
            }
        }
    }

    fun completeActiveAdSession(hiddenCount: Int, onComplete: (String) -> Unit) {
        val session = _activeAdSession.value ?: return
        _isWatchingAd.value = false
        _activeAdSession.value = null
        viewModelScope.launch(Dispatchers.IO) {
            // Actual watch simulated time
            val status = adRepository.completeAdSession(
                sessionId = session.id,
                actualDurationSeconds = 30 - _adTimeRemaining.value,
                hiddenCount = hiddenCount
            )
            launch(Dispatchers.Main) {
                if (status.isSuccess) {
                    val earnings = status.getOrNull()?.earnedAmount ?: 0.0
                    val textEarnings = String.format("%.4f", earnings)
                    _adVerificationResult.value = "+$$textEarnings Credited Successfully"
                    onComplete(textEarnings)
                } else {
                    _adVerificationResult.value = "Earning voided: " + status.exceptionOrNull()?.message
                }
            }
        }
    }

    fun tickAdTime(elapsed: Int) {
        _adTimeRemaining.value = (_adTimeRemaining.value - elapsed).coerceAtLeast(0)
        _adProgress.value = ((30 - _adTimeRemaining.value) / 30f).coerceIn(0f, 1f)
    }

    fun forceCancelAd() {
        _isWatchingAd.value = false
        _activeAdSession.value = null
    }

    // WHEEL SPIN
    fun spinLuckyWheel(onSuccess: (Double) -> Unit, onFail: (String) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            val res = adRepository.spinDailyWheel(user.id)
            launch(Dispatchers.Main) {
                if (res.isSuccess) {
                    onSuccess(res.getOrThrow())
                } else {
                    onFail(res.exceptionOrNull()?.message ?: "Ad completion limit.")
                }
            }
        }
    }

    // PAYMENT SYSTEM
    fun addReceiveMethod(type: String, label: String, detail: String, onDone: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            withdrawalRepository.savePaymentMethod(user.id, type, label, detail)
            launch(Dispatchers.Main) {
                onDone()
            }
        }
    }

    fun removeReceiveMethod(methodId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withdrawalRepository.deletePaymentMethod(methodId)
        }
    }

    fun executeWithdrawRequest(amount: Double, method: String, details: String, pin: String, onSuccess: () -> Unit) {
        val user = currentUser.value ?: return
        _withdrawalMsg.value = null
        viewModelScope.launch(Dispatchers.IO) {
            val res = withdrawalRepository.requestWithdrawal(user.id, amount, method, details, pin)
            launch(Dispatchers.Main) {
                if (res.isSuccess) {
                    userRepository.reloadCurrentUser()
                    onSuccess()
                } else {
                    _withdrawalMsg.value = res.exceptionOrNull()?.message ?: "Security verification decline."
                }
            }
        }
    }

    fun clearWithdrawMsg() {
        _withdrawalMsg.value = null
    }

    // NOTIFICATIONS CLEANUP
    fun clearNotifications() {
        val user = currentUser.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            db.notificationDao().markAllAsRead(user.id)
        }
    }

    // ADMIN PRIVILEGED ACTIONS
    fun adminApproveTransfer(withdrawalId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withdrawalRepository.approveWithdrawalSync(withdrawalId, "Approved via EarnPulse Admin Console")
        }
    }

    fun adminRejectTransfer(withdrawalId: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            withdrawalRepository.rejectWithdrawalSync(withdrawalId, reason)
        }
    }

    fun adminTierUpgrade(userId: Int, tier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUserByIdSync(userId) ?: return@launch
            val updated = user.copy(accountTier = tier)
            db.userDao().updateUser(updated)
        }
    }

    fun adminBanUser(userId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = db.userDao().getUserByIdSync(userId) ?: return@launch
            val updated = user.copy(accountStatus = "BANNED")
            db.userDao().updateUser(updated)
        }
    }
}
