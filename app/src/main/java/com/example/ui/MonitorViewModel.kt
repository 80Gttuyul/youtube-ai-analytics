package com.example.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.api.AnalysisResult
import com.example.api.GeminiClient
import com.example.api.GeneratorResult
import com.example.api.NicheResult
import com.example.api.ChannelAuditResult
import com.example.data.MonitorRepository
import com.example.data.SavedNiche
import com.example.data.SavedScript
import com.example.data.TrackedVideo
import com.example.data.ChannelAudit
import com.example.data.TrackedCompetitor
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class DashboardTab {
    DASHBOARD,
    ALGORITHM,
    NICHES,
    GENERATOR,
    SAVED
}

sealed interface VideoAnalysisState {
    object Idle : VideoAnalysisState
    object Loading : VideoAnalysisState
    data class Success(val result: AnalysisResult) : VideoAnalysisState
    data class Error(val message: String) : VideoAnalysisState
}

sealed interface NicheAnalysisState {
    object Idle : NicheAnalysisState
    object Loading : NicheAnalysisState
    data class Success(val result: NicheResult) : NicheAnalysisState
    data class Error(val message: String) : NicheAnalysisState
}

sealed interface ChannelAuditState {
    object Idle : ChannelAuditState
    object Loading : ChannelAuditState
    data class Success(val result: ChannelAuditResult) : ChannelAuditState
    data class Error(val message: String) : ChannelAuditState
}

class MonitorViewModel(private val repository: MonitorRepository) : ViewModel() {

    // Theme Screen Navigation
    var currentTab by mutableStateOf(DashboardTab.DASHBOARD)

    // Flow integration for Room State
    val trackedVideos: StateFlow<List<TrackedVideo>> = repository.allTrackedVideos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedNiches: StateFlow<List<SavedNiche>> = repository.allSavedNiches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedScripts: StateFlow<List<SavedScript>> = repository.allSavedScripts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val channelAudits: StateFlow<List<ChannelAudit>> = repository.allChannelAudits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dbCompetitors: StateFlow<List<TrackedCompetitor>> = repository.allTrackedCompetitors
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Competitor and Channel checking inputs and state ---
    var inputChannelQuery by mutableStateOf("")
    var channelAuditState by mutableStateOf<ChannelAuditState>(ChannelAuditState.Idle)
        private set

    var inputCompetitorName by mutableStateOf("")
    var inputCompetitorNiche by mutableStateOf("")

    // --- Tab 1: Live Simulated Analytics & Competitor Tracker ---
    val premiumGlobalTrends = listOf(
        TrendIndicator("AI Fakta Unik", "AI / Tech", 96, "EXPLODING", "+142%"),
        TrendIndicator("Misteri Nusantara", "Horor / Misteri", 88, "HOT", "+78%"),
        TrendIndicator("Minecraft Speedrun 100 Hari", "Gaming", 82, "STABLE", "+12%"),
        TrendIndicator("Deep-Fried ASMR Mukbang", "Mukbang / Food", 89, "EXPLODING", "+115%"),
        TrendIndicator("Konspirasi Masa Depan", "Filsafat / Sains", 75, "STABLE", "+8%"),
        TrendIndicator("Review Anime Underated", "Anime", 84, "HOT", "+62%"),
        TrendIndicator("Shorts Kehidupan Indonesia", "Daily Life", 91, "EXPLODING", "+210%")
    )

    val monitoredCompetitors = listOf(
        CompetitorChannel("NicheHorrorMaster", 420500, "+2.4K views/hr", "Exploding"),
        CompetitorChannel("SainsFaktaPopuler", 1180000, "+5.1K views/hr", "Stable"),
        CompetitorChannel("AnimeSpotlightID", 92000, "+800 views/hr", "Hot"),
        CompetitorChannel("AICraftGamer", 340000, "+3.2K views/hr", "Exploding")
    )

    // --- Tab 2: Algorithm Diagnostic Input States & Flow ---
    var inputVideoTitle by mutableStateOf("")
    var inputWatchTime by mutableStateOf("4.5")
    var inputRetention by mutableStateOf("65.0")
    var inputLikeRatio by mutableStateOf("92.5")
    var inputThumbnailCtr by mutableStateOf("7.8")
    var inputComments by mutableStateOf("450")
    var inputShares by mutableStateOf("120")

