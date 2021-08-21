package com.ives.ppboxapp.rfidutil;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.util.Calendar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ives.ppboxapp.LoginActivity;
import com.ives.ppboxapp.utils.MyLOg;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TriggerInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.zebra.rfid.api3.BEEPER_VOLUME.HIGH_BEEP;

public class Applicationfile {


    public static String getsavacoscentor(Context context){
        SharedPreferences sharedPreferences= context.getSharedPreferences("data", Context.MODE_PRIVATE);
        String costCenterdata=sharedPreferences.getString("costCenterdata","");
        if (!"".equals(costCenterdata)){
            return costCenterdata;
        }
        return "";
    }
    /**
     * 读Internal中文件的方法
     *
     * @param filePathName 文件路径及文件名
     * @return 读出的字符串
     * @throws IOException
     */
    public static String readInternal(String filePathName) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        // 打开文件输入流
        FileInputStream fileInputStream = new FileInputStream(filePathName);
        byte[] buffer = new byte[1024];
        int len = fileInputStream.read(buffer);
        // 读取文件内容
        while (len > 0) {
            stringBuffer.append(new String(buffer, 0, len));
            // 继续将数据放到buffer中
            len = fileInputStream.read(buffer);
        }
        // 关闭输入流
        fileInputStream.close();
        return stringBuffer.toString();
    }


    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     * @author jqlin
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 前补[@
     * 最后 @@
     * @param str
     * @param strLength
     * @return
     */
    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength) {
            sb = new StringBuffer();
            //sb.append("0").append(str);// 左补0
             sb.append(str).append("@");//右补0
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }


    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
               // MyLOg.e("--Method--", "Copy_Delete.deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                MyLOg.d("失败","删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            MyLOg.e("失败","删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }



    //RFD设备设置
    public static void configureReader(RfidEventsListener rf) {
        if (RFIDREAD.reader == null || !RFIDREAD.reader.isConnected()) {
            return;
        }
        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
        try {
            RFIDREAD.reader.Events.addEventsListener(rf);
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
    /**
     * 隐藏软键盘
     * @param context
     * @param v
     * @return
     */
    public static Boolean hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
        return false;
    }
    /**
     * 获取当前时间
     * 可根据需要自行截取数据显示 // HH:mm:ss
     * @return
     */
    public static String getTimes() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
    public static String getTagASCIIID(String data) {
        String str = "[@" + addZeroForNum(data,14);
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            sb.append(Integer.toHexString((int) c));
        }
        return sb.toString();
    }


    //Scan
    public void setScan(boolean is, Context context){
        Intent dwIntent = new Intent();
        dwIntent.setAction("com.symbol.datawedge.api.ACTION");
//  Enable
        if (is) {
            dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "ENABLE_PLUGIN");
        }
//  or Disable
        if (!is) {
            dwIntent.putExtra("com.symbol.datawedge.api.SCANNER_INPUT_PLUGIN", "DISABLE_PLUGIN");
        }
        context.sendBroadcast(dwIntent);
    }
}
