package com.zktony.android.ui.components

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.content.Context
import android.hardware.usb.UsbManager
import android.net.ConnectivityManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Suppress("DEPRECATION")
@SuppressLint("ServiceCast")
@Composable
fun Connection() {
    val context = LocalContext.current
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager

    val wifiStatus = remember { mutableStateOf(false) }
    val bluetoothStatus = remember { mutableStateOf(false) }
    val usbStatus = remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        while (true) {
            wifiStatus.value =
                connectivityManager.activeNetworkInfo?.isConnectedOrConnecting == true
            bluetoothStatus.value = bluetoothManager.adapter?.isEnabled == true
            usbStatus.value = usbManager.deviceList.isNotEmpty()

            delay(3000)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Wifi,
            contentDescription = "Wifi",
            tint = if (wifiStatus.value) Color.Green else Color.LightGray
        )

        Icon(
            imageVector = Icons.Default.Bluetooth,
            contentDescription = "Bluetooth",
            tint = if (bluetoothStatus.value) Color.Green else Color.LightGray
        )

        Icon(
            imageVector = Icons.Default.Usb,
            contentDescription = "Usb",
            tint = if (usbStatus.value) Color.Green else Color.LightGray
        )
    }
}

@Preview
@Composable
fun ConnectionPreview() {
    Surface {
        Connection()
    }
}