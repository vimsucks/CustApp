package com.vimsucks.custapp.util;

import java.util.HashMap;
import java.util.Map;

public class CustExpe {

    public static Map<String, Integer> chineseMap = new HashMap<String, Integer>();

    public String expeName;
    public String expeLocation;
    public Integer expeWeek;
    public Integer expeWeekday;
    public Integer nth;

    static{
        chineseMap.put("一", 1);
        chineseMap.put("元", 1);
        chineseMap.put("二", 2);
        chineseMap.put("三", 3);
        chineseMap.put("四", 4);
        chineseMap.put("五", 5);
        chineseMap.put("六", 6);
        chineseMap.put("七", 7);
        chineseMap.put("日", 7);
        chineseMap.put("八", 8);
        chineseMap.put("九", 9);
        chineseMap.put("十", 10);
    }

    public CustExpe(String epName, String epLocation, String epWeek,
                    String epWeekday, String n) {
        expeName = epName;
        expeLocation = epLocation;
        expeWeek = parseExpeTime(epWeek);
        expeWeekday = parseExpeWeekday(epWeekday);
        nth = parseExpeNth(n);
    }

    public Integer parseExpeTime(String epWeek) {
        Integer result = 0;
        for (int i = 0; i < epWeek.length(); ++i) {
            String subStr = epWeek.substring(i, i + 1);
            if (chineseMap.containsKey(subStr)) {
                result += chineseMap.get(subStr);
            }
        }
        return result;
    }

    public Integer parseExpeWeekday(String epWeekday) {
        epWeekday = epWeekday.substring(epWeekday.length() - 1);
        return chineseMap.get(epWeekday);
    }

    public Integer parseExpeNth(String n) {
        String it = n.substring(0, n.indexOf(","));
        return (Integer.parseInt(it) + 1) / 2;
    }

    @Override
    public String toString() {
        return expeName + " " + expeLocation + " " + expeWeekday + nth;
    }
}

