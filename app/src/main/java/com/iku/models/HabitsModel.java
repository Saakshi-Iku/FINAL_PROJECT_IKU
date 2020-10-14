package com.iku.models;

import com.google.firebase.database.PropertyName;

public class HabitsModel {

    @PropertyName("habit_name")
    private String habit_name;
    @PropertyName("habit_illustration")
    private String habit_illustration;
    @PropertyName("type")
    private String type;
    @PropertyName("frequency")
    private String frequency;
    @PropertyName("week")
    private int week;
    @PropertyName("day")
    private String day;
    @PropertyName("timeslot")
    private String timeslot;
    @PropertyName("support_count")
    private int support_count;
    @PropertyName("streak")
    private long streak;
    @PropertyName("timestamp")
    private long timestamp;

    public HabitsModel() {
    }

    public HabitsModel(String habit_name, String habit_ilustration, String type, String frequency, int week, String day, String timeslot, int support_count, long streak, long timestamp) {
        this.habit_name = habit_name;
        this.habit_illustration = habit_ilustration;
        this.type = type;
        this.frequency = frequency;
        this.week = week;
        this.day = day;
        this.timeslot = timeslot;
        this.support_count = support_count;
        this.streak = streak;
        this.timestamp = timestamp;
    }

    public String getHabit() {
        return habit_name;
    }

    public String getIllustration() {
        return habit_illustration;
    }

    public String getType() {
        return type;
    }

    public String getFrequency() {
        return frequency;
    }

    public int getWeek() {
        return week;
    }

    public String getDay() {
        return day;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public int getSupport_count() {
        return support_count;
    }

    public long getStreak() {
        return streak;
    }

    public long getTimestamp() {
        return timestamp;
    }

}
