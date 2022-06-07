package com.appsol.advancedvoicechangeapp.activity

import android.Manifest
import android.app.Dialog
import android.app.DownloadManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.BillingProcessor.IBillingHandler
import com.anjlab.android.iab.v3.TransactionDetails
import com.appsol.advancedvoicechangeapp.MyApp
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.SpacesItemDecoration
import com.appsol.advancedvoicechangeapp.Utils
import com.appsol.advancedvoicechangeapp.adapter.CatalogListAdapter
import com.appsol.advancedvoicechangeapp.databinding.ActivityMainBinding
import com.appsol.advancedvoicechangeapp.model.GetSetAudio
import com.appsol.advancedvoicechangeapp.ratingdailog.RatingDialog
import com.appsol.advancedvoicechangeapp.utils.AdMobInterstitial
import com.appsol.advancedvoicechangeapp.utils.SharedPrefs
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs.OnAdsFree
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import net.surina.soundtouch.SoundStreamAudioPlayer
import java.io.*


class MainActivity : AppCompatActivity(), OnAdsFree, CatalogListAdapter.EffectsClickListener {


    private var clickedPosition: Int = -1
    private lateinit var mainBinding: ActivityMainBinding

    private var mPlayer = MediaPlayer()

    private var itemsArray: ArrayList<GetSetAudio> = ArrayList()

    private var player: SoundStreamAudioPlayer? = null


    private var productId: String? = null
    private lateinit var licenseKey: String
    private var bp: BillingProcessor? = null
    private var readyToPurchase = false
    private var sharedPrefs: SharedPrefs? = SharedPrefs(this)
    private var purchaseDialog: Dialog? = null
    private var exitDialog: Dialog? = null
    private var isFromRecord = false
    private var reviewManager: ReviewManager? = null
    private var ratingDialog: RatingDialog? = null

    private var isFrom = false
    var sp: SharedPreferences? = null


    private var catalogAdaptersObject: CatalogListAdapter? = null

    private var isFromPick = false

    private var isForAudio = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        init()

        AdMobInterstitial.getAdmobAds(this, findViewById(R.id.bannerContainer))

    }

    private fun init() {
        licenseKey = resources.getString(R.string.licenseKey)

        reviewManager = ReviewManagerFactory.create(this)

        isFrom = intent.getBooleanExtra("isSaved", false)

        initExitDialog()

        setRecyclerViewData()

        setClickListeners()

        initAdsFreeBilling()

        mPlayer = MediaPlayer.create(this, R.raw.democatalog)

        productId = resources.getString(R.string.play_product_id)

        mainBinding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)


    }

    private fun setRecyclerViewData() {
        itemsArray = Utils.getAudioItemsArray()
        catalogAdaptersObject =
            CatalogListAdapter(
                this,
                itemsArray
            )
        catalogAdaptersObject!!.setClickListener(this)
        mainBinding.catalogRecyclerView.also {
            it.layoutManager =
                GridLayoutManager(this, 3)
            it.adapter = catalogAdaptersObject

            val spacingForH = resources.getDimensionPixelSize(R.dimen._4sdp)
            val spacingForV = resources.getDimensionPixelSize(R.dimen._8sdp)
            it.addItemDecoration(
                SpacesItemDecoration(
                    spacingForH,
                    spacingForV
                )
            )
        }


    }

    private fun setClickListeners() {
        mainBinding.micAnimationMain.setOnClickListener {
            isFromRecord = true
            if (isRecordPermissionGranted) {
                startActivity(Intent(this@MainActivity, RecordingActivity::class.java))
                showAds()

            }
        }

        mainBinding.imgTextToSpeech.setOnClickListener {
            startActivity(Intent(this@MainActivity, TextToSpeechActivity::class.java))
//            FacebookAdsInterstitialAd.getDefaultInstance(this).showFbInterstistial()
        }


        mainBinding.imgOpenGellery.setOnClickListener {
            isFromPick = false
            isForAudio = false
            isFromRecord = false
            if (isRecordPermissionGranted) {
                startActivity(Intent(this@MainActivity, MyAudioFileActivity::class.java))
//                FacebookAdsInterstitialAd.getDefaultInstance(this).showFbInterstistial()
            }
        }

        mainBinding.imgAdsFree.setOnClickListener { purchaseDialogSec }

        mainBinding.imgPickAudio.setOnClickListener {
            isFromPick = true
            isFromRecord = false
            if (isRecordPermissionGranted) {
                startActivity(Intent(this@MainActivity, AudioFilesReadActivity::class.java))
            }
        }

        mainBinding.appBarMain.findViewById<ImageButton>(R.id.menuButton).setOnClickListener {
            mainBinding.drawerLayout.openDrawer(Gravity.LEFT)
        }

        mainBinding.actionShare.setOnClickListener {
            if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            try {

                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "Hey check out this beautiful " + getString(R.string.app_name) + " app at: https://play.google.com/store/apps/details?id=" + packageName
                )
                sendIntent.type = "text/plain"
                startActivity(sendIntent)
            } catch (ignored: Exception) {
            }
        }

        mainBinding.actionRate.setOnClickListener {
            if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
            showRateUsDialog()

        }

        mainBinding.actionPolicy.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://advanceappsologics.blogspot.com/2020/11/privacy-policy-of-voice-changer-voice.html")
                    )
                )
                if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
                }
            } catch (ignored: ActivityNotFoundException) {
            }
        }


        mainBinding.actionMoreApps.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Advance+Appsol+Techonologies")
                    )
                )
                if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
                }

            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/developer?id=Advance+Appsol+Techonologies")
                    )
                )
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (isFrom) {
            isFrom = false
            if (sharedPrefs!!.ratingDailogValue == 5) {
                sharedPrefs!!.setRatingDailogValue(this, 3)
            } else {
                sharedPrefs!!.setRatingDailogValue(this, sharedPrefs!!.ratingDailogValue + 1)
                if (!sharedPrefs!!.premium!! && sharedPrefs!!.purchaeDailogValue <= 0) {
                    sharedPrefs!!.setPurchaeDailogValue(this, 4)
                    purchaseDialogSec
                } else {
                    sharedPrefs!!.setPurchaeDailogValue(
                        this,
                        sharedPrefs!!.purchaeDailogValue - 1
                    )
                }
            }
        }
