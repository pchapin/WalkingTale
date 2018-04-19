package com.talkingwhale.activities

import android.app.Activity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.preference.PreferenceFragmentCompat
import android.support.v7.preference.PreferenceManager
import android.util.Log
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler
import com.talkingwhale.R
import java.lang.Exception
import kotlin.concurrent.thread


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)
    }

    internal class SettingsFragment : PreferenceFragmentCompat() {

        private lateinit var mainViewModel: MainViewModel

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)
            deleteAccountListener()
            mainViewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        }

        private fun deleteAccountListener() {
            val deletePref = findPreference(resources.getString(R.string.pref_key_delete_account))

            deletePref.setOnPreferenceClickListener {
                AlertDialog.Builder(context!!)
                        .setTitle("Delete account?")
                        .setMessage("This will delete all of your content in Walking Tale and can't be undone.")
                        .setPositiveButton("Yes, delete my account.", { _, _ -> deleteAccount() })
                        .setNegativeButton("No                    ", { _, _ -> })
                        .show()
                return@setOnPreferenceClickListener true
            }
        }

        private fun deleteAccount() {
            mainViewModel.currentUser.observe(this, Observer {
                if (it?.data != null) {
                    val user = it.data
                    mainViewModel.deleteUsersPosts(it.data).observe(this, Observer {
                        if (it?.data != null) {
                            mainViewModel.deleteUserS3Content(context!!, user).observe(this, Observer {
                                if (it?.data != null) {
                                    mainViewModel.deleteUser(user).observe(this, Observer {
                                        if (it?.data != null) {

                                            thread(start = true) {
                                                CognitoUserPool(context, IdentityManager.getDefaultIdentityManager().configuration)
                                                        .currentUser.deleteUser(object : GenericHandler {
                                                    override fun onSuccess() {
                                                        IdentityManager.getDefaultIdentityManager().signOut()
                                                        val intent = Intent()
                                                        intent.putExtra(DELETED_POST_KEY, true)
                                                        activity?.setResult(Activity.RESULT_OK, intent)
                                                        activity?.finish()
                                                    }

                                                    override fun onFailure(exception: Exception?) {
                                                        Log.i(SettingsActivity::class.java.simpleName, "" + exception)
                                                    }
                                                })
                                            }
                                        }
                                    })
                                }
                            })
                        }
                    })
                }
            })
            mainViewModel.setCurrentUserId(MainActivity.cognitoId)
        }
    }

    companion object {
        const val DELETED_POST_KEY = "DELETED_POST_KEY"
    }
}
