package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.data.ExperimentalState
import com.zktony.android.ui.components.ExperimentalState
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.utils.zktyHorizontalBrush
import com.zktony.android.ui.viewmodel.ExperimentalViewModel
import com.zktony.android.utils.ProductUtils

@Composable
fun ExperimentalView(viewModel: ExperimentalViewModel = hiltViewModel()) {

    val navigationActions = LocalNavigationActions.current

    BackHandler {
        navigationActions.navigateUp()
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(ProductUtils.getChannelCount()) { index ->
            ExperimentalChannelView(
                modifier = Modifier.weight(1f),
                index = index
            )
        }
    }
}

@Composable
fun ExperimentalChannelView(
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(
                brush = zktyHorizontalBrush,
                shape = MaterialTheme.shapes.medium,
            )
    ) {

        ExperimentalChannelHeader(index = index)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(8.dp)
        ) {
            ExperimentalAttributeItem(title = "程序") {
                
            }
        }


    }
}

@Composable
fun ExperimentalChannelHeader(
    modifier: Modifier = Modifier,
    index: Int = 0
) {
    Box {
        Row(
            modifier = modifier
                .height(48.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ExperimentalState(state = ExperimentalState.READY)
            Text(
                text = (index + 1).toString(),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}



@Composable
fun ExperimentalAttributeItem(
    modifier: Modifier = Modifier,
    title: String,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal))
        }
        content()
    }
}

@Preview
@Composable
fun ChannelItemPreview() {
    ExperimentalChannelView()
}