package net.runner.rshot

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MainViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    fun onSearchTextChange(query: String) {
        _searchText.value = query
    }

    fun onToggleSearch(active:Boolean) {
        _isSearching.value = active
    }

    fun filterData(data: List<DataClass>, query: String): List<DataClass> {
        return if (query.isEmpty()) {
            data
        } else {
            data.filter {
                it.tag.contains(query, ignoreCase = true) || it.subject.contains(query, ignoreCase = true)
            }
        }
    }
}
