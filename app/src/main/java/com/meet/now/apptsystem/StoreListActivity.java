package com.meet.now.apptsystem;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

@SuppressLint("Registered")
public class StoreListActivity extends AppCompatActivity {
    ListView listView;

    StoreListAdapter storeListAdapter;
    String title;
    static public JSONArray jsonArray;
    Intent intent;
    ProgressDialog pDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_list);
        listView = findViewById(R.id.store_listview);
        intent = getIntent();

        title = intent.getStringExtra("title");
        TextView textView = findViewById(R.id.hot_place_name);
        textView.setText(title);

        pDialog = new ProgressDialog(this, AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        pDialog.setCancelable(true);
        pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pDialog.setMessage("잠시만 기다리세요...");
        pDialog.show();
        GetRestInfo getRestInfo = new GetRestInfo();
        getRestInfo.execute(title);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent storeInfo = new Intent(StoreListActivity.this, StoreDetail.class);
                storeInfo.putExtra("placeName", title);
                storeInfo.putExtra("storeInfo", storeListAdapter.getItem(position).toString());
                startActivity(storeInfo);
            }
        });
    }

    public class GetRestInfo extends AsyncTask<String, Void, Void> {

        String gu = intent.getStringExtra("gu");
        String dong = intent.getStringExtra("dong");
        LinearLayout storelistLayout = findViewById(R.id.layout_storelist);
        int storecount;

        public Void dataClear() {
            jsonArray = new JSONArray();
            return null;
        }

        @Override
        protected Void doInBackground(String... strings) {
            dataClear();

            String HotPlace = strings[0];

            try {
                Document document = Jsoup.connect("https://www.mangoplate.com/search/" + HotPlace).get();
                Elements element = document.select(".restaurant-item .only-desktop_not");

                storecount = element.size();
                if(storecount != 0) {
                    for (Element e : element) {
                        Async_GetPlaceInfo getPlaceinfo = new Async_GetPlaceInfo(new AsyncStorelist(){
                            @Override
                            public void LoadStorefinish() {
                                storecount--;
                                if(storecount == 0){
                                    storeListAdapter = new StoreListAdapter(StoreListActivity.this, jsonArray, R.layout.store_item);
                                    listView.setAdapter(storeListAdapter);
                                    pDialog.cancel();
                                }
                            }
                        });
                        getPlaceinfo.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, e.attr("href"));
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if(storecount == 0){  // 검색 결과가 없을 때
                listView.invalidateViews();
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
                        (LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                TextView storelistTextview = new TextView(StoreListActivity.this);
                storelistLayout.addView(storelistTextview, layoutParams);

                storelistTextview.setText("검색 결과가 없습니다");
                storelistTextview.setTextSize(18);
                storelistTextview.setTextColor(0xff000000);
                storelistTextview.setPadding(20,20,0,0);
            }
        }
    }

}
