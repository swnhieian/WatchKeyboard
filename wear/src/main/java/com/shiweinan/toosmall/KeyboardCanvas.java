package com.shiweinan.toosmall;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

class Bound {
    int left;
    int top;
    int right;
    int bottom;

    public Bound(int a, int b, int c, int d) {
        left = a;
        top = b;
        right = c;
        bottom = d;
    }
}

class Coordinate {
    double x;
    double y;

    public Coordinate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void Set(double x, double y) {
        this.x = x;
        this.y = y;
    }
}

public class KeyboardCanvas extends View {
    private MainActivity mainActivity;
    public ProgressController pc;

    private ArrayList<RectangleKey> allKeys = new ArrayList<RectangleKey>(); // include backkey
    private ArrayList<CandidateKey> wordSelections = new ArrayList<CandidateKey>();
    Bound bounds = null;
    Paint boundPaint = new Paint();

    public int sizeMode = 1; // 1, 2, 3

    private SoundPool sp;
    private int soundID;

    public ArrayList<Coordinate> touchpoints = new ArrayList<Coordinate>();

    double lastX, lastY;
    long firstTime = 0;
    long lastTime;

    public String targetText = "protect your environment";
    public String curText = "**********";
    boolean warmup = true;
    Typeface type;

    Vibrator vibrator;

    public boolean finished = false;
    double speed = 0;
    double accuracy = 0;

    private GestureDetector mDetector;

