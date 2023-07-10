import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme(
        darkTheme = false,
        dynamicColor = true,
    ) {
        Home(modifier = Modifier)
    }
}

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Protocol Helper",
        icon = painterResource("image/logo.svg")
    ) {
        App()
    }
}
