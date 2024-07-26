package com.example.weather.com.example.mysec

import org.json.JSONException
import org.json.JSONObject

class WeatherData {
    lateinit var tempString: String
    lateinit var icon: String
    lateinit var weatherType: String
    private var weatherId: Int = 0
    private var tempInt: Int = 0

    fun fromJson(jsonObject: JSONObject?): WeatherData? {
        try {
            var weatherData = WeatherData()
            weatherData.weatherId = jsonObject?.getJSONArray("weather")?.getJSONObject(0)?.getInt("id")!!
            weatherData.weatherType = jsonObject.getJSONArray("weather").getJSONObject(0).getString("main")
            weatherData.icon = updateWeatherIcon(weatherData.weatherId)
            val roundedTemp: Int = (jsonObject.getJSONObject("main").getDouble("temp")-273.15).toInt()
            weatherData.tempString = roundedTemp.toString()
            weatherData.tempInt = roundedTemp
            return weatherData
        } catch (e: JSONException){
            e.printStackTrace()
            return null
        }
    }

    private fun updateWeatherIcon(condition: Int): String {
        if(condition in 200..299) {
            return "thunder"
        } else if (condition in 300..599) {
            return "rain"
        } else if (condition in 600..700) {
            return "snow"
        } else if (condition in 701..771) {
            return "fog"
        } else if (condition in 701..799) {
            return "fog"
        } else if (condition == 800) {
            return "sunny"
        } else if (condition in 801..804) {
            return "cloudy"
        } else if (condition in 900..902) {
            return "thunder"
        }
        if (condition == 903) {
            return "snow"
        }
        if (condition == 904) {
            return "sunny"
        }
        return if (condition in 905..1000) {
            "thunder"
        } else ":("
    }
}