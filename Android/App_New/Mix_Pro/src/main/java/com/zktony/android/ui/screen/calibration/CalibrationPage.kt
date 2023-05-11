package com.zktony.android.ui.screen.calibration

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.data.entity.Calibration
import com.zktony.android.ui.viewmodel.CalibrationPage
import com.zktony.core.ext.simpleDateFormat

/**
 * CalibrationPage
 *
 * @param modifier Modifier
 * @param delete Function1<Long, Unit>
 * @param enable Function0<Unit>
 * @param list List<Calibration>
 * @param navigationTo Function1<CalibrationPage, Unit>
 * @param selected Long
 * @param toggleSelected Function1<Long, Unit>
 * @return Unit
 */
@Composable
fun CalibrationPage(
    modifier: Modifier = Modifier,
    delete: (Long) -> Unit = {},
    enable: () -> Unit = {},
    list: List<Calibration>,
    navigationTo: (CalibrationPage) -> Unit = {},
    selected: Long = 0L,
    toggleSelected: (Long) -> Unit = {},
) {
    val columnState = rememberLazyListState()

    Row {
        Column(
            modifier = modifier
                .weight(6f)
                .fillMaxHeight()
                .padding(start = 8.dp, top = 8.dp, end = 4.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                state = columnState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                list.forEach {
                    item {
                        val background = if (selected == it.id) {
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                        Card(
                            modifier = Modifier
                                .wrapContentHeight()
                                .clickable { toggleSelected(it.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = background,
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(start = 16.dp),
                                    imageVector = if (it.active == 1) Icons.Filled.Check else Icons.Outlined.Circle,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = it.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    maxLines = 1,
                                )
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
        }
        Column(
            modifier = modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 4.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
                .background(
                    color = MaterialTheme.colorScheme.background,
                    shape = MaterialTheme.shapes.medium
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                onClick = { navigationTo(CalibrationPage.CALIBRATION_ADD) }
            ) {
                Icon(
                    modifier = Modifier.size(36.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = R.string.add),
                )
            }
            AnimatedVisibility(visible = selected != 0L) {

                var count by remember { mutableStateOf(0) }

                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = {
                        count++
                        if (count == 2) {
                            delete(selected)
                            toggleSelected(0L)
                            count = 0
                        }
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(id = R.string.delete),
                        tint = if (count == 1) Color.Red else Color.Black,
                    )
                }
            }
            AnimatedVisibility(visible = selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { navigationTo(CalibrationPage.CALIBRATION_EDIT) }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(id = R.string.edit),
                    )
                }
            }
            AnimatedVisibility(visible = selected != 0L) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(),
                    onClick = { enable() }
                ) {
                    Icon(
                        modifier = Modifier.size(36.dp),
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(id = R.string.edit),
                    )
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun CalibrationPagePreview() {
    CalibrationPage(
        list = listOf(Calibration())
    )
}