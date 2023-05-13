package com.zktony.android.ui.screen.motor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zktony.android.data.entity.Motor
import com.zktony.android.ui.navigation.PageEnum

@Composable
fun MotorMainPage(
    modifier: Modifier = Modifier,
    uiState: MotorUiState = MotorUiState(),
    navigationTo: (PageEnum) -> Unit = {},
    toggleSelected: (Long) -> Unit = {},
) {
    LazyVerticalGrid(
        modifier = modifier.padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        columns = GridCells.Fixed(3)
    ) {
        uiState.entities.forEach {
            item {
                Card(
                    modifier = Modifier
                        .clickable {
                            toggleSelected(it.id)
                            navigationTo(PageEnum.EDIT)
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = it.text,
                            fontSize = 50.sp,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Column(
                            modifier = Modifier.padding(start = 16.dp),
                        ) {
                            Text(
                                text = "S - ${it.speed}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "A - ${it.acc}",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Text(
                                text = "D - ${it.dec}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960, heightDp = 640)
fun MotorPagePreview() {
    MotorMainPage(
        modifier = Modifier,
        uiState = MotorUiState(entities = listOf(Motor(text = "M1"), Motor(text = "M2")))
    )
}