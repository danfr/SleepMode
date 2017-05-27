package carnaude.sleepmode

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import java.text.SimpleDateFormat
import java.util.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TimeActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mClockHandler = Handler()
    private var mContentView: CustomFontTextView? = null
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
    }

    private val mUpdateClockRunnable = Runnable {
        val dateFormat = SimpleDateFormat("HH:mm")
        val time = Date()
        mContentView!!.text = dateFormat.format(time)
        startChrono()
    }

    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val mDelayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var saveAutoBrightnessState: Boolean? = null
    private var saveBrightnessValue: Int? = null

    override fun onResume() {
        super.onResume()
        val dateFormat = SimpleDateFormat("HH:mm")
        val time = Date()
        mContentView!!.text = dateFormat.format(time)
        delayedHide(1000)
        startChrono()
        if (Settings.System.canWrite(this))
            setBrightness()
    }

    private val MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 42

    private fun askPerm() {
        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_SETTINGS)) {

            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.
            val perms = Array(1, { Manifest.permission.WRITE_SETTINGS })
            ActivityCompat.requestPermissions(this,
                    perms,
                    MY_PERMISSIONS_REQUEST_WRITE_SETTINGS)

            // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_WRITE_SETTINGS -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    setBrightness()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }// other 'case' lines to check for other
        // permissions this app might request
    }

    private fun setBrightness() {
        var mode = -1
        try {
            mode = Settings.System.getInt(contentResolver, SCREEN_BRIGHTNESS_MODE) //this will return integer (0 or 1)
        } catch (e: Exception) {
        }


        if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            //Automatic mode
            saveAutoBrightnessState = true
            saveBrightnessValue = Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL)  //this will set the manual mode (set the automatic mode off)
        } else {
            //Manual mode
            saveAutoBrightnessState = false
            saveBrightnessValue = Settings.System.getInt(contentResolver, android.provider.Settings.System.SCREEN_BRIGHTNESS)
        }

        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 255)  //brightness is an integer variable (0-255), but dont use 0

        // Apply changes
        try {
            val br = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)  //this will get the information you have just set...

            val lp = window.attributes
            lp.screenBrightness = br.toFloat() / 255 //...and put it here
            window.attributes = lp
        } catch (e: Exception) {
        }
    }

    override fun onStop() {
        super.onStop()
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED) {
            restoreBrightness()
        }
    }

    private fun restoreBrightness() {
        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, saveBrightnessValue!!)  //brightness is an integer variable (0-255), but dont use 0
        if (saveAutoBrightnessState!!)
            Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);  //this will set the automatic mode on
    }

    private fun startChrono() {
        mClockHandler.postDelayed(mUpdateClockRunnable, 10000)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        // Save the calculator display text.
        outState.putString("displayText", mContentView!!.text.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_time)

        mVisible = true
        mContentView = findViewById(R.id.fullscreen_content) as CustomFontTextView

        // Restore the calculator display text.
        if (savedInstanceState != null) {
            val displayText = savedInstanceState.getString("displayText")
            mContentView!!.text = displayText
        }

        // Set up the user interaction to manually show or hide the system UI.
        mContentView!!.setOnClickListener { toggle() }
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        mContentView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [.AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private val AUTO_HIDE = true

        /**
         * If [.AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val UI_ANIMATION_DELAY = 300
    }


}
