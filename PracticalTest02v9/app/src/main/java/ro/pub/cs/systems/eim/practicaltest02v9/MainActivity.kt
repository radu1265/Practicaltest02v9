package ro.pub.cs.systems.eim.practicaltest02v9

//package com.example.mydictionary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
    private lateinit var buttonOpenMap: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inițializăm view-urile
        editTextWord = findViewById(R.id.editTextWord)
        editTextMinLetters = findViewById(R.id.editTextMinLetters)
        buttonRequest = findViewById(R.id.buttonRequest)
        textViewResult = findViewById(R.id.textViewResult)
        buttonOpenMap = findViewById(R.id.buttonOpenMap)

        // Înregistrăm un receiver pentru anagrame
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(anagramReceiver, IntentFilter("ANAGRAM_RESULT"))

        // Când apăsăm butonul "Caută anagrame"
        buttonRequest.setOnClickListener {
            val word = editTextWord.text.toString()
            val minLetters = editTextMinLetters.text.toString().toIntOrNull() ?: 0

            // Facem cererea la API anagramica.com
            requestAnagramsFromWebService(word, minLetters)
        }

        // Când apăsăm butonul "Deschide Harta"
        buttonOpenMap.setOnClickListener {
            val intent = Intent(this, MapsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestAnagramsFromWebService(word: String, minLetters: Int) {
        val url = "http://www.anagramica.com/all/$word"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                response.body?.let { responseBody ->
                    val responseString = responseBody.string()
                    // Afișăm răspunsul brut în LogCat
                    Log.d("ANAGRAM_JSON", "Răspuns brut: $responseString")

                    // Parsăm JSON-ul
                    val jsonObject = JSONObject(responseString)
                    val allArray = jsonObject.optJSONArray("all") ?: return@launch

                    val anagrams = mutableListOf<String>()
                    for (i in 0 until allArray.length()) {
                        val anagramWord = allArray.optString(i, "")
                        anagrams.add(anagramWord)
                    }

                    // Filtrăm anagramele cu lungimea >= minLetters
                    val filtered = anagrams.filter { it.length >= minLetters }

                    Log.d("ANAGRAM_JSON", "Anagrame filtrate: $filtered")

                    // Combinăm într-un string pentru afișare
                    val resultToSend = if (filtered.isNotEmpty()) {
                        filtered.joinToString(separator = "\n")
                    } else {
                        "Nicio anagramă găsită cu lungimea >= $minLetters."
                    }

                    // Trimitem prin broadcast local
                    sendBroadcastWithResult(resultToSend)
                }
            } catch (ex: Exception) {
                Log.e("ANAGRAM_JSON", "Eroare: ${ex.message}")
            }
        }
    }

    private fun sendBroadcastWithResult(result: String) {
        val intent = Intent("ANAGRAM_RESULT")
        intent.putExtra("anagramList", result)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

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