package com.iku

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.chip.Chip
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.iku.adapter.HabitsAdapter
import com.iku.app.AppConfig
import com.iku.databinding.ActivityHabitsBinding
import com.iku.models.HabitsModel
import kotlinx.android.synthetic.main.activity_habits.*


class HabitsActivity : AppCompatActivity() {
    private lateinit var mAuth: FirebaseAuth
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var db: FirebaseFirestore
    private lateinit var habitsBinding: ActivityHabitsBinding
    private lateinit var adapter: HabitsAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        habitsBinding = ActivityHabitsBinding.inflate(layoutInflater)
        setContentView(habitsBinding.root)
        linearLayoutManager = LinearLayoutManager(this)
        listHabitsPredefined.layoutManager = linearLayoutManager
        listHabitsPredefined.setHasFixedSize(true);

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        firebaseAnalytics = Firebase.analytics
        val user = mAuth.currentUser
        initButtons(user)

        val query = db.collection("habits").limit(2)
        Log.i("habits", "onCreate: $query")
        val options = FirestoreRecyclerOptions.Builder<HabitsModel>().setQuery(query, HabitsModel::class.java).setLifecycleOwner(this).build()
        adapter = HabitsAdapter(options)
        listHabitsPredefined.adapter = adapter

    }

    private fun initButtons(user: FirebaseUser?) {
        var habit: String
        var frequency: String
        var week: String
        var day: String
        var time: String
        habit = ""
        frequency = ""
        week = ""
        day = ""
        time = ""

        habitsBinding.backButton.setOnClickListener { onBackPressed() }

        habitsBinding.addHabitButton.setOnClickListener {
            Log.i("habits", "initButtons: add Habit")
            habitsBinding.editHabitName.visibility = View.VISIBLE
            habitsBinding.charCounter.visibility = View.VISIBLE
            habitsBinding.frequency.visibility = View.VISIBLE
        }

        habitsBinding.editHabitName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                habitsBinding.charCounter.text = (140 - s.toString().length).toString()
            }
        })

        habitsBinding.frequencyChipGroup.children.forEach {
            (it as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                Log.i("habits", "handleSelection: $isChecked ")
                if (habitsBinding.frequencyChipGroup.checkedChipIds.size > 0 && habitsBinding.editHabitName.text.isNotEmpty()) {
                    habitsBinding.frequencyChipGroup.checkedChipIds.forEach {
                        val chip = findViewById<Chip>(it)
                        frequency = "${chip.text}"
                        if (chip.text == "Once in 3 days" || chip.text == "Daily") {
                            habitsBinding.weekSelection.visibility = View.GONE
                            habitsBinding.daySelection.visibility = View.GONE
                            habitsBinding.timeSlot.visibility = View.VISIBLE
                        } else if (chip.text == "Once in a week") {
                            habitsBinding.weekSelection.visibility = View.GONE
                            habitsBinding.daySelection.visibility = View.VISIBLE
                            habitsBinding.timeSlot.visibility = View.GONE
                        } else {
                            habitsBinding.weekSelection.visibility = View.VISIBLE
                            habitsBinding.daySelection.visibility = View.GONE
                            habitsBinding.timeSlot.visibility = View.GONE
                        }
                    }
                } else {
                    habitsBinding.weekSelection.visibility = View.GONE
                    habitsBinding.daySelection.visibility = View.GONE
                    habitsBinding.timeSlot.visibility = View.GONE
                }

            }
        }

        habitsBinding.weekSelectionChipGroup.children.forEach {
            (it as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                Log.i("habits", "handleSelection: $isChecked ")
                if (habitsBinding.frequencyChipGroup.checkedChipIds.size > 0 &&
                        habitsBinding.weekSelectionChipGroup.checkedChipIds.size > 0)
                    habitsBinding.daySelection.visibility = View.VISIBLE
                else {
                    habitsBinding.daySelection.visibility = View.GONE
                    habitsBinding.timeSlot.visibility = View.GONE
                }
                habitsBinding.weekSelectionChipGroup.checkedChipIds.forEach {
                    val chip = findViewById<Chip>(it)
                    week = "${chip.text}"
                }
            }
        }

        habitsBinding.daySelectionChipGroup.children.forEach {
            (it as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                Log.i("habits", "handleSelection: $isChecked ")
                if (habitsBinding.frequencyChipGroup.checkedChipIds.size > 0 &&
                        habitsBinding.weekSelectionChipGroup.checkedChipIds.size > 0 &&
                        habitsBinding.daySelectionChipGroup.checkedChipIds.size > 0)
                    habitsBinding.timeSlot.visibility = View.VISIBLE
                else
                    habitsBinding.timeSlot.visibility = View.GONE
                habitsBinding.daySelectionChipGroup.checkedChipIds.forEach {
                    val chip = findViewById<Chip>(it)
                    day = "${chip.text}"
                }
            }
        }

        habitsBinding.timeSlotChipGroup.children.forEach {
            (it as Chip).setOnCheckedChangeListener { buttonView, isChecked ->
                Log.i("habits", "save button: ${habitsBinding.frequencyChipGroup.checkedChipIds.size} " +
                        " ${habitsBinding.weekSelectionChipGroup.checkedChipIds.size} " +
                        " ${habitsBinding.daySelectionChipGroup.checkedChipIds.size} " +
                        "${habitsBinding.timeSlotChipGroup.checkedChipIds.size}")
                if (habitsBinding.editHabitName.text.isNotEmpty())
                    habitsBinding.saveHabit.visibility = View.VISIBLE
                else
                    habitsBinding.saveHabit.visibility = View.GONE
                habitsBinding.timeSlotChipGroup.checkedChipIds.forEach {
                    val chip = findViewById<Chip>(it)
                    time = "${chip.text}"
                }
            }
        }

        habitsBinding.saveHabit.setOnClickListener {
            val data = mapOf(
                    "habit" to "${habitsBinding.editHabitName.text}",
                    "frequency" to frequency,
                    "week" to week,
                    "day" to day,
                    "time" to time,
                    "createdOn" to FieldValue.serverTimestamp()
            )
            if (user != null) {
                db.collection(AppConfig.USERS_COLLECTION).document(user.uid).collection("habits").document()
                        .set(data).addOnSuccessListener {
                            Log.i("habits", "saved: Remind on $frequency $week $day $time")
                        }.addOnFailureListener {
                        }
            }

        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

}