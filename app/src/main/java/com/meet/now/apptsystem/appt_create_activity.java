package com.meet.now.apptsystem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class appt_create_activity extends AppCompatActivity {

    private EditText appt_name;
    private Spinner appt_age;
    private Spinner appt_meeting_type;
    private String Name;
    private String Date;
    private String Age;
    private String Time;
    private String Meeting;
    private String USERID;
    private JSONArray jsonArray = new JSONArray();
    private JSONObject jsonMain = new JSONObject();

    ArrayList<String> friendList;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appt_create);

        appt_name = findViewById(R.id.appt_name_edit);
        CalendarView appt_date = findViewById(R.id.calendarView);
        appt_age = findViewById(R.id.age_spinner);
        TimePicker appt_time = findViewById(R.id.appt_time_spinner);
        appt_meeting_type = findViewById(R.id.appt_meeting_type_spinner);
        ImageButton apptAddfriend = findViewById(R.id.apptAddfriend);
        Button createCancelBtn = findViewById(R.id.createCancelBtn);

        Intent intent = getIntent();
        USERID = intent.getStringExtra("UserID");

        long now = System.currentTimeMillis();
        Date date = new Date(now);
        @SuppressLint("SimpleDateFormat") SimpleDateFormat CurYearFormat = new SimpleDateFormat("yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat CurMonthFormat = new SimpleDateFormat("MM");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat CurDayFormat = new SimpleDateFormat("dd");
        Date = CurYearFormat.format(date) + "-" + CurMonthFormat.format(date) + "-" + CurDayFormat.format(date);

        appt_date.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int day) {
                Date = String.valueOf(year) + "-" + String.valueOf(month + 1) + "-" + String.valueOf(day);
            }
        });

        // TimePicker 버전호환
        int hour, min;
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentApiVersion > android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            hour = appt_time.getHour();
            min = appt_time.getMinute();
        } else {
            hour = appt_time.getCurrentHour();
            min = appt_time.getCurrentMinute();
        }
        Time = String.valueOf(hour) + ":" + String.valueOf(min);
        appt_time.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
                Time = String.valueOf(hour) + ":" + String.valueOf(minute);
            }
        });

        ArrayAdapter appt_age_adapter = ArrayAdapter.createFromResource(this, R.array.age_array, R.layout.simple_spinner_custom);
        appt_age.setAdapter(appt_age_adapter);

        ArrayAdapter appt_meeting_type_adapter = ArrayAdapter.createFromResource(this, R.array.meeting_type_array, R.layout.simple_spinner_custom);
        appt_meeting_type.setAdapter(appt_meeting_type_adapter);

        Button button = findViewById(R.id.appt_create_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppointmentDetailPutPrepare();
            }
        });

        apptAddfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent ApptfriendIntent = new Intent(getApplicationContext(), ApptAddfriendActivity.class);
                startActivityForResult(ApptfriendIntent, 0);
            }
        });

        friendList = new ArrayList<>();

        createCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cancelIntent = new Intent(getApplicationContext(), MainActivity.class);
                cancelIntent.putExtra("userID", USERID);
                startActivity(cancelIntent);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent backIntent = new Intent(getApplicationContext(), MainActivity.class);
        backIntent.putExtra("userID", USERID);
        startActivity(backIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case 1:
                assert data != null;
                String nickname = data.getStringExtra("nickname");
                String friendID = data.getStringExtra("friendID");
                String userPhoto = data.getStringExtra("userPhoto");

                boolean flag = false;
                for (int i = 0; i < friendList.size(); i++) {
                    if (friendList.get(i).equals(friendID)) {
                        flag = true;
                        break;
                    }
                }
                if (flag) break;

                friendList.add(friendID);
                ApptFriend n_layout = new ApptFriend(getApplicationContext(), nickname, userPhoto);
                LinearLayout con = findViewById(R.id.con);
                con.addView(n_layout);

                try {
                    JSONObject TempFriendID = new JSONObject();
                    TempFriendID.put("FriendID", data.getStringExtra("friendID"));
                    jsonArray.put(TempFriendID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            default:
        }
    }

    public boolean Appt_Name_Set_String(EditText editText) {
        Name = editText.getText().toString().trim();
        if(Name.equals("")){
            return true;
        }
        return false;
    }

    public void Appt_Age_Set_String(Spinner spinner) {
        Age = spinner.getSelectedItem().toString();
    }

    public void Appt_Meeting_Type_Set_String(Spinner spinner) {
        Meeting = spinner.getSelectedItem().toString();
    }

    public void AppointmentDetailPutPrepare() {
        AppointmentDetailPut async_test = new AppointmentDetailPut();
        boolean nameValidate = Appt_Name_Set_String(appt_name);
        Appt_Age_Set_String(appt_age);
        Appt_Meeting_Type_Set_String(appt_meeting_type);
        long diffDays = 0;
        try {
            Date now = new Date();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date certainDay = sdf.parse(Date);
            Date today = sdf.parse(sdf.format(now));
            diffDays = certainDay.getTime() - today.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if(nameValidate){
            Toast.makeText(this, "약속 이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            return;
        }
        if(diffDays < 0){
            Toast.makeText(this, "이전 날짜는 선택할 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        async_test.execute(Name, Date, Age, Time, Meeting, USERID);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("userID", USERID);
        startActivity(intent);
        finish();
    }

    public void createCancelBtnClicked(View view) {
    }

    public void createApptBtnClicked(View view) {
    }

    @SuppressLint("StaticFieldLeak")
    class AppointmentDetailPut extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;

            try {
                String appt_name = params[0];
                String Date_String = params[1];
                String Age_String = params[2];
                String Time_String = params[3];
                String Meeting_Type_String = params[4];
                String USERID = params[5];

                jsonMain.put("FriendList", jsonArray);
                Log.w("minyong", String.valueOf(jsonMain));

                String data = URLEncoder.encode("appt_name", "UTF-8") + "=" + URLEncoder.encode(appt_name, "UTF-8");// UTF-8로  설정 실제로 string 상으로 봤을땐, tmsg="String" 요런식으로 설정 된다.
                data += "&" + URLEncoder.encode("Date_String", "UTF-8") + "=" + URLEncoder.encode(Date_String, "UTF-8");
                data += "&" + URLEncoder.encode("Age_String", "UTF-8") + "=" + URLEncoder.encode(Age_String, "UTF-8");
                data += "&" + URLEncoder.encode("Time_String", "UTF-8") + "=" + URLEncoder.encode(Time_String, "UTF-8");
                data += "&" + URLEncoder.encode("Meeting_Type_String", "UTF-8") + "=" + URLEncoder.encode(Meeting_Type_String, "UTF-8");
                data += "&" + URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(USERID, "UTF-8");
                data += "&" + URLEncoder.encode("FriendList", "UTF-8") + "=" + URLEncoder.encode(jsonMain.toString(), "UTF-8");
                Log.w("minyong", String.valueOf(jsonMain));
                String link = "http://brad903.cafe24.com/AppointmentDetailsCreate.php";

                URL url = new URL(link);

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (httpURLConnection.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);//

                }

                httpURLConnection.disconnect();
                return sb.toString();
            } catch (Exception e) {

                assert httpURLConnection != null;
                httpURLConnection.disconnect();
                return "Exception Occure" + e.getMessage();
            }
        }
    }
}
