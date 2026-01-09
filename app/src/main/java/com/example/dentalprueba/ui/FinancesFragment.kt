package com.example.dentalprueba.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.dentalprueba.databinding.FragmentFinancesBinding
import com.example.dentalprueba.databinding.ItemFinanceSummaryBinding
import com.example.dentalprueba.model.Patient
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

class FinancesFragment : Fragment() {

    private var _binding: FragmentFinancesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PatientViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FinanceAdapter()
        binding.recyclerViewFinances.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewFinances.adapter = adapter

        viewModel.patients.observe(viewLifecycleOwner) { patients ->
            val financeItems = processPayments(patients)
            adapter.submitList(financeItems)
        }
    }

    private fun processPayments(patients: List<Patient>): List<FinanceItem> {
        val items = mutableListOf<FinanceItem>()
        if (patients.isEmpty()) return items

        // Group all payments by Year -> Month -> Day
        val paymentsByDay = patients.flatMap { it.appointments }
            .groupBy {
                val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
            }
            .flatMap { (yearMonth, monthAppointments) ->
                monthAppointments.groupBy {
                    val cal = Calendar.getInstance().apply { timeInMillis = it.date }
                    cal.get(Calendar.DAY_OF_MONTH)
                }.map { (day, dayAppointments) ->
                    Triple(yearMonth.first, yearMonth.second, day) to dayAppointments.sumOf { it.paymentAmount }
                }
            }
            .sortedWith(compareByDescending<Pair<Triple<Int, Int, Int>, Double>> { it.first.first } // Year
                .thenByDescending { it.first.second } // Month
                .thenByDescending { it.first.third }) // Day

        var currentYear = -1
        var currentMonth = -1

        for ((triple, dayTotal) in paymentsByDay) {
            val (year, month, day) = triple

            if (year != currentYear) {
                items.add(FinanceItem.YearHeader(year))
                currentYear = year
                currentMonth = -1 // Reset month when year changes
            }

            if (month != currentMonth) {
                items.add(FinanceItem.MonthHeader(month, year))
                currentMonth = month
            }

            items.add(FinanceItem.DayEntry(day, dayTotal))
        }

        return items
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

sealed class FinanceItem {
    data class YearHeader(val year: Int) : FinanceItem()
    data class MonthHeader(val month: Int, val year: Int) : FinanceItem()
    data class DayEntry(val day: Int, val total: Double) : FinanceItem()
}

class FinanceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val items = mutableListOf<FinanceItem>()

    fun submitList(newItems: List<FinanceItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is FinanceItem.YearHeader -> 0
            is FinanceItem.MonthHeader -> 1
            is FinanceItem.DayEntry -> 2
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemFinanceSummaryBinding.inflate(inflater, parent, false)
        return when (viewType) {
            0 -> YearViewHolder(binding)
            1 -> MonthViewHolder(binding)
            else -> DayViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is FinanceItem.YearHeader -> (holder as YearViewHolder).bind(item)
            is FinanceItem.MonthHeader -> (holder as MonthViewHolder).bind(item)
            is FinanceItem.DayEntry -> (holder as DayViewHolder).bind(item)
        }
    }

    override fun getItemCount() = items.size

    class YearViewHolder(private val binding: ItemFinanceSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FinanceItem.YearHeader) {
            binding.textViewYear.visibility = View.VISIBLE
            binding.textViewMonth.visibility = View.GONE
            binding.layoutDay.visibility = View.GONE
            binding.textViewYear.text = item.year.toString()
        }
    }

    class MonthViewHolder(private val binding: ItemFinanceSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FinanceItem.MonthHeader) {
            binding.textViewYear.visibility = View.GONE
            binding.textViewMonth.visibility = View.VISIBLE
            binding.layoutDay.visibility = View.GONE
            val cal = Calendar.getInstance().apply { set(item.year, item.month, 1) }
            val monthName = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
            binding.textViewMonth.text = monthName?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        }
    }

    class DayViewHolder(private val binding: ItemFinanceSummaryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FinanceItem.DayEntry) {
            binding.textViewYear.visibility = View.GONE
            binding.textViewMonth.visibility = View.GONE
            binding.layoutDay.visibility = View.VISIBLE
            binding.textViewDay.text = "Día ${item.day}:"
            binding.textViewDayTotal.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.total)
        }
    }
}
