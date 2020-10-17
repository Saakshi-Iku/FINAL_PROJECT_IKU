package com.iku.domain.network

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.iku.models.UserModel

class LeaderBoardRepository {
    fun getUserData():LiveData<MutableList<UserModel>>{
        val mutableData = MutableLiveData<MutableList<UserModel>>()
        FirebaseFirestore.getInstance().collection("users").orderBy("points", Query.Direction.DESCENDING).get().addOnSuccessListener { result->
            val listData = mutableListOf<UserModel>()
            for (document in result){
                val firstName=document.getString("firstName")
                val lastName=document.getString("lastName")
                val uid = document.getString("uid")
                val points = document.getLong("points")
                val userInfo = UserModel(firstName!!,lastName!!,uid!!,points!!)
                listData.add(userInfo)
            }
            mutableData.value=listData
        }
        return mutableData
    }
}