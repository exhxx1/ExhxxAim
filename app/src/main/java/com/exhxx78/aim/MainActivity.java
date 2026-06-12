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
    private boolean isFilterActive = false;
    private Button btnAimToggle, btnFilterToggle, btnMacroToggle;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.parseColor("#0F0F1A"));

        TextView title = new TextView(this);
        title.setText("Exhxx Aim Pro 🎯\n+ الماكرو السحري 🔥");
        title.setTextColor(Color.parseColor("#FFD700")); 
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 40, 0, 40);
        mainLayout.addView(title);

        ScrollView scrollView = new ScrollView(this);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f);
        scrollView.setLayoutParams(scrollParams);

        LinearLayout shapesLayout = new LinearLayout(this);
        shapesLayout.setOrientation(LinearLayout.VERTICAL);
        shapesLayout.setGravity(Gravity.CENTER);

        String[] shapes = {
            "1. نقطة الليزر 🔴", "2. كلاسيك CS:GO ➕", "3. أوفر واتش ⭕", 
            "4. إيم Apex 🔺", "5. تقاطع فالورانت ❌", "6. سيبربانك [.]", 
            "7. إيم الرشاشات T", "8. شوتكن رينك ◎", "9. سنايبر سكوب 🎯", "10. النجمة الماسية ✦"
        };

        for (int i = 0; i < shapes.length; i++) {
            Button btnShape = new Button(this);
            btnShape.setText(shapes[i]);
            btnShape.setTextColor(Color.WHITE);
            GradientDrawable btnBg = new GradientDrawable();
            btnBg.setColor(Color.parseColor("#1A1A2E"));
            btnBg.setCornerRadius(15f);
            btnBg.setStroke(2, Color.parseColor("#FFD700"));
            btnShape.setBackground(btnBg);
            btnShape.setPadding(0, 25, 0, 25);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(60, 10, 60, 10);
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

        // حاوية الأزرار
        LinearLayout bottomLayout = new LinearLayout(this);
        bottomLayout.setOrientation(LinearLayout.VERTICAL);
        bottomLayout.setPadding(0, 20, 0, 20);

        btnAimToggle = new Button(this);
        btnAimToggle.setText("تشغيل الإيم 🎯");
        btnAimToggle.setTextColor(Color.WHITE);
        btnAimToggle.setBackgroundColor(Color.parseColor("#00B050"));

        btnFilterToggle = new Button(this);
        btnFilterToggle.setText("تشغيل فلتر كشف الأعداء 🕶️");
        btnFilterToggle.setTextColor(Color.BLACK);
        btnFilterToggle.setBackgroundColor(Color.parseColor("#FFD700"));

        // زر الماكرو الجديد
        btnMacroToggle = new Button(this);
        btnMacroToggle.setText("تفعيل الماكرو (Auto-Fire) 🔫");
        btnMacroToggle.setTextColor(Color.WHITE);
        btnMacroToggle.setBackgroundColor(Color.parseColor("#D00000"));

        bottomLayout.addView(btnAimToggle);
        bottomLayout.addView(btnFilterToggle);
        bottomLayout.addView(btnMacroToggle);
        mainLayout.addView(bottomLayout);
        setContentView(mainLayout);

        btnAimToggle.setOnClickListener(v -> {
            if (!checkOverlayPermission()) return;
            Intent serviceIntent = new Intent(this, CrosshairService.class);
            if (!isAimActive) {
                startService(serviceIntent);
                btnAimToggle.setText("إيقاف الإيم 🔴");
                btnAimToggle.setBackgroundColor(Color.parseColor("#D00000"));
                isAimActive = true;
            } else {
                stopService(serviceIntent);
                btnAimToggle.setText("تشغيل الإيم 🎯");
                btnAimToggle.setBackgroundColor(Color.parseColor("#00B050"));
                isAimActive = false;
            }
        });

        btnFilterToggle.setOnClickListener(v -> {
            if (!checkOverlayPermission()) return;
            Intent filterIntent = new Intent(this, FilterService.class);
            if (!isFilterActive) {
                startService(filterIntent);
                btnFilterToggle.setText("إيقاف الفلتر 🔴");
                btnFilterToggle.setBackgroundColor(Color.parseColor("#D00000"));
                btnFilterToggle.setTextColor(Color.WHITE);
                isFilterActive = true;
            } else {
                stopService(filterIntent);
                btnFilterToggle.setText("تشغيل فلتر كشف الأعداء 🕶️");
                btnFilterToggle.setBackgroundColor(Color.parseColor("#FFD700"));
                btnFilterToggle.setTextColor(Color.BLACK);
                isFilterActive = false;
            }
        });

        btnMacroToggle.setOnClickListener(v -> {
            Toast.makeText(this, "ابحث عن (Exhxx Aim 🎯) وقم بتفعيله من الإعدادات", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
    }

    private boolean checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "فعل إذن الظهور فوق التطبيقات!", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            return false;
        }
        return true;
    }
}
