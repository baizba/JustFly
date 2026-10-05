package com.example.justfly.map;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.function.Consumer;

/** Preserves osmdroid's horizontal zoom controls, including artwork and fading. */
final class MapZoomControls extends View {
    private final Bitmap plus;
    private final Bitmap minus;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF plusBounds = new RectF();
    private final RectF minusBounds = new RectF();
    private final ValueAnimator fade = ValueAnimator.ofFloat(1, 0);
    private float opacity;
    private boolean inEnabled;
    private boolean outEnabled;
    private boolean pressed;
    private boolean zoomIn;
    private boolean justActivated;
    private Consumer<Boolean> listener;
    private final Runnable fadeLater = () -> fade.start();

    MapZoomControls(Context context, MapArtwork artwork) {
        super(context);
        plus = artwork.bitmap("sharp_add_black_36");
        minus = artwork.bitmap("sharp_remove_black_36");
        fade.setDuration(500);
        fade.setInterpolator(new LinearInterpolator());
        fade.addUpdateListener(animation -> {
            opacity = (float) animation.getAnimatedValue();
            invalidate();
        });
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
    }

    void setZoomListener(Consumer<Boolean> listener) { this.listener = listener; }

    void updateEnabled(boolean inEnabled, boolean outEnabled) {
        this.inEnabled = inEnabled;
        this.outEnabled = outEnabled;
        invalidate();
    }

    void activate() {
        justActivated = opacity == 0;
        fade.cancel();
        removeCallbacks(fadeLater);
        opacity = 1;
        postDelayed(fadeLater, 3500);
        invalidate();
    }

    void release() {
        removeCallbacks(fadeLater);
        fade.cancel();
        listener = null;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        float size = plus.getWidth();
        float left = (width - 2.5f * size) / 2;
        float top = height - 1.5f * size;
        minusBounds.set(left, top, left + size, top + size);
        plusBounds.set(left + 1.5f * size, top, left + 2.5f * size, top + size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (opacity <= 0) {
            return;
        }
        drawButton(canvas, minus, minusBounds, outEnabled);
        drawButton(canvas, plus, plusBounds, inEnabled);
    }

    private void drawButton(Canvas canvas, Bitmap bitmap, RectF bounds, boolean enabled) {
        paint.setColor(enabled ? Color.WHITE : Color.LTGRAY);
        paint.setAlpha((int) (opacity * 255));
        canvas.drawRect(bounds.left, bounds.top, bounds.right - 1, bounds.bottom - 1, paint);
        paint.setColor(Color.BLACK);
        paint.setAlpha((int) (opacity * 255));
        canvas.drawBitmap(bitmap, bounds.left, bounds.top, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
            // An invisible button's first touch only reveals it, as in osmdroid.
            if (justActivated) {
                return false;
            }
            zoomIn = plusBounds.contains(event.getX(), event.getY());
            pressed = zoomIn || minusBounds.contains(event.getX(), event.getY());
            return pressed;
        }
        if (!pressed) {
            return false;
        }
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            boolean inside = (zoomIn ? plusBounds : minusBounds).contains(event.getX(), event.getY());
            if (inside && (zoomIn ? inEnabled : outEnabled) && listener != null) {
                listener.accept(zoomIn);
            }
            pressed = false;
        } else if (event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            pressed = false;
        }
        return true;
    }
}
