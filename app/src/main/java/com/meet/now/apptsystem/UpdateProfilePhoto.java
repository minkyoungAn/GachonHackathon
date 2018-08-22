package com.meet.now.apptsystem;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class UpdateProfilePhoto extends DialogFragment implements View.OnClickListener {

    //사진으로 전송시 되돌려 받을 번호
    static int REQUEST_PICTURE = 1;
    //앨범으로 전송시 돌려받을 번호
    static int REQUEST_PHOTO_ALBUM = 2;
    // 파일 크랍시 돌려받을 번호
    static int REQUEST_IMAGE_CROP = 3;

    //샘플
    static String SAMPLEIMG = null;

    String userID = null;
    String userPhoto = null;

    public UpdateProfilePhoto() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // 레이아웃 XML과 뷰 변수 연결
        View view = inflater.inflate(R.layout.dialog_profile_photo_edit, null);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 유저아이디 받아옴
        userID = getArguments().getString("userID");

        Button camera = (Button) getView().findViewById(R.id.camera_btn);
        camera.setOnClickListener(this);
        Button photoAlbum = (Button) getView().findViewById(R.id.photoAlbum_btn);
        photoAlbum.setOnClickListener(this);
        Button back = (Button) getView().findViewById(R.id.btn_back_photo);
        back.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.camera_btn) {
            // 카메라로 사진찍기
            Log.d("onClick", "카메라로 사진찍기");
            takePicture();
            dismiss();

        } else if (v.getId() == R.id.photoAlbum_btn) {
            // 앨범에서 가져오기
            Log.d("onClick", "앨범에서 가져오기");
            photoAlbum();
            dismiss();

        } else if(v.getId() == R.id.btn_back_photo){
            Log.d("onClick", "취소");
            dismiss();
        }

    }

    static File file = null;
    void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        SAMPLEIMG = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg"; // 파일이름.
        file = new File(Environment.getExternalStorageDirectory(), SAMPLEIMG); // sdcard에 새로운 파일 생성. 경로, 이름
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
        getActivity().startActivityForResult(intent, REQUEST_PICTURE);
    }

    void photoAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        getActivity().startActivityForResult(intent, REQUEST_PHOTO_ALBUM);

    }


    // 파일 서버 경로를 Db에 저장
    public void Async_db_Prepare() {
        Async_test async_test = new Async_test();
        async_test.execute(userID, userPhoto);
    }

    // DB에 사진경로 저장하는 asyncTask
    class Async_test extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            HttpURLConnection httpURLConnection = null;

            try {
                String userID = params[0];
                String userPhoto = params[1];

                String data = URLEncoder.encode("userID", "UTF-8") + "=" + URLEncoder.encode(userID, "UTF-8");
                data += "&" + URLEncoder.encode("userPhoto", "UTF-8") + "=" + URLEncoder.encode(userPhoto, "UTF-8");

                //String data2 = "tmsg="+testMsg+"&tmsg2="+testMsg2;

                String link = "http://brad903.cafe24.com/" + "UserPhotoUpdate.php";

                URL url = new URL(link);

                httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");

                httpURLConnection.setDoInput(true);
                httpURLConnection.setDoOutput(true);

                //httpURLConnection.setConnectTimeout(30);

                OutputStreamWriter wr = new OutputStreamWriter(httpURLConnection.getOutputStream());
                wr.write(data);
                wr.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader
                        (httpURLConnection.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    sb.append(line);

                }

                httpURLConnection.disconnect();
                return sb.toString();
            } catch (Exception e) {

                httpURLConnection.disconnect();
                return new String("Exception Occure" + e.getMessage());
            }
        }
    }



}


