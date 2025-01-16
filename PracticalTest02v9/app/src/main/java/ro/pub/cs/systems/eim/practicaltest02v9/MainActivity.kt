package ro.pub.cs.systems.eim.practicaltest02v9

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var editTextWord: EditText
    private lateinit var editTextMinLetters: EditText
    private lateinit var buttonRequest: Button
    private lateinit var textViewResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editTextWord = findViewById(R.id.editTextWord)
        editTextMinLetters = findViewById(R.id.editTextMinLetters)
        buttonRequest = findViewById(R.id.buttonRequest)
        textViewResult = findViewById(R.id.textViewResult)

        // Înregistrăm un BroadcastReceiver local pentru a recepționa rezultatele
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(anagramReceiver, IntentFilter("ANAGRAM_RESULT"))

        buttonRequest.setOnClickListener {
            val word = editTextWord.text.toString()
            val minLetters = editTextMinLetters.text.toString().toIntOrNull() ?: 0

            // Începe cererea la serviciul web Anagramica
            requestAnagramsFromWebService(word, minLetters)
        }
    }

    /**
     * Metoda care face cererea HTTP către anagramica.com.
     * Ex: GET http://www.anagramica.com/all/nevermind
     */
    private fun requestAnagramsFromWebService(word: String, minLetters: Int) {
        // Construim URL-ul
        val url = "http://www.anagramica.com/all/$word"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                response.body?.let { responseBody ->
                    // Răspuns brut
                    val responseString = responseBody.string()

                    // 3.a) – Afișăm răspunsul complet (JSON) în LogCat
                    Log.d("ANAGRAM_JSON", "Răspuns brut: $responseString")

                    // 3.b) – Parsăm JSON-ul
                    // Formatul e ceva de genul:
                    // { "all": ["denier", "reined", "dime", ...] }
                    val jsonObject = JSONObject(responseString)
                    val allArray = jsonObject.optJSONArray("all") ?: return@launch

                    // Obținem lista de anagrame într-un ArrayList<String>
                    val anagrams = mutableListOf<String>()
                    for (i in 0 until allArray.length()) {
                        val anagramWord = allArray.optString(i, "")
                        anagrams.add(anagramWord)
                    }

                    // Filtrăm anagramele în funcție de lungimea minimă
                    val filtered = anagrams.filter { it.length >= minLetters }

                    // Afișăm rezultatele parsate și filtrate în LogCat
                    Log.d("ANAGRAM_JSON", "Anagrame filtrate: $filtered")

                    // 3.c) – Trimitem datele prin broadcast
                    // Construim un string cu toate anagramele (linie cu linie)
                    val resultToSend = if (filtered.isNotEmpty()) {
                        filtered.joinToString(separator = "\n")
                    } else {
                        "Nicio anagramă cu lungimea >= $minLetters găsită."
                    }

                    sendBroadcastWithResult(resultToSend)
                }
            } catch (ex: Exception) {
                Log.e("ANAGRAM_JSON", "Eroare: ${ex.message}")
            }
        }
    }

    /**
     * Trimite rezultatul printr-un broadcast local
     */
    private fun sendBroadcastWithResult(result: String) {
        val intent = Intent("ANAGRAM_RESULT")
        intent.putExtra("anagramList", result)
        // Folosim LocalBroadcastManager pentru a trimite în interiorul aplicației
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    /**
     * BroadcastReceiver care ascultă după anagrame și actualizează UI
     */
    private val anagramReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val anagrams = intent?.getStringExtra("anagramList") ?: ""
            textViewResult.text = anagrams
        }
    }

    override fun onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(anagramReceiver)
        super.onDestroy()
    }
}
