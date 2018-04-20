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

/**
 * Callback used for model [AWSLoginModel]. This needs to be implemented when the constructor
 * of [AWSLoginModel] is called.
 */
interface AWSLoginHandler {

    /**
     * Successful completion of the first step of the registration process.
     * This will output mustConfirmToComplete in case there's the need to confirm registration to
     * complete this process.
     *
     * @param mustConfirmToComplete     will be `true` if there's the need to confirm
     * registration, otherwise `false`.
     */
    fun onRegisterSuccess(mustConfirmToComplete: Boolean)

    /**
     * Successful completion of the registration process.
     */
    fun onRegisterConfirmed()

    /**
     * Successful completion of the sign in process.
     */
    fun onSignInSuccess()

    /**
     * Successful completion of the request for confirmation code (when registering).
     *
     * @param medium              what medium the code was sent (e-mail / phone number).
     */
    fun onResendConfirmationCodeSuccess(medium: String)

    /**
     * Successful completion of the request for reset user password.
     *
     * @param medium              what medium the code was sent (e-mail / phone number).
     */
    fun onRequestResetUserPasswordSuccess(medium: String)

    /**
     * Successful completion of the reset of the user password.
     */
    fun onResetUserPasswordSuccess()

    /**
     * Failure of the process called.
     *
     * @param process       what process was called.
     * @param exception     failure details.
     * @param cause         cause of failure.
     * @param message       message of the error.
     */
    fun onFailure(process: Int, exception: Exception, cause: Int, message: String)

}
