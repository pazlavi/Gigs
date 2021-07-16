package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.paz.gigs.R
import com.paz.gigs.adapters.events_list.EventViewHolder
import com.paz.gigs.adapters.events_list.EventsListAdapter
import com.paz.gigs.callbacks.EventSelectedListener
import com.paz.gigs.databinding.FragmentSavedEventsBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.objects.UserObject
import java.text.SimpleDateFormat
import java.util.*

class SavedEventsFragment() : Fragment() {
    companion object {
        const val TAG = "Paz_SavedEventsFragment"
    }

    private var _binding: FragmentSavedEventsBinding? = null
    private var listener: EventSelectedListener? = null
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private var myEvents: ArrayList<EventInfo>? = null
    private var dateRange = System.currentTimeMillis().run {
        Pair(this, this + 1000 * 60 * 60 * 24 * 7)
    }

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedEventsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDateRangeLabel()
        getEventsFromDB()
        setOnClickListeners()
    }

    private fun setDateRangeLabel() {
        val dtRange =
            "${formatter.format(dateRange.first)} - ${formatter.format(dateRange.second)}"
        binding.savedLBLDt.text = resources.getString(R.string.dateRange, dtRange)
    }

    private fun setOnClickListeners() {
        binding.savedBARTool.setOnMenuItemClickListener{ menuItem ->
            when (menuItem.itemId ){
                R.id.saved_BTN_favorite-> {
                    showDatePicker()
                    true}
                else -> false
            }
        }
    }

    private fun getEventsFromDB() {
        DatabaseOperations.getFavoriteEvents(
            UserObject.currentUser!!.userUUID,
            object : DatabaseOperations.OnDataReady {
                override fun onDataReady(res: Any?) {
                    val events = res as ArrayList<EventInfo>
                    Log.d(TAG, "onDataReady: $events")
                    myEvents = events
                    setListAdapter()
                }
            })
    }

    private fun setListAdapter() {
        binding.savedLSTList.apply {
            myEvents?.let {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(activity)
                adapter = EventsListAdapter(
                    it,
                    object : EventViewHolder.Companion.OnCardClicked {
                        override fun cardClicked(event: EventInfo) {
                            Log.d(TAG, "cardClicked: $event")
                            listener?.onEventSelected(event)
                        }
                    }).apply {
                        filterByDate(dateRange)
                }

            }
        }
    }



    private fun showDatePicker() {

        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val dateRangePicker =
            MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("Select dates range")
                .setSelection(
                    dateRange
                )
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
        dateRangePicker.addOnPositiveButtonClickListener {
            dateRange = it
            setDateRangeLabel()
            (binding.savedLSTList.adapter as EventsListAdapter).filterByDate(dateRange)


        }
        dateRangePicker.show(childFragmentManager, MaterialTimePicker::class.java.canonicalName)

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventSelectedListener) {
            listener = context
        } else {
            throw ClassCastException(
                "$context must implement EventSelectedListener."
            )
        }
    }
}
