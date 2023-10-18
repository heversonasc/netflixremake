package co.tiagoaguiar.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.renderscript.ScriptGroup
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class CategoryTesk(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(categories:List<Category>)
        fun onFailure(message:String)

    }

    fun execute(url: String) {
        callback.onPreExecute()

        executor.execute {
            var urlConnection: HttpsURLConnection? = null
            var buffer: BufferedInputStream? = null
            var stream: InputStream? = null
            try {
                val requestURL = URL(url)
                val urlConnection = requestURL.openConnection() as HttpsURLConnection
                urlConnection.readTimeout = 2000 //tempo de leitura (2s)
                urlConnection.connectTimeout = 2000 //tempo de conexão(2s)

                val statusCode = urlConnection.responseCode
                if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }


                val stream = urlConnection.inputStream // sequencia de bytes
                val jsonAsString = stream.bufferedReader().use { it.readText() } // bytes -> strings

                val  categories = toCategories(jsonAsString)

                handler.post{
                    // aqui roda dentro da UI-thread novamente
                    callback.onResult(categories)
                }


            } catch (e: IOException) {
                val message = e.message?: "erro desconhecido"
                Log.e("Teste", e.message ?: "erro desconhecido", e)

                handler.post{
                    callback.onFailure(message)
                }



            } finally {
                urlConnection?.disconnect()
                stream?.close()
                buffer?.close()
            }
        }
    }

    private fun toCategories(jsonAsString: String): List<Category> {
        val categories = mutableListOf<Category>()
        val jsonRoot = JSONObject(jsonAsString)
        val jsonCategories = jsonRoot.getJSONArray("category")
        for ( i in 0 until jsonCategories.length()){
            val jsonCategory = jsonCategories.getJSONObject(i)

            val title = jsonCategory.getString("title")
            val jsonMovies = jsonCategory.getJSONArray("movie")

            val movies = mutableListOf<Movie>()
            for (j in 0 until jsonMovies.length()){
                val jsonMovie = jsonMovies.getJSONObject(j)
                val id = jsonMovie.getInt("id")
                val coverUrl = jsonMovie.getString("cover_url")

                movies.add(Movie(id, coverUrl))

            }

            categories.add(Category(title, movies))

        }


        return categories
    }
}