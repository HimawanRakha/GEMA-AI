package com.example.stecu.ui.screen

import android.app.Application
import androidx.compose.foundation.ExperimentalFoundationApi // CHANGE 1: Import for combinedClickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable // CHANGE 2: Import for combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import com.example.stecu.data.db.CareerPlanEntity
import com.example.stecu.data.navigation.Screen
import com.example.stecu.viewmodel.CareerDetailViewModel
import com.example.stecu.viewmodel.CareerReportViewModel

@Composable
fun AmberPointsScreen(
    navController: NavHostController,
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: CareerReportViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return CareerReportViewModel(application) as T
            }
        }
    )
    var searchQuery by remember { mutableStateOf("") }
    val careerList by viewModel.careerPlans.collectAsState()

    // --- CHANGE 3: State for managing the delete confirmation dialog ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<CareerPlanEntity?>(null) }


    // Filter list berdasarkan search query
    val filteredList = if (searchQuery.isEmpty()) {
        careerList
    } else {
        careerList.filter { it.goal.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
        ) {
            // 1. Judul
            Text(
                text = "Amber Points",
                color = Color.Black,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Search Bar
            CustomSearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                height = 48.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Daftar Item Dinamis
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    CareerReportItem(
                        title  = item.goal,
                        onItemClick = {
                            // Navigasi ke layar detail saat item diklik
                            navController.navigate(Screen.CareerDetail.createRoute(item.id.toString()))
                        },
                        // --- CHANGE 4: Handle long click to show dialog ---
                        onItemLongClick = {
                            itemToDelete = item
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        // --- CHANGE 5: The confirmation dialog itself ---
        if (showDeleteDialog && itemToDelete != null) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    itemToDelete = null
                },
                title = { Text("Delete Career Plan") },
                text = { Text("Are you sure you want to delete the plan for '${itemToDelete!!.goal}'? This action cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteCareerPlan(itemToDelete!!.id)
                            showDeleteDialog = false
                            itemToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6000))
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            showDeleteDialog = false
                            itemToDelete = null
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalFoundationApi::class) // CHANGE 6: Add OptIn for combinedClickable
@Composable
fun CareerReportItem2(
    title: String,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit // CHANGE 7: Add a new parameter for long click
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6000), Color(0xFFFC8F33))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            )
            .border(
                width = 1.dp,
                color = Color(0X9B9B9B80),
                shape = RoundedCornerShape(16.dp)
            )
            // --- CHANGE 8: Replace .clickable with .combinedClickable ---
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            )
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        clip = true
                    )
                    .border(
                        width = 1.dp,
                        color = Color(0X9B9B9B80),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .clip(RoundedCornerShape(20))
                    .background(Color.White)
            ) {
                // Anda bisa menambahkan icon di dalam Box ini jika perlu
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 24.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = Color(0X9B9B9B80),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12))
                    .background(Color.White)
                    .padding(4.dp),
                    contentAlignment = Alignment.Center, ) {
                    Text(
                        text = "See Career Tree",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// CustomSearchBar remains the same, no changes needed here.
@Composable
fun CustomSearchBar2(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp, // Tambahkan parameter tinggi
    placeholderText: String = "Search"
) {
    val gradientBrush = Brush.horizontalGradient(
        colors = listOf(Color(0xFFFF6000), Color(0xFFFC8F33))
    )
    var isFocused by remember { mutableStateOf(false) }

    val borderModifier = if (isFocused) {
        Modifier.border(
            width = 2.dp,
            color = Color(0X9B9B9B80),
            shape = RoundedCornerShape(50)
        )
    } else {
        Modifier.border(
            width = 1.dp,
            color = Color(0X9B9B9B80),
            shape = RoundedCornerShape(50)
        )
    }

    // Menggunakan BasicTextField untuk kontrol penuh
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .onFocusChanged { isFocused = it.isFocused },
        singleLine = true,
        // Atur warna cursor dengan brush
        cursorBrush = SolidColor(Color.Black),
        textStyle = LocalTextStyle.current.copy(color = Color.Black),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .height(height) // Gunakan tinggi dari parameter
                    .clip(RoundedCornerShape(50))
                    .background(Color.White)
                    .then(borderModifier)
                    .padding(start = 16.dp, end = 8.dp), // Padding internal untuk konten
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    // Jika teks kosong, tampilkan placeholder
                    if (query.isEmpty()) {
                        Text(
                            text = placeholderText,
                            color = Color(0X9B9B9B80)
                        )
                    }
                    // Ini adalah tempat teks input akan muncul
                    innerTextField()
                }

                // Ikon di sebelah kanan
                Box(
                    modifier = Modifier
                        .size(height * 0.8f) // Ukuran ikon dinamis berdasarkan tinggi
                        .clip(CircleShape)
                        .background(Color(0XFFB0E7FF)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Icon",
                        tint = Color.White,
                        modifier = Modifier.size(height * 0.4f)
                    )
                }
            }
        }
    )
}