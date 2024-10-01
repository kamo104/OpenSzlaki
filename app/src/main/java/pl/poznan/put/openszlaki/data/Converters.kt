package pl.poznan.put.openszlaki.data

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.core.net.toFile
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

val GlobalConverter = Converters()

class Converters {

    @TypeConverter
    fun fromUriString(value: String?): MutableList<Uri>? {
        if (value == null) return null
        val listType = object : TypeToken<MutableList<String>>() {}.type
        return try {
            val stringList: MutableList<String> = Gson().fromJson(value, listType)
            stringList.map { Uri.parse(it) }.toMutableList()
        } catch (e: NullPointerException) {
            println("Error parsing JSON: $value")
            null
        }
    }

    @TypeConverter
    fun fromUriList(list: MutableList<Uri>?): String? {
        if (list == null) return null
        val stringList = list.map { it.toString() }.toMutableList()
        return Gson().toJson(stringList)
    }

    @TypeConverter
    fun fromMeasurementString(value: String?): MutableList<Measurement>? {
        if (value == null) return null
        val listType = object : TypeToken<MutableList<Measurement>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromMeasurementList(list: MutableList<Measurement>?): String? {
        return Gson().toJson(list)
    }
}