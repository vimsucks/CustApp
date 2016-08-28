package tk.vimsucks.custapp;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustClass {

    private String className;
    private String classTeacher;
    private String classLocation;
    private String classTime;
    private Integer weekday;
    private Set<Integer> weeks = new TreeSet<>();
    private boolean isOdd = false;
    private boolean isEven = false;
    private Integer nth;

    public CustClass(String clsName, String clsTeacher, String clsLocation, String clsTime,
                     Integer wkday, Integer n) {
        className = clsName;
        classTeacher = clsTeacher;
        classLocation = clsLocation;
        parseClassTime(clsTime);
        weekday = wkday;
        nth = n;
        printWeeks();
    }

    private void parseClassTime(String clsTime) {
        String pattern = "(\\d+-\\d+)";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(clsTime);
        while (m.find()) {
            String match = m.group(1);
            Integer from = Integer.parseInt(match.split("-")[0]);
            Integer to = Integer.parseInt(match.split("-")[1]);
            if (clsTime.indexOf("单") != -1) {
                isOdd = true;
            } else if (clsTime.indexOf("双") != -1) {
                isEven = true;
            }
            if (isOdd) {
                for (Integer i = from; i <= to; ++i) {
                    if (i % 2 == 1) {
                        weeks.add(i);
                    }
                }
            } else if (isEven) {
                for (Integer i = from; i <= to; ++i) {
                    if (i % 2 == 0) {
                        weeks.add(i);
                    }
                }
            } else {
                for (Integer i = from; i <= to; ++i) {
                    weeks.add(i);
                }
            }
        }
        clsTime = clsTime.replaceAll(pattern, "");
        r = Pattern.compile("(\\d+)");
        m = r.matcher(clsTime);
        while(m.find()) {
            String match = m.group(1);
            Integer i = Integer.parseInt(match);
            weeks.add(i);
        }
    }

    private void printWeeks() {
        System.out.print(className);
        for (Integer i : weeks) {
            System.out.print(",");
            System.out.print(i);
        }
        System.out.println();
    }
}
