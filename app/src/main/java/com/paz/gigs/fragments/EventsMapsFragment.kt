package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.birjuvachhani.locus.Locus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.paz.gigs.R
import com.paz.gigs.adapters.CustomInfoWindowForGoogleMap
import com.paz.gigs.adapters.events_list.EventViewHolder
import com.paz.gigs.adapters.events_list.EventsListAdapter
import com.paz.gigs.callbacks.EventSelectedListener
import com.paz.gigs.databinding.ClubberFilterDialogBinding
import com.paz.gigs.databinding.FragmentEventsMapsBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.prefy_lib.Prefy
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class EventsMapsFragment : Fragment(), GoogleMap.OnInfoWindowClickListener {
    private var _binding: FragmentEventsMapsBinding? = null
    private val binding get() = _binding!!
    private var myEvents: ArrayList<EventInfo>? = null
    private var listener: EventSelectedListener? = null
    private var googleMap: GoogleMap? = null
    private val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.US)
    private var dateRange = System.currentTimeMillis().run {
        Pair(this, this + 1000 * 60 * 60 * 24 * 7)
    }

    private companion object {
        const val TAG = "Paz_EventsMapsFragment"
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.dj_event_MAP_map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        getEvents()
        setUserFab()
        setOnClick()
        setGenresNav()

    }

    private fun setGenresNav() {
        binding.root.findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<HashSet<String>>(
            Consts.GENRES
        )
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                showFilterDialog(result)
            }
    }

    private fun setUserFab() {
        UserObject.currentUser?.defaultInterface?.run {
            when {
                this == Consts.DJ_INTERFACE -> {
                    setNavToNewEvent()
                }
                this == Consts.PR_INTERFACE -> {
                    setNavToNewEvent()
                }
                this == Consts.CLUBBER_INTERFACE -> {
                    binding.djEventFABAdd.run {
                        text = resources.getString(R.string.searchOptions)
                        icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_filter, null)
                    }
                    setOpenFilterMenu()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EventSelectedListener) {
            listener = context
        } else {
            throw ClassCastException(
                "$context must implement EventsMapsFragment.EventSelectedListener."
            )
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        this.googleMap = googleMap
        googleMap.setInfoWindowAdapter(
            CustomInfoWindowForGoogleMap(
                requireContext()
            )
        )
        googleMap.setMinZoomPreference(1.0f)
        googleMap.setMaxZoomPreference(25.0f)
        this.googleMap?.setOnInfoWindowClickListener(this::onInfoWindowClick)
        this.activity?.let {
            setEventsMarkers()
            Locus.getCurrentLocation(it) { result ->
                result.location?.let { l ->
                    val loc = LatLng(l.latitude, l.longitude)
                    googleMap.addMarker(MarkerOptions().position(loc).title("Current Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f))
                }
                result.error?.let { /* Received error! */ }
            }

        }


    }

    private fun setEventsMarkers() {
        if (googleMap != null) {
            myEvents?.forEach { e ->
                addMarker(e, googleMap!!)
            }
        }
    }

    private fun addMarker(event: EventInfo, googleMap: GoogleMap) {
        event.latLng?.let {
            googleMap.addMarker(
                MarkerOptions().position(it.toGoogleLatLng()).title(event.locationName)
                    .snippet(event.toJson())
            )
        }

    }


    private fun getEvents() {
        when (UserObject.currentUser?.defaultInterface) {
            Consts.DJ_INTERFACE -> getPrOrDjEvents(true)
            Consts.PR_INTERFACE -> getPrOrDjEvents(false)
            Consts.CLUBBER_INTERFACE -> getClubberEvents()
        }
    }

    private fun getClubberEvents() {
        if (context != null) {
            Locus.getCurrentLocation(requireContext()) {
                it.location?.run {
                    val prefy = Prefy.getInstance()
                    DatabaseOperations.searchForEvents(
                        this.latitude,
                        this.longitude,
                        prefy.getInt(Consts.DEFAULT_SLIDER_VALUE, 20),
                        dateRange,
                        prefy.getArrayList(Consts.GENRES_LIST, ArrayList<String>()),
                        object : DatabaseOperations.OnDataReady {
                            override fun onDataReady(res: Any?) {
                                myEvents = res as ArrayList<EventInfo>
                                setEventsMarkers()
                                setListAdapter()
                            }

                        })

                }
            }
        }
    }


    private fun getPrOrDjEvents(isDj: Boolean) {
        UserObject.currentUser?.userUUID?.let { it ->
            DatabaseOperations.getMyFutureEvents(
                it,
                isDj,
                object : DatabaseOperations.OnDataReady {
                    override fun onDataReady(res: Any?) {
                        res?.let { r ->
                            myEvents = r as ArrayList<EventInfo>
                            setEventsMarkers()
                            setListAdapter()
                        }
                    }
                })
        }

    }

    private fun setListAdapter() {
        binding.djEventLSTList.apply {
            myEvents?.let {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(activity)
                adapter = EventsListAdapter(it, object : EventViewHolder.Companion.OnCardClicked {
                    override fun cardClicked(event: EventInfo) {
                        Log.d(TAG, "cardClicked: $event")
                        listener?.onEventSelected(event)
                    }


                })


            }
        }
    }

    private fun setNavToNewEvent() {
        binding.djEventFABAdd.setOnClickListener {
            it.findNavController().navigate(R.id.nav_to_new_dj_event)
        }
    }

    private fun showFilterDialog(set: HashSet<String>?) {
        val prefy = Prefy.getInstance()
        var sliderValue = prefy.getInt(Consts.DEFAULT_SLIDER_VALUE, 20)
        var genresList = HashSet<String>()
        var dialog: AlertDialog? = null
        genresList.addAll(prefy.getArrayList(Consts.GENRES_LIST, ArrayList<String>()))
        if (set != null) {
            genresList = set
        }

        val dtRange =
            "${formatter.format(dateRange.first)} - ${formatter.format(dateRange.second)}" // week view starting from today

        val bi = ClubberFilterDialogBinding.inflate(LayoutInflater.from(requireContext()))
        bi.filtersLBLRadius.text = resources.getString(R.string.searchRadius, sliderValue)
        bi.filtersLBLDt.text = resources.getString(R.string.dateRange, dtRange)

        bi.filtersLBLGenres.movementMethod = ScrollingMovementMethod()
        bi.filtersLBLGenres.text = resources.getString(
            R.string.genresWithValue,
            genresList.toString().replace("[", "").replace("]", "")
        )
        bi.filtersBTNDt.setOnClickListener {
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
                val dtRange =
                    "${formatter.format(dateRange.first)} - ${formatter.format(dateRange.second)}"
                bi.filtersLBLDt.text = resources.getString(R.string.dateRange, dtRange)


            }
            dateRangePicker.show(childFragmentManager, MaterialTimePicker::class.java.canonicalName)
        }

        bi.filtersBTNSelectGenres.setOnClickListener {

            val bundle = bundleOf(Consts.GENRES to genresList)
            binding.root.findNavController()
                .navigate(R.id.user_select_genres_nav, bundle)
            dialog?.dismiss()
        }
        bi.filtersSLDRadius.addOnChangeListener { _, value, _ ->
            // Responds to when slider's value is changed
            sliderValue = value.toInt()
            bi.filtersLBLRadius.text = resources.getString(R.string.searchRadius, sliderValue)
        }
        //  var dialog =
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(bi.root)
            .setPositiveButton(resources.getString(R.string.save)) { _, _ ->
                prefy.putArrayList(Consts.GENRES_LIST, ArrayList<String>(genresList))
                prefy.putInt(Consts.DEFAULT_SLIDER_VALUE, sliderValue)
                getClubberEvents()
                binding.djEventLSTList.adapter?.notifyDataSetChanged()
            }
            .setPositiveButtonIcon(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_save,
                    null
                )
            )
            .setNegativeButton(resources.getString(R.string.cancel)) { _, _ -> }
            .setNegativeButtonIcon(
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.ic_cancel,
                    null
                )
            ).create()

        dialog.show()


    }

    private fun setOpenFilterMenu() {
        binding.djEventFABAdd.setOnClickListener {
            showFilterDialog(null)
        }
    }

    private fun setOnClick() {


        binding.djEventBARTool.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.event_ITEM_list -> {
                    binding.djEventLSTList.visibility = VISIBLE
                    binding.djEventFRGMap.visibility = GONE
                    true
                }
                R.id.event_ITEM_map -> {
                    binding.djEventLSTList.visibility = GONE
                    binding.djEventFRGMap.visibility = VISIBLE
                    true
                }
                else -> false
            }
        }
    }

    override fun onInfoWindowClick(marker: Marker) {
        if (marker.isInfoWindowShown) {
            try {
                val event = Gson().fromJson(marker.snippet, EventInfo::class.java)
                Log.d(TAG, "onMarkerClick: ")
                listener?.onEventSelected(event)


            } catch (e: JsonSyntaxException) {
                e.printStackTrace()

            }
        }
    }


}