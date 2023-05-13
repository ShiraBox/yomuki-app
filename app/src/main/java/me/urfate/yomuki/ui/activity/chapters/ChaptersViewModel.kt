package me.urfate.yomuki.ui.activity.chapters

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.model.Chapter
import me.urfate.yomuki.source.ContentSource

class ChaptersViewModel(application: Application) : AndroidViewModel(application){
    private val database: AppDatabase?

    private var chaptersData: MutableLiveData<List<Chapter>>

    init {
        database = AppDatabase.getInstance(application)
        chaptersData = MutableLiveData()
    }

    fun getChapters(source: ContentSource, url: String): LiveData<List<Chapter>> {
        loadChapters(source, url)
        return chaptersData
    }

    private fun loadChapters(source: ContentSource, url: String) {
        viewModelScope.launch {
            val chapters = withContext(Dispatchers.IO) {
                source.fetchBookChapters(url)
            }

            chaptersData.postValue(chapters)
        }
    }
}