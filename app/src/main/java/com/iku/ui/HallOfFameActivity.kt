package com.iku.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.SnapHelper
import com.iku.databinding.ActivityHallOfFameBinding
import com.iku.viewmodel.HallOfFameViewModel
import kotlinx.android.synthetic.main.activity_hall_of_fame.*


class HallOfFameActivity : AppCompatActivity() {
    private lateinit var adapter: HallOfFameAdapter
    private val viewModel by lazy { ViewModelProvider(this).get(HallOfFameViewModel::class.java) }
    private lateinit var binding: ActivityHallOfFameBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHallOfFameBinding.inflate(layoutInflater)
        setContentView(binding.root)
        back_button.setOnClickListener { onBackPressed() }
        adapter = HallOfFameAdapter(this)
        hofRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val snapHelper: SnapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(hofRecyclerView)
        hofRecyclerView.adapter = adapter
        observeData()
    }
    private fun observeData() {
        viewModel.fetchWinnerData().observe(this, {
            adapter.setListData(it)
            adapter.notifyDataSetChanged()
        })
    }
}