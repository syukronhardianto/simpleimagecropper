package com.spe.simple_image_cropper

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView

private var lastX = 0f
private var lastY = 0f
private var originalWidth = 0
private var originalHeight = 0
private var originalX = 0f
private var originalY = 0f
private val touchMargin = 80
private var isResizingTopLeft = false
private var isResizingTopRight = false
private var isResizingBottomLeft = false
private var isResizingBottomRight = false
private var isDragging = false


@SuppressLint("ClickableViewAccessibility")
fun customCropper(cropper: View, imageNeedToCrop: ImageView) {
    cropper.setOnTouchListener { v, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastX = event.rawX
                lastY = event.rawY
                originalWidth = v.width
                originalHeight = v.height
                originalX = v.x
                originalY = v.y

                isResizingTopLeft = isInTopLeftCorner(event)
                isResizingTopRight = isInTopRightCorner(event, v)
                isResizingBottomLeft = isInBottomLeftCorner(event, v)
                isResizingBottomRight = isInBottomRightCorner(event, v)

                isDragging = !(isResizingTopLeft || isResizingTopRight || isResizingBottomLeft || isResizingBottomRight)
            }
            MotionEvent.ACTION_MOVE -> {
                if (isResizingTopLeft || isResizingTopRight || isResizingBottomLeft || isResizingBottomRight) {
                    val params = v.layoutParams

                    if (isResizingTopLeft) {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        val delta = minOf(deltaX, deltaY)

                        val newSize = (originalWidth - delta).toInt()

                        params.width = maxOf(newSize, 100)
                        params.height = maxOf(newSize, 100)

                        v.x = originalX + delta
                        v.y = originalY + delta
                    }

                    if (isResizingTopRight) {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        val delta = minOf(deltaX, -deltaY)

                        val newSize = (originalWidth + delta).toInt()

                        params.width = maxOf(newSize, 100)
                        params.height = maxOf(newSize, 100)

                        v.y = originalY - delta
                    }

                    if (isResizingBottomLeft) {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        val delta = minOf(-deltaX, deltaY)

                        val newSize = (originalWidth - delta).toInt()

                        params.width = maxOf(newSize, 100)
                        params.height = maxOf(newSize, 100)

                        v.x = originalX + delta
                    }

                    if (isResizingBottomRight) {
                        val deltaX = event.rawX - lastX
                        val deltaY = event.rawY - lastY
                        val delta = minOf(deltaX, deltaY)

                        val newSize = (originalWidth + delta).toInt()

                        params.width = maxOf(newSize, 100)
                        params.height = maxOf(newSize, 100)
                    }

                    v.layoutParams = params
                    v.requestLayout()

                    restrictViewWithinBounds(v, imageNeedToCrop)
                } else if (isDragging) {
                    val newX = originalX + (event.rawX - lastX)
                    val newY = originalY + (event.rawY - lastY)

                    v.x = maxOf(0f, minOf(newX, imageNeedToCrop.width - v.width.toFloat()))
                    v.y = maxOf(0f, minOf(newY, imageNeedToCrop.height - v.height.toFloat()))
                }
            }
            MotionEvent.ACTION_UP -> {
                isResizingTopLeft = false
                isResizingTopRight = false
                isResizingBottomLeft = false
                isResizingBottomRight = false
                isDragging = false
            }
        }
        true
    }
}


private fun restrictViewWithinBounds(view: View, imageView: ImageView) {
    view.x = maxOf(0f, minOf(view.x, imageView.width - view.width.toFloat()))
    view.y = maxOf(0f, minOf(view.y, imageView.height - view.height.toFloat()))
}

private fun isInTopLeftCorner(event: MotionEvent): Boolean {
    val x = event.x
    val y = event.y
    return x <= touchMargin && y <= touchMargin
}

private fun isInTopRightCorner(event: MotionEvent, view: View): Boolean {
    val x = event.x
    val y = event.y
    return x >= view.width - touchMargin && y <= touchMargin
}

private fun isInBottomLeftCorner(event: MotionEvent, view: View): Boolean {
    val x = event.x
    val y = event.y
    return x <= touchMargin && y >= view.height - touchMargin
}

private fun isInBottomRightCorner(event: MotionEvent, view: View): Boolean {
    val x = event.x
    val y = event.y
    return x >= view.width - touchMargin && y >= view.height - touchMargin
}

fun cropImage(cropper: View, imageNeedToCrop: ImageView): Bitmap {
    val bitmap = (imageNeedToCrop.drawable as BitmapDrawable).bitmap

    val cropWidth = cropper.width
    val cropHeight = cropper.height

    val location = IntArray(2)
    cropper.getLocationOnScreen(location)

    val ivLocation = IntArray(2)
    imageNeedToCrop.getLocationOnScreen(ivLocation)

    val cropX = location[0] - ivLocation[0]
    val cropY = location[1] - ivLocation[1]

    val finalCropX = cropX.coerceIn(0, bitmap.width - 1)
    val finalCropY = cropY.coerceIn(0, bitmap.height - 1)
    val finalCropWidth = cropWidth.coerceAtMost(bitmap.width - finalCropX)
    val finalCropHeight = cropHeight.coerceAtMost(bitmap.height - finalCropY)

    return Bitmap.createBitmap(bitmap, finalCropX, finalCropY, finalCropWidth, finalCropHeight)
}