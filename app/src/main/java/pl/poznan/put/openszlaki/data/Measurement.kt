package pl.poznan.put.openszlaki.data
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date
import java.time.Duration


@Parcelize
data class Measurement(val value: Date, val unit: Duration) : Parcelable