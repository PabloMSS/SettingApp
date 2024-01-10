package com.example.settingapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.settingapp.databinding.ActivitySettingBinding
import com.google.android.material.slider.RangeSlider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingActivity : AppCompatActivity() {

    companion object{
        const val preferenceDarkMode = "DarkMode"
        const val preferenceBluetooth = "Bluetooth"
        const val preferenceVibration = "Vibration"
        const val preferenceVolumen = "Volumen"
    }

    private var fristTime = true
    private lateinit var binding: ActivitySettingBinding

    private lateinit var switchDarkMode: Switch
    private lateinit var switchBluetooth: Switch
    private lateinit var switchVibration: Switch
    private lateinit var sliderVolume: RangeSlider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.IO).launch {
            getAllSettingS().filter { fristTime }.collect{settingsModel ->
                if(settingsModel != null){
                    runOnUiThread {
                        switchDarkMode.isChecked = settingsModel.backMode
                        switchVibration.isChecked = settingsModel.vibration
                        switchBluetooth.isChecked = settingsModel.bluetooth
                        sliderVolume.setValues(settingsModel.volume.toFloat())
                        fristTime = false
                    }
                }
            }
        }

        iniUI()
    }

    fun iniUI(){
        switchDarkMode = binding.switchDarkMode
        switchBluetooth = binding.switchBluetooth
        switchVibration = binding.switchVibration
        sliderVolume = binding.sliderVolume

        getAllSettingS()

        switchDarkMode.setOnCheckedChangeListener { _, value ->
            if(value)
                enableDarkMode()
            else
                disableDarkMode()
            
            CoroutineScope(Dispatchers.IO).launch {
                saveConfigSwitch(preferenceDarkMode, value)
            }
        }
        switchBluetooth.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveConfigSwitch(preferenceBluetooth, value)
            }
        }
        switchVibration.setOnCheckedChangeListener { _, value ->
            CoroutineScope(Dispatchers.IO).launch {
                saveConfigSwitch(preferenceVibration, value)
            }
        }

        sliderVolume.addOnChangeListener { _, value, _ ->
            CoroutineScope(Dispatchers.IO).launch{
                saveVolumen(value.toInt())
            }
        }
    }

    suspend fun saveVolumen(value: Int){
        dataStore.edit { preferences ->
            preferences[intPreferencesKey(preferenceVolumen)] = value
        }
    }

    suspend fun saveConfigSwitch(nameConfig: String, value: Boolean){
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(nameConfig)] = value
        }
    }

    fun getAllSettingS(): Flow<SettingsModel?> {
        return dataStore.data.map {preferences ->
            SettingsModel(
                backMode = preferences[booleanPreferencesKey(preferenceDarkMode)] ?: false,
                bluetooth = preferences[booleanPreferencesKey(preferenceBluetooth)] ?: true,
                vibration = preferences[booleanPreferencesKey(preferenceVibration)] ?: true,
                volume = preferences[intPreferencesKey(preferenceVolumen)] ?: 50
            )
        }
    }

    fun enableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        delegate.applyDayNight()
    }

    fun disableDarkMode(){
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        delegate.applyDayNight()
    }
}