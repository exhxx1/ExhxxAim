package com.exhxx78.aim;

import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private View crosshairView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // تصميم الإيم (هنا سوينا نقطة حمراء فخمة ودائرية بنص الشاشة)
        crosshairView = new View(this);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(Color.RED); // لون الإيم أحمر ناري واضح
        crosshairView.setBackground(dot);

        // إعدادات تثبيت الإيم وسط الشاشة بالضبط وعدم حجب اللمس عن اللعبة
        int layoutFlag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // حجم الإيم (هنا 40 في 40 بكسل، تكدر تكبره أو تصغره)
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                40, 40,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER; // السحر هنا: التمركز في الوسط بالضبط
        params.x = 0;
        params.y = 0;

        windowManager.addView(crosshairView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (crosshairView != null) {
            windowManager.removeView(crosshairView);
        }
    }
}
