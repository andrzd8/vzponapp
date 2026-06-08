package si.uni_lj.fe.tnuv.vzponapp;

import android.app.Activity;
import android.content.Intent;

public class NavigationHelper {

    public static final String EXTRA_TRAIL_INDEX = "trail_index";

    public static void openHome(Activity activity) {
        activity.startActivity(new Intent(activity, HomeActivity.class));
    }

    public static void openProfile(Activity activity) {
        activity.startActivity(new Intent(activity, ProfileActivity.class));
    }

    public static void openMap(Activity activity, int trailIndex) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra(EXTRA_TRAIL_INDEX, trailIndex);
        activity.startActivity(intent);
    }
}