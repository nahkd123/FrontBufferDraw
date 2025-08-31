package io.github.naharaoss.frontbufferdraw

import kotlin.math.pow
import kotlin.math.sqrt

data class PenInput(val x: Float, val y: Float, val pressure: Float) {
    infix fun distanceTo(another: PenInput) = sqrt((another.x - x).pow(2) + (another.y - y).pow(2))
}

fun lerp(a: PenInput, b: PenInput, fraction: Float) = PenInput(
    x = androidx.compose.ui.util.lerp(a.x, b.x, fraction),
    y = androidx.compose.ui.util.lerp(a.y, b.y, fraction),
    pressure = androidx.compose.ui.util.lerp(a.pressure, b.pressure, fraction)
)

fun Pair<PenInput, PenInput>.each(spacing: Float = 1f, block: (PenInput) -> Unit) {
    val (a, b) = this
    val dist = a distanceTo b
    var prog = 0f
    val step = spacing / dist

    while (prog < 1f) {
        block(lerp(a, b, prog))
        prog += step
    }
}