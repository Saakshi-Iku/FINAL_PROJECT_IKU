package com.iku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iku.domain.network.UserPostsRepository
import com.iku.models.ChatModel

class UserPostsViewModel : ViewModel() {
    private val repo = UserPostsRepository()
    fun fetchPostsData(): LiveData<MutableList<ChatModel>> {
        val mutableData = MutableLiveData<MutableList<ChatModel>>()
        repo.getPostsData().observeForever { userList ->
            mutableData.value = userList
        }
        return mutableData
    }
}