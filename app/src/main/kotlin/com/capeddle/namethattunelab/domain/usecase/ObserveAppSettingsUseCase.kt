package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = appSettingsRepository.observeSettings()
}
