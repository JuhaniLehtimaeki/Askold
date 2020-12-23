package com.lehtimaeki.askold

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.widget.ImageViewCompat
import com.google.android.material.card.MaterialCardView
import com.lehtimaeki.askold.ColorPalettes.getNextColorFromPalette
import java.util.*
import kotlin.math.absoluteValue


class GameTile @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val rnd = Random()
    private var isFlipped = false


    private val minimumDragDistance by lazy { resources.getDimension(R.dimen.minimum_drag_distance) }

    private val frontSide by lazy { findViewById<MaterialCardView>(R.id.front) }
    private val backSide by lazy { findViewById<MaterialCardView>(R.id.back) }

    private val frontText by lazy { findViewById<TextView>(R.id.frontText) }
    private val backText by lazy { findViewById<TextView>(R.id.backText) }


    private val frontImage by lazy { findViewById<ImageView>(R.id.frontImage) }
    private val backImage by lazy { findViewById<ImageView>(R.id.backImage) }


    private val x by lazy {
        val location = IntArray(2)
        getLocationOnScreen(location)
        location[0]
    }

    private val y by lazy {
        val location = IntArray(2)
        getLocationOnScreen(location)
        location[1]
    }


    init {
        inflate(getContext(), R.layout.game_tile, this)
    }


    private var bounceFlips = false

