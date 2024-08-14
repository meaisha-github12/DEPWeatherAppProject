package com.example.weatherapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weatherapp.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Create notification channel
        createNotificationChannel()

        // Schedule periodic weather updates
        scheduleWeatherUpdates()

        fetchWeatherData("Islamabad") // Default city name
        setupSearchView()
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    fetchWeatherData(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // No action needed for query text change
                return true
            }
        })
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
                        updateUI(weatherData, cityName)
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

    private fun updateUI(weatherData: WeatherApp, cityName: String) {
        val temperature = weatherData.main.temp.toString()
        val humidity = weatherData.main.humidity
        val windSpeed = weatherData.wind.speed
        val sunRise = weatherData.sys.sunrise.toLong()
        val sunSet = weatherData.sys.sunset.toLong()
        val seaLevel = weatherData.main.sea_level
        val condition = weatherData.weather.firstOrNull()?.main ?: "unknown"
        val maxTemp = weatherData.main.temp_max
        val minTemp = weatherData.main.temp_min

        binding.temp.text = "$temperature°C"
        binding.weather.text = condition
        binding.maxTemp.text = "MAX : $maxTemp °C"
        binding.minTemp.text = "MIN : $minTemp °C"
        binding.humidity.text = "$humidity %"
        binding.windSpeed.text = "$windSpeed m/s"
        binding.sunRise.text = time(sunRise)
        binding.sunSet.text = time(sunSet)
        binding.sea.text = "$seaLevel hPa"
        binding.condition.text = condition
        binding.day.text = dayName(System.currentTimeMillis())
        binding.date.text = date()
        binding.cityName.text = cityName
        changingImgsBasedOnWeather(condition)
    }

    private fun changingImgsBasedOnWeather(condition: String) {
        when (condition) {
            "Clear Sky", "Sunny", "Clear" -> {
                binding.root.setBackgroundResource(R.drawable.thissunnyday)
                binding.lottieAnimationView2.setAnimation(R.raw.suntwo)
            }
            "Partly Cloudy", "Clouds", "Overcast", "Mist", "Foggy" -> {
                binding.root.setBackgroundResource(R.drawable.cloudysky)
                binding.lottieAnimationView2.setAnimation(R.raw.cloudyy)
            }
            "Light Rain", "Drizzle", "Rain", "Moderate Rain", "Showers", "Heavy Rain" -> {
                binding.root.setBackgroundResource(R.drawable.imagesrain)
                binding.lottieAnimationView2.setAnimation(R.raw.raining)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" -> {
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView2.setAnimation(R.raw.snow)
            }
            "Haze", "Smoke", "Dust", "Fog", "Sand" -> {
                binding.root.setBackgroundResource(R.drawable.cloudimg)
                binding.lottieAnimationView2.setAnimation(R.raw.windyyy)
            }
            else -> {
                binding.root.setBackgroundResource(R.drawable.thissunnyday)
                binding.lottieAnimationView2.setAnimation(R.raw.thissun)
            }
        }
        binding.lottieAnimationView2.playAnimation()
    }

    private fun date(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun time(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp * 1000))
    }

    private fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "weather_alerts_channel"
            val channelName = "Weather Alerts"
            val channelDescription = "Channel for weather alerts"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }

            val notificationManager: NotificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleWeatherUpdates() {
        val weatherWorkRequest = PeriodicWorkRequestBuilder<WeatherWorker>(1, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(this).enqueue(weatherWorkRequest)
    }
}
