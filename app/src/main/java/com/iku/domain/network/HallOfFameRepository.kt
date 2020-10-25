package com.iku.domain.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.iku.models.WinnersModel

class HallOfFameRepository {
    fun getWinnersData(): LiveData<MutableList<WinnersModel>> {
        val mutableData = MutableLiveData<MutableList<WinnersModel>>()
        FirebaseFirestore.getInstance().collection("groups").document("aD4dmKaHhQmEJ0yqeyLQ").collection("hall_of_fame").orderBy("timestamp").get().addOnSuccessListener { result ->
            val listData = mutableListOf<WinnersModel>()
            for (document in result) {
                val timestamp = document.get("timestamp")
                val messageCount = document.get("messageCount")
                val heartsCount = document.get("heartsCount")
                val first = document.get("first") as HashMap<*, *>
                val second = document.get("second") as HashMap<*, *>
                val third = document.get("third") as HashMap<*, *>
                val winnerInfo = WinnersModel(timestamp as Long, first, second, third, messageCount as Long, heartsCount as Long)
                listData.add(winnerInfo)
            }
            mutableData.value = listData
        }
        return mutableData
    }
}