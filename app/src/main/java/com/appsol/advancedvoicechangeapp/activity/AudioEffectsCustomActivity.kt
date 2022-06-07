package com.appsol.advancedvoicechangeapp.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.airbnb.lottie.LottieAnimationView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.SpacesItemDecoration
import com.appsol.advancedvoicechangeapp.Utils
import com.appsol.advancedvoicechangeapp.Utils.Companion.getTime
import com.appsol.advancedvoicechangeapp.adapter.EffectsListAdapter
import com.appsol.advancedvoicechangeapp.databinding.VoicesListActivityBinding
import com.appsol.advancedvoicechangeapp.model.GetSetAudio
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import com.appsol.advancedvoicechangeapp.utils.BackgroundTask
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs
import net.surina.soundtouch.OnProgressChangedListener
import net.surina.soundtouch.SoundStreamAudioPlayer
import net.surina.soundtouch.SoundStreamFileWriter
import smartdevelop.ir.eram.showcaseviewlib.GuideView
import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class AudioEffectsCustomActivity : AppCompatActivity(),
    EffectsListAdapter.EffectsClickListener {

    var items: ArrayList<GetSetAudio> = ArrayList()

    private var voiceAdaptersObj: EffectsListAdapter? = null
    lateinit var voicesListActivityBinding: VoicesListActivityBinding
    private var pos = 0
    private var kept: String? = null
    private var exitDialog: Dialog? = null
    private var recyclerViewState: Parcelable? = null
    private var progressDialog: ProgressDialog? = null
    var player: SoundStreamAudioPlayer? = null

    var playerForDialog: SoundStreamAudioPlayer? = null

    private var catalogBoolean = false

    private var savedBoolean = false

    var effectPath: File? = null


    private var sp: SharedPreferences? = null
    private var spEditor: SharedPreferences.Editor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        voicesListActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.voices_list_activity)
        AdMobInterstitial.getAdmobAds(this, findViewById(R.id.bannerContainer))

        sp = getSharedPreferences(MainActivity.MY_PREFS, MODE_PRIVATE)
        spEditor = sp!!.edit()

        catalogBoolean = intent.getBooleanExtra("comingFromCatalog", false)
        savedBoolean = intent.getBooleanExtra("comingFromSaved", false)

        if (catalogBoolean) {
            effectPath = File(
                externalCacheDir?.absolutePath.toString() + "/audioFileEffects.wav"
            )
        } else if (savedBoolean) {

            val uri = Uri.parse(intent.getStringExtra("savedFileUri"))
            effectPath = File(uri.path!!)
        }


        items = Utils.getAudioItemsArray()

        if (sp!!.getBoolean("isFirstTimeLaunch", true)) {
            GuideView.Builder(this)
                .setTitle("")
                .setContentText("You can change Pitch and Tempo of your sound.")
                .setTargetView(voicesListActivityBinding.btnCustomization)
                .setGravity(GuideView.Gravity.auto)
                .setContentTextSize(18) //optional
                .setDismissType(GuideView.DismissType.anywhere)
                .setGuideListener {
                    spEditor?.putBoolean("isFirstTimeLaunch", false)
                    spEditor?.apply()
                }
                .build()
                .show()
        }


        voicesListActivityBinding.appBarMain.also { layout ->
            layout.findViewById<RelativeLayout>(R.id.RL_appbar)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPurple))
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
                    saveAudio(pos)
                }
            }



            layout.findViewById<TextView>(R.id.toolbar_title).also {
                it.text = resources.getString(R.string.app_name)
                it.setTextColor(ContextCompat.getColor(this, R.color.white))
            }
        }


        voicesListActivityBinding.btnCustomization.setOnClickListener {

            if (player != null) {
                player?.pause()
                voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
            }
            showCustomizationDialog(items[pos].pitch, items[pos].tempo)

        }

        val mBaseFolderPath = getExternalFilesDir(null)!!.absolutePath
        kept = mBaseFolderPath.substring(0, mBaseFolderPath.indexOf("/Android"))

        voicesListActivityBinding.imgPlay.setOnClickListener {
            if (player != null && !player!!.isPaused) {
                player!!.pause()
                voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
            } else if (player != null && player!!.isPaused) {
                player!!.start()
                voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_pause_effect)
            } else playRecord(pos)
        }


        voicesListActivityBinding.seekbarPlay.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (p1 > 99)
                    stopaudio()
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                setDurationAudio(seekBar!!.progress)
            }
        })

        voiceAdaptersObj = EffectsListAdapter(this, items)
        voiceAdaptersObj!!.setClickListener(this)
        voicesListActivityBinding.listView1.also {

            it.layoutManager = GridLayoutManager(this, 3)
            recyclerViewState =
                it.layoutManager!!.onSaveInstanceState()
            // Restore state
            it.layoutManager!!.onRestoreInstanceState(
                recyclerViewState
            )

            val spacingForH = resources.getDimensionPixelSize(R.dimen._4sdp)
            val spacingForV = resources.getDimensionPixelSize(R.dimen._8sdp)
            it.addItemDecoration(
                SpacesItemDecoration(
                    spacingForH,
                    spacingForV
                )
            )
            it.adapter = voiceAdaptersObj
            it.itemAnimator = null
        }


        cleardDirictory()
        val path = File("$filesDir/AdvancedVoiceChanger/")
        if (!path.exists()) path.mkdirs()
    }


    override fun onDestroy() {
        super.onDestroy()
        if (player != null) player!!.stop()
        if (playerForDialog != null) playerForDialog!!.stop()
        cleardDirictory()
    }

    private fun cleardDirictory() {
        val path = File("$filesDir/AdvancedVoiceChangerTemp/")
        if (path.exists()) {
            deleteRecursive(path)
        }
    }


    fun stopAudioDialog(imageButton: ImageButton) {
        if (playerForDialog != null && !playerForDialog!!.isPaused) {
            playerForDialog!!.stop()
            imageButton.setImageResource(R.drawable.ic_play_effect)
        }
        Log.d("iaminf", "if stopaudio")
    }


    fun stopaudio() {
        if (player != null && !player!!.isPaused) {
            player!!.stop()
            voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
        }
    }

    private fun playRecord(
        position: Int,
    ) {

        pos = position
        setImageViewIcons(
            position,
            voicesListActivityBinding.txtFileName,
            voicesListActivityBinding.imgPlay
        )
        explosion = items[position].freq
        voicesListActivityBinding.seekbarPlay.progress = 0

        playSound(
            items[position].pitch,
            items[position].tempo.toFloat()
        )
    }


    private fun playRecordDialog(
        position: Int,
        newSeekBar: SeekBar,
        textView: TextView,
        startTextView: TextView,
        fileNmae: TextView,
        imageButton: ImageButton
    ) {

        pos = position
        setImageViewIcons(position, fileNmae, imageButton)
        explosion = items[position].freq
        newSeekBar.progress = 0

        playSoundDialog(
            items[position].pitch,
            items[position].tempo.toFloat(),
            newSeekBar,
            textView,
            startTextView, imageButton
        )
    }


    var current: Long = 0
    private fun playSound(
        pitchh: Float,
        tempo: Float
    ) {
        try {
//            Log.d(
//                "TAG__", "moveFile: " + getMIMEType(
//                    intent.getStringExtra("file")
//                )
//            )

            if (player != null) player!!.stop()
//            val file_path = intent.getStringExtra("file")

            Log.d("TAG_DO", "playSound: ")

            player = if (catalogBoolean || savedBoolean) {

                SoundStreamAudioPlayer(0, effectPath.toString(), 0.01f * tempo, pitchh)

            } else {

                SoundStreamAudioPlayer(0, intent.getStringExtra("file"), 0.01f * tempo, pitchh)
            }

            voicesListActivityBinding.txtEndTime.text =
                getTime((player!!.duration.toInt() / 1000).toLong())
            current = 0
            player!!.setOnProgressChangedListener(object : OnProgressChangedListener {
                override fun onProgressChanged(
                    track: Int, currentPercentage: Double, position: Long
                ) {
                    voicesListActivityBinding.txtStartTime.text = getTime(position / 1000)
                    if (position / 1000 > current) {

                        voicesListActivityBinding.seekbarPlay.progress =
                            (currentPercentage * 100).toInt()
                        current = position / 1000 + 1000
                    }

                    voicesListActivityBinding.seekbarPlay.progress =
                        ((currentPercentage * 100).toInt())
                }

                override fun onTrackEnd(track: Int) {
                    if (player != null) {
                        player!!.stop()
                        player = null
                    }

                    runOnUiThread {

                        voicesListActivityBinding.txtStartTime.text = getString(R.string._00_00)
                        voicesListActivityBinding.txtEndTime.text = getString(R.string._00_00)

                        voicesListActivityBinding.seekbarPlay.progress = 0
                        voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
                    }

                }

                override fun onExceptionThrown(string: String) {}
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Thread(player).start()
        if (player != null) {
            player!!.start()
        }
    }

    var currentDialog: Long = 0

    private fun playSoundDialog(
        pitchh: Float,
        tempo: Float,
        newSeekBar: SeekBar,
        EndTime: TextView,
        StartTime: TextView,
        imageButton: ImageButton
    ) {
        try {

            //            if (playerForDialog != null) playerForDialog!!.stop()


            playerForDialog = if (catalogBoolean || savedBoolean) {


                SoundStreamAudioPlayer(0, effectPath.toString(), 0.01f * tempo, pitchh)

            } else {

                SoundStreamAudioPlayer(0, intent.getStringExtra("file"), 0.01f * tempo, pitchh)
            }

//            val file_path = intent.getStringExtra("file")
//            Log.d("TAG_DO", "playSound: ")
            EndTime.text = getTime((playerForDialog!!.duration.toInt() / 1000).toLong())
            currentDialog = 0
            playerForDialog!!.setOnProgressChangedListener(object : OnProgressChangedListener {
                override fun onProgressChanged(
                    track: Int, currentPercentage: Double, position: Long
                ) {
                    StartTime.text = getTime(position / 1000)
                    if (position / 1000 > currentDialog) {

                        newSeekBar.progress = (currentPercentage * 100).toInt()
                        currentDialog = position / 1000 + 1000
                    }

                    newSeekBar.progress =
                        ((currentPercentage * 100).toInt())
                }

                override fun onTrackEnd(track: Int) {
                    imageButton.setImageResource(R.drawable.ic_play_effect)
                    playerForDialog!!.stop()
                    items[pos].setIsPlaing(false)
                    StartTime.text = getString(
                        R.string._00_00
                    )
                    EndTime.text = getString(
                        R.string._00_00
                    )
                    newSeekBar.progress = 0
                }

                override fun onExceptionThrown(string: String) {}
            })
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Thread(playerForDialog).start()
        if (playerForDialog != null) {
            playerForDialog!!.start()
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null && !player!!.isPaused) {
            player!!.stop()
            voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
        }


    }

    fun setDurationAudio(progress: Int) {
        if (player != null) player!!.seekTo((progress.toFloat() / 100).toDouble(), false)
        if (player != null && player!!.isPaused) player!!.start()
        current = 0
    }

    fun setDurationAudioDialog(progress: Int) {
        if (playerForDialog != null && playerForDialog!!.isInitialized) {
            playerForDialog!!.seekTo(
                (progress.toFloat() / 100).toDouble(),
                false
            )
            if (playerForDialog!!.isPaused) playerForDialog!!.start()
            currentDialog = 0
        }
    }


    private fun setImageViewIcons(position: Int, txtFileName: TextView, imageButton: ImageButton) {
        imageButton.setImageResource(R.drawable.ic_pause_effect)
        txtFileName.text = items[position].title
    }

    private fun saveRecord(position: Int, filename: String) {
        try {
            if (position < items.size) {
                explosion = position
                SavingFile(explosion, filename)
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }

    private fun loadProgressDialog() {
        progressDialog = ProgressDialog(this@AudioEffectsCustomActivity)
        progressDialog!!.also {

            it.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            it.setTitle("Loading...")
            it.setMessage("Preparing your file, please wait...")
            it.setCancelable(false)
            it.setButton("Cancel") { _, _ ->
                if (writer != null) writer!!.stop()
                it.dismiss()
            }
            it.isIndeterminate = false
            it.max = 100
            it.progress = 0
            it.show()
        }
    }

    @SuppressLint("CutPasteId")
    private fun showCustomizationDialog(pitch: Float, tempo: Int) {

        val inflater = LayoutInflater.from(this)

        val rootView: View = inflater.inflate(R.layout.layout_dialog_audio, null)

        val dialog = Dialog(this, android.R.style.Theme_Translucent_NoTitleBar)
        val window = dialog.window
        val wlp = window!!.attributes
        wlp.gravity = Gravity.CENTER
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT


        dialog.also { dialogg ->

            dialogg.setContentView(rootView)

            dialog.findViewById<SeekBar>(R.id.seekbar).max = 13
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.findViewById<SeekBar>(R.id.seekbar).min = -8
            }
            dialog.findViewById<SeekBar>(R.id.seekbarTempo).max = 165
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                dialog.findViewById<SeekBar>(R.id.seekbarTempo).min = 45
            }
            dialog.findViewById<SeekBar>(R.id.seekbar).progress = pitch.toInt()
            dialog.findViewById<SeekBar>(R.id.seekbarTempo).progress = tempo

            dialogg.findViewById<TextView>(R.id.tv_progressText).text =
                pitch.toString()
            dialogg.findViewById<TextView>(R.id.tv_progressTextTempo).text =
                tempo.toString()

            dialogg.findViewById<Button>(R.id.btn_cancle).setOnClickListener {
                if (playerForDialog != null)
                    playerForDialog!!.stop()
                dialogg.findViewById<ImageButton>(R.id.imgPlay)
                    .setImageResource(R.drawable.ic_play_effect)
                dialog.dismiss()
            }

            dialogg.findViewById<SeekBar>(R.id.seekbarPlay).setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                    if (i > 99) {
                        stopAudioDialog(dialogg.findViewById(R.id.imgPlay))
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {

                    setDurationAudioDialog(seekBar.progress)
                }
            })

            dialogg.findViewById<SeekBar>(R.id.seekbar)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                        if (p1 > 99)
                            if (playerForDialog != null && playerForDialog!!.isPaused) {

                                stopAudioDialog(dialogg.findViewById(R.id.imgPlay))
                            }

                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        items[pos].pitch = ((seekBar!!.progress).toFloat())
                        dialogg.findViewById<TextView>(R.id.tv_progressText).text =
                            (((seekBar.progress).toString()))
                        if (items[pos].getisPlaing())

                            stopAudioDialog(dialogg.findViewById(R.id.imgPlay))
                        val handler = Handler()
                        handler.postDelayed({

                            playRecordDialog(
                                pos,
                                dialogg.findViewById(R.id.seekbarPlay),
                                dialogg.findViewById(R.id.fileEndTime),
                                dialogg.findViewById(R.id.tv_fileStartTime),
                                dialogg.findViewById(R.id.tv_fileName),
                                dialogg.findViewById(R.id.imgPlay)
                            )
                        }, 0)
                    }

                })
            dialogg.findViewById<SeekBar>(R.id.seekbarTempo)
                .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                    override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {

                        if (p1 > 99)
                            if (playerForDialog != null && !playerForDialog!!.isPaused) {

                                stopAudioDialog(dialogg.findViewById(R.id.imgPlay))
                            }

                    }

                    override fun onStartTrackingTouch(p0: SeekBar?) {}

                    override fun onStopTrackingTouch(seekBar: SeekBar?) {
                        items[pos].tempo = ((seekBar!!.progress))
                        dialogg.findViewById<TextView>(R.id.tv_progressTextTempo).text =
                            (((seekBar.progress).toString()))
                        if (items[pos].getisPlaing())
                            playerForDialog!!.pause()
                        val handler = Handler()
                        handler.postDelayed({
                            playRecordDialog(
                                pos,
                                dialogg.findViewById(R.id.seekbarPlay),
                                dialogg.findViewById(R.id.fileEndTime),
                                dialogg.findViewById(R.id.tv_fileStartTime),
                                dialogg.findViewById(R.id.tv_fileName),
                                dialogg.findViewById(R.id.imgPlay)

                            )
                        }, 0)
                    }

                })

            dialogg.findViewById<ImageButton>(R.id.imgPlay).setOnClickListener {
                if (playerForDialog != null && !playerForDialog!!.isPaused) {
                    playerForDialog!!.pause()
                    dialogg.findViewById<ImageButton>(R.id.imgPlay)
                        .setImageResource(R.drawable.ic_play_effect)
                } else if (playerForDialog != null && playerForDialog!!.isPaused) {

                    if (playerForDialog!!.isInitialized) {
                        playerForDialog!!.start()
                        dialogg.findViewById<ImageButton>(R.id.imgPlay)
                            .setImageResource(R.drawable.ic_pause_effect)
                    }


                } else
                    playRecordDialog(
                        pos,
                        dialogg.findViewById(R.id.seekbarPlay),
                        dialogg.findViewById(R.id.fileEndTime),
                        dialogg.findViewById(R.id.tv_fileStartTime),
                        dialogg.findViewById(R.id.tv_fileName),
                        dialogg.findViewById(R.id.imgPlay)

                    )
            }
            dialogg.findViewById<ImageButton>(R.id.btn_refresh).setOnClickListener {
                dialogg.findViewById<SeekBar>(R.id.seekbar).progress =
                    pitch.toInt()
                dialogg.findViewById<TextView>(R.id.tv_progressText).text =
                    pitch.toString()
            }

            dialogg.findViewById<ImageButton>(R.id.btn_refreshTempo).setOnClickListener {
                dialogg.findViewById<SeekBar>(R.id.seekbarTempo).progress =
                    tempo
                dialogg.findViewById<TextView>(R.id.tv_progressTextTempo).text =
                    tempo.toString()


                playRecordDialog(
                    pos,
                    dialogg.findViewById(R.id.seekbarPlay),
                    dialogg.findViewById(R.id.fileEndTime),
                    dialogg.findViewById(R.id.tv_fileStartTime),
                    dialogg.findViewById(R.id.tv_fileName),
                    dialogg.findViewById(R.id.imgPlay)

                )
            }
            dialog.findViewById<Button>(R.id.btn_save).setOnClickListener {
                saveAudio(pos)
            }

            dialog.show()

        }
    }

    var writer: SoundStreamFileWriter? = null
    private fun SavingFile(explosion: Int, filename: String) {
        Log.d("TAG_in", "onTrackEnd: ....1")

        loadProgressDialog()
        val file_nm: String? = null
        val file = Uri.fromFile(File(intent.getStringExtra("file")))
        var fileExt = MimeTypeMap.getFileExtensionFromUrl(file.toString())
        if (fileExt == "pcm") fileExt = "wav"
        val f2: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val path = File("$filesDir/AdvancedVoiceChanger/")
            if (!path.exists()) path.mkdirs()
            File(path, "$filename.$fileExt")
        } else {
            val path = File("$kept/AdvancedVoiceChanger/")
            if (!path.exists())
                path.mkdir()
            File(path, "$filename.$fileExt")
        }
        Log.d("TAG_in", "onTrackEnd: ....2 " + f2.path)

        try {
            writer = SoundStreamFileWriter(
                0, intent.getStringExtra("file"),
                f2.path, items[explosion].tempo * 0.01f, items[explosion].pitch
            )
            Thread(writer).start()
            val finalFileExt = fileExt
            if (player != null) {
                player!!.stop()
                voicesListActivityBinding.imgPlay.setImageResource(R.drawable.ic_play_effect)
                player!!.setOnProgressChangedListener(null)
            }
            writer!!.setOnProgressChangedListener(object : OnProgressChangedListener {
                override fun onProgressChanged(
                    track: Int,
                    currentPercentage: Double,
                    position: Long
                ) {
                    if (progressDialog != null) progressDialog!!.progress =
                        (currentPercentage * 100).toInt()
                }

                override fun onTrackEnd(track: Int) {
                    Log.d("TAG_in", "onTrackEnd: ....3" + track)

                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            BackgroundTask.execute(object : BackgroundTask.Task("", 1000L, "") {
                                override fun execute() {
                                    if (f2.length() > 100) {
                                        if (moveFile(
                                                f2.path,
                                                filename,
                                                finalFileExt
                                            ) != ""
                                        ) gotoNextScreen()
                                    } else {
                                        f2.delete()
                                        runOnUiThread {
                                            oppsDialog
                                        }
                                    }
                                }
                            })
                        } else {
                            Log.d("TAG_in", "onTrackEnd: " + f2.length())
                            if (f2.length() > 100) {
                                gotoNextScreen()
                            } else {
                                f2.delete()
                                oppsDialog
                            }
                        }
                    } catch (e: IllegalArgumentException) {
                        Log.d("TAG_in", "onTrackEnd: " + e.message)

                        e.printStackTrace()
                        Log.d("TAG_", "SavingFile: ")
                    }
                }

                override fun onExceptionThrown(string: String) {}
            })
            writer!!.start()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun gotoNextScreen() {
        if (progressDialog != null) progressDialog!!.dismiss()
        startActivity(
            Intent(this@AudioEffectsCustomActivity, MyAudioFileActivity::class.java)
                .putExtra("isSaved", true)
        )
        finish()
        AdMobInterstitial.getDefaultInstance(this@AudioEffectsCustomActivity)
            .showInterstitialAd(this@AudioEffectsCustomActivity)
    }

    private fun moveFile(inputPath: String, name: String, ext: String): String {
        var ext: String? = ext
        ext = getMIMEType(intent.getStringExtra("file"))
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

    fun SavingTempFile(position: Int, filename: String) {
//        val file_nm: String? = null
//        val file_path = intent.getStringExtra("file")
        val path = File("$filesDir/AdvancedVoiceChangerTemp/")
        if (!path.exists()) {
            path.mkdirs()
        }
        val f2 = File(path, "$filename.wav")
//                process(file_path, f2.getPath(), items.get(position).getPitch(), items.get(position).getTempo());
    }

    private fun saveAudio(pos: Int) {
        val dialog = Dialog(this@AudioEffectsCustomActivity)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE) //before
        dialog.setContentView(R.layout.voice_changer_dialog_save)
        val mEdtFileName = dialog.findViewById<View>(R.id.mEditTextFileName) as EditText
        val btnClearEditText = dialog.findViewById<View>(R.id.btn_clearEditText) as ImageButton
        val mTxtCancel = dialog.findViewById<View>(R.id.mTxtCancel) as Button
        val mTxtOk = dialog.findViewById<View>(R.id.btnOk) as Button

        btnClearEditText.setOnClickListener {
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
                saveRecord(pos, mEdtFileName.text.toString())
            } else {
                Toast.makeText(
                    this@AudioEffectsCustomActivity,
                    "Enter recording title",
                    Toast.LENGTH_SHORT
                ).show()
            }
            dialog.dismiss()
        }
    }

//    override fun finish() {
//        super.finish()
//    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()) deleteRecursive(
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

    //    fun BackPress(v: View?) {
//        back_called()
//    }
//
    private fun backCalled() {
        startActivity(
            Intent(this@AudioEffectsCustomActivity, MainActivity::class.java)
                .putExtra("isSaved", true)
        )
        finish()
    }

    private val oppsDialog: Unit
        get() {
            if (progressDialog != null) progressDialog!!.dismiss()
            val dialog = VoiceChangerDialogs.createOppsDialog(this) { backCalled() }
            dialog.show()
        }

    companion object {
        var file: String? = null
        var explosion = 0
        fun getMIMEType(url: String?): String? {
            var mType: String? = null
            val mExtension = MimeTypeMap.getFileExtensionFromUrl(url)
            if (mExtension != null) {
                mType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(mExtension)
            }
            return mType
        }
    }


    override fun onItemClickPlay(position: Int) {
        stopaudio()
        pos = position
        val handler = Handler()
        handler.postDelayed({
            playRecord(
                position
            )
        }, 0)
    }

}