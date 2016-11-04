package tk.vimsucks.custapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.CircularProgressButton;

public class ExportActivity extends AppCompatActivity {

    private CustStu stu;
    CircularProgressButton exportButton;
    EditText currentWeekEditText;
    public Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(ExportActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    public Handler buttonHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what >= 0) {
                exportButton.setProgress(msg.what);
            } else {
                exportButton.setIdleText("重新导出");
                exportButton.setProgress(0);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export);
        setTitle("导出");
        stu = ((MyApp)getApplication()).stu;
        currentWeekEditText = (EditText)findViewById(R.id.current_week_edit_text);
        exportButton = (CircularProgressButton)findViewById(R.id.export_button);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getPackageManager().PERMISSION_DENIED == getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, getPackageName())) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        ActivityCompat.requestPermissions(ExportActivity.this, new String[] {Manifest.permission.WRITE_CALENDAR}, 1);
                    }
                    return;
                }
                final String currentWeek = currentWeekEditText.getText().toString();
                if (currentWeek.length() == 0) {
                    Message msg = new Message();
                    msg.obj = "请输入本周是第几周!!";
                    toastHandler.sendMessage(msg);
                } else {
                    // set progress > 0 & < 100 to display indeterminate progress
                    // set progress to 100 or -1 to indicate complete or error state
                    // set progress to 0 to switch back to normal state
                    exportButton.setProgress(0);
                    exportButton.setIndeterminateProgressMode(true); // turn on indeterminate progress
                    exportButton.setProgress(50);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Integer week = Integer.parseInt(currentWeek);
                            stu.setCurrentWeek(week);
                            stu.deleteCalendar();
                            stu.writeCalendar();
                            buttonHandler.sendEmptyMessage(100);
                            buttonHandler.sendEmptyMessageDelayed(-1, 3000);
                        }
                    }).start();
                }
            }
        });
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
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
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
