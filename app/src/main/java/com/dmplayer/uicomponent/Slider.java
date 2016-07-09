package com.dmplayer.uicomponent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.dmplayer.R;
import com.nineoldandroids.view.ViewHelper;

public class Slider extends CustomView {

    private int backgroundColor = Color.parseColor("#4CAF50");
    private int backgroundColorLine = Color.parseColor("#4CAF50");
    private Ball ball;
    private Bitmap bitmap;
    private int max = 100;
    private int min = 0;
    private OnValueChangedListener onValueChangedListener;
    private boolean placedBall = false;
    private boolean press = false;
    private int value = 0;

    public Slider(Context context, AttributeSet attrs) {
        super(context, attrs);
        setAttributes(attrs);
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public OnValueChangedListener getOnValueChangedListener() {
        return onValueChangedListener;
    }

    public void setOnValueChangedListener(
            OnValueChangedListener onValueChangedListener) {
        this.onValueChangedListener = onValueChangedListener;
    }

    // GETERS & SETTERS

    public int getValue() {
        return value;
    }

    public void setValue(final int value) {
        if (placedBall == false)
            post(new Runnable() {

                @Override
                public void run() {
                    setValue(value);
                }
            });
        else {
            this.value = value;
            float division = (ball.xFin - ball.xIni) / max;
            ViewHelper.setX(ball, value * division + getHeight() / 2 - ball.getWidth() / 2);
            ball.changeBackground();
        }

    }

    @Override
    public void invalidate() {
        ball.invalidate();
        super.invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        isLastTouch = true;
        if (isEnabled()) {
            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {

                if ((event.getX() <= getWidth() && event.getX() >= 0)) {
                    press = true;
                    // calculate value
                    int newValue = 0;
                    float division = (ball.xFin - ball.xIni) / (max - min);
                    if (event.getX() > ball.xFin) {
                        newValue = max;
                    } else if (event.getX() < ball.xIni) {
                        newValue = min;
                    } else {
                        newValue = min + (int) ((event.getX() - ball.xIni) / division);
                    }
                    if (value != newValue) {
                        value = newValue;
                        if (onValueChangedListener != null)
                            onValueChangedListener.onValueChanged(newValue);
                    }
                    // move ball indicator
                    float x = event.getX();
                    x = (x < ball.xIni) ? ball.xIni : x;
                    x = (x > ball.xFin) ? ball.xFin : x;
                    ViewHelper.setX(ball, x);
                    ball.changeBackground();

                } else {
                    press = false;
                    isLastTouch = false;
                }

            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                press = false;
            }
        }
        return true;
    }

    @Override
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        backgroundColorLine = makeLineDeselectColor();
        if (isEnabled())
            beforeBackground = backgroundColor;
    }

    /**
     * Make a dark color to press effect
     *
     * @return
     */
    protected int makeLineDeselectColor() {
        int r = (this.backgroundColor >> 16) & 0xFF;
        int g = (this.backgroundColor >> 8) & 0xFF;
        int b = (this.backgroundColor >> 0) & 0xFF;
        r = (r - 30 < 0) ? 0 : r - 30;
        g = (g - 30 < 0) ? 0 : g - 30;
        b = (b - 30 < 0) ? 0 : b - 30;
        return Color.argb(128, r, g, b);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!placedBall) {
            placeBall();
        }

        Paint paint = new Paint();
        paint.setColor(backgroundColorLine);
        paint.setStrokeWidth(dpToPx(3, getResources()));
        canvas.drawLine(getHeight() / 2, getHeight() / 2, getWidth() - getHeight() / 2, getHeight() / 2, paint);
        paint.setColor(backgroundColor);
        float division = (ball.xFin - ball.xIni) / (max - min);
        int value = this.value - min;
        canvas.drawLine(getHeight() / 2, getHeight() / 2, value * division + getHeight() / 2, getHeight() / 2, paint);

        if (press) {
            paint.setColor(backgroundColor);
            paint.setAntiAlias(true);
            canvas.drawCircle(ViewHelper.getX(ball) + ball.getWidth() / 2, getHeight() / 2, getHeight() / 4, paint);
        }
        invalidate();
    }

    // Set atributtes of XML to View
    protected void setAttributes(AttributeSet attrs) {

        setBackgroundResource(R.drawable.background_transparent);

        // Set size of view
        setMinimumHeight(dpToPx(48, getResources()));
        setMinimumWidth(dpToPx(80, getResources()));

        // Set background Color
        // Color by resource
        int bacgroundColor = attrs.getAttributeResourceValue(ANDROIDXML, "background", -1);
        if (bacgroundColor != -1) {
            setBackgroundColor(getResources().getColor(bacgroundColor));
        } else {
            // Color by hexadecimal
            int background = attrs.getAttributeIntValue(ANDROIDXML, "background", -1);
            if (background != -1)
                setBackgroundColor(background);
        }

        min = attrs.getAttributeIntValue(MATERIALDESIGNXML, "min", 0);
        max = attrs.getAttributeIntValue(MATERIALDESIGNXML, "max", 0);
        value = attrs.getAttributeIntValue(MATERIALDESIGNXML, "value", min);

        ball = new Ball(getContext());
        LayoutParams params = new LayoutParams(dpToPx(15, getResources()), dpToPx(15, getResources()));
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        ball.setLayoutParams(params);
        addView(ball);

    }

    private void placeBall() {
        ViewHelper.setX(ball, getHeight() / 2 - ball.getWidth() / 2);
        ball.xIni = ViewHelper.getX(ball);
        ball.xFin = getWidth() - getHeight() / 2 - ball.getWidth() / 2;
        ball.xCen = getWidth() / 2 - ball.getWidth() / 2;
        placedBall = true;
    }

    // Event when slider change value
    public interface OnValueChangedListener {
        public void onValueChanged(int value);
    }

    class Ball extends View {

        float xIni, xFin, xCen;

        public Ball(Context context) {
            super(context);
            changeBackground();
        }

        public void changeBackground() {
            setBackgroundResource(R.drawable.background_checkbox);
            LayerDrawable layer = (LayerDrawable) getBackground();
            GradientDrawable shape = (GradientDrawable) layer.findDrawableByLayerId(R.id.shape_bacground);
            shape.setColor(backgroundColor);
        }

    }

    /**
     * Convert Dp to Pixel
     */
    public static int dpToPx(float dp, Resources resources) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return (int) px;
    }

    public static int getRelativeTop(View myView) {
        if (myView.getId() == android.R.id.content)
            return myView.getTop();
        else
            return myView.getTop() + getRelativeTop((View) myView.getParent());
    }

    public static int getRelativeLeft(View myView) {
        if (myView.getId() == android.R.id.content)
            return myView.getLeft();
        else
            return myView.getLeft() + getRelativeLeft((View) myView.getParent());
    }

}
