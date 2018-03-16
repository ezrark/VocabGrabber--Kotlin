
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import javax.net.ssl.HttpsURLConnection
import kotlin.collections.ArrayList

class VocabGrabber {
    
    companion object {
        
        private lateinit var definitions: ArrayList<String>
         internal lateinit var POS: ArrayList<ArrayList<String>>
        internal val scanner = Scanner(System.`in`)
    
        private fun dictionaryEntries(word: String): String {
            val language = "en"
            //final String word = "disconcerting";
            val wordId = word.toLowerCase() //word id is case sensitive and lowercase is required
            return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + wordId + "/definitions"
        }
        
        fun mainThing(word: String): ArrayList<String> {
            definitions = ArrayList(1)
            POS = ArrayList(1)
            //println("type the word")
            val appId = "5eb25c00"
            val appKey = "832b0e87de4fd6f4da78496826b620e6"
            
            try {
                val url = URL(dictionaryEntries(word))
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.setRequestProperty("Accept", "application/json")
                urlConnection.setRequestProperty("app_id", appId)
                urlConnection.setRequestProperty("app_key", appKey)
                
                // read the output from the server
                val reader = BufferedReader(InputStreamReader(urlConnection.inputStream))
                //val stringBuilder = StringBuilder()
                //lateinit var definitions: ArrayList<String>
                
                //val line: String
                var defCount = 0
                while (true) {
                    val line: String = reader.readLine()
                    if (line == "                                    \"definitions\": [") {
                        val definition: String = (reader.readLine()).drop(1).dropLast(1)
                        definitions.add(definition.drop(definition.indexOf('"') + 1))
                        defCount++
                        //println(definition)
                    }
                    if (line == "                    \"language\": \"en\",") {
                        val partSpeech: String = (reader.readLine()).drop(1).dropLast(2)
                        POS.add(arrayListOf(defCount.toString(), partSpeech.drop(partSpeech.indexOf(':') + 3)))
                        //defCount == 0
                    }
                }
            } catch (e: Exception) {
                //e.printStackTrace()
                //definitions.add(e.toString())
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
            //val writer = FileWriter
            fileName.appendText("""\item """ + "$word $part: $defin\n")
            //File("vocab.txt").writeText(word + ": " + defin)
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
    VocabGrabber.Companion.prepTex(file)
    do {
        println("Type the word")
        val word = VocabGrabber.Companion.scanner.next()
        val wordArray: ArrayList<String> = VocabGrabber.Companion.mainThing(word)
        for (string in wordArray) {
            println(string)
        }
        println("Which definition?")
        val defToUse: Int = VocabGrabber.Companion.scanner.nextInt()
        VocabGrabber.Companion.addToFile(file, word.capitalize(), "(${VocabGrabber.Companion.findPart(defToUse, VocabGrabber.Companion.POS)})".capitalize(), wordArray[defToUse - 1].capitalize())
        println("q to quit")
    } while (VocabGrabber.Companion.scanner.next() != "q")
    file.appendText("""\end{enumerate}
        |\end{document}
    """.trimMargin())
}