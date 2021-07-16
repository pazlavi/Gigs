package com.paz.gigs.view_models

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class BundleViewModel : ViewModel() {
    private val bundle = MutableLiveData<Bundle>()
    fun setBundle(name: Bundle?) {
        bundle.value = name
    }

    fun getBundle(): LiveData<Bundle?> {
        return bundle
    }

}