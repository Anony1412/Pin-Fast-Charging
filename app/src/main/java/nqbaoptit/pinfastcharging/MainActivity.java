package nqbaoptit.pinfastcharging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "QUANGBAO";

    @BindView(R.id.tv_pin_level)
    TextView tvPinLevel;

    Context context;
    boolean isCharging = false;

    private BroadcastReceiver mBatteryLevelReciver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * set rate battery
             */
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

            float batteryPct = level / (float)scale * 100;
            level = (int)batteryPct;
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
            Toast.makeText(context,"Đang sạc!", Toast.LENGTH_SHORT).show();
        } else if (!isPlugged && !isCharging){
            this.isCharging = true;
            Toast.makeText(context, "Ngừng sạc!", Toast.LENGTH_SHORT).show();
        }
    }
}
