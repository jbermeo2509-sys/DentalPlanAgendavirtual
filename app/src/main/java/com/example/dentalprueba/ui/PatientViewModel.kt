package com.example.dentalprueba.ui

import android.app.Application
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.dentalprueba.data.PatientDatabase
import com.example.dentalprueba.data.PatientRepository
import com.example.dentalprueba.model.Patient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PatientViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PatientRepository
    val patients: LiveData<List<Patient>>
    val activePatients: LiveData<List<Patient>>
    val inactivePatients: LiveData<List<Patient>>
    
    private val _selectedPatient = MutableLiveData<Patient>()
    val selectedPatient: LiveData<Patient> = _selectedPatient

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val storage = Firebase.storage
    
    // Tag for logging
    private val TAG = "PatientViewModel"
    
    private var isSyncing = false

    init {
        val patientDao = PatientDatabase.getDatabase(application).patientDao()
        repository = PatientRepository(patientDao)
        patients = repository.allPatients
        activePatients = repository.activePatients
        inactivePatients = repository.inactivePatients
    }

    fun searchPatients(query: String): LiveData<List<Patient>> {
        return repository.searchPatients(query)
    }

    fun selectPatient(patient: Patient) {
        _selectedPatient.value = patient
    }

    fun syncData() {
        if (isSyncing) return
        val userId = auth.currentUser?.uid ?: return
        isSyncing = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = db.collection("users").document(userId)
                    .collection("patients").get().await()
                if (snapshot.isEmpty) {
                    Log.d(TAG, "No patients found in cloud for user $userId")
                }
                for (document in snapshot.documents) {
                    val patient = document.toObject(Patient::class.java)
                    if (patient != null) {
                        repository.insert(patient)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error syncing data", e)
                e.printStackTrace()
            } finally {
                isSyncing = false
            }
        }
    }

    fun addPatient(patient: Patient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insert(patient)
            uploadToFirebase(patient)
        }
    }

    fun uploadPhoto(patient: Patient, uri: Uri, photoType: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileName = if (photoType == "photo") "photo_${patient.id}.jpg" else "xray_${patient.id}.jpg"
                val photoRef = storage.reference.child("images/${patient.id}/$fileName")
                val uploadTask = photoRef.putFile(uri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                if (photoType == "photo") {
                    patient.photoUrl = downloadUrl
                } else {
                    patient.xrayUrl = downloadUrl
                }
                patient.updateTimestamp()
                repository.update(patient)
                uploadToFirebase(patient)
                _selectedPatient.postValue(patient) // Update selected patient to refresh UI
                
            } catch (e: Exception) {
                 Log.e(TAG, "Error uploading photo", e)
            }
        }
    }

    fun removePatient(patient: Patient) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.delete(patient)
            deleteFromFirebase(patient)
        }
    }
    
    fun togglePatientStatus(patient: Patient) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedPatient = patient.copy(isActive = !patient.isActive)
            updatedPatient.updateTimestamp()
            repository.update(updatedPatient)
            uploadToFirebase(updatedPatient)
            
            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.postValue(updatedPatient)
            }
        }
    }

    fun reactivatePatient(patient: Patient) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedPatient = patient.copy(isActive = true)
            updatedPatient.updateTimestamp()
            repository.update(updatedPatient)
            uploadToFirebase(updatedPatient)

            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.postValue(updatedPatient)
            }
        }
    }
    
    fun addAppointment(patient: Patient, date: Long, procedure: String, payment: Double, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            patient.addAppointment(date, procedure, payment, notes)
            repository.update(patient)
            uploadToFirebase(patient)
            
             if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.postValue(patient)
            }
        }
    }
    
    fun addPayment(patient: Patient, amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            patient.addAppointment(System.currentTimeMillis(), "Abono", amount, "")
            repository.update(patient)
            uploadToFirebase(patient)
            
            if (_selectedPatient.value?.id == patient.id) {
                _selectedPatient.postValue(patient)
            }
        }
    }
    
    private fun uploadToFirebase(patient: Patient) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
             Log.w(TAG, "Cannot upload patient: No signed in user")
             viewModelScope.launch(Dispatchers.Main) {
                 Toast.makeText(getApplication(), "No se pudo sincronizar: Usuario no identificado", Toast.LENGTH_SHORT).show()
             }
             return
        }
        
        db.collection("users").document(userId)
            .collection("patients").document(patient.id)
            .set(patient)
            .addOnSuccessListener {
                Log.d(TAG, "Patient uploaded successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error uploading patient", e)
                Toast.makeText(getApplication(), "Error al guardar en nube: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun deleteFromFirebase(patient: Patient) {
        val userId = auth.currentUser?.uid ?: return
        db.collection("users").document(userId)
            .collection("patients").document(patient.id)
            .delete()
            .addOnFailureListener { e ->
                Log.e(TAG, "Error deleting patient from cloud", e)
            }
    }
}
