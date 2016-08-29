package tk.vimsucks.custapp;

/**
 * Created by sice on 8/29/16.
 */
public class CustSimpClass implements Comparable<CustSimpClass> {
    public String className;
    public String classTeacher;
    public String classLocation;
    public Integer weekday;
    public Integer nth;
    public Integer week;
    public Boolean isHalf;

    public CustSimpClass(CustClass cls, Integer wk) {
        className = cls.className;
        classTeacher = cls.classTeacher;
        classLocation = cls.classLocation;
        weekday = cls.weekday;
        nth = cls.nth;
        week = wk;
        isHalf = cls.isHalf;
    }

    @Override
    public int compareTo(CustSimpClass cls) {
        return nth - cls.nth;
    }

    public void print() {
        System.out.print(className);
        System.out.print(" ");
        System.out.print(classLocation);
        System.out.print(" ");
        System.out.print(weekday);
        System.out.print(" ");
        System.out.print(nth);
        System.out.print("\n");
    }

    public String getClassInfo() {
        String result = new String("第");
        if (isHalf) {
            result += String.valueOf(nth + 0.5);
        } else {
            result += String.valueOf(nth);
        }
        result += "节: " + className + " " + classLocation + " ";
        return  result;
    }
}
