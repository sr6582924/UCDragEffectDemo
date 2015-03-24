package com.example.john.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SlidingMenu menu = new SlidingMenu(getBaseContext());
        setContentView(menu);
        menu.setBehindOffset(1000);
        menu.setAboveOffset(300);
        View menuview = LayoutInflater.from(getBaseContext()).inflate(R.layout.weather,null);
        //ListView listview = (ListView) menuview.findViewById(R.id.mListView);
        //listview.setAdapter(new SimpleAdapter(getBaseContext(),getListAdapterData(),R.layout.list_item,new String[]{"title_name"},new int[]{R.id.title_name}));
        menu.setMenu(menuview);
        View content = LayoutInflater.from(getBaseContext()).inflate(R.layout.content,null);
        menu.setContent(content);

    }

    private List<Map<String, String>> getListAdapterData() {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        for (int i = 0; i < 10; i++) {
            Map<String, String> map = new HashMap<String, String>();
            map.put("title_name","abertam" + i);
            list.add(map);
        }
        return list;
    }

}
