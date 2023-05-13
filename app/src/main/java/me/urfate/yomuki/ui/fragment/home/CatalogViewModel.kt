package me.urfate.yomuki.ui.fragment.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.db.AppDatabase.Companion.getInstance
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.SourceManager

class CatalogViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase?
    private var updates: MutableLiveData<List<Book>>
    private var populars: MutableLiveData<List<Book>>

    init {
        database = getInstance(application)
        updates = MutableLiveData()
        populars = MutableLiveData()
    }

    fun getUpdates(): LiveData<List<Book>> {
        loadUpdates()
        return updates
    }

    fun getPopulars(): LiveData<List<Book>> {
        loadPopulars()
        return populars
    }

    private fun loadUpdates() {
        viewModelScope.launch {
            val source = withContext(Dispatchers.IO) {
                SourceManager.instance.fromUrl(database!!.sourceDao()!!.findDefault()!!.url)
            }

            val books = withContext(Dispatchers.IO) {
                source!!.fetchUpdatedBooks()
            }
            updates.postValue(books)
        }
    }

    private fun loadPopulars() {
        viewModelScope.launch {
            val source = withContext(Dispatchers.IO) {
                SourceManager.instance.fromUrl(database!!.sourceDao()!!.findDefault()!!.url)
            }

            val books = withContext(Dispatchers.IO) {
                source!!.fetchPopularBooks()
            }
            populars.postValue(books)
        }
    }
}