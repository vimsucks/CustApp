package com.vimsucks.custapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.vimsucks.custapp.R;
import com.vimsucks.custapp.util.CustStu;

import java.util.ArrayList;
import java.util.List;

import static com.vimsucks.custapp.MyApp.stu;

public class MainActivity extends AppCompatActivity {

    ProgressDialog progressDialog;
    public Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    progressDialog.setTitle(((ArrayList<String>)msg.obj).get(0));
                    progressDialog.setMessage(((ArrayList<String>)msg.obj).get(1));
                    progressDialog.setCancelable(false);
                    break;
                case 0:
                    progressDialog.hide();
                    break;
            }
        }
    };

    public void changeProgress(String title, String message) {
        List<String> lst = new ArrayList<>();
        lst.add(title);
        lst.add(message);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = lst;
        progressHandler.sendMessage(msg);
    }

    AlertDialog.Builder alertDialog;
    public Handler alertHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            alertDialog.setTitle(((ArrayList<String>)msg.obj).get(0));
            alertDialog.setMessage(((ArrayList<String>)msg.obj).get(1));
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("确认", null);
            alertDialog.show();
        }
    };

    public void changeAlert(String title, String message) {
        List<String> lst = new ArrayList<>();
        lst.add(title);
        lst.add(message);
        Message msg = new Message();
        msg.obj = lst;
        alertHandler.sendMessage(msg);
    }

    private static final String TAG = "MainActivity";

    EditText stuID;
    EditText password;
    CheckBox ifExportCls;
    CheckBox ifExportExpe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        alertDialog = new AlertDialog.Builder(MainActivity.this);
        stu = new CustStu(this);
        progressDialog = new ProgressDialog(MainActivity.this);
        if (getPackageManager().PERMISSION_DENIED == this.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, this.getPackageName())) {
            alertDialog.setTitle("权限请求");
            alertDialog.setMessage("本应用需要访问日历的权限以将课表写入日历，请赋予本应用权限");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialogInterface, int i) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_CALENDAR}, 1);
                    }
                }
            });
            alertDialog.show();
        }
    }


    private void initViews() {
        stuID = (EditText)findViewById(R.id.stuID);
        password = (EditText)findViewById(R.id.password);
        ifExportCls = (CheckBox)findViewById(R.id.if_export_cls);
        ifExportExpe = (CheckBox)findViewById(R.id.if_export_expe);
    }



    public void onClick(View view) {
        if (getPackageManager().PERMISSION_DENIED == this.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, this.getPackageName())) {
            Toast.makeText(MainActivity.this, "请赋予本应用写入日历的权限", Toast.LENGTH_LONG).show();
            return;
        }

        switch (view.getId()) {
            case R.id.sign_in_button :
                if (ifExportCls.isChecked() || ifExportExpe.isChecked()) {
                    changeProgress("导出中", "登录中");
                    progressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CustStu stu = new CustStu(MainActivity.this);
                            boolean temp = stu.login(stuID.getText().toString(), password.getText().toString());
                            if (temp) {
                                changeProgress("导出中", "正在获取课表");
                                temp = stu.getClassTable();
                                if (temp) {
                                    changeProgress("导出中", "正在获取实验表");
                                    temp = stu.getExpeTable();
                                    if (temp) {
                                        changeProgress("导出中", "正在获取当前周数");
                                        temp = stu.getCurrentWeek();
                                        if (temp) {
                                            changeProgress("导出中", "正在导出到日历");
                                            stu.writeSchedule(ifExportCls.isChecked(), ifExportExpe.isChecked());
                                        }
                                    }
                                }
                            }
                        }
                    }).start();
                } else {
                    changeAlert("选择错误", "请至少选择一项，课程或实验");
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_delete_calendar:
                if (getPackageManager().PERMISSION_DENIED == this.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, this.getPackageName())) {
                    Toast.makeText(MainActivity.this, "请赋予本应用写入日历的权限", Toast.LENGTH_LONG).show();
                    return false;
                }

                changeProgress("删除中", "正在删除先前导出的课表");
                progressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stu.deleteSchedule();
                        changeAlert("删除成功", "成功删除先前导出的课表");
                    }
                }).start();
                break;
            case R.id.action_exit:
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}

