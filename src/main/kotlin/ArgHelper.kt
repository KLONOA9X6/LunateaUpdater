package net.klonoa9x6

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default

class ArgHelper(parser: ArgParser) {
    val gamePath by parser.storing(
        "-p", "--path",
        help = "Path to the game directory"
    ) {toString()}
    val updateUrl by parser.storing(
        "-u", "--url",
        help = "URL to the update server"
    ) {toString()}
    val thread by parser.storing(
        "-t", "--thread",
        help = "Number of threads to use when checking files"
    ) {toInt()}.default(4).addValidator {
        if (value < 1) {
            throw IllegalArgumentException(
                "Thread count must be greater than 0")
        }
    }
    val silent by parser.flagging(
        "-s", "--silent",
        help = "Silent mode"
    )
}