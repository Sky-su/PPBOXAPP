package com.ives.ppboxapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ives.ppboxapp.model.Applicationentity;
import com.ives.ppboxapp.model.Handlers;
import com.ives.ppboxapp.rfidutil.RFIDREAD;
import com.ives.ppboxapp.utils.CustomToast;
import com.ives.ppboxapp.utils.Httpmodel;
import com.ives.ppboxapp.utils.MyLOg;
import com.ives.ppboxapp.utils.SoundPoolHelper;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TriggerInfo;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.ives.ppboxapp.rfidutil.RFIDREAD.device;
import static com.ives.ppboxapp.rfidutil.RFIDREAD.readers;
import static java.util.concurrent.TimeUnit.SECONDS;

public class LoginActivity extends AppCompatActivity {




    private EditText loginusername;
    private EditText loginpassword;
    private Button loginbutton;
    private SoundPoolHelper soundPoolHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestPermission();
        soundPoolHelper = new SoundPoolHelper(4, SoundPoolHelper.TYPE_MUSIC)
                .setRingtoneType(SoundPoolHelper.RING_TYPE_MUSIC)
                //加载默认音频，因为上面指定了，所以其默认是：RING_TYPE_MUSIC
                //happy1,happy2
                .loadDefault(this)
                .load(this, "happy1", R.raw.duka3);
        init();
        loginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    public void login() {
        if (QMUIDisplayHelper.hasInternet(this)){
            if (!loginusername.getText().toString().isEmpty() && !loginpassword.getText().toString().isEmpty()){
                showLoad("登录中...");
                String data = Httpmodel.getipseeting(this)+Httpmodel.loginparameters+"?account="+loginusername.getText().toString()+"&password="+loginpassword.getText().toString();
                httplogin(data,0x001);
            }else{
                CustomToast.showToast(getApplicationContext(),"用户名或密码不能为空",3000);
            }
        }else{
            CustomToast.showToast(getApplicationContext(),"无网络",3000);
        }
    }
    /**
     * 文本提示
     */
    QMUITipDialog tipDialog = null;
    public void showLoad(String data){
        tipDialog= new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord(data)
                .create();
        tipDialog.show();
    }
    //实例化
    private void init(){
        loginusername = (EditText) findViewById(R.id.login_user);
        loginpassword = (EditText)findViewById(R.id.login_PASSword);
        loginbutton =  findViewById(R.id.loginButton);
//        setupProgressDialog();
//        setupLoadReaderTask();
//        setupStatusMonitorTimer();
    }

