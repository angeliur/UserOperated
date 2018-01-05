package com.demo.useroperated;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.Date;

//参考文章：https://www.jianshu.com/p/9d4b4c13d770       http://blog.csdn.net/world_java/article/details/37886211
public class MainActivity extends AppCompatActivity {


    private ComponentName componentName;
    private DevicePolicyManager dpm;



    private long intervalTime = 5000;
    private Handler handler;
    private Date lastUpdateTime;
    private WindowManager.LayoutParams wl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //获取到窗口管理器，用来调节屏幕亮度
        wl = getWindow().getAttributes();
        handler = new Handler();
        lastUpdateTime = new Date(System.currentTimeMillis());
        dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        componentName = new ComponentName(this, DeviceReceiver.class);
        onActive();

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.postAtTime(r,intervalTime);
    }

    private void onActive() {
        if (!dpm.isAdminActive(componentName)){
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,componentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"激活设备管理器");
            startActivity(intent);
        }else {
            Log.i("tag","已激活设备管理器");
        }
    }

    private Runnable r = new Runnable() {
        @Override
        public void run() {
            Date timeNow = new Date(System.currentTimeMillis());
            long timePeriod = ((long)timeNow.getTime() - (long)lastUpdateTime.getTime()) / 1000;
            if (timePeriod > Constants.stillTime){
                //一段时间无操作自动退出
                finish();
            }else if (timePeriod > Constants.lockScreenTime){
                //一段时间无操作自动锁屏
                lockScreen();
            }else {
                if (timePeriod > Constants.darkenTime1){
                    wl.screenBrightness = 0f;
                    getWindow().setAttributes(wl);
                }else if (timePeriod > Constants.darkenTime2){
                    wl.screenBrightness = 0.6f;
                    getWindow().setAttributes(wl);
                }else {
                    wl.screenBrightness = 1f;
                    getWindow().setAttributes(wl);
                }
            }

            handler.postDelayed(r,intervalTime);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(r);
    }

    public void clickOperate(View v){
            AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.dialog);


        /*dialog.setContentView()方法调用，在设置标题，确定取消按钮就不会显示了*/

//            builder.setView(R.layout.dialog);
//            builder.setTitle("dialog");
//            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    Toast.makeText(MainActivity.this,"已点击",Toast.LENGTH_SHORT).show();
//                }
//            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });

            AlertDialog dialog = builder.create();
            dialog.show();
            dialog.setContentView(R.layout.dialog);//setContentView要在dialog.show()方法之后设置，否则布局不会显示



            //设置dialog宽度占满屏幕，并在底部显示
            Window window = dialog.getWindow();
            window.getDecorView().setPadding(0,0,0,0);
            window.setGravity(Gravity.BOTTOM);
            Display display = getWindowManager().getDefaultDisplay();
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = display.getWidth();
            window.setAttributes(attributes);


            updateUserActionTime();



    }

    private void lockScreen() {
        if (dpm.isAdminActive(componentName)){
            dpm.lockNow();
        }else {
            Toast.makeText(this,"请先激活设备管理器",Toast.LENGTH_SHORT).show();
        }
    }

    //更新用户操作时间
    private void updateUserActionTime() {
        lastUpdateTime.setTime(new Date(System.currentTimeMillis()).getTime());
    }
}
