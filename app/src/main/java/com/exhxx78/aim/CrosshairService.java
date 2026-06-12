package com.exhxx78.aim;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private DrawView crosshairView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // جلب الشكل الذي اختاره المستخدم من الذاكرة
        SharedPreferences prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);
        int shapeType = prefs.getInt("shape", 0); 

        // تشغيل محرك الرسم
        crosshairView = new DrawView(this, shapeType);

        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        // كبرنا مربع الرسم حتى يستوعب الأشكال الكبيرة مثل القناص
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                120, 120,
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

    // كلاس الرسم الهندسي الاحترافي
    private class DrawView extends View {
        private Paint paint;
        private int shape;

        public DrawView(Context context, int shapeType) {
            super(context);
            this.shape = shapeType;
            paint = new Paint();
            paint.setAntiAlias(true); // تنعيم الحواف
            paint.setColor(Color.RED); // لون الإيم أحمر فاقع
            paint.setStrokeWidth(5f); // سمك الخطوط
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;

            if (shape == 0) {
                // نقطة
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, 8, paint);
            } 
            else if (shape == 1) {
                // علامة زائد
                canvas.drawLine(cx - 25, cy, cx + 25, cy, paint);
                canvas.drawLine(cx, cy - 25, cx, cy + 25, paint);
            } 
            else if (shape == 2) {
                // دائرة مفرغة بنصها نقطة
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(cx, cy, 18, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, 4, paint);
            } 
            else if (shape == 3) {
                // قناص (دائرة + خطوط متقاطعة)
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(cx, cy, 25, paint);
                canvas.drawLine(cx - 35, cy, cx + 35, cy, paint);
                canvas.drawLine(cx, cy - 35, cx, cy + 35, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(cx, cy, 4, paint); // نقطة السنتر
            }
        }
    }
}
