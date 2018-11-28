package nqbaoptit.pinfastcharging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatInfoReceiver extends BroadcastReceiver {

    int temp = 0;
    int vol = 0;
    int health = 0;

    float get_temp(){
        return (float)(temp / 10);
    }

    float get_vol() {
        return (float) vol / 1000;
    }

    int get_health() {return health;}

    @Override
    public void onReceive(Context arg0, Intent intent) {

        temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE,0);
        vol = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);

        health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);


    }

};
