package com.example.amy01.mystudio;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class register extends AppCompatActivity {

    private String StrWhereText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        Button next = (Button) findViewById(R.id.send);
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(register.this, MyMap.class);
                startActivity(intent);
            }
        });
        //announce editText
        EditText name = (EditText) findViewById(R.id.nameEdt);
        EditText birth = (EditText) findViewById(R.id.birthedt);
        EditText email = (EditText) findViewById(R.id.emailedt);

        name.setFilters(new InputFilter[]{new InputFilter.LengthFilter(4)});
        birth.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10)});
    }
}
