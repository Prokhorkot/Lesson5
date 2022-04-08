package com.mirea.kotov.lesson5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var listView: ListView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        listView = findViewById(R.id.listView)

        val sensorManager: SensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensors: List<Sensor> = sensorManager.getSensorList(Sensor.TYPE_ALL)

        val arrayList: ArrayList<HashMap<String?, Any?>> = ArrayList()
        var sensorTypeList: HashMap<String?, Any?>

        for (i in sensors.indices) {
            sensorTypeList = HashMap()
            sensorTypeList["Name"] = sensors[i].name
            sensorTypeList["Value"] = sensors[i].maximumRange
            arrayList.add(sensorTypeList)
        }

        val mHistory = SimpleAdapter(
            this,
            arrayList,
            android.R.layout.simple_list_item_2,
            arrayOf("Name", "Value"),
            intArrayOf(android.R.id.text1, android.R.id.text2)
        )
        listView?.adapter = mHistory
    }
}