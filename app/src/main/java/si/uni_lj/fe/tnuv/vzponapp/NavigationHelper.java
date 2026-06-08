package si.uni_lj.fe.tnuv.vzponapp;

import android.app.Activity;
import android.content.Intent;
import android.widget.LinearLayout;

public class NavigationHelper {

    public static void openProfile(Activity activity) {
        activity.startActivity(new Intent(activity, ProfileActivity.class));
    }

    public static void openMap(Activity activity, int trailIndex) {
        Intent intent = new Intent(activity, MapActivity.class);
        intent.putExtra("trail_index", trailIndex);
        activity.startActivity(intent);
    }
}