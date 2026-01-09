package com.example.dentalprueba.data

import androidx.lifecycle.LiveData
import com.example.dentalprueba.model.Patient

class PatientRepository(private val patientDao: PatientDao) {

    val allPatients: LiveData<List<Patient>> = patientDao.getAllPatients()
    val activePatients: LiveData<List<Patient>> = patientDao.getActivePatients()
    val inactivePatients: LiveData<List<Patient>> = patientDao.getInactivePatients()

    fun searchPatients(query: String): LiveData<List<Patient>> {
        return patientDao.searchPatients(query)
    }

    suspend fun insert(patient: Patient) {
        patientDao.insert(patient)
    }

    suspend fun update(patient: Patient) {
        patientDao.update(patient)
    }

    suspend fun delete(patient: Patient) {
        patientDao.delete(patient)
    }
}
