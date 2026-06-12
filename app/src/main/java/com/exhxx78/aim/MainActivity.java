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
    private Button btnAimToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER);
        mainLayout.setBackgroundColor(Color.parseColor("#0F0F1A"));

        TextView title = new TextView(this);
        title.setText("Exhxx Aim Pro 🎯\nUltimate Crosshair");
        title.setTextColor(Color.parseColor("#00FFFF")); 
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 100);
        mainLayout.addView(title);

        TextView info = new TextView(this);
        info.setText("تم تفعيل ميزة (القائمة العائمة). يمكنك تغيير السكوب من داخل اللعبة عبر زر (⚙️).");
        info.setTextColor(Color.LTGRAY);
        info.setTextSize(16);
        info.setGravity(Gravity.CENTER);
        info.setPadding(40, 0, 40, 100);
        mainLayout.addView(info);

        btnAimToggle = new Button(this);
        btnAimToggle.setText("تشغيل الإيم 🎯");
        btnAimToggle.setTextSize(22);
        btnAimToggle.setTextColor(Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(20f);
        shape.setColor(Color.parseColor("#00B050"));
        btnAimToggle.setBackground(shape);
        btnAimToggle.setPadding(60, 40, 60, 40);

        mainLayout.addView(btnAimToggle);
        setContentView(mainLayout);

        btnAimToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "فعل إذن الظهور فوق التطبيقات!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                Intent serviceIntent = new Intent(this, CrosshairService.class);
                if (!isAimActive) {
                    startService(serviceIntent);
                    btnAimToggle.setText("إيقاف الإيم 🔴");
                    shape.setColor(Color.parseColor("#D00000"));
                    btnAimToggle.setBackground(shape);
                    isAimActive = true;
                } else {
                    stopService(serviceIntent);
                    btnAimToggle.setText("تشغيل الإيم 🎯");
                    shape.setColor(Color.parseColor("#00B050"));
                    btnAimToggle.setBackground(shape);
                    isAimActive = false;
                }
            }
        });
    }
}
