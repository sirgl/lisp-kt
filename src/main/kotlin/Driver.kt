import lexer.Lexer
import lexer.LexerIdentificationLayer
import lexer.LexerImpl
import lexer.remapWithKeywords
import java.io.File

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: compiler <path to file>")
    }
    val path = args.first()

    val text = File(path).readText()
    val lexer =  LexerIdentificationLayer(LexerImpl(), ::remapWithKeywords)
//    lexer.tokenize()

}