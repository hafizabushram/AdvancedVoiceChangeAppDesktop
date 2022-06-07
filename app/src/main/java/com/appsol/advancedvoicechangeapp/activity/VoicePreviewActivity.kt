package com.appsol.advancedvoicechangeapp.activity

import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.airbnb.lottie.LottieAnimationView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.databinding.ActivityVoicePreviewBinding
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import com.appsol.advancedvoicechangeapp.utils.BackgroundTask
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs
import net.surina.soundtouch.OnProgressChangedListener
import net.surina.soundtouch.SoundStreamAudioPlayer
import net.surina.soundtouch.SoundStreamFileWriter
import java.io.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

class VoicePreviewActivity : AppCompatActivity(), MediaPlayer.OnPreparedListener,
    VoiceChangerDialogs.SelectionInterFace {

//    private var spEditor: SharedPreferences.Editor? = null

    private var purchaseDialog: Dialog? = null
    private var timeWhenStopped: Long = 0


    private var mediaPlayerBoolean = false
//    private var soundPlayerBoolean = false

    var sp: SharedPreferences? = null

    private var optionsDialog: Dialog? = null
    private var progressDialog: ProgressDialog? = null

    var explosion = 0


    private var path: File? = null
//    private var pathForSaving: File? = null

    private lateinit var voicePreViewBinding: ActivityVoicePreviewBinding

    //    private var fileeshare: File? = null
    private var txtFileName: TextView? = null

    //    private var totalTIme: Long = 0
    private var pos = 0

    var player: SoundStreamAudioPlayer? = null


    private var kept: String? = null
    private var exitDialog: Dialog? = null
    private var mediaPlayer: MediaPlayer? = null
    private var prepared = false
    private var comingFromCatalog = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voicePreViewBinding = DataBindingUtil.setContentView(this, R.layout.activity_voice_preview)


        sp = getSharedPreferences(MainActivity.MY_PREFS, MODE_PRIVATE)

//        val path = File(externalCacheDir?.absolutePath + "/audioFile.wav")
//
//        mediaPlayer = MediaPlayer.create(applicationContext, Uri.fromFile(path))

//        voicePreViewBinding.chronometer.base = SystemClock.elapsedRealtime() + mediaPlayer!!.duration

        comingFromCatalog = intent.getBooleanExtra("comingFromVoice", false)

        if (comingFromCatalog) {
            voicePreViewBinding.applyEffectAnimation.also {
//                it.imageAssetsFolder = "images5"
                it.setAnimation(R.raw.trymore)
                it.setOnClickListener {
                    optionDialog
                }

            }
        } else {
            voicePreViewBinding.applyEffectAnimation.also {
                it.setAnimation(R.raw.applyeffect)
                it.setOnClickListener {

                    startActivity(
                        Intent(
                            this@VoicePreviewActivity,
                            AudioEffectsCustomActivity::class.java
                        ).putExtra(
                            "file",
                            path.toString()
                        )
                    )
                }
            }

        }



        init()


        AdMobInterstitial.getAdmobAds(this, findViewById(R.id.bannerContainer))



        setClickListeners()

    }

    private fun init() {


        val mBaseFolderPath = getExternalFilesDir(null)!!.absolutePath
        kept = mBaseFolderPath.substring(0, mBaseFolderPath.indexOf("/Android"))

//        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)

//        file = sp!!.getString("filename", "")

//        totalTIme = sp!!.getLong("file_time", 0)

//        voicePreViewBinding.chronometer.base = SystemClock.elapsedRealtime() + totalTIme
//        if (!path.exists()) path.mkdirs()

//        mediaPlayer= MediaPlayer.create(this, Uri.parse(path.path))

//        val mTempFIle = File("$filesDir/AdvancedVoiceChangerTemp/OrignalVoiceTemp.wav")
//        if (!mTempFIle.exists()) mTempFIle.delete()
//        savingTempFile(12000)


        path = File(externalCacheDir?.absolutePath + "/audioFile.wav")
//        pathForSaving = File(externalCacheDir?.absolutePath!!)


        voicePreViewBinding.appBarMain.also { layout ->

            layout.findViewById<RelativeLayout>(R.id.RL_appbar).setBackgroundColor(
                ContextCompat.getColor(this, R.color.colorPurple)
            )

            layout.findViewById<ImageButton>(R.id.menuButton).also {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    onBackPressed()
                }
                it.setImageResource(R.drawable.ic_back)
            }

            layout.findViewById<LottieAnimationView>(R.id.saveAnimation).also {
                it.visibility = View.VISIBLE
                it.playAnimation()
                it.setOnClickListener {

                    saveAudio()
//                    Toast.makeText(makeTextthis, "savedddd", Toast.LENGTH_SHORT).show()
                }
            }
            layout.findViewById<TextView>(
                R.id.toolbar_title
            ).also { textView ->

                if (comingFromCatalog) {
                    textView.text = sp!!.getString("EffectName", "") + " Effect"
                } else {
                    textView.text = resources.getString(R.string.recording)
                }

                textView.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }
    }


    var soundWriter: SoundStreamFileWriter? = null

    private fun SavingFile(filename: String) {
        loadProgressDialog()
//        val file_nm: String? = null
        val file = Uri.fromFile(path)
        var fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString())
        if (fileExt == "pcm") fileExt = "wav"
