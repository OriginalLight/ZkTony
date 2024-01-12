import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dsl.tx
import ext.crc16LE
import ext.hex2ByteArray
import ext.toHexString
import utils.QueryState
import utils.RunState
import utils.SetState
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun Home(
    modifier: Modifier,
) {
    val menu by remember { mutableStateOf(listOf("0x00", "0x01", "0x02", "0x03", "0x04", "0x05", "Crc")) }
    var selected by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize().padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FlowRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            menu.forEachIndexed { index, item ->
                Button(
                    modifier = modifier,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selected == index) MaterialTheme.colors.primary else MaterialTheme.colors.background,
                        contentColor = if (selected == index) MaterialTheme.colors.onPrimary else MaterialTheme.colors.onBackground,
                    ),
                    onClick = {
                        selected = index
                    },
                ) {
                    Text(text = item)
                }
            }
        }

        AnimatedVisibility(visible = selected == 0) {
            Reset(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 1) {
            Run(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 2) {
            Stop(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 3) {
            QueryAxis(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 4) {
            QueryGpio(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 5) {
            Valve(modifier = Modifier)
        }

        AnimatedVisibility(visible = selected == 6) {
            Crc(modifier = Modifier)
        }
    }
}

@Composable
fun Reset(
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Reset")
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var reset by remember { mutableStateOf(tx { reset() }.toHexString()) }
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = reset,
                    onValueChange = {
                        reset = it
                    },
                    label = { Text(text = "命令") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        val transferable = StringSelection(reset)
                        clipboard.setContents(transferable, null)
                    },
                ) {
                    Text(text = "Copy")
                }
            }
        }
    }
}

