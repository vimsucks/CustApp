package tk.vimsucks.custapp;

import java.util.ArrayList;

class ClassTable {
    ArrayList<CustClass> clses = new ArrayList<>();
    ArrayList<CustExpe> expes = new ArrayList<>();

    void printAll() {
        for (CustClass cls : clses) {
            System.out.println(cls.toString());
        }
        for (CustExpe ep : expes) {
            System.out.println(ep.toString());
        }
    }
}
