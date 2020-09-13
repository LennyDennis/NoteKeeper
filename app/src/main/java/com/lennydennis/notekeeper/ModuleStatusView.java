package com.lennydennis.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.widget.ExploreByTouchHelper;

import java.util.List;

import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_CLICK;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    private static final int EDIT_MODE_MODULE_COUNT = 7;
    private static final int INVALID_INDEX = -1;
    private static final int SHAPE_CIRCLE = 0;
    private static final float OUTLINE_WIDTH_DP = 2f;
    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;



    private boolean[] mModuleStatus;
    private float mOutlineWidth;
    private float mShapeSize;
    private float mSpacing;
    private Rect[] mModuleRectangle;
    private int mOutlineColor;
    private Paint mPaintOutline;
    private int mFillColor;
    private Paint mPaintFill;
    private float mRadius;
    private int mMaxHorizontalModules;
    private int mShape;
    private ModuleStatusAccessibilityHelper mAccessibilityHelper;

    public boolean[] getModuleStatus() {
        return mModuleStatus;
    }

    public void setModuleStatus(boolean[] moduleStatus) {
        mModuleStatus = moduleStatus;
    }

    public ModuleStatusView(Context context) {
        super(context);
        init(null, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ModuleStatusView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {

        if(isInEditMode())
            setUpEditModeValues();

        setFocusable(true);
        mAccessibilityHelper = new ModuleStatusAccessibilityHelper(this);
        ViewCompat.setAccessibilityDelegate(this, mAccessibilityHelper);


        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float displayDensity = displayMetrics.density;
        float defaultOutlineWidthPixels = displayDensity * OUTLINE_WIDTH_DP;

        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);

        mOutlineColor = a.getColor(R.styleable.ModuleStatusView_outlineColor, Color.BLACK);
        mShape = a.getInt(R.styleable.ModuleStatusView_shape, SHAPE_CIRCLE);
        mOutlineWidth = a.getDimension(R.styleable.ModuleStatusView_outlineWidth,defaultOutlineWidthPixels);

        a.recycle();

        mOutlineWidth = 6f;
        mShapeSize = 144f;
        mSpacing = 20f;
        mRadius = (mShapeSize - mOutlineWidth)/2;

        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setColor(mOutlineColor);

        mFillColor = getContext().getResources().getColor(R.color.pluralSight);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(mFillColor);

    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mAccessibilityHelper.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mAccessibilityHelper.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    @Override
    protected boolean dispatchHoverEvent(MotionEvent event) {
        return mAccessibilityHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    private void setUpEditModeValues() {
        boolean[] exampleModuleValues = new boolean[EDIT_MODE_MODULE_COUNT];
        int middle = EDIT_MODE_MODULE_COUNT/2;
        for(int i =0; i< middle;i++)
            exampleModuleValues[i] = true;

        setModuleStatus(exampleModuleValues);
    }

    private void setupModuleRectangle(int width) {
        int availableWidth = width-getPaddingLeft()-getPaddingRight();
        int horizontalModuleThatCanFit = (int)(availableWidth/(mShapeSize+mSpacing));
        int maxHorizontalModule = Math.min(horizontalModuleThatCanFit,mModuleStatus.length);
        mModuleRectangle = new Rect[mModuleStatus.length];
        for(int moduleIndex = 0; moduleIndex < mModuleRectangle.length; moduleIndex++){

            int column = moduleIndex % maxHorizontalModule;
            int row = moduleIndex/maxHorizontalModule;

            int x = getPaddingLeft()+(int) (column * (mShapeSize + mSpacing));
            int y = getPaddingTop()+(int)(row*(mShapeSize+mSpacing));
            mModuleRectangle[moduleIndex] = new Rect(x, y, x + (int) mShapeSize, y + (int) mShapeSize);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desireWidth = 0;
        int desireHeight = 0;

        int specWidth = MeasureSpec.getSize(widthMeasureSpec);
        int availableWidth = specWidth - getPaddingLeft() - getPaddingRight();
        int horizontalModuleThatCanFit = (int) (availableWidth/(mShapeSize - mSpacing));
        mMaxHorizontalModules = Math.min(horizontalModuleThatCanFit, mModuleStatus.length);

        desireWidth = (int)((mMaxHorizontalModules * (mShapeSize + mSpacing)) - mSpacing);
        desireWidth += getPaddingLeft() + getPaddingRight();

        int rows = ((mModuleStatus.length - 1)/mMaxHorizontalModules)+1;
        desireHeight = (int) ((rows * (mShapeSize + mSpacing)) - mSpacing);
        desireHeight += getPaddingTop() + getPaddingBottom();

        int width = resolveSizeAndState(desireWidth,widthMeasureSpec,0);
        int height = resolveSizeAndState(desireHeight,heightMeasureSpec,0);

        setMeasuredDimension(width,height);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        setupModuleRectangle(w);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for(int moduleIndex=0; moduleIndex < mModuleRectangle.length; moduleIndex++) {
            if(mShape == SHAPE_CIRCLE) {
                float x = mModuleRectangle[moduleIndex].centerX();
                float y = mModuleRectangle[moduleIndex].centerY();
                if (mModuleStatus[moduleIndex])
                    canvas.drawCircle(x, y, mRadius, mPaintFill);
                canvas.drawCircle(x, y, mRadius, mPaintOutline);
            } else {
                drawSquare(canvas, moduleIndex);
            }
        }
    }

    private void drawSquare(Canvas canvas, int moduleIndex) {
        Rect moduleRectangle = mModuleRectangle[moduleIndex];

        if(mModuleStatus[moduleIndex])
            canvas.drawRect(moduleRectangle, mPaintFill);

        canvas.drawRect(moduleRectangle.left + (mOutlineWidth/2),
                moduleRectangle.top + (mOutlineWidth/2),
                moduleRectangle.right - (mOutlineWidth/2),
                moduleRectangle.bottom - (mOutlineWidth/2),
                mPaintOutline);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                int moduleIndex = findItemAtPoint(event.getX(), event.getY());
                onModuleSelected(moduleIndex);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void onModuleSelected(int moduleIndex) {
        if(moduleIndex == INVALID_INDEX)
            return;

        mModuleStatus[moduleIndex] =! mModuleStatus[moduleIndex];
        invalidate();

        mAccessibilityHelper.invalidateVirtualView(moduleIndex);
        mAccessibilityHelper.sendEventForVirtualView(moduleIndex, AccessibilityEvent.TYPE_VIEW_CLICKED);
    }

    private int findItemAtPoint(float x, float y) {
        int moduleIndex = INVALID_INDEX;
        for (int i =0; i<mModuleRectangle.length;i++){
            if(mModuleRectangle[i].contains((int) x, (int) y)){
                moduleIndex = 1;
                break;
            }
        }
        return moduleIndex;
    }

    private class ModuleStatusAccessibilityHelper extends ExploreByTouchHelper{
        /**
         * Constructs a new helper that can expose a virtual view hierarchy for the
         * specified host view.
         *
         * @param host view whose virtual view hierarchy is exposed by this helper
         */
        public ModuleStatusAccessibilityHelper(@NonNull View host) {
            super(host);
        }

        @Override
        protected int getVirtualViewAt(float x, float y) {
            int moduleIndex = findItemAtPoint(x,y);
            return moduleIndex == INVALID_INDEX ? ExploreByTouchHelper.INVALID_ID:moduleIndex;
        }

        @Override
        protected void getVisibleVirtualViews(List<Integer> virtualViewIds) {
            if(mModuleRectangle == null){
                return;
            }
            for(int moduleIndex = 0; moduleIndex< mModuleRectangle.length;moduleIndex++){
                virtualViewIds.add(moduleIndex);
            }
        }

        @Override
        protected void onPopulateNodeForVirtualView(int virtualViewId, @NonNull AccessibilityNodeInfoCompat node) {
            node.setFocusable(true);
            node.setBoundsInScreen(mModuleRectangle[virtualViewId]);
            node.setContentDescription("Module" + virtualViewId);

            node.setCheckable(true);
            node.setChecked(mModuleStatus[virtualViewId]);
            node.addAction(AccessibilityNodeInfoCompat.ACTION_CLICK
            );
        }

        @Override
        protected boolean onPerformActionForVirtualView(int virtualViewId, int action, @Nullable Bundle arguments) {
            switch (action){
                case AccessibilityNodeInfoCompat.ACTION_CLICK:
                    onModuleSelected(virtualViewId);
                    return true;
            }
            return false;
        }
    }
}
