package com.glyme.localpass;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


public class SetPassword extends Activity {

    private Boolean nopwd; // true for setting password, false for verifying password.
    private PasswordView pwv;
    private TextView tv;
    private String pwd = "";
    private long mExitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_password);
        pwv = (PasswordView) findViewById(R.id.mPasswordView);
        tv = (TextView) findViewById(R.id.tvPrompt);

        nopwd = pwv.isPasswordEmpty();

        pwv.setOnCompleteListener(new PasswordView.OnCompleteListener() {
            @Override
            public void onComplete(String password) {
                if (nopwd) {
                    // setting password
                    if (pwd.isEmpty()) {
                        pwd = password;
                        pwv.clearPassword();
                        tv.setText("请再次绘制密码");
                    } else {
                        if (pwd.equals(password)) {
                            pwv.resetPassWord(password);
                            nopwd = false;
                            pwv.clearPassword();
                            tv.setText("请绘制手势密码");
                        } else {
                            Toast.makeText(SetPassword.this, "两次绘制的密码不吻合，请重新绘制！", Toast.LENGTH_LONG).show();
                            pwd = "";
                            pwv.clearPassword();
                            tv.setText("请绘制手势密码");
                        }
                    }
                } else {
                    // verifying password
                    if (pwv.verifyPassword(password)) {
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        Toast.makeText(SetPassword.this, "密码不正确，请重新绘制！", Toast.LENGTH_LONG).show();
                        pwv.clearPassword();
                    }
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_password, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis()-mExitTime>2000) {
            Toast.makeText(this, "再按一次返回键退出程序", Toast.LENGTH_SHORT).show();
            mExitTime= System.currentTimeMillis();
        }
        else{
            setResult(RESULT_CANCELED);
            finish();
        }
    }
}
