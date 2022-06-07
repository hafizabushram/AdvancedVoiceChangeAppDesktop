package com.appsol.advancedvoicechangeapp.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.SpacesItemDecoration
import com.appsol.advancedvoicechangeapp.adapter.MusicAdapter
import com.appsol.advancedvoicechangeapp.adapter.MusicInterface
import com.appsol.advancedvoicechangeapp.databinding.ActivityMyAudioFileBinding
import com.appsol.advancedvoicechangeapp.model.RecordingData
import com.appsol.advancedvoicechangeapp.ratingdailog.RatingDialog
import com.appsol.advancedvoicechangeapp.utils.FacebookAdsInterstitialAd
import com.appsol.advancedvoicechangeapp.utils.SharedPrefs
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit

class MyAudioFileActivity : AppCompatActivity(), MusicInterface, MusicAdapter.RefreshData {


    val myHandler = Handler()

    private lateinit var seekbar: SeekBar


    private var startTime = 0L

    private var finalTime = 0L

    private var posForPlayer = -1

    private lateinit var tx1: TextView

    private lateinit var myAudioFileBinding: ActivityMyAudioFileBinding


    private var mediaPlayer: MediaPlayer? = null

    var adapter: MusicAdapter? = null

    private var dataList = ArrayList<String>()
    private var nameList = ArrayList<RecordingData>()

    private var kept: String? = null

