package com.exhxx78.aim;

import android.app.Service;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private DrawView crosshairView;
    private WindowManager.LayoutParams crossParams;
    private ScrollView rootMenuScroll;
    private LinearLayout menuLayout;
    private SharedPreferences prefs;
    private boolean isAimVisible = true;
    private boolean isLockMode = true;

    @Override public IBinder onBind(Intent intent) { return null; }
    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_NOT_STICKY; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);

        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        crosshairView = new DrawView(this, prefs.getInt("shape", 0), prefs.getString("color", "#39FF14"), prefs.getInt("aim_size_progress", 100) / 100f);
        crossParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        crossParams.gravity = Gravity.CENTER;
        windowManager.addView(crosshairView, crossParams);

        rootMenuScroll = new ScrollView(this);
        rootMenuScroll.setBackgroundColor(Color.parseColor("#F2121212"));
        rootMenuScroll.setVisibility(View.GONE);
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(20, 20, 20, 20);

        // إضافة ميزة المنظور البرمجي (بدون روت)
        Button btnVirtualIpad = new Button(this);
        btnVirtualIpad.setText("تفعيل المنظور العريض (Virtual FOV) 📱");
        btnVirtualIpad.setTextColor(Color.WHITE);
        btnVirtualIpad.setBackgroundColor(Color.parseColor("#6200EE"));
        btnVirtualIpad.setOnClickListener(v -> {
            // هنا نغير الـ Scale الخاص بالرؤية في الـ View
            crosshairView.setVirtualFOV(1.25f); // تكبير مجال الرؤية برمجياً
        });
        menuLayout.addView(btnVirtualIpad);

        // ... (باقي أزرار القائمة كما هي)
        rootMenuScroll.addView(menuLayout);
        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(600, 800, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.START; menuParams.x = 100; menuParams.y = 100;
        windowManager.addView(rootMenuScroll, menuParams);
        
        // ... (إضافة زر الـ ≡ كما في الكود السابق)
    }

    private class DrawView extends View {
        private float fov = 1.0f; // نسبة الرؤية
        public void setVirtualFOV(float f) { this.fov = f; invalidate(); }
        // ... (باقي كود الرسم)
        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.scale(fov, 1.0f, getWidth()/2, getHeight()/2); // توسيع الرؤية أفقياً
            // ... الرسم
        }
    }
}
