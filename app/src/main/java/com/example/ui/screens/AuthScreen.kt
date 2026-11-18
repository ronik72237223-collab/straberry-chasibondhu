package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.BgOffWhite
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.BorderSoft
import com.example.ui.theme.EmeraldBorder
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSoftBg
import com.example.ui.theme.StrawberryRed
import com.example.ui.theme.StrawberrySoftBg
import com.example.ui.theme.SurfacePureWhite
import com.example.ui.theme.TextDarkBlack
import com.example.ui.theme.TextSecondaryDark
import com.example.ui.viewmodel.AuthUiState

@Composable
fun AuthScreen(
    authUiState: AuthUiState,
    onLogin: (String, String) -> Unit,
    onSignUp: (String, String, String, String) -> Unit,
    onForgotPassword: (String) -> Unit,
    onGuestMode: () -> Unit,
    onClearMessages: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var farmName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFECFDF5), BgOffWhite, Color(0xFFFFF1F2))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = SurfacePureWhite,
                    border = BorderStroke(1.5.dp, EmeraldBorder),
                    shadowElevation = 4.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.strawberry_farm_icon),
                        contentDescription = "স্ট্রবেরি চাষি বন্ধু আইকন",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(86.dp)
                            .clip(RoundedCornerShape(20.dp))
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "স্ট্রবেরি চাষি বন্ধু",
                    style = MaterialTheme.typography.displaySmall.copy(
                        color = TextDarkBlack,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "আপনার স্ট্রবেরি বাগানের স্মার্ট ডিজিটাল খাতা",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = TextSecondaryDark,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1-Tap Instant Offline Guest Mode Card (Farmer-Friendly)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = EmeraldSoftBg),
                    border = BorderStroke(1.5.dp, EmeraldPrimary)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "অ্যাকাউন্ট ছাড়াই সাথে সাথে শুরু করুন",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Bold
                            ),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "সব হিসাব আপনার ফোনেই সুরক্ষিত থাকবে (ইন্টারনেট ছাড়াই চলবে)",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondaryDark),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onGuestMode,
                            enabled = !authUiState.isBusy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp)
                                .testTag("guest_mode_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = "সরাসরি ব্যবহার",
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "গেস্ট / সরাসরি ব্যবহার করুন (অফলাইন মোড)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSlate)
                    Text(
                        text = "  অথবা ক্লাউড অ্যাকাউন্ট  ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextSecondaryDark,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = BorderSlate)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Email & Password Login / SignUp Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfacePureWhite),
                    border = BorderStroke(1.dp, BorderSoft),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Mode Switcher Pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    isSignUpMode = false
                                    onClearMessages()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (!isSignUpMode) EmeraldSoftBg else SurfacePureWhite,
                                    contentColor = TextDarkBlack
                                ),
                                border = BorderStroke(
                                    width = if (!isSignUpMode) 2.dp else 1.dp,
                                    color = if (!isSignUpMode) EmeraldPrimary else BorderSlate
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("switch_login_tab")
                            ) {
                                Text("লগইন", fontWeight = FontWeight.Bold, color = TextDarkBlack)
                            }
                            OutlinedButton(
                                onClick = {
                                    isSignUpMode = true
                                    onClearMessages()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (isSignUpMode) StrawberrySoftBg else SurfacePureWhite,
                                    contentColor = TextDarkBlack
                                ),
                                border = BorderStroke(
                                    width = if (isSignUpMode) 2.dp else 1.dp,
                                    color = if (isSignUpMode) StrawberryRed else BorderSlate
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("switch_signup_tab")
                            ) {
                                Text("নতুন অ্যাকাউন্ট", fontWeight = FontWeight.Bold, color = TextDarkBlack)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AnimatedVisibility(visible = isSignUpMode) {
                            Column {
                                HighContrastTextField(
                                    value = name,
                                    onValueChange = { name = it; onClearMessages() },
                                    label = "আপনার নাম",
                                    placeholder = "যেমন: করিম মিয়া",
                                    leadingIcon = Icons.Default.Person,
                                    testTag = "signup_name_input"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                HighContrastTextField(
                                    value = farmName,
                                    onValueChange = { farmName = it },
                                    label = "বাগানের নাম (ঐচ্ছিক)",
                                    placeholder = "যেমন: সবুজ স্ট্রবেরি বাগান",
                                    leadingIcon = Icons.Default.Agriculture,
                                    testTag = "signup_farm_input"
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }

                        HighContrastTextField(
                            value = email,
                            onValueChange = { email = it; onClearMessages() },
                            label = "ইমেইল ঠিকানা",
                            placeholder = "example@gmail.com",
                            keyboardType = KeyboardType.Email,
                            leadingIcon = Icons.Default.Email,
                            testTag = "auth_email_input"
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it; onClearMessages() },
                            label = {
                                Text(
                                    "পাসওয়ার্ড",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = TextSecondaryDark,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "পাসওয়ার্ড",
                                    tint = TextDarkBlack
                                )
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "পাসওয়ার্ড দেখুন",
                                        tint = TextDarkBlack
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            shape = RoundedCornerShape(14.dp),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = TextDarkBlack,
                                fontWeight = FontWeight.Medium
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SurfacePureWhite,
                                unfocusedContainerColor = SurfacePureWhite,
                                focusedTextColor = TextDarkBlack,
                                unfocusedTextColor = TextDarkBlack,
                                cursorColor = EmeraldPrimary,
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = BorderSlate
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_password_input")
                        )

                        if (!isSignUpMode) {
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                                TextButton(onClick = { onForgotPassword(email) }) {
                                    Text(
                                        text = "পাসওয়ার্ড ভুলে গেছেন?",
                                        color = StrawberryRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                        }

                        if (!authUiState.errorMessage.isNullOrBlank()) {
                            Surface(
                                color = StrawberrySoftBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = authUiState.errorMessage,
                                    color = StrawberryRed,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        if (!authUiState.infoMessage.isNullOrBlank()) {
                            Surface(
                                color = EmeraldSoftBg,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, EmeraldBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = authUiState.infoMessage,
                                    color = TextDarkBlack,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    onSignUp(name, farmName, email, password)
                                } else {
                                    onLogin(email, password)
                                }
                            },
                            enabled = !authUiState.isBusy,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSignUpMode) StrawberryRed else EmeraldPrimary,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 54.dp)
                                .testTag("auth_submit_button")
                        ) {
                            if (authUiState.isBusy) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.5.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = if (isSignUpMode) "নতুন অ্যাকাউন্ট খুলুন" else "লগইন করুন",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
