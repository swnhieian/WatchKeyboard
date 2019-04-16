package com.shiweinan.toosmall;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class CandidateKey {
    public double width;
    public double height;
    protected double centerX;
    protected double centerY;
    protected String word;
    protected int state; // 0 can't be touched or said released , 1 down
    protected int[] backgroundColor = new int[]{Color.TRANSPARENT, Color.TRANSPARENT, Color.TRANSPARENT, Color.GREEN};

    public CandidateKey(double centerX, double centerY, double width, double height, String symbol) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.word = symbol;
        state = 0;
    }

    public void DrawMyself(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(backgroundColor[state]);
        canvas.drawRect((float)(centerX - width / 2), (float)(centerY - height / 2), (float)(centerX + width / 2), (float)(centerY + height / 2), paint);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect((float)(centerX - width / 2), (float)(centerY - height / 2), (float)(centerX + width / 2), (float)(centerY + height / 2), paint);
        //绘制矩形
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize((float)width / 6);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(word, (float)centerX, (float)(centerY + height / 10), paint);
    }

    public boolean Hit(double x, double y) {
        //if ((Math.abs(x - centerX) < 0.5 * width) && (Math.abs(y - centerY) < 0.5 * height)) {
        if ((Math.abs(x - centerX) < 0.5 * width)) {
            this.state = 1;
            return true;
        }
        return false;
    }

    public double GetSize() {
        return width > height ? width : height;
    }

    public String GetSymbol() {
        return word;
    }

    public double GetCenterX() {
        return centerX;
    }

    public double GetCenterY() {
        return centerY;
    }

    public int GetState() {
        return state;
    }

    public void SetState(int input) {
        state = input;
    }

    public void Reset() {
        state = 0;
    }
}