package com.marxist.android.ui.fragments.feeds

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marxist.android.R
import com.marxist.android.database.entities.LocalFeeds
import com.marxist.android.ui.activities.DetailsActivity
import com.marxist.android.ui.base.ItemClickListener
import com.marxist.android.viewmodel.FeedsViewModel
import kotlinx.android.synthetic.main.fragments_list.*
import kotlinx.android.synthetic.main.fragments_list.view.*
import kotlinx.android.synthetic.main.layout_lottie_no_feed.*

class FeedsFragment : Fragment(), ItemClickListener {
    private lateinit var feedAdapter: FeedListAdapter
    private lateinit var mContext: Context
    private lateinit var feedsViewModel: FeedsViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
        feedAdapter = FeedListAdapter(mContext, mutableListOf(), this@FeedsFragment)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()
    }

    private fun initData() {
        feedsViewModel = ViewModelProviders.of(this).get(FeedsViewModel::class.java)
        feedsViewModel.getLiveFeeds().observe(this, Observer {
            if (it != null) {
                if (it.isNotEmpty()) {
                    rvListView.visibility = View.VISIBLE
                    emptyView.visibility = View.GONE
                    feedAdapter.addFeeds(it)
                } else {
                    rvListView.visibility = View.GONE
                    emptyView.visibility = View.VISIBLE
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragments_list, container, false)

        view.rvListView.setHasFixedSize(true)
        view.rvListView.layoutManager = LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        view.rvListView.adapter = feedAdapter

        return view
    }

    override fun feedItemClickListener(
        article: Any,
        adapterPosition: Int,
        view: View
    ) {
        if (article is LocalFeeds) {
            val intent = Intent(mContext, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.ARTICLE, article)
            startActivity(intent)
        }
    }
}