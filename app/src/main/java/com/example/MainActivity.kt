package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.MonitorRepository
import com.example.ui.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Room data elements
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = MonitorRepository(database.monitorDao())
        val factory = MonitorViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: MonitorViewModel = viewModel(factory = factory)
                MainAppLayout(viewModel)
            }
        }
    }
}

class MonitorViewModelFactory(private val repository: MonitorRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MonitorViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MonitorViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppLayout(viewModel: MonitorViewModel) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(100))
                                .background(CyberRed)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "YOUTUBE VIRAL ALGORITHMS",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.8.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = BlackBackground
                ),
                modifier = Modifier.border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.06f))),
                    shape = RoundedCornerShape(0.dp)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CardBackground,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.05f), Color.Transparent)),
                        shape = RoundedCornerShape(0.dp)
                    )
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                val tabs = listOf(
                    Triple(DashboardTab.DASHBOARD, Icons.Default.Home, "Live Stream"),
                    Triple(DashboardTab.ALGORITHM, Icons.Default.Build, "Diagnostic"),
                    Triple(DashboardTab.NICHES, Icons.Default.Search, "Niche Finder"),
                    Triple(DashboardTab.GENERATOR, Icons.Default.Add, "Creator AI"),
                    Triple(DashboardTab.SAVED, Icons.Default.Favorite, "Portfolio")
                )

                tabs.forEach { (tab, icon, label) ->
                    val isSelected = viewModel.currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) CyberRed else TextSecondary,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else TextMuted,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CyberRed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag("nav_item_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BlackBackground)
                .padding(innerPadding)
        ) {
            when (viewModel.currentTab) {
                DashboardTab.DASHBOARD -> DashboardScreen(viewModel)
                DashboardTab.ALGORITHM -> AlgorithmDiagnosticScreen(viewModel)
                DashboardTab.NICHES -> NicheFinderScreen(viewModel)
                DashboardTab.GENERATOR -> GeneratorScreen(viewModel)
                DashboardTab.SAVED -> SavedPortfolioScreen(viewModel)
            }
        }
    }
}
