package com.example.safedinneres.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safedinneres.R
import com.example.safedinneres.ui.gastos.AgregarGastoActivity
import com.example.safedinneres.ui.main.MainActivity
import com.example.safedinneres.ui.perfil.PerfilActivity
import com.example.safedinneres.ui.presupuesto.AgregarPresupuestoActivity
import com.example.safedinneres.ui.reportes.ReporteMensualActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivityWithNav : AppCompatActivity() {

    protected lateinit var bottomNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    protected fun setupBottomNavigation(bottomNavId: Int) {
        bottomNav = findViewById(bottomNavId)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {

                R.id.nav_home -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    true
                }

                R.id.nav_agregar -> {
                    if (this !is AgregarGastoActivity) {
                        startActivity(Intent(this, AgregarGastoActivity::class.java))
                    }
                    true
                }

                R.id.nav_reportes -> {
                    if (this !is ReporteMensualActivity) {
                        startActivity(Intent(this, ReporteMensualActivity::class.java))
                        finish()
                    }
                    true
                }

                R.id.nav_presupuesto -> {
                    if (this !is AgregarPresupuestoActivity) {
                        startActivity(Intent(this, AgregarPresupuestoActivity::class.java))
                        finish()
                    }
                    true
                }

                R.id.nav_perfil -> {
                    if (this !is PerfilActivity) {
                        startActivity(Intent(this, PerfilActivity::class.java))
                        finish()
                    }
                    true
                }


                else -> false
            }
        }

        highlightCurrentScreen()
    }

    private fun highlightCurrentScreen() {
        val itemId = when (this) {
            is MainActivity -> R.id.nav_home
            is AgregarGastoActivity -> R.id.nav_agregar
            is AgregarPresupuestoActivity -> R.id.nav_presupuesto
            is PerfilActivity -> R.id.nav_perfil
            is ReporteMensualActivity -> R.id.nav_reportes
            else -> R.id.nav_home
        }
        bottomNav.selectedItemId = itemId
    }

    override fun onResume() {
        super.onResume()
        if (::bottomNav.isInitialized) {
            highlightCurrentScreen()
        }
    }
}