package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.viewmodel.EarnPulseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboard(
    viewModel: EarnPulseViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToPinSetup: () -> Unit,
    onLogout: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val balance by viewModel.currentBalance.collectAsState()

    // Redirect to PIN setup if they logged in but haven't saved a PIN
    LaunchedEffect(currentUser) {
        if (currentUser != null && currentUser?.withdrawalPin.isNullOrBlank()) {
            onNavigateToPinSetup()
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf(
        TabItem("Home", Icons.Default.Home, Icons.Outlined.Home),
        TabItem("Earn", Icons.Default.PlayArrow, Icons.Outlined.PlayArrow),
        TabItem("Wallet", Icons.Default.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet),
        TabItem("Network", Icons.Default.People, Icons.Outlined.People),
        TabItem("Leaderboard", Icons.Default.Leaderboard, Icons.Default.Leaderboard)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.linearGradient(listOf(BrandPrimary, BrandSecondary))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Bolt, "Logo", tint = BgBase, modifier = Modifier.size(18.dp))
                            }
                            Text(
                                "EarnPulse",
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary,
                                fontSize = 18.sp
                            )
                        }

                        // Status Tag (Free / Premium Indicator)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val isPremium = currentUser?.accountTier == "PREMIUM"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isPremium) BrandSecondary.copy(alpha = 0.15f) else BrandPrimary.copy(alpha = 0.08f))
                                    .border(1.dp, if (isPremium) BrandSecondary.copy(alpha = 0.4f) else BrandPrimary.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (isPremium) "👑 PREMIUM (2X)" else "⚡ STANDARD (1X)",
                                    color = if (isPremium) BrandSecondary else BrandPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            // Admin Entrance Button (Only if user has configured system rights)
                            // For simplicity, let any email ending in @admin.com or username "admin" go to admin dashboard
                            val isAdmin = currentUser?.username?.lowercase() == "admin" || currentUser?.email?.endsWith("@admin.com") == true
                            if (isAdmin) {
                                IconButton(
                                    onClick = onNavigateToAdmin,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(BrandAccent.copy(alpha = 0.12f))
                                ) {
                                    Icon(Icons.Default.AdminPanelSettings, "Admin Options", tint = BrandAccent, modifier = Modifier.size(20.dp))
                                }
                            }

                            // Logout Button
                            IconButton(
                                onClick = { viewModel.logout(onLogout) },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ErrorColor.copy(alpha = 0.1f))
                            ) {
                                Icon(Icons.Default.ExitToApp, "Disconnect", tint = ErrorColor, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgBase)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BgSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.navigationBarsPadding()
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) BrandPrimary else TextSecondary
                            )
                        },
                        label = {
                            Text(
                                tab.title,
                                color = if (isSelected) BrandPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = BrandPrimary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        },
        containerColor = BgBase
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeTab(viewModel)
                1 -> EarnTab(viewModel)
                2 -> WalletTab(viewModel)
                3 -> ReferralTab(viewModel)
                4 -> LeaderboardTab(viewModel)
            }
        }
    }
}

