package com.iku.models;

import com.google.firebase.database.PropertyName;

public class HabitsModel {

    @PropertyName("habit")
    private String habit;
    @PropertyName("illustration")
    private String illustration;
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

    public HabitsModel(String habit, String illustration, String type, String frequency, int week, String day, String timeslot, int support_count, long streak,  long timestamp) {
        this.habit = habit;
        this.illustration = illustration;
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
        return habit;
    }

    public String getIllustration() {
        return illustration;
    }

    public String getType() {
        return type;
    }

    public String getFrequency() { return frequency; }

    public int getWeek() { return week; }

    public String getDay() {
        return day;
    }

    public String getTimeslot() {
        return timeslot;
    }

    public int getSupport_count() { return support_count; }

    public long getStreak() {
        return streak;
    }

    public long getTimestamp() {
        return timestamp;
    }

}