    //Post
    private JSONObject supplierjson;
    private String errorstring;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x001:
                    if (supplierjson.getString("data")!=null){
                        List<Handlers> list = JSON.parseArray("["+supplierjson.getString("data")+"]", Handlers.class);
                        Applicationentity.handlerspid = list.get(0).getId();
                        MyLOg.d("id",String.valueOf( Applicationentity.handlerspid));
                        Intent intent = new Intent(getApplicationContext(),ChooseActivity.class);
                        startActivity(intent);
                    }else{
                        CustomToast.showToast(getApplicationContext(),supplierjson.getString("message"),3000);
                    }
                    if (tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    break;
                case 0x005:
                    if (tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    CustomToast.showToast(getApplicationContext(), "服务器加载错误", 1500);
                    break;
                case 0x006:
                    MyLOg.e("submit","服务器连接超时");
                    if (tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    CustomToast.showToast(getApplicationContext(),"服务器连接超时,请检查网络",1000);
                    break;
                case 0x007:
                    MyLOg.e("submit","连接错误");
                    if (tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    CustomToast.showToast(getApplicationContext(),"连接错误,请检查网络",1000);
                    break;
                case 0x99:
                    CustomToast.showToast(getApplicationContext(),errorstring,1000);
                    break;
                default:
                    break;
            }
        }
    };


    /**
     * httpget请求
     * @param url
     * @param i
     * @throws IOException
     */
    public void httplogin(final String url, final int i)  {
        Request request = new Request.Builder().url(url)
                .get()
                .build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(e instanceof SocketTimeoutException){//判断超时异常
                    Log.e("logintime","连接超时");
                    handler.sendEmptyMessage(0x006);
                }
                if(e instanceof ConnectException){//判断连接异常，我这里是报Failed to connect to 10.7.5.144
                    Log.e("loginerro","连接错误");
                    handler.sendEmptyMessage(0x007);
                }
            }
            @Override
            public void onResponse(Call call, Response response)  {
                if (response.code() == 200) {
                    try {
                        String s = response.body().string();
                        if (!s.isEmpty()){
                            //MyLOg.d("MOVE",s);
                            supplierjson = JSONObject.parseObject(s);
                            handler.sendEmptyMessage(i);
                        }else{
                            handler.sendEmptyMessage(0x005) ;
                        }
                    } catch (IOException e) {
                        errorstring = e.getMessage();
                        MyLOg.d("login",e.getMessage());
                    }
                }
                if (response.code() != 200){
                    handler.sendEmptyMessage(0x005);
                }
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        tipDialog = null;

    }

    //APP 权限
    private int REQUEST_PERMISSION_CODE = 1000;
    String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET};
    private android.app.AlertDialog alertDialog;
    private void requestPermission() {
        if (Build.VERSION.SDK_INT > 23) {
            if (ContextCompat.checkSelfPermission(this,
                    permissions[0])
                    == PackageManager.PERMISSION_GRANTED) {
                //授予权限
                Log.i("requestPermission:", "用户之前已经授予了权限！");
            } else {
                //未获得权限
                Log.i("requestPermission:", "未获得权限，现在申请！");
                requestPermissions(permissions
                        , REQUEST_PERMISSION_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 42) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i("onPermissionsResult:", "权限" + permissions[0] + "申请成功");
                // permission was granted, yay! Do the
                // contacts-related task you need to do.

            } else {
                Log.i("onPermissionsResult:", "用户拒绝了权限申请");
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("permission")
                        .setMessage("点击允许才可以使用我们的app哦")
                        .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                if (alertDialog != null && alertDialog.isShowing()) {
                                    alertDialog.dismiss();
                                }
                                ActivityCompat.requestPermissions(LoginActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                            }
                        });
                alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
        }
    }



    //RFID
    AsyncTask<Void, String, String> task = null;
    ScheduledExecutorService scheduler = null;
    ScheduledFuture<?> taskHandler = null;
    public static RfidEventHandler eventHandler = null;
    ProgressDialog progressDialog = null;
    //连接信息
    private void setupProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
        }
        progressDialog.setMessage(getString(R.string.connecting));
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
    }
    private void setupStatusMonitorTimer() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newScheduledThreadPool(1);
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    if (RFIDREAD.reader != null) {
                        RFIDREAD.reader.Config.getDeviceStatus(true, true, true);
                    } else {
                        scheduler.shutdown();
                    }
                } catch (InvalidUsageException | OperationFailureException e) {
                    if (e instanceof OperationFailureException) {
                        Log.d("TAG", "OperationFailureException: " + ((OperationFailureException) e).getVendorMessage());
                    }
                    e.printStackTrace();
                }
            }
        };
        taskHandler = scheduler.scheduleAtFixedRate(task, 10, 60, SECONDS);
    }
    boolean isstarted = false;
    public void setupLoadReaderTask() {
        if (task != null) {
            task.cancel(true);
        }
        if (readers == null) {
            readers = new Readers(this, ENUM_TRANSPORT.ALL);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
        task = new AsyncTask<Void, String, String>() {
            @Override
            protected synchronized String doInBackground(Void... voids) {
                InvalidUsageException hj = null;
                if (isCancelled()) {
                    return null;
                }
                if (readers == null) {
                    return null;
                }
                publishProgress("readers.GetAvailableRFIDReaderList()");
                if (isCancelled()) {
                    return null;
                }
                List<ReaderDevice> list = null;
                try {
                    list = readers.GetAvailableRFIDReaderList();
                } catch (InvalidUsageException e) {
                    // e.printStackTrace();
                    hj = e;
                }
                if (hj != null){
                    readers.Dispose();
                    readers = null;
                    if (readers == null) {
                        readers = new Readers(getApplicationContext(), ENUM_TRANSPORT.BLUETOOTH);
                    }
                }
                if (list == null || list.isEmpty()) {
                    return null;
                }
                publishProgress("device.getRFIDReader()");
                if (isCancelled()) {
                    return null;
                }
                for (ReaderDevice readerDevice : list) {
                    device = readerDevice;
                    //Log.d("setupLoadReaderTask", device.getName());
                    RFIDREAD.reader = device.getRFIDReader();
                    // Log.d("地址：",device.getAddress());
                    if (RFIDREAD.reader.isConnected()) {
                        return null;
                    }
                    publishProgress("reader.connect()");
                    if (isCancelled()) {
                        return null;
                    }
                    try {
                        RFIDREAD.reader.connect();
                        configureReader();
                    } catch (InvalidUsageException | OperationFailureException e) {
                        // e.printStackTrace();
                    }
                    if (RFIDREAD.reader.isConnected()) {
                        break;
                    }
                }
                if (!RFIDREAD.reader.isConnected()) {
                    return null;
                }
                if (device.getName().contains("RFD8500")) {
                    try {
                        RFIDREAD.reader.Config.setBeeperVolume(BEEPER_VOLUME.QUIET_BEEP);
                    } catch (InvalidUsageException e) {
                        //  e.printStackTrace();
                    } catch (OperationFailureException e) {
                        // e.printStackTrace();
                    }
                }
                soundPoolHelper.playlooking("happy1", false,0.5f);
                return String.format("Connected to %s", device.getName());
            }
            @Override
            protected void onProgressUpdate(String... values) {
                // if (values.length == 0) return;
                // String s = null;
                // for (String value : values) s = value;
                // Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
            }

            @Override
            protected void onPostExecute(String s) {
                if (s == null) {
                    if (isstarted == false){
                        setupLoadReaderTask();
                        isstarted = true;
                    }else{
                        setupRetryDialog();
                    }
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            protected void onCancelled() {
                RFIDREAD.reader = null;
                readers = null;
                //Toast.makeText(getApplicationContext(), "Connection Cancelled", Toast.LENGTH_SHORT).show();
                CustomToast.showToast(getApplicationContext(),"Connection Cancelled",2000);
            }
        };
        task.execute();
    }
    //RFD设备设置
    private void configureReader() {
        if (RFIDREAD.reader == null || !RFIDREAD.reader.isConnected()) {
            return;
        }
        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
        if (eventHandler == null) {
            eventHandler = new RfidEventHandler();
        }
        try {
            RFIDREAD.reader.Events.addEventsListener(eventHandler);
            RFIDREAD.reader.Events.setHandheldEvent(true);
            RFIDREAD.reader.Events.setTagReadEvent(true);
            RFIDREAD.reader.Events.setBatteryEvent(false);
            RFIDREAD.reader.Events.setPowerEvent(true);
            RFIDREAD.reader.Events.setTemperatureAlarmEvent(true);
            RFIDREAD.reader.Events.setAttachTagDataWithReadEvent(false);
            RFIDREAD.reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE,false);
            RFIDREAD.reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true);
            RFIDREAD.reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            RFIDREAD.reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            Antennas.AntennaRfConfig config = null;
            config = RFIDREAD.reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(297);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            RFIDREAD.reader.Config.Antennas.setAntennaRfConfig(1, config);
            Antennas.SingulationControl control = RFIDREAD.reader.Config.Antennas.getSingulationControl(1);
            control.setSession(SESSION.SESSION_S0);
            control.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            RFIDREAD.reader.Config.Antennas.setSingulationControl(1, control);
            //int id = config.getTransmitPowerIndex();
            //intoPower(String.valueOf(id));
            RFIDREAD.reader.Actions.PreFilters.deleteAll();
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        } // Log.d("OperationFailureException", e.getVendorMessage());

    }
    private void setupRetryDialog() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        new AlertDialog.Builder(this)
                .setTitle(R.string.err_title)
                .setMessage(R.string.retry)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setupLoadReaderTask();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel(true);
        }
        if (RFIDREAD.reader != null) {
            if (device.getName().contains("RFD8500")) {
                try {
                    RFIDREAD.reader.Config.setBeeperVolume(BEEPER_VOLUME.HIGH_BEEP);
                } catch (InvalidUsageException e) {
                    // e.printStackTrace();
                } catch (OperationFailureException e) {
                    // e.printStackTrace();
                }
            }
            task = new AsyncTask<Void, String, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        RFIDREAD.reader.disconnect();
                    } catch (InvalidUsageException | OperationFailureException e) {
                        // e.printStackTrace();

                    }
                    return null;
                }
            };
            task.execute();
        }
        if (readers != null) {
            readers.Dispose();
        }
        RFIDREAD.cleanread();
    }

    public class RfidEventHandler implements RfidEventsListener {
        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {

        }
        @SuppressLint("StaticFieldLeak")
        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {

            Events.StatusEventData data = rfidStatusEvents.StatusEventData;
            STATUS_EVENT_TYPE type = data.getStatusEventType();
            // Log.d("STATUS", type.toString());
            if (type == STATUS_EVENT_TYPE.BATTERY_EVENT) {
                int battery = data.BatteryData.getLevel();
                // Log.d("BatteryData", String.valueOf(battery));
//                if (battery <= 30) {
//                    handler.sendEmptyMessage(0x002);
//                }
            }
        }
    }
}