//        val fileExt = "wav"

        val f2: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val path = File("$filesDir/AdvancedVoiceChanger/")
            if (!path.exists()) path.mkdirs()
            File(path, "$filename.$fileExt")
        } else {
            val path = File("$kept/AdvancedVoiceChanger/")
            if (!path.exists()) path.mkdir()
            File(path, "$filename.$fileExt")
        }
        try {


//            player = SoundStreamAudioPlayer(0, path?.toString(), 0.01f * tempo, pitchh)

            soundWriter = SoundStreamFileWriter(
                0, path.toString(),
                f2.path,
                sp?.getInt("CatalogTempo", 0)!! * 0.01f,
                sp?.getFloat("CatalogPitch", 0.0F)!!,
            )

            Thread(soundWriter).start()
            if (player != null) {
                player!!.stop()
                player!!.setOnProgressChangedListener(null)
            }
            soundWriter!!.setOnProgressChangedListener(object : OnProgressChangedListener {
                override fun onProgressChanged(
                    track: Int,
                    currentPercentage: Double,
                    position: Long
                ) {
                    if (progressDialog != null) progressDialog!!.progress =
                        (currentPercentage * 100).toInt()
                }

                override fun onTrackEnd(track: Int) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            BackgroundTask.execute(object : BackgroundTask.Task("", 1000L, "") {
                                override fun execute() {
                                    if (f2.length() > 100) {
                                        if (moveFile(
                                                f2.path,
                                                filename,
                                                fileExt
                                            ) != ""
                                        ) gotoNextScreen()
                                    } else {

                                        f2.delete()
                                        runOnUiThread { oppsDialog }
                                    }
                                }
                            })
                        } else {
                            Handler().postDelayed({
                                if (f2.length() > 100) {
                                    gotoNextScreen()
                                } else {
                                    f2.delete()
                                    oppsDialog
                                }
                            }, 1000)
                        }
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                        Log.d("TAG_", "SavingFile: ")
                    }
                }

                override fun onExceptionThrown(string: String) {}
            })
            soundWriter!!.start()
        } catch (e: IOException) {
            Log.d("hbssscode", "SavingFile: " + e)
            e.printStackTrace()
        }
    }


    private fun loadProgressDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog!!.also {

            it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            it.setTitle("Loading...")
            it.setMessage("Preparing your file, please wait...")
            it.setCancelable(false)
            it.setButton("Cancel") { _, _ ->
                if (soundWriter != null) soundWriter!!.stop()
                it.dismiss()
            }
            it.isIndeterminate = false
            it.max = 100
            it.progress = 0
            it.show()
        }
    }


    private val oppsDialog: Unit
        get() {
            if (progressDialog != null) progressDialog!!.dismiss()
            val dialog = VoiceChangerDialogs.createOppsDialog(this) { backCalled() }
            dialog.show()
        }

    private fun gotoNextScreen() {
        if (progressDialog != null) progressDialog!!.dismiss()

        savedDialog

        object : CountDownTimer(6000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
            }

            override fun onFinish() {
                startActivity(
                    Intent(
                        this@VoicePreviewActivity,
                        MyAudioFileActivity::class.java
                    ).putExtra("isSaved", true)
                )
            }
        }.start()

        finish()
        AdMobInterstitial.getDefaultInstance(this)
            .showInterstitialAd(this)
    }

    private fun setClickListeners() {

//        findViewById<ImageButton>(R.id.btn_applyEffects).setOnClickListener {
//            startActivity(
//                Intent(
//                    this@VoicePreviewActivity,
//                    RecordedEffectsCustomActivity::class.java
//                )
//            )
//        }
//        findViewById<ImageButton>(R.id.btn_applySave).setOnClickListener {
//            saveAudio()
//        }

        findViewById<ImageButton>(R.id.btn_applyReset).setOnClickListener {
            startActivity(
                Intent(this@VoicePreviewActivity, RecordingActivity::class.java)
            )
            finish()
        }


        voicePreViewBinding.imgPlay.setOnClickListener {

            if (comingFromCatalog) {

                voicePreViewBinding.appBarMain.findViewById<TextView>(R.id.toolbar_title)
                    .text = sp!!.getString("EffectName", "") + " Effect"

                if (player != null && !player!!.isPaused) {
                    player!!.pause()
                    voicePreViewBinding.ivSoundStop.visibility = View.VISIBLE
                    voicePreViewBinding.waveformAnimation.visibility = View.GONE
                    voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)
                    timeWhenStopped =
                        SystemClock.elapsedRealtime() - voicePreViewBinding.chronometer.base
                    voicePreViewBinding.chronometer.stop()

                } else if (player != null && player!!.isPaused) {
                    voicePreViewBinding.ivSoundStop.visibility = View.GONE
                    voicePreViewBinding.waveformAnimation.also {
                        it.visibility = View.VISIBLE
                        it.setAnimation(R.raw.ic_voice_beats)
                        it.playAnimation()
                    }

//                    if (soundPlayerBoolean) {
//                        voicePreViewBinding.chronometer.base =
//                            SystemClock.elapsedRealtime() + (player!!.duration.toInt() / 1000)
//                        soundPlayerBoolean = false
//                        if (player != null) {
//                            player!!.start()
//                        }
//
//                    } else {
                    voicePreViewBinding.chronometer.base =
                        SystemClock.elapsedRealtime() - timeWhenStopped
                    if (player != null) {
                        player!!.start()
                    }
//                    }

                    voicePreViewBinding.chronometer.start()
                    voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_pause)


                } else {
                    voicePreViewBinding.ivSoundStop.visibility = View.GONE

                    voicePreViewBinding.waveformAnimation.also {

                        it.visibility = View.VISIBLE
                        object : CountDownTimer(4000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                it.setAnimation(R.raw.ic_voice_beats)
                                it.playAnimation()
                            }
                        }.start()
                    }

                    playSound(
                        sp?.getFloat("CatalogPitch", 0.0F)!!,
                        sp?.getInt("CatalogTempo", 0)!!.toFloat()
                    )
                }


            } else {

                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                    voicePreViewBinding.ivSoundStop.visibility = View.VISIBLE
                    voicePreViewBinding.waveformAnimation.visibility = View.GONE
                    voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)
                    timeWhenStopped =
                        SystemClock.elapsedRealtime() - voicePreViewBinding.chronometer.base
                    voicePreViewBinding.chronometer.stop()

                } else if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {

                    mediaPlayer!!.start()
                    voicePreViewBinding.ivSoundStop.visibility = View.GONE
                    voicePreViewBinding.waveformAnimation.also {
                        it.visibility = View.VISIBLE
                        it.setAnimation(R.raw.ic_voice_beats)
                        it.playAnimation()
                    }

                    if (mediaPlayerBoolean) {
                        voicePreViewBinding.chronometer.base =
                            SystemClock.elapsedRealtime() + mediaPlayer!!.duration
                        mediaPlayerBoolean = false

                    } else {
                        voicePreViewBinding.chronometer.base =
                            SystemClock.elapsedRealtime() - timeWhenStopped
                    }

                    voicePreViewBinding.chronometer.start()


                    //                val elapsed =
                    //                    ((SystemClock.elapsedRealtime() - voicePreViewBinding.chronometer.base))
                    //
                    //                val elapsedI
                    //                nt = elapsed.toInt()

//                if (timeWhenStopped > 0) {
//
//                    voicePreViewBinding.chronometer.base =
//                        SystemClock.elapsedRealtime() + timeWhenStopped
//                    voicePreViewBinding.chronometer.isCountDown = true
//                    voicePreViewBinding.chronometer.start()
//
//                } else {
//                    voicePreViewBinding.chronometer.base = SystemClock.elapsedRealtime() + totalTIme
//                    voicePreViewBinding.chronometer.isCountDown = true
//                    voicePreViewBinding.chronometer.start()
//                }

                    voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_pause)

                    //                voicePreviewBinding.appBarMain.findViewById<TextView>(R.id.toolbar_title).text = "Play your sound"
                    //                timer!!.schedule(ProgressUpdate(), 0, 100)
                } else //                }

                //                voicePreviewBinding.appBarMain.findViewById<TextView>(R.id.toolbar_title).text = "Play your sound"
                //                timer!!.schedule(ProgressUpdate(), 0, 100)
                //                val elapsed =
                //                    ((SystemClock.elapsedRealtime() - voicePreViewBinding.chronometer.base))
                //
                //                val elapsedInt = elapsed.toInt()
                {
//                try {
                    voicePreViewBinding.ivSoundStop.visibility = View.GONE

                    voicePreViewBinding.waveformAnimation.also {

                        it.visibility = View.VISIBLE
                        object : CountDownTimer(4000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {}
                            override fun onFinish() {
                                it.setAnimation(R.raw.ic_voice_beats)
                                it.playAnimation()
                            }
                        }.start()
                    }


                    voicePreViewBinding.appBarMain.findViewById<TextView>(R.id.toolbar_title).text =
                        resources.getString(R.string.playYourSound)


                    playRecord(pos)

//                }
//                catch (ignored: Exception) {
//                }
                }

            }

        }
    }


    private fun playSound(
        pitchh: Float,
        tempo: Float
    ) {
//        try {
        voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_pause)



        Log.d("hbsCode", "playSound: $pitchh")
        Log.d("hbsCode", "playSound: $tempo")

