package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullName: String,
    val username: String, // Unique
    val email: String,    // Unique
    val passwordHash: String,
    val countryCode: String = "US",
    val avatarUrl: String = "",
    val referralCode: String, // Unique 8-char
    val referredBy: String? = null, // referralCode of another user
    val accountTier: String = "FREE", // "FREE", "PREMIUM"
    val accountStatus: String = "ACTIVE", // "ACTIVE", "BANNED"
    val level: Int = 1,
    val xpPoints: Int = 0,
    val streakDays: Int = 0,
    val lastStreakDate: Long? = null,
    val withdrawalPin: String = "",
    val lastIp: String = "127.0.0.1",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "balances")
data class Balance(
    @PrimaryKey val userId: Int,
    val availableBalance: Double = 0.0,
    val pendingBalance: Double = 0.0,
    val totalEarned: Double = 0.0,
    val totalWithdrawn: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "earnings")
data class Earning(
    @PrimaryKey val id: String, // UUID
    val userId: Int,
    val type: String, // "AD_WATCH", "SPIN_WHEEL", "REFERRAL_COMMISSION", "GOAL_REWARD"
    val amount: Double,
    val referenceId: String = "",
    val description: String,
    val status: String = "CONFIRMED",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "withdrawals")
data class Withdrawal(
    @PrimaryKey val id: String, // UUID
    val userId: Int,
    val amount: Double,
    val fee: Double,
    val netAmount: Double,
    val method: String, // "PAYPAL", "WISE", "CRYPTO"
    val paymentDetails: String, // e.g. PayPal Email or Wallet Address
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val rejectionReason: String? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null,
    val adminNote: String? = null,
    val referenceId: String? = null
)

@Entity(tableName = "pay_methods")
data class PayMethod(
    @PrimaryKey val id: String,
    val userId: Int,
    val methodType: String, // "PAYPAL", "WISE", "CRYPTO"
    val label: String,
    val details: String,
    val isVerified: Boolean = true,
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ad_sessions")
data class AdSession(
    @PrimaryKey val id: String,
    val userId: Int,
    val adUnitId: String,
    val adType: String, // "VIDEO_30S", "INTERSTITIAL", "BANNER"
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val durationSeconds: Int = 0,
    val earningAmount: Double = 0.0,
    val ipAddress: String = "127.0.0.1",
    val deviceFingerprint: String = "",
    val isValid: Boolean = true,
    val invalidationReason: String? = null
)

@Entity(tableName = "user_daily_stats")
data class UserDailyStats(
    @PrimaryKey val id: String, // userId_dateString, e.g. "1_2026-06-02"
    val userId: Int,
    val date: String, // "YYYY-MM-DD"
    val adsWatched: Int = 0,
    val adsClicked: Int = 0,
    val offersCompleted: Int = 0,
    val dailyEarned: Double = 0.0,
    val spinUsed: Boolean = false
)

@Entity(tableName = "referrals")
data class ReferralRel(
    @PrimaryKey val id: String,
    val referrerId: Int,
    val referredId: Int,
    val level: Int = 1, // 1 or 2
    val totalCommissionPaid: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey val id: String,
    val userId: Int,
    val type: String, // "DAILY_ADS", "EARNINGS", "REFERRALS"
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val deadline: Long,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED", "EXPIRED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey val id: String,
    val userId: Int,
    val type: String, // "EARNING", "WITHDRAWAL", "REFERRAL", "STREAK", "GOAL"
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val actionUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "fraud_flags")
data class FraudFlag(
    @PrimaryKey val id: String,
    val userId: Int,
    val flagType: String, // "TAB_VISIBILITY", "SPEED_WATCH", "DUPLICATE_IP"
    val details: String,
    val autoAction: String? = null,
    val resolved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_settings")
data class AdminSetting(
    @PrimaryKey val key: String,
    val value: String // JSON string represent values
)

@Entity(tableName = "challenges")
data class Challenge(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String, // "ADS_WATCHED", "TOTAL_EARNED", "DAILY_STREAK"
    val targetValue: Int,
    val rewardAmount: Double,
    val rewardType: String = "CASH", // "CASH", "XP"
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)

@Entity(tableName = "user_challenges")
data class UserChallenge(
    @PrimaryKey val id: String,
    val userId: Int,
    val challengeId: String,
    val currentProgress: Int = 0,
    val status: String = "ACTIVE", // "ACTIVE", "COMPLETED"
    val completedAt: Long? = null,
    val rewardClaimed: Boolean = false
)