    public KeyboardCanvas(Context context) {
        super(context);
        mainActivity = (MainActivity)context;

        sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundID = sp.load(mainActivity, R.raw.click, 1);  // last for 0.659s
        vibrator =  (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        sp.play(soundID, 0.4f, 0.4f, 0, 0, 1);

        // Configure a gesture detector
        mDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent ev) {
                mainActivity.mDismissOverlay.show();
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        for (int i = 0; i < allKeys.size(); i++) {
            allKeys.get(i).DrawMyself(canvas);
        }

        for (int i = 0; i < wordSelections.size(); i++) {
            wordSelections.get(i).DrawMyself(canvas);
        }

        //绘制背景色
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTextSize((int)(2.5 * (sizeMode + 4)));
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(type);
        if (warmup) {
            canvas.drawText("Warming: " + (pc.currentLineNum + 1) + " / 20", (float)FileGenerator.offsetX, (int)(FileGenerator.offsetY - 5 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        } else {
            canvas.drawText("Testing: " + (pc.currentLineNum + 1) + " / 20", (float)FileGenerator.offsetX, (int)(FileGenerator.offsetY - 5 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        }
        paint.setTextAlign(Paint.Align.LEFT);
        //canvas.drawText(targetText, (int)(FileGenerator.offsetX - 4.5 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), (int)(FileGenerator.offsetY - 4 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        //canvas.drawText(curText, (int)(FileGenerator.offsetX - 4.5 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), (int)(FileGenerator.offsetY - 3 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        canvas.drawText(targetText, (int)(FileGenerator.offsetX - 4 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), (int)(FileGenerator.offsetY - 4 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        canvas.drawText(curText, (int)(FileGenerator.offsetX - 4 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), (int)(FileGenerator.offsetY - 3 * (2 * FileGenerator.ppi/25.4 * (sizeMode * 0.25 + 1))), paint);
        if (finished) {
            canvas.drawText(String.format("Speed: %.1f WPM, CER:%.1f%%", speed, accuracy * 100),
                    (int) (FileGenerator.offsetX - 4.5 * (2 * FileGenerator.ppi / 25.4 * (sizeMode * 0.25 + 1))), (int) (FileGenerator.offsetY - 2 * (2 * FileGenerator.ppi / 25.4 * (sizeMode * 0.25 + 1))), paint);
        }
        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mDetector.onTouchEvent(event)) {
            int index = event.getActionIndex();
            double x = event.getX(index);
            double y = event.getY(index);

            if (bounds.left <= x && x <= bounds.right
                    && bounds.top <= y && y <= bounds.bottom) {
                int action = event.getActionMasked();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_POINTER_DOWN:
                        CaptureDown(event);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_POINTER_UP:
                        CaptureUp(event);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                invalidate();
            }
        }
        return true;
    }

    public void CaptureDown(MotionEvent event) {

        int index = event.getActionIndex();
        double x = event.getX(index);
        double y = event.getY(index);

        lastX = x;
        lastY = y;
        lastTime = System.currentTimeMillis();
        if (firstTime == 0) {
            firstTime = lastTime;
        }
    }

    public RectangleKey FindKey(String symbol) {
        if (symbol == null) {
            return null;
        }
        for (RectangleKey candi : allKeys) {
            if (candi.symbol.equalsIgnoreCase(symbol)) {
                return candi;
            }
        }
        return null;
    }

    public void CaptureUp(MotionEvent event) {
        sp.play(soundID, 0.4f, 0.4f, 0, 0, 1);
        //vibrator.vibrate(50);

        int index = event.getActionIndex();
        double x = event.getX(index);
        double y = event.getY(index);

        double horizontalSwipeThres = 75 * sizeMode / 4 + 75;
        double verticalSwipeThres = horizontalSwipeThres * 0.5;

        if (x - lastX > horizontalSwipeThres) { // swipe right, finish a task, or continue to the next task
            if (!finished) {
                finished = true;
                speed = CalcSpeed();
                accuracy = CalcAccuracy();
            } else {
                if (!warmup) {
                    mainActivity.logger.println("finish," + speed + "," + accuracy);
                    mainActivity.logger.flush();
                }
                pc.ChangeLine(1);
            }
        } else if (lastX - x > horizontalSwipeThres) { // swipe left, delete or restart the task
            if (!finished) {
                if (touchpoints.size() > 0) {
                    wordSelections.clear();
                    touchpoints.clear();
                    return;
                } else if (mainActivity.GetCurrentString().length() > 0) {
                    int space = -1;
                    for (int i = mainActivity.GetCurrentString().length() - 2; i >= 0; i--) {
                        if (mainActivity.GetCurrentString().charAt(i) == ' ') {
                            space = i;
                            break;
                        }
                    }
                    mainActivity.SetCurrentString(mainActivity.GetCurrentString().substring(0, space + 1));
                    wordSelections.clear();
                    touchpoints.clear();
                    if (!warmup) {
                        mainActivity.logger.println("delete," + (System.currentTimeMillis() - lastTime) + "," + lastX + "," + x);
                        mainActivity.logger.flush();
                    }
                    return;
                }
            } else {
                if (!warmup) {
                    mainActivity.logger.println("reset");
                    mainActivity.logger.flush();
                }
                pc.ChangeLine(0);
            }
            //} else if (y - lastY > verticalSwipeThres) { // swipe down, do nothing

        /*} else if (lastY - y > verticalSwipeThres) { // swipe up, choose a candidate
            if (!finished) {
                CandidateKey selected = null;
                int selectIndex;
                for (selectIndex = 0; selectIndex < wordSelections.size(); selectIndex++) {
                    CandidateKey tmp = wordSelections.get(selectIndex);
                    if (tmp.Hit(lastX, lastY)) {
                        selected = tmp;
                        break;
                    }
                }
                if (selected != null) {
                    int space = -1;
                    for (int i = mainActivity.GetCurrentString().length() - 1; i >= 0; i--) {
                        if (mainActivity.GetCurrentString().charAt(i) == ' ') {
                            space = i;
                            break;
                        }
                    }
                    mainActivity.SetCurrentString(mainActivity.GetCurrentString().substring(0, space + 1) + selected.word + " ");
                    if (!warmup) {
                        mainActivity.logger.println("swipeup," + (System.currentTimeMillis() - lastTime) + "," + lastY + "," + y + "," + selectIndex + "," + selected.word + "," + targetText.contains(selected.word));
                        mainActivity.logger.flush();
                    }
                    wordSelections.clear();
                    touchpoints.clear();
                    return;
                }
            }
            */
        } else { // hit space (to correct the input), or hit normal keys
            if (!finished) {
                double keyWidth = 2 * FileGenerator.ppi / 25.4 * (sizeMode * 0.25 + 1);//6.5 * 7.5;   294/25.4 pixel = 1 mm
                double keyHeight = keyWidth;//8 * 7.5;
                double qCenterX = FileGenerator.offsetX - 4.5 * keyWidth;//500 - 4.5 * keyWidth;
                double qCenterY = FileGenerator.offsetY - 1.5 * keyHeight;//400 - 1.5 * keyHeight;
                if (qCenterY - y > keyHeight * 1.5) {
                    CandidateKey selected = null;
                    int selectIndex;
                    for (selectIndex = 0; selectIndex < wordSelections.size(); selectIndex++) {
                        CandidateKey tmp = wordSelections.get(selectIndex);
                        if (tmp.Hit(lastX, lastY)) {
                            selected = tmp;
                            break;
                        }
                    }
                    if (selected != null) {
                        int space = -1;
                        for (int i = mainActivity.GetCurrentString().length() - 1; i >= 0; i--) {
                            if (mainActivity.GetCurrentString().charAt(i) == ' ') {
                                space = i;
                                break;
                            }
                        }
                        mainActivity.SetCurrentString(mainActivity.GetCurrentString().substring(0, space + 1) + selected.word + " ");
                        if (!warmup) {
                            mainActivity.logger.println("select," + lastX + "," + lastY + "," + selectIndex + "," + selected.word + "," + targetText.contains(selected.word) + "," + System.currentTimeMillis());
                            mainActivity.logger.flush();
                        }
                        wordSelections.clear();
                        touchpoints.clear();
                        return;
                    }
                }

                /////////////
                String target = null;
                if ((curText.length() < targetText.length()) && (targetText.charAt(curText.length()) == ' ')) {
                    target = " ";
                } else {
                    if (curText.length() < targetText.length()) {
                        target = targetText.substring(curText.length(), curText.length() + 1);
                    } else {
                        target = null;
                    }
                }
                RectangleKey targetKey = FindKey(target);

                RectangleKey hit = null;
                double maxRst = 1e-100;
                for (RectangleKey cur : allKeys) {
                    int size = sizeMode * 5 + 20;
                    String symbol = cur.symbol.toLowerCase();
                    double xx = pc.clouds.get(size).get(symbol).x;
                    double yy = pc.clouds.get(size).get(symbol).y;
                    double sigx = pc.clouds.get(size).get(symbol).sdx;
                    double sigy = pc.clouds.get(size).get(symbol).sdy;
                    double rst = 1 / sigx / sigy * Math.pow(Math.E, -0.5 * (((x - xx) * (x - xx)) / (sigx * sigx) + ((y - yy) * (y - yy)) / (sigy * sigy)));
                    if (rst > maxRst) {
                        maxRst = rst;
                        hit = cur;
                    }
                    /*if (cur.Hit(lastX, lastY)) {
                        hit = cur;
                        break;
                    }*/
                }
                /*if (hit.GetSymbol().equals(" ")) {
                    if (wordSelections.size() != 0) {
                        int space = -1;
                        for (int i = mainActivity.GetCurrentString().length() - 1; i >= 0; i--) {
                            if (mainActivity.GetCurrentString().charAt(i) == ' ') {
                                space = i;
                                break;
                            }
                        }
                        mainActivity.SetCurrentString(mainActivity.GetCurrentString().substring(0, space + 1) + wordSelections.get(0).word);
                        mainActivity.AppendCurrentString(" ");
                        if (!warmup) {
                            mainActivity.logger.println("space," + lastX + "," + lastY + "," + wordSelections.get(0).word + "," + targetText.contains(wordSelections.get(0).word) + "," + System.currentTimeMillis());
                            mainActivity.logger.flush();
                        }
                        wordSelections.clear();
                        touchpoints.clear();
                        return;
                    } else {
                        mainActivity.AppendCurrentString(" ");
                    }
                }*/
                if (!warmup) {
                    if (targetKey != null) { // within a task
                        mainActivity.logger.println("touch,down," + targetKey.GetSize() + "," + targetKey.GetCenterX() + "," + targetKey.GetCenterY() + "," + lastX + "," + lastY + "," + targetKey.GetID() + "," + targetKey.Hit(lastX, lastY) + "," + lastTime);
                    } else {
                        mainActivity.logger.println("touch,down," + -1 + "," + -1 + "," + -1 + "," + lastX + "," + lastY + "," + target + "," + false + "," + lastTime);
                    }
                    mainActivity.logger.println("touch,up," + x + "," + y + "," + System.currentTimeMillis());
                    mainActivity.logger.flush();
                }
                touchpoints.add(new Coordinate(lastX, lastY));

                wordSelections.clear();
                ArrayList<String> words = pc.ParseWord(touchpoints);
                for (int i = 0; i < words.size(); i++) {
                    //double keyWidth = 2 * 205 / 25.4 * (sizeMode * 0.25 + 1);//6.5 * 7.5;   294/25.4 pixel = 1 mm
                    //double keyHeight = keyWidth;//8 * 7.5;
                    //double qCenterX = FileGenerator.offsetX - 4.5 * keyWidth;//500 - 4.5 * keyWidth;
                    //double qCenterY = FileGenerator.offsetY - 1.5 * keyHeight;//400 - 1.5 * keyHeight;
                    wordSelections.add(new CandidateKey(5.0 * keyWidth / words.size() * (2 * i + 1) + qCenterX - 0.5 * keyWidth, qCenterY - keyHeight, 10 * keyWidth / words.size(), keyHeight, words.get(i)));
                }
            }
        }

        for (int i = 0; i < allKeys.size(); i++) {
            allKeys.get(i).Reset();
        }
    }

    public void ReadKeyboardConfig() {
        allKeys.clear();
        bounds = null;
        FileGenerator.GenerateSmallQWERTY(mainActivity, sizeMode);
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(mainActivity.openFileInput("keyboard.txt")));
            String data = null;
            while ((data = br.readLine()) != null) {
                String[] tokens = data.split(",");
                if (tokens[0].equals("rect")) {
                    RectangleKey aKey = new RectangleKey(Double.parseDouble(tokens[1]), Double.parseDouble(tokens[2]), Double.parseDouble(tokens[3]), Double.parseDouble(tokens[4]), Integer.parseInt(tokens[5]), tokens[6], tokens[7]);
                    allKeys.add(aKey);
                } else if (tokens[0].equals("bound")) {
                    bounds = new Bound(Integer.parseInt(tokens[1]), Integer.parseInt(tokens[2]), Integer.parseInt(tokens[3]), Integer.parseInt(tokens[4]));
                }
            }
            br.close();
        } catch (Exception e) {
            Log.e("YX", e.getMessage());
        }
        pc.shuffle();
        pc.PrepareTask();
    }

    public void Reset() {
        wordSelections.clear();
        touchpoints.clear();
        finished = false;
        firstTime = 0;
    }

    private double CalcAccuracy() {
        String a = targetText;
        String b = "";
        if (curText.length() > 0) {
            b = curText.substring(0, curText.length() - 1);
        }
        int n = a.length();
        int m = b.length();
        int[][] distance = new int[n + 1][m + 1];
        distance[0][0] = 0;
        for (int i = 1; i <= n; i++) {
            distance[i][0] = distance[i - 1][0] + 1;
        }
        for (int j = 1; j <= m; j++) {
            distance[0][j] = distance[0][j - 1] + 1;
        }
        for (int i = 1; i <= n; i++) {
            for (int j = 1; j <= m; j++) {
                int ins = distance[i - 1][j] + 1;
                int sub = distance[i - 1][j - 1];
                if (a.charAt(i - 1) != b.charAt(j - 1)) {
                    sub += 1;
                }
                int del = distance[i][j - 1] + 1;
                distance[i][j] = ins;
                if (sub < distance[i][j]) {
                    distance[i][j] = sub;
                }
                if (del < distance[i][j]) {
                    distance[i][j] = del;
                }
            }
        }

        return (double)distance[n][m] / n;
    }

    private double CalcSpeed() {
        return (double)curText.length() * 12000 / (lastTime - firstTime);
    }
}
