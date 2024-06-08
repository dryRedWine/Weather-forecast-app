package com.mirea.weatherforecastapp.adapter

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mirea.weatherforecastapp.activity.MainActivity
import com.mirea.weatherforecastapp.databinding.CityViewholderBinding
import com.mirea.weatherforecastapp.model.CityResponseApi

class CityListAdapter : RecyclerView.Adapter<CityListAdapter.ViewHolder>() {
    private lateinit var binding: CityViewholderBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityListAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        sharedPreferences = parent.context.getSharedPreferences(
            "weather_prefs",
            Context.MODE_PRIVATE
        )
        binding = CityViewholderBinding.inflate(inflater, parent, false)
        return ViewHolder()
    }

    override fun onBindViewHolder(holder: CityListAdapter.ViewHolder, position: Int) {
        val binding = CityViewholderBinding.bind(holder.itemView)
        binding.cityTxt.text = differ.currentList[position].name
        binding.root.setOnClickListener {
            val intent = Intent(binding.root.context, MainActivity::class.java)
            intent.putExtra("lat", differ.currentList[position].lat)
            intent.putExtra("lon", differ.currentList[position].lon)
            intent.putExtra("name", differ.currentList[position].name)
            sharedPreferences.edit().apply {
                putFloat("lat", differ.currentList[position].lat!!.toFloat())
                putFloat("lon", differ.currentList[position].lon!!.toFloat())
                putString("name", differ.currentList[position].name)
            }.apply()
            binding.root.context.startActivity(intent)
        }
    }

    inner class ViewHolder : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount() = differ.currentList.size

    val differCallback = object : DiffUtil.ItemCallback<CityResponseApi.CityResponseApiItem>() {
        override fun areItemsTheSame(
            oldItem: CityResponseApi.CityResponseApiItem,
            newItem: CityResponseApi.CityResponseApiItem
        ): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(
            oldItem: CityResponseApi.CityResponseApiItem,
            newItem: CityResponseApi.CityResponseApiItem
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, differCallback)

}