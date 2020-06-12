package com.example.carl.ui_loadingview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class LoadingView extends View {
    private long ROTATION_ANIMATION_TIME = 2000;
    private long SPLASH_ANIMATION_TIME = 1200;
    private float mCurrentRotationAngle = 0F;
    private int[] mCircleColors;
    //大圆半径
    private float mRotationRadius;
    private float mCurrentRotationRadius = mRotationRadius;
    //小圆半径
    private float mCircleRadius;
    private Paint mPaint;
    private int mCenterX, mCenterY;
    private int mSplashColor = Color.WHITE;

    private LoadingState mLoadingState;

    //空心圆初始半径
    private float mHoleRadius = 0;
    //屏幕对角线的一半
    private float mDiagonalDist;

    public LoadingView(Context context) {
        this(context, null);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mCircleColors = getContext().getResources().getIntArray(R.array.splash_circle_colors);
    }

    private boolean mInitParams = false;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!mInitParams) {
            initParams();
        }
        if (mLoadingState == null) {
            mLoadingState = new RotationState();
        }
        mLoadingState.draw(canvas);

        //drawRotationAnimator(canvas);
    }

    private void initParams() {
        mRotationRadius = getMeasuredWidth() / 4;
        mCircleRadius = mRotationRadius / 8;
        mPaint = new Paint();
        mPaint.setDither(true);
        mPaint.setAntiAlias(true);
        mCenterX = getMeasuredWidth() / 2;
        mCenterY = getMeasuredHeight() / 2;
        mDiagonalDist = (float) Math.sqrt(Math.pow(mCenterX,2)+Math.pow(mCenterY,2));
        mInitParams = true;
    }


    /**
     * 消失
     */
    public void disappear() {
        //关闭旋转动画
        if (mLoadingState instanceof RotationState) {
            RotationState rotationState = (RotationState) mLoadingState;
            rotationState.cancel();
        }
        //开启聚合动画
        mLoadingState = new MergeState();
    }

    public abstract class LoadingState {
        public abstract void draw(Canvas canvas);
    }

    /**
     * 旋转动画
     */
    public class RotationState extends LoadingState {
        private ValueAnimator mAnimator;

        public RotationState() {
            //属性动画 0~360
            mAnimator = ObjectAnimator.ofFloat(0f, 2 * (float) Math.PI);
            mAnimator.setDuration(ROTATION_ANIMATION_TIME);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentRotationAngle = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new LinearInterpolator());
            //让动画不断反复
            mAnimator.setRepeatCount(-1);
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            //画个背景
            canvas.drawColor(mSplashColor);
            //画六个圆
            double percentAngle = Math.PI * 2 / mCircleColors.length;
            for (int i = 0; i < mCircleColors.length; i++) {
                mPaint.setColor(mCircleColors[i]);
                double currentAngle = percentAngle * i + mCurrentRotationAngle;
                //每个小圆的圆心
                int cx = (int) (mCenterX + mRotationRadius * Math.cos(currentAngle));
                int cy = (int) (mCenterY + mRotationRadius * Math.sin(currentAngle));
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
            }
        }

        /**
         * 取消动画
         */
        public void cancel() {
            mAnimator.cancel();
        }
    }

    /**
     * 聚合动画
     */
    public class MergeState extends LoadingState {
        private ValueAnimator mAnimator;

        public MergeState() {
            mAnimator = ObjectAnimator.ofFloat(mRotationRadius, 0);
            mAnimator.setDuration(ROTATION_ANIMATION_TIME / 2);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mCurrentRotationRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.setInterpolator(new AnticipateInterpolator(3f));
            //聚合完毕后展开
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoadingState = new ExpendState();

                }
            });
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            //画个背景
            canvas.drawColor(mSplashColor);
            //画六个圆
            double percentAngle = Math.PI * 2 / mCircleColors.length;
            for (int i = 0; i < mCircleColors.length; i++) {
                mPaint.setColor(mCircleColors[i]);
                double currentAngle = percentAngle * i + mCurrentRotationAngle;
                //每个小圆的圆心
                int cx = (int) (mCenterX + mCurrentRotationRadius * Math.cos(currentAngle));
                int cy = (int) (mCenterY + mCurrentRotationRadius * Math.sin(currentAngle));
                canvas.drawCircle(cx, cy, mCircleRadius, mPaint);
            }
        }
    }

    /**
     * 展开动画
     */
    public class ExpendState extends LoadingState {
        private ValueAnimator mAnimator;
        public ExpendState() {
            mAnimator = ObjectAnimator.ofFloat(0, mDiagonalDist);
            mAnimator.setDuration(ROTATION_ANIMATION_TIME / 2);
            mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mHoleRadius = (float) animation.getAnimatedValue();
                    invalidate();
                }
            });
            mAnimator.start();
        }

        @Override
        public void draw(Canvas canvas) {
            float strokeWidth = mDiagonalDist - mHoleRadius;
            mPaint.setStrokeWidth(strokeWidth);
            mPaint.setColor(mSplashColor);
            mPaint.setStyle(Paint.Style.STROKE);
            float radius = strokeWidth/2+mHoleRadius;
            canvas.drawCircle(mCenterX,mCenterY,radius,mPaint);
        }
    }

}
