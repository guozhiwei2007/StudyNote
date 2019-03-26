package com.xin.nestedscroll;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

    }

    private void initView() {
        TextView tv = (TextView) findViewById(R.id.tv_list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append("item" + i + "\n");
        }
        tv.setText(sb.toString());
    }
}