//            Log.d(
//                "TAG__", "moveFile: " + AudioEffectsCustomActivity.getMIMEType(
//                    intent.getStringExtra("file")
//                )
//            )

//            if (player != null) player!!.stop()

//            val file_path = intent.getStringExtra("file")
        Log.d("TAG_DO", "playSound: ")
        player = SoundStreamAudioPlayer(0, path?.toString(), 0.01f * tempo, pitchh)
        voicePreViewBinding.chronometer.base =
            SystemClock.elapsedRealtime() + (player!!.duration.toInt() / 1000)
        voicePreViewBinding.chronometer.isCountDown = true
        voicePreViewBinding.chronometer.start()
        Thread(player).start()
        if (player != null) {
            player!!.start()
        }
        // voicePreViewBinding.chronometer.base = SystemClock.elapsedRealtime() + player!!.duration


        player!!.setOnProgressChangedListener(object : OnProgressChangedListener {
            override fun onProgressChanged(
                track: Int, currentPercentage: Double, position: Long
            ) {
            }

            override fun onTrackEnd(track: Int) {


                if (player != null) {
                    player!!.stop()
                    player = null
                }

                runOnUiThread {

                    voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)

                    voicePreViewBinding.chronometer.base = SystemClock.elapsedRealtime()
                    voicePreViewBinding.chronometer.stop()
                    timeWhenStopped = 0
                    voicePreViewBinding.ivSoundStop.visibility = View.VISIBLE
                    voicePreViewBinding.waveformAnimation.visibility = View.GONE
                    voicePreViewBinding.waveformAnimation.animation = null
                }


            }

            override fun onExceptionThrown(string: String) {}
        })


