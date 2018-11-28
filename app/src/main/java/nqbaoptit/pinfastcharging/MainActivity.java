package nqbaoptit.pinfastcharging;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "QUANGBAO";

    @BindView(R.id.tv_pin_level)
    TextView tvPinLevel;

    @BindView(R.id.tv_charge_state)
    TextView tvChargeState;

    @BindView(R.id.btn_start)
    Button btnStart;

    @BindView(R.id.tv_battery_capacity)
    TextView tvBatteryCapacity;

    @BindView(R.id.img_wifi)
    ImageView imgWifi;

    @BindView(R.id.img_bluetooth)
    ImageView imgBluetooth;

    @BindView(R.id.img_gps)
    ImageView imgGPS;

    @BindView(R.id.img_sync)
    ImageView imgSync;

    @BindView(R.id.img_brightness)
    ImageView imgBrightness;

    @BindView(R.id.tv_temperature)
    TextView tvTemperature;

    @BindView(R.id.tv_voltage)
    TextView tvVoltage;

    @BindView(R.id.tv_health)
    TextView tvHealth;

    @BindView(R.id.tv_typeofdevice)
    TextView tvType;

    @BindView(R.id.tv_time_remaining)
    TextView tvTimeRemaining;

    Context context;
    boolean isCharging = false;
    boolean wifi_state = false;
    boolean bluetooth_state = false;
    boolean gps_state = false;
    boolean isTurnOn = false;
    boolean brightness_reduce = false;
    int capacity = 0;
    private BroadcastReceiver mBatteryLevelReceiver = new BroadcastReceiver() {

        int temp = 0;
        int vol = 0;
        int health = 0;
        String type = "";
        String timeRemain = "";

        float get_temp() {
            return (float) (temp / 10);
        }

        float get_vol() {
            return (float) vol / 1000;
        }

        int get_health() {
            return health;
        }

        String getType() {
            return type;
        }

        String getTimeRemain() {
            return timeRemain;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * battery level
             */
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float) scale * 100;
            level = (int) batteryPct;
            tvPinLevel.setText("" + level + "%");

            /**
             * temperature
             */
            temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            String temp_value = get_temp() + Character.toString((char) 176) + " C";
            tvTemperature.setText(temp_value);

            /**
             * voltage -
             */
            vol = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
            String vol_value = get_vol() + "";
            vol_value = vol_value.substring(0, 3) + "V-1.5A";
            tvVoltage.setText(vol_value);

            /**
             * health
             */
            health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            String hea_value = get_health() + "";
            tvHealth.setText("Good");

            type = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
            tvType.setText(getType());

            if (capacity > 0) {
                int current_battery = capacity * level / 100;
                int battery_remain = capacity - current_battery;
                //1000mA = 1h = 60m
                //2900mA = 2,9h = 2h 54p
                String hour = battery_remain / 1000 + ""; // 2
                int minute = (battery_remain%1000)*60/1000; // 54
                String mi = minute + "";
                if (minute < 10) mi = "0" + minute;
                timeRemain = hour + "h " + mi + "m";
                Log.e(TAG, "timeRemain: " + timeRemain);
                tvTimeRemaining.setText(getTimeRemain());
            }

            detectChargingState(intent);
        }
    };

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

        ButterKnife.bind(this);
        context = this;

        //Get the content resolver
        cResolver = getContentResolver();
        //Get the current window
        window = getWindow();

        getBatteryCapacity(context);
        getBatteryInfo();
        setColorResource();
        setOnClick();
    }

    private void setColorResource() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mActiveNetWork = connManager.getActiveNetworkInfo();
        if (mActiveNetWork != null) {
            Log.e(TAG, "setColorResource: ádfasdfasfds");
            // connect internet
            if (mActiveNetWork.getType() == ConnectivityManager.TYPE_WIFI) {
                // wifi connected
                wifi_state = true;
            }
            if (wifi_state) {
                imgWifi.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
            } else {
                imgWifi.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
            }
        }

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
        } else {
            if (bluetoothAdapter.isEnabled()) {
                imgBluetooth.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
            } else {
                imgBluetooth.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
            }
        }

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
        } else {
            gps_state = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            if (gps_state) {
                imgGPS.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
            } else {
                imgGPS.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
            }
        }
    }

    private void setOnClick() {
        btnStart.setOnClickListener(this);
        imgWifi.setOnClickListener(this);
        imgBluetooth.setOnClickListener(this);
        imgGPS.setOnClickListener(this);
        imgSync.setOnClickListener(this);
        imgBrightness.setOnClickListener(this);
    }

    private void getBatteryInfo() {
        /**
         * automatic update and set rate battery
         */
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReceiver, batteryLevelFilter);
    }

    private void detectChargingState(Intent intent) {
        /**
         * detect charging state
         */
        int state = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        boolean isPlugged = (state > 0); // return true if state > 0
        if (isPlugged && this.isCharging) {
            this.isCharging = false;
            tvChargeState.setText("Charging Battery");
            Log.e(TAG, "cắm sạc");
        } else if (!isPlugged && !isCharging) {
            this.isCharging = true;
            tvChargeState.setText("Charged Battery");
            Log.e(TAG, "rút sạc");
        }
//        // Vibrate for 500 milliseconds
//        Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            v.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
//        } else {
//            //deprecated in API 26
//            v.vibrate(500);
//        }
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        switch (ID) {
            case R.id.btn_start: {
                Log.e(TAG, "btn start");
                turnOn_OffFastCharge();
                break;
            }
            case R.id.img_wifi: {
                wifiChangeState();
                break;
            }
            case R.id.img_bluetooth: {
                bluetoothChangeState();
                break;
            }
            case R.id.img_brightness: {
                changeBrightness();
                break;
            }
            case R.id.img_gps: {
                gpsChangeState();
                break;
            }
            case R.id.img_sync: {
                syncChangeState();
                break;
            }
        }
    }

    private void changeBrightness() {
        /**
         * brightness
         */
        if (!Settings.System.canWrite(context)) {
            Log.e(TAG, "changeBrightness: false");
            writePermission();
        } else {
            Log.e(TAG, "changeBrightness: true");
            brightnessMethod();
        }
    }

    private void brightnessMethod() {
        if (Settings.System.canWrite(context)) {
            //Set the system brightness using the brightness variable value
            Settings.System.putInt(cResolver, Settings.System.SCREEN_BRIGHTNESS, brightness);
            //Get the current window attributes
            ViewGroup.LayoutParams layoutpars = window.getAttributes();
            //Set the brightness of this window
            float temp = ((WindowManager.LayoutParams) layoutpars).screenBrightness;
            if (brightness_reduce) {
                ((WindowManager.LayoutParams) layoutpars).screenBrightness = temp;
                brightness_reduce = false;
            } else {
                ((WindowManager.LayoutParams) layoutpars).screenBrightness = brightness / (float)255;
                brightness_reduce = true;
            }
            Log.e(TAG, "brightness_reduce: " + brightness_reduce);
            //Apply attribute changes to this window
            window.setAttributes((WindowManager.LayoutParams) layoutpars);
        }
    }

    public void writePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 200);
            } else {
                brightnessMethod();
            }
        }
    }

    private void syncChangeState() {
        /**
         * sync
         */

    }

    private void gpsChangeState() {
        /**
         * gps
         */
        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Toast.makeText(context, "GPS not supported!", Toast.LENGTH_SHORT).show();
        } else {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            onResume();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        gps_state = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gps_state) {
            imgGPS.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
        } else {
            imgGPS.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
        }
    }

    private void bluetoothChangeState() {
        /**
         * bluetooth
         */
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth not supported!", Toast.LENGTH_SHORT).show();
        } else {
            if (bluetoothAdapter.isEnabled()) {
                bluetooth_state = false;
                bluetoothAdapter.disable();
                imgBluetooth.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
                Toast.makeText(context, "Bluetooth disable!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "bluetoothAdapter.disable()");
            } else {
                bluetooth_state = true;
                bluetoothAdapter.enable();
                imgBluetooth.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
                Toast.makeText(context, "Bluetooth enable!", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "bluetoothAdapter.enable()");
            }
        }
    }

    private void wifiChangeState() {
        /**
         * wifi
         */
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mActiveNetWork = connManager.getActiveNetworkInfo();
        if (mActiveNetWork != null) {
            // connect internet
            if (mActiveNetWork.getType() == ConnectivityManager.TYPE_WIFI) {
                // wifi connected
                wifi_state = true;
            }
        }
        WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        if (wifi_state) {
            wifi_state = false;
            wifiManager.setWifiEnabled(wifi_state);
            imgWifi.setColorFilter(getApplicationContext().getColor(R.color.colorWhiteDark));
            Toast.makeText(context, "Wifi disable!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "wifi disable");
        } else {
            wifi_state = true;
            wifiManager.setWifiEnabled(wifi_state);
            imgWifi.setColorFilter(getApplicationContext().getColor(R.color.colorWhite));
            Toast.makeText(context, "Wifi Enable!", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "wifi enable");
        }
    }

    private void turnOn_OffFastCharge() {
        if (isTurnOn) {
            isTurnOn = false;
            btnStart.setText("START");
            btnStart.setBackgroundResource(R.drawable.button_start_shape);
        } else {
            isTurnOn = true;
            btnStart.setText("STOP");
            btnStart.setBackgroundResource(R.drawable.button_stop_shape);
        }
    }

    public void getBatteryCapacity(Context context) {
        Object mPowerProfile;
        double batteryCapacity = 0;
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";
        try {
            mPowerProfile = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class)
                    .newInstance(context);
            batteryCapacity = (double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getBatteryCapacity")
                    .invoke(mPowerProfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        capacity = (int) batteryCapacity;
        tvBatteryCapacity.setText("" + capacity + " mAh");
        Log.e(TAG, "" + capacity);
    }

}





