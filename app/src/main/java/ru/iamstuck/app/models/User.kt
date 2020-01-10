package ru.iamstuck.app.models

data class User(val name: String = "", val city: String = "", val auto_vendor: String = "",
                val auto_model: String = "",val auto_number: String = "", val email: String = "", val phone: Long = 0L)

