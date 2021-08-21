package com.ives.ppboxapp.rfidutil;


import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.INVENTORY_STATE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RFIDReader;
import com.zebra.rfid.api3.ReaderDevice;
import com.zebra.rfid.api3.Readers;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.SESSION;
import com.zebra.rfid.api3.START_TRIGGER_TYPE;
import com.zebra.rfid.api3.STOP_TRIGGER_TYPE;
import com.zebra.rfid.api3.TriggerInfo;

import static com.zebra.rfid.api3.BEEPER_VOLUME.HIGH_BEEP;

public class RFIDREAD {

//    public static final String PROFILE1 = "Sacnner" ;

    //RFID
    public static Readers readers = null;
    public static ReaderDevice device = null;
    public static RFIDReader reader = null;

    //ASCII转化
    public static String hexToAscii(String str) {
        StringBuilder sb = new StringBuilder();
        if (!str.isEmpty()){
            for (int i = 0; i < str.length(); i += 2) {
                sb.append((char) Integer.parseInt(str.substring(i, i + 2), 16));
            }
        }
        return sb.toString();
    }


    public static void configureReader(RfidEventsListener rfidEventsListener) {
        if (reader == null || !reader.isConnected()) {
            return;
        }
        TriggerInfo triggerInfo = new TriggerInfo();
        triggerInfo.StartTrigger.setTriggerType(START_TRIGGER_TYPE.START_TRIGGER_TYPE_IMMEDIATE);
        triggerInfo.StopTrigger.setTriggerType(STOP_TRIGGER_TYPE.STOP_TRIGGER_TYPE_IMMEDIATE);
        try {
            reader.Events.addEventsListener(rfidEventsListener);
            reader.Events.setHandheldEvent(true);
            reader.Events.setTagReadEvent(true);
            reader.Events.setBatteryEvent(true);
            reader.Events.setPowerEvent(true);
            reader.Events.setTemperatureAlarmEvent(true);
            reader.Events.setAttachTagDataWithReadEvent(false);
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.BARCODE_MODE,false);
            reader.Config.setTriggerMode(ENUM_TRIGGER_MODE.RFID_MODE,true);
            reader.Config.setStartTrigger(triggerInfo.StartTrigger);
            reader.Config.setStopTrigger(triggerInfo.StopTrigger);
            Antennas.AntennaRfConfig config = null;
            reader.Config.setBeeperVolume(HIGH_BEEP);
            config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(297);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            Antennas.SingulationControl control = reader.Config.Antennas.getSingulationControl(1);
            control.setSession(SESSION.SESSION_S0);
            control.Action.setInventoryState(INVENTORY_STATE.INVENTORY_STATE_A);
            reader.Config.Antennas.setSingulationControl(1, control);
            reader.Actions.PreFilters.deleteAll();
        } catch (InvalidUsageException | OperationFailureException e) {
            e.printStackTrace();
        }

    }

    public static void cleanread() {
         readers = null;
         device = null;
         reader = null;

    }
}
