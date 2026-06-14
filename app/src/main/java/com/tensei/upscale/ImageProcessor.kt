package com.tensei.upscale

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.InputStream

object ImageProcessor {
    suspend fun enhanceImage(context: Context, uri: Uri, onProgress: (Float) -> Unit): Bitmap? = withContext(Dispatchers.Default) {
        val originalBitmap = loadBitmap(context, uri) ?: return@withContext null
        
        for (i in 1..40) {
            delay(50) 
            withContext(Dispatchers.Main) { onProgress(i / 100f) }
        }
        
        var enhancedBitmap: Bitmap? = null
        var maxPixels = 24_000_000
        var attempts = 0
        
        while (enhancedBitmap == null && attempts < 3) {
            try {
                var newWidth = originalBitmap.width * 2
                var newHeight = originalBitmap.height * 2
                if (newWidth * newHeight > maxPixels) {
                    val scale = Math.sqrt(maxPixels.toDouble() / (newWidth * newHeight))
                    newWidth = (newWidth * scale).toInt()
                    newHeight = (newHeight * scale).toInt()
                }

                for (i in 41..60) {
                    delay(20)
                    withContext(Dispatchers.Main) { onProgress(i / 100f) }
                }

                val finalBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(finalBitmap)
                
                val colorMatrix = ColorMatrix().apply { setSaturation(1.15f) }
                val contrast = 1.15f
                val scaleAmount = contrast
                val translate = (-0.5f * scaleAmount + 0.5f) * 255f
                val contrastMatrix = ColorMatrix(floatArrayOf(
                    scaleAmount, 0f, 0f, 0f, translate,
                    0f, scaleAmount, 0f, 0f, translate,
                    0f, 0f, scaleAmount, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
                colorMatrix.postConcat(contrastMatrix)
                
                val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(colorMatrix)
                }
                
                val srcRect = Rect(0, 0, originalBitmap.width, originalBitmap.height)
                val dstRect = Rect(0, 0, newWidth, newHeight)
                canvas.drawBitmap(originalBitmap, srcRect, dstRect, paint)

                for (i in 61..80) {
                    delay(20)
                    withContext(Dispatchers.Main) { onProgress(i / 100f) }
                }

                enhancedBitmap = finalBitmap

                try {
                    val rs = android.renderscript.RenderScript.create(context)
                    val allocIn = android.renderscript.Allocation.createFromBitmap(rs, enhancedBitmap)
                    val allocOut = android.renderscript.Allocation.createTyped(rs, allocIn.type)
                    val script = android.renderscript.ScriptIntrinsicConvolve3x3.create(rs, android.renderscript.Element.U8_4(rs))
                    
                    val kernel = floatArrayOf(
                         0f, -1f,  0f,
                        -1f,  5f, -1f,
                         0f, -1f,  0f
                    )
                    script.setCoefficients(kernel)
                    script.setInput(allocIn)
                    script.forEach(allocOut)
                    
                    val sharpenedBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888)
                    allocOut.copyTo(sharpenedBitmap)
                    rs.destroy()
                    enhancedBitmap = sharpenedBitmap
                } catch (e: Exception) {
                }
            } catch (e: OutOfMemoryError) {
                maxPixels /= 2
                attempts++
                System.gc()
            }
        }

        for (i in 81..100) {
            delay(20)
            withContext(Dispatchers.Main) { onProgress(i / 100f) }
        }

        enhancedBitmap ?: originalBitmap
    }

    private fun loadBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            return context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "TenSei_HD_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TenSei")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues) ?: return@withContext false
        
        try {
            resolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }
            true
        } catch (e: Exception) {
            resolver.delete(uri, null, null)
            false
        }
    }
}
