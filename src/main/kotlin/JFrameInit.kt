package net.klonoa9x6

import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Rectangle
import java.util.concurrent.TimeUnit
import javax.swing.*

object JFrameInit {
    operator fun invoke() {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        frame.isUndecorated = true
        frame.preferredSize = Dimension(852, 600)
        frame.background = Color(0,0,0,0)
        val bg = ImageIcon(classLoader.getResource("bg_klo.png"))
        val labelBackGround = JLabel(bg)
        labelBackGround.size = frame.preferredSize
        //labelLogger.bounds = Rectangle(50, 50, 752, 500)    // 这里改日志显示的区域
        labelLogger.bounds = Rectangle(50, 355, 752, 195)
        labelLogger.font = Font("Consolas", Font.BOLD, 12)
        labelLogger.foreground = Color(255,255,255)
        labelLogger.background = Color(0,0,0,0)
        labelLogger.horizontalAlignment = SwingConstants.LEFT
        labelLogger.verticalAlignment = SwingConstants.TOP
        scrollPane.bounds = labelLogger.bounds
        scrollPane.background = labelLogger.background
        scrollPane.isOpaque = false
        scrollPane.viewport.isOpaque = false
        scrollPane.border = null
        scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_NEVER
        scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        val quoteGIF = ImageIcon(classLoader.getResource("quote.gif"))
        val labelQuote = JLabel(quoteGIF)
        labelQuote.bounds = Rectangle(802, 550, 32, 32)
        val frameIcon = ImageIcon(classLoader.getResource("icon.png"))  // 任务栏图标部分
        frame.iconImage = frameIcon.image
        frame.layeredPane.add(scrollPane,0)
        frame.layeredPane.add(labelQuote,1)
        frame.layeredPane.add(labelBackGround,2)
        frame.isResizable = false
        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.opacity = 0f
    }
    fun frameFadeIn() {
        // JFrame窗口淡入效果
        for (i in 0..100 step 5) {
            frame.opacity = (i.toFloat() / 100f)
            TimeUnit.MILLISECONDS.sleep(10)
        }
    }
    fun frameFadeOut() {
        // JFrame窗口淡出效果
        for (i in 100 downTo 0 step 5) {
            frame.opacity = (i.toFloat() / 100f)
            TimeUnit.MILLISECONDS.sleep(10)
        }
    }
}
