package com.example.stecu.ui.screen

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.stecu.R
import com.example.stecu.data.model.Milestone
import com.example.stecu.data.model.Resource
import com.example.stecu.viewmodel.CareerDetailViewModel
import kotlin.math.roundToInt

@Composable
fun ChecklistItem(
    text: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit // <-- PERBAIKAN 1: Tipe data yang benar adalah (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange, // Sekarang tipe datanya cocok
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0XFFB0E7FF),
                uncheckedColor = Color.Black,
                checkmarkColor = Color.Black
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, color = Color.Black, fontSize = 14.sp)
    }
}

@Composable
fun MilestoneCard(
    milestone: Milestone,
    milestoneNumber: Int,
    onStepCheckedChange: (questId: String, stepText: String, isChecked: Boolean) -> Unit
) {
    Card(
        modifier = Modifier.width(300.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(2.dp, Color(0XFFB0E7FF))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Card: Judul dan Durasi
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$milestoneNumber. ${milestone.title}",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_schedule),
                        contentDescription = "Duration",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${milestone.duration_weeks} mgg",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }
            Divider(color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))

            // Daftar Quests
            milestone.quests.forEach { quest ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = quest.title,
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
                    )
                    if (quest.resources.isNotEmpty()) {
                        val uriHandler = LocalUriHandler.current
                        val resource = quest.resources.first()
                        IconButton(onClick = { uriHandler.openUri(resource.url) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open Resource",
                                tint = Color.Black
                            )
                        }
                    }
                }
                quest.steps.forEach { step ->
                    ChecklistItem(
                        text = step.text,
                        checked = step.isChecked,
                        onCheckedChange = { newCheckedState ->
                            onStepCheckedChange(quest.id, step.text, newCheckedState)
                        }
                    )
                }
            }
        } // <-- PERBAIKAN 2: Kurung kurawal yang hilang ditambahkan di sini
    }
}

@Composable
fun MilestoneNode(
    milestone: Milestone,
    milestoneNumber: Int,
    isLastNode: Boolean,
    onStepCheckedChange: (questId: String, stepText: String, isChecked: Boolean) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DurationBranch(durationInWeeks = milestone.duration_weeks)
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
            }

            MilestoneCard(
                milestone = milestone,
                milestoneNumber = milestoneNumber,
                onStepCheckedChange = onStepCheckedChange
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.width(16.dp), color = Color.Gray)
                val allResources = milestone.quests.flatMap { it.resources }
                ResourceBranch(resources = allResources)
            }
        }

        if (!isLastNode) {
            Icon(
                painter = painterResource(R.drawable.ic_arrowdownward),
                contentDescription = "Connector to next milestone",
                tint = Color.Gray,
                modifier = Modifier
                    .size(40.dp)
                    .padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
fun OverallProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0XFFB0E7FF))
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(
                text = "Overall Progress",
                color = Color.Black,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0XFFB0E7FF),
                trackColor = Color.Gray,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${(progress * 100).roundToInt()}%",
                color = Color.LightGray,
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.End)
            )
        }
    }
}

@Composable
fun DurationBranch(durationInWeeks: Int) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0XFFB0E7FF))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_schedule),
                contentDescription = "Duration",
                tint = Color.Black,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$durationInWeeks MINGGU",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun ResourceBranch(resources: List<Resource>) {
    if (resources.isNotEmpty()) {
        val uriHandler = LocalUriHandler.current
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0XFFB0E7FF))
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).width(120.dp)) {
                resources.forEach { resource ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = resource.title,
                            color = Color.Black,
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f),
                            maxLines = 2
                        )
                        IconButton(
                            onClick = { uriHandler.openUri(resource.url) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_link),
                                contentDescription = "Open Link",
                                tint = Color(0XFFB0E7FF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CareerDetailScreen(
    careerId: String?,
    onNavigateBack: () -> Unit
) {

    val application = LocalContext.current.applicationContext as Application
    val viewModel: CareerDetailViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CareerDetailViewModel(application) as T
            }
        }
    )

    LaunchedEffect(careerId) {
        careerId?.toLongOrNull()?.let {
            viewModel.loadCareerPlan(it)
        }
    }
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            uiState.error != null -> {
                Text(
                    text = "Error: ${uiState.error}",
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            uiState.careerPlan != null -> {
                val careerPlan = uiState.careerPlan!!

                Image(
                    painter = painterResource(id = R.drawable.bg_lines),
                    contentDescription = "Background Lines",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = careerPlan.goal,
                                    color = Color.Black,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            navigationIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(start = 16.dp)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0XFFB0E7FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    IconButton(onClick = onNavigateBack) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Back",
                                            tint = Color.White
                                        )
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    },
                    containerColor = Color.Transparent
                ) { paddingValues ->

                    var scale by remember { mutableStateOf(1f) }
                    var offset by remember { mutableStateOf(Offset.Zero) }

                    Box(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize()
                            .clipToBounds()
                            .pointerInput(Unit) {
                                detectTransformGestures { centroid, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(0.3f, 3f)
                                    offset = (offset * zoom) + centroid * (1 - zoom) + pan
                                    scale = newScale
                                }
                            }
                    ) {
                        Layout(
                            content = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    val allSteps = remember(careerPlan) {
                                        careerPlan.milestones.flatMap { it.quests }
                                            .flatMap { it.steps }
                                    }
                                    val checkedSteps = allSteps.count { it.isChecked }
                                    val totalSteps = allSteps.size
                                    val progress = if (totalSteps > 0) {
                                        checkedSteps.toFloat() / totalSteps.toFloat()
                                    } else {
                                        0f
                                    }

                                    OverallProgressBar(
                                        progress = progress,
                                        modifier = Modifier
                                            .align(Alignment.Start)
                                            .padding(bottom = 24.dp)
                                    )

                                    Spacer(modifier = Modifier.size(16.dp))

                                    careerPlan.milestones.forEachIndexed { index, milestone ->
                                        MilestoneNode(
                                            milestone = milestone,
                                            milestoneNumber = index + 1,
                                            isLastNode = index == careerPlan.milestones.lastIndex,
                                            onStepCheckedChange = viewModel::updateStepCheckedState
                                        )
                                    }

                                    Spacer(modifier = Modifier.size(16.dp))
                                }
                            },
                            modifier = Modifier
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y,
                                    transformOrigin = TransformOrigin(0.5f, 0f)
                                )
                        ) { measurables, constraints ->
                            val looseConstraints = constraints.copy(
                                minWidth = 0,
                                minHeight = 0,
                                maxWidth = Constraints.Infinity,
                                maxHeight = Constraints.Infinity
                            )
                            val placeable = measurables.first().measure(looseConstraints)

                            layout(constraints.maxWidth, constraints.maxHeight) {
                                val x = (constraints.maxWidth - placeable.width) / 2
                                val y = 0
                                placeable.placeRelative(x, y)
                            }
                        }
                    }
                }
            }
        }
    }
}