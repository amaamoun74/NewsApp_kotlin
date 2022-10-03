package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.databinding.FragmentNewsBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.util.Constants.Companion.NEWS_ARTICLE_KEY
import com.example.newsapp.util.ResponseState
import com.example.newsapp.viewModel.NewsViewModel
import kotlinx.coroutines.Job


class NewsFragment : Fragment() {
    private var _binding: FragmentNewsBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsViewModel: NewsViewModel
    private lateinit var newsAdapter: NewsAdapter


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
        }

    }

    private fun hideProgressBar() {
    binding.paginationProgressBar.visibility = View.GONE
    }

    private fun showProgressBar() {
        binding.paginationProgressBar.visibility = View.VISIBLE
    }
    private fun onError() {
        //binding.itemErrorMessage.tvErrorMessage = View.VISIBLE
    }

    private fun getData(){
        newsViewModel = (activity as MainActivity).viewModel
        newsViewModel.news.observe(viewLifecycleOwner, Observer { response ->
            when(response){
                is ResponseState.Error -> {
                    hideProgressBar()
                }
                is ResponseState.Loading ->{
                    showProgressBar()
                    Log.d("TAG " , response.message.toString() )
                }
                is ResponseState.Success -> {
                    hideProgressBar()
                    response.data?.let { newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles)
                    }
                }
            }
        })
    }
}