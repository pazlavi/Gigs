package com.paz.gigs.helpers

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.paz.gigs.R

class AuthResultContract : ActivityResultContract<Int, IdpResponse>() {

   private companion object {
        const val INPUT_INT = "input_int"
    }

    private val providers: List<AuthUI.IdpConfig> = listOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.PhoneBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
      //  AuthUI.IdpConfig.GoogleBuilder().build() //new AuthUI.IdpConfig.FacebookBuilder().build(),
        //  new AuthUI.IdpConfig.TwitterBuilder().build()
    )

    override fun createIntent(context: Context, input: Int?): Intent =  AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers).setLogo(R.drawable.ic_disco)
        .build().apply { putExtra(INPUT_INT, input) }

    override fun parseResult(resultCode: Int, intent: Intent?): IdpResponse? = when (resultCode) {
        Activity.RESULT_OK -> IdpResponse.fromResultIntent(intent)
        else -> null
    }

}