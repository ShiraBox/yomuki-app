package me.urfate.yomuki.ui.fragment.favourites

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
import me.urfate.yomuki.model.Status
import java.util.*

class FavouritesViewModel(application: Application) : AndroidViewModel(application){
    private val database: AppDatabase?
    private var favouritesData: MutableLiveData<List<Book>>

    init {
        database = AppDatabase.getInstance(application)
        favouritesData = MutableLiveData()
    }

    fun getFavourites(): LiveData<List<Book>> {
        loadFavourites()
        return favouritesData
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            val favouritesEntities: List<BookEntity?>? = withContext(Dispatchers.IO) {
                database?.booksDao()?.findFavorites()
            }

            val favourites: List<Book> = favouritesEntities?.map { bookEntity: BookEntity? ->
                Book(
                    bookEntity!!.name, "", bookEntity.description,
                    bookEntity.coverUrl, bookEntity.releaseYear, Status.UNKNOWN,
                    5f, bookEntity.url, ArrayList(), ArrayList(),
                    0, 0, bookEntity.source
                )
            }?.reversed() ?: Collections.emptyList()

            favouritesData.postValue(favourites)
        }
    }
}