package com.exhxx78.aim;

import android.app.Activity;
import android.content.Intent;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.parseColor("#1E1E2E"));

        TextView title = new TextView(this);
        title.setText("Exhxx Aim 🎯\nالمطور: محمد عدنان");
        title.setTextColor(Color.parseColor("#F5C2E7"));
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 100);

        btnToggle = new Button(this);
        btnToggle.setText("تشغيل الإيم 🟢");
        btnToggle.setTextSize(20);
        btnToggle.setTextColor(Color.WHITE);
        
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(50f);
        shape.setColor(Color.parseColor("#A6E3A1"));
        btnToggle.setBackground(shape);
        btnToggle.setPadding(60, 40, 60, 40);

        layout.addView(title);
        layout.addView(btnToggle);
        setContentView(layout);

        btnToggle.setOnClickListener(v -> {
            // التحقق من إذن الظهور فوق التطبيقات
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "يرجى تفعيل إذن الظهور فوق التطبيقات أولاً!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivity(intent);
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
