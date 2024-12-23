package com.mobileheros.gpscamera.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date

private const val TAG = "VideoExt"

private val ALBUM_DIR = Environment.DIRECTORY_PICTURES
object VideoSaveUtils {
    /**
     * 复制图片文件到相册的Pictures文件夹
     *
     * @param context 上下文
     * @param fileName 文件名。 需要携带后缀
     * @param relativePath 相对于Pictures的路径
     */
    fun File.copyToAlbum(context: Context, fileName: String, relativePath: String?): Uri? {
        if (!this.canRead() || !this.exists()) {
            return null
        }
        return this.inputStream().use {
            it.saveToAlbum(context, fileName, relativePath)
        }
    }

    /**
     * 保存图片Stream到相册的Pictures文件夹
     *
     * @param context 上下文
     * @param fileName 文件名。 需要携带后缀
     * @param relativePath 相对于Pictures的路径
     */
    fun InputStream.saveToAlbum(context: Context, fileName: String, relativePath: String?): Uri? {
        val resolver = context.contentResolver
        val outputFile = com.mobileheros.gpscamera.utils.OutputFileTaker()
        val imageUri = resolver.insertMediaVideo(fileName, relativePath, outputFile)
        if (imageUri == null) {
            return null
        }

        (imageUri.outputStream(resolver) ?: return null).use { output ->
            this.use { input ->
                input.copyTo(output)
                imageUri.finishPending(context, resolver, outputFile.file)
            }
        }
        return imageUri
    }

    private fun Uri.outputStream(resolver: ContentResolver): OutputStream? {
        return try {
            resolver.openOutputStream(this)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    private fun Uri.finishPending(
        context: Context,
        resolver: ContentResolver,
        outputFile: File?,
    ) {
        val imageValues = ContentValues()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (outputFile != null) {
                imageValues.put(MediaStore.Video.Media.SIZE, outputFile.length())
            }
            resolver.update(this, imageValues, null, null)
            // 通知媒体库更新
            val intent =
                Intent(@Suppress("DEPRECATION") (Intent.ACTION_MEDIA_SCANNER_SCAN_FILE), this)
            context.sendBroadcast(intent)
        } else {
            // Android Q添加了IS_PENDING状态，为0时其他应用才可见
            imageValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(this, imageValues, null, null)
        }
    }


    private fun String.getMimeType(): String? {
        val fileName = this.lowercase()
        return when {
            fileName.endsWith(".mp4") -> "video/mp4"
            else -> null
        }
    }

    /**
     * 插入图片到媒体库
     */
    private fun ContentResolver.insertMediaVideo(
        fileName: String,
        relativePath: String?,
        outputFileTaker: com.mobileheros.gpscamera.utils.OutputFileTaker? = null,
    ): Uri? {
        // 图片信息
        val imageValues = ContentValues().apply {
            val mimeType = fileName.getMimeType()
            if (mimeType != null) {
                put(MediaStore.Video.Media.MIME_TYPE, mimeType)
            }
            val date = System.currentTimeMillis() / 1000
            put(MediaStore.Video.Media.DATE_ADDED, date)
            put(MediaStore.Video.Media.DATE_MODIFIED, date)
        }
        // 保存的位置
        val collection: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val path = if (relativePath != null) "${com.mobileheros.gpscamera.utils.ALBUM_DIR}/${relativePath}" else com.mobileheros.gpscamera.utils.ALBUM_DIR
            imageValues.apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Video.Media.RELATIVE_PATH, path)
                put(MediaStore.Video.Media.IS_PENDING, 1)
            }
            collection = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            // 高版本不用查重直接插入，会自动重命名
        } else {
            // 老版本
            val pictures =
                @Suppress("DEPRECATION") (Environment.getExternalStoragePublicDirectory(ALBUM_DIR))
            val saveDir = if (relativePath != null) File(pictures, relativePath) else pictures

            if (!saveDir.exists() && !saveDir.mkdirs()) {
                Log.e(
                    com.mobileheros.gpscamera.utils.TAG,
                    "save: error: can't create Pictures directory"
                )
                return null
            }

            // 文件路径查重，重复的话在文件名后拼接数字
            var imageFile = File(saveDir, fileName)
            val fileNameWithoutExtension = imageFile.nameWithoutExtension
            val fileExtension = imageFile.extension

            var queryUri = this.queryMediaImage28(imageFile.absolutePath)
            var suffix = 1
            while (queryUri != null) {
                val newName = fileNameWithoutExtension + "(${suffix++})." + fileExtension
                imageFile = File(saveDir, newName)
                queryUri = this.queryMediaImage28(imageFile.absolutePath)
            }

            imageValues.apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, imageFile.name)
                // 保存路径
                val imagePath = imageFile.absolutePath
                Log.v(com.mobileheros.gpscamera.utils.TAG, "save file: $imagePath")
                put(@Suppress("DEPRECATION") MediaStore.Video.Media.DATA, imagePath)
            }
            outputFileTaker?.file = imageFile// 回传文件路径，用于设置文件大小
            collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }
        // 插入图片信息
        return this.insert(collection, imageValues)
    }

    /**
     * Android Q以下版本，查询媒体库中当前路径是否存在
     * @return Uri 返回null时说明不存在，可以进行图片插入逻辑
     */
    private fun ContentResolver.queryMediaImage28(imagePath: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return null

        val imageFile = File(imagePath)
        if (imageFile.canRead() && imageFile.exists()) {
            Log.v(com.mobileheros.gpscamera.utils.TAG, "query: path: $imagePath exists")
            // 文件已存在，返回一个file://xxx的uri
            return Uri.fromFile(imageFile)
        }
        // 保存的位置
        val collection = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

        // 查询是否已经存在相同图片
        val query = this.query(
            collection,
            arrayOf(
                MediaStore.Video.Media._ID,
                @Suppress("DEPRECATION") MediaStore.Video.Media.DATA
            ),
            "${@Suppress("DEPRECATION") MediaStore.Video.Media.DATA} == ?",
            arrayOf(imagePath), null
        )
        query?.use {
            while (it.moveToNext()) {
                val idColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val id = it.getLong(idColumn)
                val existsUri = ContentUris.withAppendedId(collection, id)
                Log.v(
                    com.mobileheros.gpscamera.utils.TAG,
                    "query: path: $imagePath exists uri: $existsUri"
                )
                return existsUri
            }
        }
        return null
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context, name: String = ""): File {
        // Create an image file name

        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        var fileName:String = ""
        if (name.isEmpty()) {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            fileName =  "JPEG_${timeStamp}.jpg"
        }

        val file = File(storageDir, name.ifEmpty { fileName })
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        return file
//        return File.createTempFile(
//            "JPEG_${timeStamp}_", /* prefix */
//            ".jpg", /* suffix */
//            storageDir /* directory */
//        )
    }


}