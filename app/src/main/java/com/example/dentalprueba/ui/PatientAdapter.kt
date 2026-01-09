package com.example.dentalprueba.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.dentalprueba.databinding.ItemPatientBinding
import com.example.dentalprueba.model.Patient
import java.text.SimpleDateFormat
import java.util.Locale

class PatientAdapter(
    private val onPatientClick: (Patient) -> Unit,
    private val onStatusClick: (Patient) -> Unit
) : ListAdapter<Patient, PatientAdapter.PatientViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientViewHolder {
        val binding = ItemPatientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PatientViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PatientViewHolder, position: Int) {
        val patient = getItem(position)
        holder.bind(patient, onPatientClick, onStatusClick)
    }

    class PatientViewHolder(private val binding: ItemPatientBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(patient: Patient, onPatientClick: (Patient) -> Unit, onStatusClick: (Patient) -> Unit) {
            binding.textViewName.text = "${patient.firstName} ${patient.lastName}"

            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            if (patient.isActive) {
                 val nextDate = patient.getNextAppointmentDate()
                 binding.textViewNextDate.text = "Próxima cita: ${dateFormat.format(nextDate)}"
            } else {
                binding.textViewNextDate.text = "Tratamiento Cancelado"
            }

            binding.root.setOnClickListener {
                onPatientClick(patient)
            }

            binding.buttonStatus.setOnClickListener {
                onStatusClick(patient)
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Patient>() {
            override fun areItemsTheSame(oldItem: Patient, newItem: Patient): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Patient, newItem: Patient): Boolean {
                return oldItem == newItem
            }
        }
    }
}
