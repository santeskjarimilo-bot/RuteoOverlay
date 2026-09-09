package com.utsoluciones.ruteo

import android.content.Context

object Prefs {
    private const val NAME = "ruteo_prefs"

    fun get(context: Context) = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    fun gasolina(context: Context) = get(context).getFloat("gasolina", 0f)
    fun comision(context: Context) = get(context).getFloat("comision", 0f)
    fun meta(context: Context) = get(context).getFloat("meta", 0f)
    fun rendimiento(context: Context) = get(context).getFloat("rendimiento", 45f)

    fun save(context: Context, gasolina: Float, comision: Float, meta: Float, rendimiento: Float) {
        get(context).edit()
            .putFloat("gasolina", gasolina)
            .putFloat("comision", comision)
            .putFloat("meta", meta)
            .putFloat("rendimiento", rendimiento)
            .apply()
    }
}
