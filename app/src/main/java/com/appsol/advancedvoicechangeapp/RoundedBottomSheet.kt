package com.appsol.advancedvoicechangeapp


import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.media.RingtoneManager
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.appsol.advancedvoicechangeapp.adapter.MusicAdapter
import com.appsol.advancedvoicechangeapp.databinding.ActivityBottomsheetBinding
import com.appsol.advancedvoicechangeapp.model.RecordingData
import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment
import java.io.File

class RoundedBottomSheet(
    private val musicAdapter: MusicAdapter,
    val arraylist: ArrayList<RecordingData>,
    val position: Int
) : RoundedBottomSheetDialogFragment() {

    private lateinit var activityBottomSheetBinding: ActivityBottomsheetBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        activityBottomSheetBinding = DataBindingUtil.setContentView(
            context as Activity,
            R.layout.activity_bottomsheet
        )

        return activityBottomSheetBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        setTextData()

        setButtonClickListeners()
    }


    private fun setButtonClickListeners() {

        activityBottomSheetBinding.actionBottomRename.setOnClickListener {

            musicAdapter.rename(arraylist[position].file_displayName)
            this.dismiss()
        }

        activityBottomSheetBinding.actionBottomDelete.setOnClickListener {

            musicAdapter.onDelete(arraylist[position].file_displayName, position)
            this.dismiss()

        }

        activityBottomSheetBinding.actionBottomAdd.setOnClickListener {
            musicAdapter.addEffects(
                arraylist[position].fileIri,
                arraylist[position].file_displayName
            )
            this.dismiss()

        }

        activityBottomSheetBinding.actionBottomRingtone.setOnClickListener {
            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath + "/AdvancedVoiceChanger/", arraylist[position].file_displayName
            )

            setRingTune(file.absolutePath)

        }

        activityBottomSheetBinding.actionBottomShare.setOnClickListener {

            val file = File(
                Environment.getExternalStorageDirectory()
                    .absolutePath + "/AdvancedVoiceChanger/", arraylist[position].file_displayName
            )

            sendMail(file.absolutePath)

        }
    }

    private fun setTextData() {

        val dateInString = Utils.getStringForDate(arraylist[position].file_date)
        val durationInString = Utils.getStringForSeconds(arraylist[position].file_duration)
        val sizeInString = Utils.getStringForSize(arraylist[position].file_Size)
        activityBottomSheetBinding.tvRecName.text = arraylist[position].file_displayName
        activityBottomSheetBinding.tvRecDate.text = dateInString
        activityBottomSheetBinding.tvRecDuration.text = durationInString
        activityBottomSheetBinding.tvRecSize.text = sizeInString

    }

    private fun setRingTune(filepath: String) {

        val content = ContentValues()
        content.put(MediaStore.MediaColumns.DATA, filepath)
        content.put(MediaStore.MediaColumns.TITLE, "ring")
        content.put(MediaStore.MediaColumns.SIZE, filepath)
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*")
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true)
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true)
        content.put(MediaStore.Audio.Media.IS_ALARM, false)
        content.put(MediaStore.Audio.Media.IS_MUSIC, false)

        val uri = MediaStore.Audio.Media.getContentUriForPath(
            filepath
        )
        context?.contentResolver?.delete(
            uri!!, MediaStore.MediaColumns.DATA + "=\"" + filepath + "\"",
            null
        )
        val newUri = uri?.let { context?.contentResolver?.insert(it, content) }
        RingtoneManager.setActualDefaultRingtoneUri(
            context?.applicationContext, RingtoneManager.TYPE_RINGTONE,
            newUri
        )
        Toast.makeText(
            context,
            StringBuilder().append("Ringtone set successfully"),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun sendMail(fileName: String?) {
        val sendIntent = Intent(Intent.ACTION_SEND)
        //        sendIntent.putExtra(Intent.EXTRA_SUBJECT, fileName);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "" + getString(R.string.app_name))
        val file = fileName?.let { File(it) }
        val contentUri =
            file?.let {
                FileProvider.getUriForFile(
                    context as Activity,
                    "${context?.packageName}.fileprovider",
                    it
                )
            }
        sendIntent.putExtra(
            Intent.EXTRA_STREAM,
            contentUri
        )
        sendIntent.type = "audio/3gpp"
        startActivity(Intent.createChooser(sendIntent, "Share Recording"))
    }


}