package com.shiweinan.toosmall;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.DismissOverlayView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class MainActivity extends WearableActivity {

    private KeyboardCanvas kc;
    private ProgressController pc;
    FrameLayout topLayout;
    public DismissOverlayView mDismissOverlay;

    public PrintWriter logger = null;

    public void SetTargetString(String input) {
        GetKeyboard().targetText = input;
    }

    public KeyboardCanvas GetKeyboard() {
        return kc;
    }

    public void SetCurrentString(String input) {
        GetKeyboard().curText = input;
    }

    public void AppendCurrentString(String input) {
        GetKeyboard().curText += input;
    }

    public String GetCurrentString() {
        return GetKeyboard().curText;
    }

    public MainActivity GetMa() {
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){ // Called when beginning or orientation changed
        super.onCreate(savedInstanceState);


        try {
            int i = 0;
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/watchLog" + i + ".txt");
            while (file.exists()) {
                i++;
                file = new File(Environment.getExternalStorageDirectory().getPath() + "/watchLog" + i + ".txt");
            }
            logger = new PrintWriter(new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/watchLog" + i + ".txt", false)), true);
        } catch(Exception e) {
            Log.v("YX", "Create File Failed: " + e.getMessage());
        }

        topLayout = new FrameLayout(this);
        topLayout.setBackgroundColor(Color.BLACK);
        topLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(topLayout);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        LinearLayout frame = new LinearLayout(this);
        frame.setOrientation(LinearLayout.VERTICAL);
        frame.setGravity(Gravity.CENTER_HORIZONTAL);
        topLayout.addView(frame, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        Spinner augSwitch = new Spinner(this);
        String[] phases = {"2.5mm Warm Up", "2.5mm Test", "3mm Warm Up", "3mm Test", "3.5mm Warm Up", "3.5mm Test"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phases);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        augSwitch.setAdapter(adapter);
        augSwitch.setGravity(Gravity.CENTER_HORIZONTAL);
        augSwitch.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                kc.sizeMode = arg2 / 2 + 1;
                kc.warmup = arg2 % 2 == 0;
                if (!kc.warmup) {
                    logger.println(String.format("changemode,%.1f", (arg2 / 2 + 5) * 0.5));
                }
                Toast.makeText(GetMa(), String.format("%.1f mm size", (arg2 / 2 + 5) * 0.5), Toast.LENGTH_SHORT).show();
                pc.currentLineNum = 0;
                kc.ReadKeyboardConfig();
                kc.postInvalidate();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        augSwitch.setPadding(0, 20, 0, 0);
        frame.addView(augSwitch, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        kc = new KeyboardCanvas(this);
        Typeface type = Typeface.createFromAsset(getAssets(), "fonts/consola.ttf");
        kc.type = type;
        LinearLayout.LayoutParams keyboardCanvasParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        frame.addView(kc, keyboardCanvasParams);

        // Obtain the DismissOverlayView element
        mDismissOverlay = new DismissOverlayView(this);//(DismissOverlayView) findViewById(R.id.dismiss_overlay);
        mDismissOverlay.setIntroText(R.string.long_press_intro);
        mDismissOverlay.showIntroIfNecessary();
        topLayout.addView(mDismissOverlay, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        pc = new ProgressController(this);
        //pc.Init();
        pc.kc = kc;
        kc.pc = pc;
        kc.ReadKeyboardConfig();
    }

    @Override
    public void onDestroy() {
        logger.close();
        super.onDestroy();
    }
}
