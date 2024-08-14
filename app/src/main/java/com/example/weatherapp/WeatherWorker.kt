package com.example.weatherapp

import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun doWork(): Result {
        fetchWeatherData("Lahore")
        return Result.success()
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build()
            .create(ApiInterface::class.java)

        val response = retrofit.getWeatherData(cityName, "71026f00aaef9c200e109db4b32d3cdd", "metric")
        response.enqueue(object : Callback<WeatherApp> {
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {
                    response.body()?.let { weatherData ->
                        checkWeatherChangeAndNotify(weatherData)
                    } ?: Log.e("TAG", "Response body is null")
                } else {
                    Log.e("TAG", "Response was not successful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                Log.e("TAG", "Failed to fetch weather data", t)
            }
        })
    }

    private fun checkWeatherChangeAndNotify(weatherData: WeatherApp) {
        // This is a simplified check. You can expand this logic based on your needs.
        val condition = weatherData.weather.firstOrNull()?.main ?: "unknown"
        val temp = weatherData.main.temp

        val notification = NotificationCompat.Builder(applicationContext, "weather_alerts_channel")
            .setSmallIcon(R.drawable.ic_weather_notification)
            .setContentTitle("Weather Update")
            .setContentText("Current weather: $condition, Temp: $temp°C")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(1, notification)
    }
}
