package com.ives.ppboxapp;

import android.app.Activity;
import android.graphics.drawable.PaintDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ives.ppboxapp.adapter.FirstClassAdapter;
import com.ives.ppboxapp.adapter.SecondClassAdapter;
import com.ives.ppboxapp.model.Applicationentity;
import com.ives.ppboxapp.model.Company;
import com.ives.ppboxapp.model.FirstClassItem;
import com.ives.ppboxapp.rfidutil.RFIDREAD;
import com.ives.ppboxapp.rfidutil.ReadRfidListon;
import com.ives.ppboxapp.rfidutil.SumbitJson;
import com.ives.ppboxapp.utils.CustomToast;
import com.ives.ppboxapp.utils.Httpmodel;
import com.ives.ppboxapp.utils.MyLOg;
import com.ives.ppboxapp.utils.ScreenUtils;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;
import com.zebra.rfid.api3.Antennas;
import com.zebra.rfid.api3.ENUM_TRIGGER_MODE;
import com.zebra.rfid.api3.Events;
import com.zebra.rfid.api3.HANDHELD_TRIGGER_EVENT_TYPE;
import com.zebra.rfid.api3.InvalidUsageException;
import com.zebra.rfid.api3.OperationFailureException;
import com.zebra.rfid.api3.RfidEventsListener;
import com.zebra.rfid.api3.RfidReadEvents;
import com.zebra.rfid.api3.RfidStatusEvents;
import com.zebra.rfid.api3.STATUS_EVENT_TYPE;
import com.zebra.rfid.api3.TagData;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import static com.ives.ppboxapp.rfidutil.RFIDREAD.hexToAscii;
import static com.ives.ppboxapp.rfidutil.RFIDREAD.reader;


/**
 *
 * @author hanj
 *
 */
public class MainActivity extends Activity {
    private TextView provenance;
    private TextView destination;
    /**左侧一级分类的数据*/
    private List<FirstClassItem> firstList;
    /**右侧二级分类的数据*/
    private List<Company> secondList;
    /**使用PopupWindow显示一级分类和二级分类*/
    private PopupWindow popupWindow;

    /**左侧和右侧两个ListView*/
    private ListView leftLV, rightLV;
    /**弹出PopupWindow时背景变暗**/
     private View darkView;

    /**弹出PopupWindow时，背景变暗的动画**/
    private Animation animIn, animOut;

    private ListView boxlistview;
    private TextView readCount;
    private Button cleanbutton;
    private Button submitbutton;
    private Company provenancecompany = null;
    private Company destinationcompany =null;
    private boolean isRFIDRead = false;
    private int selectedCompany =0;
    private ReadRfidListon readRfidList = null;
    private List<String> list = null;

