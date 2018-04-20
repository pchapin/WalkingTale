/*
 * Author: William Takashi Mimura
 * Date: November 23, 2017
 * Website: www.wtmimura.com
 * Repository: https://github.com/mimurawil/android-aws-mobilehub-login-model
 *
 * You can copy, change, or whatever. Just use some common sense and good faith.
 * If you use this, please leave a message at wotom.wtmimura@gmail.com. That'd be awesome!
 */

package com.talkingwhale.activities

import android.annotation.SuppressLint
import android.content.Context
import com.amazonaws.mobile.auth.core.IdentityManager
import com.amazonaws.mobileconnectors.cognitoidentityprovider.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.*
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.*
import com.amazonaws.regions.Regions
import com.amazonaws.services.cognitoidentityprovider.model.*
import org.json.JSONException

/**
 * This represents a model for login operations on AWS Mobile Hub. It manages login operations
 * such as:
 * - Sign In
 * - Sign Up
 * - Confirm Sign Up
 * - Resend Confirmation Code
 * - Recover Password
 * - Sign Out
 * - Delete Account
 * - Get User Name (current signed in)
 * - Get User E-mail (current signed in)
 *
 */
class AWSLoginModel
/**
 * Constructs the model for login functions in AWS Mobile Hub.
 *
 * @param mContext         REQUIRED: Android application context.
 * @param mCallback        REQUIRED: Callback handler for login operations.
 */
