package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun WelcomeScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
    ) {
        // Fintech Animated Grid Background
        FintechGridBackground()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Four-dot Google icon accent
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF4285F4))) // Blue
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEA4335))) // Red
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFFBBC05))) // Yellow
                            Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF34A853))) // Green
                        }
                        Text(
                            text = "EarnPulse",
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            letterSpacing = (-0.5).sp
                        )
                    }

                    Button(
                        onClick = onNavigateToLogin,
                        colors = ButtonDefaults.buttonColors(containerColor = BgElevated),
                        border = BorderStroke(1.dp, BorderColor),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.testTag("landing_signin")
                    ) {
                        Text("Sign In", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Hero CTA Block
            item {
                Spacer(modifier = Modifier.height(28.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandPrimary.copy(alpha = 0.08f))
                            .border(1.dp, BrandPrimary.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.TrendingUp, "Trending", tint = BrandPrimary, modifier = Modifier.size(16.dp))
                            Text("FINANCE PREMIUM PLATFORM", color = BrandPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 1.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Watch Ads.\nEarn Real Money.",
                        fontWeight = FontWeight.Black,
                        fontSize = 38.sp,
                        lineHeight = 44.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = TextPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Join 50,000+ members watch shorter finance videos, earn dollars safely & withdraw from $5. No hidden charges.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNavigateToRegister,
                        colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("start_earning_cta")
                    ) {
                        Text(
                            text = "START EARNING FREE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "⚡ Real accounts only • Powered by verified ad networks",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Live Performance Metrics Slider
            item {
                Spacer(modifier = Modifier.height(44.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(BgSurface)
                        .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                        .padding(vertical = 20.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("TOTAL DISBURSED", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("$1,243,892", color = BrandPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(BorderColor))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ACTIVE MEMBERS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("52,389", color = BrandSecondary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(38.dp).background(BorderColor))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GLOBAL COUNTRIES", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                        Text("87", color = BrandAccent, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // How It Works Steps
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Three Simple Moves",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StepItem(
                        index = "1",
                        icon = Icons.Default.PersonAdd,
                        title = "Create Profile",
                        description = "Register your identity securely. Takes under 30 seconds."
                    )
                    StepItem(
                        index = "2",
                        icon = Icons.Default.PlayCircle,
                        title = "Watch High-Quality Ads",
                        description = "Watch 30s financial briefings. Earnings are calculated dynamically."
                    )
                    StepItem(
                        index = "3",
                        icon = Icons.Default.AccountBalanceWallet,
                        title = "Disburse Real Money",
                        description = "Accumulate balance and withdraw via PayPal, Wise, or Crypto from $5."
                    )
                }
            }

            // Recent Cashouts Proof Ticker
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Live Ledger Proof",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    text = "Real-time updates of successful cashouts processed",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val ledgerMocks = listOf(
                        LedgerItem("Ahmad K.", "Pakistan", "PayPal", "$12.40", "1 min ago"),
                        LedgerItem("Gabriela M.", "Brazil", "Wise", "$45.22", "4 mins ago"),
                        LedgerItem("Li W.", "Taiwan", "USDT Wallet", "$8.00", "7 mins ago"),
                        LedgerItem("Samantha R.", "USA", "PayPal", "$25.00", "12 mins ago")
                    )
                    ledgerMocks.forEach { item ->
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(item.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("(${item.country})", color = TextSecondary, fontSize = 11.sp)
                                }
                                Text("via ${item.method} • ${item.time}", color = TextMuted, fontSize = 12.sp)
                            }
                            Text(item.amount, color = BrandPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        }
                    }
                }
            }

            // FAQ Block
            item {
                Spacer(modifier = Modifier.height(48.dp))
                Text(
                    text = "Frequently Checked",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                )

                val faqs = listOf(
                    FAQ("Is this completely free?", "Yes, EarnPulse is entirely free. There are no upgrades, memberships, or initial investments required to withdraw money."),
                    FAQ("How does the platform pay?", "Advertisers buy high-fidelity placements. We distribute up to 70% of that advertising revenue directly into your user wallets."),
                    FAQ("What is the minimum withdrawal?", "The absolute minimum limit for transfer is $5.00, processed securely within 12 to 24 hours."),
                    FAQ("Do you forbid multiple accounts?", "Yes! We operate strict anti-fraud detection systems. Proxies, VPNs, and multi-accounts are banned instantly by automated filters.")
                )

                Column(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    faqs.forEach { faq ->
                        var expanded by remember { mutableStateOf(false) }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BgSurface)
                                .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                                .clickable { expanded = !expanded }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    faq.q,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand",
                                    tint = BrandPrimary
                                )
                            }
                            if (expanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    faq.a,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StepItem(index: String, icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgSurface)
            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BrandPrimary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = BrandPrimary, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(description, color = TextSecondary, fontSize = 12.sp, lineHeight = 18.sp)
        }
    }
}

@Composable
fun FintechGridBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "grid")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize().alpha(alphaAnim)) {
        val width = size.width
        val height = size.height
        val step = 45.dp.toPx()

        var x = 0f
        while (x < width) {
            drawLine(
                color = BorderColor,
                start = Offset(x, 0f),
                end = Offset(x, height),
                strokeWidth = 1f
            )
            x += step
        }

        var y = 0f
        while (y < height) {
            drawLine(
                color = BorderColor,
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}

data class LedgerItem(val name: String, val country: String, val method: String, val amount: String, val time: String)
data class FAQ(val q: String, val a: String)
