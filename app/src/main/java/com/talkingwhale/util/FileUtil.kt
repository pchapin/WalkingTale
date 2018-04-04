package com.talkingwhale.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Environment
import android.provider.MediaStore
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

fun dispatchTakePictureIntent(requestCode: Int, activity: Activity, photoFile: File?): File? {
    var file = photoFile
    val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    // Ensure that there's a camera activity to handle the intent
    if (takePictureIntent.resolveActivity(activity.packageManager) != null) {
        // Create the File where the photo should go
        try {
            file = createFile(activity)
        } catch (e: IOException) {
            Log.i(TAG, "" + e)
        }

        // Continue only if the File was successfully created
        if (file != null) {
            val photoURI = FileProvider.getUriForFile(activity.applicationContext,
                    "com.talkingwhale",
                    file)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            activity.startActivityForResult(takePictureIntent, requestCode)
        }
    }
    return file
}

fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap {
    val drawable = context!!.getDrawable(drawableId)

    val bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
