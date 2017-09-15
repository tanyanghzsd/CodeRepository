package com.paoword.tanyang.paowordtest.widgets;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import com.paoword.tanyang.paowordtest.Utils.BitmapUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by tanyang on 2017/9/15.
 */

public class PictureWaveAnimationView extends View {

    public Paint paint;

    private List<Bitmap> bitmapList;

    private List<Point> coordinateList;

    private List<Animator> animatorList;

    private AnimatorSet animatorSet;

    private int maxBitmapHeight;
    private int totalBitmapWidth;

    private boolean isRunning = false;
    private boolean isSizeOK = false;

    public PictureWaveAnimationView(Context context) {
        super(context);
    }

    public PictureWaveAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PictureWaveAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PictureWaveAnimationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        if (!isRunning || h != maxBitmapHeight / 2 * 3) {
            super.onSizeChanged(w, h, oldw, oldh);
            return;
        }

        // 计算坐标
        coordinateList = new ArrayList<>();
        Point point;
        int offsetX = (w - totalBitmapWidth) / 2;
        for (int i = 0; i < bitmapList.size(); i++) {
            int x = offsetX + (i == 0 ? 0 : bitmapList.get(i - 1).getWidth());
            int y = h - bitmapList.get(i).getHeight();
            point = new Point(x, y);
            coordinateList.add(point);
            offsetX = x;
        }

        // 修改动画的值
        ValueAnimator animator;
        Point coordinate;
        for (int i = 0; i < animatorList.size(); i++) {
            animator = (ValueAnimator) animatorList.get(i);
            coordinate = coordinateList.get(i);
            animator.setFloatValues(coordinate.y, coordinate.y - maxBitmapHeight / 3, coordinate.y);
        }

        if (animatorSet != null) {
            // 取消之前的动画
            animatorSet.cancel();
        }
        // 执行动画
        animatorSet = new AnimatorSet();
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(animatorList);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (animatorSet != null) {
                    animatorSet.start(); // 再次执行
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        animatorSet.start();

        isSizeOK = true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!isSizeOK) {
            super.onDraw(canvas);
            return;
        }
        if (!isRunning) {
            // 绘制静态图
            for (int i = 0; i < bitmapList.size(); i++) {
                canvas.drawBitmap(bitmapList.get(i), coordinateList.get(i).x, coordinateList.get(i).y, paint);
            }
            recycleBitmap();
            return;
        }
        // 绘制动画
        for (int i = 0; i < bitmapList.size(); i++) {
            Float y = (Float) ((ValueAnimator) animatorList.get(i)).getAnimatedValue();
            if (y == null) continue;
            canvas.drawBitmap(bitmapList.get(i), coordinateList.get(i).x, y == 0 ? coordinateList.get(i).y : y, paint);
        }
        invalidate();
    }

    private void recycleBitmap() {
        for (Bitmap bitmap : bitmapList) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        }
        bitmapList.clear();
    }

    /**
     * 显示
     */
    public void startAnimation(@DrawableRes int... drawableResArray) {

        paint = new Paint();
        paint.setAntiAlias(true);

        long duration = 500;
        long delay = duration / 2;

        bitmapList = new ArrayList<>();
        animatorList = new ArrayList<>();
        Bitmap bitmap;
        for (int i = 0; i < drawableResArray.length; i++) {
            bitmap = decodeResourceByStream(getContext(), drawableResArray[i]);
            bitmapList.add(bitmap);
            animatorList.add(createValueAnim(duration, delay * i));
            totalBitmapWidth += bitmap.getWidth();
            if (bitmap.getHeight() > maxBitmapHeight) {
                maxBitmapHeight = bitmap.getHeight();
            }
        }

        if (getLayoutParams().width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            getLayoutParams().width = totalBitmapWidth;
        }
        getLayoutParams().height = maxBitmapHeight / 2 * 3;
        requestLayout();

        isRunning = true;
    }

    /**
     * 结束动画
     */
    public void endAnimation() {
        ((Activity) getContext()).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (animatorSet != null) {
                    if (animatorSet.isRunning()) {
                        animatorSet.cancel();
                    }
                    animatorSet = null;
                }
                isRunning = false;
            }
        });
    }

    /**
     * 创建动画
     *
     * @param duration   动画执行时间
     * @param startDelay 开始延时
     */
    private ValueAnimator createValueAnim(long duration, long startDelay) {
        ValueAnimator animator = new ValueAnimator();
        animator.setDuration(duration);
        animator.setStartDelay(startDelay);
        return animator;
    }

    /**
     * 通过流解析图片
     *
     * @param context Context
     * @param resId   资源ID
     */
    public static Bitmap decodeResourceByStream(Context context, @DrawableRes @RawRes int resId) {
        InputStream inputStream = context.getApplicationContext().getResources().openRawResource(resId);
        return BitmapFactory.decodeStream(inputStream);
    }
}
