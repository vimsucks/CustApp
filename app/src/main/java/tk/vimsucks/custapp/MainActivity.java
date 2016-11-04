package tk.vimsucks.custapp;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private MyApp myApp = (MyApp)getApplication();
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
        setContentView(R.layout.activity_main);
        myApp.stu = new CustStu(this);
        initViews();
        SharedPreferences  accountPref = getSharedPreferences("account", 0);
        if (accountPref.getBoolean("isLogged", false)) {
            final String username = accountPref.getString("username", "233");
            final String password = accountPref.getString("password", "233");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    myApp.stu.login(username, password);
                    myApp.stu.setCurrentWeek(0);
                    myApp.stu.getClassAndExpe();
                    myApp.stu.classTable.printAll();
                }
            }).start();
        } else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {

        }
    }
    */


    private void initViews() {
        currentWeekEditText = (EditText)findViewById(R.id.current_week_edit_text);
        weekEditText = (EditText)findViewById(R.id.week_edit_text);
        toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
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
                myApp.stu.setCurrentWeek(week);
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
                myApp.stu.setCurrentWeek(week);
            } else {
                Message msg = new Message();
                msg.obj = "Please onClick first!";
                toastHandler.sendMessage(msg);
            }
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
            editor.remove("dbUser");
            editor.commit();
            // editor.remove("username");
            // editor.remove("password");
            myApp.stu.classDatabase.removeAll();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}

