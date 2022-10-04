package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentSearchBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.ResponseState
import com.example.newsapp.viewModel.NewsViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter

    private var isLoading = false
    private var isScrolling = false
    private var isLastPage = false
    private var scrollListener = object : RecyclerView.OnScrollListener(){
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                isScrolling = true
            }
        }
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtFirst = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage &&
                    isAtLastItem && isNotAtFirst &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate){
                newsViewModel.getNews(Constants.COUNTRY_CODE)
                isScrolling = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        setupRecyclerView()
        getData()
        var job: Job? = null
        binding.etSearch.addTextChangedListener { text: Editable? ->
            job?.cancel()
            job = MainScope().launch {
                delay(350)
                text?.let {
                    if (text.toString().isNotEmpty()){
                        newsViewModel.searchNews(text.toString())
                    }
                }
            }
        }

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable(Constants.NEWS_ARTICLE_KEY,it)
            }
            findNavController().navigate(R.id.action_searchFragment_to_articleFragment,bundle)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        binding.rvSearchNews.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@SearchFragment.scrollListener)
        }

    }

    private fun hideProgressBar() {
        binding.paginationProgressBar.visibility = View.GONE
        isLoading = false
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun onError() {
        //binding.itemErrorMessage.tvErrorMessage = View.VISIBLE
    }

    private fun getData() {
        newsViewModel = (activity as MainActivity).viewModel
        newsViewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseState.Error -> {
                    hideProgressBar()
                }
                is ResponseState.Loading -> {
                    showProgressBar()
                    Log.d("TAG ", response.message.toString())
                }
                is ResponseState.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPage = newsResponse.totalResults / Constants.PAGE_SIZE + 2
                        isLastPage = newsViewModel.searchNewsPageNumber == totalPage
                        if (isLastPage) {
                            binding.rvSearchNews.setPadding(0, 0, 0, 0)
                        }
                    }
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}