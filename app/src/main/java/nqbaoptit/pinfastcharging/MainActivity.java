package nqbaoptit.pinfastcharging;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

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
    CircleImageView imgWifi;

    @BindView(R.id.img_bluetooth)
    CircleImageView imgBluetooth;

    @BindView(R.id.img_gps)
    CircleImageView imgGPS;

    @BindView(R.id.img_sync)
    CircleImageView imgSync;

    Context context;
    boolean isCharging = false;
    boolean wifi_state = false;
    boolean bluetooth_state = false;
    boolean gps_state = false;

    private BroadcastReceiver mBatteryLevelReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * set rate battery
             */
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float) scale * 100;
            level = (int) batteryPct;
            tvPinLevel.setText("" + level + "%");

            detectChargingState(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        context = this;
        batteryLevel();
        getBatteryCapacity(context);
        setOnClick();
    }

    private void setOnClick() {
        btnStart.setOnClickListener(this);
        imgWifi.setOnClickListener(this);
        imgBluetooth.setOnClickListener(this);
        imgGPS.setOnClickListener(this);
        imgSync.setOnClickListener(this);
    }

    private void batteryLevel() {
        /**
         * automatic update and set rate battery
         */
        IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryLevelReciver, batteryLevelFilter);
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (locationManager.isLocationEnabled()) {
                    gps_state = true;
                } else {
                    gps_state = false;
                }
            }
            if (gps_state) {
            }
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
                Log.e(TAG, "bluetoothAdapter.disable()");
            } else {
                bluetooth_state = true;
                bluetoothAdapter.enable();
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
                WifiManager wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
                if (wifi_state) {
                    wifi_state = false;
                    wifiManager.setWifiEnabled(wifi_state);
                    Log.e(TAG, "wifi disable");
                } else {
                    wifi_state = true;
                    wifiManager.setWifiEnabled(wifi_state);
                    Log.e(TAG, "wifi enable");
                }
            }
        }
    }


    private void forceStopApp() {
//        try {
//            Process suProcess = Runtime.getRuntime().exec("su");
//            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
//            os.writeBytes("adb shell" + "\n");
//            os.flush();
//            os.writeBytes("am force-stop com.xxxxxx" + "\n");
//            os.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    boolean isTurnOn = false;

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
        int capacity = (int) batteryCapacity;
        tvBatteryCapacity.setText("" + capacity + " mAh");
        Log.e(TAG, "" + capacity);
    }
}
