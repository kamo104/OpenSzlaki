package pl.poznan.put.openszlaki.views

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.time.delay
import pl.poznan.put.openszlaki.data.Trail
import pl.poznan.put.openszlaki.data.TrailDao
import pl.poznan.put.openszlaki.data.Measurement
import pl.poznan.put.openszlaki.data.dateMutableSaver
import java.time.Duration
import java.time.Instant
import java.util.Date
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

@SuppressLint("DefaultLocale")
@Composable
fun Stopper(dao: TrailDao, trail: Trail) {
    var currentDuration by rememberSaveable { mutableLongStateOf(0L) }
    var isStarted by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var job: Job? by remember { mutableStateOf(null) }
    val startDate = rememberSaveable(saver = dateMutableSaver) { mutableStateOf(Date.from(Instant.now())) }
    var lastStartTime by rememberSaveable { mutableLongStateOf(Instant.now().toEpochMilli()) }

    fun coroutineLaunch() {
        job = scope.launch {
            val lastDuration = currentDuration
            lastStartTime = Instant.now().toEpochMilli()
            try {
                while (job?.isActive == true) {
                    currentDuration = lastDuration + Duration.between(Instant.ofEpochMilli(lastStartTime), Instant.now()).toMillis()
                    delay(100.milliseconds.toJavaDuration())
                }
            } catch (e: CancellationException) {
                currentDuration = lastDuration + Duration.between(Instant.ofEpochMilli(lastStartTime), Instant.now()).toMillis()
            }
        }
    }

    fun timerStart() {
        isStarted = true
        coroutineLaunch()
    }

    fun timerPause() {
        isStarted = false
        job?.cancel()
        job = null
    }

    fun timerReStart() {
        if (isStarted) {
            timerPause()
            return
        }
        timerStart()
    }

    fun timerStop() {
        timerPause()
        if (currentDuration>0)
        scope.launch {
            trail.measurements.add(Measurement(startDate.value, Duration.ofMillis(currentDuration)))
            dao.updateTrail(trail)
            currentDuration = 0L
            startDate.value = Date.from(Instant.now())
        }
    }

    if (isStarted && job == null) coroutineLaunch()

    DisposableEffect(Unit) {
        onDispose {
            job?.cancel()
        }
    }

    Column(
        modifier = Modifier
//            .fillMaxSize()
            .padding(16.dp)
            .border(width = 2.dp, color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Timer Display
        val tmpDuration = Duration.ofMillis(currentDuration).toKotlinDuration()
        val hours = tmpDuration.inWholeHours
        val minutes = tmpDuration.inWholeMinutes % 60
        val seconds = tmpDuration.inWholeSeconds % 60
        val out = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        Text(
            text = out,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier
                .padding(16.dp)
                .background(MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(8.dp))
                .padding(16.dp)
        )
        // Button Row
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.width(300.dp)
        ) {
            Button(
                onClick = { timerReStart() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = if (isStarted) "Pause" else "Start", color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { timerStop() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Stop", color = Color.White)
            }
        }
    }
}