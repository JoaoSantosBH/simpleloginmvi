package com.jomar.simpleloginmvi.features.login.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit = {}
) {
    val viewState by viewModel.viewState.collectAsStateWithLifecycle()

    // Handle login success
    LaunchedEffect(viewState.isLoginSuccessful) {
        if (viewState.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

    // Send initial intent
    LaunchedEffect(Unit) {
        viewModel.handleIntent(LoginIntent.LoadScreen)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Title
        Text(
            text = "Welcome Back",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Sign in to your account",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Email Field
        OutlinedTextField(
            value = viewState.email,
            onValueChange = { viewModel.handleIntent(LoginIntent.EmailChanged(it)) },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            isError = viewState.emailError != null,
            supportingText = viewState.emailError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            enabled = !viewState.isLoading
        )

        // Password Field
        OutlinedTextField(
            value = viewState.password,
            onValueChange = { viewModel.handleIntent(LoginIntent.PasswordChanged(it)) },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(
                    onClick = { viewModel.handleIntent(LoginIntent.TogglePasswordVisibility) }
                ) {
                    Icon(
                        imageVector = if (viewState.isPasswordVisible)
                            Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (viewState.isPasswordVisible)
                            "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (viewState.isPasswordVisible)
                VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = viewState.passwordError != null,
            supportingText = viewState.passwordError?.let { { Text(it) } },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            enabled = !viewState.isLoading
        )

        // Login Error
        viewState.loginError?.let { error ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // Login Button
        Button(
            onClick = { viewModel.handleIntent(LoginIntent.LoginClicked) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !viewState.isLoading
        ) {
            if (viewState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Sign In",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Demo credentials hint
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Demo Credentials:",
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Email: demo@example.com",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Password: password123",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}