package com.um.visamate.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.um.visamate.utils.FakeDatabase
import kotlinx.coroutines.launch

// DashboardViewModel provides quick-loading summaries to meet NFR 2.1
class DashboardViewModel : ViewModel() {
    fun loadUserSubmissions(userId: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val submission = FakeDatabase.getSubmissionForUser(userId)
            val count = if (submission != null) 1 else 0
            onResult(count)
        }
    }
}