    private QMUITopBar topbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        topbar =findViewById(R.id.topbar);
        intotop();
        showLoad("正在载入中");
        findView();
        httppost(Httpmodel.getipseeting(this)+Httpmodel.areaparameters,"",0x001);
       /**点击**/
        OnClickListenerImpl l = new OnClickListenerImpl();
        provenance.setOnClickListener(l);
        destination.setOnClickListener(l);
    }

    private void findView() {
        provenance = (TextView) findViewById(R.id.provenance);
        destination = (TextView)findViewById(R.id.destination);
        darkView = findViewById(R.id.main_darkview);
        animIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);
        animOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_anim);
        boxlistview = findViewById(R.id.boxlistview);
        readCount= findViewById(R.id.readCount);
        cleanbutton =findViewById(R.id.cleanbutton);
        submitbutton = findViewById(R.id.submitbutton);
        list = new ArrayList<>();
        cleanbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (readRfidList !=null){
                    readCount.setText("000");
                    readRfidList.clear();
                }

            }
        });
        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (QMUIDisplayHelper.hasInternet(getApplicationContext())){
                    if (readRfidList !=null){
                        if (provenancecompany !=null && destinationcompany !=null){
                            if (provenancecompany.getId() != destinationcompany.getId()){
                                if (readRfidList.getCount()>0){
                                    showsure();
                                }else{
                                    CustomToast.showToast(getApplicationContext(),"无数据",3000);
                                }
                            }else{
                                CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
                            }
                        }else{
                            CustomToast.showToast(getApplicationContext(),"来源区域或目的区域不能为空",2000);
                        }
                    }else{
                        CustomToast.showToast(getApplicationContext(),"请读取RFID标签",3000);
                    }
                }else{
                    CustomToast.showToast(getApplicationContext(),"无网络",3000);
                }
            }
        });
    }


    int i =0;
    public void intotop(){
        topbar.addRightImageButton(R.mipmap.power,R.id.qq).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               showpower();

            }
        });

    }
    /**设置功率**/
    QMUIDialog.EditTextDialogBuilder builder = null;
    public void showpower() {
        builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("请输入功率(100-297)")
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .addAction(getString(R.string.cancel), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                        dialog.dismiss();
                    }
                })
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        CharSequence text = builder.getEditText().getText();
                        if ( text != null && text.length() > 0) {
                            int po = Integer.parseInt(String.valueOf(text));
                            if ( po>=100 && po <=297){
                                power(po);
                                dialog.dismiss();
                            }else{
                                CustomToast.showToast(getApplicationContext(),"错误的数值",3000);
                            }
                        }else{
                            CustomToast.showToast(getApplicationContext(),"功率不能为空",3000);
                        }
                    }
                })
                .show();
        builder.setCancelable(false);
    }
    public void power(int power){
        Antennas.AntennaRfConfig config = null;
        try {
            config = reader.Config.Antennas.getAntennaRfConfig(1);
            config.setTransmitPowerIndex(power);
            config.setrfModeTableIndex(0);
            config.setTari(0);
            reader.Config.Antennas.setAntennaRfConfig(1, config);
            Antennas.SingulationControl control = reader.Config.Antennas.getSingulationControl(1);
            reader.Config.Antennas.setSingulationControl(1, control);
        } catch (InvalidUsageException e) {
            e.printStackTrace();
        } catch (OperationFailureException e) {
            e.printStackTrace();
        }

    }
    /**提交**/
    public void submititem(){
                showLoad("上传中");
                SumbitJson sumbitJson = new SumbitJson();
                sumbitJson.setCreate_id(Applicationentity.handlerspid);
                sumbitJson.setOrigin_area_id(provenancecompany.getId());
                sumbitJson.setDest_area_id(destinationcompany.getId());
                sumbitJson.setRfid(readRfidList.getitems());
                sumbitJson.setTotal_amount(readRfidList.getCount());
                JSONObject jsonObject = (JSONObject) JSONObject.toJSON(sumbitJson);
                Log.d("jsonsubmititem",sumbitJson.toString());
                httppost(Httpmodel.getipseeting(this)+Httpmodel.submitparameters, JSONObject.toJSONString(jsonObject),0x002);
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

    /**区域赋值**/
    private void initData(List<Company> networkdata) {
        firstList = new ArrayList<FirstClassItem>();
        for (int i= 1;i<=networkdata.size();i++) {
            if (networkdata.get(i-1).getPid()==0){
                ArrayList<Company> secondList1 = new ArrayList<Company>();
                for (Company data :networkdata) {
                    if (data.getPid() == networkdata.get(i-1).getId() && data.getPid()!=0){
                        secondList1.add(data);
                    }
                }
                firstList.add(new FirstClassItem(networkdata.get(i-1).getId(), networkdata.get(i-1).getName(), secondList1));
            }
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        startReadRFID();
        if (reader != null ){
            if (!reader.isConnected()){
                try {
                    reader.connect();
                    RFIDREAD.configureReader(rfidread);
                } catch (InvalidUsageException e) {
                    //e.printStackTrace();
                } catch (OperationFailureException e) {
                   // e.printStackTrace();
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
        cancel();
        super.onStop();
    }

    class OnClickListenerImpl implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.provenance:
                    if (popupWindow !=null) {
                        selectedCompany= 1;
                        tab1OnClick();
                    }else{
                        CustomToast.showToast(getApplicationContext(),"无数据",3000);
                    }
                    break;
                case R.id.destination:
                    if (popupWindow !=null) {
                        selectedCompany= 2;
                        tab1OnClick();
                    }else{
                        CustomToast.showToast(getApplicationContext(),"无数据",3000);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void initPopup() {
        popupWindow = new PopupWindow(this);
        View view = LayoutInflater.from(this).inflate(R.layout.popup_layout, null);
        leftLV = (ListView) view.findViewById(R.id.pop_listview_left);
        rightLV = (ListView) view.findViewById(R.id.pop_listview_right);
        popupWindow.setContentView(view);
        popupWindow.setBackgroundDrawable(new PaintDrawable());
        popupWindow.setFocusable(true);
        popupWindow.setHeight(ScreenUtils.getScreenH(this) * 2 / 3);
        popupWindow.setWidth(ScreenUtils.getScreenW(this));
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                darkView.startAnimation(animOut);
                darkView.setVisibility(View.GONE);
                leftLV.setSelection(0);
                rightLV.setSelection(0);
            }
        });
        //为了方便扩展，这里的两个ListView均使用BaseAdapter.如果分类名称只显示一个字符串，建议改为ArrayAdapter.
        //加载一级分类
        final FirstClassAdapter firstAdapter = new FirstClassAdapter(this, firstList);
        leftLV.setAdapter(firstAdapter);
        //加载左侧第一行对应右侧二级分类
        secondList = new ArrayList<Company>();
        secondList.addAll(firstList.get(0).getSecondList());
        final SecondClassAdapter secondAdapter = new SecondClassAdapter(this, secondList);
        rightLV.setAdapter(secondAdapter);

        //左侧ListView点击事件
        leftLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //二级数据
                List<Company> list2 = firstList.get(position).getSecondList();
                //如果没有二级类，则直接跳转
                if (list2 == null || list2.size() == 0) {
                   // popupWindow.dismiss();
                    int firstId = firstList.get(position).getId();
                    String selectedName = firstList.get(position).getName();
                   // handleResult(firstId, -1, selectedName);
                    CustomToast.showToast(getApplicationContext(),"请选择具体区域",3000);
                    return;
                }

                FirstClassAdapter adapter = (FirstClassAdapter) (parent.getAdapter());
                //如果上次点击的就是这一个item，则不进行任何操作
                if (adapter.getSelectedPosition() == position){
                    return;
                }

                //根据左侧一级分类选中情况，更新背景色
                adapter.setSelectedPosition(position);
                adapter.notifyDataSetChanged();

                //显示右侧二级分类
                updateSecondListView(list2, secondAdapter);
            }
        });

        //右侧ListView点击事件
        rightLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //关闭popupWindow，显示用户选择的分类
               // popupWindow.dismiss();
                int firstPosition = firstAdapter.getSelectedPosition();
                int firstId = firstList.get(firstPosition).getId();
                String firstIdname = firstList.get(firstPosition).getName();
                int secondId = firstList.get(firstPosition).getSecondList().get(position).getId();
                String selectedName = firstList.get(firstPosition).getSecondList().get(position)
                        .getName();
                handleResult(firstId,firstIdname, secondId, selectedName);
            }
        });
    }

    //顶部第一个标签的点击事件
    private void tab1OnClick() {
        if (popupWindow.isShowing()) {
            popupWindow.dismiss();
        } else {
            popupWindow.showAsDropDown(findViewById(R.id.main_div_line));
            popupWindow.setAnimationStyle(-1);
            //背景变暗
            darkView.startAnimation(animIn);
            darkView.setVisibility(View.VISIBLE);
        }
    }

    //刷新右侧ListView
    private void updateSecondListView(List<Company> list2, SecondClassAdapter secondAdapter) {
        secondList.clear();
        secondList.addAll(list2);
        secondAdapter.notifyDataSetChanged();
    }

    //处理点击结果
    private void handleResult(int firstId,String firstIdname , int secondId, String selectedName){
      //  String text = "first id:" + firstId + ",second id:" + secondId;
        //Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
        choseAreas(firstId, firstIdname ,secondId,selectedName);
//        if (provenancecompany != null && destinationcompany != null ){
//            isRFIDRead = true;
//        }
    }


    /**选择区域**/

    public void choseAreas(int firstId, String firstIdname ,int secondId, String selectedName){

        /**第1个**/
        if (selectedCompany == 1){
//            if (destinationcompany != null){
//                if (destinationcompany.getId() != secondId){
//                    popupWindow.dismiss();
//                    provenancecompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//                    provenance.setText(firstList.get(firstId-1).getName()+selectedName);
//                }else{
//                    CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
//                }
//            }else{
                popupWindow.dismiss();
                provenancecompany = new Company(secondId,selectedName,firstId);
                provenance.setText(firstIdname+selectedName);
           // }
        }
        /**第2个**/
        if (selectedCompany == 2){
//            if (provenancecompany !=null){
//                if (provenancecompany.getId()!= secondId){
//                    popupWindow.dismiss();
//                    destinationcompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//                    destination.setText(firstList.get(firstId-1).getName()+selectedName);
//                }else{
//                    CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
//                }
//            }else{
                popupWindow.dismiss();
                destinationcompany = new Company(secondId,selectedName,firstId);
                destination.setText(firstIdname+selectedName);
           // }
        }


    }
    QMUIDialog showDialog =null;

    public void showsure(){
        //   isRFIDRead = false;
        showDialog =  new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提醒")
                .setMessage("是否确认转移")
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        submititem();
                        dialog.dismiss();
                    }
                }).show();
        showDialog.setCancelable(false);
    }

    public void showtip(String data){
        //   isRFIDRead = false;
        showDialog =  new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("提醒")
                .setMessage(data)
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                        dialog.dismiss();
                    }
                }).show();
        showDialog.setCancelable(false);
    }
    public void showfind(final String rfidString){
     //   isRFIDRead = false;
        showDialog =  new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("转移成功")
                .setMessage("转移单号:"+rfidString)
                .addAction(getString(R.string.ok), new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        cleanti();
                        dialog.dismiss();
                    }
                }).show();
        showDialog.setCancelable(false);
    }


    public void cleanti(){
        provenancecompany = null;
        destinationcompany = null;
        readRfidList.clear();
        readCount.setText("000");
        provenance.setText("");
        destination.setText("");

    }
    //Post
    private JSONObject supplierjson;
    private String errorstring;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x001:
                    if (supplierjson.getString("data") !=null){
                       // Log.d("JSON",supplierjson.getString("data"));
                        List<Company> list = JSON.parseArray(supplierjson.getString("data"), Company.class);
                        initData(list);
                        initPopup();
                    }
                    if(tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    break;

                case 0x002:
                    if ("200".equals(supplierjson.getString("code"))){
                        if (supplierjson.getString("data") !=null){
                            //  Log.d("JSON",supplierjson.getString("data"));
                            showfind(supplierjson.getString("data"));
                        }
                    }else{
                        showtip(supplierjson.getString("message"));
                    }

                    if(tipDialog.isShowing()){
                        tipDialog.dismiss();
                    }
                    break;
                case 0x005:
                    CustomToast.showToast(getApplicationContext(), "服务器加载错误", 1500);
                    break;
                case 0x006:
                    MyLOg.e("submit","连接超时");
                    CustomToast.showToast(getApplicationContext(),"连接超时",1000);
                    break;
                case 0x007:
                    // if (tipDialog.isShowing())tipDialog.dismiss();
                    CustomToast.showToast(getApplicationContext(),"连接错误",1000);
                    break;

                case 0x99:
                    CustomToast.showToast(getApplicationContext(),errorstring,1000);
                    break;
                default:
                    break;
            }
        }
    };

    public void httppost(final String url, final String parments , final int i)  {
        final RequestBody body = RequestBody.create(MediaType.parse("application/json"),parments);
        Request request = new Request.Builder().url(url)
                .post(body)
                .build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if(e instanceof SocketTimeoutException){//判断超时异常
                    Log.e("time","连接超时");
                    handler.sendEmptyMessage(0x006);
                }
                if(e instanceof ConnectException){//判断连接异常，我这里是报Failed to connect to 10.7.5.144
                    Log.e("erro","连接错误");
                    handler.sendEmptyMessage(0x007);
                }
            }
            @Override
            public void onResponse(Call call, Response response)  {
                if (response.code() == 200) {
                    try {
                        String s = response.body().string();
                      //  Log.d("JSON",JSONObject.parseObject(s).toJSONString());
                        if (!s.isEmpty()){
                            supplierjson = JSONObject.parseObject(s);
                            handler.sendEmptyMessage(i);
                        }else{
                            handler.sendEmptyMessage(0x005) ;
                        }
                    } catch (IOException e) {
                        errorstring = e.getMessage();
                    }
                }
                if (response.code() != 200){
                    try {
                        Log.d("JSON11",response.body().string());
                        errorstring =response.body().string();
                    } catch (IOException e) {

                    }


                    handler.sendEmptyMessage(0x005);
                }
            }
        });
        if (tipDialog !=null){
            if(tipDialog.isShowing()){
                tipDialog.dismiss();
            }
        }

    }



    //RFID
    AsyncTask<Void, Void, Boolean> task = null;
    AsyncTask<Void, Void, Void> taskPerform = null;
    AsyncTask<Void, Void, Void> taskStop = null;
    OnlineRFID rfidread = new OnlineRFID();
    private void startReadRFID() {
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
                    RFIDREAD.reader.Events.removeEventsListener(LoginActivity.eventHandler);
                    RFIDREAD.reader.Events.addEventsListener(rfidread);
                    success = true;
                } catch (InvalidUsageException e) {
                    // e.printStackTrace();
                    MyLOg.e("RFIDOnline",e.getMessage());
                } catch (OperationFailureException e) {
                    //Log.d("TAG", "doInBackground: " + e.getVendorMessage());
                    // e.printStackTrace();
                }
                return success;
            }
        };
        task.execute();
    }
    //取消
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
                    RFIDREAD.reader.Events.removeEventsListener(rfidread);
                    RFIDREAD.reader.Events.addEventsListener(LoginActivity.eventHandler);
                    success = true;
                } catch (InvalidUsageException e) {
                    // e.printStackTrace();

                } catch (OperationFailureException e) {
                    //Log.d("end", "onCancelled: " + e.getVendorMessage());
                    // e.printStackTrace();
                }
                return success;
            }
        };
        task.execute();
    }
    class OnlineRFID implements RfidEventsListener {

        @Override
        public void eventReadNotify(RfidReadEvents rfidReadEvents) {
            TagData[] tags = RFIDREAD.reader.Actions.getReadTags(100);
            if (tags == null) {
                return;
            }else{
                for (final TagData tag  : tags) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (tag.getTagID().length()>4){
                                    if (tag.getTagID().substring(0,4).equalsIgnoreCase("5b40")){
                                        select(hexToAscii(tag.getTagID().substring(4)).replaceAll("@",""));
                                    }
                                }
                            }
                        });
                }

            }
        }

        private void select(String tagName) {
                gotTag(tagName);

        }

        //读取标签信息
        private void gotTag(final String tagid) {
                    if (readRfidList == null) {
                       // list.add(new RfidView(tagid, 1));
                        list.add(tagid);
                        readRfidList = new ReadRfidListon(list, MainActivity.this);
                        boxlistview.setAdapter(readRfidList);
                        if (String.valueOf(readRfidList.getCount()).length()>3){
                            readCount.setText(String.valueOf(readRfidList.getCount()));
                        }else{
                            readCount.setText(addZeroForNummain(String.valueOf(readRfidList.getCount()),3));
                        }

                        readRfidList.sound();
                    } else {
                        readRfidList.additem(tagid);
                        if (String.valueOf(readRfidList.getCount()).length()>3){
                            readCount.setText(String.valueOf(readRfidList.getCount()));
                        }else{
                            readCount.setText(addZeroForNummain(String.valueOf(readRfidList.getCount()),3));
                        }
                    }

        }

        /**
         * 前补FB
         * 最后 FF
         * @param str
         * @param strLength
         * @return
         */
        public  String addZeroForNummain(String str, int strLength) {
            int strLen = str.length();
            StringBuffer sb = null;
            while (strLen < strLength) {
                sb = new StringBuffer();
                sb.append("0").append(str);// 左补0
                str = sb.toString();
                strLen = str.length();
            }
            return str;
        }
        @Override
        public void eventStatusNotify(RfidStatusEvents rfidStatusEvents) {
            Events.StatusEventData data = rfidStatusEvents.StatusEventData;
            STATUS_EVENT_TYPE type = data.getStatusEventType();
            // Log.d("STATUS", type.toString());
            if (type == STATUS_EVENT_TYPE.BATTERY_EVENT) {
                // int battery = data.BatteryData.getLevel();
                // Log.d("BatteryData", String.valueOf(battery));
            } else if (type == STATUS_EVENT_TYPE.TEMPERATURE_ALARM_EVENT) {

            } else if (type == STATUS_EVENT_TYPE.POWER_EVENT){
//                float io =  data.PowerData.getPower();
//                //Log.d("Power", String.valueOf(io));

            } else if (type == STATUS_EVENT_TYPE.HANDHELD_TRIGGER_EVENT) {
                HANDHELD_TRIGGER_EVENT_TYPE eventType = data.HandheldTriggerEventData.getHandheldEvent();
                if (eventType == HANDHELD_TRIGGER_EVENT_TYPE.HANDHELD_TRIGGER_PRESSED) {
                    if (taskPerform != null) {
                        taskPerform.cancel(true);
                    }
                    taskPerform = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            try {

                                    RFIDREAD.reader.Actions.Inventory.perform();

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
                                RFIDREAD.reader.Actions.Inventory.stop();
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


//    /**第1个**/
//        if (provenancecompany == null && destinationcompany == null && selectedCompany == 1) {
//        popupWindow.dismiss();
//        provenancecompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//        provenance.setText(firstList.get(firstId-1).getName()+selectedName);
//    }
//
//    /**第2个**/
//            if(provenancecompany != null && destinationcompany == null &&  selectedCompany == 2) {
//        if (provenancecompany.getId()!= secondId){
//            popupWindow.dismiss();
//            destinationcompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//            destination.setText(firstList.get(firstId-1).getName()+selectedName);
//        }else{
//            CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
//        }
//    }else
//            /** 第3个 **/
//            if (provenancecompany != null && destinationcompany != null ){
//        if (selectedCompany == 1){
//            if (destinationcompany.getId() != secondId){
//                popupWindow.dismiss();
//                provenancecompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//                provenance.setText(firstList.get(firstId-1).getName()+selectedName);
//            }else{
//                CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
//            }
//        }
//        if (selectedCompany == 2){
//            if (provenancecompany.getId()!= secondId){
//                popupWindow.dismiss();
//                destinationcompany = new Company(secondId,selectedName,firstList.get(firstId-1).getId());
//                destination.setText(firstList.get(firstId-1).getName()+selectedName);
//            }else{
//                CustomToast.showToast(getApplicationContext(),"不能选择相同地点",3000);
//            }
//        }
//    }
}