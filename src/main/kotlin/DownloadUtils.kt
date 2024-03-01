package net.klonoa9x6

import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object DownloadUtils {
    @Throws(Exception::class)
    fun fileDownload(url: String, file: File) {
        val urlConnection = URL(url).openConnection() as HttpURLConnection
        urlConnection.setConnectTimeout(30000)
        urlConnection.setReadTimeout(30000)
        val fileSize = urlConnection.contentLength
        val inputStream = BufferedInputStream(urlConnection.inputStream)
        val outputStream = FileOutputStream(file)
        val dataBuffer = ByteArray(1024)
        var bytesRead = inputStream.read(dataBuffer, 0, 1024)
        var totalBytesRead = 0L
        var oldTotalBytesRead = 0L
        var lastProgressUpdate = System.currentTimeMillis() / 1000L
        while (bytesRead != -1) {
            outputStream.write(dataBuffer, 0, bytesRead)
            totalBytesRead += bytesRead
            bytesRead = inputStream.read(dataBuffer, 0, 1024)
            val currentTime = System.currentTimeMillis() / 1000L
            if (currentTime > lastProgressUpdate) {
                val progress = (totalBytesRead * 100 / fileSize).toInt()
                outputConsole("$progress% (${(totalBytesRead-oldTotalBytesRead) / 1024} KB/s) (${totalBytesRead / 1024}/${fileSize / 1024}KB) of ${file.name}",true)
                lastProgressUpdate = currentTime
                oldTotalBytesRead = totalBytesRead
            }
        }
        outputStream.close()
        inputStream.close()
        urlConnection.disconnect()
    }
}