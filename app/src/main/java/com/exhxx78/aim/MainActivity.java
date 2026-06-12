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
        mainLayout.setBackgroundColor(Color.parseColor("#0F0F1A")); // لون داكن احترافي

        TextView title = new TextView(this);
        title.setText("EXHXX AIM PRO\nESPORTS EDITION");
        title.setTextColor(Color.parseColor("#00E5FF")); // أزرق نيون
        title.setTextSize(26);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 80);
        mainLayout.addView(title);

        TextView info = new TextView(this);
        info.setText("تم تفعيل القائمة العائمة الاحترافية.\nاضغط على أيقونة [ ≡ ] داخل اللعبة لتغيير السكوب أو إيقاف التطبيق.");
        info.setTextColor(Color.parseColor("#A0A0A0"));
        info.setTextSize(14);
        info.setGravity(Gravity.CENTER);
        info.setPadding(40, 0, 40, 80);
        mainLayout.addView(info);

        // زر التشغيل الرئيسي
        btnAimToggle = new Button(this);
        btnAimToggle.setText("START AIM");
        btnAimToggle.setTextSize(20);
        btnAimToggle.setTextColor(Color.WHITE);
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(15f);
        shape.setColor(Color.parseColor("#00C853"));
        btnAimToggle.setBackground(shape);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            400, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnParams.setMargins(0, 0, 0, 40);
        btnAimToggle.setLayoutParams(btnParams);
        mainLayout.addView(btnAimToggle);

        // زر الإطفاء الشامل والخروج من التطبيق
        Button btnKill = new Button(this);
        btnKill.setText("EXIT APP (إغلاق كامل)");
        btnKill.setTextSize(16);
        btnKill.setTextColor(Color.WHITE);
        GradientDrawable killShape = new GradientDrawable();
        killShape.setCornerRadius(15f);
        killShape.setColor(Color.parseColor("#D50000"));
        btnKill.setBackground(killShape);
        btnKill.setLayoutParams(btnParams);
        mainLayout.addView(btnKill);

        setContentView(mainLayout);

        btnAimToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "يرجى تفعيل الصلاحية أولاً", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                Intent serviceIntent = new Intent(this, CrosshairService.class);
                if (!isAimActive) {
                    startService(serviceIntent);
                    btnAimToggle.setText("STOP AIM");
                    shape.setColor(Color.parseColor("#FF6D00")); // برتقالي تحذيري
                    btnAimToggle.setBackground(shape);
                    isAimActive = true;
                } else {
                    stopService(serviceIntent);
                    btnAimToggle.setText("START AIM");
                    shape.setColor(Color.parseColor("#00C853"));
                    btnAimToggle.setBackground(shape);
                    isAimActive = false;
                }
            }
        });

        // كود الإغلاق الشامل (يمسح التطبيق من الرام)
        btnKill.setOnClickListener(v -> {
            stopService(new Intent(this, CrosshairService.class));
            finishAffinity(); // إغلاق التطبيق نهائياً
            System.exit(0);
        });
    }
}
