package com.capeddle.namethattunelab.domain.repository

import com.capeddle.namethattunelab.domain.model.AppSettings
import kotlinx.coroutines.flow.Flow

interface AppSettingsRepository {
    fun observeSettings(): Flow<AppSettings>

    suspend fun updateSettings(settings: AppSettings)
}
