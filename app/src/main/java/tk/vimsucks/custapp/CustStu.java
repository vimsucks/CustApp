package tk.vimsucks.custapp;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
    private Map<Integer, TreeSet<CustSimpClass>> weekdayClassTable;
    public String temp1 = " ";
    public String temp2 = " ";
    public String temp3 = " ";
    private Integer currentWeek;
    private Activity activity;

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

    public CustStu(Activity act) {
        activity = act;
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
        if (!classTable.isEmpty()) {
            return;
        }
        String classUrl = "http://jwgl.cust.edu.cn/teachweb/kbcx/Report/wfmRptPersonalCourses.aspx?role=student";
        //String classUrl = "http://jwgl.cust.edu.cn/teachweb/kbcx/PersonalCourses.aspx?role=student";
        try {
            request = requestBuilder
                    .url(classUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            //Elements tables = doc.getElementsByTag("table");
            Elements contentCells = doc.getElementsByClass("ContentCell");
            Integer i = 0;
            for (Element contentCell : contentCells) {
                ++i;
                Elements tables = contentCell.getElementsByTag("table");
                if (tables.size() > 1) {
                    tables.remove(0);
                    for (Element table : tables) {
                        Elements tds = table.getElementsByTag("td");
                        if (tds.first().text().trim().length() <= 1) {
                            continue;
                        } else {
                            Elements tdsAfter = new Elements();
                            for (Element td : tds) {
                                if (td.text().trim().length() > 1) {
                                    tdsAfter.add(td);
                                }
                            }
                            tdsAfter = new Elements(tdsAfter.subList(0, 4));
                            /*
                            for (Element td : tdsAfter) {
                                System.out.print(td.text() + " ");
                            }
                            */
                            classTable.add(new CustClass(tdsAfter.get(0).text(),
                                    tdsAfter.get(1).text(),
                                    tdsAfter.get(2).text(),
                                    tdsAfter.get(3).text(),
                                    i % 7 == 0 ? 7 : i % 7,
                                    i % 7 == 0 ? i / 7 : i / 7 + 1));
                            //System.out.print("\n");
                            //System.out.print(tdsAfter.size());
                        }
                    }
                } else {
                    Element table = tables.first();
                    Elements tds = table.getElementsByTag("td");
                    if (tds.first().text().trim().length() <= 1) {
                        continue;
                    } else {
                        Elements tdsAfter = new Elements();
                        for (Element td : tds) {
                            if (td.text().trim().length() > 1) {
                                tdsAfter.add(td);
                            }
                        }
                        tdsAfter = new Elements(tdsAfter.subList(0, 4));
                        /*
                        for (Element td : tdsAfter) {
                            System.out.print(td.text() + " ");
                        }
                        */
                        classTable.add(new CustClass(tdsAfter.get(0).text(),
                                tdsAfter.get(1).text(),
                                tdsAfter.get(2).text(),
                                tdsAfter.get(3).text(),
                                i % 7 == 0 ? 7 : i % 7,
                                i % 7 == 0 ? i / 7 : i / 7 + 1));
                        //System.out.print("\n");
                        //System.out.print(tdsAfter.size());
                    }
                }
            }
            /*
            tables = new Elements(tables.subList(1, tables.size() - 1));
            Integer i = 0;
            for (Element table : tables) {
                Elements tds = table.getElementsByTag("td");
                Element td1 = tds.first();
                if (td1.text().trim().length() == 1) {
                    ++i;
                    continue;
                } else {
                    //tds = new Elements(tds.subList(0, 4));
                    Elements tdsAfter = new Elements();
                    for (Element td : tds) {
                        if (td.text().trim().length() > 1) {
                            tdsAfter.add(td);
                        }
                    }
                    ++i;
                    classTable.add(new CustClass(tdsAfter.get(0).text(),
                                                 tdsAfter.get(1).text(),
                                                 tdsAfter.get(2).text(),
                                                 tdsAfter.get(3).text(),
                                                 i % 7 == 0 ? 7 : i % 7,
                                                 i % 7 == 0 ? i / 7 : i / 7 + 1));
                    System.out.print(tds.get(0).text() + " ");
                    System.out.print(tds.get(1).text() + " ");
                    System.out.print(tds.get(2).text() + " ");
                    System.out.print(tds.get(3).text() + " ");
                    System.out.print("\n");
                }
            }
            */
        } catch (IOException e) {

        }
    }

    public void getCurrentWeek(Integer week) {
        currentWeek = week;
    }

    public void getWeekClassTable() {
        weekdayClassTable = new HashMap<>();
        Integer i = 0;
        for (CustClass cls : classTable) {
            if (cls.weeks.contains(currentWeek)) {
                if (!weekdayClassTable.containsKey(cls.weekday)) {
                    weekdayClassTable.put(cls.weekday, new TreeSet<CustSimpClass>());
                }
                weekdayClassTable.get(cls.weekday).add(new CustSimpClass(cls, currentWeek));
                ++i;
            }
        }

        Object[] keys = weekdayClassTable.keySet().toArray();
        Arrays.sort(keys);
        for (Object k : keys) {
            System.out.println(k);
            for (CustSimpClass cls : weekdayClassTable.get(k)) {
                cls.print();
            }
        }

        System.out.println(i);
    }

    public void updateClassOutput(Handler handler) {
        Message msg;
        Object[] keys = weekdayClassTable.keySet().toArray();
        Arrays.sort(keys);
        for (Object key : keys) {
            msg = new Message();
            msg.obj = "星期" + key.toString();
            handler.sendMessage(msg);
            //System.out.println(key);
            for (CustSimpClass cls : weekdayClassTable.get(key)) {
                msg = new Message();
                msg.obj = cls.getClassInfo();
                handler.sendMessage(msg);
            }
        }
    }
}
