package com.example.jin.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
//SQLiteOpenHelper: DB 생성과 버전 관리
//SQLiteDatabase: DB 관리 메소드 제공
public class myDBHelper extends SQLiteOpenHelper {
    //부모 생성자
    public myDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        //context: 안드로이드의 Context 정보, DB명: DB파일 이름, 커서: db데이터 다룰 때 사용
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 DB를 만들 때 DB가 없을 경우에 호출 된다.
        String sql = "CREATE TABLE Order"+"(oid varchar(20) primary key, cid varchar(20), iid varchar(20));";
        db.execSQL(sql);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        // DB명이 있지만 버전이 다른 경우에 호출 ex)새로운 테이블 추가, 삭제
        onCreate(db);
    }
}