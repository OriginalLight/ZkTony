package com.zktony.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.ui.SettingIntent
import com.zktony.android.ui.navigation.TOP_LEVEL_DESTINATIONS
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.PageType
import kotlinx.coroutines.launch

/**
 * @author 刘贺贺
 * @date 2023/8/23 9:03
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeAppBar(
    page: Int,
    navigation: () -> Unit
) {

    val navigationActions = LocalNavigationActions.current

    TopAppBar(
        title = {
            Image(
                modifier = Modifier.height(48.dp),
                painter = painterResource(id = R.mipmap.logo),
                contentDescription = null
            )
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = page != PageType.HOME) {
                    ElevatedButton(onClick = navigation) {
                        Icon(
                            imageVector = Icons.Default.Reply,
                            contentDescription = null
                        )
                    }
                }
                TOP_LEVEL_DESTINATIONS.forEach { destination ->
                    ElevatedButton(
                        onClick = { navigationActions.navigateTo(destination) },
                    ) {
                        Icon(
                            imageVector = destination.icon,
                            contentDescription = stringResource(id = destination.iconTextId)
                        )
                    }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAppBar(
    page: Int,
    dispatch: (SettingIntent) -> Unit,
    navigation: () -> Unit
) {
    val scope = rememberCoroutineScope()

    TopAppBar(
        title = {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = stringResource(id = R.string.setting),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AnimatedVisibility(visible = page == PageType.MOTOR_LIST) {
                    ElevatedButton(onClick = { scope.launch { dispatch(SettingIntent.Insert) } }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )
                    }
                }
                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugAppBar(navigation: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.small,
                    )
                    .padding(horizontal = 32.dp, vertical = 4.dp),
                text = stringResource(id = R.string.debug),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    )
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ElevatedButton(onClick = navigation) {
                    Icon(
                        imageVector = Icons.Default.Reply,
                        contentDescription = null
                    )
                }
            }
        }
    )
}