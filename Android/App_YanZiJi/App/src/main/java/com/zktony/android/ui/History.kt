package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.viewmodel.HistoryViewModel

/**
 * @author 刘贺贺
 * @date 2023/8/31 9:57
 */
@Composable
fun HistoryView(viewModel: HistoryViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    Text(text = "History View")
}