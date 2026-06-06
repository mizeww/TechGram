package com.example.techgram;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TechGram.db";
    private static final int DATABASE_VERSION = 12;

    private static final String TABLE_USERS = "users";
    private static final String COL_USERNAME = "username";
    private static final String COL_PASSWORD = "password";
    private static final String COL_STATUS = "status";
    private static final String COL_RATING = "rating";

    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "task_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_DESC = "description";
    private static final String COL_TASK_DISCIPLINE = "discipline";
    private static final String COL_TASK_TIME = "timestamp";
    private static final String COL_TASK_AUTHOR = "author";
    private static final String COL_TASK_CRITERIA = "criteria";
    private static final String COL_TASK_IMAGE_BLOB = "task_image_blob";

    private static final String TABLE_SOLUTIONS = "solutions";
    private static final String COL_SOL_ID = "sol_id";
    private static final String COL_SOL_TASK_ID = "task_id";
    private static final String COL_SOL_TASK_TITLE = "task_title";
    private static final String COL_SOL_SENDER = "sender";
    private static final String COL_SOL_RECEIVER = "receiver";
    private static final String COL_SOL_TEXT = "solution_text";
    private static final String COL_SOL_SCORE = "score";
    private static final String COL_SOL_TIME = "sol_timestamp";
    private static final String COL_SOL_IMAGE_BLOB = "sol_image_blob";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (id INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_USERNAME + " TEXT UNIQUE, " + COL_PASSWORD + " TEXT, " + COL_STATUS + " TEXT, " + COL_RATING + " INTEGER DEFAULT 100)");

        db.execSQL("CREATE TABLE " + TABLE_TASKS + " (" +
                COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_TITLE + " TEXT, " +
                COL_TASK_DESC + " TEXT, " +
                COL_TASK_DISCIPLINE + " TEXT, " +
                COL_TASK_TIME + " TEXT, " +
                COL_TASK_AUTHOR + " TEXT, " +
                COL_TASK_CRITERIA + " TEXT, " +
                COL_TASK_IMAGE_BLOB + " BLOB)");

        db.execSQL("CREATE TABLE " + TABLE_SOLUTIONS + " (sol_id INTEGER PRIMARY KEY AUTOINCREMENT, task_id TEXT, task_title TEXT, sender TEXT, receiver TEXT, solution_text TEXT, score INTEGER DEFAULT -1, sol_timestamp TEXT, sol_image_blob BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SOLUTIONS);
        onCreate(db);
    }

    public boolean registerUser(String username, String password, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USERNAME, username);
        values.put(COL_PASSWORD, password);
        values.put(COL_STATUS, status);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{"id"}, COL_USERNAME + "=? AND " + COL_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count > 0;
    }

    public String getUserStatus(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String status = "Пользователь";
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_STATUS}, COL_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            status = cursor.getString(0);
            cursor.close();
        }
        db.close();
        return status;
    }

    public int getUserRating(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        int rating = 0;
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_RATING}, COL_USERNAME + "=?", new String[]{username}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            rating = cursor.getInt(0);
            cursor.close();
        }
        db.close();
        return rating;
    }

    public boolean addTask(String title, String desc, String discipline, String time, String author, String criteria, byte[] imageBlob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, title);
        values.put(COL_TASK_DESC, desc);
        values.put(COL_TASK_DISCIPLINE, discipline);
        values.put(COL_TASK_TIME, time);
        values.put(COL_TASK_AUTHOR, author);
        values.put(COL_TASK_CRITERIA, criteria);
        values.put(COL_TASK_IMAGE_BLOB, imageBlob);

        long result = db.insert(TABLE_TASKS, null, values);
        if (result != -1 && author != null && !author.equals("Гость")) {
            db.execSQL("UPDATE " + TABLE_USERS + " SET " + COL_RATING + " = " + COL_RATING + " + 150 WHERE " + COL_USERNAME + " = '" + author + "'");
        }
        db.close();
        return result != -1;
    }

    public List<Task> getAllTasks() {
        return getTasksFromCursor("SELECT * FROM " + TABLE_TASKS + " ORDER BY " + COL_TASK_ID + " DESC");
    }

    public List<Task> getTasksByAuthor(String author) {
        return getTasksFromCursor("SELECT * FROM " + TABLE_TASKS + " WHERE " + COL_TASK_AUTHOR + "='" + author + "' ORDER BY " + COL_TASK_ID + " DESC");
    }

    public int getPublishedCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String targetUser = (username != null) ? username : "Гость";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS + " WHERE " + COL_TASK_AUTHOR + "=?", new String[]{targetUser});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    private List<Task> getTasksFromCursor(String query) {
        List<Task> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                list.add(new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getBlob(7)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public Task getTaskById(String taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TASKS + " WHERE " + COL_TASK_ID + "=?", new String[]{taskId});
        Task task = null;
        if (cursor.moveToFirst()) {
            task = new Task(cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getBlob(7));
        }
        cursor.close();
        db.close();
        return task;
    }

    public boolean addSolution(String taskId, String taskTitle, String sender, String receiver, String text, String time, byte[] imageBlob) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("task_id", taskId);
        values.put("task_title", taskTitle);
        values.put("sender", sender);
        values.put("receiver", receiver);
        values.put("solution_text", text);
        values.put("score", -1);
        values.put("sol_timestamp", time);
        values.put("sol_image_blob", imageBlob);
        long result = db.insert(TABLE_SOLUTIONS, null, values);
        db.close();
        return result != -1;
    }

    public int getIncomingSolutionsCount(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String targetUser = (username != null) ? username : "Гость";
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SOLUTIONS + " WHERE receiver=? AND score=-1", new String[]{targetUser});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public List<Solution> getUnscoredSolutionsForAuthor(String username) {
        List<Solution> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String targetUser = (username != null) ? username : "Гость";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SOLUTIONS + " WHERE receiver=? AND score=-1", new String[]{targetUser});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Solution(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getString(7), cursor.getBlob(8)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public List getSolutionsBySender(String username) {
        List list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String targetUser = (username != null) ? username : "Гость";
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_SOLUTIONS + " WHERE sender=? ORDER BY sol_id DESC", new String[]{targetUser});
        if (cursor.moveToFirst()) {
            do {
                list.add(new Solution(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getInt(6), cursor.getString(7), cursor.getBlob(8)));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return list;
    }

    public void rateSolution(int solId, int score, String senderUsername) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.execSQL("UPDATE solutions SET score = " + score + " WHERE sol_id = " + solId);

        if (senderUsername != null && !senderUsername.equals("Гость")) {
            db.execSQL("UPDATE users SET rating = rating + " + score + " WHERE username = '" + senderUsername + "'");
        }

        db.close();
    }

}