package com.nghiatv.musicplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.media.MediaMetadataRetriever
import com.nghiatv.musicplayer.R
import java.io.ByteArrayInputStream
import java.io.InputStream
import android.support.v4.graphics.drawable.DrawableCompat
import android.os.Build
import android.support.v4.content.ContextCompat

class ImageUtils {
    companion object {
        fun songArt(path: String, context: Context): Bitmap {
            val retriever = MediaMetadataRetriever()
            val inputStream: InputStream
            retriever.setDataSource(path)

            return if (retriever.embeddedPicture != null) {
                inputStream = ByteArrayInputStream(retriever.embeddedPicture)
                val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                retriever.release()
                bitmap
            } else {
                getBitmapFromVectorDrawable(context)
            }
        }

        @SuppressLint("ObsoleteSdkInt")
        fun getBitmapFromVectorDrawable(context: Context): Bitmap {
            var drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_notification_black_24dp)

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = drawable?.let { DrawableCompat.wrap(it).mutate() }
            }

            val bitmap: Bitmap = Bitmap.createBitmap(
                    drawable!!.intrinsicWidth,
                    drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
            )

            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)

            return bitmap
        }
    }
}
