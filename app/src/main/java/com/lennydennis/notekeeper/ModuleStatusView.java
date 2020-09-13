package com.lennydennis.notekeeper;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ModuleStatusView extends View {
    private static final int EDIT_MODE_MODULE_COUNT = 7;
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
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ModuleStatusView, defStyle, 0);

        a.recycle();

        mOutlineWidth = 6f;
        mShapeSize = 144f;
        mSpacing = 20f;
        mRadius = (mShapeSize - mOutlineWidth)/2;

        mOutlineColor = Color.BLACK;
        mPaintOutline = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintOutline.setStyle(Paint.Style.STROKE);
        mPaintOutline.setColor(mOutlineColor);

        mFillColor = getContext().getResources().getColor(R.color.pluralSight);
        mPaintFill = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFill.setStyle(Paint.Style.FILL);
        mPaintFill.setColor(mFillColor);

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

        for(int moduleIndex =  0 ; moduleIndex<mModuleRectangle.length;moduleIndex++){
            float x = mModuleRectangle[moduleIndex].centerX();
            float y = mModuleRectangle[moduleIndex].centerY();

            if(mModuleStatus[moduleIndex])
                canvas.drawCircle(x,y, mRadius, mPaintFill);

            canvas.drawCircle(x,y, mRadius,mPaintOutline);


        }
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getExampleString() {
        return mExampleString;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param exampleString The example string attribute value to use.
     */
    public void setExampleString(String exampleString) {
        mExampleString = exampleString;
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getExampleColor() {
        return mExampleColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param exampleColor The example color attribute value to use.
     */
    public void setExampleColor(int exampleColor) {
        mExampleColor = exampleColor;
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getExampleDimension() {
        return mExampleDimension;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param exampleDimension The example dimension attribute value to use.
     */
    public void setExampleDimension(float exampleDimension) {
        mExampleDimension = exampleDimension;
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getExampleDrawable() {
        return mExampleDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param exampleDrawable The example drawable attribute value to use.
     */
    public void setExampleDrawable(Drawable exampleDrawable) {
        mExampleDrawable = exampleDrawable;
    }
}
