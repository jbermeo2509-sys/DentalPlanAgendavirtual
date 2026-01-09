package com.example.dentalprueba.converters

import androidx.room.TypeConverter
import com.example.dentalprueba.model.Appointment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromAppointmentList(value: MutableList<Appointment>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toAppointmentList(value: String): MutableList<Appointment> {
        if (value.isEmpty()) return mutableListOf()
        val type = object : TypeToken<MutableList<Appointment>>() {}.type
        return gson.fromJson(value, type)
    }
    
    // For the existing double list if we want to migrate
    @TypeConverter
    fun fromDoubleList(value: MutableList<Double>?): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toDoubleList(value: String): MutableList<Double> {
        if (value.isEmpty()) return mutableListOf()
        val type = object : TypeToken<MutableList<Double>>() {}.type
        return gson.fromJson(value, type)
    }
}
