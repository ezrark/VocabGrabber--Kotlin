
import org.w3c.dom.NodeList
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.collections.ArrayList

class VocabGrabber {
    
    companion object {
        
        private lateinit var definitions: ArrayList<String>
         internal lateinit var POS: ArrayList<ArrayList<String>>
        internal val scanner = Scanner(System.`in`)
    
        private fun dictionaryEntries(word: String): String {
            val language = "en"
            val wordId = word.toLowerCase() //word id is case sensitive and lowercase is required
            return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + wordId + "/definitions"
        }
        
        fun mainThing(word: String): ArrayList<String> {
            definitions = ArrayList(1)
            POS = ArrayList(1)
            val docFac = DocumentBuilderFactory.newInstance()
            val docBuild = docFac.newDocumentBuilder()
            val keyDoc = docBuild.parse("api_keys.xml")
            val resourceList: NodeList = keyDoc.getElementsByTagName("resources")
            val appId: String = resourceList.item(0).childNodes.item(1).textContent
            val appKey = resourceList.item(0).childNodes.item(3).textContent
            
            try {
                val url = URL(dictionaryEntries(word))
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.setRequestProperty("app_id", appId)
                urlConnection.setRequestProperty("app_key", appKey)
                
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                
                var defCount = 0
                while (true) {
                    val line: String = reader.readLine()
                    if (line == "                                    \"definitions\": [") {
                        val definition: String = (reader.readLine()).drop(1).dropLast(1)
                        definitions.add(definition.drop(definition.indexOf('"') + 1))
                        defCount++
                    }
                    if (line == "                    \"language\": \"en\",") {
                        val partSpeech: String = (reader.readLine()).drop(1).dropLast(2)
                        POS.add(arrayListOf(defCount.toString(), partSpeech.drop(partSpeech.indexOf(':') + 3)))
                    }
                }
            } catch (e: Exception) {
                return definitions
            }
        }
        
        fun findPart(defInt: Int, partArray: ArrayList<ArrayList<String>>): String {
            for (part in partArray) {
                if (defInt <= (part[0].toInt())) {
                    return part[1]
                }
            }
            return "N/A"
        }
        
        fun addToFile(fileName: File, word: String, part: String, defin: String) {
            fileName.appendText("""\item """ + "$word $part: $defin\n")
        }
        
        fun prepTex(fileName: File) {
            fileName.appendText("""\documentclass{article}

\usepackage[utf8]{inputenc}
\usepackage[letterpaper, portrait, margin=1in]{geometry}
\usepackage{setspace}

\begin{document}
\doublespacing
\begin{enumerate}
""")
        }
    }
}

fun main(args: Array<String>) {
    val file = File("vocab.tex")
    VocabGrabber.prepTex(file)
    do {
        println("Type the word")
        val word = VocabGrabber.scanner.next()
        val wordArray: ArrayList<String> = VocabGrabber.mainThing(word)
        for (string in wordArray) {
            println(string)
        }
        println("Which definition?")
        val defToUse: Int = VocabGrabber.scanner.nextInt()
        VocabGrabber.addToFile(file, word.capitalize(), "(${VocabGrabber.findPart(defToUse, VocabGrabber.POS)})".capitalize(), wordArray[defToUse - 1].capitalize())
        println("q to quit")
    } while (VocabGrabber.scanner.next() != "q")
    file.appendText("""\end{enumerate}
        |\end{document}
    """.trimMargin())
}