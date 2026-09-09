package com.utsoluciones.ruteo

data class ResultadoViaje(
    val gananciaLimpia: Double,
    val gananciaPorKm: Double,
    val gastoGasolina: Double,
    val comisionPlataforma: Double,
    val semaforo: String // "verde", "amarillo", "rojo"
)

object Calculator {
    // Mismas fórmulas que la calculadora web
    fun calcular(
        tarifa: Double,
        distancia: Double,
        gasolinaLitro: Double,
        comisionPct: Double,
        meta: Double,
        rendimiento: Double
    ): ResultadoViaje? {
        if (distancia <= 0 || rendimiento <= 0) return null

        val gastoGasolina = (distancia * gasolinaLitro) / rendimiento
        val comisionPlataforma = tarifa * (comisionPct / 100.0)
        val gananciaLimpia = tarifa - gastoGasolina - comisionPlataforma
        val gananciaPorKm = gananciaLimpia / distancia

        val semaforo = when {
            meta > 0 && gananciaPorKm >= meta -> "verde"
            gananciaLimpia > 0 -> "amarillo"
            else -> "rojo"
        }
        return ResultadoViaje(gananciaLimpia, gananciaPorKm, gastoGasolina, comisionPlataforma, semaforo)
    }
}
