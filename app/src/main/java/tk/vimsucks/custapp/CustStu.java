package tk.vimsucks.custapp;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.CalendarContract;
import android.text.format.Time;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;

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

    public String username;
    private String password;
    ClassTable classTable = new ClassTable();
    ClassDatabase classDatabase;
    private Integer currentWeek;
    static private MainActivity mainActivity;
    private final Integer[] MONTHDAYS = new Integer[] {31, 28, 31, 30, 31,30, 31, 31, 30, 31, 30, 31};
    private final Integer[] STARTHOURS = new Integer[] {0, 8, 8, 9, 10, 13, 14, 15, 16, 18, 18, 19, 20};
    private final Integer[] ENDHOURS = new Integer[] {0, 8, 9, 10, 11, 14, 15, 16, 17, 18, 19, 20, 21};
    private final Integer[] STARTMINUTES = new Integer[] {0, 0, 50, 55, 45, 30, 20, 25, 15, 0, 50, 45, 35};
    private final Integer[] ENDMINUTES = new Integer[] {0, 45, 35, 40, 30, 15, 5, 10, 0, 45, 35, 30, 20};

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
        classDatabase = new ClassDatabase(mainActivity);
    }

    public boolean login(String usrName, String passwd) {
        username = usrName;
        password = passwd;
        System.out.print("!!== try login " + username + " " + password + " ==!!");
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
                mainActivity.toastHandler.sendMessage(msg);
                return false;
            } else {
                String stuName = nameEle.text();
                Message msg = new Message();
                msg.obj = stuName + "登录成功";
                mainActivity.toastHandler.sendMessage(msg);
                return true;
            }
        } catch (IOException e) {
            Message msg = new Message();
            msg.obj = "登录失败,服务器炸啦";
            mainActivity.toastHandler.sendMessage(msg);
            return false;
        }
    }

    public boolean getClassAndExpe() {
        Boolean clsAcquired = getClassTable();
        Boolean expAcquired = getExpeTable();
        return clsAcquired && expAcquired;
    }

    public boolean getClassTable() {
        SharedPreferences  accountPref = mainActivity.getSharedPreferences("account", 0);
        SharedPreferences.Editor editor = accountPref.edit();
        if (!accountPref.getString("clsUser", "").equals(username)) {
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
                                classTable.classes.add(new CustClass(tdsAfter.get(0).text(),
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
                            classTable.classes.add(new CustClass(tdsAfter.get(0).text(),
                                    tdsAfter.get(1).text(),
                                    tdsAfter.get(2).text(),
                                    tdsAfter.get(3).text(),
                                    i % 7 == 0 ? 7 : i % 7,
                                    i % 7 == 0 ? i / 7 : i / 7 + 1));
                        }
                    }
                }
                for (CustClass cls : classTable.classes) {
                    for (Integer week : cls.weeks) {
                        classDatabase.insert(cls.className, cls.classTeacher, cls.classLocation, week, cls.weekday, cls.nth, (cls.isHalf ? 1 : 0), ClassDatabase.TABLE_CONTENT_TYPE_CLS);
                    }
                }
                editor.putString("clsUser", username);
                editor.commit();
                Message msg = new Message();
                msg.obj = "成功获取课表";
                mainActivity.toastHandler.sendMessage(msg);
                return true;
            } catch (IOException e) {
                Message msg = new Message();
                msg.obj = "获取课表失败,服务器炸啦";
                mainActivity.toastHandler.sendMessage(msg);
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean getExpeTable() {
        SharedPreferences  accountPref = mainActivity.getSharedPreferences("account", 0);
        SharedPreferences.Editor editor = accountPref.edit();
        if (!accountPref.getString("expUser", "").equals(username)) {
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
                for (CustExpe expe : classTable.expes) {
                    classDatabase.insert(expe.expeName, "", expe.expeLocation, expe.expeWeek, expe.expeWeekday, expe.nth, 0, ClassDatabase.TABLE_CONTENT_TYPE_EXP);
                }
                editor.putString("expUser", username);
                editor.commit();
                Message msg = new Message();
                msg.obj = "成功获取实验表";
                mainActivity.toastHandler.sendMessage(msg);
                return true;
            } catch (IOException e) {
                Message msg = new Message();
                msg.obj = "获取课表失败,服务器炸啦";
                mainActivity.toastHandler.sendMessage(msg);
                return false;
            }
        } else {
            return true;
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

    public void writeSchedule(Boolean ifWriteCls, Boolean ifWriteExp) {
        SQLiteDatabase db = classDatabase.getWritableDatabase();
        String calendarURL = "content://com.android.calendar/calendars";
        String calID = "";
        Cursor userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null);
        if (userCursor.getCount() < 1) {
            createCalendar();
            userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null); userCursor.moveToFirst();
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
                userCursor = mainActivity.getContentResolver().query(Uri.parse(calendarURL), null, null, null, null); userCursor.moveToFirst();
                while (!"课表".equals(userCursor.getString(userCursor.getColumnIndex("name")))) {
                    userCursor.moveToNext();
                }
            }
            calID = userCursor.getString(userCursor.getColumnIndex("_id"));
        }
        if (ifWriteCls) {
            Cursor cursor = db.query("cls_table", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("class_name"));
                    String teacher = cursor.getString(cursor.getColumnIndex("class_teacher"));
                    String location = cursor.getString(cursor.getColumnIndex("class_location"));
                    Integer week = cursor.getInt(cursor.getColumnIndex("week"));
                    Integer weekday = cursor.getInt(cursor.getColumnIndex("weekday"));
                    Integer nth = cursor.getInt(cursor.getColumnIndex("nth"));
                    Integer is_half = cursor.getInt(cursor.getColumnIndex("is_half"));
                    writeSingleClass(name, teacher, location, week, weekday, nth, is_half, calID, ClassDatabase.TABLE_CONTENT_TYPE_CLS);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        if (ifWriteExp) {
            Cursor cursor = db.query("exp_table", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("class_name"));
                    String teacher = cursor.getString(cursor.getColumnIndex("class_teacher"));
                    String location = cursor.getString(cursor.getColumnIndex("class_location"));
                    Integer week = cursor.getInt(cursor.getColumnIndex("week"));
                    Integer weekday = cursor.getInt(cursor.getColumnIndex("weekday"));
                    Integer nth = cursor.getInt(cursor.getColumnIndex("nth"));
                    Integer is_half = cursor.getInt(cursor.getColumnIndex("is_half"));
                    writeSingleClass(name, teacher, location, week, weekday, nth, is_half, calID, ClassDatabase.TABLE_CONTENT_TYPE_EXP);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void deleteSchdule(Boolean ifWriteCls, Boolean ifWriteExp) {
        SQLiteDatabase db = classDatabase.getWritableDatabase();
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
        Uri eventUri = Uri.parse("content://com.android.calendar/events");
        if (ifWriteCls) {
            Cursor cursor = db.query("cls_id_table", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex("id"));
                    Uri deleteUri = ContentUris.withAppendedId(eventUri, id);
                    mainActivity.getContentResolver().delete(deleteUri, null, null);
                } while (cursor.moveToNext());
            }
            classDatabase.rebuild_cls_id_table();
            cursor.close();
        }
        if (ifWriteExp) {
            Cursor cursor = db.query("exp_id_table", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    long id = cursor.getLong(cursor.getColumnIndex("id"));
                    Uri deleteUri = ContentUris.withAppendedId(eventUri, id);
                    mainActivity.getContentResolver().delete(deleteUri, null, null);
                } while (cursor.moveToNext());
            }
            classDatabase.rebuild_exp_id_table();
            cursor.close();
        }
        /*
        while (cursor.moveToNext()) {
            Uri deleteUri = ContentUris.withAppendedId(eventUri, cursor.getInt(0));
            System.out.println("event " + cursor.getInt(0) + " deleted");
            System.out.println(deleteUri.toString());
            mainActivity.getContentResolver().delete(deleteUri, null, null);
        }
        */
    }

    public void deleteSchdule() {
        SQLiteDatabase db = classDatabase.getWritableDatabase();
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
        classDatabase.rebuild_cls_id_table();
        classDatabase.rebuild_exp_id_table();
    }

    public void writeSingleClass(String name, String teacher, String location, Integer week, Integer weekday, Integer nth, Integer is_half, String calID, int contentType) {
        Integer clsNum = 0;
        String calendarEventURL = "content://com.android.calendar/events";
        String calendarReminderURL = "content://com.android.calendar/reminders";
        Integer startHour;
        Integer startMinute;
        Integer endHour;
        Integer endMinute;
        if (is_half == 1) {
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
            classDatabase.insertId(id, contentType);
            ContentValues values = new ContentValues();
            values.put("event_id", id);
            values.put("minutes", 10);
            mainActivity.getContentResolver().insert(Uri.parse(calendarReminderURL), values);
        }
    }

}
