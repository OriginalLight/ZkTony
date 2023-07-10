import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import utils.RunState

@Composable
fun Home(
    modifier: Modifier,
) {
    val lazyListState = rememberLazyListState()
    var runState by remember { mutableStateOf(RunState()) }

    LazyColumn(
        modifier = modifier.padding(8.dp),
        state = lazyListState,
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "0x01 Run")
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(16) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (runState.ids.contains(it)) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
                                        shape = MaterialTheme.shapes.small
                                    ).clickable {
                                        val ids = if (runState.ids.contains(it)) {
                                            runState.ids.filter { id -> id != it }
                                        } else {
                                            runState.ids + it
                                        }
                                        runState = runState.copy(ids = ids)
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    modifier = Modifier.padding(8.dp),
                                    text = "$it",
                                    style = MaterialTheme.typography.body2,
                                    color = MaterialTheme.colors.onSecondary,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    color = MaterialTheme.colors.primary,
                                    shape = MaterialTheme.shapes.small
                                ).clickable {
                                    runState = if (runState.ids != (0..15).toList()) {
                                        runState.copy(ids = (0..15).toList())
                                    } else {
                                        runState.copy(ids = emptyList())
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                modifier = Modifier.padding(8.dp),
                                text = "A",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSecondary,
                            )
                        }
                    }
                }
            }
        }
    }
}