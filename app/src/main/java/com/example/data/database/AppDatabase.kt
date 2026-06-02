package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserProfile::class,
        Balance::class,
        Earning::class,
        Withdrawal::class,
        PayMethod::class,
        AdSession::class,
        UserDailyStats::class,
        ReferralRel::class,
        Goal::class,
        Notification::class,
        FraudFlag::class,
        AdminSetting::class,
        Challenge::class,
        UserChallenge::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun balanceDao(): BalanceDao
    abstract fun earningDao(): EarningDao
    abstract fun withdrawalDao(): WithdrawalDao
    abstract fun payMethodDao(): PayMethodDao
    abstract fun adSessionDao(): AdSessionDao
    abstract fun dailyStatsDao(): DailyStatsDao
    abstract fun referralDao(): ReferralDao
    abstract fun goalDao(): GoalDao
    abstract fun notificationDao(): NotificationDao
    abstract fun fraudDao(): FraudDao
    abstract fun adminSettingDao(): AdminSettingDao
    abstract fun challengeDao(): ChallengeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "earnpulse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
