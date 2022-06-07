package com.appsol.advancedvoicechangeapp.activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.SpacesItemDecoration
import com.appsol.advancedvoicechangeapp.adapter.MusicAdapter
import com.appsol.advancedvoicechangeapp.adapter.MusicInterface
import com.appsol.advancedvoicechangeapp.databinding.ActivityRecordingBinding
import com.appsol.advancedvoicechangeapp.model.RecordingData
import com.appsol.advancedvoicechangeapp.utils.FacebookAdsInterstitialAd
import com.appsol.advancedvoicechangeapp.utils.SharedPrefs
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs
import com.github.squti.androidwaverecorder.RecorderState
import com.github.squti.androidwaverecorder.WaveRecorder
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class RecordingActivity : AppCompatActivity(), MusicInterface, MusicAdapter.RefreshData {

//    private var recording = false

    private var booleanForRecording = false

    private var exitDialog: Dialog? = null


//    private var sp: SharedPreferences? = null
//    private var spEditor: SharedPreferences.Editor? = null
//    private var audioRecord: AudioRecord? = null
//    private var path: File? = null
//    private var fileName: String? = null

    private var namesList = ArrayList<RecordingData>()
    private var dataList = ArrayList<String>()


    private var isRecording = false
    private var isPaused = false


    var adapter: MusicAdapter? = null
    private var kept: String? = null
    private var reviewManager: ReviewManager? = null

    private var sharedPrefs: SharedPrefs? = null

    private lateinit var recordingActivityBinding: ActivityRecordingBinding


    private var startTime = 0L
    private var finalTime = 0L


    private var mediaPlayer: MediaPlayer? = null

    private var posForPlayer = -1

    val myHandler = Handler()

    private lateinit var seekbar: SeekBar
    private lateinit var tx1: TextView

    private lateinit var waveRecorder: WaveRecorder

    private lateinit var filePath: String

    var sp: SharedPreferences? = null

    var fromCatalog = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        recordingActivityBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_recording)


        sp = getSharedPreferences(MainActivity.MY_PREFS, MODE_PRIVATE)

        fromCatalog = intent.getBooleanExtra("comingFromCatalog", false)


        if (fromCatalog) {

//            recordingActivityBinding.tvTapForRecording.text = resources.getString(R.string.tap_to_record) +
//                    "in" +                intent.getStringExtra("EffectName")
            recordingActivityBinding.tvTapForRecordingForOne.text = "Tap here to Record in " +
                    sp!!.getString("EffectName", "") + " Effect"
        } else {
            recordingActivityBinding.tvTapForRecordingForOne.text =
                resources.getString(R.string.tap_on_mic_to_start_recording)
//            recordingActivityBinding.tvTapForRecording.text = resources.getString(R.string.tap_to_record)
        }

        filePath = externalCacheDir?.absolutePath + "/audioFile.wav"

        val file = File(filePath)

        if (file.exists()) {
            file.delete()
        }


        waveRecorder = WaveRecorder(filePath)
        waveRecorder.waveConfig.sampleRate = 44100
        waveRecorder.waveConfig.channels = AudioFormat.CHANNEL_IN_STEREO

        waveRecorder.noiseSuppressorActive = true


        waveRecorder.onStateChangeListener = {
            when (it) {
                RecorderState.RECORDING -> startRecording()
                RecorderState.STOP -> stopRecording()
                RecorderState.PAUSE -> pauseRecording()
            }
        }


        init()

        reviewManager = ReviewManagerFactory.create(this)
        sharedPrefs = SharedPrefs(this)

        FacebookAdsInterstitialAd.getApplovinBanner(this, findViewById(R.id.bannerContainer))

        val mBaseFolderPath = getExternalFilesDir(null)!!.absolutePath
        kept = mBaseFolderPath.substring(0, mBaseFolderPath.indexOf("/Android"))

        getFiles()

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())


        if (intent.getBooleanExtra("isSaved", false)) {
            if (sharedPrefs!!.ratingDailogValue == 5) {
                sharedPrefs!!.setRatingDailogValue(this, 3)
            } else sharedPrefs!!.setRatingDailogValue(this, sharedPrefs!!.ratingDailogValue + 1)
        }


    }

    override fun onResume() {
        super.onResume()
        getFiles()
    }

    override fun onBackPressed() {

        if (booleanForRecording) {
            if (exitDialog == null) {
                exitDialog = VoiceChangerDialogs.createExitDialog(this, false) { backCalled() }
                exitDialog!!.show()
            } else if (!exitDialog!!.isShowing) {
                exitDialog!!.show()
            }

        } else {
            backCalled()
        }
    }


    private fun startRecording() {
        isRecording = true
        isPaused = false
    }

    private fun stopRecording() {
        isRecording = false
        isPaused = false
    }

    private fun pauseRecording() {
        isPaused = true
    }

    private fun backCalled() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .putExtra("isSaved", false)
        )
        finish()
    }


    private fun getFiles() {
        namesList.clear()
        recordingActivityBinding.recyclerViewsSavedItems.adapter = null
        try {
            val dir: File =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) File(kept + File.separator + Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger/") else File(
                    "$kept/AdvancedVoiceChanger/"
                )
            val list = dir.listFiles()
            Log.d("TAG", "before k:" + Arrays.toString(list))
            if (list != null) for (f in list) {
                val uri = Uri.fromFile(f)
                dataList.add(uri.toString())
                val mmr = MediaMetadataRetriever()
                mmr.setDataSource(uri.path)
                val durationStr: String? =
                    mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                val seconds: String = java.lang.String.valueOf(durationStr!!.toInt() % 60000 / 1000)
                Log.d("seconds===========>", seconds)
                Log.d("hbs_Code", "getFiles: $seconds")

                val fileSize: Int = java.lang.String.valueOf(f.length() / 1024).toInt()

                Log.d("TAG", "uri:$uri")
                namesList.add(
                    RecordingData(
                        seconds.toLong(), f.name,
                        f.lastModified(), fileSize.toLong(), uri
                    )
                )
            }
        } catch (ignored: java.lang.Exception) {
        }
        with(namesList) { reverse() }
        adapter =
            MusicAdapter(this, namesList, kept, this, this, true)

        if (namesList.isEmpty()) {
            recordingActivityBinding.RLNoAudio.visibility = View.VISIBLE
//            recordingActivityBinding.tvTapForRecording.visibility = View.GONE
            recordingActivityBinding.tvTapForRecordingForOne.visibility = View.VISIBLE
        } else {
            recordingActivityBinding.recyclerViewsSavedItems.also {
                it.adapter = adapter
                it.isNestedScrollingEnabled = false
                val spacingForH = resources.getDimensionPixelSize(R.dimen._2sdp)
                val spacingForV = resources.getDimensionPixelSize(R.dimen._4sdp)
                it.addItemDecoration(
                    SpacesItemDecoration(
                        spacingForH,
                        spacingForV
                    )
                )
            }
        }

    }

    private fun init() {
        recordingActivityBinding.micAnimation.also { button ->
//            button.imageAssetsFolder = "images2"
//            button.playAnimation()
            button.setOnClickListener {
                myHandler.removeCallbacks(updateSongTime)

                if (!isRecording) {


                    recordingActivityBinding.tvTapForRecordingForOne.visibility = View.GONE

                    recordingActivityBinding.CLLayoutOne.visibility = View.GONE

                    if (mediaPlayer?.isPlaying == true) {
                        stopPlaying()
                        recordingActivityBinding.RlPlayer.visibility = View.INVISIBLE
                    }

                    waveRecorder.startRecording()

//                    recordingActivityBinding.tvTapForRecording.visibility = View.GONE

                    recordingActivityBinding.CLLayoutOne.visibility = View.INVISIBLE

                    recordingActivityBinding.CLLayoutTwo.visibility = View.VISIBLE

                    recordingActivityBinding.micAnimation.also {
                        it.imageAssetsFolder = "images3"
                        it.setAnimation(R.raw.ic_pause_animation)
                        it.playAnimation()
                    }


                    recordingActivityBinding.waveformAnimation.also {
                        it.setAnimation(R.raw.data)
                        it.imageAssetsFolder = "images1"

                        it.playAnimation()

                        object : CountDownTimer(4000, 1000) {
                            override fun onTick(millisUntilFinished: Long) {
                                //here you can have your logic to set text to edittext
                            }

                            override fun onFinish() {
                                it.setAnimation(R.raw.ic_voice_beats)
                                it.playAnimation()

                            }
                        }.start()
                    }
                    isRecording = true
                    recordingActivityBinding.chronometer.base = SystemClock.elapsedRealtime()
                    recordingActivityBinding.chronometer.start()
//                    Toast.makeText(this, "recording starteddd", Toast.LENGTH_SHORT).show()

                } else {
                    recordingActivityBinding.chronometer.stop()
                    recordingActivityBinding.chronometer.base = SystemClock.elapsedRealtime()
                    val intent = Intent(this, VoicePreviewActivity::class.java)
                    intent.putExtra("comingFromVoice", fromCatalog)
                    startActivity(intent)
                    finish()
                    waveRecorder.stopRecording()
//                    Toast.makeText(this, "recording stopeddd", Toast.LENGTH_SHORT).show()

                }

//                if (!recording) {
//
//                    recordingActivityBinding.tvTapForRecording.visibility = View.GONE
//
//                    recordingActivityBinding.CLLayoutOne.visibility = View.INVISIBLE
//
//                    recordingActivityBinding.CLLayoutTwo.visibility = View.VISIBLE
//
//                    recordingActivityBinding.micAnimation.also {
//                        it.setAnimation(R.raw.ic_pause_animation)
//                        it.playAnimation()
//                    }
//
//
//                    recordingActivityBinding.waveformAnimation.also {
//                        it.setAnimation(R.raw.data)
//                        it.imageAssetsFolder = "images1"
//
//                        it.playAnimation()
//
//                        object : CountDownTimer(4000, 1000) {
//                            override fun onTick(millisUntilFinished: Long) {
//                                //here you can have your logic to set text to edittext
//                            }
//
//                            override fun onFinish() {
//                                it.setAnimation(R.raw.ic_voice_beats)
//                                it.playAnimation()
//
//                            }
//                        }.start()
//                    }
//                    recording = true
//                    recordingActivityBinding.chronometer.base = SystemClock.elapsedRealtime()
//                    recordingActivityBinding.chronometer.start()
//                    val recordThread = Thread {
//                        recording = true
//                        try {
//
//                            recordingActivityBinding.CLLayoutOne.visibility = View.GONE
//
//                            if (mediaPlayer?.isPlaying == true) {
//                                stopPlaying()
//                                recordingActivityBinding.RlPlayer.visibility = View.INVISIBLE
//                            }
//                            booleanForRecording = true
//
//
//                            waveRecorder.startRecording()
//
//                            Toast.makeText(this, "recording starteddd", Toast.LENGTH_SHORT).show()
//
//
//
//                            //  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
////                                startRecordScopStorage() else
////                                startRecord()
//                        } catch (ignored: Exception) {
//                        }
//                    }
//                    recordThread.start()
//                }
//                else {
//
//                    waveRecorder.stopRecording()
//                    Toast.makeText(this, "recording stopeddd", Toast.LENGTH_SHORT).show()
//
//
//
//                    sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
//                    spEditor = sp!!.edit()
//                    recording = false
//                    val elapsedMillis =
//                        SystemClock.elapsedRealtime() - recordingActivityBinding.chronometer.base
//                    spEditor!!.putLong("file_time", elapsedMillis)
//                    spEditor!!.commit()
//
//                    recordingActivityBinding.chronometer.stop()
//                    recordingActivityBinding.chronometer.base = SystemClock.elapsedRealtime()
////                    val intent = Intent(this, VoicePreviewActivity::class.java)
////                    startActivity(intent)
////                    finish()
//                }
            }
        }

//
//        recordingActivityBinding.playBtn.setOnClickListener {
//            waveRecorder.resumeRecording()
//            Toast.makeText(this, "resumed", Toast.LENGTH_SHORT).show()
//        }
//
//
//        recordingActivityBinding.btnPause.setOnClickListener {
//            if (!isPaused) {
//                waveRecorder.pauseRecording()
//                Toast.makeText(this, "pausedddd", Toast.LENGTH_SHORT).show()
//
//            }
//        }

        recordingActivityBinding.appBarMain.also { layout ->
            layout.findViewById<ImageButton>(R.id.menuButton).also {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    onBackPressed()
                }
                it.setImageResource(R.drawable.ic_back_black)
            }

            layout.findViewById<TextView>(R.id.toolbar_title).text =
                resources.getString(R.string.recording)
        }
    }

