package com.ives.ppboxapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ImageButton;

import com.ives.ppboxapp.utils.Buttononcilk;
import com.ives.ppboxapp.utils.CustomToast;
import com.ives.ppboxapp.utils.Httpmodel;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

public class ChooseActivity extends AppCompatActivity {


    /**容器转移**/
    private ImageButton movebutton;

    private ConstraintLayout movebuttonCon;
    /**容器寻找**/
    private ConstraintLayout findButtonCon;

    private ImageButton findButton;
    /**设置**/
    private ImageButton settingsbutton;
    private ConstraintLayout settingsbuttonCon;
    private Intent intent =null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        initialize();
        movebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Buttononcilk.isFastClick()&& intent ==null){
                    move();
                }
            }
        });
        movebuttonCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Buttononcilk.isFastClick()&& intent ==null){
                    move();
                }
            }
        });


        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Buttononcilk.isFastClick()&& intent ==null){
                    finditem();
                }
            }
        });
        findButtonCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Buttononcilk.isFastClick()&& intent ==null){
                    finditem();
                }
            }
        });


        settingsbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Buttononcilk.isFastClick()&& intent ==null){
                    settingPdate();
                }
            }
        });
        settingsbuttonCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Buttononcilk.isFastClick() && intent ==null){
                    settingPdate();
                }

            }
        });

    }


    public void initialize(){
        movebutton = findViewById(R.id.moveButton);
        movebuttonCon = findViewById(R.id.MoveButtonConla);
        findButton = findViewById(R.id.findgoodsButton);
        findButtonCon = findViewById(R.id.findButtonCon);
        settingsbutton = findViewById(R.id.settingButton);
        settingsbuttonCon = findViewById(R.id.settingButtonCon);
    }
    /** 容器转移**/
    public void move(){
        if (QMUIDisplayHelper.hasInternet(this)) {
            intent = new Intent(this,MainActivity.class);
            startActivity(intent);
        }else{
            CustomToast.showToast(getApplicationContext(),"无网络",3000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        intent =null;
    }

    /**寻找容器**/
    public void finditem(){
        intent = new Intent(this,FindOneActivity.class);
        startActivity(intent);

    }

    /**设置**/
    public void settingPdate(){
        String url = Httpmodel.getipseeting(this);
        showURl(url);
    }

    QMUIDialog.EditTextDialogBuilder builder = null;
    public void showURl(String s) {
        builder = new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle(getString(R.string.deviceURl))
                .setDefaultText(s)
                .setPlaceholder(getString(R.string.DeviceURlhit))
                .setInputType(InputType.TYPE_CLASS_TEXT)
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
                        if (text != null && text.length() > 0) {
                         Httpmodel.sendipseeting(getApplicationContext(),String.valueOf(text));
                            dialog.dismiss();
                        }
                    }
                })
                .show();
        builder.setCancelable(false);
    }

}