// ════════════════════════════════════════════
// 1. HOME TAB COMPOSABLE
// ════════════════════════════════════════════
@Composable
fun HomeTab(viewModel: EarnPulseViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val balance by viewModel.currentBalance.collectAsState()
    val dailyStats by viewModel.currentDailyStats.collectAsState()
    val earnings by viewModel.myEarnings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Welcome and User Title
        item {
            Column {
                Text(
                    text = "Welcome, ${currentUser?.fullName}!",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp
                )
                Text(
                    text = "ID Profile: @${currentUser?.username}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        // Animated Balance Counter Card
        item {
            val balanceAmount = balance?.availableBalance ?: 0.0
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("balance_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text("AVAILABLE BALANCE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(6.dp))

                    // Simulated ticker or clean high-precision monospaced text
                    Text(
                        text = String.format(Locale.US, "$%.4f", balanceAmount),
                        fontFamily = FontFamily.Monospace,
                        color = BrandPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(BorderColor))
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("TOTAL EARNED", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(String.format(Locale.US, "$%.4f", balance?.totalEarned ?: 0.0), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("TOTAL PAID OUT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                            Text(String.format(Locale.US, "$%.4f", balance?.totalWithdrawn ?: 0.0), color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Daily Ads Completed Tracker Ring Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    // Custom circular progress Canvas
                    val watchedCount = dailyStats?.adsWatched ?: 0
                    val targetCount = 30f
                    val animatedPercentage by animateFloatAsState(
                        targetValue = (watchedCount / targetCount).coerceIn(0f, 1f),
                        animationSpec = tween(1200)
                    )

                    Box(
                        modifier = Modifier.size(76.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = BorderColor,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                color = BrandPrimary,
                                startAngle = -90f,
                                sweepAngle = 360f * animatedPercentage,
                                useCenter = false,
                                style = Stroke(6.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$watchedCount", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Black)
                            Text("/30", color = TextSecondary, fontSize = 10.sp)
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Daily Quota Limits",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Watch 30 video ads per rolling day to unlock daily bonuses, level up quicker, and earn high commissions.",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // XP Level Progress Bar
        item {
            val userProfile = currentUser ?: return@item
            val requiredXp = userProfile.level * 100f
            val xpPercent = (userProfile.xpPoints / requiredXp).coerceIn(0f, 1f)

            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(BrandPrimary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Star, "Level", tint = BrandPrimary, modifier = Modifier.size(14.dp))
                            }
                            Text("LEVEL ${userProfile.level}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Text("${userProfile.xpPoints} / ${requiredXp.toInt()} XP", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(BorderColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(xpPercent)
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(BrandPrimary, BrandSecondary)))
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Multiplier Boost: +${String.format(Locale.US, "$%.5f", userProfile.level * 0.0002)} per view ad completed.",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Recent Earnings Ledger Ticker Title
        item {
            Text("Recent Earnings Logs", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (earnings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.Search, "Empty", tint = TextMuted, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No earning history yet.", color = TextSecondary, fontSize = 12.sp)
                        Text("Proceed to the Earn Tab to start viewing ad blocks!", color = TextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        } else {
            items(earnings.take(10)) { log ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(log.description, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text(
                            text = when (log.type) {
                                "AD_WATCH" -> "📺 Video Reward"
                                "SPIN_WHEEL" -> "🎯 Lucky Spin Reward"
                                "REFERRAL_COMMISSION" -> "👥 Downline Commission"
                                else -> "💵 Platform Bonus"
                            },
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Text(
                        text = "+${String.format(Locale.US, "$%.4f", log.amount)}",
                        color = BrandPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

// ════════════════════════════════════════════
// 2. EARN TAB COMPOSABLE (ADS + WHEEL)
// ════════════════════════════════════════════
@Composable
fun EarnTab(viewModel: EarnPulseViewModel) {
    val isWatchingAd by viewModel.isWatchingAd.collectAsState()
    val dailyStats by viewModel.currentDailyStats.collectAsState()

    var selectedMode by remember { mutableStateOf(0) } // 0: Video Ads, 1: Daily Spin Wheel

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Sub Tab Panel (Custom toggle layout)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BgSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedMode == 0) BrandPrimary else Color.Transparent)
                        .clickable { selectedMode = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "VIDEO BRIEFINGS",
                        color = if (selectedMode == 0) BgBase else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedMode == 1) BrandPrimary else Color.Transparent)
                        .clickable { selectedMode = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LUCKY SPIN",
                        color = if (selectedMode == 1) BgBase else TextSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when (selectedMode) {
                0 -> VideoAdsSection(viewModel)
                1 -> LuckySpinSection(viewModel)
            }
        }

        // Full Screen Video Watch Overlay System!
        if (isWatchingAd) {
            AdWatcherOverlay(viewModel)
        }
    }
}

@Composable
fun VideoAdsSection(viewModel: EarnPulseViewModel) {
    val context = LocalContext.current
    val dailyStats by viewModel.currentDailyStats.collectAsState()
    val adVerificationResult by viewModel.adVerificationResult.collectAsState()

    val adCampaigns = listOf(
        AdCampaign("AdUnit_483", "Apex Capital Holdings", "$0.0050+", "High Yield Arbitrage Strategy"),
        AdCampaign("AdUnit_821", "Prism Block Chains", "$0.0055+", "Decentralized Liquidity Staking Pools"),
        AdCampaign("AdUnit_294", "Velo Finance Solutions", "$0.0050+", "Micro Lending & Yield Automation Plans"),
        AdCampaign("AdUnit_903", "Summit Futures PLC", "$0.0060+", "Quantum Algorithmic Speculator Logs")
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        if (adVerificationResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.08f)),
                    border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Verified, "Verified", tint = BrandPrimary)
                        Text(
                            text = adVerificationResult ?: "",
                            color = BrandPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            Text("Select Available High-Paying Briefing", color = TextSecondary, fontSize = 13.sp)
        }

        items(adCampaigns) { campaign ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(campaign.title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(campaign.description, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(BrandPrimary.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(campaign.payout, color = BrandPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Duration: 30 secs", color = TextMuted, fontSize = 11.sp)
                    }
                }

                Button(
                    onClick = {
                        viewModel.startAdWatchSession(campaign.id) { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.testTag("watch_ad_btn_" + campaign.id)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.PlayArrow, "Play", tint = Color.White, modifier = Modifier.size(14.dp))
                        Text("View", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// VIDEO AD WATCH OVERLAY SCREEN
@Composable
fun AdWatcherOverlay(viewModel: EarnPulseViewModel) {
    val activeSession by viewModel.activeAdSession.collectAsState()
    val remainingTime by viewModel.adTimeRemaining.collectAsState()
    val progress by viewModel.adProgress.collectAsState()

    var tabViolations by remember { mutableStateOf(0) }
    var showOverlayWarning by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current

    // NATIVE PAUSED/MINIMIZED OBSERVER VIOLATION SECURITY CHECK
    // This perfectly matches standard PTC visibility cheating filters!
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                tabViolations++
                showOverlayWarning = true
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Dynamic timer ticker coroutine
    LaunchedEffect(Unit) {
        while (remainingTime > 0) {
            delay(1000)
            viewModel.tickAdTime(1)
        }
        // Dispatch completion upon clearing zero of countdown
        viewModel.completeActiveAdSession(tabViolations) {
            // Success call back
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .clickable(enabled = false) {}, // Intercept touch bypass
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SECURE EARNPULSE MEDIA PLAYER", color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(16.dp))

            // Simulated High-Contrast Ad Screen Presentation Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BgSurface)
                    .border(2.dp, BrandPrimary.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Bolt, "Fintech", tint = BrandPrimary, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "APEX MULTI-STRATEGY ASSET INDEX FUND",
                        color = TextPrimary,
                        fontWeight = FontWeight.Black,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Simulating high-value advertisement briefings.\nEarnings deposit directly as countdown expires.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // CIRCULAR countdown timer visualizer
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawArc(
                        color = BorderColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = BrandPrimary,
                        startAngle = -90f,
                        sweepAngle = 360f * (remainingTime / 30f),
                        useCenter = false,
                        style = Stroke(8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$remainingTime", color = TextPrimary, fontSize = 32.sp, fontWeight = FontWeight.Black)
                    Text("seconds", color = TextSecondary, fontSize = 11.sp)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
            Text("⚠️ DO NOT SWITCH APPS OR MINIMIZE SCREEN", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "Leaving the player voids credit. Cheat filters are active.",
                color = TextMuted,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (tabViolations > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Anti-Cheat Alert: $tabViolations background shift detected. Limit is 1.",
                    color = ErrorColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = { viewModel.forceCancelAd() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorColor),
                border = BorderStroke(1.dp, ErrorColor),
                shape = RoundedCornerShape(100.dp)
            ) {
                Text("CANCEL WATCHING", fontWeight = FontWeight.Bold)
            }
        }

        // Background Warning Box Overlay
        if (showOverlayWarning) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {
                    Button(
                        onClick = { showOverlayWarning = false },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(100.dp)
                    ) {
                        Text("RESUME", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                title = { Text("Anti-Cheat Shield", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = { Text("EarnPulse detected you left the media screen. Continued violation locks credit. Please resume.", color = TextSecondary) },
                containerColor = BgSurface,
                modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(28.dp))
            )
        }
    }
}

// LUCKY SPIN WHEEL COMPOSABLE
@Composable
fun LuckySpinSection(viewModel: EarnPulseViewModel) {
    val dailyStats by viewModel.currentDailyStats.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var isSpinning by remember { mutableStateOf(false) }
    var targetRotation by remember { mutableStateOf(0f) }
    val animatedRotation = remember { Animatable(0f) }

    var wonPrizeStr by remember { mutableStateOf<String?>(null) }

    val spinSegments = listOf("$0.001", "$0.003", "$0.005", "$0.010", "$0.020", "$0.050", "$0.100", "$0.500")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val used = dailyStats?.spinUsed ?: false
        if (used) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Schedule, "Lock", tint = BrandAccent)
                    Text("Daily spin exhausted! Resets at midnight UTC.", color = BrandAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // The Spinning Wheel Canvas
        Box(
            modifier = Modifier
                .size(220.dp)
                .rotate(animatedRotation.value),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = size.width / 2f
                val radius = size.width / 2f

                // Draw wheel sector segments back-panel colors
                val sweepAngle = 360f / spinSegments.size
                for (i in spinSegments.indices) {
                    val color = if (i % 2 == 0) BrandSecondary.copy(alpha = 0.8f) else BgElevated
                    drawArc(
                        color = color,
                        startAngle = i * sweepAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true
                    )
                }

                // Draw simple wheel division markers
                for (i in spinSegments.indices) {
                    val angleRad = Math.toRadians((i * sweepAngle).toDouble())
                    val outerX = center + radius * Math.cos(angleRad).toFloat()
                    val outerY = center + radius * Math.sin(angleRad).toFloat()
                    drawLine(
                        color = BorderColor,
                        start = Offset(center, center),
                        end = Offset(outerX, outerY),
                        strokeWidth = 3f
                    )
                }

                // Draw outer ring
                drawCircle(
                    color = BrandPrimary,
                    radius = radius,
                    style = Stroke(6.dp.toPx())
                )
            }

            // Central Hub Bolt
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(BrandPrimary)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Star, "Star", tint = BgBase, modifier = Modifier.size(20.dp))
            }
        }

        // Indicator Arrow pointing down
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Pointer",
            tint = BrandAccent,
            modifier = Modifier.size(36.dp).offset(y = (-14).dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (wonPrizeStr != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = BrandPrimary.copy(alpha = 0.1f)),
                border = BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.4f)),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "🏆 WINNER: $wonPrizeStr credited to wallet balance!",
                    color = BrandPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        Button(
            onClick = {
                if (used) {
                    Toast.makeText(context, "Spin again tomorrow!", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (!isSpinning) {
                    isSpinning = true
                    wonPrizeStr = null
                    scope.launch {
                        val randomTarget = (3600..7200).random().toFloat()
                        animatedRotation.animateTo(
                            targetValue = randomTarget,
                            animationSpec = tween(durationMillis = 4000, easing = CubicBezierEasing(0.1f, 0.8f, 0.2f, 1f))
                        )
                        viewModel.spinLuckyWheel(
                            onSuccess = { prize ->
                                isSpinning = false
                                wonPrizeStr = String.format(Locale.US, "$%.3f", prize)
                            },
                            onFail = { err ->
                                isSpinning = false
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
            shape = RoundedCornerShape(12.dp),
            enabled = !isSpinning && !used,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
                .testTag("spin_lucky_wheel_btn")
        ) {
            if (isSpinning) {
                CircularProgressIndicator(color = BgBase, modifier = Modifier.size(20.dp))
            } else {
                Text("LUCKY SPIN", color = BgBase, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ════════════════════════════════════════════
// 3. WALLET TAB COMPOSABLE (WITHDRAWALS)
// ════════════════════════════════════════════
@Composable
fun WalletTab(viewModel: EarnPulseViewModel) {
    val balance by viewModel.currentBalance.collectAsState()
    val payoutMethods by viewModel.myPaymentMethods.collectAsState()
    val withdrawals by viewModel.myWithdrawals.collectAsState()
    val withdrawalMsg by viewModel.withdrawalMsg.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var methodType by remember { mutableStateOf("PAYPAL") }
    var methodLabel by remember { mutableStateOf("") }
    var methodDetail by remember { mutableStateOf("") }

    var selectedMethodForWithdraw by remember { mutableStateOf<PayMethod?>(null) }
    var withdrawAmount by remember { mutableStateOf("") }
    var withdrawPin by remember { mutableStateOf("") }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        // Earning metrics
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text("EARNED BALANCE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                String.format(Locale.US, "$%.4f", balance?.availableBalance ?: 0.0),
                                color = BrandPrimary,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Ledger verification info
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessColor.copy(alpha = 0.12f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("MIN LIMIT: $5", color = SuccessColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Error message banner for payments
        if (withdrawalMsg != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = withdrawalMsg ?: "",
                            color = ErrorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearWithdrawMsg() }) {
                            Icon(Icons.Default.Close, "Dismiss", tint = ErrorColor)
                        }
                    }
                }
            }
        }

        // Payout methods management CRUD
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Select Payout Destination", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.testTag("add_payment_method")
                ) {
                    Icon(Icons.Default.Add, "Add", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Method", color = TextPrimary, fontSize = 12.sp)
                }
            }
        }

        if (payoutMethods.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No payment methods configured.", color = TextSecondary, fontSize = 13.sp)
                        Text("Configure a secure payout method to cashout commissions.", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        } else {
            items(payoutMethods) { method ->
                val isSelected = selectedMethodForWithdraw?.id == method.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) BrandPrimary.copy(alpha = 0.05f) else BgSurface)
                        .border(1.dp, if (isSelected) BrandPrimary else BorderColor, RoundedCornerShape(12.dp))
                        .clickable { selectedMethodForWithdraw = method }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedMethodForWithdraw = method },
                            colors = RadioButtonDefaults.colors(selectedColor = BrandPrimary)
                        )
                        Column {
                            Text(method.label, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${method.methodType} - ${method.details}", color = TextSecondary, fontSize = 12.sp)
                        }
                    }

                    IconButton(onClick = { viewModel.removeReceiveMethod(method.id) }) {
                        Icon(Icons.Default.Delete, "Delete", tint = ErrorColor)
                    }
                }
            }
        }

        // Ledger Withdrawal entry Form card
        if (selectedMethodForWithdraw != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("DISBURSE FUNDS REQUEST", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = withdrawAmount,
                            onValueChange = { withdrawAmount = it },
                            label = { Text("Amount to Cash Out ($)", color = TextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_amount")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = withdrawPin,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) withdrawPin = it },
                            label = { Text("4-Digit Security PIN", color = TextSecondary) },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = textFieldColors(),
                            modifier = Modifier.fillMaxWidth().testTag("withdraw_pin")
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val amtNum = withdrawAmount.toDoubleOrNull() ?: 0.0
                                viewModel.executeWithdrawRequest(
                                    amount = amtNum,
                                    method = selectedMethodForWithdraw?.methodType ?: "",
                                    details = selectedMethodForWithdraw?.details ?: "",
                                    pin = withdrawPin
                                ) {
                                    Toast.makeText(context, "Withdrawal queued successfully!", Toast.LENGTH_LONG).show()
                                    withdrawAmount = ""
                                    withdrawPin = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                            shape = RoundedCornerShape(100.dp),
                            modifier = Modifier.fillMaxWidth().testTag("submit_withdraw_btn")
                        ) {
                            Text("SUBMIT PAYOUT REQUEST", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Historical Ledger of User Payouts
        item {
            Text("Your Disbursal History", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (withdrawals.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No historic withdrawals found.", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(withdrawals) { wr ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Disbursment index: $${wr.amount}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("via ${wr.method} • Fees: $${wr.fee}", color = TextSecondary, fontSize = 12.sp)
                        if (wr.rejectionReason != null) {
                            Text("Decline reason: ${wr.rejectionReason}", color = ErrorColor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Colored status banner
                    val statusColor = when (wr.status) {
                        "APPROVED" -> SuccessColor
                        "REJECTED" -> ErrorColor
                        else -> WarningColor
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(wr.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Modal Add Method dialog block
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Receive Channel", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("PAYPAL", "WISE", "CRYPTO").forEach { choice ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (methodType == choice) BrandPrimary else BgElevated)
                                    .clickable { methodType = choice }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(choice, color = if (methodType == choice) Color.White else TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = methodLabel,
                        onValueChange = { methodLabel = it },
                        label = { Text("Channel Alias (e.g. My Paypal)", color = TextSecondary) },
                        colors = textFieldColors()
                    )

                    OutlinedTextField(
                        value = methodDetail,
                        onValueChange = { methodDetail = it },
                        label = { Text("Payment Details / Address", color = TextSecondary) },
                        colors = textFieldColors()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (methodLabel.isNotBlank() && methodDetail.isNotBlank()) {
                            viewModel.addReceiveMethod(methodType, methodLabel, methodDetail) {
                                showAddDialog = false
                                methodLabel = ""
                                methodDetail = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary)
                ) {
                    Text("SAVE VALUE", color = BgBase, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = BgSurface,
            modifier = Modifier.border(1.dp, BorderColor, RoundedCornerShape(28.dp))
        )
    }
}

// ════════════════════════════════════════════
// 4. REFERRAL TAB COMPOSABLE (AFFILIATE)
// ════════════════════════════════════════════
@Composable
fun ReferralTab(viewModel: EarnPulseViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val referralCount by viewModel.referralCount.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Affiliate Network Center", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text("Generate L1 & L2 income by circulating downline links", color = TextSecondary, fontSize = 12.sp)
            }
        }

        // Referral Code Presenter Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BgSurface),
                border = BorderStroke(1.dp, BorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("YOUR INVITE CODE", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = currentUser?.referralCode ?: "PULSE000",
                        color = BrandPrimary,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "Share this code during registration. Ref code gets 10% L1 commission on ad-watching and 3% L2 commissions instantly.",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Stats summary
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
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL REFERRALS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$referralCount", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = BgSurface),
                    border = BorderStroke(1.dp, BorderColor)
                ) {
                    Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PASSIVE INCOME (EST)", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$0.1250", color = BrandPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        item {
            Text("Level 1 & Level 2 Reward Rules", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                CommissionRuleItem("👑 L1 direct invite", "Earn 10% commission of every ad package completed by your direct invited friends.")
                CommissionRuleItem("💎 L2 indirect network", "Earn 3% commission on every ad briefings viewed by second tier downline invites.")
            }
        }
    }
}

@Composable
fun CommissionRuleItem(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BgSurface)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandPrimary.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.TrendingUp, "Up", tint = BrandPrimary, modifier = Modifier.size(18.dp))
        }
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(desc, color = TextSecondary, fontSize = 11.sp, lineHeight = 16.sp)
        }
    }
}

// ════════════════════════════════════════════
// 5. LEADERBOARD TAB COMPOSABLE
// ════════════════════════════════════════════
@Composable
fun LeaderboardTab(viewModel: EarnPulseViewModel) {
    val topUsers by viewModel.leaderboardUsers.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
    ) {
        item {
            Column {
                Text("Global Performance Listings", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 22.sp)
                Text("The top 50 active earners by current account levels.", color = TextSecondary, fontSize = 12.sp)
            }
        }

        item { Spacer(modifier = Modifier.height(4.dp)) }

        if (topUsers.isEmpty()) {
            item {
                Text("Computing index ranks...", color = TextSecondary)
            }
        } else {
            items(topUsers.take(15)) { indexUser ->
                // Visual rank
                val rankIndex = topUsers.indexOf(indexUser) + 1
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Rank Indicator badge
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    when (rankIndex) {
                                        1 -> BrandAccent
                                        2 -> BrandSecondary
                                        3 -> BrandPrimary
                                        else -> BorderColor
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rankIndex",
                                color = if (rankIndex <= 3) Color.White else TextPrimary,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Column {
                            Text(indexUser.fullName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("@${indexUser.username} • Country: ${indexUser.countryCode}", color = TextSecondary, fontSize = 11.sp)
                        }
                    }

                    // Level label Info
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(BrandPrimary.copy(alpha = 0.1f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("LEVEL ${indexUser.level}", color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class TabItem(val title: String, val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector, val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector)
data class AdCampaign(val id: String, val title: String, val payout: String, val description: String)
