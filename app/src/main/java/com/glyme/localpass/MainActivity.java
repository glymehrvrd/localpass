package com.glyme.localpass;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * @author Glyme
 */
public class MainActivity extends Activity {

    public final int REQUEST_PW = 1;
    public final int REQUEST_GETFILE = 2;

    private Button btnSearch;
    private EditText editText1;
    private ListView listView1;

    private Cursor cur;
    private SimpleCursorAdapter adapter;
    private DBManager dbmngr;

    private void init() {
        // 初始化database manager
        dbmngr = new DBManager(this);
        listView1 = (ListView) findViewById(R.id.listView1);
        editText1 = (EditText) findViewById(R.id.editText1);

        // 获取数据指针
        cur = dbmngr.getAll();
        adapter = new SimpleCursorAdapter(this, R.layout.listview, cur,
                new String[]{"url"}, new int[]{R.id.listview_textView1},
                0);
        listView1.setAdapter(adapter);

        // 点击时弹出窗口
        listView1.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                cur.moveToPosition(position);
                final String[] itemList = new String[]{cur.getString(1),
                        cur.getString(2), cur.getString(3)};
                Dialog dlg = new AlertDialog.Builder(view.getContext())
                        .setTitle("网站信息")
                        .setCancelable(true)
                        .setOnCancelListener(
                                new DialogInterface.OnCancelListener() {

                                    public void onCancel(DialogInterface dialog) {
                                        Field field = null;
                                        try {
                                            field = dialog
                                                    .getClass()
                                                    .getSuperclass()
                                                    .getDeclaredField(
                                                            "mShowing");
                                            field.setAccessible(true);
                                            field.set(dialog, true);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        dialog.cancel();
                                    }
                                })
                        .setItems(itemList,
                                new DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        // popupdialog = dialog;
                                        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                                        switch (which) {
                                            case 0:
                                                Toast.makeText(MainActivity.this,
                                                        "已复制网址到剪切板",
                                                        Toast.LENGTH_SHORT).show();
                                                clipboard.setText(itemList[0]);
                                                break;
                                            case 1:
                                                Toast.makeText(MainActivity.this,
                                                        "已复制帐号到剪切板",
                                                        Toast.LENGTH_SHORT).show();
                                                clipboard.setText(itemList[1]);
                                                break;
                                            case 2:
                                                Toast.makeText(MainActivity.this,
                                                        "已复制密码到剪切板",
                                                        Toast.LENGTH_SHORT).show();
                                                clipboard.setText(itemList[2]);
                                                break;
                                        }
                                        Field field = null;
                                        try {
                                            field = dialog
                                                    .getClass()
                                                    .getSuperclass()
                                                    .getDeclaredField(
                                                            "mShowing");
                                            field.setAccessible(true);
                                            field.set(dialog, false);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }).create();
                dlg.show();
            }
        });

        btnSearch = (Button) findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                String keyword = editText1.getText().toString();
                Cursor newcur;
                if (keyword == "") {
                    newcur = dbmngr.getAll();
                } else {
                    newcur = dbmngr.search_cur(editText1.getText().toString());
                }

                adapter.changeCursor(newcur);
                cur.close();
                cur = newcur;
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 手势密码
        Intent intent = new Intent(MainActivity.this,
                SetPassword.class);
        startActivityForResult(intent, REQUEST_PW);
    }

    @Override
    protected void onDestroy() {
        if (cur != null)
            cur.close();
        if (dbmngr != null)
            dbmngr.closeDB();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PW:
                if (resultCode == RESULT_OK) {
                    init();
                } else {
                    finish();
                }
                break;
            case REQUEST_GETFILE:
                // 返回选择的网站信息文件
                if (resultCode == RESULT_OK) {
                    clearDatabase();
                    Uri uri = data.getData();
                    importToDatabase(uri);

                    // 更改数据指针
                    Cursor newcur = dbmngr.getAll();
                    adapter.changeCursor(newcur);
                    cur.close();
                    cur = newcur;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // 导入网站数据
            case R.id.menu_import:
                Intent intent = new Intent();
            /* 限定可选文件类型 */
                intent.setType("text/comma-separated-values");
            /* 使用Intent.ACTION_GET_CONTENT这个Action */
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(intent, REQUEST_GETFILE);
                break;
            case R.id.menu_clear:
                clearDatabase();
                // 更改数据指针
                Cursor newcur = dbmngr.getAll();
                adapter.changeCursor(newcur);
                cur.close();
                cur = newcur;
                break;
            case R.id.menu_exit:
                finish();
                break;
        }

        return true;
    }

    /**
     * read all content to string
     *
     * @param fileName
     * @return
     */
    public static String readToString(String fileName) {
        File file = new File(fileName);
        long filelength = file.length();
        byte[] filecontent = new byte[(int) filelength];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, "utf8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将csv文件表示的数据导入到数据库中
     *
     * @param fpath csv文件路径
     * @return 导入是否成功
     */
    public boolean importToDatabase(Uri fpath) {
        String data = readToString(fpath.getPath());

        boolean inner = false; // 判断逗号是否在引号内
        ArrayList<WebInfo> infos = new ArrayList<WebInfo>();
        StringBuilder sb = new StringBuilder();
        WebInfo info = new WebInfo();
        for (int i = 0; i < data.length(); i++) {
            char c = data.charAt(i);
            if (c == '"') {
                // 双引号表示一个引号
                if (inner && (i + 1) < data.length()
                        && data.charAt(i + 1) == '"') {
                    sb.append('"');
                    i++;
                } else {
                    inner = !inner;
                }
            } else if (c == ',') {
                if (inner)
                    sb.append(',');
                else {
                    info.append(sb.toString());
                    sb = new StringBuilder();
                }
            } else if (c == '\n') {
                if (inner)
                    sb.append('\n');
                else {
                    info.append(sb.toString());
                    infos.add(info);
                    info = new WebInfo();
                    sb = new StringBuilder();
                }
            } else
                sb.append(c);
        }
        infos.remove(0); // remove first record
        dbmngr.add(infos);
        return true;
    }

    /**
     * 清空数据库
     *
     * @return 是否成功清空
     */
    public boolean clearDatabase() {
        dbmngr.clearDatabase();
        return true;
    }
}
