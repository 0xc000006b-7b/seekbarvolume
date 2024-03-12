package com.example.seekbarvolume;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.annotation.Nullable;


public class WidgetService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_PHONE;
        PowerManager manager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //LAYOUT_FLAG

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            LAYOUT_FLAG = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }

        mFloatingView = LayoutInflater.from(this).inflate(R.layout.layout_widget, null );
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                LAYOUT_FLAG,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        layoutParams.gravity = Gravity.TOP | Gravity.CENTER;
        //layoutParams.x = 0;
        //layoutParams.y = 200;

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        windowManager.addView(mFloatingView, layoutParams);
        mFloatingView.setVisibility(View.VISIBLE);

        View collapseView = mFloatingView.findViewById(R.id.collapse_view);
        View expandedView = mFloatingView.findViewById(R.id.expanded_container);

        ImageView closeButton = mFloatingView.findViewById(R.id.close_btn);
        closeButton.setOnClickListener(v -> stopSelf());


        //DRAG
        height = windowManager.getDefaultDisplay().getHeight();
        width = windowManager.getDefaultDisplay().getWidth();

        mFloatingView.setOnTouchListener(new View.OnTouchListener() {
            int initialX, initialY;
            float initialTouchX, initialTouchY;

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                switch (motionEvent.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        initialX = layoutParams.x;
                        initialY = layoutParams.y;

                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        //break;
                        return  true;
                    case MotionEvent.ACTION_UP:

                        //layoutParams.x = initialX+(int) (initialTouchX-motionEvent.getRawX());
                        //layoutParams.y = initialY+(int) (motionEvent.getRawY()-initialTouchY);
                        int Xdiff = (int) (motionEvent.getRawX() - initialTouchX);
                        int Ydiff = (int) (motionEvent.getRawY() - initialTouchY);


                        //The check for Xdiff <10 && YDiff< 10 because sometime elements moves a little while clicking.
                        //So that is click event.
                        if (Xdiff < 10 && Ydiff < 10) {
                            if (isViewCollapsed()) {
                                //When user clicks on the image view of the collapsed layout,
                                //visibility of the collapsed layout will be changed to "View.GONE"
                                //and expanded view will become visible.
                                collapseView.setVisibility(View.GONE);
                                expandedView.setVisibility(View.VISIBLE);
                            }
                        }


                        return  true;
                    case MotionEvent.ACTION_MOVE:
                        //calculate x y coordinates
                        layoutParams.x = initialX+ (int)(motionEvent.getRawX()-initialTouchX);
                        layoutParams.y = initialY+ (int)(motionEvent.getRawY()-initialTouchY);

                        //update layout with new coordinates
                        windowManager.updateViewLayout(mFloatingView, layoutParams);
                        return  true;
                }



                return false;
            }
        });




        volumeSeekbar = mFloatingView.findViewById(R.id.seekBar2);
        volumeSeekbar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeSeekbar.setProgress(audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC));

        volumeSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newVolume, boolean b) {
                //textview.setText("Media Volume : " + newVolume);
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_PLAY_SOUND);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        exitButton = mFloatingView.findViewById(R.id.exitWidget);
        exitButton.setOnClickListener(v -> {

            collapseView.setVisibility(View.VISIBLE);
            expandedView.setVisibility(View.GONE);
            /*if(mFloatingView!=null)
            {
                windowManager.removeView(mFloatingView);
                stopSelf();
            }*/
        });




    }

    private boolean isViewCollapsed() {
        return mFloatingView == null || mFloatingView.findViewById(R.id.collapse_view).getVisibility() == View.VISIBLE;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFloatingView != null) windowManager.removeView(mFloatingView);
    }



    int LAYOUT_FLAG;
    View mFloatingView;
    WindowManager windowManager;
    Button screenButton, exitButton;
    SeekBar volumeSeekbar = null;
    AudioManager audioManager = null;
    String tagg = "bye";
    Context context;
    float width, height;

}
