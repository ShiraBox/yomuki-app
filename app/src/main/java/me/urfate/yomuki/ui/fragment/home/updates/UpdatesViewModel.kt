package me.urfate.yomuki.ui.fragment.home.updates

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.ContentSource
import me.urfate.yomuki.source.SourceManager

class UpdatesViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase?
    private var updatesData: MutableLiveData<List<Book>>

    init {
        database = AppDatabase.getInstance(application)
        updatesData = MutableLiveData()
    }

    fun getUpdates(): LiveData<List<Book>> {
        loadUpdates()
        return updatesData
    }

    private fun loadUpdates() {

        viewModelScope.launch {
            val source = getDefaultSource()

            val updates = withContext(Dispatchers.IO) {
                source?.fetchUpdatedBooks()
            }

            updatesData.postValue(updates!!)
        }
    }

    private suspend fun getDefaultSource(): ContentSource? {
        var source: ContentSource? = null
        viewModelScope.launch {
            val defaultSource = SourceManager.instance.fromUrl(
                withContext(Dispatchers.IO) {
                    database?.sourceDao()!!.findDefault()!!.url
                }
            )

            source = defaultSource
        }.join()

        return source
    }
}