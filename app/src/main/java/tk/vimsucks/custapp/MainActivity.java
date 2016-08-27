package tk.vimsucks.custapp;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
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
        stu = new CustStu(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        outputTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    public void start(View view) {
        new Thread(new Runnable() {

            private void outputMsg(String obj) {
                Message msg = new Message();
                msg.obj = obj;
                outputHandler.sendMessage(msg);
            }

            @Override
            public void run() {
                startButton.setClickable(false);
                stu.login();
                outputMsg(stu.temp1);
                outputMsg(stu.temp2);
                outputMsg(stu.temp3);
                startButton.setClickable(true);
            }
        }).start();
    }
}
