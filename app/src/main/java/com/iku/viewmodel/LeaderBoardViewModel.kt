package com.iku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iku.domain.network.LeaderBoardRepository
import com.iku.models.UserModel

class LeaderBoardViewModel : ViewModel() {
    private val repo = LeaderBoardRepository()
    fun fetchUserdata(): LiveData<MutableList<UserModel>> {
        val mutableData = MutableLiveData<MutableList<UserModel>>()
        repo.getUserData().observeForever { userList ->
            mutableData.value = userList
        }
        return mutableData
    }
}