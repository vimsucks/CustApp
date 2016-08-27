package tk.vimsucks.custapp;

import android.app.Activity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustStu {

    Activity activity;
    private String username;
    private String password;
    public String temp1 = " ";
    public String temp2 = " ";
    public String temp3 = " ";

    private OkHttpClient httpClient = new OkHttpClient.Builder()
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
                }                    })
            .build();
    private Request.Builder requestBuilder = new Request.Builder()
            .addHeader("User-Agent", "Mozilla/6.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
    private Request request;
    private Response response;

    public CustStu(String usrName, String passwd) {
        username = usrName;
        password = passwd;
    }

    private void sysLog(String log) {
        System.out.println(log);
    }

    public boolean login() {
        try {
            String loginUrl = "http://jwgl.cust.edu.cn/teachwebsl/login.aspx";
            request = requestBuilder
                    .url(loginUrl)
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
            temp2 = html;
            response.body().close();
            doc = Jsoup.parse(html);
            String stuName = doc.getElementById("StudentNameValueLabel").text();
            temp1 = stuName;
        } catch (IOException e) {

        }
        return true;
    }
}
