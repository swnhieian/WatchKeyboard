package com.shiweinan.toosmall;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class FileGenerator {
    public static double ppi = 263;
    public static double offsetX = 180;//was 160
    public static double offsetY = 180;

    public static void GenerateSmallQWERTY(Activity content, double ratio) { // ratio = 1, 2, or 4
        double keyWidth = 2 * ppi/25.4 * (ratio / 4 + 1);//6.5 * 7.5;   294/25.4 pixel = 1 mm
        double keyHeight = keyWidth;//8 * 7.5;
        double qCenterX = offsetX - 4.5 * keyWidth;//500 - 4.5 * keyWidth;
        double qCenterY = offsetY - 1.5 * keyHeight;//400 - 1.5 * keyHeight;
        double horizontalMargin = 0;//1.5 * 7.5;
        double verticalMargin = 0;//2 * 7.5;
        String spliter = ",";
        String keyKind = "rect" + spliter;
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(content.openFileOutput("keyboard.txt", Context.MODE_PRIVATE)), true);
            String[] line1 = new String[]{"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
            String[] line2 = new String[]{"A", "S", "D", "F", "G", "H", "J", "K", "L"};
            String[] line3 = new String[]{"Z", "X", "C", "V", "B", "N", "M"};
            //String[] line4 = new String[]{" "};
            for (int i = 0; i < line1.length; i++) {
                String buf = keyKind + (int)(qCenterX + i * keyWidth + i * horizontalMargin) + spliter + (int)qCenterY + spliter + (int)keyWidth + spliter + (int)keyHeight + spliter + 1 + spliter + line1[i] + spliter + line1[i];
                pw.println(buf);
            }
            for (int i = 0; i < line2.length; i++) {
                String buf = keyKind + (int)(qCenterX + i * keyWidth + keyWidth / 2 + i * horizontalMargin) + spliter + (int)(qCenterY + keyHeight + verticalMargin) + spliter + (int)keyWidth + spliter + (int)keyHeight + spliter + 1 + spliter + line2[i] + spliter + line2[i];
                pw.println(buf);
            }
            for (int i = 0; i < line3.length; i++) {
                String buf = keyKind + (int)(qCenterX + i * keyWidth + keyWidth * 1.5 + i * horizontalMargin) + spliter + (int)(qCenterY + keyHeight * 2 + verticalMargin * 2) + spliter + (int)keyWidth + spliter + (int)keyHeight + spliter + 1 + spliter + line3[i] + spliter + line3[i];
                pw.println(buf);
            }
            //pw.println(keyKind + (int)(qCenterX + 4.5 * keyWidth + 3 * horizontalMargin) + spliter + (int)(qCenterY + keyHeight * 3 + verticalMargin * 3) + spliter + (int)(5 * keyWidth + 4 * horizontalMargin) + spliter + (int)keyHeight + spliter + 1 + spliter + line4[0] + spliter + line4[0]);
            pw.println("bound" + spliter + (int)(qCenterX - 2 * keyWidth ) + spliter + (int)(qCenterY - 7.5 * keyHeight ) + spliter + (int)(qCenterX + 11 * keyWidth) + spliter + (int)(qCenterY + 5.5 * keyHeight));
            pw.close();
        } catch (Exception e) {
            Log.e("YX", e.getMessage());
        }
    }
}
