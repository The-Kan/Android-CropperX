package com.devyd.androidcropper.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileInputStream

object FileUtil {
    const val FILE_NAME = "FileUtil"


    fun saveFileToGallery(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveFileToGalleryAboveQ(context, file, onSuccess, onFail)
        } else {
            saveFileToGalleryBelowQ(context, file, onSuccess, onFail)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileToGalleryAboveQ(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val methodName = "saveFileToGalleryAboveQ"

        val fileName = System.currentTimeMillis().toString()
        val mimeType = getMimeTypeFromFile(file)
        val relativePath = getRelativePathFromMimeType(mimeType)

        val contentUri: Uri = when {
            mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName + "." + file.extension)
            put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
            put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
        }

        // contentUri, ContentValues를 사용해서 MediaStore에 새로운 파일을 "등록"
        // 해당 경로에 빈 파일을 만들고 uri로 반환
        val uri = context.contentResolver.insert(contentUri, contentValues)

        if (uri != null) {
            copyFileToUri(context.contentResolver, file, uri)
            onSuccess()
            return
        } else {
            LogUtil.e(FILE_NAME, methodName, "copy file to uri error")
            onFail()
            return
        }
    }

    private fun saveFileToGalleryBelowQ(
        context: Context,
        file: File,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val methodName = "saveFileToGalleryBelowQ"

        val fileName = System.currentTimeMillis().toString()
        val mimeType = getMimeTypeFromFile(file)
        val relativePath = getRelativePathFromMimeType(mimeType)
        val externalStorageFilePath =
            File(getExternalStorageRoot(), "$relativePath/$fileName" + "." + file.extension)

        externalStorageFilePath.parentFile?.mkdirs()

        try {
            copyFile(file, externalStorageFilePath)
        } catch (e: Exception) {
            LogUtil.e(FILE_NAME, methodName, "${e.message}")
            onFail()
        }
    }

    private fun copyFileToUri(contentResolver: ContentResolver, file: File, uri: Uri) {
        FileInputStream(file).use { inputStream ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun copyFile(source: File, dest: File) {
        FileInputStream(source).use { inputStream ->
            dest.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    private fun getExternalStorageRoot(): File {
        return Environment.getExternalStorageDirectory()
    }

    private fun getRelativePathFromMimeType(mimeType: String): String {
        return when {
            mimeType.startsWith("image/") -> Environment.DIRECTORY_PICTURES + "/CropperX"
            else -> Environment.DIRECTORY_DOCUMENTS + "/CropperX"
        }

    }

    private fun getMimeTypeFromFile(file: File): String {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg", "png" -> "image/${file.extension.lowercase()}"
            else -> "application/octet-stream"
        }

    }
}