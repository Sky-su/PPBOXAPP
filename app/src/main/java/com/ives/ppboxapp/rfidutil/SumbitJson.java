package com.ives.ppboxapp.rfidutil;

import java.util.Date;
import java.util.List;

public class SumbitJson {
    private int type;
    private int origin_area_id;
    private int dest_area_id;
    private int total_amount;
    private List<String> rfid;
    private int create_id;

    public SumbitJson() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }



    public int getOrigin_area_id() {
        return origin_area_id;
    }

    public void setOrigin_area_id(int origin_area_id) {
        this.origin_area_id = origin_area_id;
    }

    public int getDest_area_id() {
        return dest_area_id;
    }

    public void setDest_area_id(int dest_area_id) {
        this.dest_area_id = dest_area_id;
    }

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }

    public List<String> getRfid() {
        return rfid;
    }

    public void setRfid(List<String> rfid) {
        this.rfid = rfid;
    }




    public int getCreate_id() {
        return create_id;
    }

    public void setCreate_id(int create_id) {
        this.create_id = create_id;
    }

    @Override
    public String toString() {
        return "SumbitJson{" +
                "type=" + type +
                ", origin_area_id=" + origin_area_id +
                ", dest_area_id=" + dest_area_id +
                ", total_amount=" + total_amount +
                ", rfid=" + rfid +
                ", create_id=" + create_id +
                '}';
    }
}