//            if (player != null) {
//            }
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//        Thread(player).start()

    }

    override fun onPause() {
        super.onPause()
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer != null) mediaPlayer!!.release()
    }

    override fun onPrepared(mediaPlayer: MediaPlayer) {
        prepared = true
        mediaPlayer.start()
    }

//    private fun getDuration(file: File): String? {
//        val mediaMetadataRetriever = MediaMetadataRetriever()
//        mediaMetadataRetriever.setDataSource(file.absolutePath)
//        val durationStr: String? =
//            mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//        return Utils.formateMilliSeccond(durationStr.toLong())
//    }

    private fun playRecord(position: Int) {

        if (mediaPlayer != null) mediaPlayer!!.release()
        mediaPlayer = MediaPlayer.create(applicationContext, Uri.fromFile(path))

        voicePreViewBinding.chronometer.base =
            SystemClock.elapsedRealtime() + mediaPlayer!!.duration
        voicePreViewBinding.chronometer.isCountDown = true
        voicePreViewBinding.chronometer.start()

        pos = position
        setImageViewIcons()
        explosion = 16000

//        val mTempFIle = File("$filesDir/AdvancedVoiceChangerTemp/OrignalVoiceTemp.wav")
//        if (!path.exists()) savingTempFile(explosion)

        prepared = false
        mediaPlayer!!.setOnPreparedListener(this)
        mediaPlayer!!.setOnCompletionListener {
            voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)
            voicePreViewBinding.chronometer.base =
                SystemClock.elapsedRealtime()
            timeWhenStopped = 0

            mediaPlayerBoolean = true

            voicePreViewBinding.chronometer.stop()
            voicePreViewBinding.ivSoundStop.visibility = View.VISIBLE
            voicePreViewBinding.waveformAnimation.visibility = View.GONE
            voicePreViewBinding.waveformAnimation.animation = null
        }
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
            voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_play)
        }
    }

    private fun setImageViewIcons() {
        txtFileName = findViewById(R.id.txtFileName)
        voicePreViewBinding.imgPlay.setImageResource(R.drawable.ic_pause)
    }

    private fun saveRecord(filename: String) {
        try {
//            if (position < items.size()) {
            explosion = 16000
            savingFile(explosion, filename)
            //            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun moveFile(inputPath: String, name: String, ext: String): String {
        var ext: String? = ext
        ext = AudioEffectsCustomActivity.getMIMEType(path.toString())
        var `in`: InputStream?
        var out: OutputStream? = null
        var imgUri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.Audio.Media.TITLE, getString(R.string.app_name))
            contentValues.put(MediaStore.Audio.Media.MIME_TYPE, ext)
            contentValues.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger"
            )
            imgUri =
                contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imgUri != null) {
                try {
                    out = contentResolver.openOutputStream(imgUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            `in` = FileInputStream(inputPath)
            //            out = new FileOutputStream(outputPath + inputFile);
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out!!.write(buffer, 0, read)
                Log.d("TAG__", "moveFile: ")
            }
            `in`.close()
            `in` = null

            // write the output file
            out!!.flush()
            out.close()
            out = null

            // delete the original file
            File(inputPath).delete()
        } catch (fnfe1: Exception) {
            Log.e("tag", fnfe1.message!!)
        }
        return imgUri.toString()
    }


    private fun savingFile(explosion: Int, filename: String) {
//        val fileName: String?
//        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
//        spEditor = sp!!.edit()
//        val file_name = sp!!.getString("file_nameshare", "")
//        val extra = intent.extras
//        if (extra != null) {
//            fileeshare = null
//            fileName = extra.getString("file_nameshare")
//            Log.d("TAG", "we are in extra and file name:$fileName")
//            val path = File("$filesDir/AdvancedVoiceChanger/")
//            fileeshare = fileName?.let { File(path, it) }
//        }
//        else
//            if (file_name !== "") {
//            Log.d("TAG", "we are in if")
//            val path = File("$filesDir/AdvancedVoiceChanger/")
//            fileeshare = file_name?.let { File(path, it) }
//
//        }
        val f2: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val path = File("$filesDir/AdvancedVoiceChanger/")
            File(path, "$filename.wav")
        } else {
            val path = File("$kept/AdvancedVoiceChanger/")
            if (!path.exists()) path.mkdir()
            File(path, "$filename.wav")
            // The location where you want your WAV file
        }
        try {
            rawToWave(path, f2, explosion)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) moveFile(f2.path, filename)

            savedDialog

            object : CountDownTimer(6000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                }

                override fun onFinish() {
                    startActivity(
                        Intent(
                            this@VoicePreviewActivity,
                            MyAudioFileActivity::class.java
                        ).putExtra("isSaved", true)
                    )
                }
            }.start()


                finish()
