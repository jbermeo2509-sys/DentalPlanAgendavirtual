package com.example.dentalprueba.model

data class Appointment(
    var id: String = java.util.UUID.randomUUID().toString(),
    var date: Long = 0L,
    var procedure: String = "",
    var paymentAmount: Double = 0.0,
    var notes: String = ""
) {
    // No-argument constructor required by Firestore
    constructor() : this(
        id = java.util.UUID.randomUUID().toString(),
        date = 0L,
        procedure = "",
        paymentAmount = 0.0,
        notes = ""
    )
}
