package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.newsapp.databinding.FragmentArticleBinding
import com.example.newsapp.ui.MainActivity
import com.example.newsapp.viewModel.NewsViewModel
import com.google.android.material.snackbar.Snackbar


class ArticleFragment : Fragment() {
    private var _binding: FragmentArticleBinding? = null
    private val binding get() = _binding!!
    private lateinit var newsViewModel: NewsViewModel
    private val args:ArticleFragmentArgs by navArgs()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleBinding.inflate(inflater, container, false)
        newsViewModel = (activity as MainActivity).viewModel

        binding.articleWebView.apply {
            webViewClient = WebViewClient()
            loadUrl(args.article.url)
        }

        binding.saveFab.setOnClickListener{
            newsViewModel.saveNews(args.article)
            view?.let {
                    it -> Snackbar.make(it, "successfully saved",Snackbar.LENGTH_SHORT).show()
            }
        }


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}