//        } else if (!sharedPrefs!!.premium!! && sharedPrefs!!.purchaeDailogValue <= 0) {
//            sharedPrefs!!.setPurchaeDailogValue(this, 4)
//            purchaseDialogSec
//        }
        else {
            sharedPrefs!!.setPurchaeDailogValue(this, sharedPrefs!!.purchaeDailogValue - 1)
        }
    }

    private fun showAds() {
        if (adsCount >= 3) {
            AdMobInterstitial.getDefaultInstance(this@MainActivity)
                .showInterstitialAd(this@MainActivity)
            adsCount = 1
        } else adsCount++
    }

    private val purchaseDialogSec: Unit
        get() {
            if (ratingDialog != null && ratingDialog!!.isShowing) return
            if (purchaseDialog == null) {
                purchaseDialog = VoiceChangerDialogs.createPurchaseDailog(this, this)
                purchaseDialog!!.show()
            } else if (!purchaseDialog!!.isShowing) {
                purchaseDialog!!.show()
            }
        }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp!!.handleActivityResult(requestCode, resultCode, data)) super.onActivityResult(
            requestCode,
            resultCode,
            data
        )
    }

    override fun onDestroy() {
        if (bp != null) bp!!.release()
        super.onDestroy()
    }

    private fun initAdsFreeBilling() {
        if (sharedPrefs!!.premium == true) {
            findViewById<View>(R.id.imgAdsFree).visibility = View.INVISIBLE
        } else {
            findViewById<View>(R.id.imgAdsFree).visibility = View.VISIBLE
        }
        bp = BillingProcessor(this, licenseKey, object : IBillingHandler {
            override fun onProductPurchased(productId: String, details: TransactionDetails?) {
                //Update shared References according to App
                sharedPrefs!!.premium = true
            }

            override fun onBillingError(errorCode: Int, error: Throwable?) {}
            override fun onBillingInitialized() {
                readyToPurchase = true
            }

            override fun onPurchaseHistoryRestored() {
                for (sku in bp!!.listOwnedProducts()) {
                    if (sku == productId) {
                        sharedPrefs!!.premium = true
                    }
                }
            }
        })
    }

    private val isRecordPermissionGranted: Boolean
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ActivityCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.RECORD_AUDIO
                )
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REOCORD
                )
                false
            }
        } else
            if (ActivityCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.RECORD_AUDIO
                )
                == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ), PERMISSION_REOCORD
                )
                false
            }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REOCORD) { // If request is cancelled, the result arrays are empty.
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                when {
                    isFromRecord -> startActivity(
                        Intent(
                            this@MainActivity,
                            RecordingActivity::class.java
                        )
                    )

                    isFromPick -> {
                        startActivity(
                            Intent(this@MainActivity, AudioFilesReadActivity::class.java)
                        )
                    }

                    isForAudio -> {
                        catalogAdaptersObject?.selectedState(clickedPosition)
                        playSound(clickedPosition)
                    }
                    else -> startActivity(
                        Intent(
                            this@MainActivity,
                            MyAudioFileActivity::class.java
                        )
                    )
                }

                showAds()
                (applicationContext as MyApp).initAppOpenAds()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Some Features may not work properly, manually grant permissions in Settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onClickPurchase() {
        if (!readyToPurchase) {
            Toast.makeText(
                this@MainActivity,
                "Something went wrong, please check internet or try again later",
                Toast.LENGTH_LONG
            ).show()
        } else {
            bp!!.purchase(this@MainActivity, productId)
            val consumed = bp!!.consumePurchase(productId)
            if (consumed) {
                sharedPrefs!!.premium = true
                Toast.makeText(this@MainActivity, "Consumed Successfully", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun initExitDialog() {
        exitDialog = VoiceChangerDialogs.createExitDialog(this, true) { clearActivities() }
        exitDialog!!.setOnCancelListener { clearActivities() }
    }

    private fun clearActivities() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(homeIntent)
        finish()
    }

    override fun onBackPressed() {
        if (mainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            mainBinding.drawerLayout.closeDrawer(GravityCompat.START)
        } else if (exitDialog != null && !exitDialog!!.isShowing) exitDialog!!.show()
    }

    private fun showRateUsDialog() {
        if (purchaseDialog != null) purchaseDialog!!.dismiss()
        ratingDialog = RatingDialog.Builder(this)
            .threshold(4f)
            .session(1)
            .onRatingBarFormSumbit { feedback -> if (feedback != "") sendEmail(feedback!!) }
            .onThresholdCleared { ratingDialog, _, _ ->
                openPlayStore()
//                sharedPrefs!!.setRatingDailogValue(this@MainActivity, 6)
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


    private fun openPlayStore() {
        val playStoreUrl = "market://details?id=$packageName"
        val marketUri = Uri.parse(playStoreUrl)
        try {
            startActivity(Intent(Intent.ACTION_VIEW, marketUri))
        } catch (ex: ActivityNotFoundException) {
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            player!!.stop()
        }
    }

    companion object {
        private const val PERMISSION_REOCORD = 4
        private var adsCount = 1

        var MY_PREFS = "Voice_ChangerPrefs"
    }


    private fun playSound(position: Int) {
        if (player != null) player!!.stop()

        val path = Environment.getExternalStoragePublicDirectory(
            DIRECTORY_DOWNLOADS
        )
        if (path.isDirectory) {
            try {
                val sound =
                    FileOutputStream("$path/advance_voice_demo_sound.mp3")
                val `is`: InputStream = resources.openRawResource(R.raw.democatalog)
                val a: Int = `is`.available()
                val buf = ByteArray(a)
                `is`.read(buf, 0, a)
                sound.write(buf)
                sound.flush()
                sound.close()

                val file = File(path, "advance_voice_demo_sound.mp3")
                if (!file.exists()) {

                    val downloadManager: DownloadManager =
                        getSystemService(DOWNLOAD_SERVICE) as DownloadManager

                    downloadManager.addCompletedDownload(
                        "advanceVoice.mp3",
                        "myDescription",
                        true,
                        "audio/mpeg",
                        file.absolutePath,
                        file.length(),
                        false
                    )
                }

                player = SoundStreamAudioPlayer(
                    0,
                    file.toString(),
                    0.01f * itemsArray[position].tempo,
                    itemsArray[position].pitch
                )
                Thread(player).start()
                if (player != null) {
                    player!!.start()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onItemClickPlay(position: Int) {
        clickedPosition = position
        isFromPick = false

        isFromRecord = false

        isForAudio = true


        if (isRecordPermissionGranted)

//            if (player != null && player!!.isPaused) {
//                player!!.start()
//            } else
        {
            playSound(position)
            catalogAdaptersObject?.selectedState(position)
        }
//                if (!player!!.isPaused) {


//                }
//            if (player != null && player!!.isPaused) {
//                    player!!.start()
//                } else if (player!=null && !player!!.isFinished) {
//                    playSound(position)
//                }
    }

    override fun onItemClickPause(position: Int, viewHolder: CatalogListAdapter.ViewHolder) {
        if (player != null && !player!!.isPaused) {
            player!!.pause()
        } else if (player != null && player!!.isPaused) {
            catalogAdaptersObject?.resumePlayer(viewHolder, position)
            player!!.start()
        }
    }


}