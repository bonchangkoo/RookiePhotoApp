package kr.co.yogiyo.rookiephotoapp.camera

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.ObservableField
import android.databinding.ObservableFloat
import android.databinding.ObservableInt
import android.graphics.drawable.Drawable
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import com.otaliastudios.cameraview.Flash
import com.otaliastudios.cameraview.Grid
import kr.co.yogiyo.rookiephotoapp.R

class CameraViewModel(private var context: Context) : BaseObservable() {

    companion object {
        val DELAY_DURATIONS = mutableListOf(0, 3, 10)

        // TODO: [Warning] ...Specify type explicitly as nullable or non-nullable...
        val TAG: String = CameraViewModel::class.java.simpleName
    }

    val facingButtonLabel: ObservableField<String> = ObservableField(context.getString(R.string.text_change))
    val showMoreButtonSrc: ObservableField<Drawable?> = ObservableField(ContextCompat.getDrawable(context, R.drawable.baseline_more_horiz_black_24))
    val captureSizeButtonSrc: ObservableField<Drawable?> = ObservableField(ContextCompat.getDrawable(context, R.drawable.baseline_crop_square_black_24))
    val timerButtonSrc: ObservableField<Drawable?> = ObservableField(ContextCompat.getDrawable(context, R.drawable.baseline_timer_off_white_24))
    val flashButtonSrc: ObservableField<Drawable?> = ObservableField(ContextCompat.getDrawable(context, R.drawable.baseline_flash_off_white_24))
    val flashButtonText: ObservableField<String> = ObservableField(context.getString(R.string.text_flash))
    val gridButtonSrc: ObservableField<Drawable?> = ObservableField(ContextCompat.getDrawable(context, R.drawable.baseline_grid_off_black_36))
    val gridButtonText: ObservableField<String> = ObservableField(context.getString(R.string.text_grid))
    val delayVisibility: ObservableInt = ObservableInt(View.GONE)
    val delayMessageLabel: ObservableInt = ObservableInt(0)
    val frameControlButtonVisibility: ObservableInt = ObservableInt(View.VISIBLE)
    val showMoreLayoutVisibility: ObservableInt = ObservableInt(View.GONE)
    val showCaptureSizeLayoutVisibility: ObservableInt = ObservableInt(View.GONE)
    val textColorByCaptureSize: ObservableInt = ObservableInt(ContextCompat.getColor(context, android.R.color.black))
    val alphaByCaptureSize: ObservableFloat = ObservableFloat(0.7f)
    val topSpaceVisibilityByCaptureSize: ObservableInt = ObservableInt(View.VISIBLE)

    private var flashType = Flash.OFF.ordinal
    private var gridType = Grid.OFF.ordinal
    private var delayDurationIndex = 0
    private var currentCaptureID = 0
    private var captureTimer = 0
    private var captureSizeFull = false

    private val timerHandler: Handler by lazy {
        Handler()
    }

    lateinit var captureNow: () -> Unit

    fun isCaptureSizeFull() = captureSizeFull

    fun updateFacingButton(label: String) = facingButtonLabel.set(label)

    fun updateFlashButton(text: String, drawableId: Int) {
        flashButtonText.set(text)
        flashButtonSrc.set(ContextCompat.getDrawable(context, drawableId))
    }

    fun updateGridButton(text: String, drawableId: Int) {
        gridButtonText.set(text)
        gridButtonSrc.set(ContextCompat.getDrawable(context, drawableId))
    }

    fun updateViewByCaptureSize(fullScreen: Boolean) {
        if (captureSizeFull == fullScreen) {
            return
        }

        captureSizeFull = fullScreen
        if (fullScreen) {
            showMoreButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_more_horiz_white_24))
            captureSizeButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_crop_square_white_24))
            gridButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_grid_off_white_36))
            textColorByCaptureSize.set(ContextCompat.getColor(context, android.R.color.white))
            alphaByCaptureSize.set(1f)
            topSpaceVisibilityByCaptureSize.set(View.GONE)
        } else {
            showMoreButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_more_horiz_black_24))
            captureSizeButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_crop_square_black_24))
            gridButtonSrc.set(ContextCompat.getDrawable(context, R.drawable.baseline_grid_off_black_36))
            textColorByCaptureSize.set(ContextCompat.getColor(context, android.R.color.black))
            alphaByCaptureSize.set(0.7f)
            topSpaceVisibilityByCaptureSize.set(View.VISIBLE)
        }
    }

    fun getNextFlashType() = ++flashType % Flash.values().size

    fun getNextGridType() = ++gridType % Grid.values().size

    fun onClickCaptureButton(view: View) {
        if (isTimerOn()) {
            delayVisibility.set(View.VISIBLE)
            captureTimer = getCaptureDelay()
            delayMessageLabel.set(captureTimer)

            timerHandler.postDelayed(makeDecrementTimerFunction(++currentCaptureID), 1000)
            frameControlButtonVisibility.set(View.GONE)
        } else {
            captureNow()
        }
    }

    fun updateShowMoreLayout(visibility: Int) {
        showMoreLayoutVisibility.set(visibility)
    }

    fun updateShowCaptureSizeLayout(visibility: Int) {
        showCaptureSizeLayoutVisibility.set(visibility)
    }

    fun onClickTimerButton(view: View) {
        timerButtonSrc.set(when (getNextCaptureDelay()) {
            0 -> ContextCompat.getDrawable(context, R.drawable.baseline_timer_off_white_24)
            3 -> ContextCompat.getDrawable(context, R.drawable.baseline_timer_3_white_24)
            10 -> ContextCompat.getDrawable(context, R.drawable.baseline_timer_10_white_24)
            else -> null
        })
    }

    fun initButtonVisibility() {
        frameControlButtonVisibility.set(View.VISIBLE)
        showMoreLayoutVisibility.set(View.GONE)
        showCaptureSizeLayoutVisibility.set(View.GONE)
    }

    fun timerCancel(): Boolean {
        return if (timerHandler.hasMessages(0)) {
            finishDelayCapture()
            initButtonVisibility()
            true
        } else {
            false
        }
    }

    private fun getNextCaptureDelay() = DELAY_DURATIONS[++delayDurationIndex % DELAY_DURATIONS.size]

    private fun getCaptureDelay() = DELAY_DURATIONS[delayDurationIndex % DELAY_DURATIONS.size]

    private fun isTimerOn() = delayDurationIndex % DELAY_DURATIONS.size != 0

    private fun makeDecrementTimerFunction(captureID: Int): Runnable {
        return Runnable { decrementTimer(captureID) }
    }

    private fun decrementTimer(captureID: Int) {
        if (captureID != currentCaptureID) {
            return
        }
        --captureTimer
        if (captureTimer == 0) {
            captureNow()
            finishDelayCapture()

        } else if (captureTimer > 0) {
            delayMessageLabel.set(captureTimer)
            Log.d(TAG, "$captureTimer")
            timerHandler.postDelayed(makeDecrementTimerFunction(captureID), 1000)
        }
    }

    private fun finishDelayCapture() {
        timerHandler.removeCallbacksAndMessages(null)
        delayVisibility.set(View.GONE)
    }
}