package net.klonoa9x6

import com.alibaba.fastjson2.parseArray
import com.alibaba.fastjson2.parseObject
import com.alibaba.fastjson2.toJSONString
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.mainBody
import org.apache.commons.codec.digest.DigestUtils
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

/*
    淦NM，fastjson没法在native-image里面用
*/

val frame = JFrame("Lunatea Updater")
var outLog: String = ""
val labelLogger = JLabel("")
val scrollPane = JScrollPane(labelLogger)
val classLoader: ClassLoader = Thread.currentThread().getContextClassLoader()
val logger: Logger = Logger.getLogger("LunateaUpdater")

fun outputConsole(text: String, toConsole: Boolean){
    if (toConsole) {
        println(text)
    }
    outLog += text + "\n"
    labelLogger.text = "<html><body>${outLog.replace("\n","<br>")}</body></html>"
    TimeUnit.MILLISECONDS.sleep(20)
    scrollPane.verticalScrollBar.value = scrollPane.verticalScrollBar.maximum
}

fun main(args: Array<String>) = mainBody {
    //println("Lunatea Updater by KLONOA9X6 & KLuoNuoYa")
    outputConsole("""
        -----------------------------------------
        Lunatea Updater by KLONOA9X6 & KLuoNuoYa
        -----------------------------------------
    """.trimIndent(), true)
    if (args.isEmpty()) {
        outputConsole("No arguments provided", true)
        outputConsole("Use -h or --help for help", true)
        exitProcess(1)
    }
    val parserArgs = ArgParser(args).parseInto(::ArgHelper)
    parserArgs.run {
        val executorService = Executors.newFixedThreadPool(thread)
        SwingUtilities.invokeLater {
            JFrameInit()    //初始化GUI
            if (!silent) {
                frame.isVisible = true
                JFrameInit.frameFadeIn()
            }
        }
        // 向服务端请求整合包信息
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("$updateUrl/server-manifest.json"))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        val manifestJson = response.body().toString()
        val manifest = manifestJson.parseObject()
        val packName = manifest["name"]
        val packAuthor = manifest["author"]
        val packVersion = manifest["version"]
        val packDescription = manifest["description"]
        val addonsList = manifest["addons"].toString().parseArray()
        outputConsole(
            """
Pack Name: $packName
Pack Author: $packAuthor
Pack Version: $packVersion
Pack Description:
$packDescription
        """.trimIndent()
        ,true)
        addonsList.withIndex().forEach { (_, addon) ->
            val addonID = addon.toJSONString().parseObject()["id"]
            val addonVersion = addon.toJSONString().parseObject()["version"]
            outputConsole("$addonID: $addonVersion",true)
        }
        outputConsole("File checking start in 2 seconds...",true)
        TimeUnit.SECONDS.sleep(2)
        val filesList = manifest["files"].toString().parseArray()
        var checkStatus = true
        filesList.withIndex().forEach { (_, file) ->
            executorService.execute {   // 瞎整的多线程
                if (!asyncFilesCheck(file, gamePath, updateUrl)) {
                    checkStatus = false
                }
                executorService.shutdown()
            }
        }
        executorService.awaitTermination(6, TimeUnit.HOURS)
        if (!checkStatus) {
            outputConsole("""
                ---------------------------------------------------------------------
                A error occurred while checking files, something may not work well.
                ---------------------------------------------------------------------
                """.trimIndent(),true)
        } else {
            outputConsole("""
                -------------------------
                File checking success.
                Have a good time!
                Ciallo～(∠・ω< )⌒★
                -------------------------
                """.trimIndent(),true)
        }
        TimeUnit.SECONDS.sleep(2)
        if (!silent) {
            JFrameInit.frameFadeOut()
        }
        frame.dispose()
    }
}

fun asyncFilesCheck(file: Any, gamePath: String, updateUrl: String) : Boolean {
    val serverFilePath = file.toJSONString().parseObject()["path"].toString()
    val serverFileHash = file.toJSONString().parseObject()["hash"].toString()
    val clientFile = File("$gamePath/$serverFilePath")
    return checkClientFile(clientFile, serverFilePath, serverFileHash, gamePath, updateUrl)
}

fun checkClientFile(
    clientFile: File,
    serverFilePath: String,
    serverFileHash: String,
    gamePath: String,
    updateUrl: String
): Boolean {
    if (clientFile.exists()) {
        val clientFileHash = DigestUtils.sha1Hex(clientFile.readBytes())
        if (clientFileHash == serverFileHash) {
            logger.info("$serverFilePath is matched to server version.")
            outputConsole("$serverFilePath is matched to server version.",false)
            return true
        } else {
            return downloadFile(clientFile, "$updateUrl/overrides/$serverFilePath")
        }
    } else {
        return downloadFile(clientFile, "$updateUrl/overrides/$serverFilePath")
    }
}

fun downloadFile(file: File, url: String): Boolean {
    // 下载文件到file，下载地址为url
    // 检测到文件存在时自动备份原文件
    if (file.exists()) {
        // 备份原文件
        val backupFile = File(file.absolutePath + ".bak")
        outputConsole("Backup file to ${backupFile.name}",false)
        if (!file.renameTo(backupFile)) {
            logger.severe("Failed to backup file: ${file.absolutePath}")
            outputConsole("Failed to backup file: ${file.absolutePath}",false)
            return false
        }
    } else {
        val directory = file.parentFile
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                logger.severe("Failed to create directory: ${directory.absolutePath}")
                outputConsole("Failed to create directory: ${directory.absolutePath}",false)
                return false
            }
        }
        // 创建空文件
        file.createNewFile()
    }
    try {
        logger.info("Downloading file from $url to ${file.absolutePath}")
        outputConsole("Downloading file ${file.name}",false)
        DownloadUtils.fileDownload(url,file)
        logger.info("Downloaded ${file.name}")
        outputConsole("Downloaded ${file.name}",false)
        return true
    } catch (e: Exception) {
        logger.severe("Download ${file.name} failed: ${e.message}")
        outputConsole("Download ${file.name} failed: ${e.message},",false)
        return false
    }
}
