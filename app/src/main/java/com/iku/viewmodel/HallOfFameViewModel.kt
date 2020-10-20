package com.iku.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.iku.domain.network.HallOfFameRepository
import com.iku.models.WinnersModel

class HallOfFameViewModel:ViewModel() {
    private val repo = HallOfFameRepository()
    fun fetchWinnerData(): LiveData<MutableList<WinnersModel>> {
        val mutableData = MutableLiveData<MutableList<WinnersModel>>()
        repo.getWinnersData().observeForever { userList ->
            mutableData.value = userList
        }
        return mutableData
    }
}