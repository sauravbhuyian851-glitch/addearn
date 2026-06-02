package com.example.data.repository

import com.example.data.database.BalanceDao
import com.example.data.database.NotificationDao
import com.example.data.database.PayMethodDao
import com.example.data.database.UserDao
import com.example.data.database.WithdrawalDao
import com.example.data.model.Notification
import com.example.data.model.PayMethod
import com.example.data.model.Withdrawal
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class WithdrawalRepository(
    private val userDao: UserDao,
    private val balanceDao: BalanceDao,
    private val withdrawalDao: WithdrawalDao,
    private val payMethodDao: PayMethodDao,
    private val notificationDao: NotificationDao
) {
    val MIN_WITHDRAWAL_AMOUNT = 5.0 // $5.0 minimum payout config

    fun getWithdrawals(userId: Int): Flow<List<Withdrawal>> {
        return withdrawalDao.getWithdrawalsByUser(userId)
    }

    fun getAllWithdrawals(): Flow<List<Withdrawal>> {
        return withdrawalDao.getAllWithdrawals()
    }

    fun getPaymentMethods(userId: Int): Flow<List<PayMethod>> {
        return payMethodDao.getMethodsByUser(userId)
    }

    suspend fun savePaymentMethod(userId: Int, type: String, label: String, details: String) {
        val method = PayMethod(
            id = UUID.randomUUID().toString(),
            userId = userId,
            methodType = type,
            label = label,
            details = details
        )
        payMethodDao.insertMethod(method)
    }

    suspend fun deletePaymentMethod(methodId: String) {
        payMethodDao.deleteMethod(methodId)
    }

    suspend fun requestWithdrawal(
        userId: Int,
        amount: Double,
        method: String,
        details: String,
        enteredPin: String
    ): Result<Withdrawal> {
        val user = userDao.getUserByIdSync(userId) ?: return Result.failure(Exception("UserProfile not found"))
        val balance = balanceDao.getBalanceSync(userId) ?: return Result.failure(Exception("Balance record not found"))

        if (amount < MIN_WITHDRAWAL_AMOUNT) {
            return Result.failure(Exception("Minimum withdrawal is $$MIN_WITHDRAWAL_AMOUNT"))
        }

        if (balance.availableBalance < amount) {
            return Result.failure(Exception("Insufficient balance. You have $${balance.availableBalance}"))
        }

        if (user.withdrawalPin != enteredPin) {
            return Result.failure(Exception("Incorrect 4-digit security PIN."))
        }

        // Calculate transaction charges
        val fee = when (method.uppercase()) {
            "PAYPAL" -> amount * 0.045 // 4.5% processing
            "WISE" -> amount * 0.020   // 2% processing
            "CRYPTO" -> 1.0            // Flat $1.0 USDT gas fee
            else -> 0.0
        }

        val netAmount = amount - fee
        if (netAmount <= 0) {
            return Result.failure(Exception("Amount is too small to cover transfer fees."))
        }

        val newWithdrawalId = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()

        val withdrawal = Withdrawal(
            id = newWithdrawalId,
            userId = userId,
            amount = amount,
            fee = fee,
            netAmount = netAmount,
            method = method,
            paymentDetails = details,
            status = "PENDING",
            requestedAt = now
        )

        // Debit Atomic Operation
        balanceDao.withdrawBalance(userId, amount, now)

        withdrawalDao.insertWithdrawal(withdrawal)

        // AUTO-APPROVE RULE: If amount < $20, process automatically to reward small users!
        if (amount < 20.0) {
            approveWithdrawalSync(newWithdrawalId, "Auto-approved small transfer under $20")
        } else {
            notificationDao.insertNotification(
                Notification(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    type = "WITHDRAWAL",
                    title = "Withdrawal Requested",
                    message = "Your request of $$amount via $method is in queue. Security clearances take 12-24 hours."
                )
            )
        }

        return Result.success(withdrawal)
    }

    suspend fun approveWithdrawalSync(withdrawalId: String, adminNotice: String?): Boolean {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId) ?: return false
        if (withdrawal.status != "PENDING") return false

        val updated = withdrawal.copy(
            status = "APPROVED",
            processedAt = System.currentTimeMillis(),
            adminNote = adminNotice,
            referenceId = "TXN_" + (10000000..99999999).random().toString()
        )
        withdrawalDao.insertWithdrawal(updated)

        // Notify user
        notificationDao.insertNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                userId = withdrawal.userId,
                type = "WITHDRAWAL",
                title = "Withdrawal Disbursed",
                message = "Hooray! Real money payment of $${updated.netAmount} successfully sent via ${updated.method}. Ref: ${updated.referenceId}"
            )
        )
        return true
    }

    suspend fun rejectWithdrawalSync(withdrawalId: String, reason: String): Boolean {
        val withdrawal = withdrawalDao.getWithdrawalById(withdrawalId) ?: return false
        if (withdrawal.status != "PENDING") return false

        // Refund User and mark Rejected
        val now = System.currentTimeMillis()
        val updated = withdrawal.copy(
            status = "REJECTED",
            processedAt = now,
            rejectionReason = reason
        )
        withdrawalDao.insertWithdrawal(updated)

        // Execute Balance Refund
        balanceDao.refundBalance(withdrawal.userId, withdrawal.amount, now)

        // Notify User
        notificationDao.insertNotification(
            Notification(
                id = UUID.randomUUID().toString(),
                userId = withdrawal.userId,
                type = "WITHDRAWAL",
                title = "Withdrawal Declined",
                message = "Your transfer of $${withdrawal.amount} was rejected. Reason: $reason. Credits returned to you."
            )
        )
        return true
    }
}
