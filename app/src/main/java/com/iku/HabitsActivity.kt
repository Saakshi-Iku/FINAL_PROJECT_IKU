package com.iku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.iku.databinding.ActivityHabitsBinding

class HabitsActivity : AppCompatActivity() {
    private lateinit var habitsBinding: ActivityHabitsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habitsBinding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(habitsBinding.root)
        initButtons()
    }

    private fun initButtons() {
        habitsBinding.backButton.setOnClickListener { onBackPressed() }
    }
}