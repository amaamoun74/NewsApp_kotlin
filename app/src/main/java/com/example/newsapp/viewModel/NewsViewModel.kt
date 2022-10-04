package com.example.newsapp.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.model.Article
import com.example.newsapp.model.NewsResponse
import com.example.newsapp.repo.NewsRepository
import com.example.newsapp.util.Constants
import com.example.newsapp.util.ResponseState
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(
    private val newsRepository: NewsRepository
):ViewModel() {

     var newsPageNumber = 1
    val news: MutableLiveData<ResponseState<NewsResponse>> = MutableLiveData()
    private var newsResponse:NewsResponse? = null

     var searchNewsPageNumber = 1
    val searchNews: MutableLiveData<ResponseState<NewsResponse>> = MutableLiveData()
    private var searchNewsResponse:NewsResponse? = null

    init {
        getNews(Constants.COUNTRY_CODE)
    }

    fun getNews(countryCode: String) = viewModelScope.launch {
        news.postValue(ResponseState.Loading())
        val response = newsRepository.getNews(countryCode, newsPageNumber)
        news.postValue(checkNewsResponse(response))
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