@Composable
fun Run(
    modifier: Modifier = Modifier,
) {
    var runState by remember { mutableStateOf(RunState()) }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Run")
            Divider()
            IdRow(
                ids = runState.ids,
                onItemClick = { id ->
                    val ids = if (runState.ids.contains(id)) {
                        runState.ids.filter { it != id }
                    } else {
                        runState.ids + id
                    }
                    runState = runState.copy(ids = ids)
                },
                onSelectionAll = {
                    runState = if (runState.ids != (0..15).toList()) {
                        runState.copy(ids = (0..15).toList())
                    } else {
                        runState.copy(ids = emptyList())
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                ) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = runState.steps,
                        onValueChange = {
                            runState = runState.copy(steps = it)
                        },
                        label = { Text(text = "步数") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = runState.acceleration,
                        onValueChange = {
                            runState = runState.copy(acceleration = it)
                        },
                        label = { Text(text = "加速度") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = runState.deceleration,
                        onValueChange = {
                            runState = runState.copy(deceleration = it)
                        },
                        label = { Text(text = "减速度") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = runState.speed,
                        onValueChange = {
                            runState = runState.copy(speed = it)
                        },
                        label = { Text(text = "速度") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    modifier = Modifier.weight(1.5f).height(256.dp),
                    value = runState.tx,
                    onValueChange = {
                        runState = runState.copy(tx = it)
                    },
                    label = { Text(text = "命令") },
                    singleLine = false,
                    maxLines = 12,
                )
                Column {
                    Button(
                        onClick = {
                            val tx = try {
                                tx {
                                    runState.ids.forEach { id ->
                                        run {
                                            index = id
                                            steps = runState.steps.toLong()
                                            acc = runState.acceleration.toLongOrNull() ?: 0L
                                            dec = runState.deceleration.toLongOrNull() ?: 0L
                                            speed = runState.speed.toLongOrNull() ?: 0L
                                        }
                                    }
                                }.toHexString()
                            } catch (e: Exception) {
                                e.message ?: ""
                            }
                            runState = runState.copy(tx = tx)
                        },
                    ) {
                        Text(text = "Generate")
                    }
                    if (runState.tx.isNotEmpty()) {
                        Button(
                            onClick = {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val transferable = StringSelection(runState.tx)
                                clipboard.setContents(transferable, null)
                            },
                        ) {
                            Text(text = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Stop(
    modifier: Modifier = Modifier,
) {
    var stopState by remember { mutableStateOf(QueryState()) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Stop")
            Divider()
            IdRow(
                ids = stopState.ids,
                onItemClick = { id ->
                    val ids = if (stopState.ids.contains(id)) {
                        stopState.ids.filter { it != id }
                    } else {
                        stopState.ids + id
                    }
                    stopState = stopState.copy(ids = ids)
                },
                onSelectionAll = {
                    stopState = if (stopState.ids != (0..15).toList()) {
                        stopState.copy(ids = (0..15).toList())
                    } else {
                        stopState.copy(ids = emptyList())
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f).height(128.dp),
                    value = stopState.tx,
                    onValueChange = {
                        stopState = stopState.copy(tx = it)
                    },
                    label = { Text(text = "命令") },
                    singleLine = false,
                )
                Column {
                    Button(
                        onClick = {
                            val byteArray = tx {
                                stop(stopState.ids)
                            }
                            stopState = stopState.copy(tx = byteArray.toHexString())
                        },
                    ) {
                        Text(text = "Generate")
                    }
                    if (stopState.tx.isNotEmpty()) {
                        Button(
                            onClick = {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val transferable = StringSelection(stopState.tx)
                                clipboard.setContents(transferable, null)
                            },
                        ) {
                            Text(text = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueryAxis(
    modifier: Modifier = Modifier,
) {
    var queryAxisState by remember { mutableStateOf(QueryState()) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Query Axis")
            Divider()
            IdRow(
                ids = queryAxisState.ids,
                onItemClick = { id ->
                    val ids = if (queryAxisState.ids.contains(id)) {
                        queryAxisState.ids.filter { it != id }
                    } else {
                        queryAxisState.ids + id
                    }
                    queryAxisState = queryAxisState.copy(ids = ids)
                },
                onSelectionAll = {
                    queryAxisState = if (queryAxisState.ids != (0..15).toList()) {
                        queryAxisState.copy(ids = (0..15).toList())
                    } else {
                        queryAxisState.copy(ids = emptyList())
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f).height(128.dp),
                    value = queryAxisState.tx,
                    onValueChange = {
                        queryAxisState = queryAxisState.copy(tx = it)
                    },
                    label = { Text(text = "命令") },
                    singleLine = false,
                )
                Column {
                    Button(
                        onClick = {
                            val byteArray = tx {
                                queryAxis(queryAxisState.ids)
                            }
                            queryAxisState = queryAxisState.copy(tx = byteArray.toHexString())
                        },
                    ) {
                        Text(text = "Generate")
                    }
                    if (queryAxisState.tx.isNotEmpty()) {
                        Button(
                            onClick = {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val transferable = StringSelection(queryAxisState.tx)
                                clipboard.setContents(transferable, null)
                            },
                        ) {
                            Text(text = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QueryGpio(
    modifier: Modifier = Modifier,
) {
    var queryGpioState by remember { mutableStateOf(QueryState()) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Query Gpio")
            Divider()
            IdRow(
                ids = queryGpioState.ids,
                onItemClick = { id ->
                    val ids = if (queryGpioState.ids.contains(id)) {
                        queryGpioState.ids.filter { it != id }
                    } else {
                        queryGpioState.ids + id
                    }
                    queryGpioState = queryGpioState.copy(ids = ids)
                },
                onSelectionAll = {
                    queryGpioState = if (queryGpioState.ids != (0..15).toList()) {
                        queryGpioState.copy(ids = (0..15).toList())
                    } else {
                        queryGpioState.copy(ids = emptyList())
                    }
                }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f).height(128.dp),
                    value = queryGpioState.tx,
                    onValueChange = {
                        queryGpioState = queryGpioState.copy(tx = it)
                    },
                    label = { Text(text = "命令") },
                    singleLine = false,
                )
                Column {
                    Button(
                        onClick = {
                            val byteArray = tx {
                                queryGpio(queryGpioState.ids)
                            }
                            queryGpioState = queryGpioState.copy(tx = byteArray.toHexString())
                        },
                    ) {
                        Text(text = "Generate")
                    }
                    if (queryGpioState.tx.isNotEmpty()) {
                        Button(
                            onClick = {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val transferable = StringSelection(queryGpioState.tx)
                                clipboard.setContents(transferable, null)
                            },
                        ) {
                            Text(text = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Valve(
    modifier: Modifier = Modifier,
) {
    var valveState by remember { mutableStateOf(SetState()) }
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "Valve")
            Divider()
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(16) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (valveState.hashMap.contains(it)) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
                                shape = MaterialTheme.shapes.small
                            ).clickable {
                                valveState = if (valveState.hashMap.contains(it)) {
                                    val map = valveState.hashMap[it]!!
                                    if (map == 0) {
                                        valveState.copy(hashMap = valveState.hashMap + (it to 1))
                                    } else {
                                        valveState.copy(hashMap = valveState.hashMap - it)
                                    }
                                } else {
                                    valveState.copy(hashMap = valveState.hashMap + (it to 0))
                                }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                text = "$it",
                                style = MaterialTheme.typography.body2,
                                color = MaterialTheme.colors.onSecondary,
                            )
                            if (valveState.hashMap.contains(it)) {
                                Text(
                                    text = "${valveState.hashMap[it]}",
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Light,
                                    ),
                                    color = MaterialTheme.colors.onSecondary,
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            color = MaterialTheme.colors.primary,
                            shape = MaterialTheme.shapes.small
                        ).clickable {
                            valveState = if (valveState.hashMap.any { it.value == 0 }) {
                                valveState.copy(hashMap = valveState.hashMap.map { it.key to 1 }.toMap())
                            } else {
                                valveState.copy(hashMap = valveState.hashMap.map { it.key to 0 }.toMap())
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f).height(128.dp),
                    value = valveState.tx,
                    onValueChange = {
                        valveState = valveState.copy(tx = it)
                    },
                    label = { Text(text = "命令") },
                    singleLine = false,
                )
                Column {
                    Button(
                        onClick = {
                            val byteArray = tx {
                                val pair = valveState.hashMap.map { it.key to it.value }.toList()
                                valve(pair)
                            }
                            valveState = valveState.copy(tx = byteArray.toHexString())
                        },
                    ) {
                        Text(text = "Generate")
                    }
                    if (valveState.tx.isNotEmpty()) {
                        Button(
                            onClick = {
                                val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                                val transferable = StringSelection(valveState.tx)
                                clipboard.setContents(transferable, null)
                            },
                        ) {
                            Text(text = "Copy")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Crc(
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(text = "CRC16")
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var hex by remember { mutableStateOf("") }
                var crc by remember { mutableStateOf("") }
                OutlinedTextField(
                    modifier = Modifier.weight(1.5f).height(256.dp),
                    value = hex,
                    onValueChange = {
                        hex = it
                        crc = try {
                            hex.hex2ByteArray().crc16LE().toHexString()
                        } catch (e: Exception) {
                            e.message ?: ""
                        }
                    },
                    label = { Text(text = "Hex") },
                    singleLine = false,
                    maxLines = 12
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = crc,
                    onValueChange = {
                        crc = it
                    },
                    label = { Text(text = "Crc") },
                    singleLine = true,
                )
                Button(
                    onClick = {
                        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                        val transferable = StringSelection(crc)
                        clipboard.setContents(transferable, null)
                    },
                ) {
                    Text(text = "Copy")
                }
            }
        }
    }
}

@Composable
fun IdRow(
    modifier: Modifier = Modifier,
    ids: List<Int> = emptyList(),
    onItemClick: (Int) -> Unit = {},
    onSelectionAll: () -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(16) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(
                        color = if (ids.contains(it)) MaterialTheme.colors.primary else MaterialTheme.colors.secondary,
                        shape = MaterialTheme.shapes.small
                    ).clickable {
                        onItemClick(it)
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
                    onSelectionAll()
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