package com.mirea.weatherforecastapp.activity

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.mirea.weatherforecastapp.adapter.CityListAdapter
import com.mirea.weatherforecastapp.databinding.AcivityCityListBinding
import com.mirea.weatherforecastapp.model.CityResponseApi
import com.mirea.weatherforecastapp.viewmodel.CityViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CityListActivity : AppCompatActivity() {

    private lateinit var binding: AcivityCityListBinding
    private val cityAdapter by lazy { CityListAdapter() }
    private val cityViewModel: CityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = AcivityCityListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
        }

        binding.apply {
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    newText?.let { query ->
                        progressBar2.visibility = View.VISIBLE
                        cityViewModel.loadCitiesList(query, 10)
                            .enqueue(object : Callback<CityResponseApi> {
                                override fun onResponse(
                                    call: Call<CityResponseApi>,
                                    response: Response<CityResponseApi>
                                ) {
                                    if (response.isSuccessful) {
                                        val data = response.body()
                                        data?.let {
                                            progressBar2.visibility = View.GONE
                                            cityAdapter.differ.submitList(it)
                                            if (it.isEmpty()) {
                                                progressBar2.visibility = View.GONE
                                                textNoResults.visibility = View.VISIBLE
                                            }

                                            cityView.apply {
                                                layoutManager = LinearLayoutManager(
                                                    this@CityListActivity,
                                                    LinearLayoutManager.VERTICAL,
                                                    false
                                                )
                                                adapter = cityAdapter
                                            }
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<CityResponseApi>, t: Throwable) {
                                    progressBar2.visibility = View.GONE
                                    textError.visibility = View.VISIBLE
                                }
                            })
                    }
                    return true
                }
            })
        }
    }
}