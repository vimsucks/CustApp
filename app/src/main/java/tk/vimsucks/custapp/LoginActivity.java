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
    private EditText mStuIDView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

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
        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setText(accountPref.getString("password", ""));
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            attemptLogin();
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
                        attemptLogin();
                    }
                }).start();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    Boolean attemptLogin() {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .cookieJar(new CookieJar() {
                    //non-persistent CookieJar with an ACCEPT_ALL policy
                    private final Set<Cookie> cookieStore = new LinkedHashSet<>();

                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.addAll(cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> matchingCookies = new ArrayList<>();
                        Iterator<Cookie> it = cookieStore.iterator();
                        while (it.hasNext()) {
                            Cookie cookie = it.next();
                            if (cookie.expiresAt() < System.currentTimeMillis()) {
                                it.remove();
                            } else if (cookie.matches(url)) {
                                matchingCookies.add(cookie);
                            }
                        }
                        return matchingCookies;
                    }
                })
                .build();
        Request.Builder requestBuilder = new Request.Builder()
                .addHeader("User-Agent", "Mozilla/6.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
        Request request;
        Response response;
        String username = mStuIDView.getText().toString();
        String password = mPasswordView.getText().toString();
        try {
            String loginUrl = "http://jwgl.cust.edu.cn/teachwebsl/login.aspx";
            request = requestBuilder
                    .url(loginUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            String html = response.body().string();
            response.body().close();
            Document doc = Jsoup.parse(html);
            String viewState = doc.getElementById("__VIEWSTATE").attr("value");
            String eventVal = doc.getElementById("__EVENTVALIDATION").attr("value");
            RequestBody formBody = new FormBody.Builder()
                    .add("__VIEWSTATE", viewState)
                    .add("__EVENTVALIDATION", eventVal)
                    .add("txtUserName", username)
                    .add("txtPassWord", password)
                    .add("Button1", "登录")
                    .build();
            request = requestBuilder
                    .url(loginUrl)
                    .post(formBody)
                    .build();
            response = httpClient.newCall(request).execute();
            response.body().close();
            String indexUrl = "http://jwgl.cust.edu.cn/teachweb/index1.aspx";
            request = requestBuilder
                    .url(indexUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            html = response.body().string();
            response.body().close();
            doc = Jsoup.parse(html);
            Element nameEle = doc.getElementById("StudentNameValueLabel");
            if (nameEle == null) {
                Message msg = new Message();
                msg.obj = "登录失败，可能是密码错误";
                this.toastHandler.sendMessage(msg);
                return false;
            } else {
                String stuName = nameEle.text();
                Message msg = new Message();
                msg.obj = stuName + "登录成功";
                this.toastHandler.sendMessage(msg);
                SharedPreferences accountPref = getSharedPreferences("account", 0);
                SharedPreferences.Editor editor = accountPref.edit();
                editor.putBoolean("isLogged", true);
                editor.putString("username", username);
                editor.putString("password", password);
                editor.commit();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                return true;
            }
        } catch (IOException e) {
            Message msg = new Message();
            msg.obj = "登录失败,服务器炸啦";
            this.toastHandler.sendMessage(msg);
            return false;
        }
    }

}
