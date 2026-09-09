package com.utsoluciones.ruteo

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView

/**
 * IMPORTANTE (léelo antes de usar):
 * Este servicio lee el TEXTO visible en pantalla de las apps listadas abajo
 * usando el Accessibility Service de Android, y busca patrones tipo "$1234"
 * y "3.2 km" para calcular la ganancia. Es una heurística: cada app cambia
 * su interfaz seguido, así que los patrones (extraerTarifa/extraerDistancia)
 * casi seguro van a necesitar ajustes probando en tu celular real.
 */
class TripAccessibilityService : AccessibilityService() {

    // Paquetes objetivo. Verifica en tu celular el nombre real de paquete
    // de cada app instalada (Ajustes > Apps > [la app] > nombre del paquete)
    // y ajústalos aquí si no coinciden.
    private val paquetesObjetivo = setOf(
        "com.ubercab.driver",
        "com.didiglobal.driver",
        "com.didi.driver",
        "com.rappi.repartidor",
        "com.grability.indriver",
        "sinet.startup.inDriver"
    )

    private var overlayView: View? = null
    private var windowManager: WindowManager? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return

        if (pkg !in paquetesObjetivo) {
            ocultarOverlay()
            return
        }

        val root = rootInActiveWindow ?: return
        val textos = mutableListOf<String>()
        recolectarTextos(root, textos)

        val tarifa = extraerTarifa(textos)
        val distancia = extraerDistancia(textos)

        if (tarifa != null && distancia != null) {
            val r = Calculator.calcular(
                tarifa, distancia,
                Prefs.gasolina(this).toDouble(),
                Prefs.comision(this).toDouble(),
                Prefs.meta(this).toDouble(),
                Prefs.rendimiento(this).toDouble()
            )
            if (r != null) mostrarOverlay(r)
        }
    }

    private fun recolectarTextos(node: AccessibilityNodeInfo, out: MutableList<String>) {
        node.text?.let { out.add(it.toString()) }
        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            recolectarTextos(child, out)
            child.recycle()
        }
    }

    // Busca "$1234" o "$1.234"
    private fun extraerTarifa(textos: List<String>): Double? {
        val regex = Regex("""\$\s?([\d.,]{3,})""")
        for (t in textos) {
            val m = regex.find(t) ?: continue
            val limpio = m.groupValues[1].replace(".", "").replace(",", "")
            limpio.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    // Busca "3.2 km" o "3,2 km"
    private fun extraerDistancia(textos: List<String>): Double? {
        val regex = Regex("""([\d]+[.,]?[\d]*)\s?km""", RegexOption.IGNORE_CASE)
        for (t in textos) {
            val m = regex.find(t) ?: continue
            val limpio = m.groupValues[1].replace(",", ".")
            limpio.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    private fun mostrarOverlay(r: ResultadoViaje) {
        if (overlayView == null) {
            val inflater = LayoutInflater.from(this)
            overlayView = inflater.inflate(R.layout.overlay_card, null)

            val tipoVentana = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                tipoVentana,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
            )
            params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            params.y = 100

            windowManager?.addView(overlayView, params)
        }

        val card = overlayView ?: return
        val amount = card.findViewById<TextView>(R.id.overlayAmount)
        val sub = card.findViewById<TextView>(R.id.overlaySub)
        val bg = card.findViewById<View>(R.id.overlayBg)

        amount.text = "$" + Math.round(r.gananciaLimpia)
        val colorRes = when (r.semaforo) {
            "verde" -> R.color.green
            "amarillo" -> R.color.yellow
            else -> R.color.red
        }
        bg.setBackgroundColor(resources.getColor(colorRes, theme))

        sub.text = when (r.semaforo) {
            "verde" -> "DALE - $" + Math.round(r.gananciaPorKm) + "/km"
            "amarillo" -> "AL LIMITE - $" + Math.round(r.gananciaPorKm) + "/km"
            else -> "RECHAZAR - $" + Math.round(r.gananciaPorKm) + "/km"
        }
    }

    private fun ocultarOverlay() {
        overlayView?.let {
            windowManager?.removeView(it)
            overlayView = null
        }
    }

    override fun onInterrupt() {}
}
