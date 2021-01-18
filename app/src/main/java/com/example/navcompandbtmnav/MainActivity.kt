package com.example.navcompandbtmnav

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.navcompandbtmnav.databinding.ActivityMainBinding
import com.wada811.viewbinding.viewBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val binding by viewBinding<ActivityMainBinding>()
    private lateinit var navGraphIds: List<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ボトムナビゲーションで管理するアイテムのグラフ
        navGraphIds = listOf(R.navigation.first, R.navigation.second, R.navigation.third)
        if (savedInstanceState == null)
            binding.bottomNav.setupWithNavController(
                navGraphIds,
                supportFragmentManager,
                R.id.nav_host_fragment,
            )
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        binding.bottomNav.setupWithNavController(
            navGraphIds,
            supportFragmentManager,
            R.id.nav_host_fragment,
        )
    }
}