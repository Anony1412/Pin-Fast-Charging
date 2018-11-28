package nqbaoptit.pinfastcharging;

import android.content.Context;
import android.content.pm.PackageManager;

public class PackageUtil {

    static boolean checkPermission(Context context, Class<Manifest.permission> accessFineLocation) {

        int res = context.checkCallingOrSelfPermission(String.valueOf(accessFineLocation));
        return (res == PackageManager.PERMISSION_GRANTED);

    }

}
