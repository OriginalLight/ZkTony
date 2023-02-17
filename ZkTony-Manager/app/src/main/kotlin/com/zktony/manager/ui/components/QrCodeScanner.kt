package com.zktony.manager.ui.components

import android.util.Size
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.zktony.manager.ui.utils.QrCodeAnalyzer

/**
 * @author: 刘贺贺
 * @date: 2023-02-17 16:17
 */

@Composable
fun QrCodeScanner() {
    var code by remember {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember {
        ProcessCameraProvider.getInstance(context)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AndroidView(
            factory = { context ->
                val previewView = PreviewView(context)
                val preview = Preview.Builder().build()
                val selector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()
                preview.setSurfaceProvider(previewView.surfaceProvider)
                val imageAnalysis = ImageAnalysis.Builder()
                    .setTargetResolution(
                        Size(
                            previewView.width,
                            previewView.height
                        )
                    )
                    .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                imageAnalysis.setAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    QrCodeAnalyzer { result ->
                        code = result
                    }
                )
                try {
                    cameraProviderFuture.get().bindToLifecycle(
                        lifecycleOwner,
                        selector,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                previewView
            },
            modifier = Modifier.weight(1f)
        )
        Text(
            text = code,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        )
    }
}