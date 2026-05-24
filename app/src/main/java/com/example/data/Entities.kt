package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracked_videos")
data class TrackedVideo(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val videoTitle: String,
    val watchTimeMinutes: Float,
    val retentionPercent: Float,
    val likeRatioPercent: Float,
    val thumbnailCtr: Float,
    val commentsCount: Int,
    val sharesCount: Int,
    val viralScore: Int,
    val optimalUploadTime: String,
    val suggestedTitles: String,
    val suggestedHashtags: String,
    val aiRecommendation: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_niches")
data class SavedNiche(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val competitionRate: Int, // 1 to 100
    val viralChance: Int, // 1 to 100
    val searchVolume: String,
    val audienceInterest: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_scripts")
data class SavedScript(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val niche: String,
    val conceptType: String, // "SHORTS_SCRIPT", "IDEA_GENERATOR", "CLICKBAIT_TITLES", "THUMBNAIL_CONCEPT"
    val generatedContent: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "channel_audits")
data class ChannelAudit(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val queryInput: String,
    val channelName: String,
    val totalSubscribers: String,
    val averageViews: String,
    val subscriberGrowth: String,
    val engagementRate: String,
    val uploadConsistency: String,
    val shortsPerformance: String,
    val audienceNiche: String,
    val frequentKeywords: String,
    val topVideoTitle: String,
    val algorithmPerformanceScore: Int,
    val strengths: String,
    val weaknesses: String,
    val aiStrategyToBeat: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tracked_competitors")
data class TrackedCompetitor(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val channelName: String,
    val subCount: String,
    val averageViews: String,
    val uploadPattern: String,
    val thumbnailStrategy: String,
    val trackingActivity: String,
    val trendState: String, // Exploding, Hot, Stable
    val timestamp: Long = System.currentTimeMillis()
)
