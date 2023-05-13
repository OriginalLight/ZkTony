package com.zktony.android.ui.screen.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.data.entity.Calibration
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.simpleDateFormat

@Composable
fun CalibrationMainPage(
    modifier: Modifier = Modifier,
    uiState: CalibrationUiState = CalibrationUiState(),
    active: (Long) -> Unit = {},
    delete: (Long) -> Unit = {},
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f)
        ) {
            val columnState = rememberLazyListState()

            LazyColumn(
                modifier = Modifier
                    .weight(6f)
                    .padding(end = 8.dp)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ),
                state = columnState,
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.entities.forEach {
                    item {
                        val background = if (it.id == uiState.selected) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        OutlinedCard(
                            modifier = Modifier
                                .wrapContentHeight()
                                .clickable {
                                    if (it.id == uiState.selected) {
                                        toggleSelected(0L)
                                    } else {
                                        toggleSelected(it.id)
                                    }
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = background,
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 16.dp),
                                    painter = painterResource(id = R.drawable.ic_calibration),
                                    contentDescription = null,
                                )

                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = it.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                )

                                AnimatedVisibility(visible = it.active == 1) {
                                    Icon(
                                        modifier = Modifier.size(36.dp),
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                    )
                                }

                                Spacer(modifier = Modifier.weight(1f))

                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = it.createTime.simpleDateFormat("yyyy - MM - dd"),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = MaterialTheme.shapes.medium
                    ), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                FloatingActionButton(modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                    onClick = { navigationTo(PageEnum.ADD) }) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                    )
                }

                AnimatedVisibility(visible = uiState.selected != 0L) {
                    var count by remember { mutableStateOf(0) }

                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = {
                            if (count == 1) {
                                delete(uiState.selected)
                                toggleSelected(0L)
                                count = 0
                            } else {
                                count++
                            }
                        }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.delete),
                            tint = if (count == 1) Color.Red else Color.Black,
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.selected != 0L) {
                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = { navigationTo(PageEnum.EDIT) }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                        )
                    }
                }

                AnimatedVisibility(visible = uiState.selected != 0L) {
                    FloatingActionButton(modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                        onClick = { active(uiState.selected) }) {
                        Icon(
                            modifier = Modifier.size(36.dp),
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationPagePreview() {
    CalibrationMainPage(uiState = CalibrationUiState(entities = listOf(Calibration())))
}