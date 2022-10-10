package com.example.newsapp.viewModel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.newsapp.NewsApplication
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.repo.NewsRepository
import com.example.newsapp.util.Constants.Companion.COUNTRY_CODE
import com.example.newsapp.util.ResponseState
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class NewsViewModel(
    private val newsRepository: NewsRepository,
    private val app: Application
): AndroidViewModel(app) {

     var newsPageNumber = 1
    val news: MutableLiveData<ResponseState<NewsResponse>> = MutableLiveData()
    private var newsResponse:NewsResponse? = null

     var searchNewsPageNumber = 1
    val searchNews: MutableLiveData<ResponseState<NewsResponse>> = MutableLiveData()
    private var searchNewsResponse:NewsResponse? = null

    init {
        getNews(COUNTRY_CODE)
    }
    private fun hasInternetConnection(): Boolean {
        val connectivityManager = getApplication<NewsApplication>().getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities =
                connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.activeNetworkInfo?.run {
                return when (type) {
                    ConnectivityManager.TYPE_WIFI -> true
                    ConnectivityManager.TYPE_MOBILE -> true
                    ConnectivityManager.TYPE_ETHERNET -> true
                    else -> false
                }
            }
        }
        return false
    }

    fun getNews(countryCode: String) = viewModelScope.launch {
        safeNewsCall(countryCode)
    }
    private fun checkNewsResponse(response: Response<NewsResponse>) : ResponseState<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                newsPageNumber ++
                if (newsResponse == null){
                    newsResponse = resultResponse
                }
                else{
                    val oldNews = newsResponse?.articles
                    val newNews = resultResponse.articles
                    oldNews?.addAll(newNews)
                }
                return ResponseState.Success(newsResponse?:resultResponse)
            }
        }
            return ResponseState.Error(response.message())
    }

    private suspend fun safeNewsCall(countryCode: String) {
        news.postValue(ResponseState.Loading())
        try {
            if (hasInternetConnection()) {
                val response = newsRepository.getNews(countryCode, newsPageNumber)
                news.postValue(checkNewsResponse(response))
            } else {
                news.postValue(ResponseState.Error("No internet connection"))
            }
        } catch (t: Throwable) {
            when (t) {
                is IOException -> news.postValue(ResponseState.Error("Network Failure"))
                else -> news.postValue(ResponseState.Error("Conversion Error"))
            }
        }
    }


    fun searchNews(countryCode: String) = viewModelScope.launch {
        searchNews.postValue(ResponseState.Loading())
        val response = newsRepository.searchNews(countryCode, searchNewsPageNumber)
        searchNews.postValue(checkSearchNewsResponse(response))
    }
    private fun checkSearchNewsResponse(response: Response<NewsResponse>) : ResponseState<NewsResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPageNumber ++
                if (searchNewsResponse == null){
                    searchNewsResponse = resultResponse
                }
                else{
                    val oldNews = searchNewsResponse?.articles
                    val newNews = resultResponse.articles
                    oldNews?.addAll(newNews)
                }
                return ResponseState.Success(searchNewsResponse?:resultResponse)
            }
        }
        return ResponseState.Error(response.message())
    }

    fun saveNews(article: Article) = viewModelScope.launch {
        newsRepository.upsertNews(article)
    }
    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteNews(article: Article) = viewModelScope.launch {
        newsRepository.deleteNews(article)
    }
}