package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.database.UserDao
import com.example.data.database.BalanceDao
import com.example.data.database.ReferralDao
import com.example.data.model.Balance
import com.example.data.model.UserProfile
import com.example.data.model.ReferralRel
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "earnpulse_prefs")

@OptIn(DelicateCoroutinesApi::class)
class UserRepository(
    private val context: Context,
    private val userDao: UserDao,
    private val balanceDao: BalanceDao,
    private val referralDao: ReferralDao
) {
    private val USER_ID_KEY = intPreferencesKey("logged_in_user_id")

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser = _currentUser.asStateFlow()

    init {
        // Read saved session on startup
        kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            val savedUserId = context.dataStore.data.map { preferences ->
                preferences[USER_ID_KEY] ?: -1
            }.first()
            if (savedUserId != -1) {
                _currentUser.value = userDao.getUserByIdSync(savedUserId)
            }
        }
    }

    suspend fun register(fullName: String, username: String, email: String, passwordHash: String, referralCodeInserted: String?): Result<UserProfile> {
        // Check uniqueness
        if (userDao.getUserByEmailSync(email) != null) {
            return Result.failure(Exception("Email already registered"))
        }
        if (userDao.getUserByUsernameSync(username) != null) {
            return Result.failure(Exception("Username already taken"))
        }

        // Generate own referral code (8-char uppercase alphanumeric)
        val ownReferralCode = generateReferralCode()

        var referrerProfile: UserProfile? = null
        if (!referralCodeInserted.isNullOrBlank()) {
            referrerProfile = userDao.getUserByReferralCodeSync(referralCodeInserted.uppercase().trim())
            if (referrerProfile == null) {
                return Result.failure(Exception("Invalid referral code"))
            }
        }

        val profile = UserProfile(
            fullName = fullName,
            username = username,
            email = email,
            passwordHash = passwordHash,
            referralCode = ownReferralCode,
            referredBy = referrerProfile?.referralCode
        )

        val newId = userDao.insertUser(profile).toInt()
        val createdProfile = profile.copy(id = newId)

        // Initialize balance
        balanceDao.insertBalance(Balance(userId = newId))

        // Save referrer relations (L1 referral)
        if (referrerProfile != null) {
            referralDao.insertReferral(
                ReferralRel(
                    id = UUID.randomUUID().toString(),
                    referrerId = referrerProfile.id,
                    referredId = newId,
                    level = 1
                )
            )

            // L2 Referral (Referred by the person who referred the referrer)
            if (referrerProfile.referredBy != null) {
                val l2Referrer = userDao.getUserByReferralCodeSync(referrerProfile.referredBy)
                if (l2Referrer != null) {
                    referralDao.insertReferral(
                        ReferralRel(
                            id = UUID.randomUUID().toString(),
                            referrerId = l2Referrer.id,
                            referredId = newId,
                            level = 2
                        )
                    )
                }
            }
        }

        // Auto login
        loginSession(createdProfile)
        return Result.success(createdProfile)
    }

    suspend fun login(email: String, passwordHash: String): Result<UserProfile> {
        val user = userDao.getUserByEmailSync(email) ?: return Result.failure(Exception("User not found"))
        if (user.passwordHash != passwordHash) {
            return Result.failure(Exception("Invalid credentials"))
        }
        if (user.accountStatus == "BANNED") {
            return Result.failure(Exception("This account is suspended due to fraud policy violation."))
        }

        loginSession(user)
        return Result.success(user)
    }

    suspend fun logout() {
        context.dataStore.edit { preferences ->
            preferences.remove(USER_ID_KEY)
        }
        _currentUser.value = null
    }

    private suspend fun loginSession(user: UserProfile) {
        context.dataStore.edit { preferences ->
            preferences[USER_ID_KEY] = user.id
        }
        _currentUser.value = user
    }

    suspend fun reloadCurrentUser() {
        _currentUser.value?.let { current ->
            _currentUser.value = userDao.getUserByIdSync(current.id)
        }
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        userDao.updateUser(profile)
        _currentUser.value = profile
    }

    fun getReferrals(userId: Int): Flow<List<ReferralRel>> {
        return referralDao.getReferralsForReferrer(userId)
    }

    fun getReferralCount(userId: Int): Flow<Int> {
        return referralDao.getReferralCount(userId)
    }

    fun observeUser(userId: Int): Flow<UserProfile?> {
        return userDao.getUserById(userId)
    }

    fun getLeaderboard(): Flow<List<UserProfile>> {
        return userDao.getTopUsersByLevel()
    }

    private fun generateReferralCode(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..8).map { chars.random() }.joinToString("")
    }

    suspend fun gainXp(userId: Int, amount: Int): Pair<Int, Boolean> {
        val user = userDao.getUserByIdSync(userId) ?: return Pair(1, false)
        val newXp = user.xpPoints + amount
        val requiredXpForCurrentLevel = user.level * 100 // Level 1 needs 100 XP, Level 2 needs 200 XP, etc.
        var levelUp = false
        var currentLevel = user.level
        var remainingXp = newXp

        while (remainingXp >= currentLevel * 100) {
            remainingXp -= currentLevel * 100
            currentLevel++
            levelUp = true
        }

        val updatedUser = user.copy(level = currentLevel, xpPoints = remainingXp)
        updateUserProfile(updatedUser)
        return Pair(currentLevel, levelUp)
    }
}
