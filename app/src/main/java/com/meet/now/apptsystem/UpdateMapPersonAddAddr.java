package com.meet.now.apptsystem;

import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;
import com.nhn.android.mapviewer.overlay.NMapResourceProvider;

import java.util.HashMap;
import java.util.List;

public class UpdateMapPersonAddAddr extends NMapActivity {
    private final String TAG = "UpdateMapPersonAddAddr";

    private NMapView mMapView;

    private NMapResourceProvider nMapResourceProvider;
    private NMapOverlayManager mapOverlayManager;

    LoadNonaddress loadNonaddress;
    LoadFriendaddress loadFriendaddress;
    TextView nowAddr;

    double longitude;
    double latitude;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_map_add_addr);
        Intent intent = getIntent();
        String apptNo = intent.getStringExtra("apptNo");

        TextView tv = findViewById(R.id.tv_add_addr);
        ImageView placeImg1 = findViewById(R.id.iv_center_place_above);
        ImageView placeImg2 = findViewById(R.id.iv_center_place);
        nowAddr = findViewById(R.id.tv_add_addr_now);
        Button ok = findViewById(R.id.btn_map_ok);
        tv.bringToFront();
        placeImg1.bringToFront();
        placeImg2.bringToFront();
        nowAddr.bringToFront();

        // 중심 경위도와 주소 전달.
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String address = nowAddr.getText().toString();
                intent.putExtra("longitude", longitude);
                intent.putExtra("latitude", latitude);
                intent.putExtra("friendAddr", address);

                setResult(RESULT_OK, intent);
                finish();
            }
        });

        loadNonaddress = new LoadNonaddress();
        loadNonaddress.execute(apptNo);
        loadFriendaddress = new LoadFriendaddress();
        loadFriendaddress.execute(apptNo);

        init();

        nMapResourceProvider = new NMapViewerResourceProvider(this);
        mapOverlayManager = new NMapOverlayManager(this, mMapView, nMapResourceProvider);
    }


    private void init() {

        mMapView = findViewById(R.id.map_view_non);
        mMapView.setVisibility(View.INVISIBLE);
        mMapView.setClientId(getResources().getString(R.string.n_key)); // 클라이언트 아이디 값 설정
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.setScalingFactor(1.7f);
        mMapView.requestFocus();

        mMapView.setOnMapStateChangeListener(changeListener);
        mMapView.setOnMapViewTouchEventListener(mapListener);

        NMapController mMapController = mMapView.getMapController();
        mMapController.setMapCenter(new NGeoPoint(126.978371, 37.5666091), 11);     //Default Data

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                setMarker();
                mMapView.setVisibility(View.VISIBLE);
            }
        }, 3000);

    }

    private void setMarker() {
        List<MapApptfriend> mapApptfriendList = LoadFriendaddress.mapApptfriendList;
        List<MapApptfriend> mapApptNonList = LoadNonaddress.mapApptNonList;
        int markerId = NMapPOIflagType.PIN;

        int size = mapApptfriendList.size() + 1;
        if (mapApptNonList != null) {
            size += mapApptNonList.size();
        }

        // set POI data
        NMapPOIdata poiData = new NMapPOIdata(size, nMapResourceProvider);
        poiData.beginPOIdata(size);
        for (int i = 0; i < mapApptfriendList.size(); i++) {
            MapApptfriend mapApptfriend = mapApptfriendList.get(i);
            double coordX = mapApptfriend.getPoint().x;
            double coordY = mapApptfriend.getPoint().y;
            if (coordX > 126.375924 && coordX < 127.859605 && coordY > 36.889164 && coordY < 38.313650) {  // 경기, 서울로 마크 표시 제한
                poiData.addPOIitem(coordX, coordY, mapApptfriend.userNickname, markerId, 0);
            }
        }
        if (mapApptNonList != null) {
            // 비회원 마크
            for (int i = 0; i < mapApptNonList.size(); i++) {
                MapApptfriend mapApptfriend = mapApptNonList.get(i);
                double coordX = mapApptfriend.getPoint().x;
                double coordY = mapApptfriend.getPoint().y;
                if (coordX > 126.375924 && coordX < 127.859605 && coordY > 36.889164 && coordY < 38.313650) {  // 경기, 서울로 마크 표시 제한
                    poiData.addPOIitem(coordX, coordY, mapApptfriend.userNickname, markerId, 0);
                }
            }
        }
        poiData.endPOIdata();

        // create POI data overlay
        NMapPOIdataOverlay poiDataOverlay = mapOverlayManager.createPOIdataOverlay(poiData, null);
        poiDataOverlay.showAllPOIdata(0);
        poiDataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);  //좌표 클릭시 말풍선 리스
    }

    private NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = new NMapPOIdataOverlay.OnStateChangeListener() {
        @Override
        public void onFocusChanged(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
        }

        @Override
        public void onCalloutClick(NMapPOIdataOverlay nMapPOIdataOverlay, NMapPOIitem nMapPOIitem) {
            if (nMapPOIitem != null) {
                Log.e(TAG, "onFocusChanged: " + nMapPOIitem.toString());
            } else {
                Log.e(TAG, "onFocusChanged: ");
            }
        }
    };


    private NMapView.OnMapStateChangeListener changeListener = new NMapView.OnMapStateChangeListener() {
        @Override
        public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
            Log.e(TAG, "OnMapStateChangeListener onMapInitHandler : ");
        }


        @Override
        public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {
            Log.e(TAG, "OnMapStateChangeListener onMapCenterChange : " + nGeoPoint.getLatitude() + " ㅡ  " + nGeoPoint.getLongitude());
            longitude = nGeoPoint.getLongitude();
            latitude = nGeoPoint.getLatitude();

            GeocodeToAddress geocodeToAddress = new GeocodeToAddress(new AddressAsyncResponse() {

                @Override
                public void processFinish(HashMap<String, String> hashMap) {
                    String address = null;
                    if (hashMap != null) address = hashMap.get("address");
                    nowAddr.setText(address);
                }
            });
            geocodeToAddress.execute(longitude + "," + latitude);
        }


        @Override
        public void onMapCenterChangeFine(NMapView nMapView) {
            Log.e(TAG, "OnMapStateChangeListener onMapCenterChangeFine : ");
        }

        @Override
        public void onZoomLevelChange(NMapView nMapView, int i) {
            Log.e(TAG, "OnMapStateChangeListener onZoomLevelChange : " + i);
        }

        @Override
        public void onAnimationStateChange(NMapView nMapView, int i, int i1) {
            Log.e(TAG, "OnMapStateChangeListener onAnimationStateChange : ");
        }
    };

    private NMapView.OnMapViewTouchEventListener mapListener = new NMapView.OnMapViewTouchEventListener() {
        @Override
        public void onLongPress(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onLongPress : ");
        }

        @Override
        public void onLongPressCanceled(NMapView nMapView) {
            Log.e(TAG, "OnMapViewTouchEventListener onLongPressCanceled : ");
        }

        @Override
        public void onTouchDown(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onTouchDown : ");
        }

        @Override
        public void onTouchUp(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onTouchUp : ");

        }

        @Override
        public void onScroll(NMapView nMapView, MotionEvent motionEvent, MotionEvent motionEvent1) {
            Log.e(TAG, "OnMapViewTouchEventListener onScroll : ");

        }

        @Override
        public void onSingleTapUp(NMapView nMapView, MotionEvent motionEvent) {
            Log.e(TAG, "OnMapViewTouchEventListener onSingleTapUp : ");


        }
    };
}
