package tk.vimsucks.custapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by vimsucks on 11/4/16.
 */

public class ClassDatabase extends SQLiteOpenHelper {

    public ClassDatabase(Context context) {
        super(context, "class_table.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        StringBuilder sql = new StringBuilder();
        sql.append("CREATE TABLE ");
        sql.append("class_table");
        sql.append("(");
        sql.append("id INTEGER PRIMARY KEY AUTOINCREMENT,");
        sql.append("class_name TEXT,");
        sql.append("class_teacher TEXT,");
        sql.append("class_location TEXT,");
        sql.append("week INTEGER,");
        sql.append("weekday INTEGER,");
        sql.append("nth INTEGER,");
        sql.append("is_half INTEGER");
        sql.append(");");
        db.execSQL(sql.toString());
        db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void removeAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("class_table", null, null);
        db.close();
    }

    public long insert(String class_name, String class_teacher, String class_location, int week ,int weekday, int nth, int is_half){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("class_name", class_name);
        cv.put("class_teacher", class_teacher);
        cv.put("class_location", class_location);
        cv.put("week", week);
        cv.put("weekday", weekday);
        cv.put("nth", nth);
        cv.put("is_half", is_half);
        long row = db.insert("class_table", null, cv);
        db.close();
        return row;
    }
}
