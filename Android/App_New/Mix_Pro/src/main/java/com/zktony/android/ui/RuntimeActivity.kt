package com.zktony.android.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.common.annotations.VisibleForTesting

fun launchRuntimeActivity(context: Context, item: Long) {
    context.startActivity(createRuntimeActivityIntent(context, item))
}

@VisibleForTesting
fun createRuntimeActivityIntent(context: Context, item: Long): Intent {
    val intent = Intent(context, RuntimeActivity::class.java)
    intent.putExtra("PROGRAM_ID", item)
    return intent
}

class RuntimeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val item = intent.getLongExtra("PROGRAM_ID", 0L)

        setContent {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier.clickable {
                        finish()
                    },
                    text = "RuntimeActivity $item",
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}