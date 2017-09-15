package com.paoword.tanyang.paowordtest.widgets;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import static android.animation.ValueAnimator.INFINITE;

/**
 * Created by tanyang on 2017/9/15.
 */

public class RadarScanView extends View {

    // 背景放射型渐变配置
    private final int CENTER_COLOR = Color.argb(5, 255, 233, 200);
    private final int EDGE_COLOR = Color.argb(5, 255, 233, 45);
    // 动画清扫型渐变配置
    private final int FIRST_COLOR = Color.argb(0, 255, 233, 45);
    private final int SECOND_COLOR = Color.argb(5, 255, 233, 45);
    private final int THIRD_COLOR = Color.argb(90, 255, 233, 45);
    private final int[] COLORS = new int[]{FIRST_COLOR, FIRST_COLOR, SECOND_COLOR, THIRD_COLOR, FIRST_COLOR, FIRST_COLOR};
    private final float[] POSITIONS = new float[]{0f, 0.6f, 0.65f, 0.75f, 0.751f, 1f};

    private Paint paint;

    private int radius;

    private AnimatorSet animatorSet;
    private ObjectAnimator rotation;

    private RadialGradient radialGradient;
    private SweepGradient sweepGradient;

    public RadarScanView(Context context) {
        super(context);
    }

    public RadarScanView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public RadarScanView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w <= 0 || h <= 0) return;

        radius = w > h ? h / 2 : w / 2;

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);

        radialGradient = new RadialGradient(w / 2, h / 2, radius, CENTER_COLOR, EDGE_COLOR, Shader.TileMode.CLAMP);
        sweepGradient = new SweepGradient(w / 2, h / 2, COLORS, POSITIONS);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint != null) {
            // 绘制背景
            paint.setShader(radialGradient);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
            // 绘制清扫渐变圆
            paint.setShader(sweepGradient);
            canvas.drawCircle(getWidth() / 2, getHeight() / 2, radius, paint);
        }
    }

    /**
     * 开始动画
     */
    public void start() {

        // 缩放动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0f, 1f).setDuration(500);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0f, 1f).setDuration(500);
        // 旋转动画
        rotation = ObjectAnimator.ofFloat(this, "rotation", 0, 360);
        rotation.setDuration(2000);
        rotation.setRepeatCount(INFINITE);

        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new LinearInterpolator());
        // 同时开始两个缩放动画，缩放结束后启动旋转动画
        animatorSet.play(scaleX).with(scaleY).before(rotation);
        animatorSet.start();
    }

    /**
     * 停止动画
     */
    public void stop() {
        if (animatorSet != null) {
            animatorSet.cancel();
        }
    }

}
