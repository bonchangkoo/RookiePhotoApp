package kr.co.yogiyo.rookiephotoapp.camera

import android.content.Context
import android.databinding.BaseObservable
import android.databinding.ObservableField
import android.databinding.ObservableInt
import android.os.Handler
import android.util.Log
import android.view.View
import com.otaliastudios.cameraview.Flash
import kr.co.yogiyo.rookiephotoapp.R

class CameraViewModel(context: Context) : BaseObservable() {
    private val DELAY_DURATIONS = mutableListOf(0, 2, 5, 10)

    val facingButtonLabel: ObservableField<String> = ObservableField(context.getString(R.string.text_change))

    val flashButtonLabel: ObservableField<String> = ObservableField(context.getString(R.string.text_flash))

    val delayButtonLabel: ObservableField<String> = ObservableField(context.getString(R.string.text_timer))

    val delayVisibility: ObservableInt = ObservableInt(View.GONE)

    val delayMessageLabel: ObservableInt = ObservableInt(0)

    val frameControlButtonVisibility: ObservableInt = ObservableInt(View.VISIBLE)

    private var flashType = Flash.OFF.ordinal

    private var delayDurationIndex = 0

    private var currentCaptureID = 0

    private var captureTimer = 0

    private val timerHandler: Handler by lazy {
        Handler()
    }

    lateinit var captureNow: () -> Unit

    fun updateFacingButton(label: String) {
        facingButtonLabel.set(label)
    }

    fun updateFlashButton(label: String) {
        flashButtonLabel.set(label)
    }

    fun updateDelayButton(label: String) {
        delayButtonLabel.set(label)
    }

    fun getNextCaptureDelay() = DELAY_DURATIONS[++delayDurationIndex % DELAY_DURATIONS.size]

    fun getCaptureDelay() = DELAY_DURATIONS[delayDurationIndex % DELAY_DURATIONS.size]

    fun getNextFlashType() = ++flashType % Flash.values().size

    fun isTimerOn() = delayDurationIndex % DELAY_DURATIONS.size != 0

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
            Log.d(CameraViewModel::class.java.simpleName, "$captureTimer")
            timerHandler.postDelayed(makeDecrementTimerFunction(captureID), 1000)
        }
    }

    private fun finishDelayCapture() {
        timerHandler.removeCallbacksAndMessages(null)
        delayVisibility.set(View.GONE)
        frameControlButtonVisibility.set(View.VISIBLE)
    }
}