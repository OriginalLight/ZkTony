package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.ProgramViewModel

@Composable
fun ProgramView(viewModel: ProgramViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    Text(text = "Program View")
}