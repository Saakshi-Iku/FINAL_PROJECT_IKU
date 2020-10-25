package com.iku.models

import com.google.firebase.firestore.PropertyName

data class WinnersModel(
        @PropertyName("timestamp")
        var timestamp: Long = 0,
        @PropertyName("first")
        var first: HashMap<*, *>,
        @PropertyName("second")
        var second: HashMap<*, *>,
        @PropertyName("third")
        var third: HashMap<*, *>,
        @PropertyName("messageCount")
        var messageCount: Long = 0,
        @PropertyName("heartsCount")
        var heartsCount: Long = 0
)