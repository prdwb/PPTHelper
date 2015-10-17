package ppthelper.com.ppthelper;

/**
 * Created by Chen Qu on 2015/9/22.
 */

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private DBHelper helper;
    private SQLiteDatabase db;

    public DBManager(Context context) {
        helper = new DBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add pictures
     * @param pictures
     */
    public void add(List<Picture> pictures) {
        db.beginTransaction();	//开始事务
        try {
            for (Picture picture : pictures) {
                db.execSQL("INSERT INTO picture VALUES(null, ?, ?)", new Object[]{picture.tag, picture.fileName});
            }
            db.setTransactionSuccessful();	//设置事务成功完成
        } finally {
            db.endTransaction();	//结束事务
        }
    }

    /**
     * update picture's fileName
     * @param picture
     */
    /*public void updateFileName(Picture picture) {
        ContentValues cv = new ContentValues();
        cv.put("fileName", picture.fileName);
        db.update("person", cv, "name = ?", new String[]{person.name});
    }*/

    /**
     * delete old picture
     * @param picture
     */
    public void deleteOldPicture(Picture picture) {
        db.delete("person", "fileName >= ?", new String[]{String.valueOf(picture.fileName)});
    }

    /**
     * query all pictures, return list
     * @return List<Picture>
     */
    public List<Picture> query(String keyword) {
        ArrayList<Picture> pictures = new ArrayList<Picture>();
        Cursor c = queryTheCursor();
        ArrayList<String> pictureArray = new ArrayList<String> ();
        while (c.moveToNext()) {
            Picture picture = new Picture();
            picture._id = c.getInt(c.getColumnIndex("_id"));
            picture.tag = c.getString(c.getColumnIndex("tag"));
            picture.fileName = c.getString(c.getColumnIndex("fileName"));
            if(picture.tag.equals(keyword) && !pictureArray.contains(picture.fileName)) {
                pictures.add(picture);
                pictureArray.add(picture.fileName);
            }
        }
        c.close();
        return pictures;
    }

    /**
     * query all pictures, return cursor
     * @return	Cursor
     */
    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM picture", null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }
}

