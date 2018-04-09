package com.vimsucks.custapp.util;

import java.util.ArrayList;

/**
 * Created by vimsucks on 9/4/17.
 */

public class Direction {

    private String name;
    private ArrayList<String> classes = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<String> getClasses() {
        return classes;
    }

    public void addClass(String cls) {
        classes.add(cls);
    }

    @Override
    public String toString() {
        return "Direction{" +
                "name='" + name + '\'' +
                ", classes=" + classes +
                '}';
    }
}
