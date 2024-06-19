package com.zktony.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileOpen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.BuildConfig
import com.zktony.android.R


@Composable
fun LoginRoute() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Logo(modifier = Modifier.align(Alignment.TopStart))
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp)
        ) {
            QrCode(modifier = Modifier.align(Alignment.BottomEnd))
            WellCome(modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun Logo(modifier: Modifier) {
    Image(
        modifier = modifier.width(350.dp),
        painter = painterResource(id = R.mipmap.logo),
        contentDescription = "Logo"
    )
}

@Composable
fun QrCode(modifier: Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(48.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = modifier.width(150.dp),
                painter = painterResource(id = R.mipmap.wx),
                contentDescription = "WX Code"
            )
            Text(text = "公众号", style = MaterialTheme.typography.bodyLarge)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = modifier.width(150.dp),
                painter = painterResource(id = R.mipmap.sph),
                contentDescription = "SPH Code"
            )
            Text(text = "视频号", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun Ver(modifier: Modifier) {

    val ver by remember { mutableStateOf(BuildConfig.VERSION_NAME) }
    Text(
        modifier = modifier,
        text = ver,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun WellCome(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "欢迎使用",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = "请根据您的需求，并按照使用规范执行相应程序……",
            style = MaterialTheme.typography.bodyLarge
        )

        Ver(modifier = Modifier.padding(top = 16.dp))
    }
}

@Preview
@Composable
fun LogoPreview() {
    Logo(modifier = Modifier)
}

@Preview
@Composable
fun QrCodePreview() {
    QrCode(modifier = Modifier)
}

@Preview
@Composable
fun VerPreview() {
    Ver(modifier = Modifier)
}

@Preview
@Composable
fun WellComePreview() {
    WellCome(modifier = Modifier)
}