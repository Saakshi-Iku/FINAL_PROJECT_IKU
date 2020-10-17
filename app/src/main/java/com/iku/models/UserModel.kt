package com.iku.models

import com.google.firebase.firestore.PropertyName

data class UserModel(
        @PropertyName("firstName")
        var firstName: String = "",

        @PropertyName("lastName")
        var lastName: String = "",

        @PropertyName("uid")
        var uid: String = "",

        @PropertyName("points")
        var points: Long = 0
)