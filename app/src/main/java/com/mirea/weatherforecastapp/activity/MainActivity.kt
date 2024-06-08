package com.mirea.weatherforecastapp.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.matteobattilana.weather.PrecipType
import com.mirea.weatherforecastapp.R
import com.mirea.weatherforecastapp.adapter.ForecastAdapter
import com.mirea.weatherforecastapp.databinding.ActivityMainBinding
import com.mirea.weatherforecastapp.model.CurrentWeatherResponseApi
import com.mirea.weatherforecastapp.model.FiveDaysForecastResponseApi
import com.mirea.weatherforecastapp.viewmodel.WeatherViewModel
import eightbitlab.com.blurview.RenderScriptBlur
import retrofit2.Call
import retrofit2.Response
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private val calendar by lazy { Calendar.getInstance() }
    private val forecastAdapter by lazy { ForecastAdapter() }
    private val sharedPreferences by lazy {
        getSharedPreferences(
            "weather_prefs",
            Context.MODE_PRIVATE
        )
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }


        binding.apply {
            var lat = sharedPreferences.getFloat("lat", 0.0f)
            var lon = sharedPreferences.getFloat("lon", 0.0f)
            var name = sharedPreferences.getString("name","")



            addCity.setOnClickListener {
                startActivity(Intent(this@MainActivity, CityListActivity::class.java))
            }
            //current temperature
            cityTxt.text = name
            progressBar.visibility = View.VISIBLE
            weatherViewModel.loadCurrentWeather(lat.toDouble(), lon.toDouble(), "metric").enqueue(object :
                retrofit2.Callback<CurrentWeatherResponseApi> {
                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<CurrentWeatherResponseApi>,
                    response: Response<CurrentWeatherResponseApi>
                ) {
                    if (response.isSuccessful) {
                        val data = response.body()
                        progressBar.visibility = View.GONE
                        detailLayout.visibility = View.VISIBLE

                        if (lat == 0.0f || lon == 0.0f) {
                            loadData()
                        } else {
                            data?.let {
                                statusTxt.text = it.weather?.get(0)?.main ?: "-"
                                humidityTxt.text = it.main?.humidity?.toString() + "%"
                                windTxt.text = it.wind?.speed.let {
                                    it?.let { it1 ->
                                        Math.round(it1).toString()
                                    }
                                } + "Km"
                                currentTempTxt.text = it.main?.temp.let {
                                    it?.let { it1 ->
                                        Math.round(it1).toString()
                                    }
                                } + "°"
                                maxTempTxt.text = it.main?.tempMax.let {
                                    it?.let { it1 ->
                                        Math.round(it1).toString()
                                    }
                                } + "°"
                                minTempTxt.text = it.main?.tempMin.let {
                                    it?.let { it1 ->
                                        Math.round(it1).toString()
                                    }
                                } + "°"

                                val drawable = if (isNightNow()) R.drawable.night_bg
                                else {
                                    setDynamicallyWallpaper(it.weather?.get(0)?.icon ?: "-")
                                }
                                bgImage.setImageResource(drawable)
                                setEffectRainSnow(it.weather?.get(0)?.icon ?: "-")

                                saveData(it)
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<CurrentWeatherResponseApi>, t: Throwable) {
                    Toast.makeText(this@MainActivity, t.toString(), Toast.LENGTH_SHORT).show()
                }
            })

            //settings blur view
            var radius = 8f
            val decorView = window.decorView
            val rootView = (decorView.findViewById(android.R.id.content) as ViewGroup?)
            val windowBackground = decorView.background

            rootView?.let {
                blurView.setupWith(it, RenderScriptBlur(this@MainActivity))
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(radius)
                blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
                blurView.clipToOutline = true

            }


            //forecast temp
            weatherViewModel.loadFiveDaysForecast(lat.toDouble(), lon.toDouble(), "metric")
                .enqueue(object : retrofit2.Callback<FiveDaysForecastResponseApi> {
                    override fun onResponse(
                        call: Call<FiveDaysForecastResponseApi>,
                        response: Response<FiveDaysForecastResponseApi>
                    ) {
                        if (response.isSuccessful) {
                            val data = response.body()
                            blurView.visibility = View.VISIBLE

                            data?.let {
                                forecastAdapter.differ.submitList(it.list)
                                forecastView.apply {
                                    visibility = View.VISIBLE
                                    layoutManager = LinearLayoutManager(
                                        this@MainActivity,
                                        LinearLayoutManager.HORIZONTAL,
                                        false
                                    )
                                    adapter = forecastAdapter
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<FiveDaysForecastResponseApi>, t: Throwable) {
                    }

                })
        }
    }

    private fun saveData(data: CurrentWeatherResponseApi) {
        sharedPreferences.edit().apply {

            putString("weather_main", data.weather?.get(0)?.main)
            putInt("humidity", data.main?.humidity ?: 0)
            putFloat("wind_speed", data.wind?.speed?.toFloat() ?: 0f) // Конвертируем в Float
            putFloat("temp", data.main?.temp?.toFloat() ?: 0f) // Конвертируем в Float
            putFloat("temp_max", data.main?.tempMax?.toFloat() ?: 0f) // Конвертируем в Float
            putFloat("temp_min", data.main?.tempMin?.toFloat() ?: 0f) // Конвертируем в Float
            putString("weather_icon", data.weather?.get(0)?.icon)
        }.apply()

        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()

    }

    private fun loadData() {
        val weatherMain = sharedPreferences.getString("weather_main", "-")
        val humidity = sharedPreferences.getInt("humidity", 0)
        val windSpeed = sharedPreferences.getFloat("wind_speed", 0f)
        val temp = sharedPreferences.getFloat("temp", 0f)
        val tempMax = sharedPreferences.getFloat("temp_max", 0f)
        val tempMin = sharedPreferences.getFloat("temp_min", 0f)
        val weatherIcon = sharedPreferences.getString("weather_icon", "-")

        binding.statusTxt.text = weatherMain ?: "-"
        binding.humidityTxt.text = "$humidity%"
        binding.windTxt.text = "${Math.round(windSpeed)}Km"
        binding.currentTempTxt.text = "${Math.round(temp)}°"
        binding.maxTempTxt.text = "${Math.round(tempMax)}°"
        binding.minTempTxt.text = "${Math.round(tempMin)}°"

        val drawable = if (isNightNow()) R.drawable.night_bg
        else {
            setDynamicallyWallpaper(weatherIcon ?: "-")
        }
        binding.bgImage.setImageResource(drawable)
        setEffectRainSnow(weatherIcon ?: "-")
    }


    private fun isNightNow(): Boolean {
        return calendar.get(Calendar.HOUR_OF_DAY) >= 18
    }
//    private fun isNightNow(timeZone: Int?): Boolean {
//        val zonedDateTime = ZonedDateTime.now(ZoneId.of(timeZone.toString()))
//        val hour = zonedDateTime.hour
//        return hour >= 18
//    }

    private fun setDynamicallyWallpaper(icon: String): Int {
        return when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.sunny_bg
            }

            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.cloudy_bg
            }

            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
                R.drawable.rainy_bg
            }

            "13" -> {
                initWeatherView(PrecipType.SNOW)
                R.drawable.snow_bg
            }

            "50" -> {
                initWeatherView(PrecipType.CLEAR)
                R.drawable.haze_bg
            }

            else -> 0
        }

    }

    private fun setEffectRainSnow(icon: String) {
        when (icon.dropLast(1)) {
            "01" -> {
                initWeatherView(PrecipType.CLEAR)
            }

            "02", "03", "04" -> {
                initWeatherView(PrecipType.CLEAR)
            }

            "09", "10", "11" -> {
                initWeatherView(PrecipType.RAIN)
            }

            "13" -> {
                initWeatherView(PrecipType.SNOW)
            }

            "50" -> {
                initWeatherView(PrecipType.CLEAR)
            }
        }

    }

    private fun initWeatherView(type: PrecipType) {
        binding.weatherView.apply {
            setWeatherData(type)
            angle = -20
            emissionRate = 100.0f
        }
    }
}
