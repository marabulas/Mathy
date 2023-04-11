package com.example.mathy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.view.View;

public class LineView extends View {

    private Paint mPaint;
    private Path mPath;
    private PathMeasure mPathMeasure;

    private float mPathLength;
    private float mFraction = 0f;

    public LineView(Context context, int color) {
        super(context);

        mPaint = new Paint();
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(10);

        mPath = new Path();
        mPathMeasure = new PathMeasure(mPath, false);
    }

    public void setPath(Path path) {
        mPath = path;
        mPathMeasure = new PathMeasure(mPath, false);
        mPathLength = mPathMeasure.getLength();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mFraction < 1f) {
            mFraction += 0.05f;
            if (mFraction > 1f) {
                mFraction = 1f;
            }
            drawLine(canvas);
            invalidate();
        }
    }

    private void drawLine(Canvas canvas) {
        mPathMeasure.getSegment(0, mPathLength * mFraction, mPath, true);
        canvas.drawPath(mPath, mPaint);
    }

}

