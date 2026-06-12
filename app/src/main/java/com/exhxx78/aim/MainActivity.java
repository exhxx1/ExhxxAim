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

        // الشاشة الرئيسية
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.parseColor("#1E1E2E"));

        TextView title = new TextView(this);
        title.setText("Exhxx Aim 🎯\nأشكال الإيم الاحترافية");
        title.setTextColor(Color.parseColor("#F5C2E7"));
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 50);

        // حاوية أزرار الأشكال
        LinearLayout shapesLayout = new LinearLayout(this);
        shapesLayout.setOrientation(LinearLayout.VERTICAL);
        shapesLayout.setGravity(Gravity.CENTER);
        shapesLayout.setPadding(0, 0, 0, 80);

        String[] shapes = {"شكل 1: نقطة ليزر 🔴", "شكل 2: علامة زائد ➕", "شكل 3: دائرة مجوفة ⭕", "شكل 4: قناص احترافي 🎯"};
        for (int i = 0; i < shapes.length; i++) {
            Button btnShape = new Button(this);
            btnShape.setText(shapes[i]);
            btnShape.setTextColor(Color.WHITE);
            btnShape.setBackgroundColor(Color.parseColor("#313244"));
            btnShape.setPadding(30, 20, 30, 20);
            
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(50, 10, 50, 10);
            btnShape.setLayoutParams(lp);

            final int shapeType = i;
            btnShape.setOnClickListener(v -> {
                prefs.edit().putInt("shape", shapeType).apply();
                Toast.makeText(this, "تم اختيار " + shapes[shapeType], Toast.LENGTH_SHORT).show();
                
                // إعادة تشغيل الإيم فوراً لتطبيق الشكل الجديد إذا كان شغال
                if (isAimActive) {
                    stopService(new Intent(this, CrosshairService.class));
                    startService(new Intent(this, CrosshairService.class));
                }
            });
            shapesLayout.addView(btnShape);
        }

        // زر التشغيل والإيقاف
        btnToggle = new Button(this);
        btnToggle.setText("تشغيل الإيم 🟢");
        btnToggle.setTextSize(20);
        btnToggle.setTextColor(Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(50f);
        shape.setColor(Color.parseColor("#A6E3A1"));
        btnToggle.setBackground(shape);
        btnToggle.setPadding(60, 40, 60, 40);

        mainLayout.addView(title);
        mainLayout.addView(shapesLayout);
        mainLayout.addView(btnToggle);
        setContentView(mainLayout);

        btnToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "يرجى تفعيل إذن الظهور فوق التطبيقات!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                toggleCrosshair();
            }
        });
    }

    private void toggleCrosshair() {
        Intent serviceIntent = new Intent(this, CrosshairService.class);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(50f);

        if (!isAimActive) {
            startService(serviceIntent);
            btnToggle.setText("إيقاف الإيم 🔴");
            shape.setColor(Color.parseColor("#F38BA8"));
            btnToggle.setBackground(shape);
            isAimActive = true;
        } else {
            stopService(serviceIntent);
            btnToggle.setText("تشغيل الإيم 🟢");
            shape.setColor(Color.parseColor("#A6E3A1"));
            btnToggle.setBackground(shape);
            isAimActive = false;
        }
    }
}
