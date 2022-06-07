package com.appsol.advancedvoicechangeapp.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.SpacesItemDecoration
import com.appsol.advancedvoicechangeapp.adapter.AudioItemsAdapter
import com.appsol.advancedvoicechangeapp.databinding.ActivityAudioFilesReadBinding
import com.appsol.advancedvoicechangeapp.model.MusicData
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import java.io.File
import java.util.concurrent.Executors

class AudioFilesReadActivity : AppCompatActivity() {
    private var mEditorMusicData: ArrayList<MusicData>? = null
    private var audioItemsAdapter: AudioItemsAdapter? = null
    private var mDataList: ArrayList<MusicData>? = ArrayList()
    private var editorFilename = "record"


    private lateinit var audioFilesReadBinding: ActivityAudioFilesReadBinding

    override fun onResume() {
        super.onResume()
        mLoadAllMusicFiles()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        audioFilesReadBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_audio_files_read)


        audioFilesReadBinding.appBarMain.also{ layout ->

            layout.findViewById<RelativeLayout>(R.id.RL_appbar)
                .setBackgroundColor(ContextCompat.getColor(this, R.color.colorPurple))
            layout.findViewById<View>(R.id.menuSearchButton).also {
                it.visibility = View.VISIBLE
                it.setOnClickListener {
                    audioFilesReadBinding.linearLayout2.visibility = View.VISIBLE
                }
            }

            layout.findViewById<ImageButton>(R.id.menuButton).also {
                it.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.ic_back
                    )
                )
                it.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
                it.setOnClickListener {
                    onBackPressed()
                }

            }

            layout.findViewById<TextView>(R.id.toolbar_title).also {

                it.text =
                resources.getString(R.string.importAudio)
                it.setTextColor(ContextCompat.getColor(this,R.color.white))
            }
        }



        audioFilesReadBinding.etSearch.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {
                audioFilesReadBinding.etSearch.clearFocus()
                val `in` = this.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                `in`.hideSoftInputFromWindow(audioFilesReadBinding.etSearch.windowToken, 0)
                if (audioFilesReadBinding.etSearch.text.toString()
                        .isNotEmpty() && audioFilesReadBinding.etSearch.text.toString().length <= 2
                )
                    search(audioFilesReadBinding.etSearch.text.toString())

                return@OnEditorActionListener true

            }
            false
        })


        audioFilesReadBinding.etSearch.setOnClickListener {
            audioFilesReadBinding.etSearch.requestFocus()
            audioFilesReadBinding.etSearch.isFocusable = true

            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(
                audioFilesReadBinding.etSearch,
                0
            )
        }


        audioFilesReadBinding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()) {
                    search("")
                }
                if (s.length >= 2)
                    search(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }

            override fun afterTextChanged(s: Editable) {}
        })


        //        }
        mLoadAllMusicFiles()
        AdMobInterstitial.getAdmobAds(this, findViewById(R.id.bannerContainer))
    }


    private fun search(searched: String?) {
        if (searched != null && searched != "") {
            mDataList?.clear()
            for (ob in mEditorMusicData!!) {
                if (ob.track_displayName.contains(searched, true))
                    mDataList?.add(ob)

            }
            audioItemsAdapter?.setLabelsList(mDataList)
            if (mDataList!!.size == 0) audioFilesReadBinding.noRecord.visibility =
                View.VISIBLE else audioFilesReadBinding.noRecord.visibility =
                View.GONE
        } else {
            mDataList!!.clear()
            mEditorMusicData.let {
                if (it != null) {
                    mDataList!!.addAll(it)
                }
            }
            audioItemsAdapter?.setLabelsList(mDataList)
            if (mDataList!!.size == 0)
                audioFilesReadBinding.noRecord.visibility = View.VISIBLE
            else audioFilesReadBinding.noRecord.visibility = View.GONE
        }
    }

    private fun mLoadAllMusicFiles() {
        val handler = Handler(Looper.getMainLooper())
        val progressDialog = ProgressDialog(this@AudioFilesReadActivity)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Loading music...")
        progressDialog.show()
        Executors.newSingleThreadExecutor().execute {
            mEditorMusicData = musicFiles
            editorFilename = if (mEditorMusicData!!.size > 0) {
                mEditorMusicData!![0].getTrack_data()
            } else {
                "record"
            }
            handler.post {
                if (editorFilename != "record") {
                    mediaFiles
                } else if (mEditorMusicData!!.size > 0) {
                    Toast.makeText(
                        applicationContext, """
     No Music found in device
     Please add music in sdCard
     """.trimIndent(), Toast.LENGTH_LONG
                    ).show()
                }
                progressDialog.dismiss()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        } catch (e: Exception) {
            Log.d("TAG", "onDestroy: ")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        } catch (e: Exception) {
            Log.d("TAG", "onDestroy: ")
        }
    }

    private val musicFiles: ArrayList<MusicData>
        get() {
            val musicNewData: ArrayList<MusicData> = ArrayList()
            val mCursor = contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                arrayOf("_id", "title", "_data", "_display_name", "duration", "_size"),
                "is_music != 0",
                null,
                "title ASC"
            )
            val trackId = mCursor!!.getColumnIndex("_id")
            val trackTitle = mCursor.getColumnIndex("title")
            val trackDisplayName = mCursor.getColumnIndex("_display_name")
            val trackData = mCursor.getColumnIndex("_data")
            val trackDuration = mCursor.getColumnIndex("duration")
            val size = mCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (mCursor.moveToNext()) {
                val path = mCursor.getString(trackData)
                if (isAudioFile(path) && !path.endsWith(".opus")) {
                    val musicData = MusicData()
                    musicData.track_Id = mCursor.getLong(trackId)
                    musicData.track_Title = mCursor.getString(trackTitle)
                    musicData.track_data = path
                    musicData.track_duration = mCursor.getLong(trackDuration)
                    musicData.track_displayName = mCursor.getString(trackDisplayName)
                    musicData.setTrack_Size(size.toLong())
                    val file = File(path)
                    if (file.length() > 2) musicNewData.add(musicData)
                }
            }
            mCursor.close()
            return musicNewData
        }

    private fun isAudioFile(path: String): Boolean {
        return !TextUtils.isEmpty(path)
    }

    private val mediaFiles: Unit
        get() {
            audioItemsAdapter =
                AudioItemsAdapter(mEditorMusicData!!, this@AudioFilesReadActivity)
            audioFilesReadBinding.recylerView.also {
                it.layoutManager = GridLayoutManager(this@AudioFilesReadActivity, 1)
                it.itemAnimator = DefaultItemAnimator()
                it.adapter = audioItemsAdapter

                val spacingForV = resources.getDimensionPixelSize(R.dimen._2sdp)
                it.addItemDecoration(
                    SpacesItemDecoration(
                        0,
                        spacingForV
                    )
                )
            }
        }
}