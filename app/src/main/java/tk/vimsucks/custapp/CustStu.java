package tk.vimsucks.custapp;

import android.Manifest;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.text.format.Time;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
    private MainActivity activity;
    private Integer[] monthDays = new Integer[] {31, 28, 31, 30, 31,30, 31, 31, 30, 31, 30, 31}; private Integer[] startHours = new Integer[] {0, 8, 8, 9, 10, 13, 14, 15, 16, 18, 18, 19, 20};
    private Integer[] endHours = new Integer[] {0, 8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21};
    private Integer[] startMinutes = new Integer[] {0, 0, 50, 55, 45, 30, 20, 25, 15, 0, 50, 55, 45};
    private Integer[] endMinutes = new Integer[] {0, 45, 35, 40, 30, 15, 5, 10, 0, 45, 35, 40, 30};

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
                }
            })
            .build();
    private Request.Builder requestBuilder = new Request.Builder()
            .addHeader("User-Agent", "Mozilla/6.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36");
    private Request request;
    private Response response;

    public CustStu(MainActivity act) {
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
            String viewState = doc.getElementById("__VIEWSTATE").attr("value"); String eventVal = doc.getElementById("__EVENTVALIDATION").attr("value");
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

    public void writeCalendar(Handler handler) {
        //TODO: Create a Calendar named "课程表"
        String calendarURL = "content://com.android.calendar/calendars";
        String calID = "";
        Cursor userCursor = activity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
        if (activity.getPackageManager().PERMISSION_GRANTED != activity.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, activity.getPackageName())) {
            Message msg = new Message();
            msg.obj = "请赋予本APP写入日历的权限!";
            handler.sendMessage(msg);
            if (Build.VERSION.SDK_INT >= 23) {
                activity.requestPermissions(new String[] {Manifest.permission.WRITE_CALENDAR}, 1);
            }
            return;
        }
        if (userCursor.getCount() < 1) {
            Message msg = new Message();
            msg.obj = "请在系统日历APP中添加一个新账户!";
            handler.sendMessage(msg);
            return;
        }
        if (userCursor.getCount() > 0) {
            userCursor.moveToFirst();
        }
        calID = userCursor.getString(userCursor.getColumnIndex("_id"));
        Integer i = 0;
        for (CustClass cls : classTable) {
            writeSingleClass(cls, calID, i++, handler);
        }
        Message msg = new Message();
        msg.obj = "导入完成";
        handler.sendMessage(msg);
    }
    public void writeSingleClass(CustClass cls, String calID, Integer colorIndex, Handler handler) {
        String calendarEventURL = "content://com.android.calendar/events";
        String calendarReminderURL = "content://com.android.calendar/reminders";
        Integer startHour;
        Integer startMinute;
        Integer endHour;
        Integer endMinute;
        if (cls.isHalf) {
            startHour = startHours[cls.nth * 2 - 1];
            startMinute = startMinutes[cls.nth * 2 - 1];
            endHour = endHours[cls.nth * 2 - 1];
            endMinute = endMinutes[cls.nth * 2 - 1];
        } else {
            startHour = startHours[cls.nth * 2 - 1];
            startMinute = startMinutes[cls.nth * 2 - 1];
            endHour = endHours[cls.nth * 2];
            endMinute = endMinutes[cls.nth * 2];
        }
        //System.out.println(cls.className);
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.TITLE, cls.className);
        event.put(CalendarContract.Events.DESCRIPTION, "教师: " + cls.classTeacher);
        event.put(CalendarContract.Events.EVENT_LOCATION, cls.classLocation);
        event.put(CalendarContract.Events.CALENDAR_ID, calID);

        Calendar current = Calendar.getInstance();
        Integer year = current.get(Calendar.YEAR);
        if (year % 100 != 0 && year % 4 == 0) {
            monthDays[2] = 29;
        } else if (year % 400 == 0) {
            monthDays[2] = 29;
        }
        Integer month = current.get(Calendar.MONTH);
        Integer day = current.get(Calendar.DATE);
        Integer currentWeekday = current.get(Calendar.DAY_OF_WEEK);
        Integer clsWeekday = cls.weekday;
        day += (clsWeekday - currentWeekday + 1);
        Integer lastWeek = 0;
        for (Integer week : cls.weeks) {
            Integer wk = week - lastWeek;
            lastWeek = week;
            day += wk * 7;
            Integer mnDay = monthDays[month];
            if (mnDay < day) {
                day -= mnDay;
                month += 1;
                if (month == 12) {
                    month = 0;
                }
            }
            //System.out.println(String.valueOf(month + 1) + "月" + String.valueOf(day) + "日");
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, day, startHour, startMinute);
            long start = calendar.getTime().getTime();
            calendar.set(year, month, day, endHour, endMinute);
            long end = calendar.getTime().getTime();
            event.put(CalendarContract.Events.DTSTART, start);
            //event.put(CalendarContract.Events.EVENT_COLOR, colors[colorIndex]);
            event.put(CalendarContract.Events.DTEND, end);
            event.put(CalendarContract.Events.HAS_ALARM, 1);
            event.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
            Uri newEvent = activity.getContentResolver().insert(Uri.parse(calendarEventURL), event);
            long id = Long.parseLong(newEvent.getLastPathSegment());
            ContentValues values = new ContentValues();
            values.put("event_id", id);
            values.put("minutes", 10);
            activity.getContentResolver().insert(Uri.parse(calendarReminderURL), values);
        }
    }
}
