package com.selves.xnn.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.selves.xnn.data.MemberPreferences
import com.selves.xnn.model.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val memberPreferences: MemberPreferences
) : ViewModel() {
    
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()
    
    private val _showThemeModeDialog = MutableStateFlow(false)
    val showThemeModeDialog: StateFlow<Boolean> = _showThemeModeDialog.asStateFlow()
    
    init {
        viewModelScope.launch {
            memberPreferences.themeMode.collect { mode ->
                _themeMode.value = mode
            }
        }
    }
    
    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch {
            memberPreferences.saveThemeMode(themeMode)
        }
    }
    
    fun showThemeModeDialog() {
        _showThemeModeDialog.value = true
    }
    
    fun hideThemeModeDialog() {
        _showThemeModeDialog.value = false
    }
} 