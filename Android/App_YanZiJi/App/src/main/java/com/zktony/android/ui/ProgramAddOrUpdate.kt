package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.ProgramAddOrUpdateViewModel
import com.zktony.room.defaults.defaultProgram
import com.zktony.room.entities.Program

@Composable
fun ProgramAddOrUpdateView(viewModel: ProgramAddOrUpdateViewModel = hiltViewModel()) {
    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    val navObj by viewModel.navObj.collectAsStateWithLifecycle()
    var obj by remember(navObj) { mutableStateOf(navObj ?: defaultProgram()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 顶部导航栏
        ProgramAddOrUpdateTopBar(navObj = navObj, obj = obj, viewModel = viewModel)

    }
}

// 顶部导航栏
@Composable
fun ProgramAddOrUpdateTopBar(
    modifier: Modifier = Modifier,
    navObj: Program?,
    obj: Program,
    viewModel: ProgramAddOrUpdateViewModel
) {
    val navigationActions = LocalNavigationActions.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(brush = zktyBrush, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.medium)
                .clickable { navigationActions.navigateUp() }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.AutoMirrored.Default.Reply, contentDescription = "Back")
            Text(
                text = if (navObj == null) "程序添加" else "程序编辑",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Button(
            enabled = obj.canSave(),
            onClick = { /*TODO*/ }
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save")
            Text(text = "保存", style = MaterialTheme.typography.bodyLarge)
        }
    }
}