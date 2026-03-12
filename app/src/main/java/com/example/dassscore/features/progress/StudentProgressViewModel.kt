package com.example.dassscore.features.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dassscore.data.repository.FirebaseRepository
import com.example.dassscore.model.DassResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StudentProgressViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _studentResults = MutableStateFlow<List<DassResult>>(emptyList())
    val studentResults: StateFlow<List<DassResult>> = _studentResults

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun fetchResults(userId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            repository
                    .getUserDassResults(userId)
                    .fold(
                            onSuccess = { results -> _studentResults.value = results },
                            onFailure = { e ->
                                _error.value = e.message ?: "Failed to fetch results"
                            }
                    )
            _isLoading.value = false
        }
    }
}
