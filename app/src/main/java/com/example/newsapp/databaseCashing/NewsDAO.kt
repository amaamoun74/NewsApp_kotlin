package com.example.newsapp.databaseCashing

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.newsapp.model.Article

@Dao
interface NewsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE) // used to replace the news article if it is already exist
    suspend fun insert(article:Article):Long // used to insert or update news article as mentioned above .. long is for ID

    @Query("SELECT * From News" )
    fun getAllSavedNews():LiveData<List<Article>> // live data is mainly used to access data by XML views even landscape or portrait

    @Delete
    suspend fun deleteSavedNews(article: Article)

}