//            AdMobInterstitial.getDefaultInstance(this@VoicePreviewActivity)
//                .showInterstitialAd(this@VoicePreviewActivity)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("TAG_", "SavingFile: ")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Log.d("TAG_", "SavingFile: ")
        }
    }


    private val savedDialog: Unit
        get() {
            if (purchaseDialog == null) {
                purchaseDialog = VoiceChangerDialogs.createSavedDialog(this)
                purchaseDialog!!.show()
            } else if (!purchaseDialog!!.isShowing) {
                purchaseDialog!!.show()
            }
        }

    private fun moveFile(inputPath: String, name: String): String {
        var `in`: InputStream?
        var out: OutputStream? = null
        var imgUri: Uri? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val contentResolver = contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            contentValues.put(MediaStore.Audio.Media.TITLE, getString(R.string.app_name))
            contentValues.put(MediaStore.Audio.Media.MIME_TYPE, "audio/x-wav")
            contentValues.put(MediaStore.Audio.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger"
            )
            val collection =
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            imgUri = contentResolver.insert(collection, contentValues)

//            imgUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (imgUri != null) {
                try {
                    out = contentResolver.openOutputStream(imgUri)
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }
            }
        }
        try {
            `in` = FileInputStream(inputPath)
            //            out = new FileOutputStream(outputPath + inputFile);
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out!!.write(buffer, 0, read)
            }
            `in`.close()
            `in` = null

            // write the output file
            out!!.flush()
            out.close()
            out = null

            // delete the original file
            File(inputPath).delete()
        } catch (fnfe1: Exception) {
            Log.e("tag", fnfe1.message!!)
        }
        return imgUri.toString()
    }


