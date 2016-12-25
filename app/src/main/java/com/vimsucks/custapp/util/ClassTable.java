package com.vimsucks.custapp.util;

import android.util.Log;

import java.util.ArrayList;

public class ClassTable {
    ArrayList<CustClass> clses = new ArrayList<>();
    ArrayList<CustExpe> expes = new ArrayList<>();
    private static final String TAG = "ClassTable";

    public void logAll() {
        for (CustClass cls : clses) {
            Log.d(TAG, "logAll: class: " + cls.toString());
        }
        for (CustExpe exp : expes) {
            Log.d(TAG, "logAll: experiment: " + exp.toString());
        }
    }
}