    private var reviewManager: ReviewManager? = null
    private var ratingDialog: RatingDialog? = null
    private var sharedPrefs: SharedPrefs? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        myAudioFileBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_audio_file)

        myAudioFileBinding.appBarMain.also { layout ->
            layout.findViewById<RelativeLayout>(R.id.RL_appbar)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPurple))

            layout.findViewById<ImageButton>(R.id.menuButton).also {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    onBackPressed()
                }
                it.setImageResource(R.drawable.ic_back)
            }

            layout.findViewById<TextView>(R.id.toolbar_title).also {
                it.setTextColor(ContextCompat.getColor(this, R.color.white))
                it.text = resources.getString(R.string.my_recordings)
            }
        }

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
                showRateUsDialog()
                sharedPrefs!!.setRatingDailogValue(this, 3)
            } else sharedPrefs!!.setRatingDailogValue(this, sharedPrefs!!.ratingDailogValue + 1)
        }
    }


    private fun getFiles() {
        nameList.clear()
        myAudioFileBinding.listViewPlay.adapter = null
        try {
            val dir: File =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) File(kept + File.separator + Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger/") else File(
                    "$kept/AdvancedVoiceChanger/"
                )
            val list = dir.listFiles()
            Log.d("TAG", "before k:" + Arrays.toString(list))
            if (list != null)
                for (f in list) {
                    val uri = Uri.fromFile(f)
                    dataList.add(uri.toString())
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(uri.path)
                    val durationStr: String? =
                        mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    val seconds: String =
                        java.lang.String.valueOf(durationStr!!.toInt() % 60000 / 1000)
                    Log.d("seconds===========>", seconds)
                    Log.d("hbs_Code", "getFiles: $seconds")

                    val fileSize: Int = java.lang.String.valueOf(f.length() / 1024).toInt()

                    if (fileSize != 0) {
                        nameList.add(
                            RecordingData(
                                seconds.toLong(), f.name,
                                f.lastModified(), fileSize.toLong(), uri
                            )
                        )
                    }

                    Log.d("TAG", "uri:$uri")
                }
        } catch (ignored: java.lang.Exception) {
        }
        with(nameList) { reverse() }
        adapter =
            MusicAdapter(this, nameList, kept, this, this, true)

        if (nameList.isEmpty()) {
            myAudioFileBinding.RLNoAudio.visibility = View.VISIBLE
        } else {
            myAudioFileBinding.listViewPlay.also {
                it.adapter = adapter
                myAudioFileBinding.RLNoAudio.visibility = View.GONE
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


//    private fun getFiles() {
//        nameList.clear()
//        myAudioFileBinding.listViewPlay.adapter = null
////        try {
//        val dir: File =
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) File(
//                kept + File.separator + Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger/"
//            ) else File(
//                "$kept/AdvancedVoiceChanger/"
//            )
//        val list = dir.listFiles()
//
//        Log.d("TAG", "before k:" + Arrays.toString(list))
//        if (list != null)
//            for (f in list) {
//                if (f != null) {
//
//                    val uri = Uri.fromFile(f)
//                    if (uri != null) {
//                        dataList.add(uri.toString())
//
//                        val mmr = MediaMetadataRetriever()
//                        try {
//                            mmr.setDataSource(uri.toString(), HashMap())
//                            val durationStr: String? =
//                                mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
//                            val seconds: String =
//                                java.lang.String.valueOf(durationStr!!.toInt() % 60000 / 1000)
//                            Log.d("seconds===========>", seconds)
//
//                            val fileSize: Int = java.lang.String.valueOf(f.length() / 1024).toInt()
//
//                            Log.d("TAG", "uri:$uri")
//                            nameList.add(
//                                RecordingData(
//                                    seconds.toLong(), f.name,
//                                    f.lastModified(), fileSize.toLong(), uri
//                                )
//                            )
//                        } catch (ex: RuntimeException) {
//                            Toast.makeText(this, "" + ex.message, Toast.LENGTH_SHORT).show()
//                        }
//                    } else {
//                        Toast.makeText(
//                            this,
//                            "nulllllllllllllllllllllllllllllllllllll",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//
//                }
//
//            }
//
////        } catch (ignored: java.lang.Exception) {
////        }
//        with(nameList) { reverse() }
////        if (nameList.isEmpty())
////        {
////            myAudioFileBinding.RLNoAudio.visibility=View.VISIBLE
////        }
////        else
////        {
//        adapter = MusicAdapter(this, nameList, kept, this, false)
//        myAudioFileBinding.listViewPlay.adapter = adapter
//        myAudioFileBinding.listViewPlay.isNestedScrollingEnabled = false
//        val spacingForH = resources.getDimensionPixelSize(R.dimen._2sdp)
//        val spacingForV = resources.getDimensionPixelSize(R.dimen._6sdp)
//        myAudioFileBinding.listViewPlay.addItemDecoration(
//            SpacesItemDecoration(
//                spacingForH,
//                spacingForV
//            )
//        )
////        }
//    }

    private fun stopPlaying() {
        if (mediaPlayer != null) {
            mediaPlayer?.stop()
            myHandler.removeCallbacks(updateSongTime)
            mediaPlayer?.release()
            mediaPlayer = null

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

            startTime = mediaPlayer!!.currentPosition.toLong()
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

            adapter?.scrollToNoPosition(-1)

            myAudioFileBinding.RlPlayer.visibility = View.GONE
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

    override fun onBackPressed() {
        if (mediaPlayer != null && mediaPlayer?.isPlaying == true) {
            stopPlaying()

            myHandler.removeCallbacks(updateSongTime)

        }
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showRateUsDialog() {
        ratingDialog = RatingDialog.Builder(this)
            .threshold(4f)
            .session(1)
            .onRatingBarFormSumbit { feedback ->
                if (feedback != "") sendEmail(
                    feedback!!
                )
            }
            .onThresholdCleared { ratingDialog, _, _ ->
                showRateApp()
                sharedPrefs!!.setRatingDailogValue(this@MyAudioFileActivity, 6)
                ratingDialog!!.dismiss()
            }
            .build()
        ratingDialog!!.show()
    }

    private fun sendEmail(string: String) {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("advappsoltech@gmail.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name) + " Feedback")
        intent.putExtra(Intent.EXTRA_TEXT, string)
        startActivity(Intent.createChooser(intent, "Send Email"))
    }

    private fun showRateApp() {
        val request = reviewManager!!.requestReviewFlow()
        request.addOnCompleteListener { task: Task<ReviewInfo?> ->
            if (task.isSuccessful) {
                // We can get the ReviewInfo object
                val reviewInfo: ReviewInfo = task.result
                val flow: Task<Void?> = reviewManager!!.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { }
            } else {
                openPlayStore()
            }
        }
    }

    private fun openPlayStore() {
        val playStoreUrl = "market://details?id=$packageName"
        val marketUri = Uri.parse(playStoreUrl)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (ex: ActivityNotFoundException) {
        }
    }

    override fun onItemClickForMusic(position: Int, arraylist: ArrayList<RecordingData>) {


        myAudioFileBinding.RlPlayer.visibility = View.VISIBLE

        posForPlayer = position

        stopPlaying()


        mediaPlayer = MediaPlayer.create(this, arraylist[position].fileIri)

        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }


        finalTime = mediaPlayer?.duration?.toLong() ?: 0L
        startTime = mediaPlayer?.currentPosition?.toLong() ?: 0L


        myAudioFileBinding.customPlayer.also { linearLayout ->

            seekbar = linearLayout.findViewById(R.id.seekBar)

            linearLayout.findViewById<ImageButton>(R.id.btn_prev).also { buttonPrevious ->
                buttonPrevious.setOnClickListener {

                    posForPlayer -= 1


                    stopPlaying()



                    if (posForPlayer < 0) {

                        posForPlayer = 0

                        mediaPlayer = MediaPlayer.create(this, arraylist[0].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }


                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        seekBarFunction()

                        onCompleteListener()


                        adapter?.scrollToPosition(0)
                        myAudioFileBinding.listViewPlay.smoothScrollToPosition(
                            0
                        )


                        setTextForTextViews(linearLayout, arraylist)
                    } else {


                        mediaPlayer = MediaPlayer.create(this, arraylist[posForPlayer].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }


                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        seekBarFunction()

                        onCompleteListener()


                        adapter?.scrollToPosition(posForPlayer)

                        myAudioFileBinding.listViewPlay.smoothScrollToPosition(
                            posForPlayer
                        )

                        setTextForTextViews(linearLayout, arraylist)
                    }


                }
            }


            linearLayout.findViewById<ImageButton>(R.id.btn_next).also { imageButton ->

                imageButton.setOnClickListener {

                    posForPlayer += 1


                    stopPlaying()

                    if (posForPlayer >= arraylist.size) {

                        posForPlayer = arraylist.size - 1

                        mediaPlayer =
                            MediaPlayer.create(this, arraylist[arraylist.size - 1].fileIri)
                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }
                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        onCompleteListener()


                        seekBarFunction()

                        adapter?.scrollToPosition(arraylist.size - 1)

                        myAudioFileBinding.listViewPlay.smoothScrollToPosition(
                            arraylist.size - 1
                        )

                        setTextForTextViews(linearLayout, arraylist)
                    } else {

                        mediaPlayer = MediaPlayer.create(this, arraylist[posForPlayer].fileIri)

                        mediaPlayer?.setOnPreparedListener { p0 -> p0?.start() }
                        finalTime = mediaPlayer?.duration?.toLong() ?: 0L


                        onCompleteListener()


                        seekBarFunction()

                        adapter?.scrollToPosition(posForPlayer)

                        myAudioFileBinding.listViewPlay.smoothScrollToPosition(
                            posForPlayer
                        )

                        setTextForTextViews(linearLayout, arraylist)
                    }


                }
            }



            tx1 = linearLayout.findViewById<TextView>(R.id.tv_total).also { textView ->

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


            linearLayout.findViewById<ImageButton>(R.id.btn_pause).setOnClickListener {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    linearLayout.findViewById<ImageButton>(R.id.btn_pause).setImageResource(
                        R.drawable.ic_baseline_play_arrow_24
                    )
                } else if (mediaPlayer?.isPlaying == false) {
                    mediaPlayer?.start()

                    linearLayout.findViewById<ImageButton>(R.id.btn_pause).setImageResource(
                        R.drawable.ic_baseline_pause_24
                    )
                }
            }

            onCompleteListener()

            seekBarFunction()

            setTextForTextViews(linearLayout, arraylist)
        }
    }

    override fun onItemClickFroRefresh() {
        getFiles()
        adapter?.updateList()
    }

    override fun onResume() {
        super.onResume()
        getFiles()
    }
}