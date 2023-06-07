package com.zktony.android.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.zktony.android.ui.components.ZkTonyScaffold
import com.zktony.core.ext.getTimeFormat


@Composable
fun LcScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    viewModel: LcViewModel,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    ZkTonyScaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.vec.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.small
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            modifier = Modifier.padding(8.dp),
                            text = it.toString(),
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                uiState.queryHistory.reversed().forEach {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            text = it
                        )
                    }
                }
            }
            LazyColumn(
                modifier = Modifier
                    .height(96.dp)
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                uiState.replyHistory.reversed().forEach {
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            text = it
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = uiState.time.getTimeFormat(),
                    textAlign = TextAlign.Center,
                    fontSize = 60.sp,
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = uiState.log,
                    textAlign = TextAlign.Center,
                    fontSize = 60.sp,
                )

            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.job != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    onClick = { viewModel.test() }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "单个测试"
                    )
                }
                Spacer(modifier = Modifier.width(32.dp))
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.job != null) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    onClick = { viewModel.test1() }
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = "多个测试"
                    )
                }
            }
        }
    }
}
