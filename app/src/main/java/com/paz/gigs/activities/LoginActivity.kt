package com.paz.gigs.activities

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appsflyer.AppsFlyerLib
import com.firebase.ui.auth.AuthUI.IdpConfig.*
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.paz.gigs.R
import com.paz.gigs.callbacks.OnRegistration
import com.paz.gigs.databinding.ActivityLoginBinding
import com.paz.gigs.helpers.AuthResultContract
import com.paz.gigs.models.enums.UserType
import com.paz.gigs.models.users.User
import com.paz.gigs.objects.UserObject
import com.paz.gigs.utils.Consts
import com.paz.prefy_lib.Prefy
import java.util.*


class LoginActivity : AppCompatActivity(), OnRegistration {
    companion object {
        private const val TAG = "Paz_LoginActivity"
        private const val RC_SIGN_IN = 1803
    }

    private val authResultLauncher =
        registerForActivityResult(AuthResultContract()) { idpResponse ->
            handleAuthResponse(idpResponse)
        }
    private lateinit var binding: ActivityLoginBinding
    private var uri: Uri? = null
    private var eventId: String? = null
    private lateinit var prefy: Prefy

    private val listener = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action == Consts.AF_DEEP_LINK) {
                eventId = intent.getStringExtra(Consts.AF_DEEP_LINK_VALUE)
            }
        }

    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            this.intent = it
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        prefy = Prefy.getInstance()
        binding.loginFRGFragment.visibility = GONE

    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(listener)
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(listener, IntentFilter(Consts.AF_DEEP_LINK))
        super.onResume()
        startLoginUI()

    }

    private fun startLoginUI() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            binding.loginPROGBar.visibility = GONE
            authResultLauncher.launch(RC_SIGN_IN)
        } else {
            onUserLoggedIn(FirebaseAuth.getInstance().currentUser)
        }

    }


    private fun onUserLoggedIn(user: FirebaseUser?) {

        user?.run {
            AppsFlyerLib.getInstance().apply {
                setCustomerIdAndLogSession(uid, this@LoginActivity)
                logEvent(
                    this@LoginActivity, "user_login",
                    mapOf("cuid" to uid, "ts" to System.currentTimeMillis())
                )
            }

            UserObject.findUser(uid, object : UserObject.OnFindUserListener {
                override fun getResults(isSuccessful: Boolean, user: User?) {
                    binding.loginPROGBar.visibility = GONE
                    if (isSuccessful && user != null) {
                        navigateToInterface()
                    } else {
                        binding.loginFRGFragment.visibility = VISIBLE

                    }
                }

            })
        }


    }

    private fun <T : Activity> makeIntent(newActivity: Class<T>) {
        val i = Intent(this@LoginActivity, newActivity).apply {
            data = uri
            if (!eventId.isNullOrEmpty())
                putExtra(Consts.AF_DEEP_LINK_VALUE, eventId)
        }
        startActivity(i)
        finish()

    }


    private fun shoWErrorDialog() {
        AlertDialog.Builder(this)
            .setTitle("Login failed")
            .setMessage("The login failed. Please try again")
            .setPositiveButton("Dismiss", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    override fun onRegistered(user: User) {
        prefy.putArrayList(Consts.USER_TYPES, user.userTypes as ArrayList<UserType>)
        prefy.putString(Consts.USER_ID, user.userUUID)
        prefy.putBoolean(Consts.REGISTER_KEY, true)
        UserObject.findUser(user.userUUID, object : UserObject.OnFindUserListener {
            override fun getResults(isSuccessful: Boolean, user: User?) {
                if (isSuccessful) {
                    user?.run {
                        if (this.defaultInterface in listOf(
                                Consts.CLUBBER_INTERFACE,
                                Consts.PR_INTERFACE, Consts.DJ_INTERFACE
                            )
                        )
                            navigateToInterface()
                        else
                            selectInterface()

                    }
                }
            }
        })

    }

    private fun navigateToInterface() {

        makeIntent(HomeActivity::class.java)

    }

    private fun selectInterface() {
        UserObject.currentUser?.let {
            if (it.userTypes.size == 1) {
                when (it.userTypes[0]) {
                    UserType.DJ -> UserObject.updateDefaultInterface(Consts.DJ_INTERFACE)
                    UserType.PR -> UserObject.updateDefaultInterface(Consts.PR_INTERFACE)
                    UserType.CLUBBER -> UserObject.updateDefaultInterface(Consts.CLUBBER_INTERFACE)
                }
                navigateToInterface()
            } else {
                val singleItems =
                    it.userTypes.map { userType -> userType.toString() }.toTypedArray()
                var checkedItem = 1

                MaterialAlertDialogBuilder(this)
                    .setTitle(resources.getString(R.string.selectInterface))
                    .setPositiveButton(resources.getString(R.string.select)) { _, _ ->
                        when (it.userTypes[checkedItem]) {
                            UserType.DJ -> UserObject.updateDefaultInterface(Consts.DJ_INTERFACE)
                            UserType.PR -> UserObject.updateDefaultInterface(Consts.PR_INTERFACE)
                            UserType.CLUBBER -> UserObject.updateDefaultInterface(
                                Consts.CLUBBER_INTERFACE
                            )
                        }
                        navigateToInterface()

                    }
                    // Single-choice items (initialized with checked item)
                    .setSingleChoiceItems(singleItems, checkedItem) { _, which ->
                        checkedItem = which
                    }
                    .show()
            }
        }
    }

    private fun handleAuthResponse(idpResponse: IdpResponse?) {
        when {
            (idpResponse == null || idpResponse.error != null) -> {
                shoWErrorDialog()
            }
            else -> {
                val user = FirebaseAuth.getInstance().currentUser
                onUserLoggedIn(user)
            }
        }
    }


}