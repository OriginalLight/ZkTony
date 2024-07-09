package com.zktony.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zktony.android.BuildConfig
import com.zktony.android.R
import com.zktony.android.ui.components.PasswordInputField
import com.zktony.android.ui.components.UserNameInputField
import com.zktony.android.ui.navigation.Route
import com.zktony.android.ui.utils.LocalNavigationActions
import com.zktony.android.ui.utils.zktyBrush
import com.zktony.android.ui.viewmodel.LoginViewModel
import com.zktony.android.utils.ProductUtils
import kotlinx.coroutines.launch


@Composable
fun LoginView(viewModel: LoginViewModel = hiltViewModel()) {

    BackHandler {
        // 拦截返回键
        return@BackHandler
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = zktyBrush)
            .padding(16.dp)
    ) {
        Logo(modifier = Modifier.align(Alignment.TopStart))
        QrCode(modifier = Modifier.align(Alignment.BottomEnd))
        Ver(modifier = Modifier.align(Alignment.BottomStart))
        LoginForm(
            modifier = Modifier.align(Alignment.Center),
            viewModel = viewModel
        )
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
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = modifier.width(100.dp),
                painter = painterResource(id = R.mipmap.wx),
                contentDescription = "WX Code"
            )
            Text(
                text = stringResource(id = R.string.wechat),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.surface
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = modifier.width(100.dp),
                painter = painterResource(id = R.mipmap.sph),
                contentDescription = "SPH Code"
            )
            Text(
                text = stringResource(id = R.string.video),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.surface
            )
        }
    }
}

@Composable
fun Ver(modifier: Modifier = Modifier) {

    val ver by remember { mutableStateOf(BuildConfig.VERSION_NAME) }
    Text(
        modifier = modifier,
        text = ver,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun LoginForm(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        val scope = rememberCoroutineScope()
        val navigationActions = LocalNavigationActions.current
        var userName by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var loading by remember { mutableStateOf(false) }

        Text(
            text = ProductUtils.getProductNumber(),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.surface
        )

        UserNameInputField(
            modifier
                .height(56.dp)
                .width(450.dp),
            value = userName,
            onValueChange = { userName = it }
        )

        PasswordInputField(
            modifier
                .height(56.dp)
                .width(450.dp),
            value = password,
            onValueChange = { password = it }
        )

        Button(
            modifier = Modifier.width(450.dp),
            enabled = userName.isNotEmpty() && password.isNotEmpty(),
            onClick = {
                scope.launch {
                    loading = true
                    if (viewModel.login(userName, password)) {
                        navigationActions.popBackStack()
                        navigationActions.navigate(Route.EXPERIMENTAL)
                    }
                    loading = false
                }
            }
        ) {
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = stringResource(id = R.string.login),
                style = MaterialTheme.typography.bodyLarge
            )
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