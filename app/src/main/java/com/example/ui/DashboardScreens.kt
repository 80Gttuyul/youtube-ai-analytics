package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.AnalysisResult
import com.example.api.GeminiClient
import com.example.api.GeneratorResult
import com.example.api.NicheResult
import com.example.ui.theme.*

// --- Custom Glowing Interactive Card Helper ---
@Composable
fun CyberCard(
    modifier: Modifier = Modifier,
    borderColor: Color = CyberRed.copy(alpha = 0.4f),
    backgroundColor: Color = CardBackground,
    title: String? = null,
    titleIcon: @Composable (() -> Unit)? = null,
    glowing: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "border_glow")
    val alphaGlow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_float"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                border = BorderStroke(
                    width = if (glowing) 2.dp else 1.2.dp,
                    color = if (glowing) borderColor.copy(alpha = alphaGlow) else borderColor
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        if (title != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                if (titleIcon != null) {
                    titleIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
            }
        }
        content()
    }
}

// --- Dynamic Interactive Canvas Neon Chart ---
@Composable
fun NeonGraph(
    modifier: Modifier = Modifier,
    lineColor: Color = CyberRed,
    points: List<Float> = listOf(20f, 45f, 35f, 65f, 50f, 95f, 80f, 130f, 110f, 160f)
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        val width = size.width
        val height = size.height
        val spacing = width / (points.size - 1)
        val maxPoint = points.maxOrNull() ?: 100f
        val minPoint = points.minOrNull() ?: 0f
        val range = (maxPoint - minPoint).coerceAtLeast(1f)

        val path = Path()
        val filledPath = Path()

        points.forEachIndexed { i, p ->
            val x = i * spacing
            // Invert y because canvas (0,0) is top-left
            val ratio = (p - minPoint) / range
            val y = height - (ratio * (height - 30f)) - 15f

            if (i == 0) {
                path.moveTo(x, y)
                filledPath.moveTo(x, height)
                filledPath.lineTo(x, y)
            } else {
                // Curved Bezier calculation
                val previousX = (i - 1) * spacing
                val previousRatio = (points[i - 1] - minPoint) / range
                val previousY = height - (previousRatio * (height - 30f)) - 15f
                
                val cpX1 = previousX + (spacing / 2)
                val cpY1 = previousY
                val cpX2 = previousX + (spacing / 2)
                val cpY2 = y

                path.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
                filledPath.cubicTo(cpX1, cpY1, cpX2, cpY2, x, y)
            }
            if (i == points.size - 1) {
                filledPath.lineTo(x, height)
                filledPath.close()
            }
        }

        // Draw ambient glow shadow
        drawPath(
            path = path,
            color = lineColor.copy(alpha = 0.4f),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Draw crisp foreground line
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.5f, cap = StrokeCap.Round)
        )

        // Draw area gradient fill
        drawPath(
            path = filledPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // Draw active glowing node at last point
        val lastX = width
        val lastRatio = (points.last() - minPoint) / range
        val lastY = height - (lastRatio * (height - 30f)) - 15f
        
        drawCircle(
            color = lineColor.copy(alpha = 0.4f),
            radius = 12f,
            center = Offset(lastX, lastY)
        )
        drawCircle(
            color = lineColor,
            radius = 5f,
            center = Offset(lastX, lastY)
        )
    }
}

