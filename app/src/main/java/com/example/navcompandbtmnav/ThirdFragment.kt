package com.example.navcompandbtmnav

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment

class ThirdFragment : Fragment(R.layout.fragment_third) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: third")
    }
}

const val TAG = "hoge"