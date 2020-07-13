package com.marxist.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.marxist.android.R
import com.marxist.android.model.ConnectivityType
import com.marxist.android.model.DarkModeChanged
import com.marxist.android.model.NetWorkMessage
import com.marxist.android.model.ShowSnackBar
import com.marxist.android.ui.base.BaseActivity
import com.marxist.android.utils.DeviceUtils
import com.marxist.android.utils.PrintLog
import com.marxist.android.utils.RxBus
import com.marxist.android.utils.network.NetworkSchedulerService
import com.marxist.android.utils.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity() {

    companion object {
        const val PLAY_NEW_VIDEO = "com.marxist.android.ui.activities.PLAY_NEW_VIDEO"
    }

    private var currentNavController: LiveData<NavController>? = null

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        val path = DeviceUtils.getRootDirPath(baseContext)

        RxBus.subscribe({
            when (it) {
                is NetWorkMessage -> displayMaterialSnackBar(it.message, it.type, container2)
                is DarkModeChanged -> Handler().post {
                    recreate()
                }
                is ShowSnackBar -> displayMaterialSnackBar(
                    it.message,
                    ConnectivityType.OTHER,
                    container2
                )
            }
        }, {
            PrintLog.debug("Marxist", "$it")
        })
    }

    private fun setupBottomNavigationBar() {
        val navigationView = findViewById<BottomNavigationView>(R.id.nav_view)

        val navGraphIds = listOf(
            R.navigation.nav_feeds,
            R.navigation.nav_ebooks,
            R.navigation.nav_saved,
            R.navigation.nav_notification,
            R.navigation.nav_settings
        )

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = navigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.nav_host_container,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
        })
        currentNavController = controller
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    override fun onStart() {
        super.onStart()
        try {
            val startServiceIntent = Intent(this@MainActivity, NetworkSchedulerService::class.java)
            startService(startServiceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onStop() {
        stopService(Intent(this@MainActivity, NetworkSchedulerService::class.java))
        super.onStop()
    }
}
