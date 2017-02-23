package com.vimsucks.custapp.util;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.text.format.Time;
import android.util.Log;

import com.vimsucks.custapp.activities.MainActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CustStu {


    public String username;
    private String password;
    private final ClassTable classTable = new ClassTable();
    private Integer currentWeek;
    private static MainActivity mainActivity;
    private final Integer[] MONTHDAYS = new Integer[] {31, 28, 31, 30, 31,30, 31, 31, 30, 31, 30, 31};
    private final Integer[] STARTHOURS = new Integer[] {0, 8, 8, 9, 10, 13, 14, 15, 16, 18, 18, 19, 20};
    private final Integer[] ENDHOURS = new Integer[] {0, 8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21};
    private final Integer[] STARTMINUTES = new Integer[] {0, 0, 50, 55, 45, 30, 20, 25, 15, 0, 50, 45, 35};
    private final Integer[] ENDMINUTES = new Integer[] {0, 45, 35, 40, 30, 15, 5, 10, 0, 45, 35, 30, 20};

    private static final String TAG = "CustStu";

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }


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
        mainActivity = act;
    }


    /**
     * Login method
     * @param usrName username
     * @param passwd password
     * @return true if login successfully
     */
    public boolean login(String usrName, String passwd) {
        username = usrName;
        password = passwd;
        Log.i(TAG, "login: try login username " + username + " password " + password);
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
                Log.i(TAG, "login : login failed, maybe username & password incorrect");
                Message msg = new Message();
                msg.what = 0;
                mainActivity.progressHandler.sendMessage(msg);
                mainActivity.changeAlert("登录失败", "登录失败，用户名密码错误");
                return false;
            } else {
                return true;
            }
        } catch (IOException e) {
            Log.i(TAG, "login : login failed due to network error");
            Message msg = new Message();
            msg.what = 0;
            mainActivity.progressHandler.sendMessage(msg);
            mainActivity.changeAlert("登录失败", "登录失败,服务器炸啦");
            return false;
        }
    }


    public boolean getClassAndExpe() {
        Boolean clsAcquired = getClassTable();
        Boolean expAcquired = getExpeTable();
        return clsAcquired && expAcquired;
    }


    /**
     * Fetching class table
     * @return true If successfully getting class table
     */
    public boolean getClassTable() {
        String classUrl = "http://jwgl.cust.edu.cn/teachweb/kbcx/Report/wfmRptPersonalCourses.aspx?role=student";
        try {
            request = requestBuilder
                    .url(classUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
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
                            classTable.clses.add(new CustClass(tdsAfter.get(0).text(),
                                    tdsAfter.get(1).text(),
                                    tdsAfter.get(2).text(),
                                    tdsAfter.get(3).text(),
                                    i % 7 == 0 ? 7 : i % 7,
                                    i % 7 == 0 ? i / 7 : i / 7 + 1));
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
                        classTable.clses.add(new CustClass(tdsAfter.get(0).text(),
                                tdsAfter.get(1).text(),
                                tdsAfter.get(2).text(),
                                tdsAfter.get(3).text(),
                                i % 7 == 0 ? 7 : i % 7,
                                i % 7 == 0 ? i / 7 : i / 7 + 1));
                    }
                }
            }
            return true;
        } catch (IOException e) {
            Message msg = new Message();
            msg.what = 0;
            mainActivity.progressHandler.sendMessage(msg);
            mainActivity.changeAlert("获取课表失败", "获取课表失败,服务器炸啦");
            return false;
        }
    }


    public boolean getExpeTable() {
            String expeUrl = "http://jwgl.cust.edu.cn/teachweb/syyy/EBCousesQuery.aspx";
            try {
                request = requestBuilder
                        .url(expeUrl)
                        .get()
                        .build();
                response = httpClient.newCall(request).execute();
                String html = response.body().string();
                Document doc = Jsoup.parse(html);
                Elements trs = doc.getElementsByTag("tr");
                trs.remove(0);
                Integer i = 0;
                for (Element tr : trs) {
                    ++i;
                    Elements tds = tr.getElementsByTag("td");
                    CustExpe expe = new CustExpe(tds.get(0).text() + "-" + tds.get(1).text(),
                            tds.get(5).text(),
                            tds.get(2).text(),
                            tds.get(3).text(),
                            tds.get(4).text()
                    );
                    classTable.expes.add(expe);
                }
                return true;
            } catch (IOException e) {
                Message msg = new Message();
                msg.what = 0;
                mainActivity.progressHandler.sendMessage(msg);
                mainActivity.changeAlert("获取实验课失败", "获取课表失败,服务器炸啦");
                return false;
            }
    }


    public boolean getCurrentWeek() {
        String schoolCalendarUrl = "http://jwgl.cust.edu.cn/teachweb/SchoolCalendar/Calendar.aspx";
        try {
            request = requestBuilder
                    .url(schoolCalendarUrl)
                    .get()
                    .build();
            response = httpClient.newCall(request).execute();
            String html = response.body().string();
            Document doc = Jsoup.parse(html);
            Elements trs = doc.getElementsByTag("tr");
            String temp = trs.get(4).getElementsByTag("td").get(1).text();
            int beginYear = Integer.parseInt(temp.split("年")[0]);
            String beginChineseMonth = temp.split("年")[1];
            beginChineseMonth = beginChineseMonth.substring(0, beginChineseMonth.length()-1);
            int beginMonth = 0;
            for (int i = 0; i < beginChineseMonth.length(); ++i) {
                beginMonth += CustExpe.chineseMap.get(beginChineseMonth.substring(i, i+1));
            }
            int beginDay = Integer.parseInt(trs.get(4).getElementsByTag("td").get(2).text());
            Log.i(TAG, "Successfully fetch term begin year : " + beginYear);
            Log.i(TAG, "Successfully fetch term begin month : " + beginMonth);
            Log.i(TAG, "Successfully fetch term begin day : " + beginDay);
            Calendar fromCalendar = Calendar.getInstance();
            Calendar toCalendar = Calendar.getInstance();
            fromCalendar.set(beginYear, beginMonth - 1, beginDay);
            long intervalDays = TimeUnit.MILLISECONDS.toDays(
                    toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis());
            System.out.println(intervalDays);
            if (intervalDays == 0) {
                currentWeek = 1;
            } else if (intervalDays > 0){
                currentWeek = (int)intervalDays / 7 + 1;
            } else {
                currentWeek = (int)intervalDays / 7;
            }
            Log.i(TAG, "Successfully fetch current week: " + currentWeek);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


    public void setCurrentWeek(Integer week) {
        currentWeek = week;
    }


    public static Uri createCalendar() {
        String accountName = "ClassTable";
        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL).build();

        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, accountName);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL);
        values.put(CalendarContract.Calendars.NAME, "课表");
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "课表");
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0x00FF00);
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, accountName);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, "Asia/Shanghai");
        values.put(CalendarContract.Calendars.CAN_PARTIALLY_UPDATE, 1);
        values.put(CalendarContract.Calendars.CAL_SYNC1, "https://www.google.com/calendar/feeds/" + accountName + "/private/full");
        values.put(CalendarContract.Calendars.CAL_SYNC2, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName); values.put(CalendarContract.Calendars.CAL_SYNC3, "https://www.google.com/calendar/feeds/default/allcalendars/full/" + accountName);
        values.put(CalendarContract.Calendars.CAL_SYNC4, 1);
        values.put(CalendarContract.Calendars.CAL_SYNC5, 0);
        values.put(CalendarContract.Calendars.CAL_SYNC8, System.currentTimeMillis());

        Uri newCalendar = mainActivity.getContentResolver().insert(target, values);

        return newCalendar;
    }


    public void writeSchedule(boolean ifWriteCls, boolean ifWriteExpe) {
        try {
            String calendarURL = "content://com.android.calendar/calendars";
            Cursor userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
            String calID;
            if (userCursor.getCount() < 1) {
                createCalendar();
                userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
                userCursor.moveToFirst();
                userCursor.moveToFirst();
                while (!"课表".equals(userCursor.getString(userCursor.getColumnIndex("name")))) {
                    userCursor.moveToNext();
                }
                calID = userCursor.getString(userCursor.getColumnIndex("_id"));
            } else {
                userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
                userCursor.moveToFirst();
                while (!userCursor.isAfterLast() && !"课表".equals(userCursor.getString(userCursor.getColumnIndex("name")))) {
                    userCursor.moveToNext();
                }
                if (userCursor.isAfterLast()) {
                    createCalendar();
                    userCursor.moveToFirst();
                    userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
                    userCursor.moveToFirst();
                    while (!"课表".equals(userCursor.getString(userCursor.getColumnIndex("name")))) {
                        userCursor.moveToNext();
                    }
                }
                calID = userCursor.getString(userCursor.getColumnIndex("_id"));
            }

            if (ifWriteCls) {
                Integer clsNum = classTable.clses.size();
                int i = 1;
                for (CustClass cls : classTable.clses) {
                    for (int week : cls.weeks) {
                        writeSingleClass(cls.className, cls.classTeacher, cls.classLocation,
                                week, cls.weekday, cls.nth, cls.isHalf, calID);
                    }
                    mainActivity.changeProgress("导出中", "正在导出" + cls.className + "(" + i + "/" + clsNum + ")");
                    System.out.println(cls + " exported");
                    ++i;
                }
            }
            if (ifWriteExpe) {
                Integer expeNum = classTable.expes.size();
                int i = 1;
                for (CustExpe expe : classTable.expes) {
                    writeSingleClass(expe.expeName, "", expe.expeLocation,
                            expe.expeWeek, expe.expeWeekday, expe.nth, false, calID);
                    mainActivity.changeProgress("导出中", "正在导出" + expe.expeName + "(" + i + "/" + expeNum + ")");
                    System.out.println(expe + " exported");
                    ++i;
                }
            }

            Message msg = new Message();
            msg.what = 0;
            mainActivity.progressHandler.sendMessage(msg);
            if (ifWriteCls && ifWriteExpe) {
                mainActivity.changeAlert("导出成功", "成功将课程与实验导出到日历");
            } else if (ifWriteCls) {
                mainActivity.changeAlert("导出成功", "成功将课程导出到日历");
            } else {
                mainActivity.changeAlert("导出成功", "成功将实验导出到日历");
            }
        } catch (Exception exp) {
            Message msg = new Message();
            msg.what = 0;
            mainActivity.progressHandler.sendMessage(msg);
            mainActivity.changeAlert("导出失败", "导出过程中发生了一些错误");
        }
    }


    public void deleteSchedule() {
        String calendarURL = "content://com.android.calendar/calendars";
        String calID = "";
        Cursor userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
        userCursor.moveToFirst();
        while (!userCursor.isAfterLast() && !"课表".equals(userCursor.getString(userCursor.getColumnIndex("name")))) {
            userCursor.moveToNext();
        }
        if (userCursor.isAfterLast()) {
            return;
        } else {
            calID = userCursor.getString(userCursor.getColumnIndex("_id"));
        }

        Uri eventUri = Uri.parse("content://com.android.calendar/events");  // or "content://com.android.calendar/events"

        Cursor cursor = mainActivity.getContentResolver().query(eventUri, new String[]{"_id"}, "calendar_id = " + calID, null, null); // calendar_id can change in new versions

        while(cursor.moveToNext()) {
            Uri deleteUri = ContentUris.withAppendedId(eventUri, cursor.getInt(0));
            mainActivity.getContentResolver().delete(deleteUri, null, null);
        }
        Message msg = new Message();
        msg.what = 0;
        mainActivity.progressHandler.sendMessage(msg);
    }

    public void writeSingleClass(String name, String teacher, String location, Integer week, Integer weekday, Integer nth, boolean is_half, String calID) {
        Integer clsNum = 0;
        String calendarEventURL = "content://com.android.calendar/events";
        String calendarReminderURL = "content://com.android.calendar/reminders";
        Integer startHour;
        Integer startMinute;
        Integer endHour;
        Integer endMinute;
        if (is_half) {
            startHour = STARTHOURS[nth * 2 - 1];
            startMinute = STARTMINUTES[nth * 2 - 1];
            endHour = ENDHOURS[nth * 2 - 1];
            endMinute = ENDMINUTES[nth * 2 - 1];
        } else {
            startHour = STARTHOURS[nth * 2 - 1];
            startMinute = STARTMINUTES[nth * 2 - 1];
            endHour = ENDHOURS[nth * 2];
            endMinute = ENDMINUTES[nth * 2];
        }
        ContentValues event = new ContentValues();
        event.put(CalendarContract.Events.TITLE, name);
        if (!teacher.isEmpty()) {
            event.put(CalendarContract.Events.DESCRIPTION, "教师: " + teacher);
        }
        event.put(CalendarContract.Events.EVENT_LOCATION, location);
        event.put(CalendarContract.Events.CALENDAR_ID, calID);

        Calendar current = Calendar.getInstance();
        Integer year = current.get(Calendar.YEAR);
        if (year % 100 != 0 && year % 4 == 0) {
            MONTHDAYS[2] = 29;
        } else if (year % 400 == 0) {
            MONTHDAYS[2] = 29;
        }
        Integer month = current.get(Calendar.MONTH);
        Integer day = current.get(Calendar.DATE);
        Integer currentWeekday = current.get(Calendar.DAY_OF_WEEK);
        currentWeekday = (currentWeekday == 1 ? 7 : currentWeekday - 1);
        Integer clsWeekday = weekday;
        day += (clsWeekday - currentWeekday);
        Integer lastWeek = currentWeek;
        ++clsNum;
        Integer wk = week - lastWeek;
        lastWeek = week;
        day += wk * 7;
        Integer monthDay = MONTHDAYS[month];
        if (monthDay < day) {
            day -= monthDay;
            month += 1;
            if (month == 12) {
                month = 0;
            }
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day, startHour, startMinute);
        long start = calendar.getTime().getTime();
        calendar.set(year, month, day, endHour, endMinute);
        long end = calendar.getTime().getTime();
        event.put(CalendarContract.Events.DTSTART, start);
        event.put(CalendarContract.Events.DTEND, end);
        event.put(CalendarContract.Events.HAS_ALARM, 1);
        event.put(CalendarContract.Events.EVENT_TIMEZONE, Time.getCurrentTimezone());
        if (mainActivity.getPackageManager().PERMISSION_GRANTED == mainActivity.getPackageManager().checkPermission(Manifest.permission.WRITE_CALENDAR, mainActivity.getPackageName())) {
            Uri newEvent = mainActivity.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, event);
            long id = Long.parseLong(newEvent.getLastPathSegment());
            ContentValues values = new ContentValues();
            values.put("event_id", id);
            values.put("minutes", 10);
            mainActivity.getContentResolver().insert(Uri.parse(calendarReminderURL), values);
        }
    }


    /* *** *** Debug Methods *** *** */
    public void logClassTable() {
        classTable.logAll();
    }

}
