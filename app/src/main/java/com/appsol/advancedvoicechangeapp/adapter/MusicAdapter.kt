package com.appsol.advancedvoicechangeapp.adapter

import android.app.Activity
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.appsol.advancedvoicechangeapp.R
import com.appsol.advancedvoicechangeapp.RoundedBottomSheet
import com.appsol.advancedvoicechangeapp.Utils
import com.appsol.advancedvoicechangeapp.activity.AudioEffectsCustomActivity
import com.appsol.advancedvoicechangeapp.activity.MainActivity
import com.appsol.advancedvoicechangeapp.model.RecordingData
import com.appsol.advancedvoicechangeapp.utils.VoiceChangerDialogs
import java.io.File

class MusicAdapter(
    private var mContext: Context,
    var arraylist: ArrayList<RecordingData>,
    private val kept: String?,
    private val musicClickListenerObject: MusicInterface,
    private val refreshListener: RefreshData,
    private val comingFrom: Boolean
) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>(),
    VoiceChangerDialogs.OnAdsFree,
    VoiceChangerDialogs.RenameInterFace,
    forBottomSheetCallBacks {
    var rowIndex = -1

    private var renamingDialog: Dialog? = null

    private var deletingDialog: Dialog? = null

    var sp: SharedPreferences? = mContext.getSharedPreferences(
        MainActivity.MY_PREFS,
        AppCompatActivity.MODE_PRIVATE
    )
    private var spEditor: SharedPreferences.Editor? = sp!!.edit()


    private lateinit var fileNameForDelete: String

    private var positionForDelete: Int = -1

    override fun getItemCount(): Int {
        return arraylist.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater =
            LayoutInflater.from(parent.context)
        val listItem = layoutInflater.inflate(
            R.layout.content_saved_item,
            parent,
            false
        )
        Log.d("hbs_codee", "onCreateViewHolder: binding")
        return ViewHolder(listItem)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dateInString = Utils.getStringForDate(arraylist[position].file_date)
        val durationInString = Utils.getStringForSeconds(arraylist[position].file_duration)
        val sizeInString = Utils.getStringForSize(arraylist[position].file_Size)

        holder.tvRecName.text = arraylist[position].file_displayName
        holder.tvRecDate.text = dateInString
        holder.tvRecDuration.text = durationInString
        holder.tvRecSize.text = sizeInString

        holder.clickPlay.setOnClickListener {

            rowIndex = position
            notifyDataSetChanged()

            musicClickListenerObject.onItemClickForMusic(position, arraylist)
        }


        if (rowIndex == position) {
            holder.mPlay.setImageResource(R.drawable.ic_play_recording_select)
            holder.tvRecName.setTextColor(ContextCompat.getColor(mContext, R.color.colorPurple))
        } else {
            holder.mPlay.setImageResource(R.drawable.ic_play_recording_unselect)
            holder.tvRecName.setTextColor(ContextCompat.getColor(mContext, R.color.colorBlack))
        }

        holder.ivOpenMenuOptions.setOnClickListener {
            if (comingFrom) {

                val popup = PopupMenu(mContext, holder.ivOpenMenuOptions)
                popup.menuInflater.inflate(R.menu.menu_main, popup.menu)
                popup.setOnMenuItemClickListener { item ->
                    val id = item.itemId
                    if (id == R.id.action_delete) {
                        deleteRecord(arraylist[position].file_displayName, position)
                    }

                    if (id == R.id.action_rename) {
                        renameRecord(arraylist[position].file_displayName)

                    }

                    if (id == R.id.action_addEffects) {
                        addEffectsRec(
                            arraylist[position].fileIri
                        )
                    }
                    true
                }
                popup.show() //showing popup menu
            } else {
                val manager: FragmentManager =
                    (mContext as AppCompatActivity).supportFragmentManager
                val myRoundedBottomSheet = RoundedBottomSheet(this, arraylist, position)
                myRoundedBottomSheet.show(manager, myRoundedBottomSheet.tag)
            }
        }
    }

    private fun addEffectsRec(fileIri: Uri?) {
        (mContext as Activity).finish()


        val intent = Intent(mContext, AudioEffectsCustomActivity::class.java)
        intent.putExtra("savedFileUri", fileIri.toString())
        intent.putExtra("comingFromSaved", true)
        mContext.startActivity(intent)

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var tvRecName: TextView = itemView.findViewById(R.id.tv_recName)
        var tvRecDate: TextView = itemView.findViewById(R.id.tv_recDate)
        var tvRecDuration: TextView = itemView.findViewById(R.id.tv_recDuration)
        var tvRecSize: TextView = itemView.findViewById(R.id.tv_recSize)

        val mPlay = itemView
            .findViewById<View>(R.id.imageView2) as ImageView
        val ivOpenMenuOptions = itemView
            .findViewById<View>(R.id.iv_menuOption) as ImageView
        val clickPlay: View = itemView
            .findViewById(R.id.clickPlay)

    }


    private fun renameRecord(fileName: String) {

        spEditor?.putString("filenameForRecord", fileName)

        spEditor?.apply()

        renameDialog


    }


    private val renameDialog: Unit
        get() {
            if (renamingDialog == null) {
                renamingDialog = VoiceChangerDialogs.createRenameDialog(mContext, this)
                renamingDialog!!.show()
            } else if (!renamingDialog!!.isShowing) {
                renamingDialog!!.show()
            }
        }


    private val deleteDialog: Unit
        get() {
            if (deletingDialog == null) {
                deletingDialog = VoiceChangerDialogs.createDeleteDialog(mContext, this)
                deletingDialog!!.show()
            } else if (!deletingDialog!!.isShowing) {
                deletingDialog!!.show()
            }
        }

    private fun deleteRecord(fileName: String, position: Int) {


        deleteDialog

        fileNameForDelete = fileName

        positionForDelete = position

    }

    fun delete(file: File) {
        val where = MediaStore.MediaColumns.DATA + "=?"
        val selectionArgs = arrayOf(
            file.absolutePath
        )
        val contentResolver: ContentResolver = mContext.contentResolver
        val filesUri = MediaStore.Files.getContentUri("external")
        contentResolver.delete(filesUri, where, selectionArgs)
        if (file.exists()) {
            contentResolver.delete(filesUri, where, selectionArgs)
        }
        file.exists()
    }

    fun scrollToPosition(i: Int) {

        rowIndex = i
        notifyDataSetChanged()


    }

    fun scrollToNoPosition(i: Int) {

        rowIndex = i
        notifyDataSetChanged()


    }

    override fun onClickPurchase() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val filepath =
                File(kept + File.separator + Environment.DIRECTORY_MUSIC + "/AdvancedVoiceChanger/")
            val file = File(filepath, fileNameForDelete)
            delete(file)
        } else {
            val filepath = File("$kept/AdvancedVoiceChanger/")
            val file = File(filepath, fileNameForDelete)
            if (file.exists()) {
                file.delete()
            }
        }

        try {
            arraylist.removeAt(positionForDelete)
        } catch (ignored: Exception) {
        }
        notifyDataSetChanged()
    }

    override fun onDelete(fileDisplayName: String, position: Int) {
        deleteRecord(fileDisplayName, position)
    }

    override fun rename(fileDisplayName: String) {
        renameRecord(fileDisplayName)
    }

    override fun addEffects(fileIri: Uri, fileDisplayname: String) {
        addEffectsRec(fileIri)
    }

    override fun onClickRename() {
        val dir =
            Environment.getExternalStorageDirectory().absolutePath + "/AdvancedVoiceChanger/"

        val previousFileName = sp?.getString("filenameForRecord", "")
        val newFileName = sp?.getString("textOfStringToRename", "")

        val from = File(dir, previousFileName!!)
        val to = File(dir, newFileName!!)
        if (from.exists()) from.renameTo(to)
        Toast.makeText(mContext, "File Renamed Successfully", Toast.LENGTH_SHORT).show()

        refreshListener.onItemClickFroRefresh()
    }

    fun updateList() {
        notifyDataSetChanged()
    }


    interface RefreshData {
        fun onItemClickFroRefresh()
    }
}
