package com.appsol.advancedvoicechangeapp
import android.text.format.DateFormat
import android.text.format.DateUtils
import com.appsol.advancedvoicechangeapp.model.GetSetAudio
import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {
        fun getStringForSeconds(seconds: Long): String {
            return DateUtils.formatElapsedTime(seconds)
        }

        fun getStringForSize(fileSize: Long): String {
            return if (fileSize >= 1024) {
                (fileSize / 1024).toString() + " MB"
            } else {
                "$fileSize KB"
            }
        }

        fun getAudioItemsArray(): ArrayList<GetSetAudio> {
            val items = ArrayList<GetSetAudio>()
            items.add(GetSetAudio(R.drawable.original_oice, "Original ", 0f, 100))
            items.add(GetSetAudio(R.drawable.ic_helium, "Helium", 12f, 100))
            items.add(GetSetAudio(R.drawable.ic_nervous, "Nervous", -1f, 165))
            items.add(GetSetAudio(R.drawable.ic_drunk, "Drunk", 0f, 60))
            items.add(GetSetAudio(R.drawable.chipmunk, "Squirrel", 13f, 165))
            items.add(GetSetAudio(R.drawable.ic_child, "Child", 8f, 100))
            items.add(GetSetAudio(R.drawable.giant_voice, "Giant", -10f, 80))
            items.add(GetSetAudio(R.drawable.ic_zombie, "Zombie", -5f, 45))
            items.add(GetSetAudio(R.drawable.ic_monster, "Monster", -15f, 85))
            items.add(GetSetAudio(R.drawable.ic_deep_voice, "Deep Voice", -18f, 80))
            items.add(GetSetAudio(R.drawable.ic_poltergeist, "Poltergeist", -7f, 80))
            items.add(GetSetAudio(R.drawable.ic_hexa_fluoride, "Hexafluor", -8f, 105))
            items.add(GetSetAudio(R.drawable.ic_small_creature, "Bee", 10f, 160))
            items.add(GetSetAudio(R.drawable.ic_extra_terrestrial, "Alien", 5f, 65))
            return items
        }


        fun getStringForDate(date: Long): String {
            return DateFormat.format("dd/MM/yyyy", Date(date)).toString()
        }

        fun getTime(ti: Long): String? {
            val formatter: java.text.DateFormat =
                SimpleDateFormat("mm:ss", Locale.US)
            formatter.timeZone = TimeZone.getTimeZone("UTC")
            return formatter.format(Date(ti))
        }
    }
}