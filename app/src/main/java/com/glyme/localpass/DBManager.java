package com.glyme.localpass;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class DBManager {
	private DBHelper hlper;
	private SQLiteDatabase db;

	public DBManager(Context context) {
		hlper = new DBHelper(context);
		// 因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0,
		// mFactory);
		// 所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
		db = hlper.getWritableDatabase();
	}

	/**
	 * add webinfo
	 * 
	 * @param persons
	 */
	public void add(List<WebInfo> infos) {
		db.beginTransaction(); // 开始事务
		try {
			for (WebInfo info : infos) {
				db.execSQL("INSERT INTO " + DBHelper.TABLE_NAME
						+ " VALUES(null, ?, ?, ?)", new Object[] { info.url,
						info.username, info.password });
			}
			db.setTransactionSuccessful(); // 设置事务成功完成
		} finally {
			db.endTransaction(); // 结束事务
		}
	}

	/**
	 * 清空数据库
	 */
	public void clearDatabase() {
		db.execSQL("DROP TABLE IF EXISTS " + DBHelper.TABLE_NAME);
		db.execSQL("CREATE TABLE IF NOT EXISTS "
				+ DBHelper.TABLE_NAME
				+ " (_id INTEGER PRIMARY KEY AUTOINCREMENT, url TEXT, username TEXT, password TEXT);");
	}

	/**
	 * 搜索符合条件的网站
	 * 
	 * @param keyword
	 *            搜索关键字
	 * @return 所有符合要求的网站
	 */
	public List<WebInfo> search(String keyword) {
		ArrayList<WebInfo> infos = new ArrayList<WebInfo>();
		Cursor cur = db.rawQuery(String.format(
				"select url,username,password from %s "
						+ "where url like %2$s or "
						+ "username like %2$s or password like %2$s;",
				DBHelper.TABLE_NAME, "'%" + keyword + "%'"), null);
		while (cur.moveToNext()) {
			WebInfo info = new WebInfo();
			info.url = cur.getString(cur.getColumnIndex("url"));
			info.username = cur.getString(cur.getColumnIndex("username"));
			info.password = cur.getString(cur.getColumnIndex("password"));
			infos.add(info);
		}
		cur.close();
		return infos;
	}

	/**
	 * 搜索符合条件的网站
	 * 
	 * @param keyword
	 *            搜索关键字
	 * @return 所有符合要求的网站的游标
	 */
	public Cursor search_cur(String keyword) {
		Cursor cur = db.rawQuery(String.format(
				"select _id,url,username,password from %s "
						+ "where url like %2$s or "
						+ "username like %2$s or password like %2$s;",
				DBHelper.TABLE_NAME, "'%" + keyword + "%'"), null);
		return cur;
	}

	public Cursor getAll() {
		Cursor cur = db.rawQuery("select * from " + DBHelper.TABLE_NAME, null);
		return cur;
	}

	/**
	 * 关闭数据库
	 */
	public void closeDB() {
		db.close();
	}
}
