package com.zktony.android.ui.screen.config

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.R
import com.zktony.android.ui.navigation.PageEnum
import com.zktony.core.ext.format

@Composable
fun ConfigMainPage(
    modifier: Modifier = Modifier,
    navigationTo: (PageEnum) -> Unit = {},
    uiState: ConfigUiState,
) {

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item {
            ElevatedCard(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(PageEnum.TRAVEL_EDIT) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_distance),
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.maximum_stroke),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val list = uiState.settings.travelList.ifEmpty { listOf(0f, 0f, 0f) }
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${list[0].format()} , ${list[1].format()} , ${list[2].format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
        item {
            ElevatedCard(
                modifier = Modifier
                    .wrapContentHeight()
                    .clickable { navigationTo(PageEnum.WASTE_EDIT) },
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(start = 16.dp),
                        painter = painterResource(id = R.drawable.ic_coordinate),
                        contentDescription = null,
                    )

                    Text(
                        modifier = Modifier.padding(start = 8.dp),
                        text = stringResource(id = R.string.waste_tank),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    val list = uiState.settings.wasteList.ifEmpty { listOf(0f, 0f, 0f) }
                    Text(
                        modifier = Modifier.padding(end = 16.dp),
                        text = "( ${list[0].format()} , ${list[1].format()} , ${list[2].format()} )",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true, widthDp = 960)
fun ConfigPagePreview() {
    ConfigMainPage(
        uiState = ConfigUiState()
    )
}