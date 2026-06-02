package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.EarnPulseViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConsole(
    viewModel: EarnPulseViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    val users by viewModel.adminAllUsers.collectAsState()
    val withdrawals by viewModel.adminAllWithdrawals.collectAsState()
    val fraudLogs by viewModel.adminFraudLogs.collectAsState()
    val userCount by viewModel.adminCountUsers.collectAsState()

    var activeSubMenu by remember { mutableStateOf(0) } // 0: Withdrawals, 1: Users, 2: Fraud Center

    var showRejectDialogForId by remember { mutableStateOf<String?>(null) }
    var rejectionReason by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Core Admin Console", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgBase)
            )
        },
        containerColor = BgBase
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            // HIGH END FINTECH METRIC BARS PAGE (KPIs)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("TOTAL USERS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text("$userCount", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    val pendingSum = withdrawals.filter { it.status == "PENDING" }.sumOf { it.amount }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = BgSurface),
                        border = BorderStroke(1.dp, BorderColor)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("QUEUED PAYOUTS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(String.format(Locale.US, "$%.2f", pendingSum), color = BrandAccent, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Sub Navigation for Admin options
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(100.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(100.dp))
                        .padding(4.dp)
                ) {
                    val adminTabs = listOf("Payout Queue", "User Base", "Fraud Alerts")
                    adminTabs.forEachIndexed { i, title ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (activeSubMenu == i) BrandPrimary.copy(alpha = 0.12f) else Color.Transparent)
                                .border(1.dp, if (activeSubMenu == i) BrandPrimary.copy(alpha = 0.25f) else Color.Transparent, RoundedCornerShape(100.dp))
                                .clickable { activeSubMenu = i }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                color = if (activeSubMenu == i) BrandPrimary else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Content depending on sub-menu
            when (activeSubMenu) {
                0 -> { // PAYOUT QUEUE
                    val pendingWithdrawals = withdrawals.filter { it.status == "PENDING" }
                    if (pendingWithdrawals.isEmpty()) {
                        item {
                            EmptyAdminState("No pending transfers found.", "All payouts are cleared.")
                        }
                    } else {
                        items(pendingWithdrawals) { wrLog ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgSurface),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Cashout request amount: $${wrLog.amount}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("Net Due: $${wrLog.netAmount} • Fee: $${wrLog.fee}", color = TextSecondary, fontSize = 12.sp)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(WarningColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(wrLog.method, color = WarningColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Receiver destination: ${wrLog.paymentDetails}", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                viewModel.adminApproveTransfer(wrLog.id)
                                                Toast.makeText(context, "Payout authorized & sent!", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = SuccessColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("approve_btn_" + wrLog.id)
                                        ) {
                                            Icon(Icons.Default.Check, "Approve", modifier = Modifier.size(16.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("AUTHORIZE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }

                                        Button(
                                            onClick = { showRejectDialogForId = wrLog.id },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f).testTag("reject_btn_" + wrLog.id)
                                        ) {
                                            Icon(Icons.Default.Block, "Reject", modifier = Modifier.size(16.dp), tint = Color.White)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("DECLINE", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> { // USER MANAGEMENT BASE LISTS
                    if (users.isEmpty()) {
                        item {
                            EmptyAdminState("No users registered", "Check database nodes.")
                        }
                    } else {
                        items(users) { userProfile ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgSurface),
                                border = BorderStroke(1.dp, BorderColor)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(userProfile.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                            Text("@${userProfile.username} • Level ${userProfile.level}", color = TextSecondary, fontSize = 12.sp)
                                            Text(userProfile.email, color = TextMuted, fontSize = 11.sp)
                                        }

                                        val isBanned = userProfile.accountStatus == "BANNED"
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isBanned) ErrorColor.copy(alpha = 0.12f) else SuccessColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (isBanned) "BANNED" else "ACTIVE_OK",
                                                color = if (isBanned) ErrorColor else SuccessColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val isPremium = userProfile.accountTier == "PREMIUM"
                                        Button(
                                            onClick = {
                                                val nextTier = if (isPremium) "FREE" else "PREMIUM"
                                                viewModel.adminTierUpgrade(userProfile.id, nextTier)
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = BgElevated),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (isPremium) "DOWNGRADE" else "UPGRADE PREMIUM",
                                                color = BrandPrimary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp
                                            )
                                        }

                                        if (userProfile.accountStatus != "BANNED") {
                                            Button(
                                                onClick = {
                                                    viewModel.adminBanUser(userProfile.id)
                                                    Toast.makeText(context, "User profile locked instantly.", Toast.LENGTH_SHORT).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ErrorColor.copy(alpha = 0.12f)),
                                                border = BorderStroke(1.dp, ErrorColor),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("BAN ACCOUNT", color = ErrorColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> { // FRAUD LOGGER ALERTS
                    if (fraudLogs.isEmpty()) {
                        item {
                            EmptyAdminState("No fraud patterns flagged", "Users are browsing legitimately.")
                        }
                    } else {
                        items(fraudLogs) { log ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgSurface),
                                border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.12f))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Icon(Icons.Default.Warning, "Flag", tint = ErrorColor, modifier = Modifier.size(16.dp))
                                            Text(log.flagType, color = ErrorColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Text(
                                            text = if (log.resolved) "RESOLVED" else "CRITICAL",
                                            color = if (log.resolved) SuccessColor else ErrorColor,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("UserId: ${log.userId} flagged on network audit", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(log.details, color = TextSecondary, fontSize = 12.sp, lineHeight = 16.sp, modifier = Modifier.padding(top = 2.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject Dialogue pop up
    if (showRejectDialogForId != null) {
        AlertDialog(
            onDismissRequest = { showRejectDialogForId = null },
            title = { Text("Reject Transfer Request", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Provide a reason why this transfer request is rejected. This refund will automatically readdress the user's ledger wallet balance.", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = rejectionReason,
                        onValueChange = { rejectionReason = it },
                        label = { Text("Rejection Reason", color = TextSecondary) },
                        colors = textFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val reasonText = if (rejectionReason.isBlank()) "Flagged by security audits. Multiple IPs suspected." else rejectionReason
                        showRejectDialogForId?.let { id ->
                            viewModel.adminRejectTransfer(id, reasonText)
                        }
                        showRejectDialogForId = null
                        rejectionReason = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor)
                ) {
                    Text("DECLINE & REFUND", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialogForId = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = BgElevated,
            modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(28.dp))
        )
    }
}

@Composable
fun EmptyAdminState(title: String, desc: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = BgSurface),
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.VerifiedUser, "System Checked", tint = TextMuted, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(desc, color = TextMuted, fontSize = 12.sp, textAlign = TextAlign.Center)
        }
    }
}
