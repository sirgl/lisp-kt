import lexer.Lexer
import lexer.LexerImpl

fun main(args: Array<String>) {
    if (args.size != 1) {
        println("Usage: compiler <path to file>")
    }
    val path = args.first()

//    val text = File(path).readText()
//    val lexer =  LexerIdentificationLayer(LexerImpl(), ::remapWithKeywords)
//    lexer.tokenize()

}