//    private fun savingTempFile(explosion: Int) {
//        val fileName= "OrignalVoiceTemp"
//        val file_nm: String?
////        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
////        spEditor = sp!!.edit()
////        val file_name = sp!!.getString("file_nameshare", "")
//        val extra = intent.extras
//        if (extra != null) {
//            fileeshare = null
//            file_nm = extra.getString("file_nameshare")
//            Log.d("TAG", "we are in extra and file name:$file_nm")
//            val path = File("$filesDir/AdvancedVoiceChanger/")
//            fileeshare = file_nm?.let { File(path, it) }
//        } else if (file_name !== "") {
//            Log.d("TAG", "we are in if")
//            val path = File("$filesDir/AdvancedVoiceChanger/")
//            fileeshare = file_name?.let { File(path, it) }
//        }
//        val path = File("$filesDir/AdvancedVoiceChangerTemp/")
//        if (!path.exists()) {
//            path.mkdirs()
//        }
//        val f2 = File(path, "$fileName.wav")
//        try {
//            rawToWave(fileeshare, f2, explosion)
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }

    private fun saveAudio() {
        val dialog = Dialog(this@VoicePreviewActivity)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        dialog.setContentView(R.layout.voice_changer_dialog_save)
        val mEdtFileName = dialog.findViewById<EditText>(R.id.mEditTextFileName)
        val mTxtCancel = dialog.findViewById<Button>(R.id.mTxtCancel)
        val mTxtOk = dialog.findViewById<Button>(R.id.btnOk)
        dialog.findViewById<ImageButton>(R.id.btn_clearEditText).setOnClickListener {
            mEdtFileName.setText("")
        }
        mEdtFileName.setText(
            SimpleDateFormat(
                "yyyy_MM_dd_hh_mm_ss", Locale
                    .getDefault()
            ).format(Date())
        )
        dialog.show()
        mTxtCancel.setOnClickListener { dialog.dismiss() }
        mTxtOk.setOnClickListener {
            if (mEdtFileName.text.toString().isNotEmpty()) {


                if (comingFromCatalog) {
                    SavingFile(mEdtFileName.text.toString())

                } else {

                    saveRecord(mEdtFileName.text.toString())
                }
            } else {
                Toast.makeText(
                    this@VoicePreviewActivity,
                    "Enter recording title",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }
    }

    override fun finish() {
        super.finish()
        val path = File("$filesDir/AdvancedVoiceChangerTemp/")
        if (path.exists()) {
            deleteRecursive(path)
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
            child
        )
        fileOrDirectory.delete()
    }

    override fun onBackPressed() {
        if (exitDialog == null) {
            exitDialog = VoiceChangerDialogs.createExitDialog(this, false) { backCalled() }
            exitDialog!!.show()
        } else if (!exitDialog!!.isShowing) {
            exitDialog!!.show()
        }
    }


    private val optionDialog: Unit
        get() {
            if (optionsDialog == null) {
                optionsDialog = VoiceChangerDialogs.createOptionsDialog(
                    this,
                    this,
                    sp!!.getString("EffectName", "")
                )
                optionsDialog!!.show()
            } else if (!optionsDialog!!.isShowing) {
                optionsDialog!!.show()
            }
        }

//    fun BackPress(v: View?) {
//        back_called()
//    }

    private fun backCalled() {
        startActivity(
            Intent(this@VoicePreviewActivity, MainActivity::class.java)
                .putExtra("isSaved", true)
        )
        finish()
    }

    @Throws(IOException::class)
    private fun rawToWave(rawFile: File?, waveFile: File, sample_rate: Int) {
//
        val rawData = ByteArray(rawFile!!.length().toInt())
        var input: DataInputStream? = null
        try {
            input = DataInputStream(FileInputStream(rawFile))
            input.read(rawData)
        } finally {
            input?.close()
        }
        var output: DataOutputStream? = null
        try {
            output = DataOutputStream(FileOutputStream(waveFile))
            // WAVE header
            // see http://ccrma.stanford.edu/courses/422/projects/WaveFormat/
            writeString(output, "RIFF") // chunk id
            writeInt(output, 36 + rawData.size) // chunk size
            writeString(output, "WAVE") // format
            writeString(output, "fmt ") // subchunk 1 id
            writeInt(output, 16) // subchunk 1 size
            writeShort(output, 1.toShort()) // audio format (1 = PCM)
            writeShort(output, 1.toShort()) // number of channels
            writeInt(output, sample_rate) // 44100 sample rate
            writeInt(output, sample_rate * 2) // byte rate
            writeShort(output, 2.toShort()) // block align
            writeShort(output, 16.toShort()) // bits per sample
            writeString(output, "data") // subchunk 2 id
            writeInt(output, rawData.size) // subchunk 2 size
            // Audio data (conversion big endian -> little endian)
            val shorts = ShortArray(rawData.size / 2)
            ByteBuffer.wrap(rawData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer()[shorts]
            val bytes = ByteBuffer.allocate(shorts.size * 2)
            for (s in shorts) {
                bytes.putShort(s)
            }
            output.write(fullyReadFileToBytes(rawFile))
        } finally {
            output?.close()
        }
    }

    @Throws(IOException::class)
    fun fullyReadFileToBytes(f: File?): ByteArray {
        val size = f!!.length().toInt()
        val bytes = ByteArray(size)
        val tmpBuff = ByteArray(size)
        val fis = FileInputStream(f)
        try {
            var read = fis.read(bytes, 0, size)
            if (read < size) {
                var remain = size - read
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain)
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read)
                    remain -= read
                }
            }
        } catch (e: IOException) {
            throw e
        } finally {
            fis.close()
        }
        return bytes
    }

    @Throws(IOException::class)
    private fun writeInt(output: DataOutputStream, value: Int) {
        output.write(value shr 0)
        output.write(value shr 8)
        output.write(value shr 16)
        output.write(value shr 24)
    }

    @Throws(IOException::class)
    private fun writeShort(output: DataOutputStream, value: Short) {
        output.write(value.toInt() shr 0)
        output.write(value.toInt() shr 8)
    }

    @Throws(IOException::class)
    private fun writeString(output: DataOutputStream, value: String) {
        for (element in value) {
            output.write(element.toInt())
        }
    }

