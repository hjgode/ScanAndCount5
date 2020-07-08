package com.sample.scanandcount;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeDeviceConnectionEvent;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerNotClaimedException;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_ESCAPE;
import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static com.honeywell.aidc.BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BarcodeReader.BarcodeListener,
        BarcodeReader.TriggerListener, View.OnKeyListener {
    private static String TAG="ScanAndCount";
    private boolean btnPressed=false;
    private boolean scannerEnabled=true;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Context _context=this;
    private boolean bUseFullScreen=false;

    TextView txtScan;
    EditText txtAmount;
    Button btnScan;
    Button btnEnter,btnClr;
    Button btn0, btn1, btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        txtScan=findViewById(R.id.txtScan);
        txtAmount=findViewById(R.id.txtAmount);

        btnScan=findViewById(R.id.btnScan);

        btnEnter=findViewById(R.id.btnEnter);
        btnClr=findViewById(R.id.btnClr);

        btn0=findViewById(R.id.btn0);
        btn1=findViewById(R.id.btn1);
        btn2=findViewById(R.id.btn2);
        btn3=findViewById(R.id.btn3);
        btn4=findViewById(R.id.btn4);
        btn5=findViewById(R.id.btn5);
        btn6=findViewById(R.id.btn6);
        btn7=findViewById(R.id.btn7);
        btn8=findViewById(R.id.btn8);
        btn9=findViewById(R.id.btn9);

        btn0.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);
        btn3.setOnClickListener(this);
        btn4.setOnClickListener(this);
        btn5.setOnClickListener(this);
        btn6.setOnClickListener(this);
        btn7.setOnClickListener(this);
        btn8.setOnClickListener(this);
        btn9.setOnClickListener(this);

        btnEnter.setOnClickListener(this);
        btnClr.setOnClickListener(this);

        //btnScan.setOnClickListener(this);
        btnScan.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Log.d(TAG, "OnTouch: " + motionEvent.getAction());
                switch (motionEvent.getAction()){
                    case ACTION_DOWN:
                        doScan();
                        break;
                    case ACTION_UP:
                        doStopScan();
                        break;
                }

                return true;
            }
        });

        if(savedInstanceState!=null){
            txtScan.setText(savedInstanceState.getString("MyScan"));
            txtAmount.setText(savedInstanceState.getString("MyAmount"));
        }
        createScanner();
        hideSystemUI();
        getWindow().getDecorView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                int screenHeight = getWindow().getDecorView().getRootView().getHeight();

                int keyboardHeight = screenHeight - rect.bottom;
                Log.d(TAG,"LayoutListener: rect="+rect.toShortString());
                if (keyboardHeight > screenHeight * 0.15) {
                    hideSystemUI();
                }
                else
                    showSystemUI();
            }
        });
        enableScanner(scannerEnabled);
    }

    @Override
    public void onClick(View view){
        Log.d(TAG, "onClick: "+view.toString());
        switch (view.getId()) {
            //TODO: OBSOLETE
            case R.id.btnScan:
                doScan();
                break;
            case R.id.btn0:
                setCount(0);
                break;
            case R.id.btn1:
                setCount(1);
                break;
            case R.id.btn2:
                setCount(2);
                break;
            case R.id.btn3:
                setCount(3);
                break;
            case R.id.btn4:
                setCount(4);
                break;
            case R.id.btn5:
                setCount(5);
                break;
            case R.id.btn6:
                setCount(6);
                break;
            case R.id.btn7:
                setCount(7                );
                break;
            case R.id.btn8:
                setCount(8                );
                break;
            case R.id.btn9:
                setCount(9);
                break;
            case R.id.btnClr:
                txtAmount.setText("");

                break;
            case R.id.btnEnter:
                txtAmount.setText("");
                txtScan.setText("");
                scannerEnabled=true;
                enableScanner(scannerEnabled);
                break;
        }
    }

    private void hideSystemUI() {
        if(bUseFullScreen) {
            getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LOW_PROFILE |
                            View.SYSTEM_UI_FLAG_IMMERSIVE
            );
        }
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        if(bUseFullScreen) {
            getWindow().getDecorView().setSystemUiVisibility(
//                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
//                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            );
        }
    }


    private void setCount(int i){
        txtAmount.append(""+i);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.
//        savedInstanceState.putBoolean("MyBoolean", true);
//        savedInstanceState.putDouble("myDouble", 1.9);
//        savedInstanceState.putInt("MyInt", 1);
        savedInstanceState.putString("MyScan", txtScan.getText().toString());
        savedInstanceState.putString("MyAmount", txtAmount.getText().toString());
        // etc.
    }

    private void doStopScan(){
        if(barcodeReader!=null) {
                try {
                    barcodeReader.aim(false);
                    barcodeReader.light(false);
                    barcodeReader.decode(false);
                }catch (Exception ex){
                    Log.d(TAG, "exception in doStopScan(): " +ex.getMessage());
                }
        }
    }

    private void doScan(){
        Log.d(TAG, "doScan");
        try {
            Log.d(TAG, barcodeReader.getStringProperty(PROPERTY_TRIGGER_CONTROL_MODE));
        }catch(Exception ex){

        }
        if(!scannerEnabled){
            Log.d(TAG, "Scanner disabled");
            return;
        }
        if(barcodeReader!=null) {
            if (!btnPressed) {
                btnPressed=true;
                try {
                    barcodeReader.aim(true);
                    barcodeReader.light(true);
                    barcodeReader.decode(true);
                    btnPressed=false;
                }catch (Exception ex){
                    Log.d(TAG, "exception in doScan(): " +ex.getMessage());
                }
            }
        }

    }
    private void createScanner(){
        Log.d(TAG, "createScanner");
        // create the AidcManager providing a Context and a
        // CreatedCallback implementation.
        AidcManager.create(this, new CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                try {
                    barcodeReader = manager.createBarcodeReader();
                    claimScanner();
                }catch(Exception ex){
                    Log.e(TAG, "exception in manager.createBarcodeReader: "+ex.getMessage());
                }
            }
        });

    }
    private void claimScanner(){
        Log.d(TAG, "claimScanner");
        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
                barcodeReader.addBarcodeListener(this);
                barcodeReader.addTriggerListener(this);
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
            try {
                barcodeReader.setProperty(PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
            }catch (Exception ex){
                Toast.makeText(this, "PROPERTY_TRIGGER_CONTROL_MODE failed: "+ex.getMessage(), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void releaseScanner(){
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            // notifications while paused.
            try {
                barcodeReader.setProperty(PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            }catch (Exception ex){
                Toast.makeText(this, "PROPERTY_TRIGGER_CONTROL_MODE failed: "+ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            barcodeReader.release();
        }

    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        claimScanner();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        releaseScanner();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();

        if (barcodeReader != null) {
            // unregister barcode event listener
            barcodeReader.removeBarcodeListener(this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener(this);
            // close BarcodeReader to clean up resources.
            barcodeReader.close();
            barcodeReader = null;
        }

        if (manager != null) {
            // close AidcManager to disconnect from the scanner service.
            // once closed, the object can no longer be used.
            manager.close();
        }
        showSystemUI();
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent arg0) {
        Log.d(TAG, "onFailureEvent");
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(_context, "No data", Toast.LENGTH_SHORT).show();
            }
        });
        btnPressed=false;
        scannerEnabled=true;
        enableScanner(scannerEnabled);
    }

    // When using Automatic Trigger control do not need to implement the
    // onTriggerEvent function
    @Override
    public void onTriggerEvent(TriggerStateChangeEvent event) {
        Log.d(TAG, "onTriggerevent");
        if(scannerEnabled && event.getState()==true) //pressed?
            doScan();
//        try {
//            // only handle trigger presses
//            // turn on/off aimer, illumination and decoding
//            barcodeReader.aim(event.getState());
//            barcodeReader.light(event.getState());
//            barcodeReader.decode(event.getState());
//
//        } catch (ScannerNotClaimedException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Scanner is not claimed", Toast.LENGTH_SHORT).show();
//        } catch (ScannerUnavailableException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
//        }
    }

    @Override
    public void onBarcodeEvent(final BarcodeReadEvent event) {
        Log.d(TAG, "onBarcodeEvent: " + event.getBarcodeData());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
                txtScan.setText(event.getBarcodeData());
                txtAmount.setText("");
            }
        });
        btnPressed=false; //need to load that for next button press will start a scan
        scannerEnabled=false; //enable scanner only after OK has been pressed
        enableScanner(scannerEnabled);
    }

    private void enableScanner(boolean bEnable){
        if(barcodeReader!=null){
            try {
                if(bEnable)
                    barcodeReader.setProperty(PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_CLIENT_CONTROL);
                else
                    barcodeReader.setProperty(PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_DISABLE);
            }catch (Exception ex){
                Log.d(TAG, "exception in enableScanner: " + ex.getMessage());
            }
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        int keyCode=keyEvent.getKeyCode();
        if(keyEvent.getAction()==KeyEvent.ACTION_UP){

            if(keyCode==KEYCODE_ENTER){
                btnEnter.callOnClick();
                return true;
            }else if(keyCode==KEYCODE_ESCAPE){
                btnClr.callOnClick();
                return true;
            }
        }
        super.dispatchKeyEvent(keyEvent);
        return false;
    }

    @Override
    public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
        if(keyEvent.getAction()==KeyEvent.ACTION_UP){
            if(keyCode==KEYCODE_ENTER){
                btnClr.callOnClick();
                return true;
            }else if(keyCode==KEYCODE_ESCAPE){
                btnEnter.callOnClick();
                return true;
            }
        }
        return false;
    }
}
