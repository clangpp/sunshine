package com.clangpp.sunshine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

/**
 * Created by ytzhang on 11/5/14.
 */
public class MyView extends View {
    private Context context;

    // private String windSpeedDir;
    public MyView(Context context) {
        super(context);
        this.context = context;
    }

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public MyView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        this.context = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void setContentDescription(CharSequence contentDescription) {
        super.setContentDescription(contentDescription);
        // if (AccessibilityManager.getInstance(context).isEnabled()) {
        //     sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED);
        // }
    }

    @Override
    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        // event.getText().add(windSpeedDir);
        return true;
    }
}
