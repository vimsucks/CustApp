package tk.vimsucks.custapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.SharedPreferencesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private CustStu stu;
    EditText currentWeekEditText;
    EditText weekEditText;
    Toolbar toolbar;
    //WeekView mWeekView;
    boolean isLogin = false;
    boolean isClassTableAcquired = false;
    boolean isExpeTableAcquired = false;
    SharedPreferences  accountPref;
    public Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        stu = new CustStu(this);
        initViews();;
        SharedPreferences  accountPref = getSharedPreferences("account", 0);
        if (accountPref.getBoolean("isLogged", false)) {
            setSupportActionBar(toolbar);
            final String username = accountPref.getString("username", "233");
            final String password = accountPref.getString("password", "233");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    isLogin = stu.login(username, password);
                    stu.getCurrentWeek(0);
                    isClassTableAcquired = stu.getClassTable();
                    isExpeTableAcquired = stu.getExpeTable();
                }
            }).start();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    private void initViews() {
        currentWeekEditText = (EditText)findViewById(R.id.current_edit_text);
        /*
        mWeekView = (WeekView)findViewById(R.id.week_view);
        mWeekView.setMonthChangeListener(new MonthLoader.MonthChangeListener() {
            @Override
            public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
                List<WeekViewEvent> events = new ArrayList<WeekViewEvent>();
                ArrayList<WeekViewEvent> newEvents = getNewEvents(newYear, newMonth);
                events.addAll(newEvents);
                return events;
            }
        });
        */
        weekEditText = (EditText)findViewById(R.id.week_edit_text);
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.week_minus_button) {
            EditText weekEditText = (EditText) findViewById(R.id.week_edit_text);
            Integer week = Integer.parseInt(weekEditText.getText().toString());
            if (week > 1) {
                --week;
                weekEditText.setText(String.valueOf(week));
            }
            if (isLogin) {
                stu.getCurrentWeek(week);
            } else {
                Message msg = new Message();
                msg.obj = "Please onClick first!";
                toastHandler.sendMessage(msg);
            }
        } else if (view.getId() == R.id.week_plus_button) {
            Integer week = Integer.parseInt(weekEditText.getText().toString());
            if (week < 20) {
                ++week;
                weekEditText.setText(String.valueOf(week));
            }
            if (isLogin) {
                stu.getCurrentWeek(week);
            } else {
                Message msg = new Message();
                msg.obj = "Please onClick first!";
                toastHandler.sendMessage(msg);
            }
        } else if (view.getId() == R.id.export_button) {
            if (!isLogin) {
                Message msg = new Message();
                msg.obj = "请先登录";
                toastHandler.sendMessage(msg);
                return;
            }
            if (!isClassTableAcquired) {
                Message msg = new Message();
                msg.obj = "未获取课表, 请重新点击登录";
                toastHandler.sendMessage(msg);
                return;
            }
            if (getPackageManager().PERMISSION_DENIED == getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, getPackageName())) {
                Message msg = new Message();
                msg.obj = "请赋予本APP写入日历的权限!";
                toastHandler.sendMessage(msg);
                if (Build.VERSION.SDK_INT >= 23) {
                    ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_CALENDAR}, 1);
                }
                return;
            }
            new Thread(new Runnable() {
                @Override
                public void run() {

                    String currentWeek = currentWeekEditText.getText().toString();
                    if (currentWeek.length() == 0) {
                        Message msg = new Message();
                        msg.obj = "请输入本周是第几周!!";
                        toastHandler.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.obj = "导入开始, 请稍等片刻...";
                        toastHandler.sendMessage(msg);
                        stu.getCurrentWeek(Integer.parseInt(currentWeek));
                        stu.writeCalendar();
                    }
                }
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        if (requestCode == 1)
        {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //onClick(exportButton);
            } else
            {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_export) {
            Intent intent = new Intent(MainActivity.this, ExportActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_exit) {
            SharedPreferences  accountPref = getSharedPreferences("account", 0);
            SharedPreferences.Editor editor = accountPref.edit();
            editor.remove("isLogged");
            // editor.remove("username");
            // editor.remove("password");
            editor.commit();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}

