package com.vimsucks.custapp.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.vimsucks.custapp.R;
import com.vimsucks.custapp.util.CustStu;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.vimsucks.custapp.MyApp.stu;

public class MainActivity extends AppCompatActivity {

    ProgressDialog exportProgressDialog;
    public Handler exportProgressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    exportProgressDialog.setTitle(((ArrayList<String>) msg.obj).get(0));
                    exportProgressDialog.setMessage(((ArrayList<String>) msg.obj).get(1));
                    exportProgressDialog.setCancelable(false);
                    break;
                case 0:
                    exportProgressDialog.hide();
                    break;
            }
        }
    };

    public Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_LONG).show();
        }
    };

    public Handler etCurrentWeekVisibilityHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            etCurrentWeek.setVisibility(msg.what);
        }
    };

    public void updateExportProgress(String title, String message) {
        List<String> lst = new ArrayList<>();
        lst.add(title);
        lst.add(message);
        Message msg = new Message();
        msg.what = 1;
        msg.obj = lst;
        exportProgressHandler.sendMessage(msg);
    }

    AlertDialog.Builder alertDialog;
    public Handler alertHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            alertDialog.setTitle(((ArrayList<String>) msg.obj).get(0));
            alertDialog.setMessage(((ArrayList<String>) msg.obj).get(1));
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

    public Handler directionsHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ArrayList<String> directions = (ArrayList<String>) msg.obj;
            AlertDialog.Builder alBuilder = new AlertDialog.Builder(MainActivity.this);
            alBuilder.setTitle("请选择专业方向");
            alBuilder.setIcon(R.drawable.ic_directions_green_500_24dp);
            alBuilder.setCancelable(false);
            final Spinner popupSpn = new Spinner(MainActivity.this, Spinner.MODE_DIALOG);
            LinearLayout layout = new LinearLayout(MainActivity.this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            layout.setPadding(40, 8, 40, 8);
            layout.addView(popupSpn);
            popupSpn.setLayoutParams(params);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, directions);
            popupSpn.setAdapter(adapter);
            alBuilder.setView(layout);
            alBuilder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    stu.setDirection(popupSpn.getSelectedItemPosition());
                    Log.d(TAG, stu.getDirection().toString());
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            stu.parseDirectionClasses();
                        }
                    }).start();
                }
            });
            alBuilder.create().show();
        }
    };

    private static final String TAG = MainActivity.class.getName();

    @BindView(R.id.etStudentID)
    EditText etStudentId;
    @BindView(R.id.etPassword)
    EditText etPassword;
    @BindView(R.id.etCurrentWeek)
    EditText etCurrentWeek;
    @BindView(R.id.cbExportCls)
    CheckBox cbExportCls;
    @BindView(R.id.cbExportExp)
    CheckBox cbExportExpe;
    @BindView(R.id.btnLogin)
    Button btnLogin;

    SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind views
        ButterKnife.bind(this);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        String username = sharedPref.getString(getString(R.string.saved_username), "");
        String password = sharedPref.getString(getString(R.string.saved_password), "");

        if (!username.isEmpty() && !password.isEmpty()) {
            etStudentId.setText(username);
            etPassword.setText(password);
        }

        alertDialog = new AlertDialog.Builder(MainActivity.this);
        stu = new CustStu(this);
        exportProgressDialog = new ProgressDialog(MainActivity.this);

        checkPermission();
    }

    public void checkPermission() {
        if (getPackageManager().PERMISSION_DENIED == this.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, this.getPackageName())) {
            alertDialog.setTitle("权限请求");
            alertDialog.setMessage("本应用需要访问日历的权限以将课表写入日历，请赋予本应用权限");
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface alertDialogInterface, int i) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_CALENDAR}, 1);
                    }
                }
            });
            alertDialog.show();
        }
    }

    @OnClick(R.id.btnLogin)
    public void onClick(View view) {
        if (getPackageManager().PERMISSION_DENIED == this.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, this.getPackageName())) {
            Toast.makeText(MainActivity.this, "请赋予本应用写入日历的权限", Toast.LENGTH_LONG).show();
            return;
        }

        if (checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CALENDAR},
                    0);
            return;
        }

        switch (view.getId()) {
            case R.id.btnLogin:
                if (cbExportCls.isChecked() || cbExportExpe.isChecked()) {
                    updateExportProgress("导出中", "登录中");
                    exportProgressDialog.show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            stu = new CustStu(MainActivity.this);
                            boolean loginSuccessfully = stu.login(etStudentId.getText().toString(), etPassword.getText().toString());
                            if (loginSuccessfully) {
                                // save username and password to shared preferences for next time use
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString(getString(R.string.saved_username), etStudentId.getText().toString());
                                editor.putString(getString(R.string.saved_password), etPassword.getText().toString());
                                editor.commit();

                                updateExportProgress("导出中", "正在获取课表");
                                boolean clsAcquired = stu.getClassTable();
                                if (clsAcquired) {
                                    updateExportProgress("导出中", "正在获取实验表");
                                    boolean expAcquired = stu.getExpeTable();
                                    if (expAcquired) {
                                        if (etCurrentWeek.getVisibility() == View.GONE) {
                                            updateExportProgress("导出中", "正在获取当前周数");
                                            boolean currentWeekAcquired = stu.getCurrentWeek();
                                            if (currentWeekAcquired) {
                                                updateExportProgress("导出中", "正在导出到日历");
                                                stu.writeSchedule(cbExportCls.isChecked(), cbExportExpe.isChecked());
                                            } else {
                                                Message msg = new Message();
                                                msg.what = 0;
                                                exportProgressHandler.sendMessage(msg);
                                                msg = new Message();
                                                msg.obj = "无法获取当前周数，请手动设置";
                                                toastHandler.sendMessage(msg);
                                                msg = new Message();
                                                msg.what = View.VISIBLE;
                                                etCurrentWeekVisibilityHandler.sendMessage(msg);
                                            }
                                        } else {
                                            stu.setCurrentWeek(Integer.valueOf(etCurrentWeek.getText().toString()));
                                            updateExportProgress("导出中", "正在导出到日历");
                                            stu.writeSchedule(cbExportCls.isChecked(), cbExportExpe.isChecked());
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

                updateExportProgress("删除中", "正在删除先前导出的课表");
                exportProgressDialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        stu.deleteSchedule();
                        changeAlert("删除成功", "成功删除先前导出的课表");
                    }
                }).start();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case 0:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onClick(btnLogin);
                }
        }
    }
}
