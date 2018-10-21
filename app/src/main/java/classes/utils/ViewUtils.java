package classes.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.antianyu.mymusic.R;

public class ViewUtils {

    public static String getString(int resID) {
        return MusicApplication.getContext().getResources().getString(resID);
    }

    public static int getColor(int resID) {
        return MusicApplication.getContext().getResources().getColor(resID);
    }

    public static void showToast(int resID) {
        Toast.makeText(MusicApplication.getContext(), resID, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(String content) {
        Toast.makeText(MusicApplication.getContext(), content, Toast.LENGTH_SHORT).show();
    }

    public static int dpToPixel(double dp) {
        DisplayMetrics metrics = MusicApplication.getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) dp, metrics);
    }

    public static int getPhoneWindowHeight() {
        DisplayMetrics metrics = MusicApplication.getContext().getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    public static int getStatusBarHeight() {
        int resourceId =
            MusicApplication.getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        return resourceId > 0 ? MusicApplication.getContext().getResources().getDimensionPixelSize(resourceId) : 0;
    }

    public static int getActionBarHeight() {
        Context context = MusicApplication.getContext();
        TypedValue typedValue = new TypedValue();
        MusicApplication.getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true);
        return context.getResources().getDimensionPixelSize(typedValue.resourceId);
    }

    public static PopupWindow buildBottomPopupWindow(final Activity activity, View view) {
        int backgroundColor = ViewUtils.getColor(android.R.color.transparent);

        PopupWindow popupWindow = new PopupWindow(activity);
        popupWindow.setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setContentView(view);
        popupWindow.setBackgroundDrawable(new ColorDrawable(backgroundColor));
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setAnimationStyle(R.style.WindowBottomAnimation);
        popupWindow.setOnDismissListener(() -> recoverBackground(activity));

        return popupWindow;
    }

    public static void dimBackground(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.alpha = 0.4f;
        activity.getWindow().setAttributes(params);
    }

    private static void recoverBackground(Activity activity) {
        WindowManager.LayoutParams params = activity.getWindow().getAttributes();
        params.alpha = 1f;
        activity.getWindow().setAttributes(params);
    }
}
