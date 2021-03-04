package com.example.notification;

import android.content.Intent;
import android.media.MediaSync;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import internet.HttpConnect;
import internet.Internet;
import tool.KeyStoreHelper;
import tool.SharedPreferencesHelper;

public class LoginMain extends AppCompatActivity
{
    private Button loginButton;
    private Button registerButton;
    private EditText account;
    private EditText password;
    private CheckBox rememberCheck;
    private SharedPreferencesHelper sharedPreferencesHelper ;
    private KeyStoreHelper keyStoreHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_main);
        sharedPreferencesHelper =new SharedPreferencesHelper(getApplicationContext());
        keyStoreHelper=new KeyStoreHelper(getApplicationContext(),sharedPreferencesHelper);

        loginButton = (Button)findViewById(R.id.button2);
        registerButton=(Button)findViewById(R.id.button1);
        account=(EditText)findViewById(R.id.editTextUsername);
        password=(EditText)findViewById(R.id.editTextPassword);
        rememberCheck=(CheckBox)findViewById(R.id.checkBox2);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setClass(LoginMain.this,Register.class);
                startActivity(intent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                login();
            }
        });
        if(!sharedPreferencesHelper.getString("Account").equals(""))
        {
            account.setText(sharedPreferencesHelper.getString("Account"));
            password.setText(getPassword());
            rememberCheck.setChecked(true);
        }
    }
    private void login()
    {
        String userName=account.getText().toString().trim().toLowerCase();
        String password=this.password.getText().toString().trim().toLowerCase();
        singIn(userName,password);

    }
    private void singIn(String userName,String password)
    {
        HttpConnect send=new HttpConnect();
        class SingInUser extends AsyncTask<String,Void,String >
        {
            @Override
            protected void onPreExecute()
            {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s)
            {
                super.onPostExecute(s);
                Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
                if(s.equals("登入成功!"))
                {
                    if(rememberCheck.isChecked())
                        keepPassword(userName,password);
                    else
                    {
                        sharedPreferencesHelper.clear();
                    }
                    Intent intent = new Intent();
                    intent.setClass(LoginMain.this, MainActivity.class);
                    startActivity(intent);
                    LoginMain.this.finish();
                }
            }
            @Override
            protected String doInBackground(String... data)
            {
                HashMap<String,String> dataOut=new HashMap<>();
                dataOut.put("name",data[0]);
                dataOut.put("password",data[1]);
                return send.sendPostRequest(Internet.REGISTER_URL + "login.php",dataOut);
            }
        }
        SingInUser ru = new SingInUser();/**傳送資料**/
        ru.execute(userName, password);
    }

    private void keepPassword(String account,String password)
    {
        String passwordEnc=keyStoreHelper.encrypt(password);
        sharedPreferencesHelper.setInput(passwordEnc);
        sharedPreferencesHelper.setData("Account",account);
    }
    private String getPassword()
    {
        String passwordEnc=sharedPreferencesHelper.getInput();
        return keyStoreHelper.decrypt(passwordEnc);
    }
}