//
//    fun flip() {
//        if (bounceFlips) {
//            return
//        }
//        bounceFlips = true
//
//        if (isFlipped) {
//            when (rnd.nextInt(3)) {
//                0 -> animateToFront1()
//                1 -> animateToFront2()
//                2 -> animateToFront3()
//            }
//
//        } else {
//            when (rnd.nextInt(3)) {
//                0 -> animateToBack1()
//                1 -> animateToBack2()
//                2 -> animateToBack3()
//            }
//
//        }
//        isFlipped = !isFlipped
//    }


    private fun flipHorizontal(reverse: Boolean) {
        bounceFlips = true
        if (isFlipped) {
            animateToFront1(reverse)
        } else {
            animateToBack1(reverse)
        }
    }


    private fun flipVertical(reverse: Boolean) {
        bounceFlips = true
        if (isFlipped) {
            animateToFront2(reverse)
        } else {
            animateToBack2(reverse)
        }
    }


    private fun animateToFront1(reverse: Boolean) {
        animate().rotationY(
            if (reverse) {
                -90f
            } else {
                90f
            }
        ).withEndAction {
            swapToFront()
            rotationY = if (reverse) {
                90f
            } else {
                -90f
            }
            animate().rotationY(0f).withEndAction { resetBounce() }
        }
    }

    private fun animateToFront2(reverse: Boolean) {


        animate().rotationX(
            if (reverse) {
                -90f
            } else {
                90f
            }
        ).withEndAction {
            swapToFront()
            rotationX = if (reverse) {
                90f
            } else {
                -90f
            }
            animate().rotationX(0f).withEndAction { resetBounce() }
        }
    }

    private fun animateToFront3() {
        animate().scaleX(0f).scaleY(0f).withEndAction {
            swapToFront()
            animate().scaleX(1f).scaleY(1f).withEndAction { resetBounce() }
        }
    }


    private fun animateToBack1(reverse: Boolean) {
        animate().rotationY(
            if (reverse) {
                -90f
            } else {
                90f
            }
        ).withEndAction {
            swapToBack()
            rotationY = if (reverse) {
                90f
            } else {
                -90f
            }
            animate().rotationY(0f).withEndAction { resetBounce() }
        }
    }

    private fun animateToBack2(reverse: Boolean) {
        animate().rotationX(
            if (reverse) {
                -90f
            } else {
                90f
            }
        ).withEndAction {
            swapToBack()
            rotationX = if (reverse) {
                90f
            } else {
                -90f
            }
            animate().rotationX(0f).withEndAction { resetBounce() }
        }
    }

    private fun animateToBack3() {
        animate().scaleX(0f).scaleY(0f).withEndAction {
            swapToBack()
            animate().scaleX(1f).scaleY(1f).withEndAction { resetBounce() }
        }
    }


    private fun resetBounce() {
        lastTrackedIndexes.clear()
        bounceFlips = false
    }

    private fun swapToBack() {
        val newBackground = getNextColorFromPalette()
        backSide.setCardBackgroundColor(newBackground)
        frontSide.isGone = true
        backSide.isVisible = true

        handleContent(newBackground, backText, backImage)
    }


    private fun swapToFront() {
        val newBackground = getNextColorFromPalette()
        frontSide.setCardBackgroundColor(newBackground)
        frontSide.isVisible = true
        backSide.isGone = true

        handleContent(newBackground, frontText, frontImage)
    }


    private fun handleContent(backgroundColor: Int, textView: TextView, imageView: ImageView) {


        val random = rnd.nextInt(100)
        when {
            random < IMAGE_ICON_PROBABILITY -> {
                imageView.isVisible = true
                textView.isGone = true
                imageView.setImageResource(IMAGE_ICONS[rnd.nextInt(IMAGE_ICONS.size)])
                ImageViewCompat.setImageTintList(imageView, null)
            }
            random < SYMBOL_PROBABILITY -> {
                textView.setTextColor(ColorPalettes.getContrastColor(backgroundColor))
                imageView.isGone = true
                textView.isVisible = true
                textView.text = SYMBOLS[rnd.nextInt(SYMBOLS.size)]
            }
            random < ICON_PROBABILITY -> {
                ImageViewCompat.setImageTintList(
                    imageView,
                    ColorStateList.valueOf(ColorPalettes.getContrastColor(backgroundColor))
                )
                imageView.isVisible = true
                textView.isGone = true
                imageView.setImageResource(ICONS[rnd.nextInt(ICONS.size)])
            }
            else -> {
                textView.isGone = true
                imageView.isGone = true
            }
        }


    }


    private val lastTrackedIndexes = mutableMapOf<Int, Pair<Float, Float>>()

    /**
     * This function will trigger
     */
    private fun trackTouchOnThisTile(x: Float, y: Float, pointerIndex: Int) {

        val lastPoint = lastTrackedIndexes[pointerIndex]

        if (lastPoint == null) {
            lastTrackedIndexes[pointerIndex] = Pair(x, y)
            return
        }


        val xAbs = (lastPoint.first - x).absoluteValue
        val yAbs = (lastPoint.second - y).absoluteValue

        if (xAbs > minimumDragDistance && xAbs > yAbs) {
            flipHorizontal(x < lastPoint.first)
        } else if (yAbs > minimumDragDistance) {
            flipVertical(lastPoint.second < y)
        }


    }

    private fun isThiTile(eventX: Float, eventY: Float): Boolean {
        return eventX > x && eventX < x + width && eventY > y && eventY < y + height
    }

    fun onTouchOnOverlay(eventX: Float, eventY: Float, pointerIndex: Int) {
        if (bounceFlips) {
            return
        }
        if (isThiTile(eventX, eventY)) {
            trackTouchOnThisTile(eventX, eventY, pointerIndex)
        } else {
            lastTrackedIndexes.remove(pointerIndex)
        }
    }


    companion object {
        const val IMAGE_ICON_PROBABILITY = 40
        const val SYMBOL_PROBABILITY = 60
        const val ICON_PROBABILITY = 90

        val ICONS =
            arrayListOf(
                R.drawable.ic_android_black_24dp,
                R.drawable.ic_baseline_ac_unit_24,
                R.drawable.ic_baseline_airport_shuttle_24,
                R.drawable.ic_baseline_brightness_3_24,
                R.drawable.ic_baseline_brightness_5_24,
                R.drawable.ic_baseline_child_friendly_24,
                R.drawable.ic_baseline_cloud_24,
                R.drawable.ic_baseline_directions_bike_24,
                R.drawable.ic_baseline_directions_boat_24,
                R.drawable.ic_baseline_emoji_emotions_24,
                R.drawable.ic_baseline_local_shipping_24,
            )

        val IMAGE_ICONS =
            arrayListOf(
                R.drawable.fllaticon_01_mouse,
                R.drawable.fllaticon_02_cow,
                R.drawable.fllaticon_03_kangaroo,
                R.drawable.fllaticon_04_bear,
                R.drawable.fllaticon_05_flamingo,
                R.drawable.fllaticon_06_fox,
                R.drawable.fllaticon_07_bat,
                R.drawable.fllaticon_08_crab,
                R.drawable.fllaticon_09_lion,
                R.drawable.fllaticon_10_frog,
                R.drawable.fllaticon_11_bee,
                R.drawable.fllaticon_12_koala,
                R.drawable.fllaticon_13_tiger,
                R.drawable.fllaticon_14_rhino,
                R.drawable.fllaticon_15_squirrel,
                R.drawable.fllaticon_16_whale,
                R.drawable.fllaticon_17_duck,
                R.drawable.fllaticon_18_camel,
                R.drawable.fllaticon_19_shark,
                R.drawable.fllaticon_20_bird,
                R.drawable.fllaticon_21_rabbit,
                R.drawable.fllaticon_22_llama,
                R.drawable.fllaticon_23_cat,
                R.drawable.fllaticon_24_hedgehog,
                R.drawable.fllaticon_25_octopus,
                R.drawable.fllaticon_26_snail,
                R.drawable.fllaticon_27_giraffe,
                R.drawable.fllaticon_28_manta_ray,
                R.drawable.fllaticon_29_wolf,
                R.drawable.fllaticon_30_penguin,
                R.drawable.fllaticon_31_panther,
                R.drawable.fllaticon_32_elephant,
                R.drawable.fllaticon_33_reindeer,
                R.drawable.fllaticon_34_chameleon,
                R.drawable.fllaticon_35_crocodile,
                R.drawable.fllaticon_36_butterfly,
                R.drawable.fllaticon_37_owl,
                R.drawable.fllaticon_38_turtle,
                R.drawable.fllaticon_39_snake,
                R.drawable.fllaticon_40_polar_bear,
                R.drawable.fllaticon_41_monkey,
                R.drawable.fllaticon_42_chicken,
                R.drawable.fllaticon_43_sloth,
                R.drawable.fllaticon_44_dog,
                R.drawable.fllaticon_45_dolphin,
                R.drawable.fllaticon_46_pig,
                R.drawable.fllaticon_47_hippopotamus,
                R.drawable.fllaticon_48_parrot,
                R.drawable.fllaticon_49_clownfish,
                R.drawable.fllaticon_50_horse,
            )

        val SYMBOLS =
            arrayListOf(
                "A",
                "B",
                "C",
                "D",
                "E",
                "F",
                "G",
                "H",
                "I",
                "J",
                "K",
                "L",
                "M",
                "N",
                "O",
                "P",
                "Q",
                "R",
                "S",
                "T",
                "U",
                "V",
                "W",
                "X",
                "Y",
                "Y",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "10"
            )

    }
}