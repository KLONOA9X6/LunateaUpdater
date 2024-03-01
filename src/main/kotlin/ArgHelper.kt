package net.klonoa9x6

import com.xenomachina.argparser.ArgParser

class ArgHelper(parser: ArgParser) {
    val gamePath by parser.storing(
        "-p", "--path",
        help = "Path to the game directory"
    ) {toString()}
    val updateUrl by parser.storing(
        "-u", "--url",
        help = "URL to the update server"
    ) {toString()}
    val silent by parser.flagging(
        "-s", "--silent",
        help = "Silent mode"
    )
}