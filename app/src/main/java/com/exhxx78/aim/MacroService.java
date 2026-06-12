package com.exhxx78.aim;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.TextView;

public class MacroService extends AccessibilityService {
    private WindowManager windowManager;
    private View targetView, triggerView;
    private WindowManager.LayoutParams targetParams, triggerParams;
    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isFiring = false;

    private Runnable fireRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFiring) {
                int[] location = new int[2];
                targetView.getLocationOnScreen(location);
                float x = location[0] + (targetView.getWidth() / 2f);
                float y = location[1] + (targetView.getHeight() / 2f);
                performClickAt(x, y);
                handler.postDelayed(this, 30); // السرعة الجنونية: نقرة كل 30 ملي ثانية
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        // 1. علامة الهدف (تسحبها فوق زر الرمي باللعبة)
        targetView = new TextView(this);
        ((TextView)targetView).setText("🎯");
        ((TextView)targetView).setTextSize(30);
        ((TextView)targetView).setGravity(Gravity.CENTER);
        GradientDrawable targetBg = new GradientDrawable();
        targetBg.setShape(GradientDrawable.OVAL);
        targetBg.setStroke(4, Color.RED);
        targetBg.setColor(Color.parseColor("#40FF0000")); // شفاف
        targetView.setBackground(targetBg);

        targetParams = new WindowManager.LayoutParams(
                120, 120, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        targetParams.gravity = Gravity.TOP | Gravity.START;
        targetParams.x = 500; targetParams.y = 500;
        makeDraggable(targetView, targetParams);
        windowManager.addView(targetView, targetParams);

        // 2. زر إطلاق الماكرو (تضغط عليه باستمرار للرمي)
        triggerView = new Button(this);
        ((Button)triggerView).setText("🔥 زر الماكرو");
        ((Button)triggerView).setTextColor(Color.WHITE);
        GradientDrawable triggerBg = new GradientDrawable();
        triggerBg.setCornerRadius(30f);
        triggerBg.setColor(Color.parseColor("#D00000")); // أحمر
        triggerBg.setStroke(3, Color.BLACK);
        triggerView.setBackground(triggerBg);

        triggerParams = new WindowManager.LayoutParams(
                280, 150, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        triggerParams.gravity = Gravity.TOP | Gravity.START;
        triggerParams.x = 100; triggerParams.y = 100;

        // كود سحب وإفلات زر الإطلاق + تفعيل الرمي
        triggerView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = triggerParams.x; initialY = triggerParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isDragging = false;
                        isFiring = true;
                        handler.post(fireRunnable); // بدء الرشق
                        triggerBg.setColor(Color.parseColor("#00FF00")); // تحويله للأخضر أثناء الرمي
                        triggerView.setBackground(triggerBg);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 15 || Math.abs(event.getRawY() - initialTouchY) > 15) {
                            isDragging = true;
                            isFiring = false; // إيقاف الرمي أثناء سحب الزر
                            triggerBg.setColor(Color.parseColor("#D00000"));
                            triggerView.setBackground(triggerBg);
                        }
                        if (isDragging) {
                            triggerParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            triggerParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(triggerView, triggerParams);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isFiring = false; // توقف الرمي عند رفع الإصبع
                        triggerBg.setColor(Color.parseColor("#D00000"));
                        triggerView.setBackground(triggerBg);
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(triggerView, triggerParams);
    }

    private void makeDraggable(View view, WindowManager.LayoutParams params) {
        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x; initialY = params.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(view, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void performClickAt(float x, float y) {
        Path path = new Path();
        path.moveTo(x, y);
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 10)); // 10ms مدة الضغطة
        dispatchGesture(builder.build(), null, null);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    
    @Override 
    public boolean onUnbind(Intent intent) {
        isFiring = false;
        if (targetView != null) windowManager.removeView(targetView);
        if (triggerView != null) windowManager.removeView(triggerView);
        return super.onUnbind(intent);
    }
}
