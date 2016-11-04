package tk.vimsucks.custapp;

import java.util.ArrayList;

/**
 * Created by vimsucks on 11/4/16.
 */

public class ClassTable {
    public  ArrayList<CustClass> classes = new ArrayList<>();
    public ArrayList<CustExpe> expes = new ArrayList<>();

    public ClassTable() {
        return;
    }

    public void printAll() {
        for (CustClass cls : classes) {
            System.out.println(cls.toString());
        }
        for (CustExpe ep : expes) {
            System.out.println(ep.toString());
        }
    }
}
