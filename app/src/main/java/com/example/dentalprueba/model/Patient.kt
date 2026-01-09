package com.example.dentalprueba.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date

@Entity(tableName = "patient_table")
data class Patient(
    @PrimaryKey
    var id: String = java.util.UUID.randomUUID().toString(),
    var firstName: String = "",
    var lastName: String = "",
    var idCard: String = "",
    var phone: String = "",
    var procedure: String = "",
    var startDate: Long = 0L,
    var isActive: Boolean = true,
    var appointments: MutableList<Appointment> = mutableListOf(),
    var totalPaid: Double = 0.0,
    var lastUpdated: Long = System.currentTimeMillis(),
    var age: Int = 0,
    var photoUrl: String? = null,
    var xrayUrl: String? = null
) {
    // No-argument constructor required by Firestore
    constructor() : this(
        id = "",
        firstName = "",
        lastName = "",
        idCard = "",
        phone = "",
        procedure = "",
        startDate = 0L,
        isActive = true,
        appointments = mutableListOf(),
        totalPaid = 0.0,
        lastUpdated = System.currentTimeMillis(),
        age = 0,
        photoUrl = null,
        xrayUrl = null
    )

    fun getNextAppointmentDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val now = Calendar.getInstance()
        now.set(Calendar.HOUR_OF_DAY, 0)
        now.set(Calendar.MINUTE, 0)
        now.set(Calendar.SECOND, 0)
        now.set(Calendar.MILLISECOND, 0)
        
        while (calendar.before(now)) {
            calendar.add(Calendar.MONTH, 1)
        }
        
        return calendar.time
    }

    fun addAppointment(date: Long, procedure: String, payment: Double, notes: String) {
        val appointment = Appointment(
            date = date,
            procedure = procedure,
            paymentAmount = payment,
            notes = notes
        )
        appointments.add(appointment)
        totalPaid += payment
        // Actualizamos la fecha de modificación automáticamente
        lastUpdated = System.currentTimeMillis()
    }
    
    // Método auxiliar para actualizar timestamp manualmente si se editan otros datos
    fun updateTimestamp() {
        lastUpdated = System.currentTimeMillis()
    }
}
