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
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

public class CrosshairService extends Service {
    private WindowManager windowManager;
    private DrawView crosshairView;

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        SharedPreferences prefs = getSharedPreferences("AimPrefs", MODE_PRIVATE);
        int shapeType = prefs.getInt("shape", 0); 

        crosshairView = new DrawView(this, shapeType);

        int layoutFlag = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O 
            ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
            : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                150, 150, // كبرنا المربع حتى يكفي الأشكال الاحترافية
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | 
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;
        windowManager.addView(crosshairView, params);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (crosshairView != null) windowManager.removeView(crosshairView);
    }

    private class DrawView extends View {
        private Paint mainPaint, bgPaint;
        private int shape;

        public DrawView(Context context, int shapeType) {
            super(context);
            this.shape = shapeType;
            
            // لون الإيم الأساسي (أخضر نيون للرؤية الليلية والنهارية)
            mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mainPaint.setColor(Color.parseColor("#39FF14")); 
            mainPaint.setStyle(Paint.Style.STROKE);
            mainPaint.setStrokeWidth(4f);
            mainPaint.setStrokeCap(Paint.Cap.ROUND);

            // الظل الأسود (حتى ما يختفي الإيم بالأماكن البيضاء)
            bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bgPaint.setColor(Color.BLACK);
            bgPaint.setStyle(Paint.Style.STROKE);
            bgPaint.setStrokeWidth(8f); // أعرض من الرئيسي ليكون كخلفية
            bgPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        private void drawLinePro(Canvas c, float startX, float startY, float stopX, float stopY) {
            c.drawLine(startX, startY, stopX, stopY, bgPaint); // رسم الظل أولاً
            c.drawLine(startX, startY, stopX, stopY, mainPaint); // رسم اللون فوقه
        }

        private void drawCirclePro(Canvas c, float cx, float cy, float radius, boolean fill) {
            bgPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            mainPaint.setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
            c.drawCircle(cx, cy, fill ? radius + 2 : radius, bgPaint);
            c.drawCircle(cx, cy, radius, mainPaint);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int cx = getWidth() / 2;
            int cy = getHeight() / 2;
            float gap = 8f; // الفراغ بين السنتر والخطوط
            float len = 20f; // طول الخطوط

            switch (shape) {
                case 0: // 1. Dot
                    drawCirclePro(canvas, cx, cy, 5f, true);
                    break;
                case 1: // 2. CS:GO Cross
                    drawLinePro(canvas, cx - gap - len, cy, cx - gap, cy);
                    drawLinePro(canvas, cx + gap, cy, cx + gap + len, cy);
                    drawLinePro(canvas, cx, cy - gap - len, cx, cy - gap);
                    drawLinePro(canvas, cx, cy + gap, cx, cy + gap + len);
                    break;
                case 2: // 3. Overwatch Hollow
                    drawCirclePro(canvas, cx, cy, 14f, false);
                    drawCirclePro(canvas, cx, cy, 3f, true);
                    break;
                case 3: // 4. Apex Chevron (سهم لفوق)
                    Path bgPath = new Path(); bgPath.moveTo(cx - 15, cy + 10); bgPath.lineTo(cx, cy - 10); bgPath.lineTo(cx + 15, cy + 10);
                    canvas.drawPath(bgPath, bgPaint);
                    canvas.drawPath(bgPath, mainPaint);
                    drawCirclePro(canvas, cx, cy + 15, 3f, true);
                    break;
                case 4: // 5. Valorant X-Cross
                    drawLinePro(canvas, cx - gap - 10, cy - gap - 10, cx - gap, cy - gap);
                    drawLinePro(canvas, cx + gap + 10, cy + gap + 10, cx + gap, cy + gap);
                    drawLinePro(canvas, cx - gap - 10, cy + gap + 10, cx - gap, cy + gap);
                    drawLinePro(canvas, cx + gap + 10, cy - gap - 10, cx + gap, cy - gap);
                    drawCirclePro(canvas, cx, cy, 3f, true);
                    break;
                case 5: // 6. Cyberpunk [ . ]
                    drawLinePro(canvas, cx - 15, cy - 10, cx - 15, cy + 10);
                    drawLinePro(canvas, cx - 15, cy - 10, cx - 10, cy - 10);
                    drawLinePro(canvas, cx - 15, cy + 10, cx - 10, cy + 10);
                    drawLinePro(canvas, cx + 15, cy - 10, cx + 15, cy + 10);
                    drawLinePro(canvas, cx + 15, cy - 10, cx + 10, cy - 10);
                    drawLinePro(canvas, cx + 15, cy + 10, cx + 10, cy + 10);
                    drawCirclePro(canvas, cx, cy, 4f, true);
                    break;
                case 6: // 7. T-Shape (للتحكم بالارتداد)
                    drawLinePro(canvas, cx - gap - len, cy, cx - gap, cy);
                    drawLinePro(canvas, cx + gap, cy, cx + gap + len, cy);
                    drawLinePro(canvas, cx, cy + gap, cx, cy + gap + len);
                    drawCirclePro(canvas, cx, cy, 2f, true);
                    break;
                case 7: // 8. Shotgun Ring
                    drawCirclePro(canvas, cx, cy, 25f, false);
                    drawCirclePro(canvas, cx, cy, 15f, false);
                    drawCirclePro(canvas, cx, cy, 4f, true);
                    break;
                case 8: // 9. Sniper Pro
                    drawCirclePro(canvas, cx, cy, 35f, false);
                    drawLinePro(canvas, cx - 45, cy, cx - 25, cy);
                    drawLinePro(canvas, cx + 25, cy, cx + 45, cy);
                    drawLinePro(canvas, cx, cy - 45, cx, cy - 25);
                    drawLinePro(canvas, cx, cy + 25, cx, cy + 45);
                    drawCirclePro(canvas, cx, cy, 3f, true);
                    break;
                case 9: // 10. Diamond Star
                    Path dPath = new Path(); dPath.moveTo(cx, cy - 20); dPath.lineTo(cx + 20, cy); dPath.lineTo(cx, cy + 20); dPath.lineTo(cx - 20, cy); dPath.close();
                    canvas.drawPath(dPath, bgPaint);
                    canvas.drawPath(dPath, mainPaint);
                    drawCirclePro(canvas, cx, cy, 3f, true);
                    break;
            }
        }
    }
}
