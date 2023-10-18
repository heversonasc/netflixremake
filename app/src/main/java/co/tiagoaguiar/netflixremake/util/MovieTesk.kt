package co.tiagoaguiar.netflixremake.util

import android.os.Handler
import android.os.Looper
import android.renderscript.ScriptGroup
import android.util.Log
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.model.MovieDetail
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.lang.Exception
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors
import javax.net.ssl.HttpsURLConnection

class MovieTesk(private val callback: Callback) {

    private val handler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    interface Callback {
        fun onPreExecute()
        fun onResult(movieDetail: MovieDetail)
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

                if (statusCode == 400){
                    stream = urlConnection.errorStream
                    buffer = BufferedInputStream(stream)
                    val jsonAsString = toString(buffer)


                    val json = JSONObject(jsonAsString)
                    val message= json.getString("message")
                    throw IOException(message)

                } else if (statusCode > 400) {
                    throw IOException("Erro na comunicação com o servidor!")
                }


               stream = urlConnection.inputStream // sequencia de bytes
                // val jsonAsString = stream.bufferedReader().use { it.readText() } // bytes -> strings

                buffer = BufferedInputStream(stream)
                val  jsonAsString = toString(buffer)

                val movieDetail = toMovieDatil(jsonAsString)

                handler.post{
                    // aqui roda dentro da UI-thread novamente
                    callback.onResult(movieDetail)
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

    private fun toMovieDatil (jsonAsString: String): MovieDetail{
        val json = JSONObject(jsonAsString)

        val id = json.getInt("id")
        val title = json.getString("title")
        val desc = json.getString("desc")
        val cast = json.getString("cast")
        val coverUrl = json.getString("cover_url")
        val jsonMovies = json.getJSONArray("movie")


        val similars = mutableListOf<Movie>()
        for (i in 0 until jsonMovies.length()) {
            val jsonMovie = jsonMovies.getJSONObject(i)

            val similiarId = jsonMovie.getInt("id")
            val similiarCoverUrl= jsonMovie.getString("cover_url")


            val m = Movie(similiarId, similiarCoverUrl)
            similars.add(m)
        }

        val movie = Movie(id, coverUrl, title, desc, cast)

        return MovieDetail(movie, similars)
    }


    private fun toString(stream: InputStream): String{
        val bytes = ByteArray(1024)
        val baos = ByteArrayOutputStream()
        var read:Int

        while(true){
            read = stream.read(bytes)
            if (read <= 0) {
                break
            }
            baos.write(bytes, 0, read)

        }
        return String(baos.toByteArray())
    }



}