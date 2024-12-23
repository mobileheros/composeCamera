package com.mobileheros.gpscamera.utils

import android.content.ComponentName
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeZone
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.provider.MediaStore.Video
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.navigation.NavController
import com.mobileheros.gpscamera.BuildConfig
import java.io.File
import java.io.IOException
import java.util.Locale


object CommonUtils {
    fun dp2px(ctx: Context, dp: Float): Int {
        val scale: Float = ctx.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    fun canVerticalScroll(editText: EditText): Boolean {
        val scrollY = editText.scrollY
        val scrollRange = editText.layout.height
        val scrollExtent =
            editText.height - editText.compoundPaddingTop - editText.compoundPaddingBottom
        val scrollDifference = scrollRange - scrollExtent
        if (scrollDifference == 0) return false
        return scrollY > 0 || scrollY < scrollDifference - 1
    }


    fun openGooglePlay(context: Context, packageName: String) {
        var marketFound = false
        val ratingIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        val resolveInfoList = context.packageManager.queryIntentActivities(ratingIntent, 0)
        for (resolveInfo in resolveInfoList) {
            if (resolveInfo.activityInfo.applicationInfo.packageName == "com.android.vending") {
                val activityInfo = resolveInfo.activityInfo
                val componentName =
                    ComponentName(activityInfo.applicationInfo.packageName, activityInfo.name)
                ratingIntent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                ratingIntent.component = componentName
                context.startActivity(ratingIntent)
                marketFound = true
                break
            }
        }
        if (!marketFound) {
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            webIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(webIntent)
        }
    }

    fun goSetting(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.parse("package:" + context.packageName)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun isNetworkConnected(context: Context) : Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    fun createWatermarkImage(watermarkImg: Bitmap?, backgroundImg: Bitmap?, offsetX: Int, offsetY: Int) : Bitmap? {
        if (watermarkImg != null && backgroundImg != null) {
            val watermarkPaint = Paint()
            val newBitmap = Bitmap.createBitmap(
                backgroundImg.width,
                backgroundImg.height,
                backgroundImg.config
            )
            val watermarkCanvas = Canvas(newBitmap)
            watermarkCanvas.drawBitmap(backgroundImg, 0.0f, 0.0f, null as Paint?)
            watermarkCanvas.drawBitmap(
                watermarkImg,
                backgroundImg.width - watermarkImg.width - offsetX
                    .toFloat(),
                backgroundImg.height - watermarkImg.height - offsetY
                    .toFloat(),
                watermarkPaint
            )
            return newBitmap
        } else {
            return backgroundImg
        }
    }
    fun getBitmapFromViewUsingCanvas(view: View): Bitmap {
        // Create a new Bitmap object with the desired width and height
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

        // Create a new Canvas object using the Bitmap
        val canvas = Canvas(bitmap)

        // Draw the View into the Canvas
        view.draw(canvas)

        // Return the resulting Bitmap
        return bitmap
    }
    fun getBitmapFromView(view: View): Bitmap {
        view.isDrawingCacheEnabled = true
        // Create a new Bitmap object with the desired width and height
        val bitmap = Bitmap.createBitmap(view.getDrawingCache())
        view.isDrawingCacheEnabled = false

        return bitmap
    }

    fun getFormatLocationString(location: Location) : String {
        val build: StringBuilder = StringBuilder().apply {
            append(Location.convert(location.latitude, Location.FORMAT_DEGREES))
            append("°")
            append(if (location.latitude >= 0) "N" else "S")
            append(", ")
            append(Location.convert(location.longitude, Location.FORMAT_DEGREES))
            append("°")
            append(if (location.longitude >= 0) "E" else "W")
        }
        return build.toString()
    }

    fun NavController.safeNavigate(@IdRes actionId: Int, bundle: Bundle = Bundle()) {
        currentDestination?.getAction(actionId)?.run {
            navigate(this.destinationId, bundle)
        }
    }

    fun formatTime(pattern: String, time: Long): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(time)
    }

    fun formatDuration(pattern: String, time: Long): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.GMT_ZONE
        }.format(time)
    }

    fun getFileFromDocumentUriSAF(ctx: Context, uri: Uri, is_folder: Boolean): File? {
        val authority = uri.authority
        if (BuildConfig.DEBUG) {
            Log.d("storageUtil", "authority: $authority")
            Log.d("storageUtil", "scheme: " + uri.scheme)
            Log.d("storageUtil", "fragment: " + uri.fragment)
            Log.d("storageUtil", "path: " + uri.path)
            Log.d(
                "storageUtil",
                "last path segment: " + uri.lastPathSegment
            )
        }
        var file: File? = null
        if ("com.android.externalstorage.documents" == authority) {
            val id =
                if (is_folder) DocumentsContract.getTreeDocumentId(uri) else DocumentsContract.getDocumentId(
                    uri
                )
            if (BuildConfig.DEBUG) Log.d("storageUtil", "id: $id")
            val split = id.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (split.size >= 1) {
                val type = split[0]
                val path = if (split.size >= 2) split[1] else ""
                /*if( BuildConfig.DEBUG ) {
					Log.d(TAG, "type: " + type);
					Log.d(TAG, "path: " + path);
				}*/
                val storagePoints = File("/storage").listFiles()
                if ("primary".equals(type, ignoreCase = true)) {
                    val externalStorage = Environment.getExternalStorageDirectory()
                    file = File(externalStorage, path)
                }
                var i = 0
                while (storagePoints != null && i < storagePoints.size && file == null) {
                    val externalFile = File(storagePoints[i], path)
                    if (externalFile.exists()) {
                        file = externalFile
                    }
                    i++
                }
                if (file == null) {
                    // just in case?
                    file = File(path)
                }
            }
        } else if ("com.android.providers.downloads.documents" == authority) {
            if (!is_folder) {
                val id = DocumentsContract.getDocumentId(uri)
                if (id.startsWith("raw:")) {
                    // unclear if this is needed for Open Camera, but on Vibrance HDR
                    // on some devices (at least on a Chromebook), I've had reports of id being of the form
                    // "raw:/storage/emulated/0/Download/..."
                    val filename = id.replaceFirst("raw:".toRegex(), "")
                    file = File(filename)
                } else {
                    try {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            id.toLong()
                        )
                        val filename: String? = getDataColumn(ctx, contentUri, null, null)
                        if (filename != null) file = File(filename)
                    } catch (e: NumberFormatException) {
                        // have had crashes from Google Play from Long.parseLong(id)
                        Log.e(
                            "storageUtil",
                            "failed to parse id: $id"
                        )
                        e.printStackTrace()
                    }
                }
            } else {
                if (BuildConfig.DEBUG) Log.d(
                    "storageUtil",
                    "downloads uri not supported for folders"
                )
                // This codepath can be reproduced by enabling SAF and selecting Downloads.
                // DocumentsContract.getDocumentId() throws IllegalArgumentException for
                // this (content://com.android.providers.downloads.documents/tree/downloads).
                // If we use DocumentsContract.getTreeDocumentId() for folders, it returns
                // "downloads" - not clear how to parse this!
            }
        } else if ("com.android.providers.media.documents" == authority) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val type = split[0]
            var contentUri: Uri? = null
            when (type) {
                "image" -> contentUri = Images.Media.EXTERNAL_CONTENT_URI
                "video" -> contentUri = Video.Media.EXTERNAL_CONTENT_URI
                "audio" -> contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            val selection = "_id=?"
            val selectionArgs = arrayOf(
                split[1]
            )
            val filename: String? = getDataColumn(ctx, contentUri!!, selection, selectionArgs)
            if (filename != null) file = File(filename)
        }
        if (BuildConfig.DEBUG) {
            if (file != null) Log.d(
                "storageUtil",
                "file: " + file.absolutePath
            ) else Log.d("storageUtil", "failed to find file")
        }
        return file
    }

    private fun getDataColumn(ctx: Context, uri: Uri, selection: String?, selectionArgs: Array<String>?): String? {
        val column = Images.ImageColumns.DATA
        val projection = arrayOf(
            column
        )
        var cursor: Cursor? = null
        try {
            cursor = ctx.getContentResolver().query(
                uri, projection, selection, selectionArgs,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: SecurityException) {
            // have received crashes from Google Play for this
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return null
    }

    @Throws(IOException::class)
    fun createOutputFileSAF(ctx: Context, dir: String, filename: String?, mimeType: String?): Uri {
        return try {
            val treeUri: Uri = Uri.parse("")
            if (BuildConfig.DEBUG) Log.d(
                "storageUtil",
                "treeUri: $treeUri"
            )
            val docUri = DocumentsContract.buildDocumentUriUsingTree(
                treeUri,
                DocumentsContract.getTreeDocumentId(treeUri)
            )
            if (BuildConfig.DEBUG) Log.d(
                "storageUtil",
                "docUri: $docUri"
            )
            // note that DocumentsContract.createDocument will automatically append to the filename if it already exists
            val fileUri = DocumentsContract.createDocument(
                ctx.contentResolver, docUri,
                mimeType!!,
                filename!!
            )
            if (BuildConfig.DEBUG) Log.d(
                "storageUtil",
                "returned fileUri: $fileUri"
            )
            /*if( true )
                        throw new SecurityException(); // test*/
            if (fileUri == null) throw IOException()
            fileUri
        } catch (e: java.lang.IllegalArgumentException) {
            // DocumentsContract.getTreeDocumentId throws this if URI is invalid
            if (BuildConfig.DEBUG) Log.e(
                "storageUtil",
                "createOutputMediaFileSAF failed with IllegalArgumentException"
            )
            e.printStackTrace()
            throw IOException()
        } catch (e: IllegalStateException) {
            // Have reports of this from Google Play for DocumentsContract.createDocument - better to fail gracefully and tell user rather than crash!
            if (BuildConfig.DEBUG) Log.e(
                "storageUtil",
                "createOutputMediaFileSAF failed with IllegalStateException"
            )
            e.printStackTrace()
            throw IOException()
        } catch (e: NullPointerException) {
            // Have reports of this from Google Play for DocumentsContract.createDocument - better to fail gracefully and tell user rather than crash!
            if (BuildConfig.DEBUG) Log.e(
                "storageUtil",
                "createOutputMediaFileSAF failed with NullPointerException"
            )
            e.printStackTrace()
            throw IOException()
        } catch (e: SecurityException) {
            // Have reports of this from Google Play - better to fail gracefully and tell user rather than crash!
            if (BuildConfig.DEBUG) Log.e(
                "storageUtil",
                "createOutputMediaFileSAF failed with SecurityException"
            )
            e.printStackTrace()
            throw IOException()
        }
    }


}