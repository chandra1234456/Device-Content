package com.chandra.practice.deviceinfo.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

data class SensorInfo(
    val id: Int,
    val type: Int,
    val name: String,
    val vendor: String,
    val version: Int,
    val power: Float,
    val resolution: Float,
    val maxRange: Float,
    val stringType: String,
)

data class SensorDataSample(
    val timestamp: Long,
    val values: List<Float>,
)

class SensorExplorerRepository(context: Context) {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    fun getAllSensors(): List<SensorInfo> {
        val sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL)
        return sensorList.mapIndexed { index, sensor ->
            SensorInfo(
                id = index,
                type = sensor.type,
                name = sensor.name ?: "Unknown Sensor",
                vendor = sensor.vendor ?: "Generic",
                version = sensor.version,
                power = sensor.power,
                resolution = sensor.resolution,
                maxRange = sensor.maximumRange,
                stringType = sensor.stringType ?: "android.sensor.custom",
            )
        }
    }

    fun getSensorDataStream(sensorType: Int): Flow<SensorDataSample> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(sensorType)
        if (sensor == null) {
            close()
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                event?.let {
                    trySend(
                        SensorDataSample(
                            timestamp = System.currentTimeMillis(),
                            values = it.values.toList(),
                        ),
                    )
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)

        awaitClose {
            sensorManager.unregisterListener(listener)
        }
    }
}
