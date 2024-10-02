package pl.poznan.put.openszlaki.views

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import me.onebone.toolbar.CollapsingToolbarScaffold
import me.onebone.toolbar.ScrollStrategy
import me.onebone.toolbar.rememberCollapsingToolbarScaffoldState
import pl.poznan.put.openszlaki.data.Measurement
import pl.poznan.put.openszlaki.data.Trail
import pl.poznan.put.openszlaki.data.TrailDao
import pl.poznan.put.openszlaki.data.trailMutableSaver
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.time.Duration
import kotlin.time.toKotlinDuration


@Composable
fun TrailDetails(dao: TrailDao, trailIn: Trail, drawerState: DrawerState) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    val trail = rememberSaveable(saver = trailMutableSaver) { mutableStateOf(trailIn) }
    val showConfirm = rememberSaveable {mutableStateOf(false)}

    val getTmpUri = {
        val imageName = "IMG_" + System.currentTimeMillis().toString() + ".jpg"
        val file = File(ctx.filesDir, imageName)
        FileProvider.getUriForFile(ctx, ctx.packageName + ".provider", file)
    }
    val copyFileFromUri: (Uri, Uri) -> Unit = { sourceUri, destUri ->
        val contentResolver = ctx.contentResolver
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(sourceUri)
            val outputStream: OutputStream? = contentResolver.openOutputStream(destUri)
            inputStream?.use { input ->
                outputStream?.use { output ->
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                    output.flush()
                }
            }
            Log.d("FileCopy", "File copied successfully from $sourceUri to $destUri")
        } catch (e: Exception) {
            Log.e("FileCopyError", "Error copying file: ${e.message}", e)
        }
    }

    val tmpPhotoUri = rememberSaveable {getTmpUri()}

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        Log.d("Photo picked?", "Selected URI: $uri")
        if(uri == null){
            return@rememberLauncherForActivityResult
        }
        val tmpUri = getTmpUri()
        copyFileFromUri(uri, tmpUri)
        trail.value.imagePaths.add(tmpUri)
        trail.value = trail.value.copy() // dirty trick to force recomposition

        showConfirm.value = true;
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { succ ->
        Log.d("Photo taken?", "$succ")
        if(!succ) return@rememberLauncherForActivityResult
        trail.value.imagePaths.add(tmpPhotoUri)
        trail.value = trail.value.copy()
        showConfirm.value = true;
    }

    Scaffold(
        topBar = {},
        floatingActionButton = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                // confirm changes
                AnimatedVisibility(
                    visible = showConfirm.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                dao.updateTrail(trail.value)
                                showConfirm.value = false
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Confirm"
                            )
                        }
                    )

                }
                // cancel changes
                AnimatedVisibility(
                    visible = showConfirm.value,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = {
                            scope.launch {
                                trail.value = dao.get(trail.value.uid)
                                showConfirm.value = false
                            }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel"
                            )
                        }
                    )
                }
                // pick photo from gallery
                FloatingActionButton(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Image"
                        )
                    }
                )
                // take a new photo
                FloatingActionButton(
                    onClick = {
                        takePhoto.launch(tmpPhotoUri)
                    },
                    content = {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Take a Photo"
                        )
                    }
                )
            }
        },
        content = { padding ->
            Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            content = {
                val state = rememberCollapsingToolbarScaffoldState()
                CollapsingToolbarScaffold(
                    modifier = Modifier.fillMaxSize(),
                    state = state,
                    scrollStrategy = ScrollStrategy.EnterAlwaysCollapsed,
                    toolbar = {
                        if(trail.value.imagePaths.isNotEmpty())
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(trail.value.imagePaths.first())
                                    .build(),
                                contentDescription = "${trail.value.name} thumbnail",
                                modifier = Modifier
                                    .sizeIn(maxHeight = LocalConfiguration.current.screenHeightDp.dp / 2)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Crop
                            )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                                .pin()
                                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5F))
                        ){
                            Column (modifier = Modifier.align(Alignment.CenterVertically)){
                                IconButton(modifier = Modifier.padding(16.dp), onClick = {scope.launch {drawerState.open()}}) {
                                    Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                                }
                            }
                            Column (modifier = Modifier.align(Alignment.CenterVertically)) {
                                BasicTextField(
                                    value = trail.value.name,
                                    onValueChange = {
                                        trail.value = trail.value.copy(name = it)
                                        if (trail.value.name.isNotBlank())
                                            showConfirm.value = true
                                    },
                                    modifier = Modifier
                                        .padding(16.dp),
                                    textStyle = TextStyle(
                                        color = Color.White,
                                        fontSize = 20.sp
                                    ),
                                    decorationBox = { innerTextField ->
                                        if (trail.value.name.isEmpty()) {
                                            Text(
                                                text = "Enter trail name",
                                                color = Color.White.copy(alpha = 0.5f),
                                                style = MaterialTheme.typography.titleMedium,
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                ) {
                    TrailDetailScreen(dao, trail, showConfirm)
                }
            }
        )
    })
}


@Composable
fun TrailDetailScreen(
    dao: TrailDao,
    trail: MutableState<Trail>,
    isChanged: MutableState<Boolean>,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        // Description TextField with aesthetic styling
        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(text = "Description", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)

                BasicTextField(
                    value = trail.value.description,
                    onValueChange = {
                        trail.value.description = it
                        trail.value = trail.value.copy()
                        isChanged.value = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp),
                    textStyle = TextStyle(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    decorationBox = { innerTextField ->
                        if (trail.value.description.isEmpty()) {
                            Text(
                                text = "Enter description",
                                style = TextStyle(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }
        item(span = { GridItemSpan(maxCurrentLineSpan) }) {
            Stopper(dao, trail.value)
        }
        if(trail.value.measurements.isNotEmpty())
            item(span = {GridItemSpan(maxCurrentLineSpan)}) {
                MeasurementTable(trail.value.measurements)
            }
        // Displaying images
        if(trail.value.imagePaths.size>1)
        items(trail.value.imagePaths.subList(1,trail.value.imagePaths.size)) { photo ->
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(photo)
                    .build(),
                contentDescription = "${trail.value.name} image",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun MeasurementTable(measurements: List<Measurement>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Start Time", fontWeight = FontWeight.Bold)
            Text(text = "Duration", fontWeight = FontWeight.Bold)
        }
        // Measurement rows
        for (measurement in measurements) {
            MeasurementRow(measurement)
        }
    }
}



@Composable
fun MeasurementRow(measurement: Measurement) {
    val dateDisplayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = dateDisplayFormat.format(measurement.value))
        Text(text = measurement.unit.format())
    }
}

fun Duration.format(template: String = "HH:MM:SS.ms"): String {
    val asKot = this.toKotlinDuration()
    val hours = asKot.inWholeHours
    val minutes = asKot.inWholeMinutes % 60
    val seconds = asKot.inWholeSeconds % 60
    val milliseconds = asKot.inWholeMilliseconds/10 % 100

    return template.replace("HH", "%02d".format(hours))
        .replace("MM", "%02d".format(minutes))
        .replace("SS", "%02d".format(seconds))
        .replace("ms", "%02d".format(milliseconds))
}