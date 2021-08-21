package com.ives.ppboxapp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ives.ppboxapp.rfidutil.Applicationfile;
import com.ives.ppboxapp.rfidutil.FindBar;
import com.ives.ppboxapp.rfidutil.RFIDREAD;
import com.ives.ppboxapp.utils.CustomToast;
import com.ives.ppboxapp.utils.SoundPoolHelper;
import com.zebra.rfid.api3.BEEPER_VOLUME;
import com.zebra.rfid.api3.ENUM_TRANSPORT;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.TagData;

import java.util.List;

import static com.ives.ppboxapp.rfidutil.RFIDREAD.device;
import static com.ives.ppboxapp.rfidutil.RFIDREAD.reader;
import static com.ives.ppboxapp.rfidutil.RFIDREAD.readers;

public class FindOneActivity extends AppCompatActivity {


    private static final String TAG = "FindActivity";
    Button buttonCancel = null;
    EditText editText = null;
    FindBar findBar = null;

    AsyncTask<Void, Void, Boolean> task = null;
    AsyncTask<Void, Void, Void> taskPerform = null;
    AsyncTask<Void, Void, Void> taskStop = null;
    class LocatingEventHandler implements RfidEventsListener {
        FindOneActivity context = null;
        LocatingEventHandler(FindOneActivity context) {
            this.context = context;
        }
        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {
            final TagData[] tags = RFIDREAD.reader.Actions.getReadTags(100);
            if (tags != null) {
                for (TagData tag : tags) {
                    if (tag != null) {
                        if (tag.isContainsLocationInfo()) {
                            final short distance = tag.LocationInfo.getRelativeDistance();
                            //Log.d("iiiii",String.valueOf(distance));
                            soundPoolHelper.playlooking("happy1",false,(distance/100f));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    context.findBar.set(distance / 100f);
                                }
                            });
                            break;
                        }
                    }
                }
            }
        }

        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Events.StatusEventData data = rfidStatusEvents.StatusEventData;
            STATUS_EVENT_TYPE type = data.getStatusEventType();
            if (type == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                HANDHELD_TRIGGER_EVENT_TYPE eventType = data.HandheldTriggerEventData.getHandheldEvent();
                if (eventType == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    if (taskPerform != null) {
                        taskPerform.cancel(true);
                    }
                    taskPerform = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                              //  Log.d("TAGtttt", editText.getText().toString().toUpperCase());

                               // Log.d(TAG, Applicationfile.getTagASCIIID(editText.getText().toString()));
                                RFIDREAD.reader.Actions.TagLocationing.Perform(Applicationfile.getTagASCIIID(editText.getText().toString().toUpperCase()), null, null);
                            } catch (InvalidUsageException | OperationFailureException e) {
                                // e.printStackTrace();
                                // MyLOg.e("pickstart：",e.getMessage());
                            }
                            return null;
                        }
                    };
                    taskPerform.execute();
                } else if (eventType == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_RELEASED) {
                    if (taskStop != null) {
                        taskStop.cancel(true);
                    }
                    taskStop = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {
                                RFIDREAD.reader.Actions.TagLocationing.Stop();
                            } catch (InvalidUsageException | OperationFailureException e) {
                                // e.printStackTrace();
                                //   MyLOg.e("pickstop：",e.getMessage());

                            }
                            return null;
                        }
                    };
                    taskStop.execute();
                }
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        cancel();
        finish();
    }
    LocatingEventHandler handler = new LocatingEventHandler(this);
    private SoundPoolHelper soundPoolHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_one);
        editText = findViewById(R.id.editText);
        editText.setTransformationMethod(replacementTransformationMethod);
        findBar = findViewById(R.id.findBar);
        buttonCancel = findViewById(R.id.button);
        soundPoolHelper = new SoundPoolHelper(4, SoundPoolHelper.TYPE_MUSIC)
                .setRingtoneType(SoundPoolHelper.RING_TYPE_MUSIC)
                //加载默认音频，因为上面指定了，所以其默认是：RING_TYPE_MUSIC
                //happy1,happy2
                .loadDefault(this)
                .load(this, "happy1", R.raw.duka3);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("StaticFieldLeak")
            @Override
            public void onClick(View v) {
                cancel();
                finish();
            }

        });
        startLocating();
    }


    private ReplacementTransformationMethod replacementTransformationMethod = new ReplacementTransformationMethod() {
        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            return cc;
        }
    };

    /**取消RFID**/
    private void cancel(){
        if (task != null) {
            task.cancel(true);
        }
        task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                if (isCancelled()) {
                    return false;
                }
                if (RFIDREAD.reader == null) {
                    return false;
                }
                if (!RFIDREAD.reader.isConnected()) {
                    return false;
                }
                boolean success = false;
                try {
                    RFIDREAD.reader.Events.removeEventsListener(handler);
                    RFIDREAD.reader.Events.addEventsListener(LoginActivity.eventHandler);
                    success = true;
                } catch (InvalidUsageException e) {
                    e.printStackTrace();
                } catch (OperationFailureException e) {
                    Log.d(TAG, "onCancelled: " + e.getVendorMessage());
                    e.printStackTrace();
                }
                return success;
            }
        };
        task.execute();
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (reader != null ){
            if (!reader.isConnected()){
                try {
                    reader.connect();
                    RFIDREAD.configureReader(handler);
                } catch (InvalidUsageException e) {
                   // e.printStackTrace();
                } catch (OperationFailureException e) {
                    //e.printStackTrace();
                }
            }else{
                try {
                    RFIDREAD.reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true);
                    RFIDREAD.reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE,false);
                } catch (InvalidUsageException e) {

                } catch (OperationFailureException e) {

                }
            }

        }


    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    @SuppressLint("StaticFieldLeak")
    private void startLocating() {
        if (task != null) {
            task.cancel(true);
        }
        Log.d(TAG, "startLocating: ");
        task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                if (isCancelled()) {
                    return false;
                }
                if (RFIDREAD.reader == null) {
                    return false;
                }
                if (!RFIDREAD.reader.isConnected()) {
                    return false;
                }
                boolean success = false;
                try {
                    RFIDREAD.reader.Events.removeEventsListener(LoginActivity.eventHandler);
                    RFIDREAD.reader.Events.addEventsListener(handler);
                    success = true;
                } catch (InvalidUsageException e) {
                } catch (OperationFailureException e) {
                    Log.d(TAG, "doInBackground: " + e.getVendorMessage());
                }
                return success;
            }
        };
        task.execute();
    }

    @Override
    protected void onDestroy() {
        if (task != null) {
            task.cancel(true);
        }
        soundPoolHelper.release();
        super.onDestroy();
    }
}