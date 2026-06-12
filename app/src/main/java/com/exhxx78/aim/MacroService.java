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
                
                // فاصل 40 ملي ثانية: يعطيك 16 طلقة بالثانية ويسمح لإصبعك الثاني بالحركة (يمنع تجميد الشاشة)
                handler.postDelayed(this, 40); 
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        cleanupViews();

        int layoutFlag = WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY;

        // 1. الهدف 🎯 (تصميم صغير جداً وشفاف)
        targetView = new TextView(this);
        ((TextView)targetView).setText("🎯");
        ((TextView)targetView).setTextSize(20);
        ((TextView)targetView).setGravity(Gravity.CENTER);
        GradientDrawable targetBg = new GradientDrawable();
        targetBg.setShape(GradientDrawable.OVAL);
        targetBg.setStroke(2, Color.RED);
        targetBg.setColor(Color.parseColor("#20FF0000")); // شفافية عالية
        targetView.setBackground(targetBg);

        targetParams = new WindowManager.LayoutParams(
                80, 80, layoutFlag, // حجم صغير (80x80)
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        targetParams.gravity = Gravity.TOP | Gravity.START;
        targetParams.x = 500; targetParams.y = 500;
        makeDraggable(targetView, targetParams);
        windowManager.addView(targetView, targetParams);

        // 2. زر الإطلاق 🔥 (دائرة صغيرة شفافة وما تحجب الرؤية)
        triggerView = new TextView(this);
        ((TextView)triggerView).setText("🔥");
        ((TextView)triggerView).setTextColor(Color.WHITE);
        ((TextView)triggerView).setTextSize(26);
        ((TextView)triggerView).setGravity(Gravity.CENTER);
        GradientDrawable triggerBg = new GradientDrawable();
        triggerBg.setShape(GradientDrawable.OVAL); // دائري
        triggerBg.setColor(Color.parseColor("#90D00000")); // أحمر شفاف
        triggerBg.setStroke(3, Color.BLACK);
        triggerView.setBackground(triggerBg);

        triggerParams = new WindowManager.LayoutParams(
                140, 140, layoutFlag, // حجم مثالي للإصبع بدون إزعاج
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
                        targetParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; // وضع الشبح
                        windowManager.updateViewLayout(targetView, targetParams);

                        initialX = triggerParams.x; initialY = triggerParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isDragging = false;
                        isFiring = true;
                        handler.post(fireRunnable);
                        triggerBg.setColor(Color.parseColor("#9000FF00")); // أخضر شفاف عند الإطلاق
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
                            triggerBg.setColor(Color.parseColor("#90D00000"));
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
                        triggerBg.setColor(Color.parseColor("#90D00000"));
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
        path.lineTo(x + 1, y + 1); // حيلة البكسل الواحد (Anti-Cheat Bypass)
        
        GestureDescription.Builder builder = new GestureDescription.Builder();
        // مدة النقرة 20 ملي ثانية (لمسة بشرية حقيقية 100% تقبلها كل الألعاب)
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 20)); 
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
    @Override public boolean onUnbind(Intent intent) {
        isFiring = false;
        cleanupViews();
        return super.onUnbind(intent);
    }
}
