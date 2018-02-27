package com.android.example.github.ui.common

import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.util.Log
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

val TAG = "FileUtil"

@Throws(IOException::class)
fun createFile(activity: Activity): File {
    // Create an image file name
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val imageFileName = "WT" + timeStamp + "_"
    val storageDir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(
            imageFileName, /* prefix */
            ".temp", /* suffix */
            storageDir      /* directory */
    )
}

fun dispatchTakePictureIntent(requestCode: Int, fragment: Fragment, photoFile: File?): File? {
    var file = photoFile
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(fragment.activity!!.packageManager) != null) {
        // Create the File where the photo should go
        try {
            file = createFile(fragment.activity!!)
        } catch (e: IOException) {
            Log.i(TAG, "" + e)
        }

        // Continue only if the File was successfully created
        if (file != null) {
            val photoURI = FileProvider.getUriForFile(fragment.context!!,
                    "com.android.example.github",
                    file)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            fragment.startActivityForResult(takePictureIntent, requestCode)
        }
    }
    return file
}

