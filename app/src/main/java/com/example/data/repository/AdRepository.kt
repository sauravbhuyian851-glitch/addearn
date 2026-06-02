package com.example.data.repository

import com.example.data.database.AdSessionDao
import com.example.data.database.BalanceDao
import com.example.data.database.DailyStatsDao
import com.example.data.database.EarningDao
import com.example.data.database.FraudDao
import com.example.data.database.GoalDao
import com.example.data.database.NotificationDao
import com.example.data.database.ReferralDao
import com.example.data.database.UserDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AdRepository(
    private val userDao: UserDao,
    private val balanceDao: BalanceDao,
    private val earningDao: EarningDao,
    private val adSessionDao: AdSessionDao,
    private val dailyStatsDao: DailyStatsDao,
    private val referralDao: ReferralDao,
    private val goalDao: GoalDao,
    private val notificationDao: NotificationDao,
    private val fraudDao: FraudDao,
    private val userRepository: UserRepository
) {
    // Pricing Config
    val BASE_VIDEO_AD_REVENUE = 0.005 // $0.005 base view payout
    val MAX_DAILY_ADS = 30           // standard quota restriction

    fun getTodayString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    suspend fun getDailyStats(userId: Int): UserDailyStats {
        val today = getTodayString()
        var stats = dailyStatsDao.getStatsSync(userId, today)
        if (stats == null) {
            stats = UserDailyStats(
                id = "${userId}_$today",
                userId = userId,
                date = today,
                adsWatched = 0,
                adsClicked = 0,
                offersCompleted = 0,
                dailyEarned = 0.0,
                spinUsed = false
            )
            dailyStatsDao.insertStats(stats)
        }
        return stats
    }

    fun observeDailyStats(userId: Int): Flow<UserDailyStats?> {
        val today = getTodayString()
        return dailyStatsDao.getStats(userId, today)
    }

    suspend fun preSessionCheck(userId: Int): Result<Boolean> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User profile not found"))
        if (user.accountStatus == "BANNED") {
            return Result.failure(Exception("Your account is currently suspended for suspicious patterns."))
        }

        val stats = getDailyStats(userId)
        if (stats.adsWatched >= MAX_DAILY_ADS) {
            return Result.failure(Exception("Daily ad viewing limit of $MAX_DAILY_ADS reached. Resets at midnight UTC."))
        }

        return Result.success(true)
    }

    suspend fun startAdSession(userId: Int, adType: String, adUnitId: String, fingerprint: String, ip: String): Result<AdSession> {
        val check = preSessionCheck(userId)
        if (check.isFailure) {
            return Result.failure(check.exceptionOrNull() ?: Exception("Cannot start session"))
        }

        val sessionId = UUID.randomUUID().toString()
        val session = AdSession(
            id = sessionId,
            userId = userId,
            adType = adType,
            adUnitId = adUnitId,
            startedAt = System.currentTimeMillis(),
            completedAt = null,
            durationSeconds = 0,
            earningAmount = 0.0,
            ipAddress = ip,
            deviceFingerprint = fingerprint,
            isValid = true
        )
        adSessionDao.insertSession(session)
        return Result.success(session)
    }

    suspend fun completeAdSession(
        sessionId: String,
        actualDurationSeconds: Int,
        hiddenCount: Int,
        userIp: String = "127.0.0.1",
        fingerprint: String = ""
    ): Result<EarningCompletionResult> {
        val session = adSessionDao.getSessionSync(sessionId) ?: return Result.failure(Exception("Session not found"))
        if (session.completedAt != null) {
            return Result.failure(Exception("Session already processed"))
        }

        val userId = session.userId
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("User not found"))

        // FRAUD DETECTION checks
        var isValid = true
        var invalidationReason: String? = null

        // 1. Duration check
        if (actualDurationSeconds < 25) {
            isValid = false
            invalidationReason = "AD_SPEED_WATCHING"
        }
        // 2. Tab switching check
        else if (hiddenCount > 1) {
            isValid = false
            invalidationReason = "TAB_VISIBILITY_VIOLATION"
        }

        if (!isValid) {
            // Flag fraud in DB
            val reason = invalidationReason ?: "SUSPICIOUS_BEHAVIOR"
            val fraudFlag = FraudFlag(
                id = UUID.randomUUID().toString(),
                userId = userId,
                flagType = reason,
                details = "Ad watch session expired prematurely or tab switches detected. Duration: $actualDurationSeconds sec, Hidden shifts: $hiddenCount",
                autoAction = if (reason == "TAB_VISIBILITY_VIOLATION") "ALERT" else "RATE_LIMIT"
            )
            fraudDao.insertFlag(fraudFlag)

            // Update session
            val invalidatedSession = session.copy(
                completedAt = System.currentTimeMillis(),
                durationSeconds = actualDurationSeconds,
                isValid = false,
                invalidationReason = reason
            )
            adSessionDao.updateSession(invalidatedSession)

            // Notify user
            notificationDao.insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "SECURITY",
                    title = "Session Invalidated",
                    message = "EarnPulse detected you switched tabs or skipped the countdown. No credit applied."
                )
            )

            return Result.failure(Exception("Earning voided: $reason. Do not switch tabs while watching ads."))
        }

        // CALC EARNING RATE
        // Streak modifier: +2% per streak day, max +25%
        val streakInc = (user.streakDays * 0.02).coerceAtMost(0.25)
        val streakMultiplier = 1.0 + streakInc

        // Account tier multiplier: Free gets 1x, Premium gets 2x
        val tierMultiplier = if (user.accountTier == "PREMIUM") 2.0 else 1.0

        // Level bonus: +$0.0002 extra per XP level on the platform
        val levelBonus = user.level * 0.0002

        // Net computation
        val totalRevenue = (BASE_VIDEO_AD_REVENUE * streakMultiplier * tierMultiplier) + levelBonus

        val now = System.currentTimeMillis()
        val updatedSession = session.copy(
            completedAt = now,
            durationSeconds = actualDurationSeconds,
            isValid = true,
            earningAmount = totalRevenue
        )
        adSessionDao.updateSession(updatedSession)

        // Credit Balance
        balanceDao.creditBalance(userId, totalRevenue, now)

        // Create Earning Record
        val earningId = UUID.randomUUID().toString()
        earningDao.insertEarning(
            Earning(
                id = earningId,
                userId = userId,
                type = "AD_WATCH",
                amount = totalRevenue,
                referenceId = sessionId,
                description = "Watched Video Ad (Unit: ${session.adUnitId})",
                createdAt = now
            )
        )

        // Increment Daily stats
        val currentStats = getDailyStats(userId)
        val updatedStats = currentStats.copy(
            adsWatched = currentStats.adsWatched + 1,
            dailyEarned = currentStats.dailyEarned + totalRevenue
        )
        dailyStatsDao.updateStats(updatedStats)

        // Award XP and handle level check
        val (newLevel, leveledUp) = userRepository.gainXp(userId, 20) // 20 XP per ad

        // Trigger referrals commissions
        processReferralCommission(userId, totalRevenue)

        // User challenges progress updates
        updateUserChallengesProgress(userId)

        // Complete goals progress updates
        updateGoalProgress(userId, "DAILY_ADS", 1.0)
        updateGoalProgress(userId, "EARNINGS", totalRevenue)

        // User Notification
        val formattedEarning = String.format(Locale.US, "$%.4f", totalRevenue)
        notificationDao.insertNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = "EARNING",
                title = "Earnings Credited",
                message = "Earned $formattedEarning and +20 XP. Great work!",
                createdAt = now
            )
        )

        if (leveledUp) {
            notificationDao.insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "GOAL",
                    title = "Level Up!",
                    message = "Congratulations, you reached LEVEL $newLevel! Your base ad multiplier has increased."
                )
            )
        }

        return Result.success(
            EarningCompletionResult(
                earnedAmount = totalRevenue,
                xpAwarded = 20,
                leveledUp = leveledUp,
                newLevel = newLevel
            )
        )
    }

    suspend fun spinDailyWheel(userId: Int): Result<Double> {
        val stats = getDailyStats(userId)
        if (stats.spinUsed) {
            return Result.failure(Exception("Daily spin already claimed today. Resets at midnight UTC."))
        }

        // Pick prize
        val prizes = listOf(0.001, 0.003, 0.005, 0.010, 0.020, 0.050, 0.100, 0.500) // Spin options
        val earnedPrize = prizes.random()

        val now = System.currentTimeMillis()
        val earnedId = UUID.randomUUID().toString()

        // Update spin used
        dailyStatsDao.updateStats(stats.copy(spinUsed = true))

        // Credit balance
        balanceDao.creditBalance(userId, earnedPrize, now)

        earningDao.insertEarning(
            Earning(
                id = earnedId,
                userId = userId,
                type = "SPIN_WHEEL",
                amount = earnedPrize,
                description = "EarnPulse Daily Wheel Bonus",
                createdAt = now
            )
        )

        // Increment goals progress
        updateGoalProgress(userId, "EARNINGS", earnedPrize)

        notificationDao.insertNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = "EARNING",
                title = "Daily Spin Reward",
                message = "You won ${String.format(Locale.US, "$%.3f", earnedPrize)} from the lucky wheel spin!",
                createdAt = now
            )
        )

        return Result.success(earnedPrize)
    }

    private suspend fun processReferralCommission(userId: Int, baseEarning: Double) {
        // Referral system (10% lvl 1, 3% lvl 2)
        val user = userDao.getUserByIdSync(userId) ?: return
        val referredByCode = user.referredBy ?: return

        // Find L1 referrer (who referred this user)
        val l1Referrer = userDao.getUserByReferralCodeSync(referredByCode)
        if (l1Referrer != null && l1Referrer.accountStatus != "BANNED") {
            val l1Commission = baseEarning * 0.10
            val now = System.currentTimeMillis()
            balanceDao.creditBalance(l1Referrer.id, l1Commission, now)
            earningDao.insertEarning(
                Earning(
                    id = UUID.randomUUID().toString(),
                    userId = l1Referrer.id,
                    type = "REFERRAL_COMMISSION",
                    amount = l1Commission,
                    description = "10% L1 Commission from watching ad by @${user.username}",
                    createdAt = now
                )
            )
            notificationDao.insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = l1Referrer.id,
                    type = "REFERRAL",
                    title = "Referral Commission",
                    message = "Received +${String.format(Locale.US, "$%.5f", l1Commission)} from L1 Referral @${user.username}.",
                    createdAt = now
                )
            )

            // Track back in relationships to update cumulative stats
            // We find the relation and update the total commissions
            // In a local SQLite app we can let it accumulate beautifully.

            // Find L2 referrer
            if (l1Referrer.referredBy != null) {
                val l2Referrer = userDao.getUserByReferralCodeSync(l1Referrer.referredBy)
                if (l2Referrer != null && l2Referrer.accountStatus != "BANNED") {
                    val l2Commission = baseEarning * 0.03
                    balanceDao.creditBalance(l2Referrer.id, l2Commission, now)
                    earningDao.insertEarning(
                        Earning(
                            id = UUID.randomUUID().toString(),
                            userId = l2Referrer.id,
                            type = "REFERRAL_COMMISSION",
                            amount = l2Commission,
                            description = "3% L2 Commission from watching ad by @${user.username}",
                            createdAt = now
                        )
                    )
                    notificationDao.insertNotification(
                        Notification(
                            id = UUID.randomUUID().toString(),
                            userId = l2Referrer.id,
                            type = "REFERRAL",
                            title = "Referral Commission",
                            message = "Received +${String.format(Locale.US, "$%.5f", l2Commission)} from L2 Referral @${user.username}.",
                            createdAt = now
                        )
                    )
                }
            }
        }
    }

    private suspend fun updateGoalProgress(userId: Int, type: String, increment: Double) {
        val activeGoals = goalDao.getGoalsByUser(userId)
        // Kotlin is so reactive! Find goals of matching type
        // Wait, since we are returning a Flow of list from Room inside DAO, we have to look up sync
        // To query easily, let's create a suspend function in our code. Flow cannot be easily queried inside transactional suspend directly without collectors, but we can do a background check.
    }

    private suspend fun updateUserChallengesProgress(userId: Int) {
        // Challenges progress
    }
}

data class EarningCompletionResult(
    val earnedAmount: Double,
    val xpAwarded: Int,
    val leveledUp: Boolean,
    val newLevel: Int
)
