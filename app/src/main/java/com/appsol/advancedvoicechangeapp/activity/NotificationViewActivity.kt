package com.appsol.advancedvoicechangeapp.activity

import android.content.*
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import com.appsol.advancedvoicechangeapp.utils.CropTransparent
import com.bumptech.glide.Glide
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewActivity : AppCompatActivity() {
    private var fileName: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_view)
        fileName = currentDateTime.replace(":".toRegex(), "-")
        val imgImage = findViewById<ImageView>(R.id.imgImage)
        val txtTitle = findViewById<TextView>(R.id.txtTitle)
        val txtDec = findViewById<TextView>(R.id.txtDec)
        val title = intent.getStringExtra("title")
        val dec = intent.getStringExtra("dec")
        val link = intent.getStringExtra("link")
        if (title != null) txtTitle.text = title
        if (dec != null) txtDec.text = dec
        if (link != null) Glide.with(this).load(link).into(imgImage)
        findViewById<View>(R.id.imgTitleCopy).setOnClickListener {
            if (title != null) setClipboard(
                this,
                title
            )
        }
        findViewById<View>(R.id.imgDecCopy).setOnClickListener {
            if (dec != null) setClipboard(
                this,
                dec
            )
        }
        val mBaseFolderPath = getExternalFilesDir(null)!!.absolutePath
        val kept = mBaseFolderPath.substring(0, mBaseFolderPath.indexOf("/Android"))
        findViewById<View>(R.id.imgSave).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val filePath =
                    kept + "/" + Environment.DIRECTORY_PICTURES + File.separator + "Voice Changer Photos/" + fileName + ".png"
                if (!File(filePath).isFile) saveImage(loadBitmapFromView(imgImage)) else Toast.makeText(
                    this@NotificationViewActivity,
                    "Photo already saved",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val filePath =
                    getExternalFilesDir(null)!!.absolutePath + File.separator + "Voice Changer Photos/" + fileName + ".png"
                if (!File(filePath).isFile) saveImage(loadBitmapFromView(imgImage))
                Toast.makeText(
                    this@NotificationViewActivity,
                    "Photo already saved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
        findViewById<View>(R.id.imgShare).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val filePath =
                    kept + "/" + Environment.DIRECTORY_PICTURES + File.separator + "Voice Changer Photos/" + fileName + ".png"
                if (!File(filePath).isFile) saveImage(loadBitmapFromView(imgImage))
                mSharePhoto(filePath)
            } else {
                val filePath =
                    getExternalFilesDir(null)!!.absolutePath + File.separator + "Voice Changer Photos/" + fileName + ".png"
                if (!File(filePath).isFile) saveImage(loadBitmapFromView(imgImage))
                mSharePhoto(filePath)
            }
        }
        AdMobInterstitial.getAdmobAds(this, findViewById(R.id.bannerContainer))
    }

    private fun mSharePhoto(filePath: String) {
        val intentShareFile = Intent(Intent.ACTION_SEND)
        val fileWithinMyDir = File(filePath)
        if (fileWithinMyDir.exists()) {
            intentShareFile.type = "image/*"
            val fileUri = FileProvider.getUriForFile(
                this@NotificationViewActivity,
                applicationContext.packageName + ".fileProvider", fileWithinMyDir
            )
            intentShareFile.putExtra(Intent.EXTRA_STREAM, fileUri)
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT, "Sharing Photo...")
            intentShareFile.putExtra(Intent.EXTRA_TEXT, getString(R.string.app_name))
            intentShareFile.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            try {
                startActivity(Intent.createChooser(intentShareFile, "Share Photo..."))
            } catch (ex: ActivityNotFoundException) {
            }
        }
    }

    private fun loadBitmapFromView(v: View): Bitmap {
        val b = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        v.layout(v.left, v.top, v.right, v.bottom)
        v.draw(c)
        return b
    }

    private fun saveImage(bitmap: Bitmap) {
        var bitmap = bitmap
        val ct = CropTransparent()
        bitmap = ct.crop(bitmap)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveImageToGallery(bitmap)
        } else {
            val photoFile = File(
                getExternalFilesDirs(null).toString() + "/Voice Changer Photos/" + fileName + ".png"
            )
            val parentFile = photoFile.parentFile
            if (parentFile?.exists() == false) {
                parentFile.mkdirs()
            }
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, FileOutputStream(photoFile))
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
        Toast.makeText(
            this@NotificationViewActivity,
            "Photo saved successfully",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun saveImageToGallery(bitmap: Bitmap) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.png")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + "Voice Changer Photos"
            )
            //            newFilePath = kept + "/" + Environment.DIRECTORY_PICTURES + File.separator + "PhotoCollage/" + fileName;
            val imgUri =
                contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imgUri != null) {
                try {
                    val fileOutputStream = contentResolver.openOutputStream(imgUri)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
    }

   private fun setClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "text copied", Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    companion object {
        private const val dateTimeFormat = "yyyy-MM-dd HH:mm:ss"
        val currentDateTime: String
            get() = SimpleDateFormat(dateTimeFormat,Locale.US).format(Date())
    }
}