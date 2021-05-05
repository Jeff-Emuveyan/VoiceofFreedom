package com.bellogate.voiceoffreedom.model

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey


@Keep
@Entity(tableName = "user")
class User (@PrimaryKey val id: Int,
                 val name: String,
                 val email: String,
                 val timeCreated: Long,
                 var isAdmin: Boolean = false){

    constructor(): this(1, "", "", 0)
}