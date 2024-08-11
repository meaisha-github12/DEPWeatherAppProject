package com.example.weatherapp

import android.os.Bundle
import android.util.Log
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.weatherapp.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//71026f00aaef9c200e109db4b32d3cdd
class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy{
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
        //
        fetchWeatherData("lahore")
        SearchCity()
    }

    private fun SearchCity() {
       val searchView = binding.searchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null)
                {
                    fetchWeatherData(query.toString())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
               return true
            }

        })
    }

    private fun fetchWeatherData(cityName: String) {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .build().create(ApiInterface::class.java)
        // Here response
        val response = retrofit.getWeatherData(cityName, "71026f00aaef9c200e109db4b32d3cdd", "metric")
        response.enqueue(object : Callback<WeatherApp>{
            override fun onResponse(call: Call<WeatherApp>, response: Response<WeatherApp>) {
                if (response.isSuccessful) {  // Check if the response is successful
                    val responseBody = response.body()  // Get the response body
                    if (responseBody != null) {
                        val temperature = responseBody.main.temp.toString()
                        val humidity = responseBody.main.humidity
                        val windSpeed = responseBody.wind.speed
                        val sunRise = responseBody.sys.sunrise.toLong()
                        val sunSet = responseBody.sys.sunset.toLong()
                        val seaLevel = responseBody.main.sea_level
                        val condition = responseBody.weather.firstOrNull()?.main?: "unknown"
                        val maxTemp = responseBody.main.temp_max
                        val minTemp = responseBody.main.temp_min
                      //  Log.d("TAG", "onResponse: $temperature")
                        binding.temp.text = "$temperature°C"
                        binding.weather.text = condition
                        binding.maxTemp.text = "MAX : $maxTemp °C"
                        binding.minTemp.text = "MIN : $minTemp °C"
                        binding.humidity.text = "$humidity %"
                        binding.windSpeed.text = "$windSpeed m/s"
                        binding.sunRise.text = "${time(sunRise)}"
                        binding.sunSet.text = "${time(sunSet)}"
                        binding.sea.text = "$seaLevel hPa"
                        binding.condition.text = condition
                        binding.day.text = dayName(System.currentTimeMillis())
                        binding.date.text = date()
                          binding.cityName.text = "$cityName"
                changingImgsBasedOnWeather(condition)

                    }
                } else {
                    Log.e("TAG", "Response was not successful: ${response.code()}")
                }
            }


            override fun onFailure(call: Call<WeatherApp>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })

    }

    private fun changingImgsBasedOnWeather(conditions: String) {
        when (conditions)
        {
            "Clear Sky", "Sunny", "Clear" ->{
                binding.root.setBackgroundResource(R.drawable.thissunnyday)
                binding.lottieAnimationView2.setAnimation(R.raw.suntwo)
            }
            "Partly Clouds", "Clouds", "Overcast", "Mist", "Foggy" ->{
                binding.root.setBackgroundResource(R.drawable.colud_background)
                binding.lottieAnimationView2.setAnimation(R.raw.cloudyy)
            }
            "Light Rain", "Drizzle", "Rain", "Moderate Rain", "Showers", "Heavy Rain" ->{
                binding.root.setBackgroundResource(R.drawable.imagesrain)
                binding.lottieAnimationView2.setAnimation(R.raw.raining)
            }
            "Light Snow", "Moderate Snow", "Heavy Snow", "Blizzard" ->{
                binding.root.setBackgroundResource(R.drawable.snow_background)
                binding.lottieAnimationView2.setAnimation(R.raw.snow)
            }

           "Haze","Smoke", "Dust", "Fog", "Sand", "Dust" ->{
               binding.root.setBackgroundResource(R.drawable.colud_background)
               binding.lottieAnimationView2.setAnimation(R.raw.cloudyy)
           }
else ->{
    binding.root.setBackgroundResource(R.drawable.thissunnyday)
    binding.lottieAnimationView2.setAnimation(R.raw.thissun)
}
        }
        binding.lottieAnimationView2.playAnimation()
    }

    private fun date(): String  {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format((Date()))
    }
    private fun time(timestamp: Long): String  {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format((Date(timestamp*1000)))
    }


    fun dayName(timestamp: Long): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format((Date()))

    }
}