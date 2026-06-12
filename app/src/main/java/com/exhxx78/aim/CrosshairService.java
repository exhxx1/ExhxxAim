package com.exhxx78.aim;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrosshairService extends AccessibilityService {
    private WindowManager windowManager;
    private DrawView crosshairView;
    private TextView btnSettings, antiRecoilView;
    private LinearLayout menuLayout;
    private SharedPreferences prefs;
    
    // متغيرات ثبات السلاح
    private int recoilStrength;
    private boolean isAntiRecoilActive;
    private boolean isFiring = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private WindowManager.LayoutParams antiRecoilParams;

    private Runnable recoilRunnable = new Runnable() {
        @Override
        public void run() {
            if (isFiring && antiRecoilView != null) {
                float x = antiRecoilParams.x + (antiRecoilView.getWidth() / 2f);
                float y = antiRecoilParams.y + (antiRecoilView.getHeight() / 2f);
                
                // السحبة المجهرية للأسفل (تدمج النقرة مع سحب الشاشة لمقاومة الارتداد)
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x, y + recoilStrength); 
                
                GestureDescription.Builder builder = new GestureDescription.Builder();
                builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 20)); 
                dispatchGesture(builder.build(), null, null);
                
                // سرعة الإطلاق (40 ملي ثانية تسمح بحركة الشاشة بسلاسة)
                handler.postDelayed(this, 40); 
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);

        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        int shapeType = prefs.getInt("shape", 0); 
        String savedColor = prefs.getString("color", "#39FF14"); 
        recoilStrength = prefs.getInt("recoil_strength", 5);
        isAntiRecoilActive = prefs.getBoolean("recoil_active", false);

        // 1. الإيم الأساسي
        crosshairView = new DrawView(this, shapeType, savedColor);
        WindowManager.LayoutParams crossParams = new WindowManager.LayoutParams(
                400, 400, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        crossParams.gravity = Gravity.CENTER;
        windowManager.addView(crosshairView, crossParams);

        // 2. زر إطلاق النار والثبات (Anti-Recoil Button)
        antiRecoilView = new TextView(this);
        antiRecoilView.setText("🔫");
        antiRecoilView.setTextSize(26);
        antiRecoilView.setGravity(Gravity.CENTER);
        GradientDrawable recoilBg = new GradientDrawable();
        recoilBg.setShape(GradientDrawable.OVAL);
        recoilBg.setColor(Color.parseColor("#90D00000")); // أحمر شفاف
        recoilBg.setStroke(3, Color.parseColor("#FFD700"));
        antiRecoilView.setBackground(recoilBg);

        antiRecoilParams = new WindowManager.LayoutParams(
                140, 140, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        antiRecoilParams.gravity = Gravity.TOP | Gravity.START;
        antiRecoilParams.x = 200; antiRecoilParams.y = 500;
        
        antiRecoilView.setVisibility(isAntiRecoilActive ? View.VISIBLE : View.GONE);
        windowManager.addView(antiRecoilView, antiRecoilParams);

        antiRecoilView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        antiRecoilParams.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        windowManager.updateViewLayout(antiRecoilView, antiRecoilParams);
                        initialX = antiRecoilParams.x; initialY = antiRecoilParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isDragging = false;
                        isFiring = true;
                        handler.post(recoilRunnable);
                        recoilBg.setColor(Color.parseColor("#9000FF00")); // أخضر عند الرمي
                        antiRecoilView.setBackground(recoilBg);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 15 || Math.abs(event.getRawY() - initialTouchY) > 15) {
                            isDragging = true;
                            if (isFiring) {
                                isFiring = false;
                                antiRecoilParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                                windowManager.updateViewLayout(antiRecoilView, antiRecoilParams);
                            }
                            antiRecoilParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            antiRecoilParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(antiRecoilView, antiRecoilParams);
                            recoilBg.setColor(Color.parseColor("#90D00000"));
                            antiRecoilView.setBackground(recoilBg);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        isFiring = false;
                        antiRecoilParams.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
                        windowManager.updateViewLayout(antiRecoilView, antiRecoilParams);
                        recoilBg.setColor(Color.parseColor("#90D00000"));
                        antiRecoilView.setBackground(recoilBg);
                        return true;
                }
                return false;
            }
        });

        // 3. القائمة العائمة
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.parseColor("#F2121212")); 
        menuLayout.setPadding(20, 20, 20, 20);
        menuLayout.setVisibility(View.GONE); 

        // - شريط الألوان
        LinearLayout colorLayout = new LinearLayout(this);
        colorLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorLayout.setGravity(Gravity.CENTER);
        String[] colors = {"#39FF14", "#FF0000", "#00E5FF", "#FFD700", "#FFFFFF"}; 
        for (String c : colors) {
            Button cb = new Button(this);
            GradientDrawable cd = new GradientDrawable(); cd.setShape(GradientDrawable.OVAL); cd.setColor(Color.parseColor(c)); cd.setStroke(2, Color.DKGRAY);
            cb.setBackground(cd);
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(70, 70); clp.setMargins(10, 10, 10, 20); cb.setLayoutParams(clp);
            cb.setOnClickListener(v -> { prefs.edit().putString("color", c).apply(); crosshairView.setColor(c); });
            colorLayout.addView(cb);
        }
        menuLayout.addView(colorLayout);

        // - قسم لوحة تحكم ثبات السلاح (Anti-Recoil Settings)
        LinearLayout recoilBox = new LinearLayout(this);
        recoilBox.setOrientation(LinearLayout.VERTICAL);
        recoilBox.setBackgroundColor(Color.parseColor("#242424"));
        recoilBox.setPadding(15, 15, 15, 15);
        LinearLayout.LayoutParams boxParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        boxParams.setMargins(0, 0, 0, 20);
        recoilBox.setLayoutParams(boxParams);

        TextView recoilTitle = new TextView(this);
        recoilTitle.setText("🔫 لوحة تحكم ثبات السلاح");
        recoilTitle.setTextColor(Color.parseColor("#FFD700"));
        recoilTitle.setGravity(Gravity.CENTER);
        recoilBox.addView(recoilTitle);

        LinearLayout recoilControls = new LinearLayout(this);
        recoilControls.setOrientation(LinearLayout.HORIZONTAL);
        recoilControls.setGravity(Gravity.CENTER);
        recoilControls.setPadding(0, 10, 0, 0);

        Button btnToggleRecoil = new Button(this);
        btnToggleRecoil.setText(isAntiRecoilActive ? "إيقاف 🔴" : "تشغيل 🟢");
        btnToggleRecoil.setTextColor(Color.WHITE);
        btnToggleRecoil.setBackgroundColor(Color.parseColor("#313244"));

        Button btnMinus = new Button(this); btnMinus.setText(" - "); btnMinus.setTextColor(Color.WHITE); btnMinus.setBackgroundColor(Color.parseColor("#D50000"));
        TextView txtStrength = new TextView(this); txtStrength.setText(" قوة: " + recoilStrength + " "); txtStrength.setTextColor(Color.WHITE); txtStrength.setTextSize(16);
        Button btnPlus = new Button(this); btnPlus.setText(" + "); btnPlus.setTextColor(Color.WHITE); btnPlus.setBackgroundColor(Color.parseColor("#00C853"));

        btnToggleRecoil.setOnClickListener(v -> {
            isAntiRecoilActive = !isAntiRecoilActive;
            prefs.edit().putBoolean("recoil_active", isAntiRecoilActive).apply();
            btnToggleRecoil.setText(isAntiRecoilActive ? "إيقاف 🔴" : "تشغيل 🟢");
            antiRecoilView.setVisibility(isAntiRecoilActive ? View.VISIBLE : View.GONE);
        });

        btnMinus.setOnClickListener(v -> {
            if(recoilStrength > 1) {
                recoilStrength--;
                prefs.edit().putInt("recoil_strength", recoilStrength).apply();
                txtStrength.setText(" قوة: " + recoilStrength + " ");
            }
        });

        btnPlus.setOnClickListener(v -> {
            if(recoilStrength < 50) {
                recoilStrength++;
                prefs.edit().putInt("recoil_strength", recoilStrength).apply();
                txtStrength.setText(" قوة: " + recoilStrength + " ");
            }
        });

        recoilControls.addView(btnToggleRecoil);
        recoilControls.addView(btnMinus);
        recoilControls.addView(txtStrength);
        recoilControls.addView(btnPlus);
        recoilBox.addView(recoilControls);
        menuLayout.addView(recoilBox);

        // - زر الإغلاق الشامل
        Button btnCloseAll = new Button(this);
        btnCloseAll.setText("إيقاف التطبيق بالكامل [ X ]");
        btnCloseAll.setTextColor(Color.WHITE);
        GradientDrawable closeBg = new GradientDrawable(); closeBg.setColor(Color.parseColor("#D50000")); closeBg.setCornerRadius(10f);
        btnCloseAll.setBackground(closeBg); btnCloseAll.setPadding(10, 20, 10, 20);
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); closeLp.setMargins(0, 0, 0, 20);
        btnCloseAll.setLayoutParams(closeLp);
        btnCloseAll.setOnClickListener(v -> stopSelf());
        menuLayout.addView(btnCloseAll);

        // - قائمة الأشكال (101 سكوب)
        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this); list.setOrientation(LinearLayout.VERTICAL);
        String[] scopes = new String[101];
        scopes[0] = "1. دائرة القنص العملاقة (الأساسية) 🎯";
        for (int i = 1; i <= 100; i++) { scopes[i] = (i + 1) + ". سكوب تكتيكي عملاق V" + i + " 🔭"; }
        for (int i = 0; i < scopes.length; i++) {
            Button b = new Button(this); b.setText(scopes[i]); b.setTextColor(Color.parseColor("#E0E0E0"));
            GradientDrawable btnBg = new GradientDrawable(); btnBg.setColor(Color.parseColor("#242424")); btnBg.setCornerRadius(8f);
            b.setBackground(btnBg); b.setPadding(20, 15, 20, 15);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); lp.setMargins(0, 5, 0, 5); b.setLayoutParams(lp);
            final int finalI = i;
            b.setOnClickListener(v -> { prefs.edit().putInt("shape", finalI).apply(); crosshairView.setShape(finalI); menuLayout.setVisibility(View.GONE); });
            list.addView(b);
        }
        scrollView.addView(list);
        menuLayout.addView(scrollView);

        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(600, 850, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.START; menuParams.x = 200; menuParams.y = 200;
        windowManager.addView(menuLayout, menuParams);

        // 4. زر القائمة العائم ≡
        btnSettings = new TextView(this); btnSettings.setText("≡"); btnSettings.setTextColor(Color.WHITE); btnSettings.setTextSize(30); btnSettings.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable(); bg.setShape(GradientDrawable.OVAL); bg.setColor(Color.parseColor("#90000000")); btnSettings.setBackground(bg);
        WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams(120, 120, layoutFlag, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        btnParams.gravity = Gravity.TOP | Gravity.START; btnParams.x = 50; btnParams.y = 200;

        btnSettings.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY; private float initialTouchX, initialTouchY; private boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = btnParams.x; initialY = btnParams.y; initialTouchX = event.getRawX(); initialTouchY = event.getRawY(); isDragging = false; return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 10 || Math.abs(event.getRawY() - initialTouchY) > 10) {
                            isDragging = true; btnParams.x = initialX + (int) (event.getRawX() - initialTouchX); btnParams.y = initialY + (int) (event.getRawY() - initialTouchY); windowManager.updateViewLayout(btnSettings, btnParams);
                            menuParams.x = btnParams.x + 130; menuParams.y = btnParams.y; if (menuLayout.getVisibility() == View.VISIBLE) { windowManager.updateViewLayout(menuLayout, menuParams); }
                        } return true;
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) { menuLayout.setVisibility(menuLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE); } return true;
                } return false;
            }
        });
        windowManager.addView(btnSettings, btnParams);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {}
    @Override public void onInterrupt() {}
    @Override
    public void onDestroy() {
        super.onDestroy();
        isFiring = false;
        if (crosshairView != null) windowManager.removeView(crosshairView);
        if (btnSettings != null) windowManager.removeView(btnSettings);
        if (menuLayout != null) windowManager.removeView(menuLayout);
        if (antiRecoilView != null) windowManager.removeView(antiRecoilView);
    }

    // محرك الرسم (نفسه للـ 101 سكوب عملاق)
    private class DrawView extends View {
        private Paint mainPaint, bgPaint; private int shape; private String colorHex;
        public DrawView(Context context, int shapeType, String colorHex) {
            super(context); this.shape = shapeType; this.colorHex = colorHex;
            mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG); mainPaint.setColor(Color.parseColor(colorHex)); mainPaint.setStyle(Paint.Style.STROKE); mainPaint.setStrokeWidth(4f); 
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG); bgPaint.setColor(Color.BLACK); bgPaint.setStyle(Paint.Style.STROKE); bgPaint.setStrokeWidth(8f); 
        }
        public void setShape(int newShape) { this.shape = newShape; invalidate(); }
        public void setColor(String newColor) { this.colorHex = newColor; mainPaint.setColor(Color.parseColor(newColor)); invalidate(); }
        private void drawLinePro(Canvas c, float startX, float startY, float stopX, float stopY) { c.drawLine(startX, startY, stopX, stopY, bgPaint); c.drawLine(startX, startY, stopX, stopY, mainPaint); }
        private void drawCirclePro(Canvas c, float cx, float cy, float radius, boolean fill) {
            bgPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE); mainPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            c.drawCircle(cx, cy, fill ? radius + 1.5f : radius, bgPaint); c.drawCircle(cx, cy, radius, mainPaint);
        }
        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas); int cx = getWidth() / 2; int cy = getHeight() / 2;
            if (shape == 0) {
                drawCirclePro(canvas, cx, cy, 100f, false); drawLinePro(canvas, cx - 120, cy, cx - 80, cy); drawLinePro(canvas, cx + 80, cy, cx + 120, cy); 
                drawLinePro(canvas, cx, cy - 120, cx, cy - 80); drawLinePro(canvas, cx, cy + 80, cx, cy + 120); drawCirclePro(canvas, cx, cy, 8f, true); 
            } else {
                float outRad = 85f + (shape % 8) * 5f; boolean hasInnerRing = (shape % 3 != 0); float inRad = outRad * 0.6f;
                drawCirclePro(canvas, cx, cy, outRad, false); if (hasInnerRing) drawCirclePro(canvas, cx, cy, inRad, false);
                float dotR = 2f + (shape % 6) * 1.5f; drawCirclePro(canvas, cx, cy, dotR, (shape % 4 != 0));
                int lineStyle = shape % 4;
                if (lineStyle == 0) { drawLinePro(canvas, cx - outRad - 30, cy, cx - outRad, cy); drawLinePro(canvas, cx + outRad, cy, cx + outRad + 30, cy); drawLinePro(canvas, cx, cy - outRad - 30, cx, cy - outRad); drawLinePro(canvas, cx, cy + outRad, cx, cy + outRad + 30); }
                else if (lineStyle == 1) { drawLinePro(canvas, cx - outRad - 10, cy, cx - 20, cy); drawLinePro(canvas, cx + 20, cy, cx + outRad + 10, cy); drawLinePro(canvas, cx, cy - outRad - 10, cx, cy - 20); drawLinePro(canvas, cx, cy + 20, cx, cy + outRad + 10); }
                else if (lineStyle == 2) { drawLinePro(canvas, cx - outRad, cy, cx - 30, cy); drawLinePro(canvas, cx + 30, cy, cx + outRad, cy); drawLinePro(canvas, cx, cy + 30, cx, cy + outRad); }
                else { float oOut = (float)((outRad + 10f) * 0.707f); float oIn = (float)((outRad - 10f) * 0.707f); drawLinePro(canvas, cx - oOut, cy - oOut, cx - oIn, cy - oIn); drawLinePro(canvas, cx + oIn, cy + oIn, cx + oOut, cy + oOut); drawLinePro(canvas, cx - oOut, cy + oOut, cx - oIn, cy + oIn); drawLinePro(canvas, cx + oIn, cy - oIn, cx + oOut, cy - oOut); }
                if (shape % 2 == 0) { float tickSpacing = outRad / 4f; for(int i=1; i<=3; i++) { drawCirclePro(canvas, cx + (i*tickSpacing), cy, 1.5f, true); drawCirclePro(canvas, cx - (i*tickSpacing), cy, 1.5f, true); drawCirclePro(canvas, cx, cy + (i*tickSpacing), 1.5f, true); drawCirclePro(canvas, cx, cy - (i*tickSpacing), 1.5f, true); } }
            }
        }
    }
}
