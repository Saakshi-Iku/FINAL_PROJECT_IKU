package com.iku.domain.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.iku.app.AppConfig
import com.iku.models.ChatModel

class UserPostsRepository {
    fun getPostsData(): LiveData<MutableList<ChatModel>> {
        val mutableData = MutableLiveData<MutableList<ChatModel>>()
        FirebaseFirestore.getInstance().collection(AppConfig.GROUPS_MESSAGES_COLLECTION).document("aD4dmKaHhQmEJ0yqeyLQ").collection(AppConfig.MESSAGES_SUB_COLLECTION).whereEqualTo("type", "image").get().addOnSuccessListener { result ->
            val listData = mutableListOf<ChatModel>()
            for (document in result) {
                val message = document.get("message")
                val timestamp = document.get("timestamp")
                val postInfo = ChatModel(message as String, timestamp as Long)
                listData.add(postInfo)
            }
            mutableData.value = listData
        }
        return mutableData
    }
}