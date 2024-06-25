package com.zktony.android.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions


@Composable
fun LoginView() {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Logo(modifier = Modifier.align(Alignment.TopStart))
        QrCode(modifier = Modifier.align(Alignment.BottomEnd))
        Ver(modifier = Modifier.align(Alignment.BottomStart))
        LoginForm(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun Logo(modifier: Modifier = Modifier) {
    Image(
        modifier = modifier.width(350.dp),
        painter = painterResource(id = R.mipmap.logo),
        contentDescription = "Logo"
    )
}

@Composable
fun QrCode(modifier: Modifier = Modifier) {
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
fun Ver(modifier: Modifier = Modifier) {

    val ver by remember { mutableStateOf(BuildConfig.VERSION_NAME) }
    Text(
        modifier = modifier,
        text = ver,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun LoginForm(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val navigationActions = LocalNavigationActions.current
        Button(onClick = { navigationActions.navigate(Route.SETTINGS) }) {
            Text(text = "Test")
        }
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