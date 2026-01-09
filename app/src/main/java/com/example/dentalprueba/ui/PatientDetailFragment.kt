package com.example.dentalprueba.ui

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.dentalprueba.R
import com.example.dentalprueba.databinding.FragmentPatientDetailBinding
import com.example.dentalprueba.databinding.ItemAppointmentBinding
import com.example.dentalprueba.model.Appointment
import com.example.dentalprueba.model.Patient
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class PatientDetailFragment : Fragment() {

    private var _binding: FragmentPatientDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientViewModel by activityViewModels()

    private val getXrayPhoto = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            viewModel.selectedPatient.value?.let { patient ->
                viewModel.uploadPhoto(patient, it, "xray")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPatientDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AppointmentAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewHistory.adapter = adapter

        viewModel.selectedPatient.observe(viewLifecycleOwner) { patient ->
            if (patient == null) {
                findNavController().popBackStack()
                return@observe
            }
            bindPatientData(patient)
            adapter.submitList(patient.appointments.sortedByDescending { it.date })
        }

        binding.buttonAddAppointment.setOnClickListener {
            viewModel.selectedPatient.value?.let { patient ->
                showAddAppointmentDialog(patient)
            }
        }

        binding.buttonViewXray.setOnClickListener {
            viewModel.selectedPatient.value?.xrayUrl?.let {
                showImageDialog(it)
            }
        }
    }

    private fun bindPatientData(patient: Patient) {
        binding.imageViewPatientPhoto.load(patient.photoUrl) {
            crossfade(true)
            placeholder(R.mipmap.ic_launcher_round)
            error(R.mipmap.ic_launcher_round)
        }
        binding.textViewDetailName.text = "${patient.firstName} ${patient.lastName}"
        binding.textViewDetailAge.text = "Edad: ${patient.age}"
        binding.textViewDetailProcedure.text = "Tratamiento: ${patient.procedure}"
        
        val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
        binding.textViewDetailTotalPaid.text = "Total Abonado: ${format.format(patient.totalPaid)}"

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val nextDate = patient.getNextAppointmentDate()
        binding.textViewDetailNextDate.text = "Próxima Cita: ${dateFormat.format(nextDate)}"
    }

    private fun showImageDialog(imageUrl: String) {
        val builder = AlertDialog.Builder(requireContext())
        val imageView = ImageView(requireContext())
        imageView.load(imageUrl)
        builder.setView(imageView)
        builder.setPositiveButton("Cerrar") { dialog, _ -> dialog.dismiss() }
        builder.create().show()
    }

    private fun showAddAppointmentDialog(patient: Patient) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Nueva Cita / Pago")

        val layout = LinearLayout(requireContext())
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(50, 40, 50, 10)

        // Date Picker Button
        val buttonDate = Button(requireContext(), null, android.R.attr.borderlessButtonStyle)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val calendar = Calendar.getInstance()
        buttonDate.text = "Fecha: ${dateFormat.format(calendar.time)}"
        
        buttonDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    buttonDate.text = "Fecha: ${dateFormat.format(calendar.time)}"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
        layout.addView(buttonDate)

        // Procedure Spinner
        val spinnerProcedure = Spinner(requireContext())
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
            "Protesis flex",
            "Abono"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, procedures)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProcedure.adapter = adapter
        val currentProcedureIndex = procedures.indexOf(patient.procedure)
        if (currentProcedureIndex >= 0) {
            spinnerProcedure.setSelection(currentProcedureIndex)
        }
        layout.addView(spinnerProcedure)

        val inputPayment = EditText(requireContext())
        inputPayment.hint = "Monto pagado"
        inputPayment.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        layout.addView(inputPayment)
        
        val inputNotes = EditText(requireContext())
        inputNotes.hint = "Notas adicionales"
        inputNotes.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
        layout.addView(inputNotes)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val procedure = spinnerProcedure.selectedItem.toString()
            val paymentText = inputPayment.text.toString()
            val notes = inputNotes.text.toString()
            
            val payment = paymentText.toDoubleOrNull() ?: 0.0
            val date = calendar.timeInMillis
            
            viewModel.addAppointment(patient, date, procedure, payment, notes)
            Toast.makeText(context, "Cita registrada", Toast.LENGTH_SHORT).show()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class AppointmentAdapter : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {
        
        private var list: List<Appointment> = emptyList()

        fun submitList(newList: List<Appointment>) {
            list = newList
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
            val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return AppointmentViewHolder(binding)
        }

        override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
            holder.bind(list[position])
        }

        override fun getItemCount() = list.size

        class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(appointment: Appointment) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                binding.textViewDate.text = dateFormat.format(Date(appointment.date))
                binding.textViewProcedure.text = appointment.procedure
                
                val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
                binding.textViewPayment.text = "Pago: ${format.format(appointment.paymentAmount)}"
                
                if (appointment.notes.isNotBlank()) {
                    binding.textViewNotes.visibility = View.VISIBLE
                    binding.textViewNotes.text = appointment.notes
                } else {
                    binding.textViewNotes.visibility = View.GONE
                }
            }
        }
    }
}
