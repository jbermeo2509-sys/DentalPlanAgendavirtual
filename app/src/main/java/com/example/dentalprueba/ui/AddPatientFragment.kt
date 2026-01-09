package com.example.dentalprueba.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.dentalprueba.databinding.FragmentAddPatientBinding
import com.example.dentalprueba.model.Patient
import java.util.Calendar

class AddPatientFragment : Fragment() {

    private var _binding: FragmentAddPatientBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientViewModel by activityViewModels()
    
    private var patientPhotoUri: Uri? = null
    private var xrayPhotoUri: Uri? = null

    private val getPatientPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            patientPhotoUri = it
            binding.imageViewPatientPhoto.setImageURI(it)
            binding.imageViewPatientPhoto.visibility = View.VISIBLE
        }
    }

    private val getXrayPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            xrayPhotoUri = it
            binding.imageViewXrayPhoto.setImageURI(it)
            binding.imageViewXrayPhoto.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val procedures = listOf(
            "Calces",
            "Limpieza profilaxis",
            "Limpieza profunda con ultrasonido",
            "Blanqueamiento dental",
            "Carillas dentales",
            "Diseño de sonrisa",
            "Ortodoncia",
            "Endodoncia",
            "Odontopediatria",
            "Protesis acrilica",
            "Protesis flex"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, procedures)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProcedure.adapter = adapter

        binding.buttonAddPatientPhoto.setOnClickListener {
            getPatientPhoto.launch("image/*")
        }

        binding.buttonAddXrayPhoto.setOnClickListener {
            getXrayPhoto.launch("image/*")
        }

        binding.buttonSave.setOnClickListener {
            savePatient()
        }
    }

    private fun savePatient() {
        val firstName = binding.editTextFirstName.text.toString()
        val lastName = binding.editTextLastName.text.toString()
        val ageText = binding.editTextAge.text.toString()
        val idCard = binding.editTextIdCard.text.toString()
        val phone = binding.editTextPhone.text.toString()
        val procedure = binding.spinnerProcedure.selectedItem.toString()
        val initialPaymentText = binding.editTextInitialPayment.text.toString()

        if (firstName.isBlank() || lastName.isBlank() || ageText.isBlank()) {
            Toast.makeText(context, "Nombre, Apellido y Edad son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val calendar = Calendar.getInstance()
        calendar.set(
            binding.datePickerStart.year,
            binding.datePickerStart.month,
            binding.datePickerStart.dayOfMonth
        )

        val patient = Patient(
            firstName = firstName,
            lastName = lastName,
            age = ageText.toInt(),
            idCard = idCard,
            phone = phone,
            procedure = procedure,
            startDate = calendar.timeInMillis
        )
        
        if (initialPaymentText.isNotBlank()) {
            val amount = initialPaymentText.toDoubleOrNull()
            if (amount != null && amount > 0) {
                 patient.addAppointment(calendar.timeInMillis, procedure, amount, "Pago inicial")
            }
        }

        viewModel.addPatient(patient)
        
        patientPhotoUri?.let {
            viewModel.uploadPhoto(patient, it, "photo")
        }
        xrayPhotoUri?.let {
            viewModel.uploadPhoto(patient, it, "xray")
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
