import java.io.File

fun main() {
    val file = File("app/src/main/java/com/example/presentation/ui/Screens.kt")
    val content = file.readText()
    
    var braceCount = 0
    var inString = false
    var escape = false
    
    val lineStarts = mutableListOf<Int>()
    var lineNum = 1
    
    val unbalancedLines = mutableListOf<Int>()

    for (i in content.indices) {
        val c = content[i]
        
        if (inString) {
            if (escape) {
                escape = false
            } else if (c == '\\') {
                escape = true
            } else if (c == '"') {
                inString = false
            }
            if (c == '\n') lineNum++
            continue
        }
        
        if (c == '"') {
            inString = true
        } else if (c == '{') {
            braceCount++
            unbalancedLines.add(lineNum)
        } else if (c == '}') {
            braceCount--
            if (unbalancedLines.isNotEmpty()) {
                unbalancedLines.removeAt(unbalancedLines.size - 1)
            }
        } else if (c == '\n') {
            lineNum++
        }
    }

    println("Final Brace Count: \$braceCount")
    if (braceCount > 0) {
        println("Unmatched '{' opened at lines:")
        unbalancedLines.forEach { println(it) }
    } else if (braceCount < 0) {
        println("Too many '}' closed")
    } else {
        println("Perfectly balanced!")
    }
}
