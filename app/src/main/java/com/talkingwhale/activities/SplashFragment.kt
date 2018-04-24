package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobile.client.AWSMobileClient
import com.talkingwhale.R
import com.talkingwhale.databinding.FragmentSplashBinding
import com.talkingwhale.pojos.Status
import com.talkingwhale.util.navigateToFragment

class SplashFragment : Fragment() {

    private lateinit var binding: FragmentSplashBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_splash, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AWSMobileClient.getInstance().initialize(context) {
            val identityManager = IdentityManager.getDefaultIdentityManager()
            identityManager.resumeSession(activity, { authResults ->
                if (authResults.isUserSignedIn) {
                    AWSLoginModel.getUserId(context!!).observe(this, Observer {
                        if (it?.status == Status.SUCCESS) {
                            (activity as AppCompatActivity).navigateToFragment(MainFragment(), true)
                        }
                    })
                } else {
                    (activity as AppCompatActivity).navigateToFragment(LoginFragment(), true)
                }
            }, 1000)
        }.execute()
    }
}
