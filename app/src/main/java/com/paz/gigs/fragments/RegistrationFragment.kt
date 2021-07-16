package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.paz.gigs.R
import com.paz.gigs.callbacks.OnRegistration
import com.paz.gigs.databinding.FragmentRegistrationBinding
import com.paz.gigs.models.enums.UserType
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.BundleViewModel
import java.util.*
import kotlin.collections.HashSet

class RegistrationFragment() : Fragment() {
    companion object {
        const val TAG = "Paz_RegistrationFragment"
    }

    private lateinit var listener: OnRegistration
    private var _binding: FragmentRegistrationBinding? = null
    private lateinit var genresSet: HashSet<String>
    private var genres: HashSet<String>? = null
    private var bundleViewModel: BundleViewModel? = null



    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.let {
            bundleViewModel = ViewModelProvider(it).get(BundleViewModel::class.java)

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        setGenresChips()
        setListeners()
        genresSet = HashSet()
        FirebaseAuth.getInstance().currentUser?.email
        binding.registrationETDEmail.editText?.setText(FirebaseAuth.getInstance().currentUser?.email)

        bundleViewModel?.getBundle()?.observe(viewLifecycleOwner) {
            it?.let { b ->
                restoreSavedData(b)
            }

        }
        val navController = binding.registrationBTNSelectGenres.findNavController()

        navController.currentBackStackEntry?.savedStateHandle?.getLiveData<java.util.HashSet<String>>(Consts.GENRES)
            ?.observe(
                viewLifecycleOwner
            ) { result ->
                // Do something with the result.
                genres = result
                Log.d(TAG, "got genres: $result")
                binding.registrationBTNSelectGenres.text =
                    resources.getString(R.string.selectGenresCounter, result.size)
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setListeners() {
        binding.registrationCPDj.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                binding.registrationLAYDj.visibility = GONE
            } else {
                binding.registrationLAYDj.visibility = VISIBLE

            }
        }

        binding.registrationBTNSelectGenres.setOnClickListener {
            saveCurrentState()
            val bundle = bundleOf(Consts.GENRES to genres)
            it.findNavController()
                .navigate(R.id.action_registrationFragment_to_genresDialog, bundle)
        }

        binding.registrationBTNSignUp.setOnClickListener {
            if (fieldsValidation()) {
                registerNewUser()
            }
        }

    }

    private fun registerNewUser() {
        val djUser: DjUser?
        val user: User?
        val uuid = FirebaseAuth.getInstance().currentUser?.uid ?: UUID.randomUUID().toString()
        val name = binding.registrationETDName.editText!!.text.toString()
        val phone = binding.registrationETDPhone.editText!!.text.toString()
        val email = binding.registrationETDEmail.editText!!.text.toString()
        val type = ArrayList<UserType>()
        if (binding.registrationCPClubber.isChecked) {
            type.add(UserType.CLUBBER)
        }
        if (binding.registrationCPPr.isChecked) {
            type.add(UserType.PR)
        }

        if (binding.registrationCPDj.isChecked) {
            type.add(UserType.DJ)
            val stageName = binding.registrationETDStageName.editText!!.text.toString()
            djUser = DjUser(uuid, name, phone, email, type, -1, stageName, genres!!.toList())
            addDjToDB(djUser)
        } else {
            user = User(uuid, name, phone, email, type,-1)
            addUserToDB(user)
        }

    }

