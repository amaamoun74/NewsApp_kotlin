package com.example.newsapp.ui.fragments
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentNewsBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Constants.Companion.COUNTRY_CODE
import com.example.newsapp.util.Constants.Companion.NEWS_ARTICLE_KEY
import com.example.newsapp.util.Constants.Companion.PAGE_SIZE
import com.example.newsapp.util.ResponseState
import com.example.newsapp.viewModel.NewsViewModel

class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
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
            val isTotalMoreThanVisible = totalItemCount >= PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotLastPage &&
                    isAtLastItem && isNotAtFirst &&
                    isTotalMoreThanVisible && isScrolling

            if (shouldPaginate){
                newsViewModel.getNews(COUNTRY_CODE)
                isScrolling = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNewsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        getData()
        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable(NEWS_ARTICLE_KEY,it)
            }
            findNavController().navigate(R.id.action_newsFragment_to_articleFragment,bundle)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView(){
        newsAdapter = NewsAdapter()
        binding.newsList.apply {
            adapter= newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(this@NewsFragment.scrollListener)
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

    private fun onError(errorMessage : String) {
        hideProgressBar()
        binding.contentLayout.visibility = View.GONE
        binding.animationLayout.visibility = View.VISIBLE
        binding.tvErrorMessage.text = errorMessage
    }

    private fun getData(){
        newsViewModel = (activity as MainActivity).viewModel
        newsViewModel.news.observe(viewLifecycleOwner) { response ->
            when (response) {
                is ResponseState.Error -> {
                    onError(response.message.toString())
                }
                is ResponseState.Loading -> {
                    showProgressBar()
                }
                is ResponseState.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPage = newsResponse.totalResults / PAGE_SIZE +  2
                        isLastPage = newsViewModel.newsPageNumber == totalPage
                        if (isLastPage){
                            binding.newsList.setPadding(0,0,0,0)
                        }
                    }
                }
            }
        }
    }
}