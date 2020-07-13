package com.marxist.android.ui.fragments.books

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.marxist.android.R
import com.marxist.android.database.AppDatabase
import com.marxist.android.database.entities.LocalBooks
import com.marxist.android.utils.DeviceUtils
import com.marxist.android.utils.toPixel
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions

class BookListAdapter(
    private val mContext: Context,
    private var booksList: MutableList<LocalBooks>,
    private val appDatabase: AppDatabase
) : RecyclerView.Adapter<BookViewHolder>() {
    private var previousClickedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val layoutId = R.layout.book_list_item
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)

        val lp = view.layoutParams as GridLayoutManager.LayoutParams
        val spanCount = mContext.resources.getInteger(R.integer.books_span_count)
        lp.height = (parent.measuredHeight / spanCount) - 20.toPixel(mContext)
        view.layoutParams = lp

        val targetPath = DeviceUtils.getRootDirPath(mContext).plus("/books")
        return BookViewHolder(mContext, view, targetPath)
    }

    override fun getItemCount(): Int {
        return if (booksList.isNullOrEmpty()) 0 else booksList.size
    }

    private fun getItem(position: Int): LocalBooks {
        return booksList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bindData(getItem(position), holder.adapterPosition, appDatabase)
    }

    private fun downloadClicked() {
        Permissions.check(
            mContext,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            null,
            object : PermissionHandler() {
                override fun onGranted() {

                }
            })
    }

    fun setItems(it: MutableList<LocalBooks>) {
        booksList = it
        notifyDataSetChanged()
    }

    fun updateDownloadId(itemPosition: Int) {
        previousClickedPosition = -1
        notifyItemChanged(itemPosition)
    }
}