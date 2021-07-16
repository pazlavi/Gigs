package com.paz.gigs.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.paz.gigs.R
import com.paz.gigs.databinding.FragmentUserProfileBinding
import com.paz.gigs.models.enums.UserType
import com.paz.gigs.models.users.DjUser
import com.paz.gigs.models.users.User
import com.paz.gigs.models.users.UserAbs
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.gigs.view_models.UsersViewModel


class UserProfileFragment : Fragment() {
    private var _binding: FragmentUserProfileBinding? = null
    private lateinit var listener: LogoutListener
    private val model: UsersViewModel by activityViewModels()
    private var interfaceId = -1

    companion object {
        interface LogoutListener {
            fun logout()
        }

        const val TAG = "Paz_DjProfileFragment"
    }

    // This property is only valid between onCreateView and
// onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        UserObject.currentUser?.defaultInterface?.let {
            interfaceId = it

        }
        bindUserInfo()
        serOnClicks()
    }

    private fun serOnClicks() {

        binding.djProfileFABEdit.setOnClickListener { editUserProfile() }
        binding.djProfileBTNLogout.setOnClickListener {
            listener.logout()
        }
        binding.profileCPGChips.setOnCheckedChangeListener { _, checkedId ->
            interfaceId = when (checkedId) {
                binding.profileCPClubber.id -> Consts.CLUBBER_INTERFACE
                binding.profileCPDj.id -> Consts.DJ_INTERFACE
                binding.profileCPPr.id -> Consts.PR_INTERFACE
                else -> -1
            }
        }
    }

    private fun editUserProfile() {

        Log.d(TAG, "editUserProfile: ")
    }


    private fun bindUserInfo() {
        if (UserObject.currentUser!!.defaultInterface == Consts.DJ_INTERFACE) {
            model.getDj().observe(viewLifecycleOwner, { u ->
                bindUserInfoToUI(u , true)
            })
        }else{
            model.getUser().observe(viewLifecycleOwner, { u ->
                bindUserInfoToUI(u , false)
            })
        }
    }
    private fun bindUserInfoToUI(user : UserAbs, isDj : Boolean ){
        val u = if (isDj)  (user as DjUser) else (user as User)
        binding.djProfileLBLName.text =
            resources.getString(R.string.nameWithValue, u.fullName)

        binding.djProfileLBLEmail.text =
            resources.getString(R.string.phoneWithValue, u.email)
        binding.djProfileLBLPhone.text =
            resources.getString(R.string.emailWithValue, u.phoneNumber)
        binding.djProfileLBLTypes.text = resources.getString(
            R.string.userTypesWithValue,
            u.userTypes.toString().replace("[", "").replace("]", "")
        )
        if (isDj) {
            bindDjFieldsToUI(u as DjUser)
        }
        if (!u.userTypes.contains(UserType.DJ)) {
            binding.profileCPDj.visibility = GONE
        }
        if (!u.userTypes.contains(UserType.CLUBBER)) {
            binding.profileCPClubber.visibility = GONE
        }
        if (!u.userTypes.contains(UserType.PR)) {
            binding.profileCPPr.visibility = GONE
        }

        when (u.defaultInterface) {
            Consts.CLUBBER_INTERFACE -> binding.profileCPClubber.isChecked = true
            Consts.PR_INTERFACE -> binding.profileCPPr.isChecked = true
            Consts.DJ_INTERFACE -> binding.profileCPDj.isChecked = true
        }
    }

    private fun bindDjFieldsToUI(u: DjUser) {
        binding.djProfileLBLStageName.text =
            resources.getString(R.string.stageNameWithValue, u.stageName)
        binding.djProfileLBLStageName.visibility = VISIBLE
        binding.djProfileLBLGenres.text = resources.getString(
            R.string.genresWithValue,
            u.genresSet.toString().replace("[", "").replace("]", ""))
                    binding.djProfileLBLGenres.visibility = VISIBLE}

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is LogoutListener) {
            listener = context
        } else {
            throw ClassCastException(
                "$context must implement DjProfileFragment.LogoutListener."
            )
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: ")
        UserObject.currentUser?.defaultInterface?.let {
            if (it != interfaceId) {
                UserObject.updateDefaultInterface(interfaceId)
                Log.d(TAG, "onPause: updateDefaultInterface")

            }
        }
    }
}