//    companion object {
//        //    RecyclerView listview;
//        //    byte[] bBuffer;
//        //    ArrayList<GetSetAudio> items;
////        var sp: SharedPreferences? = null
////        var file: String? = null
//    }


    override fun onClickSelect(someValue: Int) {
        when (someValue) {
            0 -> {
                startActivity(
                    Intent(
                        this,
                        AudioEffectsCustomActivity::class.java
                    ).putExtra(
                        "file",
                        path.toString()
                    )
                )
            }
            1 -> {
                saveTempFileInCache()


            }
            2 -> {
                startActivity(Intent(this, RecordingActivity::class.java))
            }
        }

    }

    private fun saveTempFileInCache() {
//        val filename = "audioFileEffects"
////
////
//        val fileExt = "wav"

        val f2: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val path = File(externalCacheDir?.absolutePath.toString())
            if (!path.exists()) path.mkdirs()
            File(path, "audioFileEffects.wav")
        } else {
            val path = File(externalCacheDir?.absolutePath.toString())
            if (!path.exists()) path.mkdir()
            File(path, "audioFileEffects.wav")
        }
        try {
            soundWriter = SoundStreamFileWriter(
                0, path.toString(),
                f2.path,
                sp?.getInt("CatalogTempo", 0)!! * 0.01f,
                sp?.getFloat("CatalogPitch", 0.0F)!!,
            )
            Thread(soundWriter).start()
//
////            Toast.makeText(this, "Savedddd", Toast.LENGTH_SHORT).show()
//            val newFilePath = File(externalCacheDir?.absolutePath + "/audioFileEffects.wav")
////            Toast.makeText(this, "length"+newFilePath.length(), Toast.LENGTH_SHORT).show()
////            Toast.makeText(this, "path"+newFilePath.path, Toast.LENGTH_SHORT).show()

            soundWriter!!.start()
            val intent = Intent(
                this,
                AudioEffectsCustomActivity::class.java
            )
            intent.putExtra("comingFromCatalog", true)
            startActivity(intent)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }


}