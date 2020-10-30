package com.iku.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.iku.R
import com.iku.models.ChatModel
import kotlinx.android.synthetic.main.chat_right_image.view.*

class UserPostsAdapter(val context: Context) : RecyclerView.Adapter<UserPostsAdapter.CurrentUserViewHolder>() {
    private var dataList = mutableListOf<ChatModel>()
    fun setListData(data: MutableList<ChatModel>) {
        dataList = data
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class CurrentUserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(postData: ChatModel) {
            itemView.message.text = postData.message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrentUserViewHolder {
        return CurrentUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.chat_right_image, parent, false))
    }

    override fun onBindViewHolder(holder: CurrentUserViewHolder, position: Int) {
        val postData: ChatModel = dataList[position]
        holder.bindView(postData)
    }
}