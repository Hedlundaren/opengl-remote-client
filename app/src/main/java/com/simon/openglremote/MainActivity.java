package com.simon.openglremote;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity implements SocketManager.SocketListener, SensorEventListener {

    public Button button_connect;
    private SocketManager mSocketManager;
    private SensorManager mSensorManager;
    private Sensor mRotation;
    private EditText editText_ipAddress;
    private TextView textView_status;
    private Point p1, p2, p3;
    private Point p_translate, p_rotate, p_scale;
    private ImageView touchPad;
    private SeekBar sb_color_r, sb_color_g, sb_color_b, sb_opacity, sb_stiffness, sb_brushsize;
    private int send_type;
    private double oldPositionX, oldPositionY, oldPositionZ;
    private double currPositionX, currPositionY, currPositionZ;
    private double oldRotationX, oldRotationY, oldRotationZ;
    private double currRotationX, currRotationY, currRotationZ;

    // Buttons
    private Button button_send1, button_send2, button_send3, button_draw_texture;
    private boolean texture_draw = false;

    private int TRANSLATION = 0,
            ROTATION = 1,
            SCALE = 2,
            CLOSE = 3,
            TRANSLATION_DRAG = 4,
            ROTATION_DRAG = 5,
            TEXTURE_PAINTING = 6,
            BRUSH_COLOR_R = 7,
            BRUSH_COLOR_G = 8,
            BRUSH_COLOR_B = 9,
            BRUSH_OPACITY = 10,
            BRUSH_STIFFNESS = 11,
            BRUSH_SIZE = 12;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send_type = SCALE;


        oldPositionX = 0.0f;
        oldPositionY = 0.0f;
        oldPositionZ = 0.0f;

        oldRotationX = 0.0f;
        oldRotationY = 0.0f;
        oldRotationZ = 0.0f;

        currPositionX = 0.0f;
        currPositionY = 0.0f;
        currPositionZ = 0.0f;

        currRotationX = 0.0f;
        currRotationY = 0.0f;
        currRotationZ = 0.0f;

        sb_color_r = (SeekBar) findViewById(R.id.slider_color_r);
        sb_color_g = (SeekBar) findViewById(R.id.slider_color_g);
        sb_color_b = (SeekBar) findViewById(R.id.slider_color_b);
        sb_opacity = (SeekBar) findViewById(R.id.slider_opacity);
        sb_stiffness = (SeekBar) findViewById(R.id.slider_stiffness);
        sb_brushsize = (SeekBar) findViewById(R.id.slider_brushsize);

        sb_color_r.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.MULTIPLY));
        sb_color_g.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.GREEN, PorterDuff.Mode.MULTIPLY));
        sb_color_b.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.MULTIPLY));
        sb_opacity.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        sb_stiffness.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));
        sb_brushsize.getProgressDrawable().setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY));

        touchPad = (ImageView) findViewById(R.id.touchPad);
        p1 = new Point(0,0);
        p2 = new Point(0,0);
        p3 = new Point(0,0);
        p_translate = new Point(0,0);
        p_rotate = new Point(0,0);
        p_scale = new Point(0,0);
        mSocketManager = new SocketManager(this);
        mSocketManager.setIpAddress("NTUSECURE");

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_FASTEST);

        editText_ipAddress = (EditText)findViewById(R.id.editText_ipAddress);
        editText_ipAddress.setSelection(editText_ipAddress.getText().length());

        textView_status = (TextView) findViewById(R.id.textView_status);

        sb_color_r.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage( BRUSH_COLOR_R + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        sb_color_g.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage(BRUSH_COLOR_G + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        sb_color_b.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage(BRUSH_COLOR_B + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        sb_opacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage(BRUSH_OPACITY + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        sb_stiffness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage(BRUSH_STIFFNESS + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        sb_brushsize.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSocketManager.sendMessage(BRUSH_SIZE + ";" + (float) progress / 100.0f);
            }
            public void onStartTrackingTouch(SeekBar seekBar) { }
            public void onStopTrackingTouch(SeekBar seekBar) { }

        });

        touchPad.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                if (editText_ipAddress.isFocused()) {
                    editText_ipAddress.clearFocus();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                int action = event.getActionMasked();

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    p1 = new Point((int) event.getX(), (int) event.getY());
                    return true;
                }

                if(action == MotionEvent.ACTION_MOVE){
                    p2 = new Point((int) event.getX(), (int) event.getY());
                    if(send_type == TRANSLATION){ // translate
                        p3 = new Point( p2.x - p1.x + p_translate.x, p1.y - p2.y + p_translate.y);
                        mSocketManager.sendMessage( TRANSLATION_DRAG + ";" + p3.x + "," + p3.y);
                    }else if(send_type == ROTATION){ // rotate
                        p3 = new Point( p2.x - p1.x + p_rotate.x, p1.y - p2.y + p_rotate.y);
                        mSocketManager.sendMessage( ROTATION_DRAG + ";" + p3.x + "," + p3.y);
                    }else if(send_type == SCALE){ // scale
                        p3 = new Point( p2.x - p1.x + p_scale.x, p1.y - p2.y + p_scale.y);
                        mSocketManager.sendMessage( SCALE + ";" + p3.x + "," + p3.y);
                    }
                    return true;
                }

                if(action == MotionEvent.ACTION_UP) {
                    if(send_type == TRANSLATION){ // translate
                        p_translate = p3;

                    }else if(send_type == ROTATION){ // rotate
                        p_rotate = p3;

                    }else if(send_type == SCALE){ // scale
                        p_scale = p3;
                    }

                    return true;
                }

                    return false;
            }
        });

        touchPad.setOnDragListener(new View.OnDragListener() {
            public boolean onDrag(View v, DragEvent event) {

                Log.d("Main", "Action");

                return false;
            }
        });

        editText_ipAddress.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(editText_ipAddress.getText().toString().equals("NTUSECURE")){
                    editText_ipAddress.setText("EDUROAM");
                    mSocketManager.setIpAddress("EDUROAM");
                }
                else{
                    editText_ipAddress.setText("NTUSECURE");
                    mSocketManager.setIpAddress("NTUSECURE");
                }
                editText_ipAddress.setSelection(editText_ipAddress.getText().length());
            }
        });

        button_connect = (Button) findViewById(R.id.button_connect);
        button_connect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSocketManager.connectToServer();
                updateOldTransforms();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //Sync client and server
                        syncClient();
                    }
                }, 1000);
            }
        });

        final Button button_close = (Button) findViewById(R.id.button_close);
        button_close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSocketManager.sendMessage( CLOSE + ";1");
                setStatus("Goodbye");
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
        });

        button_draw_texture = (Button) findViewById(R.id.button_draw_texture);
        button_draw_texture.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSocketManager.sendMessage(TEXTURE_PAINTING + ";1");

                texture_draw = !texture_draw;

                if(texture_draw){
                    button_draw_texture.setTextColor(Color.GREEN);
                }else{
                    button_draw_texture.setTextColor(Color.WHITE);
                }
            }
        });

        button_send1 = (Button) findViewById(R.id.button_send1);
        button_send1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = TRANSLATION;
                setActiveButton(button_send1);
                updateOldTransforms();
            }
        });

        button_send2 = (Button) findViewById(R.id.button_send2);
        button_send2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = ROTATION;
                setActiveButton(button_send2);
                updateOldTransforms();
            }
        });

        button_send3 = (Button) findViewById(R.id.button_send3);
        button_send3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = SCALE;
                setActiveButton(button_send3);
                updateOldTransforms();
            }
        });

        sb_opacity.setProgress(100);
        sb_color_g.setProgress(100);
        sb_brushsize.setProgress(20);
    }

    private void syncClient(){

        final Handler h1 = new Handler();
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_COLOR_R + ";" + (float) sb_color_r.getProgress() / 100);  }}, 100);
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_COLOR_G + ";" + (float) sb_color_g.getProgress() / 100);  }}, 100);
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_COLOR_B + ";" + (float) sb_color_b.getProgress() / 100);  }}, 100);
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_OPACITY + ";" + (float) sb_opacity.getProgress() / 100);  }}, 100);
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_STIFFNESS + ";" + (float) sb_stiffness.getProgress() / 100);  }}, 100);
        h1.postDelayed(new Runnable() { public void run() { mSocketManager.sendMessage(BRUSH_SIZE + ";" + (float) sb_brushsize.getProgress() / 100);  }}, 100);
        button_draw_texture.setTextColor(Color.WHITE);
        setActiveButton(button_send3);
    }

    private void setActiveButton(Button btn){
        button_send1.setTextColor(Color.WHITE);
        button_send2.setTextColor(Color.WHITE);
        button_send3.setTextColor(Color.WHITE);

        btn.setTextColor(Color.GREEN);
    }

    private void updateOldTransforms(){
        oldPositionX = currPositionX;
        oldPositionY = currPositionY;
        oldPositionZ = currPositionZ;

        oldRotationX = currRotationX;
        oldRotationY = currRotationY;
        oldRotationZ = currRotationZ;
    }

    public String getIP(){
        return editText_ipAddress.getText().toString();
    }

    public void setStatus(String status){
        textView_status.setText(status);
    }

    public void setConnected(){
        button_connect.setText("CLOSE");
    }

    @Override
    public void OnSocketEvent(int event) {
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    public void onSensorChanged(SensorEvent event) {

        String message = "";
        double v0, v1, v2;
        v0 = event.values[0];
        v1 = event.values[1];
        v2 = event.values[2];
        currPositionX = v0;
        currPositionY = v1;
        currPositionZ = v2;
        currRotationX = v0;
        currRotationY = v1;
        currRotationZ = v2;

        Sensor sensor = event.sensor;
        if (sensor.getType() == mRotation.getType()) {

            float sv0 = (float) (v0 - oldPositionX);
            float sv1 = (float) (v1 - oldPositionY);
            float sv2 = (float) (v2 - oldPositionZ);
            message =  ROTATION + ";" + sv0 + "," + sv1  + "," + sv2 ;
            mSocketManager.sendMessage(message);

        }else if(false && sensor.getType() == mRotation.getType() && send_type == 2){

            float sv0 = (float) (v0 - oldRotationX);
            float sv1 = (float) (v1 - oldPositionY);
            float sv2 = (float) (v2 - oldPositionZ);
            message =  ROTATION + ";" + sv0 + "," + sv1  + "," + sv2 ;
            mSocketManager.sendMessage(message);
        }


    }

}
