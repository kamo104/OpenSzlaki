package pl.poznan.put.openszlaki

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.material.appbar.CollapsingToolbarLayout
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.poznan.put.openszlaki.data.AppDatabase
import pl.poznan.put.openszlaki.data.Trail
import pl.poznan.put.openszlaki.ui.theme.OpenSzlakiTheme
import pl.poznan.put.openszlaki.views.NewTrail
import pl.poznan.put.openszlaki.views.TrailDetailScreen
import pl.poznan.put.openszlaki.views.TrailDetails
import pl.poznan.put.openszlaki.views.TrailsGrid


class MainActivity : ComponentActivity() {
    private val db by lazy {
        AppDatabase.getDatabase(applicationContext)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OpenSzlakiTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Main(db)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun Main(db: AppDatabase){
    val multiplePermissionsState = rememberMultiplePermissionsState(listOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_MEDIA_IMAGES,
    ))

    if(!multiplePermissionsState.allPermissionsGranted){
        SideEffect {
            multiplePermissionsState.launchMultiplePermissionRequest()
        }
    } else {
        SplashScreen(db)
    }
}

@Composable
fun SplashScreen(db: AppDatabase) {
    var isSplashVisible by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1000)
        isSplashVisible = false
    }

    if (isSplashVisible) {
        AnimatedMountainClimb()
    } else {
        TrailApp(db)
    }
}


@Composable
fun AnimatedMountainClimb() {
    // Infinite transition for smooth animation
    val infiniteTransition = rememberInfiniteTransition()

    // Animation controlling the line movement
    val lineProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing)
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Define the path for the mountain
            val mountainPath = Path().apply {
                moveTo(100f, size.height - 100f) // Starting point (left bottom)
                lineTo(size.width * 0.3f, size.height * 0.6f) // Peak 1
                lineTo(size.width * 0.5f, size.height * 0.8f) // Valley 1
                lineTo(size.width * 0.7f, size.height * 0.4f) // Peak 2
                lineTo(size.width * 0.9f, size.height - 100f) // End point (right bottom)
            }

            // Create a PathMeasure to calculate the total length of the path
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(mountainPath, false)
            val pathLength = pathMeasure.length

            // Create a temporary path to store the animated segment
            val animatedPath = Path()

            // Get the segment of the path based on the animation progress
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = pathLength * lineProgress,
                destination = animatedPath,
                startWithMoveTo = true
            )

            // Draw the entire mountain path as a reference (light color)
            drawPath(
                path = mountainPath,
                color = Color.LightGray,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Draw the animated path as the line climbs the mountain
            drawPath(
                path = animatedPath,
                color = Color.Red,
                style = Stroke(width = 8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}


@Composable
fun TrailApp(db: AppDatabase) {
    val navController = rememberNavController()
    val dao = db.trailDao()
    val trails by dao.getSortedFlow().collectAsState(initial = emptyList())
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    NavHost(navController = navController, startDestination = "trailList") {
        // trail grid screen
        composable("trailList") {
            drawer(drawerState, navController, trails) {
                TrailsGrid(dao, navController, drawerState)
            }
        }
        // new trail screen
        composable("trailList/new") {
            drawer(drawerState, navController, trails) {
                NewTrail(dao, navController, drawerState)
            }
        }

        // trail details screen
        composable("trailDetails/{trailId}") { backStackEntry ->
            drawer(drawerState, navController, trails) {
                val trailId = backStackEntry.arguments?.getString("trailId")?.toInt()
                val trail = trails.find { it.uid == trailId } ?: return@drawer
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures { change, dragAmount ->
                                change.consume()
                                if (dragAmount > 0) {
                                    val nextUid =
                                        trails.findBefore { it.uid == trailId }?.uid ?: trail.uid
                                    navController.navigate("trailDetails/$nextUid") {
                                        popUpTo("trailList") { inclusive = false }
                                    }
                                    scope.launch { drawerState.close() }
                                    return@detectHorizontalDragGestures
                                }
                                val prevUid =
                                    trails.findAfter { it.uid == trailId }?.uid ?: trail.uid
                                navController.navigate("trailDetails/$prevUid") {
                                    popUpTo("trailList") { inclusive = false }
                                }
                                scope.launch { drawerState.close() }
                            }
                        },
                    content = {
                        TrailDetails(dao, trail, drawerState)
                    }
                )
            }
        }
    }
}

@Composable
fun drawer(state: DrawerState, navController: NavHostController, trails: List<Trail>, inside: @Composable () -> Unit){
    val scope = rememberCoroutineScope()
    ModalNavigationDrawer(
        drawerState = state,
        drawerContent = {
            ModalDrawerSheet {
                Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.Center){
                    Button(onClick = {
                        navController.navigate("trailList")
                        {scope.launch {state.close()}}
                    }) { Text("Trails List")}
                    Spacer(modifier = Modifier.padding(16.dp))
                    Button(onClick = {
                        navController.navigate("trailList/new")
                        {scope.launch {state.close()}}
                    }) { Text("New Trail")}
                }
                // Trail details entries
                trails.forEach { trail ->
                    NavigationDrawerItem(
                        label = { Text(trail.name) },
                        selected = false,
                        onClick = {
                            navController.navigate("trailDetails/${trail.uid}")
                            {scope.launch {state.close()}}
                        }
                    )
                }
            }
        }
    ) {
        inside()
    }
}

private fun <E> List<E>.findAfter(fn: (it:E) -> Boolean): E? {
    for(i in this.indices){
        if(fn(this[i])) return this[(i+1)%this.size]
    }
    return null
}
private fun <E> List<E>.findBefore(fn: (it:E) -> Boolean): E? {
    for(i in this.indices){
        if(fn(this[i])) return this[(i-1+this.size)%this.size]
    }
    return null
}



