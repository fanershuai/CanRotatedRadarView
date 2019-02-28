package com.xsw.radarchartview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.xsw.radarchartview.view.ElectricView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<ElectricView.DataModel> datas = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ElectricView view = (ElectricView) findViewById(R.id.eview);
        datas.add(new ElectricView.DataModel(380, 33, 0, "#EBF873"));
        datas.add(new ElectricView.DataModel(380, 40.345, 120, "#64D36F"));
        datas.add(new ElectricView.DataModel(380, 50.345, 210, "#AC2334"));
        view.setDataAni(datas);
    }
}
