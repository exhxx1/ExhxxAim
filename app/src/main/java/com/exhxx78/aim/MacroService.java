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
                float x = targetParams.x + (targetView.getWidth() / 2f);
                float y = targetParams.y + (targetView.getHeight() / 2f);
                performClickAt(x, y);
                handler.postDelayed(this, 10); // السرعة القصوى
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        
        // إزالة الأزرار القديمة إذا كانت موجودة لمنع التكرار
        cleanupViews();

        // السحر هنا: استخدام طبقة إمكانية الوصول الموثوقة من النظام
        int layoutFlag = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

        // 1. إنشاء الهدف 🎯
        targetView = new TextView(this);
        ((TextView)targetView).setText("🎯");
        ((TextView)targetView).setTextSize(35);
        ((TextView)targetView).setGravity(Gravity.CENTER);
        GradientDrawable targetBg = new GradientDrawable();
        targetBg.setShape(GradientDrawable.OVAL);
        targetBg.setStroke(4, Color.RED);
        targetBg.setColor(Color.parseColor("#40FF0000"));
        targetView.setBackground(targetBg);

        targetParams = new WindowManager.LayoutParams(
                130, 130, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        targetParams.gravity = Gravity.TOP | Gravity.START;
        targetParams.x = 400; targetParams.y = 400;
        makeDraggable(targetView, targetParams);
        windowManager.addView(targetView, targetParams);

        // 2. إنشاء زر الإطلاق 🔥 (تم تغييره لـ TextView لضمان عدم الاختفاء)
        triggerView = new TextView(this);
        ((TextView)triggerView).setText("🔥 زر الماكرو");
        ((TextView)triggerView).setTextColor(Color.WHITE);
        ((TextView)triggerView).setTextSize(18);
        ((TextView)triggerView).setGravity(Gravity.CENTER);
        GradientDrawable triggerBg = new GradientDrawable();
        triggerBg.setCornerRadius(30f);
        triggerBg.setColor(Color.parseColor("#D00000"));
        triggerBg.setStroke(4, Color.BLACK);
        triggerView.setBackground(triggerBg);

        triggerParams = new WindowManager.LayoutParams(
                300, 140, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        triggerParams.gravity = Gravity.TOP | Gravity.START;
        triggerParams.x = 100; triggerParams.y = 100;

        triggerView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        targetParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        windowManager.updateViewLayout(targetView, targetParams);

                        initialX = triggerParams.x; initialY = triggerParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isDragging = false;
                        isFiring = true;
                        handler.post(fireRunnable);
                        triggerBg.setColor(Color.parseColor("#00FF00"));
                        triggerView.setBackground(triggerBg);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 15 || Math.abs(event.getRawY() - initialTouchY) > 15) {
                            isDragging = true;
                            if (isFiring) {
                                isFiring = false;
                                targetParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                                windowManager.updateViewLayout(targetView, targetParams);
                            }
                            triggerBg.setColor(Color.parseColor("#D00000"));
                            triggerView.setBackground(triggerBg);
                            triggerParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            triggerParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(triggerView, triggerParams);
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isFiring = false;
                        targetParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        windowManager.updateViewLayout(targetView, targetParams);

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
        path.lineTo(x + 1, y + 1); 
        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 1)); 
        dispatchGesture(builder.build(), null, null);
    }

    private void cleanupViews() {
        if (targetView != null) {
            try { windowManager.removeView(targetView); } catch (Exception e) {}
            targetView = null;
        }
        if (triggerView != null) {
            try { windowManager.removeView(triggerView); } catch (Exception e) {}
            triggerView = null;
        }
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    
    @Override 
    public boolean onUnbind(Intent intent) {
        isFiring = false;
        cleanupViews();
        return super.onUnbind(intent);
    }
}
