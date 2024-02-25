package com.example.blutetooth_relay_control;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import java.util.Calendar;

public class ArrowView extends View {
    private Paint paint;
    private Path hourPath;
    private Path minutePath;
    private Path secondPath;
    private float secondAngle;

    public ArrowView(Context context) {
        super(context);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ArrowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(15f);

        secondPath = new Path();
    }

    public void setSecondAngle(float angle) {
        secondAngle = angle;
        invalidate(); // Request a redraw when the angle is updated
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the dimensions of the view
        int width = getWidth();
        int height = getHeight();

        // Calculate the center of the view
        float centerX = width / 2f;
        float centerY = height / 2f;

        float secondLength = width / 2.5f;

        // Calculate the coordinates of the arrow endpoints
        float secondX = (float) (centerX + Math.sin(Math.toRadians(secondAngle)) * secondLength);
        float secondY = (float) (centerY - Math.cos(Math.toRadians(secondAngle)) * secondLength);

        // Set paint color to red
        paint.setColor(Color.RED);

        // Draw a red circle at the center point
        canvas.drawCircle(centerX, centerY, 10f, paint); // Adjust the circle radius as per your requirement

        // Draw a red arrow at the endpoint
        paint.setStyle(Paint.Style.FILL);
        canvas.drawLine(centerX, centerY, secondX, secondY, paint);
        canvas.drawPath(createArrowPath(secondX, secondY, secondAngle), paint);
    }

    private Path createArrowPath(float x, float y, float angle) {
        Path path = new Path();
        float arrowSize = 60f; // Adjust the arrow size as per your requirement

        path.moveTo(x, y);
        path.lineTo(x - arrowSize, y + arrowSize); // Reverse the direction of the arrow here
        path.lineTo(x + arrowSize, y + arrowSize); // Reverse the direction of the arrow here
        path.lineTo(x, y);
        path.close();

        // Rotate the arrow path based on the angle
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, x, y);
        path.transform(matrix);

        return path;
    }
}