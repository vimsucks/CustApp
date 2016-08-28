package tk.vimsucks.custapp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
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

    private String username;
    private String password;
    private ArrayList<CustClass> classTable = new ArrayList<>();
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
                }})
            .build();
    private Request.Builder requestBuilder = new Request.Builder()
            .addHeader("User-Agent", "Mozilla/6.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
    private Request request;
    private Response response;

    public CustStu() {
    }

    private void sysLog(String log) {
        System.out.print(log);
    }

    private void sysLog(Integer log) {
        System.out.print(log);
    }

    public boolean login(String usrName, String passwd) {
        username = usrName;
        password = passwd;
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
            temp2 = html;
            response.body().close();
            doc = Jsoup.parse(html);
            Element nameEle = doc.getElementById("StudentNameValueLabel");
            if (nameEle == null) {
                sysLog("Login failed\n");
                return false;
            } else {
                sysLog("Login success\n");
                String stuName = nameEle.text();
                temp1 = stuName;
                return true;
            }
        } catch (IOException e) {
            return true;
        }
    }

    public void getClassTable() {
        String classUrl = "http://jwgl.cust.edu.cn/teachweb/kbcx/PersonalCourses.aspx?role=student";
        try {
            request = requestBuilder
                    .url(classUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            Elements tables = doc.getElementsByTag("table");
            tables = new Elements(tables.subList(1, tables.size() - 1));
            Integer i = 0;
            for (Element table : tables) {
                Elements tds = table.getElementsByTag("td");
                Element td1 = tds.first();
                if (td1.text().trim().length() == 1) {
                    ++i;
                    continue;
                } else {
                    tds = new Elements(tds.subList(0, 4));
                    /*
                    for (Element td : tds) {
                        sysLog(td.text() + " ");
                    }
                    sysLog(i % 7 + 1);
                    sysLog(" ");
                    sysLog(i / 7 + 1);
                    sysLog("\n");
                    */
                    ++i;
                    classTable.add(new CustClass(tds.get(0).text(),
                                                 tds.get(1).text(),
                                                 tds.get(2).text(),
                                                 tds.get(3).text(),
                                                 i % 7 + 1,
                                                 i / 7 + 1));
                }
            }
            //sysLog(i);
        } catch (IOException e) {

        }
    }
}