// --- Custom Animated Semi-Circular Radial Score Meter ---
@Composable
fun RadialScoreMeter(
    score: Int,
    modifier: Modifier = Modifier
) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) {
        animatedScore.animateTo(
            targetValue = score.toFloat(),
            animationSpec = tween(1200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(160.dp)
    ) {
        Canvas(modifier = Modifier.size(140.dp)) {
            val sweep = (animatedScore.value / 100f) * 360f
            
            // Background ring track
            drawArc(
                color = Color.White.copy(alpha = 0.08f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )

            // Neon glowing indicators
            val gradientBrush = Brush.sweepGradient(
                colorStops = arrayOf(
                    0.0f to CyberRed,
                    0.7f to NeonRedGlow,
                    1.0f to NeonCyan
                )
            )

            drawArc(
                brush = gradientBrush,
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${score}%",
                color = if (score > 80) NeonCyan else Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "VIRAL OPPORTUNITY",
                color = TextSecondary,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = if (score > 80) NeonCyan.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = when {
                        score > 85 -> "EXPLODING 🔥"
                        score > 70 -> "HIGHLY VIRAL ⚡"
                        score > 50 -> "STABLE TREND ✨"
                        else -> "COMPETITIVE 🔨"
                    },
                    color = if (score > 80) NeonCyan else NeonRedGlow,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

// --- API CONFIGURATION ALERT NOTICE ---
@Composable
fun ApiStatusNotice() {
    val isConfigured = GeminiClient.isApiKeyConfigured
    
    Surface(
        color = if (isConfigured) MatteGray.copy(alpha = 0.4f) else CyberRed.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, if (isConfigured) Color.White.copy(alpha = 0.15f) else CyberRed.copy(alpha = 0.4f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConfigured) Icons.Default.Info else Icons.Default.Warning,
                contentDescription = "Status Key",
                tint = if (isConfigured) NeonCyan else CyberRed,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = if (isConfigured) "PRO MODE: ACTIVE" else "MOCK MODE: SIMULATOR",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isConfigured) 
                        "Mesin AI Studio Gemini-3.5-Flash terhubung dengan responsif." 
                        else "API Key belum terpasang di Secrets Panel. Aplikasi berjalan menggunakan model simulasi cerdas.",
                    color = TextSecondary,
                    fontSize = 9.5.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

// ============================================
// TAB 1: SCREEN DASHBOARD PRESET (Live Global Trends & Competitor Stream)
// ============================================
@Composable
fun DashboardScreen(viewModel: MonitorViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            ApiStatusNotice()
        }

        // Section 1: Algoritma Monitor Quick Chart
        item {
            CyberCard(
                title = "Live Momentum Algorithm Global Signal",
                titleIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = CyberRed, modifier = Modifier.size(16.dp)) },
                glowing = true
            ) {
                Text(
                    text = "Pergerakan Algoritma real-time saat ini merekomendasikan video berdurasi pendek (< 50 detik) dengan retensi penonton di atas 72%.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                NeonGraph(
                    lineColor = CyberRed,
                    points = listOf(35f, 40f, 30f, 63f, 52f, 84f, 81f, 112f, 105f, 142f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    MiniStatItem("Sinyal Induk", "Aktiv/Sehat", NeonCyan)
                    MiniStatItem("Global CTR Rata", "6.4%", Color.White)
                    MiniStatItem("Puncak Jam", "18.30 WIB", NeonRedGlow)
                }
            }
        }

        // Section 2: Rising Trends Now (Exploding Content Indicator)
        item {
            Text(
                text = "KONTEN SEDANG NAIK (TRENDING TERCEPAT SEKARANG)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        items(viewModel.premiumGlobalTrends) { trend ->
            Surface(
                color = CardBackground,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.inputNicheQuery = trend.title
                        viewModel.currentTab = DashboardTab.NICHES
                        viewModel.searchNiche()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = trend.title,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = when (trend.rate) {
                                    "EXPLODING" -> CyberRed.copy(alpha = 0.15f)
                                    "HOT" -> NeonRedGlow.copy(alpha = 0.15f)
                                    else -> NeonCyan.copy(alpha = 0.1f)
                                },
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = trend.rate,
                                    color = when (trend.rate) {
                                        "EXPLODING" -> CyberRed
                                        "HOT" -> NeonRedGlow
                                        else -> NeonCyan
                                    },
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Niche: ${trend.category}",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = trend.growthRate,
                            color = if (trend.rate == "STABLE") NeonCyan else CyberRed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "Viral Score: ${trend.score}",
                            color = TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }

        // Section 3: Live tracked competitors
        item {
            Text(
                text = "KOMPETITOR CHANNEL LIVE TRACKING (MONITOR DATA)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        item {
            CyberCard(borderColor = Color.White.copy(alpha = 0.1f)) {
                viewModel.monitoredCompetitors.forEachIndexed { idx, competitor ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "@${competitor.name}",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${competitor.subs / 1000}K subs",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Surface(
                                color = if (competitor.trendState == "Exploding") CyberRed.copy(alpha = 0.15f) else MatteGray,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = competitor.trackingActivity,
                                    color = if (competitor.trendState == "Exploding") CyberRed else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    if (idx < viewModel.monitoredCompetitors.lastIndex) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MiniStatItem(label: String, valStr: String, highlightColor: Color) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 9.sp)
        Text(text = valStr, color = highlightColor, fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}


// ============================================
// TAB 2: DETEKSI ALGORITMA DIAGNOSTIC
// ============================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AlgorithmDiagnosticScreen(viewModel: MonitorViewModel) {
    var activeDiagnosticSubTab by remember { mutableStateOf(0) } // 0: Video Diagnostic, 1: Channel Audit & Competitors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        ApiStatusNotice()

        // Beautiful custom glowing segment buttons row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val subTabs = listOf("VIDEO DIAGNOSTIC", "CHANNEL AUDIT & COMPETITORS")
            subTabs.forEachIndexed { index, title ->
                val isSel = activeDiagnosticSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSel) CyberRed.copy(alpha = 0.15f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            1.dp,
                            if (isSel) CyberRed.copy(alpha = 0.6f) else Color.Transparent,
                            RoundedCornerShape(6.dp)
                        )
                        .clickable { activeDiagnosticSubTab = index }
                        .padding(vertical = 10.dp)
                        .testTag("diag_subtab_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSel) Color.White else TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (activeDiagnosticSubTab == 0) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "YOUTUBE SEO & STATS VIRAL SIMULATION TESTER",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Fields Inputs
                CyberCard(borderColor = CyberRed.copy(alpha = 0.25f)) {
                    // Video Title
                    Text("Judul Video Ideasi/Aktif", color = TextSecondary, fontSize = 11.sp)
                    OutlinedTextField(
                        value = viewModel.inputVideoTitle,
                        onValueChange = { viewModel.inputVideoTitle = it },
                        placeholder = { Text("E.g: Mematahkan Mitos Kecerdasan Buatan", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = CyberRed,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("diag_input_title")
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Retention rate
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Retensi Rata-Rata (%)", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputRetention,
                                onValueChange = { viewModel.inputRetention = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_retention")
                            )
                        }
                        // CTR
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Thumbnail CTR (%)", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputThumbnailCtr,
                                onValueChange = { viewModel.inputThumbnailCtr = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_ctr")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Watch Time Minutes
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Dengar Rata-Rata (m)", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputWatchTime,
                                onValueChange = { viewModel.inputWatchTime = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_watchtime")
                            )
                        }
                        // Like ratio
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Like vs Dislike (%)", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputLikeRatio,
                                onValueChange = { viewModel.inputLikeRatio = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_likeratio")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Comments count
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jumlah Koment", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputComments,
                                onValueChange = { viewModel.inputComments = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_comments")
                            )
                        }
                        // Shares count
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Jumlah Share", color = TextSecondary, fontSize = 11.sp)
                            OutlinedTextField(
                                value = viewModel.inputShares,
                                onValueChange = { viewModel.inputShares = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier.padding(vertical = 4.dp).testTag("diag_input_shares")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action run button
                    Button(
                        onClick = { viewModel.analyzeVideo() },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("diagnose_trigger_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "JALANKAN STRATEGI AI",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                // Output calculation state visualizer
                when (val state = viewModel.videoAnalysisState) {
                    is VideoAnalysisState.Idle -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Menunggu parameter input video untuk didiagnosa secara matang.",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is VideoAnalysisState.Loading -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 30.dp)
                        ) {
                            CircularProgressIndicator(color = CyberRed, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "PROSES INTEGRASI ALGORITMA GEMINI AI...",
                                color = NeonRedGlow,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Membongkar metric CTR, watchtime & menghitung probabilitas viralitas...",
                                color = TextMuted,
                                fontSize = 10.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    is VideoAnalysisState.Error -> {
                        Surface(
                            color = CyberRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CyberRed),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("ERROR DIAGNOSTIK AI", color = CyberRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(state.message, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }
                        }
                    }

                    is VideoAnalysisState.Success -> {
                        val res = state.result
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "LAPORAN VIRALITAS & ANALISIS AKHIR",
                                color = NeonCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(top = 8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadialScoreMeter(score = res.viralScore, modifier = Modifier.weight(1f))
                                
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(start = 12.dp)
                                ) {
                                    Text("WAKTU PUBLIKASI TERBAIK", color = TextSecondary, fontSize = 10.sp)
                                    Text(
                                        text = res.optimalUploadTime,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    Button(
                                        onClick = { viewModel.clearAnalysis() },
                                        colors = ButtonDefaults.buttonColors(containerColor = MatteGray),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Isi Ulang", color = Color.White, fontSize = 10.sp)
                                    }
                                }
                            }

                            // Alternatives Titles clickbaits
                            CyberCard(
                                title = "Ide Judul Optimasi High-CTR AI",
                                borderColor = NeonCyan.copy(alpha = 0.4f)
                            ) {
                                res.suggestedTitles.forEachIndexed { i, title ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Text(
                                                text = "#${i + 1}",
                                                color = NeonCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            // Hashtags global
                            CyberCard(title = "Rekomendasi Hashtag Viral") {
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    res.suggestedHashtags.forEach { hashtag ->
                                        Surface(
                                            color = CyberRed.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(8.dp),
                                            border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.3f))
                                        ) {
                                            Text(
                                                text = hashtag,
                                                color = NeonRedGlow,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // AI growth blueprint strategy
                            CyberCard(
                                title = "Blueprint Strategi Algoritma Kreator",
                                borderColor = NeonRedGlow.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = res.aiRecommendation,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    lineHeight = 17.sp
                                )
                            }
                            
                            Text(
                                text = "💡 Perhatian: Sesi diagnosa ini otomatis tersimpan di riwayat portofolio lokal Anda.",
                                color = TextMuted,
                                fontSize = 9.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        } else {
            ChannelAuditAndCompetitorScreen(viewModel)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChannelAuditAndCompetitorScreen(viewModel: MonitorViewModel) {
    val dbCompetitors by viewModel.dbCompetitors.collectAsState()
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section 1: Audit Form
        item {
            Text(
                text = "CEK PERFORMA CHANNEL & MONITORING KOMPETITOR",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            CyberCard(borderColor = CyberRed.copy(alpha = 0.3f)) {
                Text(
                    text = "Masukkan Username (@handle), Link Channel, atau URL Video akun manapun untuk membedah kinerja algoritmanya secara realtime.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                OutlinedTextField(
                    value = viewModel.inputChannelQuery,
                    onValueChange = { viewModel.inputChannelQuery = it },
                    placeholder = { Text("E.g: @gadgetins atau youtube.com/c/misteri", color = TextMuted) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = CyberRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("channel_query_input"),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = { viewModel.analyzeChannel() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("channel_audit_trigger_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AUDIT ALGORITMA CHANNEL",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
        
        // Section 2: Audit Result Display
        item {
            when (val state = viewModel.channelAuditState) {
                is ChannelAuditState.Idle -> {
                    CyberCard(borderColor = Color.White.copy(alpha = 0.05f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Analisis Taktikal Menang Kompetisi", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text("Gunakan fitur audit untuk menganalisa kelemahan kompetitor Anda, menyontek pola upload, dan menyusun strategi AI terbaik.", color = TextSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
                is ChannelAuditState.Loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = CyberRed, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("CYBER SCANNING CHANNEL ACTIVE...", color = NeonRedGlow, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        Text("Membongkar database stats, subscriber velocity, tingkat engagement & melacak video viral...", color = TextMuted, fontSize = 9.5.sp, textAlign = TextAlign.Center)
                    }
                }
                is ChannelAuditState.Error -> {
                    Surface(
                        color = CyberRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, CyberRed),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.message,
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
                is ChannelAuditState.Success -> {
                    val audit = state.result
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            text = "LAPORAN AUDIT: @${audit.channelName.uppercase()}",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadialScoreMeter(score = audit.algorithmPerformanceScore, modifier = Modifier.weight(1.1f))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("NICHE CHANNEL", color = TextSecondary, fontSize = 9.sp)
                                Text(audit.audienceNiche, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("ESTIMASI VIRAL RATE", color = TextSecondary, fontSize = 9.sp)
                                val rating = if (audit.algorithmPerformanceScore > 85) "SANGAT TINGGI" else "SEDANG-TINGGI"
                                Text(rating, color = if (audit.algorithmPerformanceScore > 85) CyberRed else NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                
                                Spacer(modifier = Modifier.height(10.dp))
                                Button(
                                    onClick = { viewModel.clearChannelAudit() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MatteGray),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text("Reset Audit", color = Color.White, fontSize = 9.sp)
                                }
                            }
                        }
                        
                        // Performance grid in CyberCards
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Total Sub", audit.totalSubscribers, "Subscriber")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Rata Views", audit.averageViews, "Per Video")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Pertumbuhan", audit.subscriberGrowth, "Bulan Ini")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Engagement", audit.engagementRate, "Koment & Like")
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Konsistensi", audit.uploadConsistency, "Pola Upload")
                            }
                            Box(modifier = Modifier.weight(1f)) {
                                MiniStatCard("Shorts Perfom", audit.shortsPerformance, "Rating Shorts")
                            }
                        }
                        
                        // SEO Keywords
                        CyberCard(title = "Kata Kunci Dominan Kompetitor") {
                            Text("Tag yang paling sering menarik trafik penonton mereka:", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.padding(bottom = 6.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                audit.frequentKeywords.split(",").forEach { kw ->
                                    val cleanKw = kw.trim()
                                    if (cleanKw.isNotEmpty()) {
                                        Surface(
                                            color = NeonCyan.copy(alpha = 0.1f),
                                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.3f)),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "#$cleanKw",
                                                color = NeonCyan,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Top Video
                        CyberCard(title = "Video Tersukses Kompetitor", borderColor = NeonCyan.copy(alpha = 0.3f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.List, contentDescription = null, tint = NeonCyan)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("JUDUL VIDEO UTAMA", color = TextSecondary, fontSize = 9.sp)
                                    Text(audit.topVideoTitle, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        
                        // Strengths & Weaknesses
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                CyberCard(title = "Kekuatan Channel", borderColor = Color.Green.copy(alpha = 0.3f)) {
                                    Text(audit.strengths, color = Color.White, fontSize = 10.5.sp, lineHeight = 15.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                CyberCard(title = "Kelemahan Channel", borderColor = CyberRed.copy(alpha = 0.3f)) {
                                    Text(audit.weaknesses, color = Color.White, fontSize = 10.5.sp, lineHeight = 15.sp)
                                }
                            }
                        }
                        
                        // AI strategy to beat
                        SelectionContainer {
                            CyberCard(title = "Siasat AI Mengalahkan Kompetitor", borderColor = NeonRedGlow) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        text = audit.aiStrategyToBeat,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        lineHeight = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "💡 Catatan: Analisis ini otomatis diarsipkan di riwayat Portofolio Anda.",
                                        color = TextMuted,
                                        fontSize = 9.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Section 3: Add new tracked competitor
        item {
            Text(
                text = "MONITORING DAFTAR KOMPETITOR AKTIF (SQLITE)",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(6.dp))
            CyberCard(borderColor = NeonCyan.copy(alpha = 0.3f)) {
                Text(
                    text = "Daftarkan nama channel kompetitor langsung ke database pengawasan SQLite lokal Anda agar Anda dapat memantau pola dan kebiasaan thumbnail mereka secara periodik.",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = viewModel.inputCompetitorName,
                        onValueChange = { viewModel.inputCompetitorName = it },
                        placeholder = { Text("E.g: TechReviewer", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("competitor_input_name")
                    )
                    OutlinedTextField(
                        value = viewModel.inputCompetitorNiche,
                        onValueChange = { viewModel.inputCompetitorNiche = it },
                        placeholder = { Text("E.g: Gadget", color = TextMuted) },
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)),
                        textStyle = MaterialTheme.typography.bodyMedium,
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("competitor_input_niche")
                    )
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Button(
                    onClick = { viewModel.addTrackedCompetitor() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                    modifier = Modifier.fillMaxWidth().height(42.dp).testTag("competitor_add_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("TAMBAH KE DAFTAR MONITOR", color = Color.White, fontSize = 10.5.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        }
        
        // Section 4: Live List of competitors from SQLite
        if (dbCompetitors.isEmpty()) {
            item {
                CyberCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = TextMuted, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Belum ada kompetitor terdaftar", color = TextSecondary, fontSize = 11.sp)
                        Text("Gunakan formulir di atas untuk mendaftarkan kompetitor perdana Anda.", color = TextMuted, fontSize = 10.sp)
                    }
                }
            }
        } else {
            items(dbCompetitors) { comp ->
                CyberCard(
                    title = comp.channelName,
                    borderColor = if (comp.trendState == "Exploding") CyberRed.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.15f)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(comp.subCount, color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                    Surface(
                                        color = if (comp.trendState == "Exploding") CyberRed.copy(alpha = 0.2f) else MatteGray,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = comp.trendState.uppercase(),
                                            color = if (comp.trendState == "Exploding") CyberRed else NeonCyan,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text("Rata-rata: ${comp.averageViews}", color = TextSecondary, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.deleteTrackedCompetitor(comp.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus kompetitor", tint = CyberRed.copy(alpha = 0.7f))
                            }
                        }
                        
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        
                        Text("📅 POLA UPLOAD & NICHE:", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(comp.uploadPattern, color = Color.White, fontSize = 10.5.sp)
                        
                        Text("🎨 STRATEGI DESAIN THUMBNAIL:", color = NeonRedGlow, fontSize = 9.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Text(comp.thumbnailStrategy, color = Color.White, fontSize = 10.5.sp)
                        
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Notifikasi Kecepatan Viral:", color = TextSecondary, fontSize = 9.5.sp)
                            Surface(
                                color = CyberRed.copy(alpha = 0.12f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LIVE: ${comp.trackingActivity}",
                                    color = CyberRed,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun MiniStatCard(label: String, valStr: String, desc: String) {
    Surface(
        color = Color.White.copy(alpha = 0.03f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = label.uppercase(), color = TextSecondary, fontSize = 8.5.sp, fontFamily = FontFamily.Monospace)
            Text(text = valStr, color = NeonCyan, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(vertical = 2.dp))
            Text(text = desc, color = TextMuted, fontSize = 9.sp)
        }
    }
}


// ============================================
// TAB 3: PENCARIAN NICHE VIRALITAS
// ============================================
@Composable
fun NicheFinderScreen(viewModel: MonitorViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ApiStatusNotice()

        Text(
            text = "AI GLOBAL NICHE SEARCH ENGINE ENGINE",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = viewModel.inputNicheQuery,
                onValueChange = { viewModel.inputNicheQuery = it },
                placeholder = { Text("E.g: horror, anime, fakta unik, AI", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = CyberRed,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f)
                ),
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("niche_query_input")
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.searchNiche() },
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                modifier = Modifier
                    .height(56.dp)
                    .testTag("niche_search_button")
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
            }
        }

        // Suggestions buttons
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Fakta Unik", "Horror", "AI", "Anime", "Misteri", "Gaming").forEach { n ->
                Surface(
                    color = MatteGray,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.clickable {
                        viewModel.inputNicheQuery = n
                        viewModel.searchNiche()
                    }
                ) {
                    Text(
                        text = "#$n",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        when (val state = viewModel.nicheAnalysisState) {
            is NicheAnalysisState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Temukan tingkat kompetisi, kesempatan viral, serta volume pasar YouTube untuk niche kreasi apa pun.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            is NicheAnalysisState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp)
                ) {
                    CircularProgressIndicator(color = CyberRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "MEMINDAI DATABASE TREN SECARA GLOBAL...",
                        color = NeonRedGlow,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            is NicheAnalysisState.Error -> {
                Surface(
                    color = CyberRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, CyberRed),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(state.message, color = Color.White, modifier = Modifier.padding(16.dp), fontSize = 11.sp)
                }
            }

            is NicheAnalysisState.Success -> {
                val res = state.result
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "HASIL KINERJA TREN: ${viewModel.inputNicheQuery.uppercase()}",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )

                    // Scoring panels
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            CyberCard(borderColor = CyberRed) {
                                Text("TINGKAT PERSAINGAN", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = "${res.competitionRate}%",
                                    color = if (res.competitionRate > 70) CyberRed else NeonCyan,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                LinearProgressIndicator(
                                    progress = { res.competitionRate.toFloat() / 100f },
                                    color = if (res.competitionRate > 70) CyberRed else NeonCyan,
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp)
                                )
                                Text(
                                    text = if (res.competitionRate > 70) "KOMPETISI TINGGI" else "RELATIF AMAN",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            CyberCard(borderColor = NeonCyan) {
                                Text("PELUANG VIRAL", color = TextSecondary, fontSize = 9.sp)
                                Text(
                                    text = "${res.viralChance}%",
                                    color = if (res.viralChance > 70) NeonCyan else NeonRedGlow,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                LinearProgressIndicator(
                                    progress = { res.viralChance.toFloat() / 100f },
                                    color = if (res.viralChance > 70) NeonCyan else NeonRedGlow,
                                    trackColor = Color.White.copy(alpha = 0.1f),
                                    modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 8.dp)
                                )
                                Text(
                                    text = if (res.viralChance > 70) "SANGAT POTENSIAL" else "SEDANG",
                                    color = TextMuted,
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }

                    // Search volume & Audience Int
                    CyberCard {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("ESTIMASI PENCARIAN /MO", color = TextSecondary, fontSize = 10.sp)
                                Text(res.searchVolume, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MINAT KHALAYAK", color = TextSecondary, fontSize = 10.sp)
                                Text(res.audienceInterest, color = NeonCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Growth note tactical advice
                    CyberCard(
                        title = "Siasat Algoritma di Niche ini",
                        borderColor = NeonRedGlow.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = res.growthNotes,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    // Save trend line action
                    Button(
                        onClick = { viewModel.saveNicheLocal(viewModel.inputNicheQuery, res) },
                        colors = ButtonDefaults.buttonColors(containerColor = MatteGray),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("save_niche_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Favorite, contentDescription = null, tint = CyberRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("SIMPAN NICHE INI KE PORTOFOLIO", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}


// ============================================
// TAB 4: AI GENERATOR (Shorts, script, title concept)
// ============================================
@Composable
fun GeneratorScreen(viewModel: MonitorViewModel) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ApiStatusNotice()

        Text(
            text = "YOUTUBE CYBER AI GENERATOR SUITE",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        CyberCard(borderColor = CyberRed.copy(alpha = 0.3f)) {
            // Choice Selector
            Text("Jenis Creator Tool AI", color = TextSecondary, fontSize = 11.sp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "SHORTS_SCRIPT" to "Yt Shorts Script",
                    "IDEA_GENERATOR" to "Ide Konten Baru",
                    "CLICKBAIT_TITLES" to "Judul Clickbait",
                    "THUMBNAIL_CONCEPT" to "Thumbnail Concept"
                ).forEach { item ->
                    val isSelected = viewModel.generatorType == item.first
                    Surface(
                        color = if (isSelected) CyberRed else MatteGray,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { viewModel.generatorType = item.first }
                    ) {
                        Text(
                            text = item.second,
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Topic niche line
            Text("Niche / Topik Utama", color = TextSecondary, fontSize = 11.sp)
            OutlinedTextField(
                value = viewModel.generatorNiche,
                onValueChange = { viewModel.generatorNiche = it },
                placeholder = { Text("E.g: Horor, Sains Populer, Eksperimen", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("gen_input_niche")
            )

            // Reference title
            Text("Judul Acuan (Opsional)", color = TextSecondary, fontSize = 11.sp)
            OutlinedTextField(
                value = viewModel.generatorTitle,
                onValueChange = { viewModel.generatorTitle = it },
                placeholder = { Text("E.g: 5 Kejadian Janggal Di Dunia", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .testTag("gen_input_title")
            )

            // Extra parameter briefs
            Text("Brief Tambahan / Tone Khusus", color = TextSecondary, fontSize = 11.sp)
            OutlinedTextField(
                value = viewModel.generatorBrief,
                onValueChange = { viewModel.generatorBrief = it },
                placeholder = { Text("E.g: Gunakan konsep interaksi tegang, berikan ending yang nge-cliffhanger, batasi durasi.", color = TextMuted) },
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = CyberRed, unfocusedBorderColor = Color.White.copy(alpha = 0.15f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .padding(vertical = 6.dp)
                    .testTag("gen_input_brief")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Run generate button
            Button(
                onClick = { viewModel.generateContent() },
                enabled = !viewModel.isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generator_trigger_button")
            ) {
                if (viewModel.isGenerating) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("BUAT MATERIAL TINGKAT TINGGI", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        // Output Result Generator Container
        viewModel.generatorResult?.let { res ->
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AI GENERATED CREATIVE MATERIAL",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                CyberCard(
                    title = res.title,
                    borderColor = NeonCyan
                ) {
                    SelectionContainer {
                        Text(
                            text = res.content,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Button(
                    onClick = { viewModel.saveContentLocal() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_script_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SIMPAN FILE INI KE PORTOFOLIO", color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}


// ============================================
// TAB 5: PORTOFOLIO SIMPANAN (Room Tracked Databases)
// ============================================
@Composable
fun SavedPortfolioScreen(viewModel: MonitorViewModel) {
    val trackedVideos by viewModel.trackedVideos.collectAsState()
    val savedNiches by viewModel.savedNiches.collectAsState()
    val savedScripts by viewModel.savedScripts.collectAsState()
    val channelAudits by viewModel.channelAudits.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0: Video Diagnosis, 1: Niche Saved, 2: AI Scripts, 3: Channel Audit

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Text(
                text = "PORTOFOLIO STRATEGI & KARYA GENERATOR",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            // Navigation Row inside saved portfolio selection list
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "Diagno. (${trackedVideos.size})" to 0,
                    "Niche (${savedNiches.size})" to 1,
                    "Skrip (${savedScripts.size})" to 2,
                    "Channel (${channelAudits.size})" to 3
                ).forEach { item ->
                    val isSel = activeSubTab == item.second
                    Surface(
                        color = if (isSel) CyberRed else MatteGray,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubTab = item.second }
                    ) {
                        Text(
                            text = item.first,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Sub Tab lists conditions
        if (activeSubTab == 0) {
            // Tracked Videos list
            if (trackedVideos.isEmpty()) {
                item {
                    EmptyHistoryNotice("Daftar Diagnosa Kosong", "Belum ada riwayat video yang diperiksa. Periksalah video di tab DIAGNOSTIK untuk menyimpannya di sini.")
                }
            } else {
                items(trackedVideos) { v ->
                    CyberCard(
                        title = v.videoTitle,
                        borderColor = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text("Skor Viral: ${v.viralScore}/100", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Upload Ideal: ${v.optimalUploadTime}", color = TextSecondary, fontSize = 10.sp)
                                Text("Statistik input: Retensi ${v.retentionPercent}%, CTR ${v.thumbnailCtr}%", color = TextMuted, fontSize = 9.5.sp)
                            }
                            IconButton(onClick = { viewModel.deleteTrackedVideo(v.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 1) {
            // Saved niches list
            if (savedNiches.isEmpty()) {
                item {
                    EmptyHistoryNotice("Daftar Niche Kosong", "Telusuri niche idaman Anda di tab NICHES dan klik simpan untuk menyimpannya di daftar pantauan ini.")
                }
            } else {
                items(savedNiches) { n ->
                    CyberCard(
                        title = n.name,
                        borderColor = NeonCyan.copy(alpha = 0.2f)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Kompetisi: ${n.competitionRate}%", color = CyberRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Peluang: ${n.viralChance}%", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Pasar: ${n.searchVolume}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = n.audienceInterest,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 6.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.deleteNiche(n.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed.copy(alpha = 0.7f))
                            }
                        }
                    }
                }
            }
        } else if (activeSubTab == 2) {
            // Saved scripts content generator list
            if (savedScripts.isEmpty()) {
                item {
                    EmptyHistoryNotice("Daftar Skrip Kosong", "Belum ada skrip kreasi yang disimpan. Buatlah skrip baru di tab CREATOR TOOLS dan klik Simpan File.")
                }
            } else {
                items(savedScripts) { s ->
                    var isExpanded by remember { mutableStateOf(false) }
                    CyberCard(
                        title = s.title,
                        borderColor = NeonRedGlow.copy(alpha = 0.25f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Niche: ${s.niche.uppercase()} • Tipe: ${s.conceptType}",
                                    color = NeonCyan,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = s.generatedContent,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.clickable { isExpanded = !isExpanded }
                                )
                                Text(
                                    text = if (isExpanded) "Klik untuk menyembunyikan..." else "Klik untuk baca selengkapnya...",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                IconButton(onClick = { viewModel.deleteScript(s.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed.copy(alpha = 0.7f))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Channel Audits list
            if (channelAudits.isEmpty()) {
                item {
                    EmptyHistoryNotice("Daftar Audit Kosong", "Belum ada riwayat audit channel kompetitor yang disimpan. Audit channel kompetitor di menu DIAGNOSTIK > CHANNEL AUDIT.")
                }
            } else {
                items(channelAudits) { a ->
                    var isExpanded by remember { mutableStateOf(false) }
                    CyberCard(
                        title = "@${a.channelName} (${a.audienceNiche})",
                        borderColor = NeonRedGlow.copy(alpha = 0.3f)
                    ) {
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text("Algoritma Score: ${a.algorithmPerformanceScore}/100", color = CyberRed, fontWeight = FontWeight.Bold, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                                    Text("Subscribers: ${a.totalSubscribers} • Avg Views: ${a.averageViews}", color = Color.White, fontSize = 11.sp)
                                    Text("Engagement: ${a.engagementRate} • Konsistensi: ${a.uploadConsistency}", color = TextSecondary, fontSize = 10.sp)
                                }
                                IconButton(onClick = { viewModel.deleteChannelAudit(a.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = CyberRed.copy(alpha = 0.7f))
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Niche: ${a.audienceNiche} • Top Video: ${a.topVideoTitle}",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("💪 KEKUATAN CHANNEL:", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(a.strengths, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                                
                                Text("⚠️ KELEMAHAN CHANNEL:", color = CyberRed, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(a.weaknesses, color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                                
                                Text("🚀 MEMENANGKAN ALGORITMA (TAKTIK AI):", color = NeonRedGlow, fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                Text(a.aiStrategyToBeat, color = Color.White, fontSize = 11.sp, lineHeight = 16.sp)
                            }
                            
                            Text(
                                text = if (isExpanded) "Klik untuk menyembunyikan detail..." else "Klik untuk melihat analisis taktik AI...",
                                color = TextMuted,
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .clickable { isExpanded = !isExpanded }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun EmptyHistoryNotice(title: String, desc: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(36.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = title.uppercase(), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
        Text(
            text = desc,
            color = TextSecondary,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp)
        )
    }
}
