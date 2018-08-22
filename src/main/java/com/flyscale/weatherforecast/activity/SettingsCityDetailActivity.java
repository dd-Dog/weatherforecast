package com.flyscale.weatherforecast.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.flyscale.weatherforecast.R;

/**
 * Created by MrBian on 2017/11/23.
 */

public class SettingsCityDetailActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int CODE_SET_CITY = 1;
    private EditText mCity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_city);

        initView();
    }

    private void initView() {
        findViewById(R.id.confirm).setOnClickListener(this);
        mCity = findViewById(R.id.et_city);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                String cityName = mCity.getText().toString().trim();
                boolean cityValid = isCityValid(cityName);
                if (!cityValid) {
                    Toast.makeText(this, "城市名不正确", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra("city", cityName);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
    }

    private boolean isCityValid(String cityName) {
        if (TextUtils.isEmpty(cityName)) {
            return false;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
