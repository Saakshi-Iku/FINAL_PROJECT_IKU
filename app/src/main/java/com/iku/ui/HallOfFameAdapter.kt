package com.iku.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.iku.R
import com.iku.models.WinnersModel
import kotlinx.android.synthetic.main.hall_of_fame_data.view.*
import java.text.SimpleDateFormat
import java.util.*

class HallOfFameAdapter(val context: Context) : RecyclerView.Adapter<HallOfFameAdapter.WinnersViewHolder>() {
    private var dataList = mutableListOf<WinnersModel>()
    fun setListData(data: MutableList<WinnersModel>) {
        dataList = data
    }

    override fun onBindViewHolder(holder: WinnersViewHolder, position: Int) {
        val winner: WinnersModel = dataList[position]
        holder.bindView(winner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WinnersViewHolder {
        return WinnersViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.hall_of_fame_data, parent, false))
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class WinnersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindView(winner: WinnersModel) {
            itemView.month.text = SimpleDateFormat("MMMM yyyy", Locale.US).format(winner.timestamp)
            itemView.winnerName.text = winner.first["name"] as CharSequence
            itemView.winnerHearts.text = winner.first["points"].toString()
            itemView.runnerUp1Name.text = winner.second["name"] as CharSequence
            itemView.runnerUp1Hearts.text = winner.second["points"].toString()
            itemView.runnerUp2Name.text = winner.third["name"] as CharSequence
            itemView.runnerUp2Hearts.text = winner.third["points"].toString()
            itemView.winnerImage.load(winner.first["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            itemView.runnerUp1Image.load(winner.second["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            itemView.runnerUp2Image.load(winner.third["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            itemView.messagesCount.text = winner.messageCount.toString()
            itemView.heartsCount.text = winner.heartsCount.toString()
        }
    }
}