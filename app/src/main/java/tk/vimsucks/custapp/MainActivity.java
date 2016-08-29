package tk.vimsucks.custapp;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private CustStu stu;
    Button startButton;
    ScrollView outputScrollView;
    TextView outputTextView;
    EditText usernameEditText;
    EditText passwordEditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main);
        startButton = (Button) findViewById(R.id.start_button);
        outputScrollView = (ScrollView)findViewById(R.id.output_scroll_view);
        outputTextView = (TextView)findViewById(R.id.output_text_view);
        usernameEditText = (EditText)findViewById(R.id.username_edit_text);
        passwordEditText = (EditText)findViewById(R.id.password_edit_text);
        stu = new CustStu(this);
        outputTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void login(View view) {
        new Thread(new Runnable() {

            private void outputMsg(String obj) {
                Message msg = new Message();
                msg.obj = obj;
                outputHandler.sendMessage(msg);
            }

            @Override
            public void run() {
                EditText weekEditText = (EditText)findViewById(R.id.week_edit_text);
                Integer week = Integer.parseInt(weekEditText.getText().toString());
                if (stu.login(usernameEditText.getText().toString(), passwordEditText.getText().toString())) {
                    stu.getCurrentWeek(week);
                    stu.getClassTable();
                    stu.getWeekClassTable();
                    outputClearHandler.sendMessage(new Message());
                    stu.updateClassOutput(outputHandler);
                    //startButton.setClickable(false);
                    isLogin = true;
                } else {
                    outputMsg("Login failed");
                }
            }
        }).start();
        View currentView = this.getCurrentFocus();
        if (currentView != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentView.getWindowToken(), 0);
        }
    }

    public void weekMinus(View view) {
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
            msg.obj = "Please login first!";
            outputHandler.sendMessage(msg);
        }
    }


    public void weekPlus(View view) {
        EditText weekEditText = (EditText)findViewById(R.id.week_edit_text);
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
            msg.obj = "Please login first!";
            outputHandler.sendMessage(msg);
        }
    }
}

