package carnaude.sleepmode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.provider.Settings.System.SCREEN_BRIGHTNESS_MODE
import android.support.v7.app.AppCompatActivity
import android.view.View
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*

const val MY_PERMISSIONS_REQUEST = 42

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class TimeActivity : AppCompatActivity() {
    private val mHideHandler = Handler()
    private val mClockHandler = Handler()
    private var mContentView: CustomFontTextView? = null
    private var permOk: Boolean = false
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
    private var saveRingerValue: Int? = null

    override fun onResume() {
        super.onResume()
        val dateFormat = SimpleDateFormat("HH:mm")
        val time = Date()
        mContentView!!.text = dateFormat.format(time)
        delayedHide(1000)
        startChrono()
        if (Settings.System.canWrite(this))
            setBrightness()
        else
            openAndroidPermissionsMenu()

        if (permOk)
            muteSound()
    }

    private fun muteSound() {
        val amanager = getSystemService(AUDIO_SERVICE) as AudioManager

        //turn ringer silent
        saveRingerValue = amanager.ringerMode
        amanager.ringerMode = AudioManager.RINGER_MODE_SILENT
    }

    private fun restoreSound() {
        val amanager = getSystemService(AUDIO_SERVICE) as AudioManager

        amanager.ringerMode = saveRingerValue!!
    }

    private fun openAndroidPermissionsMenu() {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
        intent.data = Uri.parse("package:" + this.packageName)
        startActivity(intent)
    }

    @AfterPermissionGranted(MY_PERMISSIONS_REQUEST)
    private fun askPerm() {
        val perms = Manifest.permission.GET_ACCOUNTS
        if (EasyPermissions.hasPermissions(this, perms)) {
            permOk = true
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, "Toto",
                    MY_PERMISSIONS_REQUEST, perms)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
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

        Settings.System.putInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 10)  //brightness is an integer variable (0-255), but dont use 0

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
        if (Settings.System.canWrite(this))
            restoreBrightness()

        if (permOk)
            restoreSound()
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

        askPerm()
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