    var videoAnalysisState by mutableStateOf<VideoAnalysisState>(VideoAnalysisState.Idle)
        private set

    fun analyzeVideo() {
        val title = inputVideoTitle.ifBlank { "Optimasi Algoritma Video ${System.currentTimeMillis()}" }
        val watchTime = inputWatchTime.toFloatOrNull() ?: 4.5f
        val retention = inputRetention.toFloatOrNull() ?: 60f
        val likeRatio = inputLikeRatio.toFloatOrNull() ?: 90f
        val ctr = inputThumbnailCtr.toFloatOrNull() ?: 6.5f
        val comments = inputComments.toIntOrNull() ?: 150
        val shares = inputShares.toIntOrNull() ?: 50

        videoAnalysisState = VideoAnalysisState.Loading
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeVideo(title, watchTime, retention, likeRatio, ctr, comments, shares)
                videoAnalysisState = VideoAnalysisState.Success(result)

                // AUTO-SAVE to SQLite Tracking History on Success
                repository.insertTrackedVideo(
                    TrackedVideo(
                        videoTitle = title,
                        watchTimeMinutes = watchTime,
                        retentionPercent = retention,
                        likeRatioPercent = likeRatio,
                        thumbnailCtr = ctr,
                        commentsCount = comments,
                        sharesCount = shares,
                        viralScore = result.viralScore,
                        optimalUploadTime = result.optimalUploadTime,
                        suggestedTitles = result.suggestedTitles.joinToString("\n"),
                        suggestedHashtags = result.suggestedHashtags.joinToString(","),
                        aiRecommendation = result.aiRecommendation
                    )
                )
            } catch (e: Exception) {
                videoAnalysisState = VideoAnalysisState.Error(e.message ?: "Koneksi API Gagal.")
            }
        }
    }

    fun clearAnalysis() {
        videoAnalysisState = VideoAnalysisState.Idle
    }

    // --- Tab 3: Niche Finder Input States & Flow ---
    var inputNicheQuery by mutableStateOf("")
    var nicheAnalysisState by mutableStateOf<NicheAnalysisState>(NicheAnalysisState.Idle)
        private set

    fun searchNiche() {
        val niche = inputNicheQuery.trim()
        if (niche.isEmpty()) return

        nicheAnalysisState = NicheAnalysisState.Loading
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeNiche(niche)
                nicheAnalysisState = NicheAnalysisState.Success(result)
            } catch (e: Exception) {
                nicheAnalysisState = NicheAnalysisState.Error(e.message ?: "Pencarian Niche Gagal.")
            }
        }
    }

    fun saveNicheLocal(nicheName: String, result: NicheResult) {
        viewModelScope.launch {
            repository.insertSavedNiche(
                SavedNiche(
                    name = nicheName,
                    competitionRate = result.competitionRate,
                    viralChance = result.viralChance,
                    searchVolume = result.searchVolume,
                    audienceInterest = result.audienceInterest + "\n💡 " + result.growthNotes
                )
            )
        }
    }

    fun deleteNiche(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedNicheById(id)
        }
    }

    // --- Tab 4: AI Contents Ideas & Shorts Generator ---
    var generatorNiche by mutableStateOf("")
    var generatorTitle by mutableStateOf("")
    var generatorType by mutableStateOf("SHORTS_SCRIPT") // SHORTS_SCRIPT, IDEA_GENERATOR, CLICKBAIT_TITLES, THUMBNAIL_CONCEPT
    var generatorBrief by mutableStateOf("")

    var generatorResult by mutableStateOf<GeneratorResult?>(null)
    var isGenerating by mutableStateOf(false)

    fun generateContent() {
        val niche = generatorNiche.ifBlank { "Umum" }
        val title = generatorTitle.ifBlank { "Ide Hebat" }
        val brief = generatorBrief

        isGenerating = true
        generatorResult = null
        viewModelScope.launch {
            try {
                val result = GeminiClient.generateShortsTool(title, niche, generatorType, brief)
                generatorResult = result
            } catch (e: Exception) {
                generatorResult = GeneratorResult("Sistem Overload", "Gagal menghubungi modul AI. Silahkan coba lagi nanti.")
            } finally {
                isGenerating = false
            }
        }
    }

    fun saveContentLocal() {
        val result = generatorResult ?: return
        viewModelScope.launch {
            repository.insertSavedScript(
                SavedScript(
                    title = result.title,
                    niche = generatorNiche,
                    conceptType = generatorType,
                    generatedContent = result.content
                )
            )
        }
    }

    fun deleteScript(id: Int) {
        viewModelScope.launch {
            repository.deleteSavedScriptById(id)
        }
    }

    // --- Channel and Competitor Operations ---
    fun analyzeChannel() {
        val query = inputChannelQuery.trim()
        if (query.isEmpty()) return

        channelAuditState = ChannelAuditState.Loading
        viewModelScope.launch {
            try {
                val result = GeminiClient.analyzeChannel(query)
                channelAuditState = ChannelAuditState.Success(result)

                // Auto-save audit history in Database
                repository.insertChannelAudit(
                    ChannelAudit(
                        queryInput = query,
                        channelName = result.channelName,
                        totalSubscribers = result.totalSubscribers,
                        averageViews = result.averageViews,
                        subscriberGrowth = result.subscriberGrowth,
                        engagementRate = result.engagementRate,
                        uploadConsistency = result.uploadConsistency,
                        shortsPerformance = result.shortsPerformance,
                        audienceNiche = result.audienceNiche,
                        frequentKeywords = result.frequentKeywords,
                        topVideoTitle = result.topVideoTitle,
                        algorithmPerformanceScore = result.algorithmPerformanceScore,
                        strengths = result.strengths,
                        weaknesses = result.weaknesses,
                        aiStrategyToBeat = result.aiStrategyToBeat
                    )
                )
            } catch (e: Exception) {
                channelAuditState = ChannelAuditState.Error(e.message ?: "Gagal memproses analisis channel.")
            }
        }
    }

    fun clearChannelAudit() {
        channelAuditState = ChannelAuditState.Idle
    }

    fun addTrackedCompetitor() {
        val name = inputCompetitorName.trim()
        if (name.isEmpty()) return
        val niche = inputCompetitorNiche.ifBlank { "Kreator/Niche Umum" }

        viewModelScope.launch {
            // Generate simulated data points matching high-end YouTube studio analytics
            val sampleAct = "+${(100..4500).random()} views/jam"
            val trend = listOf("Exploding", "Hot", "Stable").random()
            val subStr = "${(50..3500).random()}K subscribers"
            val viewStr = "${(10..450).random()}K views/video"
            
            repository.insertTrackedCompetitor(
                TrackedCompetitor(
                    channelName = name,
                    subCount = subStr,
                    averageViews = viewStr,
                    uploadPattern = "Konsisten (${(1..4).random()}/minggu) • Niche: $niche",
                    thumbnailStrategy = "Skema merah cyber neon tebal, font brutalist kontras tinggi, ekspresi emosional ekstrim",
                    trackingActivity = sampleAct,
                    trendState = trend
                )
            )
            inputCompetitorName = ""
            inputCompetitorNiche = ""
        }
    }

    fun deleteTrackedCompetitor(id: Int) {
        viewModelScope.launch {
            repository.deleteTrackedCompetitorById(id)
        }
    }

    fun deleteChannelAudit(id: Int) {
        viewModelScope.launch {
            repository.deleteChannelAuditById(id)
        }
    }

    fun deleteTrackedVideo(id: Int) {
        viewModelScope.launch {
            repository.deleteTrackedVideoById(id)
        }
    }
}

// Helper models for mock list
data class TrendIndicator(
    val title: String,
    val category: String,
    val score: Int,
    val rate: String, // EXPLODING, HOT, STABLE
    val growthRate: String
)

data class CompetitorChannel(
    val name: String,
    val subs: Int,
    val trackingActivity: String,
    val trendState: String // Exploding, Stable, Hot
)
