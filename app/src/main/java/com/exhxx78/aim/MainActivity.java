package com.exhxx78.aim;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
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
    private Button btnAimToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // الحاوية الأساسية (للتمرير)
        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#0F0F1A"));

        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        mainLayout.setPadding(40, 60, 40, 60);

        // 1. عنوان التطبيق
        TextView title = new TextView(this);
        title.setText("EXHXX78 AIM PRO");
        title.setTextColor(Color.parseColor("#00E5FF")); // سماوي نيون
        title.setTextSize(32);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        mainLayout.addView(title);

        TextView subTitle = new TextView(this);
        subTitle.setText("ESPORTS EDITION 🎯");
        subTitle.setTextColor(Color.parseColor("#FFD700")); // ذهبي
        subTitle.setTextSize(18);
        subTitle.setGravity(Gravity.CENTER);
        subTitle.setPadding(0, 0, 0, 40);
        mainLayout.addView(subTitle);

        // 2. بطاقة التعليمات (Tutorial Card)
        LinearLayout infoCard = new LinearLayout(this);
        infoCard.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#1A1A2E"));
        cardBg.setCornerRadius(20f);
        cardBg.setStroke(3, Color.parseColor("#313244"));
        infoCard.setBackground(cardBg);
        infoCard.setPadding(40, 40, 40, 40);
        
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 60);
        infoCard.setLayoutParams(cardParams);

        TextView infoTitle = new TextView(this);
        infoTitle.setText("📌 طريقة الاستخدام:");
        infoTitle.setTextColor(Color.WHITE);
        infoTitle.setTextSize(18);
        infoTitle.setTypeface(null, Typeface.BOLD);
        infoTitle.setPadding(0, 0, 0, 20);
        infoCard.addView(infoTitle);

        TextView infoText = new TextView(this);
        infoText.setText("1. اضغط على زر (START AIM) بالأسفل لتشغيل الإيم.\n" +
                         "2. امنح التطبيق صلاحية الظهور فوق التطبيقات إذا طلب منك ذلك.\n" +
                         "3. افتح لعبتك المفضلة (مثل CODM أو PUBG).\n" +
                         "4. ستجد أيقونة ( ≡ ) عائمة في اللعبة، اضغط عليها لاختيار السكوب المناسب لك من بين أكثر من 100 شكل عملاق!\n" +
                         "5. لإيقاف التطبيق، اضغط على زر الإغلاق الأحمر.");
        infoText.setTextColor(Color.parseColor("#B0B0B0"));
        infoText.setTextSize(15);
        infoText.setLineSpacing(10f, 1f);
        infoCard.addView(infoText);
        
        mainLayout.addView(infoCard);

        // 3. أزرار التحكم الرئيسية
        btnAimToggle = new Button(this);
        btnAimToggle.setText("START AIM (تشغيل)");
        btnAimToggle.setTextSize(18);
        btnAimToggle.setTypeface(null, Typeface.BOLD);
        btnAimToggle.setTextColor(Color.WHITE);
        GradientDrawable shapeStart = new GradientDrawable();
        shapeStart.setCornerRadius(15f);
        shapeStart.setColor(Color.parseColor("#00C853"));
        btnAimToggle.setBackground(shapeStart);
        
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 150);
        btnParams.setMargins(0, 0, 0, 30);
        btnAimToggle.setLayoutParams(btnParams);
        mainLayout.addView(btnAimToggle);

        Button btnKill = new Button(this);
        btnKill.setText("EXIT APP (إغلاق التطبيق)");
        btnKill.setTextSize(18);
        btnKill.setTypeface(null, Typeface.BOLD);
        btnKill.setTextColor(Color.WHITE);
        GradientDrawable shapeKill = new GradientDrawable();
        shapeKill.setCornerRadius(15f);
        shapeKill.setColor(Color.parseColor("#D50000"));
        btnKill.setBackground(shapeKill);
        btnKill.setLayoutParams(btnParams);
        mainLayout.addView(btnKill);

        // 4. زر قناة التيليجرام النابض
        Button btnTelegram = new Button(this);
        btnTelegram.setText("📢 انضم لقناتنا على تليجرام");
        btnTelegram.setTextSize(16);
        btnTelegram.setTypeface(null, Typeface.BOLD);
        btnTelegram.setTextColor(Color.WHITE);
        GradientDrawable shapeTele = new GradientDrawable();
        shapeTele.setCornerRadius(15f);
        shapeTele.setColor(Color.parseColor("#0088CC")); // لون تيليجرام الرسمي
        btnTelegram.setBackground(shapeTele);
        
        LinearLayout.LayoutParams teleParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 140);
        teleParams.setMargins(0, 40, 0, 60);
        btnTelegram.setLayoutParams(teleParams);
        mainLayout.addView(btnTelegram);

        // إضافة الأنيميشن (نبض الزر)
        ObjectAnimator scaleDown = ObjectAnimator.ofPropertyValuesHolder(
                btnTelegram,
                PropertyValuesHolder.ofFloat("scaleX", 1.0f, 1.05f),
                PropertyValuesHolder.ofFloat("scaleY", 1.0f, 1.05f)
        );
        scaleDown.setDuration(800);
        scaleDown.setRepeatCount(ValueAnimator.INFINITE);
        scaleDown.setRepeatMode(ValueAnimator.REVERSE);
        scaleDown.start();

        // 5. حقوق المطور
        TextView developerText = new TextView(this);
        developerText.setText("Developed by:\nMuhammad Adnan (@m_7004)");
        developerText.setTextColor(Color.parseColor("#808080"));
        developerText.setTextSize(14);
        developerText.setGravity(Gravity.CENTER);
        mainLayout.addView(developerText);

        scrollView.addView(mainLayout);
        setContentView(scrollView);

        // --- الأوامر البرمجية للأزرار ---

        btnAimToggle.setOnClickListener(v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "يرجى تفعيل صلاحية الظهور فوق التطبيقات", Toast.LENGTH_LONG).show();
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
            } else {
                Intent serviceIntent = new Intent(this, CrosshairService.class);
                if (!isAimActive) {
                    startService(serviceIntent);
                    btnAimToggle.setText("STOP AIM (إيقاف)");
                    shapeStart.setColor(Color.parseColor("#FF6D00")); 
                    btnAimToggle.setBackground(shapeStart);
                    isAimActive = true;
                } else {
                    stopService(serviceIntent);
                    btnAimToggle.setText("START AIM (تشغيل)");
                    shapeStart.setColor(Color.parseColor("#00C853"));
                    btnAimToggle.setBackground(shapeStart);
                    isAimActive = false;
                }
            }
        });

        btnKill.setOnClickListener(v -> {
            stopService(new Intent(this, CrosshairService.class));
            finishAffinity();
            System.exit(0);
        });

        btnTelegram.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/exhxx78"));
            startActivity(browserIntent);
        });
    }
}
