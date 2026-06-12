package com.exhxx78.aim;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
import android.widget.TextView;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private DrawView crosshairView;
    private TextView btnSettings;
    private LinearLayout menuLayout;
    private SharedPreferences prefs;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);

        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        int shapeType = prefs.getInt("shape", 0); 
        String savedColor = prefs.getString("color", "#39FF14"); // الأخضر هو الافتراضي

        crosshairView = new DrawView(this, shapeType, savedColor);
        
        // كبرنا الشاشة إلى 400x400 حتى تستوعب الإيمات العملاقة براحتها
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

        // إنشاء القائمة العائمة
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setBackgroundColor(Color.parseColor("#E60F0F1A")); 
        menuLayout.setPadding(15, 15, 15, 15);
        menuLayout.setVisibility(View.GONE); 

        // 1. إضافة شريط الألوان (Color Palette)
        LinearLayout colorLayout = new LinearLayout(this);
        colorLayout.setOrientation(LinearLayout.HORIZONTAL);
        colorLayout.setGravity(Gravity.CENTER);
        
        String[] colors = {"#39FF14", "#FF0000", "#00FFFF", "#FFD700", "#FF00FF"}; // أخضر، أحمر، أزرق، أصفر، وردي
        for (String c : colors) {
            Button cb = new Button(this);
            GradientDrawable cd = new GradientDrawable();
            cd.setShape(GradientDrawable.OVAL);
            cd.setColor(Color.parseColor(c));
            cd.setStroke(3, Color.WHITE);
            cb.setBackground(cd);
            
            LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(70, 70);
            clp.setMargins(10, 10, 10, 20);
            cb.setLayoutParams(clp);
            
            cb.setOnClickListener(v -> {
                prefs.edit().putString("color", c).apply();
                crosshairView.setColor(c); // تغيير اللون فوراً
            });
            colorLayout.addView(cb);
        }
        menuLayout.addView(colorLayout);

        // 2. قائمة الأشكال والسكوبات
        ScrollView scrollView = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);

        String[] scopes = {
            "1. ليزر 🔴", "2. كلاسيك ➕", "3. أوفر واتش ⭕", "4. Apex 🔺", 
            "5. فالورانت ❌", "6. سيبربانك [.]", "7. رشاش T", "8. شوتكن ◎", 
            "9. قناص بسيط 🎯", "10. نجمة ✦", 
            "11. قناص واقعي 🔭", "12. سكوب (ACOG) 🔽", 
            "13. هولوكرافيك 🎛️", "14. ريد دوت ⭕",
            "15. علامة مرسيدس العملاقة ☮️", "16. دائرة القنص الكبيرة 🞇"
        };

        for (int i = 0; i < scopes.length; i++) {
            Button b = new Button(this);
            b.setText(scopes[i]);
            b.setTextColor(Color.WHITE);
            b.setBackgroundColor(Color.parseColor("#313244"));
            b.setPadding(20, 15, 20, 15);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 5, 0, 5);
            b.setLayoutParams(lp);

            final int finalI = i;
            b.setOnClickListener(v -> {
                prefs.edit().putInt("shape", finalI).apply();
                crosshairView.setShape(finalI);
                menuLayout.setVisibility(View.GONE); 
            });
            list.addView(b);
        }
        scrollView.addView(list);
        menuLayout.addView(scrollView);

        WindowManager.LayoutParams menuParams = new WindowManager.LayoutParams(
                550, 700, layoutFlag, // كبرنا القائمة لتسع الألوان
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.x = 200; menuParams.y = 200;
        windowManager.addView(menuLayout, menuParams);

        // إنشاء زر الإعدادات العائم (⚙️)
        btnSettings = new TextView(this);
        btnSettings.setText("⚙️");
        btnSettings.setTextSize(26);
        btnSettings.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#90000000")); 
        bg.setStroke(3, Color.parseColor("#FFD700"));
        btnSettings.setBackground(bg);

        WindowManager.LayoutParams btnParams = new WindowManager.LayoutParams(
                120, 120, layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );
        btnParams.gravity = Gravity.TOP | Gravity.START;
        btnParams.x = 50; btnParams.y = 200;

        btnSettings.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private boolean isDragging = false;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = btnParams.x; initialY = btnParams.y;
                        initialTouchX = event.getRawX(); initialTouchY = event.getRawY();
                        isDragging = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        if (Math.abs(event.getRawX() - initialTouchX) > 10 || Math.abs(event.getRawY() - initialTouchY) > 10) {
                            isDragging = true;
                            btnParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                            btnParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                            windowManager.updateViewLayout(btnSettings, btnParams);
                            menuParams.x = btnParams.x + 130;
                            menuParams.y = btnParams.y;
                            if (menuLayout.getVisibility() == View.VISIBLE) {
                                windowManager.updateViewLayout(menuLayout, menuParams);
                            }
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!isDragging) { 
                            menuLayout.setVisibility(menuLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                        }
                        return true;
                }
                return false;
            }
        });
        windowManager.addView(btnSettings, btnParams);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (crosshairView != null) windowManager.removeView(crosshairView);
        if (btnSettings != null) windowManager.removeView(btnSettings);
        if (menuLayout != null) windowManager.removeView(menuLayout);
    }

    private class DrawView extends View {
        private Paint mainPaint, bgPaint;
        private int shape;
        private String colorHex;

        public DrawView(Context context, int shapeType, String colorHex) {
            super(context);
            this.shape = shapeType;
            this.colorHex = colorHex;
            
            mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mainPaint.setColor(Color.parseColor(colorHex)); 
            mainPaint.setStyle(Paint.Style.STROKE);
            mainPaint.setStrokeWidth(4f); // عرض الخطوط الأساسية
            
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(Color.BLACK);
            bgPaint.setStyle(Paint.Style.STROKE);
            bgPaint.setStrokeWidth(8f); // التظليل الأسود
        }

        public void setShape(int newShape) {
            this.shape = newShape;
            invalidate(); 
        }

        public void setColor(String newColor) {
            this.colorHex = newColor;
            mainPaint.setColor(Color.parseColor(newColor));
            invalidate();
        }

        private void drawLinePro(Canvas c, float startX, float startY, float stopX, float stopY) {
            c.drawLine(startX, startY, stopX, stopY, bgPaint);
            c.drawLine(startX, startY, stopX, stopY, mainPaint);
        }

        private void drawCirclePro(Canvas c, float cx, float cy, float radius, boolean fill) {
            bgPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            mainPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            c.drawCircle(cx, cy, fill ? radius + 1.5f : radius, bgPaint);
            c.drawCircle(cx, cy, radius, mainPaint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            float gap = 8f; float len = 20f;

            switch (shape) {
                case 0: drawCirclePro(canvas, cx, cy, 4f, true); break;
                case 1: 
                    drawLinePro(canvas, cx - gap - len, cy, cx - gap, cy); drawLinePro(canvas, cx + gap, cy, cx + gap + len, cy);
                    drawLinePro(canvas, cx, cy - gap - len, cx, cy - gap); drawLinePro(canvas, cx, cy + gap, cx, cy + gap + len);
                    break;
                case 2: drawCirclePro(canvas, cx, cy, 14f, false); drawCirclePro(canvas, cx, cy, 3f, true); break;
                case 3: 
                    Path bgP = new Path(); bgP.moveTo(cx - 15, cy + 10); bgP.lineTo(cx, cy - 10); bgP.lineTo(cx + 15, cy + 10);
                    canvas.drawPath(bgP, bgPaint); canvas.drawPath(bgP, mainPaint);
                    drawCirclePro(canvas, cx, cy + 15, 3f, true); break;
                case 4: 
                    drawLinePro(canvas, cx - gap - 10, cy - gap - 10, cx - gap, cy - gap); drawLinePro(canvas, cx + gap + 10, cy + gap + 10, cx + gap, cy + gap);
                    drawLinePro(canvas, cx - gap - 10, cy + gap + 10, cx - gap, cy + gap); drawLinePro(canvas, cx + gap + 10, cy - gap - 10, cx + gap, cy - gap);
                    drawCirclePro(canvas, cx, cy, 3f, true); break;
                case 5: 
                    drawLinePro(canvas, cx - 15, cy - 10, cx - 15, cy + 10); drawLinePro(canvas, cx - 15, cy - 10, cx - 10, cy - 10);
                    drawLinePro(canvas, cx - 15, cy + 10, cx - 10, cy + 10); drawLinePro(canvas, cx + 15, cy - 10, cx + 15, cy + 10);
                    drawLinePro(canvas, cx + 15, cy - 10, cx + 10, cy - 10); drawLinePro(canvas, cx + 15, cy + 10, cx + 10, cy + 10);
                    drawCirclePro(canvas, cx, cy, 4f, true); break;
                case 6: 
                    drawLinePro(canvas, cx - gap - len, cy, cx - gap, cy); drawLinePro(canvas, cx + gap, cy, cx + gap + len, cy);
                    drawLinePro(canvas, cx, cy + gap, cx, cy + gap + len); drawCirclePro(canvas, cx, cy, 2f, true); break;
                case 7: drawCirclePro(canvas, cx, cy, 25f, false); drawCirclePro(canvas, cx, cy, 15f, false); drawCirclePro(canvas, cx, cy, 4f, true); break;
                case 8: 
                    drawCirclePro(canvas, cx, cy, 35f, false);
                    drawLinePro(canvas, cx - 45, cy, cx - 25, cy); drawLinePro(canvas, cx + 25, cy, cx + 45, cy);
                    drawLinePro(canvas, cx, cy - 45, cx, cy - 25); drawLinePro(canvas, cx, cy + 25, cx, cy + 45);
                    drawCirclePro(canvas, cx, cy, 3f, true); break;
                case 9: 
                    Path dP = new Path(); dP.moveTo(cx, cy - 20); dP.lineTo(cx + 20, cy); dP.lineTo(cx, cy + 20); dP.lineTo(cx - 20, cy); dP.close();
                    canvas.drawPath(dP, bgPaint); canvas.drawPath(dP, mainPaint);
                    drawCirclePro(canvas, cx, cy, 3f, true); break;
                case 10: 
                    drawLinePro(canvas, cx - 80, cy, cx + 80, cy); drawLinePro(canvas, cx, cy - 80, cx, cy + 80);
                    for(int i=1; i<=4; i++) {
                        drawCirclePro(canvas, cx + (i*15), cy, 1.5f, true); drawCirclePro(canvas, cx - (i*15), cy, 1.5f, true);
                        drawCirclePro(canvas, cx, cy + (i*15), 1.5f, true); drawCirclePro(canvas, cx, cy - (i*15), 1.5f, true);
                    }
                    break;
                case 11: 
                    Path acog = new Path(); acog.moveTo(cx, cy - 10); acog.lineTo(cx + 10, cy + 10); acog.lineTo(cx - 10, cy + 10); acog.close();
                    canvas.drawPath(acog, bgPaint); canvas.drawPath(acog, mainPaint);
                    drawLinePro(canvas, cx, cy + 15, cx, cy + 45);
                    drawLinePro(canvas, cx - 6, cy + 25, cx + 6, cy + 25); drawLinePro(canvas, cx - 10, cy + 35, cx + 10, cy + 35);
                    break;
                case 12: 
                    drawCirclePro(canvas, cx, cy, 22f, false);
                    drawLinePro(canvas, cx, cy - 22, cx, cy - 30); drawLinePro(canvas, cx, cy + 22, cx, cy + 30);
                    drawLinePro(canvas, cx - 22, cy, cx - 30, cy); drawLinePro(canvas, cx + 22, cy, cx + 30, cy);
                    drawCirclePro(canvas, cx, cy, 3f, true);
                    break;
                case 13: 
                    drawCirclePro(canvas, cx, cy, 30f, false); drawCirclePro(canvas, cx, cy, 4f, true);
                    break;

                // التحديث العملاق (المرسيدس والدائرة العملاقة)
                case 14: // 15. علامة مرسيدس العملاقة
                    drawCirclePro(canvas, cx, cy, 80f, false); // دائرة عملاقة بحجم 80
                    drawLinePro(canvas, cx, cy, cx, cy - 80); // خط للفوك
                    drawLinePro(canvas, cx, cy, cx + 69, cy + 40); // خط للزاوية اليمين الجوه
                    drawLinePro(canvas, cx, cy, cx - 69, cy + 40); // خط للزاوية اليسار الجوه
                    drawCirclePro(canvas, cx, cy, 6f, true); // نقطة السنتر كبيرة
                    break;

                case 15: // 16. دائرة القنص الكبيرة
                    drawCirclePro(canvas, cx, cy, 100f, false); // أكبر دائرة بالتطبيق
                    drawLinePro(canvas, cx - 120, cy, cx - 80, cy); // خط يسار
                    drawLinePro(canvas, cx + 80, cy, cx + 120, cy); // خط يمين
                    drawLinePro(canvas, cx, cy - 120, cx, cy - 80); // خط فوك
                    drawLinePro(canvas, cx, cy + 80, cx, cy + 120); // خط جوه
                    drawCirclePro(canvas, cx, cy, 8f, true); // سنتر ضخم
                    break;
            }
        }
    }
}
