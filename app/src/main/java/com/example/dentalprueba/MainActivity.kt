package com.example.dentalprueba

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.appcompat.app.AppCompatActivity
import com.example.dentalprueba.databinding.ActivityMainBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import androidx.navigation.ui.NavigationUI

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.appBarMain.toolbar)

        val navHostFragment =
            (supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment?)!!
        val navController = navHostFragment.navController

        binding.appBarMain.fab?.setOnClickListener {
            navController.navigate(R.id.nav_add_patient)
        }

        binding.navView?.let { navView ->
            val drawerLayout = binding.drawerLayout
            
            appBarConfiguration = if (drawerLayout != null) {
                AppBarConfiguration(
                    setOf(R.id.nav_home, R.id.nav_add_patient, R.id.nav_history),
                    drawerLayout
                )
            } else {
                AppBarConfiguration(setOf(R.id.nav_home, R.id.nav_add_patient, R.id.nav_history))
            }

            setupActionBarWithNavController(navController, appBarConfiguration)
            navView.setupWithNavController(navController)

            updateNavHeader(navView)

            navView.setNavigationItemSelectedListener { menuItem ->
                if (menuItem.itemId == R.id.nav_logout) {
                    logout()
                    true
                } else {
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout?.closeDrawers()
                    }
                    handled
                }
            }
        } ?: run {
             appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_home, R.id.nav_add_patient, R.id.nav_history)
            )
        }

        binding.appBarMain.contentMain.bottomNavView?.let {
            if (!::appBarConfiguration.isInitialized) {
                 appBarConfiguration = AppBarConfiguration(
                    setOf(R.id.nav_home, R.id.nav_add_patient, R.id.nav_history)
                )
            }
            
            if (binding.navView == null) {
                setupActionBarWithNavController(navController, appBarConfiguration)
            }
            
            it.setupWithNavController(navController)
        }
        
        // Listener para asegurar que el título se actualice correctamente al navegar al inicio
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.nav_home) {
                val user = Firebase.auth.currentUser
                if (user != null) {
                    val name = user.displayName ?: "Usuario"
                    supportActionBar?.title = getString(R.string.welcome_user, name)
                } else {
                    supportActionBar?.title = getString(R.string.welcome_default)
                }
            }
        }
    }

    private fun updateNavHeader(navView: NavigationView) {
        val user = Firebase.auth.currentUser
        val headerView = navView.getHeaderView(0)
        val userNameTextView = headerView.findViewById<TextView>(R.id.textViewUserName)
        val userEmailTextView = headerView.findViewById<TextView>(R.id.textViewUserEmail)

        if (user != null) {
            val name = user.displayName ?: "Usuario"
            userNameTextView.text = getString(R.string.welcome_user, name)
            userEmailTextView.text = user.email
        } else {
            userNameTextView.text = getString(R.string.app_title)
            userEmailTextView.text = getString(R.string.welcome_default)
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        
        navController.navigate(R.id.nav_login, null, 
            androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_home, true)
                .build()
        )
        binding.drawerLayout?.closeDrawers()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                logout()
                true
            }
            R.id.nav_settings -> {
                val navController = findNavController(R.id.nav_host_fragment_content_main)
                navController.navigate(R.id.nav_settings)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }
}
