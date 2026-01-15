package com.um.visamate.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.launch

// SettingsViewModel: admin actions (toggle final lock)
class SettingsViewModel : ViewModel() {
    fun setFinalLock(locked: Boolean) {
        viewModelScope.launch {
            FakeDatabase.setFinalSubmissionLocked(locked)
        }
    }
}

