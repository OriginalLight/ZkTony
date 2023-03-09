package com.zktony.common.http.download

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import androidx.core.net.toUri
import com.zktony.common.app.CommonApplicationProxy
import java.io.File
import java.util.concurrent.Executors


/**
 * A class that manages downloads.
 */
object DownloadManager {

    private val fixedThreadPool = Executors.newFixedThreadPool(3)

    /**
     * Starts a download.
     *
     * @param url the URL to download
     * @param listener a listener to receive notifications about the download
     */
    @SuppressLint("ServiceCast", "Range")
    fun startDownload(url: String, listener: DownloadListener) {
        // get the download manager
        val downloadManager =
            CommonApplicationProxy.application.getSystemService(DOWNLOAD_SERVICE) as DownloadManager

        // create a request for the download
        val request = DownloadManager.Request(url.toUri())
        val fileName = url.substring(url.lastIndexOf("/") + 1)
        // if exists, overwrite
        request.setDestinationInExternalFilesDir(
            CommonApplicationProxy.application,
            null,
            fileName
        )
        val downloadId = downloadManager.enqueue(request)

        fixedThreadPool.execute {
            var downloading = true
            while (downloading) {
                val query = DownloadManager.Query()
                query.setFilterById(downloadId)
                val cursor = downloadManager.query(query)
                cursor.moveToFirst()
                val bytesDownloaded =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                listener.onProgress(bytesDownloaded, bytesTotal)

                val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))

                if (status == DownloadManager.STATUS_FAILED) {
                    downloading = false
                    downloadManager.remove(downloadId)
                    listener.onError(Exception("Download failed"))
                }

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    downloading = false
                    listener.onComplete(
                        File(
                            CommonApplicationProxy.application.getExternalFilesDir(null),
                            fileName
                        )
                    )
                }
                cursor.close()
            }
        }
    }
}