package com.shiweinan.toosmall;

import android.media.AudioManager;
import android.media.SoundPool;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

class Cloud {
    double x;
    double y;
    double sdx;
    double sdy;
    public Cloud(double x, double y, double sdx, double sdy) {
        this.x = x;
        this.y = y;
        this.sdx = sdx;
        this.sdy = sdy;
    }
}

public class ProgressController {
    private MainActivity mainActivity;
    public KeyboardCanvas kc;

    private ArrayList<String> allLines = new ArrayList<String>();
    public int currentLineNum;
    private SoundPool sp;
    private int soundID;

    HashMap<String, Integer> corpus = new HashMap<String, Integer>();
    HashMap<Integer, HashMap<String, Cloud>> clouds = new HashMap<Integer, HashMap<String, Cloud>>();

    public String GetTargetString() {
        return allLines.get(currentLineNum);
    }

    public void shuffle() {
        if (allLines.size() > 0) {
            Random r = new Random();
            for (int i = 0; i < 20000; i++) {
                int first = r.nextInt(allLines.size());
                int second = r.nextInt(allLines.size());
                String tmp = allLines.get(first);
                allLines.set(first, allLines.get(second));
                allLines.set(second, tmp);
            }
        }
        currentLineNum = 0;
    }

    public ProgressController(MainActivity content) {
        mainActivity = content;
        currentLineNum = 0;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(content.getResources().openRawResource(R.raw.task)));
            String data;
            while ((data = br.readLine()) != null) {
                allLines.add(data.toLowerCase());
            }

            br = new BufferedReader(new InputStreamReader(content.getResources().openRawResource(R.raw.corpus)));
            String buf;
            int counter = 0;
            while ((buf = br.readLine()) != null) {
                String[] tokens = buf.split("\t");
                corpus.put(tokens[0], Integer.parseInt(tokens[1]));
                counter++;
                if (counter > 10000) {
                    break;
                }
            }

            for (int i = 20; i < 45; i += 5) {
                clouds.put(i, new HashMap<String, Cloud>());
            }

            br = new BufferedReader(new InputStreamReader(content.getResources().openRawResource(R.raw.cloud)));
            br.readLine();
            while ((buf = br.readLine()) != null) {
                String[] tokens = buf.split(",");
                int mode = (int)(Double.parseDouble(tokens[0]) * 10);
                String symbol = tokens[1].toLowerCase();
                int dataNum = Integer.parseInt(tokens[2]);
                double x = Double.parseDouble(tokens[3]);
                x = (x - 750) * 205 / 469 + FileGenerator.offsetX;
                double y = Double.parseDouble(tokens[4]);
                y = (y - 600) * 205 / 469 + FileGenerator.offsetY;
                double sdx = Double.parseDouble(tokens[5]) * Math.sqrt( (double)dataNum/ (dataNum - 1)) * 205 / 469;
                double sdy = Double.parseDouble(tokens[6]) * Math.sqrt( (double)dataNum/ (dataNum - 1)) * 205 / 469;
                Cloud tmp = new Cloud(x, y, sdx, sdy);
                clouds.get(mode).put(symbol, tmp);
            }
            br.close();
        } catch (Exception e) {
        }
        //allLines.subList(49, allLines.size() - 1).clear();
        if (allLines.size() > 0) {
            Random r = new Random();
            for (int i = 0; i < 20000; i++) {
                int first = r.nextInt(allLines.size());
                int second = r.nextInt(allLines.size());
                String tmp = allLines.get(first);
                allLines.set(first, allLines.get(second));
                allLines.set(second, tmp);
            }
        }
        sp = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        soundID = sp.load(mainActivity, R.raw.click, 1);  // last for 0.659s
    }

    public void Init() {
        kc = mainActivity.GetKeyboard();
        sp.play(soundID, 1f, 1f, 0, 0, 1);
        PrepareTask();
    }

    public void PrepareTask() {
        mainActivity.SetCurrentString("");
        mainActivity.SetTargetString(GetTargetString());
        if (!kc.warmup) {
            mainActivity.logger.println("sentence," + GetTargetString());
        }
        kc.Reset();
        kc.postInvalidate();
    }

    // Change to next line
    public void ChangeLine(int delta) {
        if (delta == 1) {
            if (currentLineNum == allLines.size() - 1) {
                Toast.makeText(mainActivity, "Finished!", Toast.LENGTH_SHORT).show();
                //System.exit(0);
                return;
            } else {
                currentLineNum++;
            }
        }
        PrepareTask();
    }

    public ArrayList<String> ParseWord(ArrayList<Coordinate> touchpoints) {
        ArrayList<String> rsttt = new ArrayList<String>();
        int size = kc.sizeMode * 5 + 20;
        ArrayList<String> candidates = new ArrayList<String>();
        ArrayList<Double> probs = new ArrayList<Double>();
        for (String candi : corpus.keySet()) {
            if (candi.length() == touchpoints.size()) {
                double rst = 1;
                for (int i = 0; i < candi.length(); i++) {
                    String symbol = candi.substring(i, i + 1);
                    double x = clouds.get(size).get(symbol).x;
                    double y = clouds.get(size).get(symbol).y;
                    double sigx = clouds.get(size).get(symbol).sdx;
                    double sigy = clouds.get(size).get(symbol).sdy;
                    rst *= 1 / sigx / sigy * Math.pow(Math.E, -0.5 * (((touchpoints.get(i).x - x) * (touchpoints.get(i).x - x)) / (sigx * sigx) + ((touchpoints.get(i).y - y) * (touchpoints.get(i).y - y)) / (sigy * sigy)));
                    //rst *= corpus.get(candi);
                }
                candidates.add(candi);
                probs.add(rst);
            }
        }
        String winner = null;
        double winnerValue = -1;
        for (int i = 0; i < candidates.size(); i++) {
            if (probs.get(i) > winnerValue) {
                winnerValue = probs.get(i);
                winner = candidates.get(i);
            }
        }
        if (winner != null) {
            rsttt.add(winner);
        }
        String second = null;
        double secondValue = -1;
        for (int i = 0; i < candidates.size(); i++) {
            if (probs.get(i) > secondValue && probs.get(i) != winnerValue) {
                secondValue = probs.get(i);
                second = candidates.get(i);
            }
        }
        if (second != null) {
            rsttt.add(second);
        }
        String third = null;
        double thirdValue = -1;
        for (int i = 0; i < candidates.size(); i++) {
            if (probs.get(i) > thirdValue && probs.get(i) != winnerValue && probs.get(i) != secondValue) {
                thirdValue = probs.get(i);
                third = candidates.get(i);
            }
        }
        if (third != null) {
            rsttt.add(third);
        }
        return rsttt;
    }
}