package tk.vimsucks.custapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.RunnableFuture;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    // UI references.
    MyApp myApp = (MyApp)getApplication();
    private EditText mStuIDView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button signInButton;

    public Handler toastHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Toast.makeText(LoginActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        SharedPreferences accountPref = getSharedPreferences("account", 0);
        mStuIDView = (EditText) findViewById(R.id.stuID);
        mStuIDView.setText(accountPref.getString("username", ""));
        signInButton = (Button)findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String username = mStuIDView.getText().toString();
                        String password = mPasswordView.getText().toString();
                        attemptLogin(username, password);
                    }
                }).start();
            }
        });
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(accountPref.getString("password", ""));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String username = mStuIDView.getText().toString();
                            String password = mPasswordView.getText().toString();
                            attemptLogin(username, password);
                        }
                    }).start();
                    return true;
                }
                return false;
            }
        });

        Button mSignInButton = (Button) findViewById(R.id.sign_in_button);
        mSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String username = mStuIDView.getText().toString();
                        String password = mPasswordView.getText().toString();
                        attemptLogin(username, password);
                    }
                }).start();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    Boolean attemptLogin(String username, String password) {
        Boolean success = myApp.stu.login(username, password);
        if (success) {
            SharedPreferences  accountPref = getSharedPreferences("account", 0);
            SharedPreferences.Editor editor = accountPref.edit();
            editor.putBoolean("isLogged", true);
            editor.putString("username", username);
            editor.putString("password", password);
            editor.commit();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivityForResult(intent, 1);
        }
        return success;
    }

}
