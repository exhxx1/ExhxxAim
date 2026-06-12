package com.exhxx78.aim;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private boolean isAimActive = false;
    private Button btnToggle;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);

        // الحاوية الرئيسية
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#0F0F1A"));

        // العنوان
        TextView title = new TextView(this);
        title.setText("Exhxx Aim Pro 🎯\nالمطور: محمد عدنان");
        title.setTextColor(Color.parseColor("#00FFFF")); // لون سيبربانك
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 40);
        mainLayout.addView(title);

        // ميزة التمرير (Scroll) لأن الأشكال صارت هواية
        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollParams);

        LinearLayout shapesLayout = new LinearLayout(this);
        shapesLayout.setOrientation(LinearLayout.VERTICAL);
        shapesLayout.setGravity(Gravity.CENTER);

        // قائمة بـ 10 أشكال عالمية
        String[] shapes = {
            "1. نقطة الليزر الاحترافية (Dot)", 
            "2. كلاسيك CS:GO (Cross)", 
            "3. أوفر واتش (Hollow Circle)", 
            "4. إيم Apex Legends (Chevron)", 
            "5. تقاطع فالورانت (X-Cross)", 
            "6. سيبربانك (Brackets) [ . ]", 
            "7. إيم الرشاشات T-Shape", 
            "8. شوتكن رينك (Double Circle)", 
            "9. سنايبر سكوب (Sniper Pro)", 
            "10. النجمة الماسية (Diamond)"
        };

        for (int i = 0; i < shapes.length; i++) {
            Button btnShape = new Button(this);
            btnShape.setText(shapes[i]);
            btnShape.setTextColor(Color.WHITE);
            
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setColor(Color.parseColor("#1A1A2E"));
            btnBg.setCornerRadius(20f);
            btnBg.setStroke(2, Color.parseColor("#00FFFF"));
            btnShape.setBackground(btnBg);
            btnShape.setPadding(0, 30, 0, 30);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(60, 15, 60, 15);
            btnShape.setLayoutParams(lp);

            final int shapeType = i;
            btnShape.setOnClickListener(v -> {
                prefs.edit().putInt("shape", shapeType).apply();
                Toast.makeText(this, "تم تفعيل: " + shapes[shapeType], Toast.LENGTH_SHORT).show();
                if (isAimActive) {
                    stopService(new Intent(this, CrosshairService.class));
                    startService(new Intent(this, CrosshairService.class));
                }
            });
            shapesLayout.addView(btnShape);
        }
        
        scrollView.addView(shapesLayout);
        mainLayout.addView(scrollView);

        // زر التشغيل أسفل الشاشة
        btnToggle = new Button(this);
        btnToggle.setText("تشغيل الإيم 🟢");
        btnToggle.setTextSize(22);
        btnToggle.setTextColor(Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(0f);
        shape.setColor(Color.parseColor("#00B050"));
        btnToggle.setBackground(shape);
        btnToggle.setPadding(0, 50, 0, 50);

        mainLayout.addView(btnToggle);
        setContentView(mainLayout);

        btnToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "فعل إذن الظهور فوق التطبيقات!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                toggleCrosshair();
            }
        });
    }

    private void toggleCrosshair() {
        Intent serviceIntent = new Intent(this, CrosshairService.class);
        if (!isAimActive) {
            startService(serviceIntent);
            btnToggle.setText("إيقاف الإيم 🔴");
            btnToggle.setBackgroundColor(Color.parseColor("#D00000"));
            isAimActive = true;
        } else {
            stopService(serviceIntent);
            btnToggle.setText("تشغيل الإيم 🟢");
            btnToggle.setBackgroundColor(Color.parseColor("#00B050"));
            isAimActive = false;
        }
    }
}
