package com.capeddle.namethattunelab.domain.usecase

import com.capeddle.namethattunelab.domain.model.AppSettings
import com.capeddle.namethattunelab.domain.repository.AppSettingsRepository
import javax.inject.Inject

class UpdateAppSettingsUseCase @Inject constructor(
    private val appSettingsRepository: AppSettingsRepository
) {
    suspend operator fun invoke(settings: AppSettings) {
        appSettingsRepository.updateSettings(settings)
    }
}
