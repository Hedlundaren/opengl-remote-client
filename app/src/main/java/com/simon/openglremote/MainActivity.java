package com.simon.openglremote;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
    private int send_type;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        send_type = 1;

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
                    if(send_type == 1){ // translate
                        p3 = new Point( p2.x - p1.x + p_translate.x, p1.y - p2.y + p_translate.y);
                        mSocketManager.sendMessage("5;" + p3.x + "," + p3.y);
                    }else if(send_type == 2){ // rotate
                        p3 = new Point( p2.x - p1.x + p_rotate.x, p1.y - p2.y + p_rotate.y);
                        mSocketManager.sendMessage("6;" + p3.x + "," + p3.y);
                    }else if(send_type == 3){ // scale
                        p3 = new Point( p2.x - p1.x + p_scale.x, p1.y - p2.y + p_scale.y);
                        mSocketManager.sendMessage("3;" + p3.x + "," + p3.y);
                    }
                    return true;
                }

                if(action == MotionEvent.ACTION_UP) {
                    if(send_type == 1){ // translate
                        p_translate = p3;

                    }else if(send_type == 2){ // rotate
                        p_rotate = p3;

                    }else if(send_type == 3){ // scale
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
            }
        });

        final Button button_send1 = (Button) findViewById(R.id.button_send1);
        button_send1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = 1;
            }
        });

        final Button button_send2 = (Button) findViewById(R.id.button_send2);
        button_send2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = 2;
            }
        });

        final Button button_send3 = (Button) findViewById(R.id.button_send3);
        button_send3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                send_type = 3;
            }
        });

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
        Log.d("MainActivity", "OnSocketEvent");
    }

    protected void onResume() {
        super.onResume();


    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d("onAccuracyChanged", "accuracy: " + accuracy);
    }

    public void onSensorChanged(SensorEvent event) {

        String message = "";
        double v0, v1, v2;
        v0 = event.values[0];
        v1 = event.values[1];
        v2 = event.values[2];

        Sensor sensor = event.sensor;
        if (sensor.getType() == mRotation.getType() && send_type == 1) {
            message =  "1;" + v0 + "," + v1 + "," + v2;
            mSocketManager.sendMessage(message);
        }else if(sensor.getType() == mRotation.getType() && send_type == 2){
            message =  "2;" + v0 + "," + v1 + "," + v2;
            mSocketManager.sendMessage(message);
        }


    }
}
