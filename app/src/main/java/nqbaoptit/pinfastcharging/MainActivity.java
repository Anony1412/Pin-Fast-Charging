package nqbaoptit.pinfastcharging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;

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

    Context context;
    boolean isCharging = false;
    boolean isTurnOn = false;

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
        setOnClick();
    }

    private void setOnClick() {
        btnStart.setOnClickListener(this);
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
            Toast.makeText(context, "Đang sạc!", Toast.LENGTH_SHORT).show();
        } else if (!isPlugged && !isCharging) {
            this.isCharging = true;
            tvChargeState.setText("Charged Battery");
            Toast.makeText(context, "Ngừng sạc!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        int ID = v.getId();
        switch (ID) {
            case R.id.btn_start: {
                turnOn_OffFastCharge();
                forceStopApp();
            }
        }
    }

    boolean status = false;

    private void forceStopApp() {

        WifiManager wifiManager =(WifiManager)this.context.getSystemService(Context.WIFI_SERVICE);
        if (status) {
            status = false;
            wifiManager.setWifiEnabled(status);
        } else {
            status = true;
            wifiManager.setWifiEnabled(status);
        }

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

    private void turnOn_OffFastCharge() {
        if (isTurnOn) {
            isTurnOn = false;
            btnStart.setText("STOP");
            btnStart.setBackgroundResource(R.drawable.button_stop_shape);
        } else {
            isTurnOn = true;
            btnStart.setText("START");
            btnStart.setBackgroundResource(R.drawable.button_start_shape);
        }
    }
}
