package com.talkingwhale.activities

import android.arch.lifecycle.Observer
import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.talkingwhale.R
import com.talkingwhale.databinding.ActivityLoginBinding
import com.talkingwhale.pojos.Status
import com.talkingwhale.util.navigateToFragment
import com.talkingwhale.util.toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : Fragment(), View.OnClickListener, AWSLoginHandler {

    private lateinit var awsLoginModel: AWSLoginModel
    private lateinit var binding: ActivityLoginBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_login, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        awsLoginModel = AWSLoginModel(context!!, this)

        // setting listeners
        registerButton.setOnClickListener(this)
        loginButton.setOnClickListener(this)
        confirmButton.setOnClickListener(this)
        resendConfirmationButton.setOnClickListener(this)
        resetButton.setOnClickListener(this)
        forgotButton.setOnClickListener(this)
        showLoginButton.setOnClickListener(this)
        showRegisterButton.setOnClickListener(this)

        registerContainer.visibility = View.GONE
        confirmContainer.visibility = View.GONE
        forgotContainer.visibility = View.GONE
    }

    override fun onRegisterSuccess(mustConfirmToComplete: Boolean) {
        if (mustConfirmToComplete) {
            Toast.makeText(context, "Almost done! Confirm code to complete registration", Toast.LENGTH_LONG).show()
            showConfirm(true)
        } else {
            Toast.makeText(context, "Registered! Login Now!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRegisterConfirmed() {
        Toast.makeText(context, "Registered! Login Now!", Toast.LENGTH_LONG).show()
        showLoginAction(true)
    }

    override fun onSignInSuccess() {
        AWSLoginModel.getUserId(context!!).observe(this, Observer {
            if (it?.status == Status.SUCCESS) {
                (activity as AppCompatActivity).navigateToFragment(MainActivity(), true)
            }
        })
    }

    override fun onResendConfirmationCodeSuccess(medium: String) {
        Toast.makeText(context, "Confirmation code sent! Destination:$medium", Toast.LENGTH_LONG).show()
    }

    override fun onRequestResetUserPasswordSuccess(medium: String) {
        Toast.makeText(context, "Reset code sent! Destination:$medium", Toast.LENGTH_LONG).show()
        showForgotAction(true)
    }

    override fun onResetUserPasswordSuccess() {
        Toast.makeText(context, "Password reset! Login Now!", Toast.LENGTH_LONG).show()
        showLoginAction(true)
    }

    override fun onFailure(process: Int, exception: Exception, cause: Int, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        if (cause != AWSLoginModel.CAUSE_MUST_CONFIRM_FIRST) {
            exception.printStackTrace()
        } else {
            showConfirm(true)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.registerButton -> registerAction()
            R.id.confirmButton -> confirmAction()
            R.id.resendConfirmationButton -> resendConfirmationAction()
            R.id.loginButton -> loginAction()
            R.id.resetButton -> resetAction()
            R.id.forgotButton -> forgotPasswordAction()
            R.id.showLoginButton -> showLoginAction(true)
            R.id.showRegisterButton -> showRegisterAction(true)
        }
    }

    private fun registerAction() {
        // do register and handles on interface
        awsLoginModel.registerUser(registerUsername!!.text.toString(), registerEmail!!.text.toString(), registerPassword!!.text.toString())
    }

    private fun confirmAction() {
        // do confirmation and handles on interface
        awsLoginModel.confirmRegistration(confirmationCode!!.text.toString())
    }

    private fun resendConfirmationAction() {
        // do resend confirmation code and handles on interface
        awsLoginModel.resendConfirmationCode()
    }

    private fun loginAction() {
        if (loginUser.text.isBlank()) {
            toast("Username cannot be blank")
            return
        }
        // do sign in and handles on interface
        awsLoginModel.signInUser(loginUser!!.text.toString(), loginPassword!!.text.toString())
    }

    private fun forgotPasswordAction() {
        if (loginUser!!.text.toString().isEmpty()) {
            Toast.makeText(context, "Username required.", Toast.LENGTH_LONG).show()
        } else {
            awsLoginModel.requestResetUserPassword(loginUser!!.text.toString())
        }
    }

    private fun resetAction() {
        // request reset password and handles on interface
        awsLoginModel.resetUserPasswordWithCode(resetCode!!.text.toString(), newPassword!!.text.toString())
    }

    private fun showLoginAction(show: Boolean) {
        if (show) {
            showRegisterAction(false)
            showConfirm(false)
            showForgotAction(false)
            loginContainer.visibility = View.VISIBLE
            showRegisterButton.visibility = View.VISIBLE
            showLoginButton.visibility = View.GONE
        } else {
            loginContainer.visibility = View.GONE
            loginUser!!.setText("")
            loginPassword!!.setText("")
        }
    }

    private fun showRegisterAction(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showConfirm(false)
            showForgotAction(false)
            registerContainer.visibility = View.VISIBLE
            showRegisterButton.visibility = View.GONE
            showLoginButton.visibility = View.VISIBLE
        } else {
            registerContainer.visibility = View.GONE
            registerUsername!!.setText("")
            registerEmail!!.setText("")
            registerPassword!!.setText("")
        }
    }

    private fun showConfirm(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showRegisterAction(false)
            showForgotAction(false)
            confirmContainer.visibility = View.VISIBLE
            showRegisterButton.visibility = View.GONE
            showLoginButton.visibility = View.VISIBLE
        } else {
            confirmContainer.visibility = View.GONE
            confirmationCode!!.setText("")
        }
    }

    private fun showForgotAction(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showRegisterAction(false)
            showConfirm(false)
            forgotContainer.visibility = View.VISIBLE
            showRegisterButton.visibility = View.GONE
            showLoginButton.visibility = View.VISIBLE
        } else {
            forgotContainer.visibility = View.GONE
            resetCode!!.setText("")
            newPassword!!.setText("")
        }
    }

}
