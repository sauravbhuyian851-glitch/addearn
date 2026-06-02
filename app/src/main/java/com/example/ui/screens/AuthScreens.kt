package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.EarnPulseViewModel

@Composable
fun LoginScreen(
    viewModel: EarnPulseViewModel,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4285F4))) // Blue
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEA4335))) // Red
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFBBC05))) // Yellow
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34A853))) // Green
                }
                Text(
                    text = "EarnPulse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            Text(
                text = "Welcome Back",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = TextPrimary
            )
            Text(
                text = "Log in to claim your PTC daily dividends.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            // Auth Error Banner
            if (authError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, "Error", tint = ErrorColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = authError ?: "",
                            color = ErrorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Input Fields
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; viewModel.clearAuthError() },
                label = { Text("Email Address", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; viewModel.clearAuthError() },
                label = { Text("Password", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password",
                            tint = BrandPrimary
                        )
                    }
                },
                colors = textFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_input")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.isNotBlank() && password.isNotBlank()) {
                        isLoading = true
                        viewModel.login(email.trim(), password) {
                            isLoading = false
                            onLoginSuccess()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("SIGN IN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    text = "Sign Up",
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.clearAuthError(); onNavigateToRegister() }
                        .testTag("link_register")
                )
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: EarnPulseViewModel,
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var referrerCode by remember { mutableStateOf("") }
    
    var showPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val authError by viewModel.authError.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF4285F4))) // Blue
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFEA4335))) // Red
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFFFBBC05))) // Yellow
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF34A853))) // Green
                }
                Text(
                    text = "EarnPulse",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
            }

            Text(
                text = "Join EarnPulse",
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
                color = TextPrimary
            )
            Text(
                text = "Generate and disburse global revenue safely.",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            val displayError = validationError ?: authError
            if (displayError != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ErrorColor.copy(alpha = 0.1f)),
                    border = BorderStroke(1.dp, ErrorColor.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Error, "Error", tint = ErrorColor, modifier = Modifier.size(20.dp))
                        Text(
                            text = displayError,
                            color = ErrorColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it; validationError = null; viewModel.clearAuthError() },
                label = { Text("Full Name", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reg_fullName")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it; validationError = null; viewModel.clearAuthError() },
                label = { Text("Username", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reg_username")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it; validationError = null; viewModel.clearAuthError() },
                label = { Text("Email Address", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reg_email")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it; validationError = null; viewModel.clearAuthError() },
                label = { Text("Create Password (6+ chars)", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password",
                            tint = BrandPrimary
                        )
                    }
                },
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reg_password")
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = referrerCode,
                onValueChange = { referrerCode = it; validationError = null; viewModel.clearAuthError() },
                label = { Text("Referral Code (Optional)", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("reg_refcode"),
                placeholder = { Text("E.g. PULSE452", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.GroupAdd, "Referral", tint = BrandPrimary) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (fullName.isBlank() || username.isBlank() || email.isBlank() || password.isBlank()) {
                        validationError = "Please complete all fields"
                        return@Button
                    }
                    if (password.length < 6) {
                        validationError = "Password must be at least 6 characters"
                        return@Button
                    }
                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                        validationError = "Enter a valid email address"
                        return@Button
                    }

                    isLoading = true
                    val ref = if (referrerCode.isBlank()) null else referrerCode.trim()
                    viewModel.register(fullName.trim(), username.trim(), email.trim(), password, ref) {
                        isLoading = false
                        onRegisterSuccess()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("register_submit")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("CREATE WALLET", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already registered? ", color = TextSecondary, fontSize = 13.sp)
                Text(
                    text = "Sign In",
                    color = BrandPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clickable { viewModel.clearAuthError(); onNavigateToLogin() }
                        .testTag("link_login")
                )
            }
        }
    }
}

@Composable
fun PinSetupScreen(
    viewModel: EarnPulseViewModel,
    onPinLogged: () -> Unit
) {
    var pinValue by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = "Lock",
                tint = BrandPrimary,
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Secure Your Wallet",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = TextPrimary
            )
            Text(
                text = "Configure a 4-digit security PIN to clear payout payouts. Never share this PIN.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 28.dp)
            )

            if (pinError != null) {
                Text(pinError ?: "", color = ErrorColor, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(bottom = 12.dp))
            }

            OutlinedTextField(
                value = pinValue,
                onValueChange = {
                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                        pinValue = it
                        pinError = null
                    }
                },
                label = { Text("Enter 4-Digit PIN", color = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = PasswordVisualTransformation(),
                colors = textFieldColors(),
                modifier = Modifier
                    .width(180.dp)
                    .testTag("pin_value")
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (pinValue.length != 4) {
                        pinError = "PIN must be exactly 4 digits."
                        return@Button
                    }
                    viewModel.updateWithDrawalPin(pinValue) {
                        onPinLogged()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                shape = RoundedCornerShape(100.dp),
                modifier = Modifier
                    .width(180.dp)
                    .height(48.dp)
                    .testTag("pin_save_btn")
            ) {
                Text("SAVE PIN", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = BrandPrimary,
    unfocusedBorderColor = BorderColor,
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedLabelColor = BrandPrimary,
    unfocusedLabelColor = TextSecondary,
    cursorColor = BrandPrimary
)
