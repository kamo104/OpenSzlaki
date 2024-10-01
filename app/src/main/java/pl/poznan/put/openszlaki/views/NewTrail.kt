package pl.poznan.put.openszlaki.views

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import pl.poznan.put.openszlaki.data.Trail
import pl.poznan.put.openszlaki.data.TrailDao
import pl.poznan.put.openszlaki.data.trailSaver

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTrail(dao: TrailDao, navController: NavHostController, drawerState: DrawerState) {
    val scope = rememberCoroutineScope()
    val trail = rememberSaveable(saver = trailSaver) { Trail(name="", description = "", imagePaths = mutableListOf(), measurements = mutableListOf()) }
    var trailName by remember { mutableStateOf("") }
    var isValidName by remember { mutableStateOf(true) }
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
        floatingActionButton = {},
        content = { padding -> Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // TextField to enter trail name
                    OutlinedTextField(
                        value = trailName,
                        onValueChange = {
                            trailName = it
                            isValidName = trailName.isNotBlank()
                        },
                        label = { Text("Trail Name") },
                        isError = !isValidName,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (!isValidName) {
                        Text(
                            text = "Trail name cannot be empty",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.align(Alignment.Start)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Submit Button
                    Button(
                        onClick = {
                            if (trailName.isNotBlank()) {
                                trail.name = trailName
                                scope.launch {
                                    dao.insertAll(trail)
                                    val trailId = dao.get(trail.name).uid
                                    navController.navigate("trailDetails/$trailId") {
                                        popUpTo("trailList") { inclusive = false }
                                    }
                                    drawerState.close()
                                }
                            } else {
                                isValidName = false
                            }
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Create Trail")
                    }
                }
            }
        )
        })

}