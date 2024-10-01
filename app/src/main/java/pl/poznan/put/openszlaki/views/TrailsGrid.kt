package pl.poznan.put.openszlaki.views

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch
import pl.poznan.put.openszlaki.data.Trail
import pl.poznan.put.openszlaki.data.TrailDao


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrailsGrid(dao: TrailDao, navController: NavHostController, drawerState: DrawerState) {
    val trails by dao.getFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trails") },
                navigationIcon = {
                    IconButton(onClick = {scope.launch {drawerState.open()}}) {
                        Icon(Icons.Filled.Menu, contentDescription = "Open Drawer")
                    }
                }
            )
        },
        floatingActionButton = {FloatingActionButton(
            onClick = {
                navController.navigate("trailList/new")
                {scope.launch {drawerState.close()}}
            },
            content = {Icon(imageVector = Icons.Default.Add, contentDescription = "Add Trail")}
        )},
        content = { padding -> Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            content = {
                LazyVerticalGrid(columns = GridCells.Adaptive(minSize = 140.dp)) {
                    items(trails) { trail ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(onTap = {
                                        navController.navigate("trailDetails/${trail.uid}")
                                        {scope.launch {drawerState.close()}}
                                    })
                                },
                            content = {TrailItem(trail)}
                        )
                    }
                }
            }
        )
    })
}


@Composable
fun TrailItem(trail: Trail) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.tertiaryContainer,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
//                .padding(bottom = 4.dp)
        ) {
            if (trail.imagePaths.isNotEmpty()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(trail.imagePaths.first())
                        .build(),
                    contentDescription = "${trail.name} image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = trail.name,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(8.dp),
                style = MaterialTheme.typography.headlineSmall,
                overflow = TextOverflow.Clip,
                maxLines = 1
            )
        }
    }
}