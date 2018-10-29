package com.example.amy01.mystudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //初始化FacebookSdk，記得要放第一行，不然setContentView會出錯
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button login =(Button)findViewById(R.id.loginbtn);
        login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent2=new Intent(Login.this, MyMap.class);
                startActivity(intent2);
            }
        });

        Button register =(Button)findViewById(R.id.registerbtn);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(Login.this, register.class);
                startActivity(intent);
            }
        });
    }
}
