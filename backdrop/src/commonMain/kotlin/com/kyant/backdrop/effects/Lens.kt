package com.kyant.backdrop.effects

import androidx.annotation.FloatRange
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastCoerceAtLeast
import androidx.compose.ui.util.fastCoerceAtMost
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.BackdropEffectScopeImpl
import com.kyant.backdrop.internal.RoundedRectRefractionShaderString
import com.kyant.backdrop.internal.RoundedRectRefractionWithDispersionShaderString
import com.kyant.backdrop.internal.RuntimeShaderEffect
import com.kyant.backdrop.isRuntimeShaderSupported
import com.kyant.shapes.RoundedRectangularShape

fun BackdropEffectScope.lens(
    @FloatRange(from = 0.0) refractionHeight: Float,
    @FloatRange(from = 0.0) refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false,
    @FloatRange(from = 0.0) saturation: Float = 1f
) {
    if (!isRuntimeShaderSupported() || refractionHeight <= 0f || refractionAmount <= 0f) {
        if (saturation != 1f) {
            colorControls(saturation = saturation)
        }
        return
    }

    if (padding > 0f) {
        padding = (padding - refractionHeight).fastCoerceAtLeast(0f)
    }

    val cornerRadii = cornerRadii
    val effect =
        if (cornerRadii != null) {
            val shader =
                if (!chromaticAberration) {
                    obtainRuntimeShader(
                        "Refraction",
                        RoundedRectRefractionShaderString
                    )
                } else {
                    obtainRuntimeShader(
                        "RefractionWithDispersion",
                        RoundedRectRefractionWithDispersionShaderString
                    )
                }
            shader.apply {
                setFloatUniform("size", size.width, size.height)
                setFloatUniform("offset", -padding, -padding)
                setFloatUniform("cornerRadii", cornerRadii)
                setFloatUniform("refractionHeight", refractionHeight)
                setFloatUniform("refractionAmount", -refractionAmount)
                setFloatUniform("depthEffect", if (depthEffect) 1f else 0f)
                setFloatUniform("saturation", saturation)
                if (chromaticAberration) {
                    setFloatUniform("chromaticAberration", 1f)
                }
            }
            RuntimeShaderEffect(shader, "content")
        } else {
            throwUnsupportedSDFException()
        }
    effect(effect)
}

@Deprecated("Use the overload with saturation", level = DeprecationLevel.HIDDEN)
fun BackdropEffectScope.lens(
    @FloatRange(from = 0.0) refractionHeight: Float,
    @FloatRange(from = 0.0) refractionAmount: Float,
    depthEffect: Boolean = false,
    chromaticAberration: Boolean = false
) {
    lens(
        refractionHeight = refractionHeight,
        refractionAmount = refractionAmount,
        depthEffect = depthEffect,
        chromaticAberration = chromaticAberration,
        saturation = 1f
    )
}

private val BackdropEffectScope.cornerRadii: FloatArray?
    get() {
        // BackdropEffectScope is sealed with a single internal implementation, so this cast
        // succeeds in practice; the fallback keeps the property total.
        val radii = (this as? BackdropEffectScopeImpl)?.lensCornerRadii ?: FloatArray(4)
        return when (val shape = shape) {
            is RoundedRectangularShape -> {
                val corners = shape.corners(size, layoutDirection, this)
                radii[0] = corners.topLeft
                radii[1] = corners.topRight
                radii[2] = corners.bottomRight
                radii[3] = corners.bottomLeft
                radii
            }

            is AbsoluteRoundedCornerShape -> {
                val size = size
                val maxRadius = size.minDimension / 2f
                radii[0] = shape.topStart.toPx(size, this).fastCoerceAtMost(maxRadius)
                radii[1] = shape.topEnd.toPx(size, this).fastCoerceAtMost(maxRadius)
                radii[2] = shape.bottomEnd.toPx(size, this).fastCoerceAtMost(maxRadius)
                radii[3] = shape.bottomStart.toPx(size, this).fastCoerceAtMost(maxRadius)
                radii
            }

            is CornerBasedShape -> {
                val size = size
                val maxRadius = size.minDimension / 2f
                val isLtr = layoutDirection == LayoutDirection.Ltr
                radii[0] =
                    (if (isLtr) shape.topStart else shape.topEnd).toPx(size, this)
                        .fastCoerceAtMost(maxRadius)
                radii[1] =
                    (if (isLtr) shape.topEnd else shape.topStart).toPx(size, this)
                        .fastCoerceAtMost(maxRadius)
                radii[2] =
                    (if (isLtr) shape.bottomEnd else shape.bottomStart).toPx(size, this)
                        .fastCoerceAtMost(maxRadius)
                radii[3] =
                    (if (isLtr) shape.bottomStart else shape.bottomEnd).toPx(size, this)
                        .fastCoerceAtMost(maxRadius)
                radii
            }

            else -> null
        }
    }

private fun throwUnsupportedSDFException(): Nothing {
    throw UnsupportedOperationException(
        "Only RoundedRectangularShape or CornerBasedShape is supported in lens effects."
    )
}
