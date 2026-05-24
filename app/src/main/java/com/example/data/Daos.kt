package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitorDao {
    // Tracked Videos queries
    @Query("SELECT * FROM tracked_videos ORDER BY timestamp DESC")
    fun getAllTrackedVideos(): Flow<List<TrackedVideo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedVideo(video: TrackedVideo)

    @Query("DELETE FROM tracked_videos WHERE id = :id")
    suspend fun deleteTrackedVideoById(id: Int)

    // Saved Niches queries
    @Query("SELECT * FROM saved_niches ORDER BY timestamp DESC")
    fun getAllSavedNiches(): Flow<List<SavedNiche>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedNiche(niche: SavedNiche)

    @Query("DELETE FROM saved_niches WHERE id = :id")
    suspend fun deleteSavedNicheById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_niches WHERE LOWER(name) = LOWER(:name) LIMIT 1)")
    fun doesNicheExist(name: String): Flow<Boolean>

    // Generated Scripts / Ideas queries
    @Query("SELECT * FROM saved_scripts ORDER BY timestamp DESC")
    fun getAllSavedScripts(): Flow<List<SavedScript>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedScript(script: SavedScript)

    @Query("DELETE FROM saved_scripts WHERE id = :id")
    suspend fun deleteSavedScriptById(id: Int)

    // Channel Audits queries
    @Query("SELECT * FROM channel_audits ORDER BY timestamp DESC")
    fun getAllChannelAudits(): Flow<List<ChannelAudit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannelAudit(audit: ChannelAudit)

    @Query("DELETE FROM channel_audits WHERE id = :id")
    suspend fun deleteChannelAuditById(id: Int)

    // Tracked Competitors queries
    @Query("SELECT * FROM tracked_competitors ORDER BY timestamp DESC")
    fun getAllTrackedCompetitors(): Flow<List<TrackedCompetitor>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrackedCompetitor(competitor: TrackedCompetitor)

    @Query("DELETE FROM tracked_competitors WHERE id = :id")
    suspend fun deleteTrackedCompetitorById(id: Int)
}
