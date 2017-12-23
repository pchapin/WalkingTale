package com.android.example.github.ui.common

import android.app.Activity
import android.os.Environment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

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