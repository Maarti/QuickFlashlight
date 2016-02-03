package net.maarti.quickflashlight;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Camera cam = null;
    //Variable to store brightness value
    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Get the content resolver
        cResolver = getContentResolver();

//Get the current window
        window = getWindow();

        try
        {
            // To handle the auto
            Settings.System.putInt(cResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            //Get the current system brightness
            brightness = Settings.System.getInt(cResolver, Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Settings.SettingNotFoundException e)
        {
            //Throw an error case it couldn't be retrieved
            Log.e("Error", "Cannot access system brightness");
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        turnOnFlashLight();
        changeBrightness();
    }

    @Override
    protected void onStop() {
        super.onStop();
        turnOffFlashLight();
    }


    public void turnOnFlashLight() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception throws in turning on flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    public void turnOffFlashLight() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "Exception throws in turning off flashlight.", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeBrightness(){
        //Set the system brightness using the brightness variable value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        //Get the current window attributes
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        //Set the brightness of this window
        //layoutpars.screenBrightness = brightness / (float)255;
        layoutpars.screenBrightness = 1F;
        //Apply attribute changes to this window
        window.setAttributes(layoutpars);
    }
}
