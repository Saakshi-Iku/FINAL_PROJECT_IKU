package com.iku.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.iku.R
import com.iku.models.UserModel
import kotlinx.android.synthetic.main.leaderboard_data.view.*
import kotlinx.android.synthetic.main.otheruser_leaderboard_data.view.*

class LeaderBoardAdapter(val context: Context) : RecyclerView.Adapter<LeaderBoardAdapter.BaseViewHolder>() {
    private val user = FirebaseAuth.getInstance().currentUser
    private var dataList = mutableListOf<UserModel>()

    fun setListData(data: MutableList<UserModel>) {
        dataList = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        val holder: BaseViewHolder?
        return if (viewType == R.layout.leaderboard_data) {
            holder = CurrentUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_data, parent, false))
            holder
        } else {
            holder = OtherUserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.otheruser_leaderboard_data, parent, false))
            holder
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class OtherUserViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindView(user: UserModel) {
            itemView.otherUserName.text = context.getString(R.string.user_full_name, "${user.firstName} ${user.lastName}")
            itemView.otherUserPoints.text = user.points.toString()
        }
    }

    inner class CurrentUserViewHolder(itemView: View) : BaseViewHolder(itemView) {
        override fun bindView(user: UserModel) {
            itemView.userName.text = context.getString(R.string.user_full_name, "${user.firstName} ${user.lastName}")
            itemView.userPoints.text = user.points.toString()
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bindView(dataList[position])
    }

    fun getItem(position: Int): UserModel? {
        return dataList[position]
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position)?.uid.equals(user?.uid))
            R.layout.leaderboard_data
        else
            R.layout.otheruser_leaderboard_data
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bindView(user: UserModel)
    }
}