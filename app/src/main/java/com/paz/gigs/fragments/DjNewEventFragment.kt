package com.paz.gigs.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.birjuvachhani.locus.Locus
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.*
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.google.firebase.auth.FirebaseAuth
import com.paz.gigs.R
import com.paz.gigs.databinding.FragmentDjNewEventBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.helpers.MyLatLng
import com.paz.gigs.location.LocationUtils
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import com.paz.gigs.objects.DjUserObject
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.BundleViewModel
import java.text.SimpleDateFormat
import java.util.*


class DjNewEventFragment : Fragment() {
    private var _binding: FragmentDjNewEventBinding? = null

    private companion object {
        const val TAG = "Paz_DjNewEventFragment"
    }

    private var bundleViewModel: BundleViewModel? = null
    private var selectedHour: Int? = null
    private var selectedMinute: Int? = null
    private var dateAsLong: Long? = null
    private val binding get() = _binding!!
    private var genres: HashSet<String>? = null
    private var prUser: User? = null
    private var djUser: DjUser? = null

    private lateinit var placesClient: PlacesClient
    private var sessionToken: AutocompleteSessionToken? = null


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: ")
        outState.putAll(saveCurrentState())
        Log.d(TAG, "onSaveInstanceState: ${outState.keySet()}")

    }

    private fun saveCurrentState(): Bundle {
        val outState = Bundle()
        selectedHour?.let {
            outState.putInt("selectedHour", it)
        }
        selectedMinute?.let {
            outState.putInt("selectedMinute", it)
        }
        dateAsLong?.let {
            outState.putLong("dateAsLong", it)
        }
        outState.putString("pr", binding.djNewEventEDTPrName.editText?.text.toString())
        outState.putString("loc", binding.djNewEventEDTLocName.editText?.text.toString())
        outState.putString("address", binding.djNewEventEDTAddress.editText?.text.toString())
        outState.putString("djName", binding.djNewEventEDTDjName.editText?.text.toString())
        outState.putString(
            "djStageName",
            binding.djNewEventEDTDjStageName.editText?.text.toString()
        )
        bundleViewModel?.setBundle(outState)
        return outState
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            bundleViewModel = ViewModelProvider(it).get(BundleViewModel::class.java)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDjNewEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = binding.djNewEventBTNSelectGenres.findNavController()
        savedInstanceState?.let {
            restoreSavedData(it)
        }
        sessionToken = AutocompleteSessionToken.newInstance()

        context?.let {
            placesClient = Places.createClient(it)

        }

        bundleViewModel?.getBundle()?.observe(viewLifecycleOwner) {
            it?.let { b ->
                restoreSavedData(b)
            }

        }
        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<HashSet<String>>(Consts.GENRES)
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                genres = result
                Log.d(TAG, "got genres: $result")
                binding.djNewEventBTNSelectGenres.text =
                    resources.getString(R.string.selectGenresCounter, result.size)
            }

        showRequiredEDTs()
        bindUser()
        setAutoComplete()
        setOnClicks()

    }

    private fun setAutoComplete() {
        UserObject.currentUser?.defaultInterface?.run {
            if (this == Consts.DJ_INTERFACE) {
                if (DjUserObject.currentUser != null) {
                    djUser = DjUserObject.currentUser
                } else {
                    DjUserObject.findUser(
                        FirebaseAuth.getInstance().uid!!,
                        object : DjUserObject.OnFindUserListener {
                            override fun getResults(isSuccessful: Boolean, user: DjUser?) {
                                if (isSuccessful)
                                  djUser = DjUserObject.currentUser
                            }

                        })
                }
                bindPrUsers()
            } else if (this == Consts.PR_INTERFACE) {
                prUser = UserObject.currentUser
                bindDjUsers()

            }
        }
    }

    private fun restoreSavedData(bundle: Bundle) {
        Log.d(TAG, "restoreSavedData: ")
        bundle.getInt("selectedHour", -1).also {
            if (it != -1) {
                selectedHour = it
            }
        }

        bundle.getInt("selectedMinute", -1).also {
            if (it != -1) {
                selectedMinute = it
            }
        }
        bundle.getLong("dateAsLong", -1L).also {
            if (it != -1L) {
                dateAsLong = it
                onDateSelected(it)

            }
        }
        if (selectedHour != null && selectedMinute != null) {
            onTimeSelected(selectedHour!!, selectedMinute!!)
        }



        binding.djNewEventEDTPrName.editText?.setText(bundle.getString("pr", ""))
        binding.djNewEventEDTLocName.editText?.setText(bundle.getString("loc", ""))
        binding.djNewEventEDTAddress.editText?.setText(bundle.getString("address", ""))
        binding.djNewEventEDTDjName.editText?.setText(bundle.getString("djName", ""))
        binding.djNewEventEDTDjStageName.editText?.setText(bundle.getString("djStageName", ""))

    }

    private fun showRequiredEDTs() {
        binding.djNewEventEDTDjName.also {
            textFiledHideError(it)
            it.editText?.addTextChangedListener { _ ->
                textFiledHideError(it)
            }
        }
        binding.djNewEventEDTDjStageName.also {
            textFiledHideError(it)
            it.editText?.addTextChangedListener { _ ->
                textFiledHideError(it)
            }
        }
        binding.djNewEventEDTAddress.also {
            textFiledHideError(it)
            it.editText?.addTextChangedListener { _ ->
                textFiledHideError(it)
            }
        }
        binding.djNewEventEDTLocName.also {
            textFiledHideError(it)
            it.editText?.addTextChangedListener { _ ->
                textFiledHideError(it)
            }
        }

    }

    private fun getPlacePredictions(query: String) {
        Log.d(TAG, "getPlacePredictions: $query")
        activity?.let {
            Locus.getCurrentLocation(it) { result ->
                result.location?.let { l ->
                    val loc = LatLng(l.latitude, l.longitude)
                    val bias: LocationBias =
                        LocationUtils.getRectangularBoundsWithRadius(loc, 20000)

                    // Create a new programmatic Place Autocomplete request in Places SDK for Android
                    val newRequest = FindAutocompletePredictionsRequest
                        .builder()
                        .setSessionToken(sessionToken)
                        .setLocationBias(bias)
                        .setTypeFilter(TypeFilter.ESTABLISHMENT)
                        .setQuery(query)
                        .build()

                    // Perform autocomplete predictions request
                    placesClient.findAutocompletePredictions(newRequest)
                        .addOnSuccessListener { response ->
                            Log.d(TAG, "getPlacePredictions: response = $response")
                            val predictions = response.autocompletePredictions
                            bindAutoCompletePlaces(predictions)
                        }.addOnFailureListener { exception: Exception? ->
                            exception?.printStackTrace()
                            if (exception is ApiException) {
                                Log.e(TAG, "Place not found: " + exception.statusCode)
                            }
                        }
                }
                result.error?.let { /* Received error! */ }
            }
        }

    }

    private fun bindPrUsers() {
        DatabaseOperations.getAllPrUsers(object : DatabaseOperations.OnDataReady {
            override fun onDataReady(res: Any?) {
                Log.d(TAG, "onDataReady: $res")

                res?.let {
                    try {
                        val users = res as ArrayList<User>

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.list_item,
                            users.map { u -> u.fullName })
                        (binding.djNewEventEDTPrName.editText as? AutoCompleteTextView)?.apply {
                            setAdapter(adapter)
                            setOnItemClickListener { _, _, position, _ ->
                                prUser = users[position]
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }

        })
    }

    private fun bindDjUsers() {
        DatabaseOperations.getAllDjUsers(object : DatabaseOperations.OnDataReady {
            override fun onDataReady(res: Any?) {
                Log.d(TAG, "onDataReady: $res")

                res?.let {
                    try {
                        val users = res as ArrayList<DjUser>

                        val adapter = ArrayAdapter(
                            requireContext(),
                            R.layout.list_item,
                            users.map { u -> u.fullName })
                        (binding.djNewEventEDTDjName.editText as? AutoCompleteTextView)?.apply {
                            setAdapter(adapter)
                            setOnItemClickListener { _, _, position, _ ->
                                djUser = users[position]
                                binding.djNewEventEDTDjStageName.editText?.setText(djUser!!.stageName)
                            }
                        }
                        val adapter2 = ArrayAdapter(
                            requireContext(),
                            R.layout.list_item,
                            users.map { u -> u.stageName })
                        (binding.djNewEventEDTDjStageName.editText as? AutoCompleteTextView)?.apply {
                            setAdapter(adapter2)
                            setOnItemClickListener { _, _, position, _ ->
                                djUser = users[position]
                                binding.djNewEventEDTDjName.editText?.setText(djUser!!.fullName)

                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }
            }

        })
    }

    private fun bindAutoCompletePlaces(predictions: MutableList<AutocompletePrediction>) {
        Log.d(TAG, "bindAutoCompletePlaces: $predictions")
        try {

            val adapter2 = ArrayAdapter(
                requireContext(),
                R.layout.list_item,
                predictions.map { p -> p.getPrimaryText(null) })
            (binding.djNewEventEDTLocName.editText as? AutoCompleteTextView)?.apply {
                Log.d(TAG, "bindAutoCompletePlaces: apply")
                setAdapter(adapter2)
                setOnItemClickListener { _, _, position, _ ->
                    Log.d(TAG, "bindAutoCompletePlaces: onItemSelected")

                    predictions[position]
                        .placeId
                    getPlaceAddress(
                        predictions[position]
                            .placeId
                    )
                    Log.d(
                        TAG, "placeId: ${
                            predictions[position]
                                .placeId
                        }"
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, "bindAutoCompletePlaces: Exception: ${e.message}")
        }
    }


    private fun getPlaceAddress(placeId: String) {
// Specify the fields to return.
        val placeFields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS)

// Construct a request object, passing the place ID and fields array.
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.d(
                    TAG,
                    "Place found: ${place.name}, ${place.address},${place.addressComponents}"
                )
                binding.djNewEventEDTAddress.editText?.setText(
                    place.address
                )
            }.addOnFailureListener { exception: Exception ->
                exception.printStackTrace()
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")

                }
            }
    }

    private fun bindUser() {
        DjUserObject.currentUser?.let {
            when (it.defaultInterface) {
                Consts.DJ_INTERFACE -> {
                    binding.djNewEventEDTDjName.editText?.run {
                        setText(it.fullName)
                        isEnabled = false
                    }
                    binding.djNewEventEDTDjStageName.editText?.run {
                        setText(it.stageName)
                        isEnabled = false
                    }
                }
                Consts.PR_INTERFACE -> {
                    binding.djNewEventEDTPrName.editText?.run {
                        setText(it.fullName)
                        isEnabled = false
                    }
                }
                else -> return
            }
        }
    }

    private fun setOnClicks() {
        binding.djNewEventBARTool.setOnClickListener {
            it.findNavController().popBackStack()
        }

        binding.djNewEventBTNSelectGenres.setOnClickListener {
            saveCurrentState()
            val bundle = bundleOf(Consts.GENRES to genres)
            it.findNavController()
                .navigate(R.id.action_djNewEventFragment_to_selectGenresFragment, bundle)
        }

        binding.djNewEventBTNDate.setOnClickListener {
            showDatePicker()
        }
        binding.djNewEventBTNTime.setOnClickListener {
            showTimePicker()
        }

        binding.djNewEventBTNSave.setOnClickListener {
            saveNewEvent()
        }

        binding.djNewEventEDTLocName.editText?.addTextChangedListener {
            Log.d(TAG, "djNewEventEDTLocName: addTextChangedListener ")
            it?.toString()?.let { str ->
                getPlacePredictions(str)

            }
        }
    }

    private fun saveNewEvent() {
        if (validateFields()) {
            val address = binding.djNewEventEDTAddress.editText?.text.toString()
            context?.let {
                val latLng = LocationUtils.getLocationFromAddress(it, address)?.let { it1 ->
                    MyLatLng(
                        it1
                    )
                }
                val eventUUID = UUID.randomUUID().toString()
                val djName = binding.djNewEventEDTDjName.editText?.text.toString()
                val djStageName = binding.djNewEventEDTDjStageName.editText?.text.toString()
                val prName = binding.djNewEventEDTPrName.editText?.text.toString()
                val locationName = binding.djNewEventEDTLocName.editText?.text.toString()
                val eventTimestamp = getEventStartTS()
                val creationTimestamp = System.currentTimeMillis()
                val genres = genres?.toList()
                val isPrivateEvent =
                    binding.djNewEventCPGGroup.checkedChipId == binding.djNewEventCPPrivate.id
                val newEvent = EventInfo(
                    eventUUID,
                    djName,
                    djStageName,
                    prName,
                    locationName,
                    address,
                    latLng,
                    latLng?.toGeoLocationHash(),
                    eventTimestamp,
                    creationTimestamp,
                    genres,
                    isPrivateEvent,
                    djUser?.userUUID,
                    prUser?.userUUID
                )
                Log.d(TAG, "saveNewEvent: newEvent$newEvent")
                DatabaseOperations.saveNewEvent(
                    newEvent,
                    object : DatabaseOperations.OnInsertResults {
                        override fun isInserted(insert: Boolean) {
                            if (insert) {
                                showSnakeBar("Event Saved Successfully")
                                val navController = binding.djNewEventBARTool.findNavController()
                                bundleViewModel?.setBundle(null)
                                navController.popBackStack()
                            } else {
                                showSnakeBar("Failed to save the event")
                            }

                        }

                    })
            }
        }

    }


    private fun validateFields(): Boolean {
        var valid = true

        if (binding.djNewEventEDTDjName.editText?.text.toString().isEmpty()) {
            valid = false
            textFiledShowError(binding.djNewEventEDTDjName, "Dj Name")

        }
        if (binding.djNewEventEDTDjStageName.editText?.text.toString().isEmpty()) {
            valid = false
            textFiledShowError(binding.djNewEventEDTDjStageName, "Dj Stage Name")

        }

        if (binding.djNewEventEDTAddress.editText?.text.toString().isEmpty()) {
            valid = false
            textFiledShowError(binding.djNewEventEDTAddress, "Event Address")


        }
        if (binding.djNewEventEDTLocName.editText?.text.toString().isEmpty()) {
            valid = false
            textFiledShowError(binding.djNewEventEDTLocName, "Location Name")

        }
        if (dateAsLong == null) {
            valid = false
            showToast("Event date is missing")
        }
        if (selectedHour == null) {
            valid = false
            showToast("Event start hour is missing")


        }
        if (selectedMinute == null) {
            valid = false
            showToast("Event start hour is missing")
        }
        if ((genres?.size ?: -1) <= 0) {
            valid = false
            showToast("Event genres are missing")

        }

        return valid

    }

    private fun showToast(msg: String) {
        context?.let {
            Toast.makeText(it, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun textFiledShowError(edt: TextInputLayout, fileName: String) {
        edt.isErrorEnabled = true
        edt.error = "Missing $fileName"
        edt.helperText = ""
    }

    private fun textFiledHideError(edt: TextInputLayout) {
        edt.isErrorEnabled = false
        edt.error = ""
        edt.helperText = "*Required"
    }


    private fun showDatePicker() {
        val constraintsBuilder =
            CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.now())
        val datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .setCalendarConstraints(constraintsBuilder.build())
                .build()
        datePicker.addOnPositiveButtonClickListener {
            onDateSelected(it)
        }


        datePicker.showNow(childFragmentManager, "dt")

    }

    private fun onDateSelected(it: Long) {
        dateAsLong = it
        val dFormat = SimpleDateFormat("dd-MM-yyyy", Locale.US)
        binding.djNewEventBTNDate.text = dFormat.format(it)

    }


    private fun showTimePicker() {
        val hFormat = SimpleDateFormat("HH", Locale.US)
        val mFormat = SimpleDateFormat("mm", Locale.US)
        val hour = selectedHour ?: hFormat.format(Date()).toInt()
        val minute = selectedMinute ?: mFormat.format(Date()).toInt()

        MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(hour)
            .setMinute(minute)
            .build()
            .apply {
                addOnPositiveButtonClickListener { onTimeSelected(this.hour, this.minute) }
            }.show(childFragmentManager, MaterialTimePicker::class.java.canonicalName)
    }

    private fun onTimeSelected(hour: Int, minute: Int) {
        selectedHour = hour
        selectedMinute = minute
        val hourAsText = if (hour < 10) "0$hour" else hour
        val minuteAsText = if (minute < 10) "0$minute" else minute
        binding.djNewEventBTNTime.text =
            resources.getString(R.string.selectedTime, hourAsText, minuteAsText)

    }

    private fun getEventStartTS(): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = dateAsLong!!
            set(Calendar.HOUR, selectedHour!!)
            set(Calendar.MINUTE, selectedMinute!!)
        }
        Log.d(TAG, "getEventStartTS: ${cal.timeInMillis}")
        return cal.timeInMillis
    }

    private fun showSnakeBar(msg: String) {
        Snackbar.make(binding.djNewEventBTNSave.rootView, msg, Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


}