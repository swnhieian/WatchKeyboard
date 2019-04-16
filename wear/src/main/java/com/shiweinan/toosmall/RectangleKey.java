package com.shiweinan.toosmall;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class RectangleKey {
    public double width;
    public double height;
    protected double centerX;
    protected double centerY;
    protected String symbol;
    public String ID;
    protected int state; // 0 can't be touched or said released , 1 down
    protected int[] backgroundColor = new int[]{Color.rgb(0xf8, 0xf8, 0xff), Color.rgb(0xf8, 0xf8, 0xff), Color.rgb(0xf8, 0xf8, 0xff), Color.GREEN};
    protected boolean isHidden = false;
    protected int hand = 0; // 0 not decide, 1 left, 2 right

    public RectangleKey(double centerX, double centerY, double width, double height, int hand, String symbol, String ID) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.symbol = symbol;
        state = 0;
        this.hand = hand;
        this.ID = ID;
    }

    public void DrawMyself(Canvas canvas) {
        if (isHidden) {
            return;
        }
        Paint paint = new Paint();
        paint.setColor(backgroundColor[state]);
        canvas.drawRect((float)(centerX - width / 2), (float)(centerY - height / 2), (float)(centerX + width / 2), (float)(centerY + height / 2), paint);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect((float)(centerX - width / 2), (float)(centerY - height / 2), (float)(centerX + width / 2), (float)(centerY + height / 2), paint);
        //ç»˜åˆ¶çŸ©å½¢
        paint.setColor(Color.BLACK);
        paint.setTextSize((float)(height / 2));
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(symbol, (float)centerX, (float)centerY, paint);
    }

    public boolean Hit(double x, double y) {
        if (isHidden) {
            return false;
        }
        if ((Math.abs(x - centerX) < 0.5 * width) && (Math.abs(y - centerY) < 0.5 * height)) {
            //vb.vibrate(new long[]{0, 40}, -1);
            this.state = 1;
            //sp.play(soundID, 0.4f, 0.4f, 0, 0, 1);
            return true;
        }
        return false;
    }

    public double GetSize() {
        return width > height ? height : width;
    }

    public void SetHidden(boolean input) {
        isHidden = input;
    }

    public int GetHand() {
        return hand;
    }

    public String GetSymbol() {
        return symbol;
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
        if (isHidden) {
            return;
        }
        state = 0;
    }

    public String GetID() {
        return ID;
    }
}
