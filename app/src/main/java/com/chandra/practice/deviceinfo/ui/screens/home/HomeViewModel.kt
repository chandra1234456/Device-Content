package com.chandra.practice.deviceinfo.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.chandra.practice.deviceinfo.data.model.DeviceInfoItem
import com.chandra.practice.deviceinfo.data.model.InfoCategory
import com.chandra.practice.deviceinfo.data.repository.DeviceInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: DeviceInfoRepository,
    initialCategory: InfoCategory = InfoCategory.OVERVIEW,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(selectedCategory = initialCategory))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadQuickStats()
        loadHealthScore()
        // Load the initial category directly rather than through selectCategory(), whose
        // same-category guard would otherwise treat this as a no-op since the state already
        // starts on initialCategory.
        viewModelScope.launch { loadCategory(initialCategory) }
    }

    fun selectCategory(category: InfoCategory) {
        if (category == _uiState.value.selectedCategory) return
        _uiState.update { it.copy(selectedCategory = category, content = CategoryContent.Loading, searchQuery = "") }
        viewModelScope.launch { loadCategory(category) }
    }

    /** Reloads only the currently selected category — used after camera permission changes. */
    fun reloadCurrentCategory() {
        val category = _uiState.value.selectedCategory
        repository.invalidate(category)
        viewModelScope.launch { loadCategory(category) }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    /** Category title and items currently on screen, for PDF export. */
    fun currentExportData(): Pair<String, List<DeviceInfoItem>> {
        val state = _uiState.value
        return state.selectedCategory.title to (state.content as? CategoryContent.Success)?.items.orEmpty()
    }

    private suspend fun loadCategory(category: InfoCategory) {
        val result = runCatching { repository.getCategoryInfo(category) }
        _uiState.update { state ->
            // Drop stale results if the user already switched to another category.
            if (state.selectedCategory != category) return@update state
            val content = result.fold(
                onSuccess = { items ->
                    if (items.isNotEmpty()) CategoryContent.Success(items) else CategoryContent.Empty("Nothing to show here.")
                },
                onFailure = { e -> CategoryContent.Error(e.message ?: "Something went wrong reading this section.") },
            )
            state.copy(content = content)
        }
    }

    private fun loadQuickStats() {
        viewModelScope.launch {
            val stats = repository.getQuickStats()
            _uiState.update { it.copy(quickStats = stats) }
        }
    }

    private fun loadHealthScore() {
        viewModelScope.launch {
            val score = repository.healthScore()
            _uiState.update { it.copy(healthScore = score) }
        }
    }

    class Factory(
        private val repository: DeviceInfoRepository,
        private val initialCategory: InfoCategory = InfoCategory.OVERVIEW,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            HomeViewModel(repository, initialCategory) as T
    }
}
