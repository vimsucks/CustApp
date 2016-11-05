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

    static final int TABLE_CONTENT_TYPE_CLS = 0;
    static final int TABLE_CONTENT_TYPE_EXP = 1;


    public ClassDatabase(Context context) {
        super(context, "school_schedule.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "class_name TEXT," +
                "class_teacher TEXT,"+
                "class_location TEXT,"+
                "week INTEGER," +
                "weekday INTEGER," +
                "nth INTEGER," +
                "is_half INTEGER)";
        db.execSQL("CREATE TABLE " + "cls_table" + sql);
        db.execSQL("CREATE TABLE " + "exp_table" + sql);
        db.execSQL("CREATE TABLE cls_id_table(id BINT PRIMARY KEY)");
        db.execSQL("CREATE TABLE exp_id_table(id BINT PRIMARY KEY)");
        //db.close();
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public void removeAll() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("cls_table", null, null);
        db.delete("exp_table", null, null);
        db.delete("cls_id_table", null, null);
        db.delete("exp_id_table", null, null);
        //db.close();
    }

    public void rebuld_cls_id_table() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("cls_id_table", null, null);
        // db.execSQL("CREATE TABLE cls_id_table(id BINT PRIMARY KEY)");
    }

    public void rebuld_exp_id_table() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("exp_id_table", null, null);
        // db.execSQL("CREATE TABLE exp_id_table(id BINT PRIMARY KEY)");
    }

    public long insert(String class_name, String class_teacher, String class_location, int week ,int weekday, int nth, int is_half, int contentType) {
        SQLiteDatabase db = getWritableDatabase();
        long row = -1;
        ContentValues cv = new ContentValues();
        cv.put("class_name", class_name);
        cv.put("class_teacher", class_teacher);
        cv.put("class_location", class_location);
        cv.put("week", week);
        cv.put("weekday", weekday);
        cv.put("nth", nth);
        cv.put("is_half", is_half);
        if (contentType == TABLE_CONTENT_TYPE_CLS) {
            row = db.insert("cls_table", null, cv);
        } else if (contentType == TABLE_CONTENT_TYPE_EXP) {
            row = db.insert("exp_table", null, cv);
        }
        return row;
    }

    public long insertId(long id, int contentTyep) {
        SQLiteDatabase db = getWritableDatabase();
        long row = -1;
        ContentValues cv = new ContentValues();
        cv.put("id", id);
        if (contentTyep == TABLE_CONTENT_TYPE_CLS) {
            row = db.insert("cls_id_table", null, cv);
        } else if (contentTyep == TABLE_CONTENT_TYPE_EXP) {
            row = db.insert("exp_id_table", null, cv);
        }
        return row;
    }

}
