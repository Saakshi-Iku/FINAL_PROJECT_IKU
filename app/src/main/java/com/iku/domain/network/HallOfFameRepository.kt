package com.iku.domain.network

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.iku.models.WinnersModel

class HallOfFameRepository {
    fun getWinnersData(): LiveData<MutableList<WinnersModel>> {
        val mutableData = MutableLiveData<MutableList<WinnersModel>>()
        FirebaseFirestore.getInstance().collection("groups").document("iku_earth").collection("hall_of_fame").orderBy("timestamp").get().addOnSuccessListener { result ->
            val listData = mutableListOf<WinnersModel>()
            for (document in result) {
                val timestamp = document.get("timestamp")
                val first = document.get("first") as HashMap<*, *>
                val second = document.get("second") as HashMap<*, *>
                val third = document.get("third") as HashMap<*, *>
                val winnerInfo = WinnersModel(timestamp as Long, first, second, third)
                listData.add(winnerInfo)
            }
            mutableData.value = listData
        }
        return mutableData
    }
}