/*
 * This is the source code of DMPLayer for Android v. 1.0.0.
 * You should have received a copy of the license in this archive (see LICENSE).
 * Copyright @Dibakar_Mistry, 2015.
 */
package com.dmplayer.uicomponent;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.dmplayer.R;

public class PlayPauseView extends FrameLayout {

    private static final Property<PlayPauseView, Integer> COLOR = new Property<PlayPauseView, Integer>(Integer.class, "color") {
        @Override
        public Integer get(PlayPauseView v) {
            return v.getColor();
        }

        @Override
        public void set(PlayPauseView v, Integer value) {
            v.setColor(value);
        }
    };

    private static final long PLAY_PAUSE_ANIMATION_DURATION = 200;

    private final PlayPauseDrawable mDrawable;
    private final Paint mPaint = new Paint();
    private final int mPauseBackgroundColor;
    private final int mPlayBackgroundColor;
    public boolean isDrawCircle = true;

    private AnimatorSet mAnimatorSet;
    private int mBackgroundColor;
    private int mWidth;
    private int mHeight;

    public PlayPauseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);

        TypedValue colorTheme = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.colorAccent, colorTheme, true);

        mBackgroundColor = colorTheme.data;
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mDrawable = new PlayPauseDrawable(context);
        mDrawable.setCallback(this);

        mPauseBackgroundColor = colorTheme.data;
        mPlayBackgroundColor = colorTheme.data;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PlayPause);
        isDrawCircle = a.getBoolean(R.styleable.PlayPause_isCircleDraw, isDrawCircle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        // final int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        // setMeasuredDimension(size, size);
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mDrawable.setBounds(0, 0, w, h);
        mWidth = w;
        mHeight = h;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new ViewOutlineProvider() {
                @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            setClipToOutline(true);
        }
    }

    private void setColor(int color) {
        mBackgroundColor = color;
        invalidate();
    }

    private int getColor() {
        return mBackgroundColor;
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        return who == mDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setColor(mBackgroundColor);
        final float radius = Math.min(mWidth, mHeight) / 2f;
        if (isDrawCircle) {
            canvas.drawCircle(mWidth / 2f, mHeight / 2f, radius, mPaint);
        }
        mDrawable.draw(canvas);
    }

    private boolean mIsPlay;

    public boolean isPlay() {
        return mIsPlay;
    }

    // public void toggle() {
    // if (mAnimatorSet != null) {
    // mAnimatorSet.cancel();
    // }
    //
    // mAnimatorSet = new AnimatorSet();
    // final boolean isPlay = mDrawable.isPlay();
    // final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, isPlay
    // ? mPauseBackgroundColor : mPlayBackgroundColor);
    // colorAnim.setEvaluator(new ArgbEvaluator());
    // final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
    // mAnimatorSet.setInterpolator(new DecelerateInterpolator());
    // mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
    // mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
    // mAnimatorSet.start();
    // }

    public void Play() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, mPlayBackgroundColor);
        mIsPlay = true;
        colorAnim.setEvaluator(new ArgbEvaluator());
        mDrawable.setmIsPlay(mIsPlay);
        final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator());
        mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
        mAnimatorSet.start();
    }

    public void Pause() {
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        mAnimatorSet = new AnimatorSet();
        final ObjectAnimator colorAnim = ObjectAnimator.ofInt(this, COLOR, mPauseBackgroundColor);
        mIsPlay = false;
        colorAnim.setEvaluator(new ArgbEvaluator());
        mDrawable.setmIsPlay(mIsPlay);
        final Animator pausePlayAnim = mDrawable.getPausePlayAnimator();
        mAnimatorSet.setInterpolator(new DecelerateInterpolator());
        mAnimatorSet.setDuration(PLAY_PAUSE_ANIMATION_DURATION);
        mAnimatorSet.playTogether(colorAnim, pausePlayAnim);
        mAnimatorSet.start();
    }

}
