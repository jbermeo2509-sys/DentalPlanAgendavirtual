package com.example.dentalprueba.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dentalprueba.R
import com.example.dentalprueba.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = PatientAdapter(
            onPatientClick = { patient ->
                // On click, navigate to patient detail
                viewModel.selectPatient(patient)
                findNavController().navigate(R.id.action_nav_home_to_nav_patient_detail)
            },
            onStatusClick = { patient ->
                // On click "Terminar Tratamiento"
                viewModel.togglePatientStatus(patient)
            }
        )

        binding.recyclerViewPatients.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewPatients.adapter = adapter

        // Observer only ACTIVE patients
        viewModel.activePatients.observe(viewLifecycleOwner) { patients ->
            adapter.submitList(patients)
            binding.textEmpty.visibility = if (patients.isEmpty()) View.VISIBLE else View.GONE
        }
        
        // Trigger sync
        viewModel.syncData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
