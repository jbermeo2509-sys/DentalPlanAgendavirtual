package com.example.dentalprueba.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.dentalprueba.model.Patient

@Dao
interface PatientDao {
    @Query("SELECT * FROM patient_table ORDER BY startDate ASC")
    fun getAllPatients(): LiveData<List<Patient>>

    @Query("SELECT * FROM patient_table WHERE isActive = 1 ORDER BY startDate ASC")
    fun getActivePatients(): LiveData<List<Patient>>

    @Query("SELECT * FROM patient_table WHERE isActive = 0 ORDER BY firstName ASC")
    fun getInactivePatients(): LiveData<List<Patient>>

    @Query("SELECT * FROM patient_table")
    suspend fun getAllPatientsSync(): List<Patient>
    
    @Query("SELECT * FROM patient_table WHERE firstName LIKE '%' || :query || '%' OR lastName LIKE '%' || :query || '%' OR idCard LIKE '%' || :query || '%'")
    fun searchPatients(query: String): LiveData<List<Patient>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Update
    suspend fun update(patient: Patient)

    @Delete
    suspend fun delete(patient: Patient)
}
