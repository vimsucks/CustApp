package tk.vimsucks.custapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private CustStu stu;
    Button startButton;
    ScrollView outputScrollView;
    TextView outputTextView;
    EditText usernameEditText;
    EditText passwordEditText;
    EditText currentWeekEditText;
    EditText weekEditText;
    Button exportButton;
    boolean isLogin;
    private Handler outputHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            outputTextView.append((String) msg.obj + "\n");
            // 在TextView的append后面马上调用fullScroll, 无法滚动到真正的底部
            // 因为Android下很多函数都是基于消息的，用消息队列来保证同步，所以函数调用多数是异步操作的。
            // 有消息队列是异步的，消息队列先滚动到底部，然后TextView的append方法显示。所以无法正确滚动到底部。
            if (outputScrollView != null) {
                outputScrollView.post(new Runnable() {
                    public void run() {
                        outputScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        }
    };
    private Handler outputClearHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            outputTextView.setText("");
        }
    };
    public Handler makeToast = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        stu = new CustStu(this);
        initViews();;
    }

    private void initViews() {
        startButton = (Button) findViewById(R.id.start_button);
        outputScrollView = (ScrollView)findViewById(R.id.output_scroll_view);
        outputTextView = (TextView)findViewById(R.id.output_text_view);
        outputTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        usernameEditText = (EditText)findViewById(R.id.username_edit_text);
        passwordEditText = (EditText)findViewById(R.id.password_edit_text);
        currentWeekEditText = (EditText)findViewById(R.id.current_edit_text);
        weekEditText = (EditText)findViewById(R.id.week_edit_text);
        exportButton = (Button)findViewById(R.id.export_button);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.start_button) {
            new Thread(new Runnable() {

                private void outputMsg(String obj) {
                    Message msg = new Message();
                    msg.obj = obj;
                    outputHandler.sendMessage(msg);
                }

                @Override
                public void run() {
                    EditText weekEditText = (EditText) findViewById(R.id.week_edit_text);
                    Integer week = Integer.parseInt(weekEditText.getText().toString());
                    if (stu.login(usernameEditText.getText().toString(), passwordEditText.getText().toString())) {
                        stu.getCurrentWeek(week);
                        stu.getClassTable();
                        stu.getWeekClassTable();
                        outputClearHandler.sendMessage(new Message());
                        stu.updateClassOutput(outputHandler);
                        startButton.setClickable(false);
                        exportButton.setClickable(true);
                        isLogin = true;
                    } else {
                        outputMsg("Login failed");
                    }
                }
            }).start();

            // Auto scroll to the bottom when text is updated
            View currentView = this.getCurrentFocus();
            if (currentView != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
            }
        } else if (view.getId() == R.id.week_minus_button) {
            EditText weekEditText = (EditText) findViewById(R.id.week_edit_text);
            Integer week = Integer.parseInt(weekEditText.getText().toString());
            if (week > 1) {
                --week;
                weekEditText.setText(String.valueOf(week));
            }
            if (isLogin) {
                stu.getCurrentWeek(week);
                stu.getWeekClassTable();
                outputClearHandler.sendMessage(new Message());
                stu.updateClassOutput(outputHandler);
            } else {
                Message msg = new Message();
                msg.obj = "Please onClick first!";
                outputHandler.sendMessage(msg);
            }
        } else if (view.getId() == R.id.week_plus_button) {
            Integer week = Integer.parseInt(weekEditText.getText().toString());
            if (week < 20) {
                ++week;
                weekEditText.setText(String.valueOf(week));
            }
            if (isLogin) {
                stu.getCurrentWeek(week);
                stu.getWeekClassTable();
                outputClearHandler.sendMessage(new Message());
                stu.updateClassOutput(outputHandler);
            } else {
                Message msg = new Message();
                msg.obj = "Please onClick first!";
                outputHandler.sendMessage(msg);
            }
        } else if (view.getId() == R.id.export_button) {
            if (getPackageManager().PERMISSION_DENIED == getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, getPackageName())) {
                Message msg = new Message();
                msg.obj = "请赋予本APP写入日历的权限!";
                makeToast.sendMessage(msg);
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
                        makeToast.sendMessage(msg);
                    } else {
                        Message msg = new Message();
                        msg.obj = "导入开始, 请稍等片刻...";
                        makeToast.sendMessage(msg);
                        stu.getCurrentWeek(Integer.parseInt(currentWeek));
                        stu.writeCalendar(makeToast);
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
                onClick(exportButton);
            } else
            {
                // Permission Denied
                Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}

