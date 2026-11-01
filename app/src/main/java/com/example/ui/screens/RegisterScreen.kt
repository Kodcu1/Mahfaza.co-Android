package com.example.ui.screens

import androidx.compose.runtime.Composable
import com.example.model.UserRole
import com.example.viewmodel.MainViewModel

/**
 * Unified RegisterScreen entry point that cleanly routes to
 * CoachRegistrationScreen or StudentRegistrationScreen.
 */
@Composable
fun RegisterScreen(
    role: UserRole,
    packageId: String = "",
    viewModel: MainViewModel,
    onSuccess: () -> Unit,
    onNavigateBack: () -> Unit = {}
) {
    if (role == UserRole.COACH) {
        CoachRegistrationScreen(
            viewModel = viewModel,
            onSuccess = onSuccess,
            onNavigateBack = onNavigateBack
        )
    } else {
        StudentRegistrationScreen(
            viewModel = viewModel,
            onSuccess = onSuccess,
            onNavigateBack = onNavigateBack
        )
    }
}
