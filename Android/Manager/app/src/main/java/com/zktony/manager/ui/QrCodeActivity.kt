package com.zktony.manager.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.hmsscankit.ScanUtil.RESULT
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions

@Suppress("DEPRECATION")
class QrCodeActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ScanUtil.startScan(
            this,
            REQUEST_CODE_SCAN_ONE,
            HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create()
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != RESULT_OK || data == null) {
            finish()
            return
        }
        if (requestCode == REQUEST_CODE_SCAN_ONE) {
            val obj: HmsScan = data.getParcelableExtra(RESULT)!!
            val intent = Intent()
            intent.putExtra(SCAN_RESULT, obj.getOriginalValue())
            setResult(RESULT_OK, intent)
            finish()
        }

    }

    companion object {
        const val REQUEST_CODE_SCAN_ONE = 0X01
        const val SCAN_RESULT = "SCAN_RESULT"
    }
}