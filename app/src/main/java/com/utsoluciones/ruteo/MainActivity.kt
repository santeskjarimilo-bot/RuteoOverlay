package com.utsoluciones.ruteo

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etGasolina = findViewById<EditText>(R.id.etGasolina)
        val etComision = findViewById<EditText>(R.id.etComision)
        val etMeta = findViewById<EditText>(R.id.etMeta)
        val etRendimiento = findViewById<EditText>(R.id.etRendimiento)

        etGasolina.setText(Prefs.gasolina(this).toString())
        etComision.setText(Prefs.comision(this).toString())
        etMeta.setText(Prefs.meta(this).toString())
        etRendimiento.setText(Prefs.rendimiento(this).toString())

        findViewById<Button>(R.id.btnGuardar).setOnClickListener {
            Prefs.save(
                this,
                etGasolina.text.toString().toFloatOrNull() ?: 0f,
                etComision.text.toString().toFloatOrNull() ?: 0f,
                etMeta.text.toString().toFloatOrNull() ?: 0f,
                etRendimiento.text.toString().toFloatOrNull() ?: 45f
            )
            Toast.makeText(this, "Ajustes guardados", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnAccesibilidad).setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            }
        }
    }
}
