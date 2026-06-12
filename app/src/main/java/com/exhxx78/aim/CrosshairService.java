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

        // تصميم الإيم
        crosshairView = new View(this);
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(Color.RED); 
        crosshairView.setBackground(dot);

        int layoutFlag;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            layoutFlag = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutFlag = WindowManager.LayoutParams.TYPE_PHONE;
        }

        // السحر هنا: أضفنا أوامر (FLAG_LAYOUT_NO_LIMITS) و (FLAG_LAYOUT_IN_SCREEN)
        // هاي الأوامر تجبر الإيم يتجاهل النوتش ويقعد بنص الشاشة الحقيقية للعبة
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                35, 35, // صغرنا حجمه شوية حتى يصير أدق بالتصويب
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.CENTER;
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
