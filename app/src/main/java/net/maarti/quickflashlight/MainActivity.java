package net.maarti.quickflashlight;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Camera cam = null;
    //Variable to store brightness value
    private int brightness;
    //Content resolver used as a handle to the system's settings
    private ContentResolver cResolver;
    //Window object, that will store a reference to the current window
    private Window window;
    Switch vSwitchFlash = null, vSwitchWhite = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vSwitchFlash = (Switch) findViewById(R.id.switchFlash);
        vSwitchWhite = (Switch) findViewById(R.id.switchWhite);

        vSwitchFlash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    turnOnFlashLight();
                else
                    turnOffFlashLight();
            }
        });

        vSwitchWhite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    setWhiteScreen();
                else
                    setDefaultScreen();
            }
        });

        initBrightness();
    }




    @Override
    protected void onStart() {
        super.onStart();
        turnOnFlashLight();
        setMaxBrightness();
    }

    @Override
    protected void onStop() {
        super.onStop();
        turnOffFlashLight();
    }



    ///////////////
    // FLASHLIGHT
    ///////////////

    private void turnOnFlashLight() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
                vSwitchFlash.setChecked(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            vSwitchFlash.setChecked(false);
            Toast.makeText(getBaseContext(), R.string.toast_exceptionFlashOn, Toast.LENGTH_SHORT).show();
        }
    }

    private void turnOffFlashLight() {
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
                vSwitchFlash.setChecked(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
            //vSwitchFlash.setChecked(true);
            Toast.makeText(getBaseContext(), R.string.toast_exceptionFlashOff, Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////
    // BRIGHTNESS
    ///////////////

    private void setDefaultScreen() {
        setDefaultBrightness();
    }

    private void setWhiteScreen() {
        setMaxBrightness();
    }

    private void initBrightness(){
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

    private void setMaxBrightness(){
        //Set the system brightness using the brightness variable value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        //Get the current window attributes
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        //Set the brightness of this window
        //layoutpars.screenBrightness = brightness / (float)255;
        layoutpars.screenBrightness = 1F;
        //Apply attribute changes to this window
        window.setAttributes(layoutpars);
        vSwitchWhite.setChecked(true);
    }

    private void setDefaultBrightness(){
        //Set the system brightness using the brightness variable value
        Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
        //Get the current window attributes
        WindowManager.LayoutParams layoutpars = window.getAttributes();
        //Set the brightness of this window
        //layoutpars.screenBrightness = brightness / (float)255;
        layoutpars.screenBrightness = (float) brightness;
        //Apply attribute changes to this window
        window.setAttributes(layoutpars);
        vSwitchWhite.setChecked(false);
    }



    ///////////////
    // ONCLICK
    ///////////////

    public void onClickAuthor(View view) {
        String appSite = getString(R.string.appWebSiteUrl)+"#quickflashlight";
        Uri uri = Uri.parse(appSite);
        Intent webActivity = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(webActivity);
    }

    public void onClickAppTitle(View view) {
        Uri uri = Uri.parse("market://details?id=" + getApplicationContext().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }else{
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        }

        Toast.makeText(this, R.string.toast_rate_app, Toast.LENGTH_LONG).show();
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getApplicationContext().getPackageName())));
        }
    }
}
