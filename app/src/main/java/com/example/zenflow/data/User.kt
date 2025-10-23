package com.example.zenflow.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User(
    val name: String = "User Name",
    val email: String = "user@example.com",
    val profilePictureUri: String? = null
) : Parcelable