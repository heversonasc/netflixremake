package co.tiagoaguiar.netflixremake

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.tiagoaguiar.netflixremake.model.Category
import co.tiagoaguiar.netflixremake.model.Movie
import co.tiagoaguiar.netflixremake.util.CategoryTesk

class MainActivity : AppCompatActivity(), CategoryTesk.Callback {

    private lateinit var progress : ProgressBar
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        progress = findViewById(R.id.progress_main)

        adapter = CategoryAdapter(categories){ id ->
            val intent = Intent (this@MainActivity, MovieActivity::class.java)
            intent.putExtra("id", id)
            startActivity(intent)
        }
        val rv: RecyclerView = findViewById(R.id.rv_main)
        rv.layoutManager = LinearLayoutManager(this,  RecyclerView.VERTICAL, false)
        rv.adapter = adapter

        CategoryTesk(this).execute("https://api.tiagoaguiar.co/netflixapp/home?apiKey=a1cab02b-d194-4526-b52a-85da3b8f6534")
    }

    override fun onPreExecute() {
        progress.visibility = View.VISIBLE

    }

    override fun onResult(categories: List<Category>) {
        // aqui ser[a qndo o categorytask chamará de volta ou seja o famoso callback ou listener
        this.categories.clear()
        this.categories.addAll(categories)
        adapter.notifyDataSetChanged() // forçar o adapter a chamar de novo o onBindViewHolder...

        progress.visibility = View.GONE
    }


    override fun onFailure(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        progress.visibility = View.GONE
    }

}