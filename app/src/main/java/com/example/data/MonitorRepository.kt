package com.example.data

import kotlinx.coroutines.flow.Flow

class MonitorRepository(private val monitorDao: MonitorDao) {
    val allTrackedVideos: Flow<List<TrackedVideo>> = monitorDao.getAllTrackedVideos()
    val allSavedNiches: Flow<List<SavedNiche>> = monitorDao.getAllSavedNiches()
    val allSavedScripts: Flow<List<SavedScript>> = monitorDao.getAllSavedScripts()
    val allChannelAudits: Flow<List<ChannelAudit>> = monitorDao.getAllChannelAudits()
    val allTrackedCompetitors: Flow<List<TrackedCompetitor>> = monitorDao.getAllTrackedCompetitors()

    suspend fun insertTrackedVideo(video: TrackedVideo) {
        monitorDao.insertTrackedVideo(video)
    }

    suspend fun deleteTrackedVideoById(id: Int) {
        monitorDao.deleteTrackedVideoById(id)
    }

    suspend fun insertSavedNiche(niche: SavedNiche) {
        monitorDao.insertSavedNiche(niche)
    }

    suspend fun deleteSavedNicheById(id: Int) {
        monitorDao.deleteSavedNicheById(id)
    }

    fun doesNicheExist(name: String): Flow<Boolean> {
        return monitorDao.doesNicheExist(name)
    }

    suspend fun insertSavedScript(script: SavedScript) {
        monitorDao.insertSavedScript(script)
    }

    suspend fun deleteSavedScriptById(id: Int) {
        monitorDao.deleteSavedScriptById(id)
    }

    suspend fun insertChannelAudit(audit: ChannelAudit) {
        monitorDao.insertChannelAudit(audit)
    }

    suspend fun deleteChannelAuditById(id: Int) {
        monitorDao.deleteChannelAuditById(id)
    }

    suspend fun insertTrackedCompetitor(competitor: TrackedCompetitor) {
        monitorDao.insertTrackedCompetitor(competitor)
    }

    suspend fun deleteTrackedCompetitorById(id: Int) {
        monitorDao.deleteTrackedCompetitorById(id)
    }
}
