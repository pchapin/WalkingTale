package com.talkingwhale.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.talkingwhale.R

class LoginActivity : AppCompatActivity(), View.OnClickListener, AWSLoginHandler {

    private lateinit var awsLoginModel: AWSLoginModel

    // UI variables
    // login
    private var userLoginEditText: EditText? = null
    private var passwordLoginEditText: EditText? = null
    // register
    private var userNameRegisterEditText: EditText? = null
    private var userEmailRegisterEditText: EditText? = null
    private var passwordRegisterEditText: EditText? = null
    // confirm registration
    private var confirmationCodeEditText: EditText? = null
    // reset / forgot
    private var resetCodeEditText: EditText? = null
    private var newPasswordEditText: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        awsLoginModel = AWSLoginModel(this, this)

        // assigning UI variables
        userLoginEditText = findViewById(R.id.loginUser)
        passwordLoginEditText = findViewById(R.id.loginPassword)
        userNameRegisterEditText = findViewById(R.id.registerUsername)
        userEmailRegisterEditText = findViewById(R.id.registerEmail)
        passwordRegisterEditText = findViewById(R.id.registerPassword)
        confirmationCodeEditText = findViewById(R.id.confirmationCode)
        resetCodeEditText = findViewById(R.id.resetCode)
        newPasswordEditText = findViewById(R.id.newPassword)

        // setting listeners
        findViewById<View>(R.id.registerButton).setOnClickListener(this)
        findViewById<View>(R.id.loginButton).setOnClickListener(this)
        findViewById<View>(R.id.confirmButton).setOnClickListener(this)
        findViewById<View>(R.id.resendConfirmationButton).setOnClickListener(this)
        findViewById<View>(R.id.resetButton).setOnClickListener(this)
        findViewById<View>(R.id.forgotButton).setOnClickListener(this)
        findViewById<View>(R.id.showLoginButton).setOnClickListener(this)
        findViewById<View>(R.id.showRegisterButton).setOnClickListener(this)
    }

    override fun onRegisterSuccess(mustConfirmToComplete: Boolean) {
        if (mustConfirmToComplete) {
            Toast.makeText(this@LoginActivity, "Almost done! Confirm code to complete registration", Toast.LENGTH_LONG).show()
            showConfirm(true)
        } else {
            Toast.makeText(this@LoginActivity, "Registered! Login Now!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onRegisterConfirmed() {
        Toast.makeText(this@LoginActivity, "Registered! Login Now!", Toast.LENGTH_LONG).show()
        showLoginAction(true)
    }

    override fun onSignInSuccess() {
        this@LoginActivity.startActivity(Intent(this@LoginActivity, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
    }

    override fun onResendConfirmationCodeSuccess(medium: String) {
        Toast.makeText(this@LoginActivity, "Confirmation code sent! Destination:$medium", Toast.LENGTH_LONG).show()
    }

    override fun onRequestResetUserPasswordSuccess(medium: String) {
        Toast.makeText(this@LoginActivity, "Reset code sent! Destination:$medium", Toast.LENGTH_LONG).show()
        showForgotAction(true)
    }

    override fun onResetUserPasswordSuccess() {
        Toast.makeText(this@LoginActivity, "Password reset! Login Now!", Toast.LENGTH_LONG).show()
        showLoginAction(true)
    }

    override fun onFailure(process: Int, exception: Exception, cause: Int, message: String) {
        Toast.makeText(this@LoginActivity, message, Toast.LENGTH_LONG).show()
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
        awsLoginModel.registerUser(userNameRegisterEditText!!.text.toString(), userEmailRegisterEditText!!.text.toString(), passwordRegisterEditText!!.text.toString())
    }

    private fun confirmAction() {
        // do confirmation and handles on interface
        awsLoginModel.confirmRegistration(confirmationCodeEditText!!.text.toString())
    }

    private fun resendConfirmationAction() {
        // do resend confirmation code and handles on interface
        awsLoginModel.resendConfirmationCode()
    }

    private fun loginAction() {
        // do sign in and handles on interface
        awsLoginModel.signInUser(userLoginEditText!!.text.toString(), passwordLoginEditText!!.text.toString())
    }

    private fun forgotPasswordAction() {
        if (userLoginEditText!!.text.toString().isEmpty()) {
            Toast.makeText(this@LoginActivity, "Username required.", Toast.LENGTH_LONG).show()
        } else {
            awsLoginModel.requestResetUserPassword(userLoginEditText!!.text.toString())
        }
    }

    private fun resetAction() {
        // request reset password and handles on interface
        awsLoginModel.resetUserPasswordWithCode(resetCodeEditText!!.text.toString(), newPasswordEditText!!.text.toString())
    }

    private fun showLoginAction(show: Boolean) {
        if (show) {
            showRegisterAction(false)
            showConfirm(false)
            showForgotAction(false)
            findViewById<View>(R.id.loginContainer).visibility = View.VISIBLE
            findViewById<View>(R.id.showRegisterButton).visibility = View.VISIBLE
            findViewById<View>(R.id.showLoginButton).visibility = View.GONE
        } else {
            findViewById<View>(R.id.loginContainer).visibility = View.GONE
            userLoginEditText!!.setText("")
            passwordLoginEditText!!.setText("")
        }
    }

    private fun showRegisterAction(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showConfirm(false)
            showForgotAction(false)
            findViewById<View>(R.id.registerContainer).visibility = View.VISIBLE
            findViewById<View>(R.id.showRegisterButton).visibility = View.GONE
            findViewById<View>(R.id.showLoginButton).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.registerContainer).visibility = View.GONE
            userNameRegisterEditText!!.setText("")
            userEmailRegisterEditText!!.setText("")
            passwordRegisterEditText!!.setText("")
        }
    }

    private fun showConfirm(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showRegisterAction(false)
            showForgotAction(false)
            findViewById<View>(R.id.confirmContainer).visibility = View.VISIBLE
            findViewById<View>(R.id.showRegisterButton).visibility = View.GONE
            findViewById<View>(R.id.showLoginButton).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.confirmContainer).visibility = View.GONE
            confirmationCodeEditText!!.setText("")
        }
    }

    private fun showForgotAction(show: Boolean) {
        if (show) {
            showLoginAction(false)
            showRegisterAction(false)
            showConfirm(false)
            findViewById<View>(R.id.forgotContainer).visibility = View.VISIBLE
            findViewById<View>(R.id.showRegisterButton).visibility = View.GONE
            findViewById<View>(R.id.showLoginButton).visibility = View.VISIBLE
        } else {
            findViewById<View>(R.id.forgotContainer).visibility = View.GONE
            resetCodeEditText!!.setText("")
            newPasswordEditText!!.setText("")
        }
    }

}
