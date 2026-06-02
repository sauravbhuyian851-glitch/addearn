package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profiles WHERE id = :id")
    fun getUserById(id: Int): Flow<UserProfile?>

    @Query("SELECT * FROM user_profiles WHERE id = :id")
    suspend fun getUserByIdSync(id: Int): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE email = :email LIMIT 1")
    suspend fun getUserByEmailSync(email: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE username = :username LIMIT 1")
    suspend fun getUserByUsernameSync(username: String): UserProfile?

    @Query("SELECT * FROM user_profiles WHERE referralCode = :code LIMIT 1")
    suspend fun getUserByReferralCodeSync(code: String): UserProfile?

    @Query("SELECT * FROM user_profiles ORDER BY level DESC, xpPoints DESC LIMIT 50")
    fun getTopUsersByLevel(): Flow<List<UserProfile>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: UserProfile): Long

    @Update
    suspend fun updateUser(user: UserProfile)

    @Query("SELECT * FROM user_profiles")
    fun getAllUsers(): Flow<List<UserProfile>>

    @Query("SELECT COUNT(*) FROM user_profiles")
    fun getUserCount(): Flow<Int>
}

@Dao
interface BalanceDao {
    @Query("SELECT * FROM balances WHERE userId = :userId")
    fun getBalance(userId: Int): Flow<Balance?>

    @Query("SELECT * FROM balances WHERE userId = :userId")
    suspend fun getBalanceSync(userId: Int): Balance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: Balance)

    @Update
    suspend fun updateBalance(balance: Balance)

    @Query("UPDATE balances SET availableBalance = availableBalance + :amount, totalEarned = totalEarned + :amount, updatedAt = :time WHERE userId = :userId")
    suspend fun creditBalance(userId: Int, amount: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE balances SET availableBalance = availableBalance - :amount, totalWithdrawn = totalWithdrawn + :amount, updatedAt = :time WHERE userId = :userId")
    suspend fun withdrawBalance(userId: Int, amount: Double, time: Long = System.currentTimeMillis())

    @Query("UPDATE balances SET availableBalance = availableBalance + :amount, totalWithdrawn = totalWithdrawn - :amount, updatedAt = :time WHERE userId = :userId")
    suspend fun refundBalance(userId: Int, amount: Double, time: Long = System.currentTimeMillis())
}

@Dao
interface EarningDao {
    @Query("SELECT * FROM earnings WHERE userId = :userId ORDER BY createdAt DESC")
    fun getEarningsByUser(userId: Int): Flow<List<Earning>>

    @Query("SELECT SUM(amount) FROM earnings WHERE userId = :userId")
    fun getTotalEarningsByUser(userId: Int): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarning(earning: Earning)
}

@Dao
interface WithdrawalDao {
    @Query("SELECT * FROM withdrawals WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getWithdrawalsByUser(userId: Int): Flow<List<Withdrawal>>

    @Query("SELECT * FROM withdrawals ORDER BY requestedAt DESC")
    fun getAllWithdrawals(): Flow<List<Withdrawal>>

    @Query("SELECT * FROM withdrawals WHERE id = :id")
    suspend fun getWithdrawalById(id: String): Withdrawal?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(withdrawal: Withdrawal)

    @Update
    suspend fun updateWithdrawal(withdrawal: Withdrawal)
}

@Dao
interface PayMethodDao {
    @Query("SELECT * FROM pay_methods WHERE userId = :userId ORDER BY createdAt DESC")
    fun getMethodsByUser(userId: Int): Flow<List<PayMethod>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMethod(method: PayMethod)

    @Query("DELETE FROM pay_methods WHERE id = :id")
    suspend fun deleteMethod(id: String)
}

@Dao
interface AdSessionDao {
    @Query("SELECT * FROM ad_sessions WHERE userId = :userId ORDER BY startedAt DESC")
    fun getSessionsByUser(userId: Int): Flow<List<AdSession>>

    @Query("SELECT * FROM ad_sessions WHERE id = :id")
    suspend fun getSessionSync(id: String): AdSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AdSession)

    @Update
    suspend fun updateSession(session: AdSession)
}

@Dao
interface DailyStatsDao {
    @Query("SELECT * FROM user_daily_stats WHERE userId = :userId AND date = :date LIMIT 1")
    suspend fun getStatsSync(userId: Int, date: String): UserDailyStats?

    @Query("SELECT * FROM user_daily_stats WHERE userId = :userId AND date = :date")
    fun getStats(userId: Int, date: String): Flow<UserDailyStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserDailyStats)

    @Update
    suspend fun updateStats(stats: UserDailyStats)
}

@Dao
interface ReferralDao {
    @Query("SELECT * FROM referrals WHERE referrerId = :referrerId")
    fun getReferralsForReferrer(referrerId: Int): Flow<List<ReferralRel>>

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :referrerId")
    fun getReferralCount(referrerId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReferral(referral: ReferralRel)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getGoalsByUser(userId: Int): Flow<List<Goal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal)

    @Update
    suspend fun updateGoal(goal: Goal)
    
    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: String)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsByUser(userId: Int): Flow<List<Notification>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadCount(userId: Int): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)
}

@Dao
interface FraudDao {
    @Query("SELECT * FROM fraud_flags ORDER BY createdAt DESC")
    fun getAllFraudFlags(): Flow<List<FraudFlag>>

    @Query("SELECT * FROM fraud_flags WHERE userId = :userId ORDER BY createdAt DESC")
    fun getFlagsByUser(userId: Int): Flow<List<FraudFlag>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFlag(flag: FraudFlag)
}

@Dao
interface AdminSettingDao {
    @Query("SELECT * FROM admin_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): AdminSetting?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetting(setting: AdminSetting)
}

@Dao
interface ChallengeDao {
    @Query("SELECT * FROM challenges WHERE isActive = 1")
    fun getActiveChallenges(): Flow<List<Challenge>>

    @Query("SELECT * FROM challenges")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Query("SELECT * FROM user_challenges WHERE userId = :userId")
    fun getUserChallenges(userId: Int): Flow<List<UserChallenge>>

    @Query("SELECT * FROM user_challenges WHERE userId = :userId AND challengeId = :challengeId LIMIT 1")
    suspend fun getUserChallengeSync(userId: Int, challengeId: String): UserChallenge?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserChallenge(userChallenge: UserChallenge)
}
