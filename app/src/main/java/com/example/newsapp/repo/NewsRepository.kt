package com.example.newsapp.repo

import android.app.DownloadManager.Query
import com.example.newsapp.databaseCashing.NewsDatabase
import com.example.newsapp.webServices.WebService

class NewsRepository (
    val database: NewsDatabase
        ){
    suspend fun getNews(countryCode:String , paging: Int) =
        WebService.api.getNews(countryCode,paging)

    suspend fun searchNews(query: String , paging: Int) =
        WebService.api.searchForNews(query,paging)
}