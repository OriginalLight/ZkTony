import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme(
        darkTheme = false,
        dynamicColor = true,
    ) {
        Surface(
            color = MaterialTheme.colors.background,
        ) {
            Home(modifier = Modifier)
        }
    }
}