(private val mContext: Context, // interface handler
 private val mCallback: AWSLoginHandler) {

    // constants
    // AWS attributes
    private val ATTR_EMAIL = "email"
    private val ATTR_USERNAME = "preferred_username"

    // control variables
    private var userName: String? = null
    private var userPassword: String? = null
    private var currentProcessInResetPassword: Int = 0
    private var mCognitoUserPool: CognitoUserPool? = null
    private var mCognitoUser: CognitoUser? = null

    // Handler of the signInUser method
    private val authenticationHandler = object : AuthenticationHandler {
        override fun onSuccess(userSession: CognitoUserSession, newDevice: CognitoDevice?) {
            // Get details of the logged user (in this case, only the e-mail)
            mCognitoUser = mCognitoUserPool!!.currentUser
            mCognitoUser!!.getDetailsInBackground(object : GetDetailsHandler {
                @SuppressLint("ApplySharedPref")
                override fun onSuccess(cognitoUserDetails: CognitoUserDetails) {
                    // Save in SharedPreferences
                    val editor = mContext.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE).edit()
                    val email = cognitoUserDetails.attributes.attributes[ATTR_EMAIL]
                    val userName = cognitoUserDetails.attributes.attributes[ATTR_USERNAME]
                    editor.putString(PREFERENCE_USER_EMAIL, email)
                    editor.putString(PREFERENCE_USER_NAME, userName)
                    editor.commit()
                    mCallback.onSignInSuccess()
                }

                override fun onFailure(exception: Exception) {
                    mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
                }
            })

        }

        override fun getAuthenticationDetails(authenticationContinuation: AuthenticationContinuation, userId: String) {
            val authenticationDetails = AuthenticationDetails(userName, userPassword, null)
            authenticationContinuation.setAuthenticationDetails(authenticationDetails)
            authenticationContinuation.continueTask()
            userPassword = ""
        }

        override fun getMFACode(continuation: MultiFactorAuthenticationContinuation) {
            // Not implemented for this Model
        }

        override fun authenticationChallenge(continuation: ChallengeContinuation) {
            // Not implemented for this Model
        }

        override fun onFailure(exception: Exception) {
            userPassword = ""
            if (exception is UserNotConfirmedException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_MUST_CONFIRM_FIRST, "User not confirmed.")
            } else if (exception is UserNotFoundException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_USER_NOT_FOUND, MESSAGE_USER_NOT_FOUND)
            } else if (exception is NotAuthorizedException) {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_INCORRECT_PASSWORD, "Incorrect username or password.")
            } else {
                mCallback.onFailure(PROCESS_SIGN_IN, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
            }
        }
    }

    // Handler for
    private val forgotPasswordHandler = object : ForgotPasswordHandler {
        override fun onSuccess() {
            mCallback.onResetUserPasswordSuccess()
        }

        override fun getResetCode(continuation: ForgotPasswordContinuation) {
            forgotPasswordContinuation = continuation
            mCallback.onRequestResetUserPasswordSuccess(continuation.parameters.deliveryMedium)
        }

        override fun onFailure(exception: Exception) {
            if (exception is LimitExceededException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_LIMIT_EXCEEDED, "Limit exceeded. Wait to try again")
            } else if (exception is UserNotFoundException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_USER_NOT_FOUND, MESSAGE_USER_NOT_FOUND)
            } else if (exception is InvalidParameterException) {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_INVALID_PARAMETERS, "User not confirmed. Cannot send e-mail.")
            } else {
                mCallback.onFailure(currentProcessInResetPassword, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
            }
        }
    }

    private var forgotPasswordContinuation: ForgotPasswordContinuation? = null

    init {
        val identityManager = IdentityManager.getDefaultIdentityManager()
        try {
            val myJSON = identityManager.configuration.optJsonObject("CognitoUserPool")
            val COGNITO_POOL_ID = myJSON.getString("PoolId")
            val COGNITO_CLIENT_ID = myJSON.getString("AppClientId")
            val COGNITO_CLIENT_SECRET = myJSON.getString("AppClientSecret")
            val REGION = myJSON.getString("Region")
            mCognitoUserPool = CognitoUserPool(mContext, COGNITO_POOL_ID, COGNITO_CLIENT_ID, COGNITO_CLIENT_SECRET, Regions.fromName(REGION))
        } catch (e: JSONException) {
            e.printStackTrace()
        }

    }

    /**
     * Registers new user to the AWS Cognito User Pool.
     *
     * This will trigger [AWSLoginHandler] interface defined when the constructor was called.
     *
     * @param userName         REQUIRED: Username to be registered. Must be unique in the User Pool.
     * @param userEmail        REQUIRED: E-mail to be registered. Must be unique in the User Pool.
     * @param userPassword     REQUIRED: Password of this new account.
     */
    fun registerUser(userName: String, userEmail: String, userPassword: String) {
        val userAttributes = CognitoUserAttributes()
        userAttributes.addAttribute(ATTR_EMAIL, userEmail)
        userAttributes.addAttribute(ATTR_USERNAME, userName)

        val signUpHandler = object : SignUpHandler {
            override fun onSuccess(user: CognitoUser, signUpConfirmationState: Boolean, cognitoUserCodeDeliveryDetails: CognitoUserCodeDeliveryDetails) {
                mCognitoUser = user
                mCallback.onRegisterSuccess(!signUpConfirmationState)
            }

            override fun onFailure(exception: Exception) {
                if (exception is UsernameExistsException) {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_USER_ALREADY_EXISTS, "Username or e-mail already exists.")
                } else if (exception is InvalidParameterException) {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_INVALID_PARAMETERS, "Invalid parameters.")
                } else {
                    mCallback.onFailure(PROCESS_REGISTER, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
                }
            }
        }

        mCognitoUserPool!!.signUpInBackground(userName, userPassword, userAttributes, null, signUpHandler)
    }

    /**
     * Confirms registration of the new user in AWS Cognito User Pool.
     *
     * This will trigger [AWSLoginHandler] interface defined when the constructor was called.
     *
     * @param confirmationCode      REQUIRED: Code sent from AWS to the user.
     */
    fun confirmRegistration(confirmationCode: String) {
        val confirmationHandler = object : GenericHandler {
            override fun onSuccess() {
                mCallback.onRegisterConfirmed()
            }

            override fun onFailure(exception: Exception) {
                mCallback.onFailure(PROCESS_CONFIRM_REGISTRATION, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
            }
        }

        mCognitoUser!!.confirmSignUpInBackground(confirmationCode, false, confirmationHandler)
    }

    /**
     * Sign in process. If succeeded, this will save the user name and e-mail in SharedPreference of
     * this context.
     *
     * This will trigger [AWSLoginHandler] interface defined when the constructor was called.
     *
     * @param userName               REQUIRED: Username.
     * @param userPassword           REQUIRED: Password.
     */
    fun signInUser(userName: String, userPassword: String) {
        this.userName = userName
        this.userPassword = userPassword

        mCognitoUser = mCognitoUserPool!!.getUser(userName)
        mCognitoUser!!.getSessionInBackground(authenticationHandler)
    }

    /**
     * Re-sends the confirmation code from the current user
     */
    fun resendConfirmationCode() {
        mCognitoUser!!.resendConfirmationCodeInBackground(object : VerificationHandler {
            override fun onSuccess(verificationCodeDeliveryMedium: CognitoUserCodeDeliveryDetails) {
                mCallback.onResendConfirmationCodeSuccess(verificationCodeDeliveryMedium.deliveryMedium)
            }

            override fun onFailure(exception: Exception) {
                mCallback.onFailure(PROCESS_RESEND_CONFIRMATION_CODE, exception, CAUSE_UNKNOWN, MESSAGE_UNKNOWN_ERROR)
            }
        })
    }

    /**
     * Requests the reset of the user's password (in case of forgotten password).
     * This method sends the reset code to the user.
     *
     * @param userName          REQUIRED: Username.
     */
    fun requestResetUserPassword(userName: String) {
        currentProcessInResetPassword = PROCESS_REQUEST_RESET_PASSWORD
        mCognitoUser = mCognitoUserPool!!.getUser(userName)
        mCognitoUser!!.forgotPasswordInBackground(forgotPasswordHandler)
    }

    /**
     * Resets current user password if the resetCode matches with the one sent to the user (when
     * requestResetUserPassword was called).
     *
     * @param resetCode         REQUIRED: should be same code received when request was called.
     * @param newPassword       REQUIRED: new password.
     */
    fun resetUserPasswordWithCode(resetCode: String, newPassword: String) {
        currentProcessInResetPassword = PROCESS_RESET_PASSWORD
        forgotPasswordContinuation!!.setVerificationCode(resetCode)
        forgotPasswordContinuation!!.setPassword(newPassword)
        forgotPasswordContinuation!!.continueTask()
    }

    companion object {
        // saved values on shared preferences
        private val SHARED_PREFERENCE = "SavedValues"
        private val PREFERENCE_USER_NAME = "awsUserName"
        private val PREFERENCE_USER_EMAIL = "awsUserEmail"
        // message errors used in more than one place
        private val MESSAGE_UNKNOWN_ERROR = "Unknown error. Check internet connection."
        private val MESSAGE_USER_NOT_FOUND = "User does not exist."
        // current process requested
        val PROCESS_SIGN_IN = 1
        val PROCESS_REGISTER = 2
        val PROCESS_CONFIRM_REGISTRATION = 3
        val PROCESS_RESEND_CONFIRMATION_CODE = 4
        val PROCESS_REQUEST_RESET_PASSWORD = 5
        val PROCESS_RESET_PASSWORD = 6
        // error causes
        val CAUSE_MUST_CONFIRM_FIRST = 1
        val CAUSE_USER_NOT_FOUND = 2
        val CAUSE_INCORRECT_PASSWORD = 3
        val CAUSE_LIMIT_EXCEEDED = 4
        val CAUSE_USER_ALREADY_EXISTS = 5
        val CAUSE_INVALID_PARAMETERS = 6
        val CAUSE_UNKNOWN = 999

        /**
         * Signs out from current session.
         */
        fun doUserLogout() {
            val identityManager = IdentityManager.getDefaultIdentityManager()
            identityManager.signOut()
        }

        /**
         * Gets the user name saved in SharedPreferences.
         *
         * @param context               REQUIRED: Android application context.
         * @return                      user name saved in SharedPreferences.
         */
        fun getSavedUserName(context: Context): String {
            val savedValues = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
            return savedValues.getString(PREFERENCE_USER_NAME, "")
        }

        /**
         * Gets the user e-mail saved in SharedPreferences.
         *
         * @param context               REQUIRED: Android application context.
         * @return                      user e-mail saved in SharedPreferences.
         */
        fun getSavedUserEmail(context: Context): String {
            val savedValues = context.getSharedPreferences(SHARED_PREFERENCE, Context.MODE_PRIVATE)
            return savedValues.getString(PREFERENCE_USER_EMAIL, "")
        }
    }

}
