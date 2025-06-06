package com.popolam.app.dailyfact.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.popolam.app.dailyfact.data.model.Fact
import com.popolam.app.dailyfact.data.repository.FactRepository
import com.popolam.app.dailyfact.domain.GetDailyFactUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FactUiState(
    val isLoading: Boolean = true,
    val fact: Fact? = null,
    val error: String? = null
)

class FactViewModel(
    private val getDailyFactUseCase: GetDailyFactUseCase,
    private val factRepository: FactRepository // For refresh action
) : ViewModel() {

    private val _uiState = MutableStateFlow(FactUiState())
    val uiState: StateFlow<FactUiState> = _uiState

    init {
        observeDailyFact()
    }

    private fun observeDailyFact() {
        viewModelScope.launch {
            getDailyFactUseCase().collect { fact ->
                if (fact != null) {
                    _uiState.update { it.copy(isLoading = false, fact = fact, error = null) }
                } else {
                    // No fact yet, might be fetching for the first time or an error occurred during fetch.
                    // The worker should handle the daily fetch.
                    // We can also trigger an initial fetch if the DB is empty on app start.
                    _uiState.update { it.copy(isLoading = true, fact = null) } // Show loading until worker provides one
                    // Optionally, trigger a fetch if it's the very first launch and no fact exists
                    // This check needs to be more robust (e.g. check if a fact for *today* exists)
                    if (_uiState.value.fact == null) { // Simplified check
                        refreshFact()
                    }
                }
            }
        }
    }

    fun refreshFact() {
        _uiState.update { it.copy(isLoading = true, error = null, fact = null) }
        viewModelScope.launch {
            val result = factRepository.forceRefreshFact()
            if (result.isFailure) {
                _uiState.update { it.copy(isLoading = false, error = "Failed to load new fact.") }
            }
            // Success is handled by the Flow from getDailyFactUseCase
        }
    }
}