package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.ExperimentalViewModel

@Composable
fun ExperimentalView(viewModel: ExperimentalViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }
}