//    private fun startRecord() {
//        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
//        spEditor = sp!!.edit()
//        path = File("$filesDir/AdvancedVoiceChanger/")
//        path!!.mkdirs()
//        fileName = "myrecording.pcm"
//        val fileName1 = "myrecordingshare.pcm"
//        spEditor!!.putString("file_name", fileName)
//        spEditor!!.putString("file_nameshare", fileName1)
//        spEditor!!.apply()
//        val file = File(path, fileName!!)
//        val file1 = File(path, fileName1)
//        val path = File("$filesDir/AdvancedVoiceChangerTemp/")
//        if (path.exists()) deleteRecursive(path)
//        if (file.exists()) {
//            file.delete()
//            file1.delete()
//        }
//        try {
//            val outputStream: OutputStream = FileOutputStream(file)
//            val outputStreamshare: OutputStream = FileOutputStream(file1)
//            val bufferedOutputStream = BufferedOutputStream(
//                outputStream
//            )
//            val bufferedOutputStreamshare = BufferedOutputStream(
//                outputStreamshare
//            )
//            val dataOutputStream = DataOutputStream(
//                bufferedOutputStream
//            )
//            val dataOutputStreamshare = DataOutputStream(
//                bufferedOutputStreamshare
//            )
//            val minBufferSize = AudioRecord
//                .getMinBufferSize(
//                    12000, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT
//                )
//            val audioData = ShortArray(minBufferSize)
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            audioRecord = AudioRecord(
//                MediaRecorder.AudioSource.MIC,
//                12000, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, minBufferSize
//            )
//            audioRecord!!.startRecording()
//
//            while (recording) {
//
//
//                val numberOfShort = audioRecord!!.read(
//                    audioData, 0,
//                    minBufferSize
//                )
//                for (i in 0 until numberOfShort) {
//                    dataOutputStream.writeShort(audioData[i].toInt())
//                    dataOutputStreamshare.writeShort(audioData[i].toInt() shr 8 and 255 or (audioData[i].toInt() shl 8))
//                }
//            }
//            audioRecord!!.stop()
//            dataOutputStream.close()
//            dataOutputStreamshare.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }

//    private fun deleteRecursive(fileOrDirectory: File) {
//        if (fileOrDirectory.isDirectory) for (child in fileOrDirectory.listFiles()!!) deleteRecursive(
//            child
//        )
//        fileOrDirectory.delete()
//    }

//    private fun startRecordScopStorage() {
//        sp = getSharedPreferences(MY_PREFS, MODE_PRIVATE)
//        spEditor = sp!!.edit()
//        path = File("$filesDir/AdvancedVoiceChanger/")
//        path!!.mkdirs()
//        fileName = "myrecording.pcm"
//        val fileName1 = "myrecordingshare.pcm"
//        spEditor!!.putString("file_name", fileName)
//        spEditor!!.putString("file_nameshare", fileName1)
//        spEditor!!.apply()
//        val file = File(path, fileName!!)
//        val file1 = File(path, fileName1)
//        try {
//            val outputStream: OutputStream = FileOutputStream(file)
//            val outputStreamshare: OutputStream = FileOutputStream(file1)
//            val bufferedOutputStream = BufferedOutputStream(
//                outputStream
//            )
//            val bufferedOutputStreamshare = BufferedOutputStream(
//                outputStreamshare
//            )
//            val dataOutputStream = DataOutputStream(
//                bufferedOutputStream
//            )
//            val dataOutputStreamshare = DataOutputStream(
//                bufferedOutputStreamshare
//            )
//            val minBufferSize = AudioRecord
//                .getMinBufferSize(
//                    12000, AudioFormat.CHANNEL_IN_MONO,
//                    AudioFormat.ENCODING_PCM_16BIT
//                )
//            val audioData = ShortArray(minBufferSize)
//            if (ActivityCompat.checkSelfPermission(
//                    this,
//                    Manifest.permission.RECORD_AUDIO
//                ) != PackageManager.PERMISSION_GRANTED
//            ) {
//                return
//            }
//            audioRecord = AudioRecord(
//                MediaRecorder.AudioSource.MIC,
//                12000, AudioFormat.CHANNEL_IN_MONO,
//                AudioFormat.ENCODING_PCM_16BIT, minBufferSize
//            )
//            audioRecord!!.startRecording()
//
//            while (recording) {
//                val numberOfShort = audioRecord!!.read(audioData, 0, minBufferSize)
//                for (i in 0 until numberOfShort) {
//                    dataOutputStream.writeShort(audioData[i].toInt())
//                    dataOutputStreamshare.writeShort(audioData[i].toInt() shr 8 and 255 or (audioData[i].toInt() shl 8))
//                }
//            }
//            audioRecord!!.stop()
//            dataOutputStream.close()
//            dataOutputStreamshare.close()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }


    companion object {
        @JvmField
        var MY_PREFS = "Voice_ChangerPrefs"
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }

    override fun onStop() {
        super.onStop()
        if (mediaPlayer != null) {
            myHandler.removeCallbacks(updateSongTime)
            mediaPlayer?.stop()
        }
    }

    private fun stopPlaying() {
        if (mediaPlayer != null) {
            myHandler.removeCallbacks(updateSongTime)
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

        }
    }

    override fun onItemClickForMusic(position: Int, arraylist: ArrayList<RecordingData>) {

        recordingActivityBinding.RlPlayer.visibility = View.VISIBLE

        posForPlayer = position

        stopPlaying()

        mediaPlayer = MediaPlayer.create(this, arraylist[position].fileIri)

        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }


        finalTime = mediaPlayer?.duration?.toLong() ?: 0L
        startTime = mediaPlayer?.currentPosition?.toLong() ?: 0L


        recordingActivityBinding.customPlayer.also { it ->

            seekbar = it.findViewById(R.id.seekBar)

            it.findViewById<ImageButton>(R.id.btn_prev).also { buttonPrevious ->
                buttonPrevious.setOnClickListener { _ ->

                    stopPlaying()

                    posForPlayer -= 1

                    if (posForPlayer < 0) {

                        posForPlayer = 0

                        mediaPlayer = MediaPlayer.create(this, arraylist[0].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }


                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        seekBarFunction()

                        onCompleteListener()


                        adapter?.scrollToPosition(0)

                        recordingActivityBinding.recyclerViewsSavedItems.smoothScrollToPosition(
                            0
                        )

                        setTextForTextViews(it, arraylist)
                    } else {

                        mediaPlayer = MediaPlayer.create(this, arraylist[posForPlayer].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }

                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        seekBarFunction()

                        onCompleteListener()


                        adapter?.scrollToPosition(posForPlayer)

                        recordingActivityBinding.recyclerViewsSavedItems.smoothScrollToPosition(
                            posForPlayer
                        )

                        setTextForTextViews(it, arraylist)
                    }
                }
            }


            it.findViewById<ImageButton>(R.id.btn_next).also { imageButton ->

                imageButton.setOnClickListener { _ ->

                    stopPlaying()

                    posForPlayer += 1

                    if (posForPlayer >= arraylist.size) {

                        posForPlayer = arraylist.size - 1

                        mediaPlayer =
                            MediaPlayer.create(this, arraylist[arraylist.size - 1].fileIri)
                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }
                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        onCompleteListener()


                        seekBarFunction()

                        adapter?.scrollToPosition(arraylist.size - 1)
                        recordingActivityBinding.recyclerViewsSavedItems.smoothScrollToPosition(
                            arraylist.size - 1
                        )

                        setTextForTextViews(it, arraylist)
                    } else {

                        mediaPlayer = MediaPlayer.create(this, arraylist[posForPlayer].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }

                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        onCompleteListener()


                        seekBarFunction()

                        adapter?.scrollToPosition(posForPlayer)

                        recordingActivityBinding.recyclerViewsSavedItems.smoothScrollToPosition(
                            posForPlayer
                        )

                        setTextForTextViews(it, arraylist)
                    }
                }
            }



            tx1 = it.findViewById<TextView>(R.id.tv_total).also { textView ->

                textView.text = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(startTime),
                    TimeUnit.MILLISECONDS.toSeconds(startTime) -
                            TimeUnit.MINUTES.toSeconds(
                                TimeUnit.MILLISECONDS.toMinutes(
                                    startTime
                                )
                            )
                )
            }


            it.findViewById<ImageButton>(R.id.btn_pause).setOnClickListener {


                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    it.findViewById<ImageButton>(R.id.btn_pause).setImageResource(
                        R.drawable.ic_baseline_play_arrow_24
                    )
                } else if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()
                    it.findViewById<ImageButton>(R.id.btn_pause).setImageResource(
                        R.drawable.ic_baseline_pause_24
                    )
                }
            }

            onCompleteListener()

            seekBarFunction()

            setTextForTextViews(it, arraylist)

        }

    }

    private fun setTextForTextViews(
        linearLayout: View,
        arraylist: ArrayList<RecordingData>
    ) {
        linearLayout.findViewById<TextView>(R.id.tv_size).text =
            arraylist[posForPlayer].file_Size.toString() + resources.getString(R.string.kb)


        linearLayout.findViewById<TextView>(R.id.tv_recName).text =
            arraylist[posForPlayer].file_displayName.toString()

        myHandler.postDelayed(updateSongTime, 100)

        linearLayout.findViewById<TextView>(R.id.tv_duration).text = String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(finalTime),
            TimeUnit.MILLISECONDS.toSeconds(finalTime) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                            finalTime
                        )
                    )
        )
    }

    private val updateSongTime: Runnable = object : Runnable {
        override fun run() {

            startTime = mediaPlayer?.currentPosition!!.toLong()
            tx1.text = String.format(
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(startTime),
                TimeUnit.MILLISECONDS.toSeconds(startTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime))
            )
            seekbar.progress = startTime.toInt()
            myHandler.postDelayed(this, 100)

        }
    }

    private fun onCompleteListener() {
        mediaPlayer?.setOnCompletionListener {
            recordingActivityBinding.RlPlayer.visibility = View.GONE
        }

    }

    private fun seekBarFunction() {
        seekbar.also { seekbar ->

            seekbar.max = finalTime.toInt()

            seekbar.thumb.mutate().alpha = 0
            seekbar.progress = startTime.toInt()

            seekbar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                    mediaPlayer?.seekTo(p0!!.progress)
                }

            })

        }
    }

    override fun onItemClickFroRefresh() {
        getFiles()
        adapter?.updateList()
    }
}