package id.ipaddr.mobile.android.location.currentlocation.helper;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by iippermana on 3/27/16.
 */
public class GoogleHelper {
    /**
     * Check the device to make sure it has the Google Play Store APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * web browser or Google Play Store or enable it in the device's system settings.
     * @param context
     * @return
     */
    public static boolean isGooglePlayInstalled(Context context) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try
        {
            PackageInfo info = pm.getPackageInfo("com.android.vending", PackageManager.GET_ACTIVITIES);
            String label = (String) info.applicationInfo.loadLabel(pm);
            app_installed = (label != null && !label.equals("Market"));
        }
        catch (PackageManager.NameNotFoundException e)
        {
            app_installed = false;
        }
        return app_installed;
    }
}