    private fun addDjToDB(djUser: DjUser) {
        val db = Firebase.firestore
        db.collection("users")
            .document("djs")
            .collection("djs_list")
            .document(djUser.userUUID).set(djUser)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                addUserToDB(djUser.toUser())
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }


    }

    private fun addUserToDB(user: User) {
        val db = Firebase.firestore
        db.collection("users").document("all_users").collection("all_users_list")
            .document(user.userUUID).set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                showSnakeBar("Welcome ${user.fullName}!")
                listener.onRegistered(user)
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                showSnakeBar("Registration failed. Please try again")
            }
        db.collection("favorites_events").document(user.userUUID).set(mapOf(Consts.SAVED_EVENTS_KEY to listOf<String>()))
    }

    private fun fieldsValidation(): Boolean {
        var valid = true
        if (binding.registrationETDName.editText?.text?.isEmpty() == true) {
            binding.registrationETDName.error = "Name is missing"
            binding.registrationETDName.isErrorEnabled = true
            valid = false
        }
        if (binding.registrationETDEmail.editText?.text?.isEmpty() == true) {
            binding.registrationETDEmail.error = "Email is missing"
            binding.registrationETDEmail.isErrorEnabled = true
            valid = false
        }
        if (binding.registrationETDPhone.editText?.text?.isEmpty() == true) {
            binding.registrationETDPhone.error = "Phone number is missing"
            binding.registrationETDPhone.isErrorEnabled = true
            valid = false
        }
        if (binding.registrationCPDj.isChecked && binding.registrationETDStageName.editText?.text?.isEmpty() == true) {
            binding.registrationETDStageName.error = "Stage name is missing"
            binding.registrationETDStageName.isErrorEnabled = true
            valid = false
        }
        if(!binding.registrationCPDj.isChecked && (genres?.size ?: -1) <= 0){
            valid = false
            context?.let {
                Toast.makeText(it , "Missing Genres" , Toast.LENGTH_SHORT).show()

            }
        }

        return valid
    }

//    private fun setGenresChips() {
//        val lst = listOf("hip hop", "electronic", "house", "techno")
//        lst.forEach { s ->
//            Log.d(TAG, "setGenresChips: $s")
//            val c = Chip(binding.registrationCPGenresGroup.context).apply {
//                text = s
//                width = ViewGroup.LayoutParams.WRAP_CONTENT
//                height = ViewGroup.LayoutParams.WRAP_CONTENT
//                id = lst.indexOf(s)
//                isChecked = true
//                isCheckable = true
//                isEnabled = true
//                //setTextAppearance(com.google.android.material.R.style.Widget_MaterialComponents_Chip_Filter)
//                checkedIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_check)
//                setOnCheckedChangeListener { _, isChecked ->
//                    if (isChecked) {
//                        genresSet.add(this.text.toString())
//                    } else {
//                        genresSet.remove(this.text.toString())
//
//                    }
//                }
//            }
//
//            binding.registrationCPGenresGroup.addView(c)
//        }
//
//    }

    private fun showSnakeBar(msg : String){
        Snackbar.make(binding.registrationBTNSignUp.rootView, msg, Snackbar.LENGTH_LONG)
            .show()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnRegistration) {
            listener = context

        } else {
            throw ClassCastException(
                "$context must implement OnRegistration"
            )
        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "onSaveInstanceState: ")
        outState.putAll(saveCurrentState())
        Log.d(TAG, "onSaveInstanceState: ${outState.keySet()}")

    }

    private fun saveCurrentState(): Bundle {
        val outState = Bundle()
        outState.putString("name", binding.registrationETDName.editText?.text.toString())
        outState.putString("email", binding.registrationETDEmail.editText?.text.toString())
        outState.putString("phone", binding.registrationETDPhone.editText?.text.toString())
        outState.putString("djStageName", binding.registrationETDStageName.editText?.text.toString())
        outState.putBoolean("isDj" , binding.registrationCPDj.isChecked )
        outState.putBoolean("isPr" , binding.registrationCPPr.isChecked )
        outState.putBoolean("isClubber" , binding.registrationCPClubber.isChecked )
        bundleViewModel?.setBundle(outState)
        return outState
    }

    private fun restoreSavedData(bundle: Bundle) {



        binding.registrationETDName.editText?.setText(bundle.getString("name", ""))
        binding.registrationETDEmail.editText?.setText(bundle.getString("email", ""))
        binding.registrationETDPhone.editText?.setText(bundle.getString("phone", ""))
        binding.registrationETDStageName.editText?.setText(bundle.getString("djStageName", ""))
        binding.registrationCPDj.isChecked = bundle.getBoolean("isDj")
        binding.registrationCPPr.isChecked = bundle.getBoolean("isPr")
        binding.registrationCPClubber.isChecked = bundle.getBoolean("isClubber")

    }
}
