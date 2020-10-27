package com.iku.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.amulyakhare.textdrawable.TextDrawable
import com.iku.R
import com.iku.models.WinnersModel
import kotlinx.android.synthetic.main.hall_of_fame_data.view.*
import java.text.SimpleDateFormat
import java.util.*

class HallOfFameAdapter(val context: Context, val cellClickListener: CellClickListener) : RecyclerView.Adapter<HallOfFameAdapter.WinnersViewHolder>() {
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

    interface CellClickListener {
        fun onCellClickListener(name: String, uid: String)
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
            if (winner.first["imageUrl"]!=null)
                itemView.winnerImage.load(winner.first["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            else{
                val firstLetter = winner.first["name"].toString()[0].toString()
                val secondLetter = winner.first["name"].toString().substring(winner.first["name"].toString().indexOf(' ') + 1, winner.first["name"].toString().indexOf(' ') + 2).trim { it <= ' ' }
                val drawable = TextDrawable.builder()
                        .beginConfig()
                        .width(200)
                        .height(200)
                        .endConfig()
                        .buildRect(firstLetter + secondLetter, Color.DKGRAY)
                itemView.winnerImage.load(drawable){ transformations(CircleCropTransformation()) }
            }
            itemView.winnerImage.setOnClickListener { cellClickListener.onCellClickListener(winner.first["name"] as String, winner.first["uid"] as String) }
            if (winner.second["imageUrl"]!=null)
                itemView.runnerUp1Image.load(winner.second["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            else{
                val firstLetter = winner.second["name"].toString()[0].toString()
                val secondLetter = winner.second["name"].toString().substring(winner.second["name"].toString().indexOf(' ') + 1, winner.second["name"].toString().indexOf(' ') + 2).trim { it <= ' ' }
                val drawable = TextDrawable.builder()
                        .beginConfig()
                        .width(200)
                        .height(200)
                        .endConfig()
                        .buildRect(firstLetter + secondLetter, Color.DKGRAY)
                itemView.runnerUp1Image.load(drawable){ transformations(CircleCropTransformation()) }
            }
            itemView.runnerUp1Image.setOnClickListener { cellClickListener.onCellClickListener(winner.second["name"] as String, winner.second["uid"] as String) }
            if (winner.third["imageUrl"]!=null)
                itemView.runnerUp2Image.load(winner.third["imageUrl"].toString()) { transformations(CircleCropTransformation()) }
            else{
                val firstLetter = winner.third["name"].toString()[0].toString()
                val secondLetter = winner.third["name"].toString().substring(winner.third["name"].toString().indexOf(' ') + 1, winner.third["name"].toString().indexOf(' ') + 2).trim { it <= ' ' }
                val drawable = TextDrawable.builder()
                        .beginConfig()
                        .width(200)
                        .height(200)
                        .endConfig()
                        .buildRect(firstLetter + secondLetter, Color.DKGRAY)
                itemView.runnerUp2Image.load(drawable){ transformations(CircleCropTransformation()) }
            }
            itemView.runnerUp2Image.setOnClickListener { cellClickListener.onCellClickListener(winner.third["name"] as String, winner.third["uid"] as String) }
            itemView.messagesCount.text = winner.messageCount.toString()
            itemView.heartsCount.text = winner.heartsCount.toString()
        }
    }
}