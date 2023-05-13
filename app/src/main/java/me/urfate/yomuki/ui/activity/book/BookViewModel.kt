package me.urfate.yomuki.ui.activity.book

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.urfate.yomuki.db.AppDatabase
import me.urfate.yomuki.db.entity.BookEntity
import me.urfate.yomuki.model.Book
import me.urfate.yomuki.source.ContentSource

class BookViewModel(application: Application) : AndroidViewModel(application) {
    private val database: AppDatabase?

    private var bookData: MutableLiveData<Book?>
    private var bookEntityData: MutableLiveData<BookEntity?>

    init {
        database = AppDatabase.getInstance(application)
        bookData = MutableLiveData()
        bookEntityData = MutableLiveData()
    }

    fun getBook(source: ContentSource, url: String): LiveData<Book?> {
        loadBook(source, url)
        return bookData
    }

    fun getBookEntity(url: String): LiveData<BookEntity?> {
        loadBookEntity(url)
        return bookEntityData
    }

    private fun loadBookEntity(url: String) {
        viewModelScope.launch {
            val bookEntity: BookEntity? = withContext(Dispatchers.IO) {
                database?.booksDao()?.findByUrl(url)
            }
            bookEntityData.postValue(bookEntity)
        }
    }

    private fun loadBook(source: ContentSource, url: String) {
        viewModelScope.launch {
            val book = withContext(Dispatchers.IO) {
                source.fetchBook(url)
            }

            bookData.postValue(book)
        }
    }

    fun insertBookEntity(bookEntity: BookEntity) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                database?.booksDao()?.insertAll(bookEntity)
            }
        }
    }
}