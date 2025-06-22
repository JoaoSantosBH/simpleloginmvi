package com.jomar.simpleloginmvi.features.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.jomar.simpleloginmvi.features.login.ui.LoginScreen
import com.jomar.simpleloginmvi.features.login.ui.LoginViewModel


@Composable
fun LoginApp() {
    val viewModel = remember { LoginViewModel() }

    LoginScreen(
        viewModel = viewModel,
        onLoginSuccess = {
           print("LOGIN SUCCESS")
        }
    )
}