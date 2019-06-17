package com.deepster.mafiaparty.model.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Role : Parcelable {
    UNSELECTED, MAFIA, CITIZEN, COP, DOCTOR
}


@Parcelize
data class User(val email: String = "", val username: String = "", val uid: String = "  ") : Parcelable