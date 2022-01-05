package com.example.x_tour;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DBNAME = "X-Tour.db";
    Context context;

    public DatabaseHelper(@Nullable Context context) {
        super(context, DBNAME, null, 1);
        this.context = context;
    }

    // create table on database
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE USERS(userID INTEGER primary key AUTOINCREMENT, username TEXT, password TEXT, profilePic BLOB)");
        sqLiteDatabase.execSQL("CREATE TABLE BOOKMARKS(userID INTEGER, placeID TEXT)");
    }

    // drop table if already exists
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS USERS");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS BOOKMARKS");
    }

    public Boolean insertUserData(String username, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);

        // add image to content values
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.raw.default_userpic);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, byteArrayOutputStream);
        byte[] bytesImage = byteArrayOutputStream.toByteArray();
        contentValues.put("profilePic", bytesImage);

        long result = sqLiteDatabase.insert("USERS", null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public Boolean checkUsername(String username) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM USERS WHERE username = ?", new String[]{username});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public Boolean authenticateUser(String username, String password) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM USERS WHERE username = ? AND password =?", new String[]{username, password});
        if (cursor.getCount() > 0)
            return true;
        else
            return false;
    }

    public Integer getUserID(String username) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT userID FROM USERS WHERE username = ?", new String[]{username});
        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    public String getUsername(String userID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT username FROM USERS WHERE userID = ?", new String[]{userID});
        cursor.moveToFirst();

        return cursor.getString(0);
    }

    public Bitmap getProfilePic(String userID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT profilePic FROM USERS WHERE userID = ?", new String[]{userID});
        cursor.moveToFirst();
        byte[] bytesImage = cursor.getBlob(0);
        cursor.close();

        Bitmap bitmapImage = BitmapFactory.decodeByteArray(bytesImage, 0, bytesImage.length);

        return bitmapImage;
    }

    public Boolean updateProfilePic(Bitmap profilePicBitmap, String userID) {

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        // add image to content values
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        profilePicBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytesImage = byteArrayOutputStream.toByteArray();

        contentValues.put("profilePic", bytesImage);

        long result = sqLiteDatabase.update("USERS", contentValues, "userID = ?", new String[]{userID});
        if (result == 1)
            return true;
        else
            return false;
    }

    public Boolean insertBookmarkData(String userID, String placeID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("userID", userID);
        contentValues.put("placeID", placeID);

        long result = sqLiteDatabase.insert("BOOKMARKS", null, contentValues);
        if (result == -1)
            return false;
        else
            return true;
    }

    public ArrayList<String> getAllBookmarks(String userID) {
        ArrayList<String> bookmarksList = new ArrayList<>();

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT placeID FROM BOOKMARKS WHERE userID = ?", new String[]{userID});

        while (cursor.moveToNext()) {
            bookmarksList.add(cursor.getString(0));
        }
        cursor.close();

        return bookmarksList;
    }

    public Boolean checkIfBookmarked(String userID, String placeID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery("SELECT EXISTS (SELECT 1 FROM BOOKMARKS WHERE userID = ? AND placeID = ?)", new String[]{userID, placeID});

        cursor.moveToFirst();
        if (cursor.getInt(0) == 1)
            return true;
        else
            return false;
    }

    public Boolean deleteBookmarkData(String userID, String placeID) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        long result = sqLiteDatabase.delete("BOOKMARKS", "userID = ? AND placeID = ?", new String[]{userID, placeID});
        if (result == 1)
            return true;
        else
            return false;
    }
}
