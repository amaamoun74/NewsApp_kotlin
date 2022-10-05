package com.example.newsapp.repo

import com.example.newsapp.databaseCashing.NewsDatabase
import com.example.newsapp.model.Article
import com.example.newsapp.webServices.WebService

class NewsRepository (
    private val database: NewsDatabase
        ){
    suspend fun getNews(countryCode:String , paging: Int) =
        WebService.api.getNews(countryCode,paging)

    suspend fun searchNews(query: String , paging: Int) =
        WebService.api.searchForNews(query,paging)

    suspend fun upsertNews(article: Article) = database.getNewsDao().insert(article)
    fun getSavedNews() = database.getNewsDao().getAllSavedNews()
    suspend fun deleteNews(article: Article) = database.getNewsDao().deleteSavedNews(article)
}
