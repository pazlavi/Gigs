package com.paz.gigs.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.appsflyer.CreateOneLinkHttpTask.ResponseListener
import com.appsflyer.share.ShareInviteHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.paz.gigs.R
import com.paz.gigs.databinding.FragmentEventInfoBinding
import com.paz.gigs.firestore.DatabaseOperations
import com.paz.gigs.models.events.EventInfo
import com.paz.gigs.objects.UserObject
import com.paz.gigs.view_models.EventInfoViewModel
import java.text.SimpleDateFormat
import java.util.*


class EventInfoFragment() : Fragment() {
    private companion object {
        const val TAG = "Paz_EventInfoFragment"
    }

    private var _binding: FragmentEventInfoBinding? = null
    private val model: EventInfoViewModel by activityViewModels()
    private var event: EventInfo? = null


    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.getEvent().observe(viewLifecycleOwner, { ev ->
            this.event = ev
            bindEventData()
        })
    }


    private fun bindEventData() {
        if (event != null) {
            val res = binding.eventInfoBTNFavorite.resources
            binding.eventInfoLBLDjName.text =
                res.getString(R.string.djNameWithValue, event!!.djStageName)
            binding.eventInfoLBLPrName.text =
                res.getString(R.string.prNameWithValue, event!!.prName)
            binding.eventInfoLBLEventAddress.text =
                res.getString(R.string.addressWithValue, event!!.address)
            binding.eventInfoLBLLocationName.text =
                res.getString(R.string.locationNameWithValue, event!!.locationName)
            binding.eventInfoLBLGenres.text = res.getString(
                R.string.genresWithValue,
                event!!.genres?.toString()?.replace("[", "")?.replace("]", "")
            )
            val dt = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.US).format(event!!.eventTimestamp)
            binding.eventInfoLBLDateTime.text =
                res.getString(R.string.dateTimeWithValue, dt)
            binding.eventInfoBTNFavorite.setOnClickListener { addEventToFavorite() }
            binding.eventInfoBTNNavigate.setOnClickListener { navigateToEvent() }
            binding.eventInfoBTNShare.setOnClickListener { shareEvent() }
        }
    }

    private fun addEventToFavorite() {
        Log.d(TAG, "addEventToFavorite: ")
        if (event != null && UserObject.currentUser?.userUUID != null)
            DatabaseOperations.addEventToFavorite(event!!, UserObject.currentUser!!.userUUID)

    }

    private fun navigateToEvent() {
        if (event == null)
            return
        val uri = "geo:${event!!.latLng?.latitude}${event!!.latLng?.longitude}?q=${event!!.address}"
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(uri)))


    }

    private fun shareEvent() {
        Log.d(TAG, "shareEvent: ")
        if (event != null) {
            val linkGenerator = ShareInviteHelper.generateInviteUrl(requireContext()).apply {

                channel = "user_sharing"
                campaign = "user_invite"
                setReferrerUID(UserObject.currentUser?.userUUID)
                setReferrerName(UserObject.currentUser?.fullName)
                addParameter("ts", System.currentTimeMillis().toString())
                addParameter("deep_link_value",event?.eventUUID)

            }
            val listener: ResponseListener = object : ResponseListener {
                override fun onResponse(s: String) {
                    Log.d(TAG, s)
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, s)
                        type = "text/plain"
                    }

                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }

                override fun onResponseError(s: String) {
                    MaterialAlertDialogBuilder(requireContext())
                        .setIcon(R.drawable.ic_error)
                        .setMessage(resources.getString(R.string.linkGeneratorFailed))
                        .setNeutralButton(resources.getString(R.string.close)) { _, _ ->
                            // Respond to negative button press
                        }
                        .show()
                }
            }
            linkGenerator.generateLink(requireContext(), listener)


        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
