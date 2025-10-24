package com.example.lab6_cattv

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

// Lab 6 - AsyncTask and Android TV
// This app shows random cat pictures using AsyncTask and updates a progress bar.

class MainActivity : AppCompatActivity() {

    private lateinit var catImage: ImageView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        catImage = findViewById(R.id.catImage)
        progressBar = findViewById(R.id.progressBar)

        // start background task to load cat images
        CatImages().execute()
    }

    inner class CatImages : AsyncTask<Void, Int, Bitmap?>() {

        override fun doInBackground(vararg params: Void?): Bitmap? {
            while (true) {
                try {
                    // get random cat JSON
                    val jsonUrl = URL("https://cataas.com/cat?json=true")
                    val connection = jsonUrl.openConnection() as HttpURLConnection
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val jsonText = reader.readText()
                    reader.close()

                    val jsonObj = JSONObject(jsonText)
                    val id = jsonObj.getString("id")
                    val imageUrl = "https://cataas.com/cat/$id"

                    // check if we already have the image saved
                    val file = File(filesDir, "$id.jpg")
                    val bitmap: Bitmap = if (file.exists()) {
                        BitmapFactory.decodeFile(file.absolutePath)
                    } else {
                        // download and save image
                        val inputStream = URL(imageUrl).openStream()
                        val bmp = BitmapFactory.decodeStream(inputStream)
                        val fos = FileOutputStream(file)
                        bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        fos.close()
                        bmp
                    }

                    // progress bar animation
                    for (i in 0..100) {
                        publishProgress(i)
                        Thread.sleep(30)
                    }

                    // show the new cat image
                    runOnUiThread { catImage.setImageBitmap(bitmap) }

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        override fun onProgressUpdate(vararg values: Int?) {
            val progress = values[0] ?: 0
            if (progress in 0..100) {
                progressBar.progress = progress
            }
        }
    }
}
