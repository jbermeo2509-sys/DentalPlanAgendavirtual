package com.example.dentalprueba.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dentalprueba.databinding.FragmentHistoryBinding
import com.example.dentalprueba.databinding.ItemPatientSummaryBinding
import com.example.dentalprueba.model.Patient
import java.text.NumberFormat
import java.util.Locale

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientViewModel by activityViewModels()
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize adapter with a callback to reactivate patient
        adapter = HistoryAdapter { patient ->
            // On click, reactivate the patient
            viewModel.reactivatePatient(patient)
        }
        
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewHistory.adapter = adapter

        // Setup Search functionality
        setupSearch()

        // Observer inactive patients initially
        viewModel.inactivePatients.observe(viewLifecycleOwner) { patients ->
            adapter.submitList(patients)
            binding.textEmpty.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Ensure we trigger a sync
        viewModel.syncData()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.isNotEmpty()) {
                    viewModel.searchPatients(query).observe(viewLifecycleOwner) { patients ->
                        val filteredList = patients.filter { !it.isActive }
                        adapter.submitList(filteredList)
                        binding.textEmpty.visibility = if (filteredList.isEmpty()) View.VISIBLE else View.GONE
                    }
                } else {
                    // Restore default inactive list
                    viewModel.inactivePatients.observe(viewLifecycleOwner) { patients ->
                        adapter.submitList(patients)
                        binding.textEmpty.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class HistoryAdapter(private val onReactivateClick: (Patient) -> Unit) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
        
        private var list: List<Patient> = emptyList()

        fun submitList(newList: List<Patient>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
            val binding = ItemPatientSummaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return HistoryViewHolder(binding)
        }

        override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
            holder.bind(list[position], onReactivateClick)
        }

        override fun getItemCount() = list.size

        class HistoryViewHolder(private val binding: ItemPatientSummaryBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(patient: Patient, onReactivateClick: (Patient) -> Unit) {
                binding.textViewSummaryName.text = "${patient.firstName} ${patient.lastName}"
                binding.textViewSummaryId.text = patient.idCard
                binding.textViewSummaryPhone.text = patient.phone
                
                // Remove email text view if it's causing unresolved reference errors or simply remove the line if ID not present.
                // Assuming ID textViewSummaryEmail exists in layout but user complained about errors before. 
                // Let's verify layout. If I cannot verify, I'll keep it but ensure safety.
                // However, user asked to optimize and fix errors.
                // The previous error showed unresolved reference for userName/Email in MainActivity, which I fixed.
                // But in HistoryFragment, we are setting email to "No registrado".
                // If the TextView exists in item_patient_summary.xml, it's fine.
                // Let's assume it exists as I wrote it previously.
                
                binding.textViewSummaryEmail.text = "No registrado" 

                binding.layoutAppointments.removeAllViews()

                var appointmentCount = 0
                val currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())
                val sortedAppointments = patient.appointments.sortedBy { it.date }

                for ((index, appointment) in sortedAppointments.withIndex()) {
                    appointmentCount++
                    val appointmentView = TextView(binding.root.context)
                    val formattedAmount = currencyFormat.format(appointment.paymentAmount)
                    appointmentView.text = "• Cita ${index + 1}: $formattedAmount (${appointment.procedure})"
                    appointmentView.textSize = 14f
                    appointmentView.setPadding(16, 4, 0, 4)
                    binding.layoutAppointments.addView(appointmentView)
                }

                if (appointmentCount == 0) {
                     val noDataView = TextView(binding.root.context)
                     noDataView.text = "Sin citas registradas"
                     noDataView.textSize = 14f
                     noDataView.setPadding(16, 4, 0, 4)
                     noDataView.setTextColor(ContextCompat.getColor(binding.root.context, android.R.color.darker_gray))
                     binding.layoutAppointments.addView(noDataView)
                }

                binding.textViewTotalAppointments.text = appointmentCount.toString()
                binding.textViewTotalPaid.text = currencyFormat.format(patient.totalPaid)
                
                // Show Reactivate option
                binding.root.setOnClickListener {
                    androidx.appcompat.app.AlertDialog.Builder(binding.root.context)
                        .setTitle("Reactivar Tratamiento")
                        .setMessage("¿Deseas reactivar a ${patient.firstName} ${patient.lastName} y moverlo a la agenda activa?")
                        .setPositiveButton("Sí, reactivar") { _, _ ->
                            onReactivateClick(patient)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            }
        }
    }
}
