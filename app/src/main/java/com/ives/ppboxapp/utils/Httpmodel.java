package com.ives.ppboxapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class Httpmodel {

    /**登录参数**/
    public static String loginparameters = "/api/v1/rbac/user/login";
    /**公司区域参数**/
    public static String areaparameters = "/api/v1/basic/area/list";
    /**提交参数**/
    public static String submitparameters ="/api/v1/basic/box/transfer";

    /**设置ip地址**/
    public static void sendipseeting(Context context, String ip){
        SharedPreferences sharedPref = context.getSharedPreferences("ipName",MODE_PRIVATE);
        SharedPreferences.Editor prefEditor = sharedPref.edit();
        prefEditor.putString("ipaddress",ip);
        prefEditor.commit();
    }
    /**获取ip地址**/
    public static String getipseeting(Context context){
        String result = "http://192.168.0.72:801";
        SharedPreferences sharedPreferences= context.getSharedPreferences("ipName", Context.MODE_PRIVATE);
        String timetext = sharedPreferences.getString("ipaddress","");
        if(!timetext.equals("")){
            return timetext;
        }
        return result;
    }





}
