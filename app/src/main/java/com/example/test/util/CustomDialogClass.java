package com.example.test.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.example.test.R;

import java.io.File;

public class CustomDialogClass extends Dialog implements
        android.view.View.OnClickListener{

    public Activity c;
    public Dialog d;
    public Button yes, no;

    public CustomDialogClass(Activity a) {
        super(a);
        // TODO Auto-generated constructor stub
        this.c = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_travelers);
        yes = (Button) findViewById(R.id.btn_ok);
        no = (Button) findViewById(R.id.btn_calcel);
        yes.setOnClickListener(this);
        no.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_ok:

                break;
            case R.id.btn_calcel:
                dismiss();
                break;
            default:
                break;
        }
        dismiss();
    }
}