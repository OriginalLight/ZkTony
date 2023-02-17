package com.zktony.manager.ui.utils

import android.graphics.ImageFormat
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import java.nio.ByteBuffer

class QrCodeAnalyzer(
    private val onQrCodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @RequiresApi(Build.VERSION_CODES.M)
    private val supportedImageFormats = listOf(
        ImageFormat.YUV_420_888,
        ImageFormat.YUV_422_888,
        ImageFormat.YUV_444_888,
    )

    @RequiresApi(Build.VERSION_CODES.M)
    override fun analyze(image: ImageProxy) {
        if (image.format in supportedImageFormats) {
            val bytes = image.planes.first().buffer.toByteArray()
            val source = PlanarYUVLuminanceSource(
                bytes,
                image.width,
                image.height,
                0,
                0,
                image.width,
                image.height,
                false
            )
            val binaryBmp = BinaryBitmap(HybridBinarizer(source))
            try {
                val result = MultiFormatReader().apply {
                    setHints(
                        mapOf(
                            DecodeHintType.POSSIBLE_FORMATS to listOf(
                                BarcodeFormat.QR_CODE,
                                BarcodeFormat.CODE_128,
                                BarcodeFormat.CODE_39,
                                BarcodeFormat.CODE_93,
                                BarcodeFormat.EAN_13,
                                BarcodeFormat.EAN_8,
                                BarcodeFormat.ITF,
                                BarcodeFormat.UPC_A,
                                BarcodeFormat.UPC_E,
                                BarcodeFormat.CODABAR,
                                BarcodeFormat.RSS_14,
                                BarcodeFormat.RSS_EXPANDED,
                                BarcodeFormat.DATA_MATRIX,
                                BarcodeFormat.AZTEC,
                                BarcodeFormat.PDF_417,
                                BarcodeFormat.MAXICODE,
                            ),
                            DecodeHintType.CHARACTER_SET to "UTF-8",
                            DecodeHintType.PURE_BARCODE to true,
                            DecodeHintType.TRY_HARDER to true,
                        )
                    )
                }.decode(binaryBmp)
                onQrCodeScanned(result.text)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                image.close()
            }
        }
    }

    private fun ByteBuffer.toByteArray(): ByteArray {
        rewind()
        return ByteArray(remaining()).also {
            get(it)
        }
    }
}