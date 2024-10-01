package pl.poznan.put.openszlaki.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val trailSaver: Saver<Trail, List<String>> = object : Saver<Trail, List<String>> {
    override fun SaverScope.save(value: Trail): List<String> {
        return listOf(value.uid.toString(), value.name, value.description, GlobalConverter.fromUriList(value.imagePaths)?: "", GlobalConverter.fromMeasurementList(value.measurements)?: "")
    }
    override fun restore(value: List<String>): Trail {
        return Trail(
            uid = value[0].toInt(),
            name = value[1],
            description = value[2],
            imagePaths = GlobalConverter.fromUriString(value[3]) ?: mutableListOf(),
            measurements = GlobalConverter.fromMeasurementString(value[4]) ?: mutableListOf()
        )
    }
}
val trailMutableSaver: Saver<MutableState<Trail>, List<String>> = Saver(
    save = { state ->
        listOf(
            state.value.uid.toString(),
            state.value.name,
            state.value.description,
            GlobalConverter.fromUriList(state.value.imagePaths) ?: "",
            GlobalConverter.fromMeasurementList(state.value.measurements) ?: ""
        )
    },
    restore = { list ->
        mutableStateOf(
            Trail(
                uid = list[0].toInt(),
                name = list[1],
                description = list[2],
                imagePaths = GlobalConverter.fromUriString(list[3]) ?: mutableListOf(),
                measurements = GlobalConverter.fromMeasurementString(list[4]) ?: mutableListOf()
            )
        )
    }
)

val dateSaveFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault())
val dateSaver: Saver<Date, String> = Saver(
    save = { state ->
        dateSaveFormat.format(state)
    },
    restore = { str ->
        dateSaveFormat.parse(str)
    }
)

val dateMutableSaver: Saver<MutableState<Date>, String> = Saver(
    save = { state ->
        dateSaveFormat.format(state.value)
    },
    restore = { string ->
        mutableStateOf(dateSaver.restore(string) ?: Date())
    }
)