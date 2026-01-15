package com.um.visamate.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// PaymentViewModel mocks payment processing (F.R.3.x)
class PaymentViewModel : ViewModel() {
    fun pay(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            delay(800)
            onResult(true)
        }
    }
}

