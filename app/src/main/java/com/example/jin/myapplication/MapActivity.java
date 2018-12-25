package com.example.jin.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapOverlay;
import com.nhn.android.maps.NMapOverlayItem;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.overlay.NMapPOIdata;
import com.nhn.android.maps.overlay.NMapPOIitem;
import com.nhn.android.mapviewer.overlay.NMapCalloutCustomOverlay;
import com.nhn.android.mapviewer.overlay.NMapCalloutOverlay;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;
import com.nhn.android.mapviewer.overlay.NMapPOIdataOverlay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapActivity extends NMapActivity {
    private final String CLIENT_ID = "YBRqbXFKSsM5y518LATt";
    private static final String LOG_TAG = "MapActivity";

    private static final boolean DEBUG = false;
    private static boolean mIsMapEnlared = false;

    private static final NGeoPoint NMAP_LOCATION_DEFAULT = new NGeoPoint(126.978371, 37.5666091);
    private static final int NMAP_ZOOMLEVEL_DEFAULT = 11;
    private static final int NMAP_VIEW_MODE_DEFAULT = NMapView.VIEW_MODE_VECTOR;
    private static final boolean NMAP_TRAFFIC_MODE_DEFAULT = false;
    private static final boolean NMAP_BICYCLE_MODE_DEFAULT = false;

    private static final String KEY_ZOOM_LEVEL = "MapActivity.zoomLevel";
    private static final String KEY_CENTER_LONGITUDE = "MapActivity.centerLongitudeE6";
    private static final String KEY_CENTER_LATITUDE = "MapActivity.centerLatitudeE6";
    private static final String KEY_VIEW_MODE = "MapActivity.viewMode";
    private static final String KEY_TRAFFIC_MODE = "MapActivity.trafficMode";
    private static final String KEY_BICYCLE_MODE = "MapActivity.bicycleMode";

    public final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    public final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 2;

    private NMapView mMapView = null;
    private NMapController mMapController = null;
    private MapContainerView MapContainer;
    private SharedPreferences mPreferences;

    private NMapViewerResourceProvider mMapViewerResourceProvider = null;
    private NMapOverlayManager mOverlayManager;
    private static boolean USE_XML_LAYOUT = false;

    private NMapLocationManager mMapLocationManager;
    private NMapCompassManager mMapCompassManager;
    private NMapMyLocationOverlay mMyLocationOverlay;

    private Location mLocation; // 내 위치를 받는 Location 변수

    private int markerId;
    private NMapPOIdata poidata;
    private NMapPOIdataOverlay poidataOverlay;
    NMapPOIdataOverlay.OnStateChangeListener onPOIdataStateChangeListener = null;
    /* 마커 찍을 때 필요한 것 */

    private LinearLayout mapview; // 네이버 맵이 들어갈 레이아웃
    private Button myloc_button; // 내 위치로 가는 버튼
    private SeekBar dist_bar; // 반경 내의 가게를 찾는 바
    private Button searchdist_button; // 반경 내의 가게를 찾는 버튼
    private TextView dist_text; // 가게의 갯수와 거리를 알려주는 텍스트

    private int dist; // dist_bar 에 입력된 수치

    private Spinner Si_selector;
    private Spinner Sgg_selector;
    private String selectedSi; // 선택된 도, 시
    private String selectedSgg; // 선택된 시, 군, 구
    private String si_text = "si"; // 선택된 도, 시
    private Button searchname_button;

    private List<Store> store; // 가게 정보


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //타이틀바 없애기
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_map);

        //---------------------------------------------------------------------------------------------------------------// 팝업 창 사이즈 변경
        DisplayMetrics disp = getApplicationContext().getResources().getDisplayMetrics();
        int deviceWidth = disp.widthPixels;
        int deviceHeight = disp.heightPixels;

        int width = (int) (deviceWidth * 0.9); //Display 사이즈의 90%

        int height = (int) (deviceHeight * 0.8);  //Display 사이즈의 80%

        getWindow().getAttributes().width = width;

        getWindow().getAttributes().height = height;
        //---------------------------------------------------------------------------------------------------------------//


        //---------------------------------------------------------------------------------------------------------------// 기본 셋팅
        if (USE_XML_LAYOUT) {
            setContentView(R.layout.activity_main);

            mMapView = new NMapView(this);
        } else {
            // create map view
            mMapView = new NMapView(this);

            // create parent view to rotate map view
            MapContainer = new MapContainerView(MapActivity.this);

            MapContainer.addView(mMapView);

            // set the activity content to the parent view
        }

        // set Client ID for Open MapViewer Library
        mMapView.setClientId(CLIENT_ID);

        // set the activity content to the map view

        // initialize map view
        mMapView.setClickable(true);
        mMapView.setEnabled(true);
        mMapView.setFocusable(true);
        mMapView.setFocusableInTouchMode(true);
        mMapView.requestFocus();

        // register listener for map state changes
        mMapView.setOnMapStateChangeListener(onMapViewStateChangeListener);
        mMapView.setOnMapViewTouchEventListener(onMapViewTouchEventListener);
        mMapView.setOnMapViewDelegate(onMapViewTouchDelegate);

        // use built in zoom controls
        NMapView.LayoutParams lp = new NMapView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT, NMapView.LayoutParams.BOTTOM_RIGHT);
        mMapView.setBuiltInZoomControls(true, lp);

        // use map controller to zoom in/out, pan and set map center, zoom level etc.
        mMapController = mMapView.getMapController();

        // create resource provider
        mMapViewerResourceProvider = new NMapViewerResourceProvider(this);

        // create overlay manager
        mOverlayManager = new NMapOverlayManager(this, mMapView, mMapViewerResourceProvider);
        // register callout overlay listener to customize it.
        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);

        // location manager
        mMapLocationManager = new NMapLocationManager(this);
        mMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);

        // compass manager
        mMapCompassManager = new NMapCompassManager(this);

        // create my location overlay
        mMyLocationOverlay = mOverlayManager.createMyLocationOverlay(mMapLocationManager, mMapCompassManager);

        mLocation = new Location("myLocation");

        //가게 모두 로드
        store = new ArrayList<Store>();
        loadStore();

        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 현재 위치 받아오기
        // Location 제공자에서 정보를 얻어오기(GPS)
        // 1. Location을 사용하기 위한 권한을 얻어와야한다 AndroidManifest.xml
        //     ACCESS_FINE_LOCATION : NETWORK_PROVIDER, GPS_PROVIDER
        //     ACCESS_COARSE_LOCATION : NETWORK_PROVIDER
        // 2. LocationManager 를 통해서 원하는 제공자의 리스너 등록
        // 3. GPS 는 에뮬레이터에서는 기본적으로 동작하지 않는다
        // 4. 실내에서는 GPS_PROVIDER 를 요청해도 응답이 없다.  특별한 처리를 안하면 아무리 시간이 지나도
        //    응답이 없다.
        //    해결방법은
        //     ① 타이머를 설정하여 GPS_PROVIDER 에서 일정시간 응답이 없는 경우 NETWORK_PROVIDER로 전환
        //     ② 혹은, 둘다 한꺼번헤 호출하여 들어오는 값을 사용하는 방식.

        // LocationManager 객체를 얻어온다
        final LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, // 등록할 위치제공자
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);
        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, // 등록할 위치제공자
                1000, // 통지사이의 최소 시간간격 (miliSecond)
                1, // 통지사이의 최소 변경거리 (m)
                mLocationListener);

        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 맵 넣기

        AlertDialog.Builder adb = new AlertDialog.Builder(MapActivity.this);

        //---------------------------------------------------------------------------------------------------------------// 선언된 아이템들 초기화
        mapview = (LinearLayout)findViewById(R.id.mapview);
        myloc_button = (Button)findViewById(R.id.mylocation);
        dist_bar = (SeekBar)findViewById(R.id.seekBar);
        searchdist_button = (Button)findViewById(R.id.button2);
        dist_text = (TextView)findViewById(R.id.textView);
        searchname_button = (Button)findViewById(R.id.button);
        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 내 위치로 가는 버튼기능
        myloc_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if((mLocation.getLatitude()>=33 && mLocation.getLatitude()<=38) && (mLocation.getLongitude()>=125 && mLocation.getLongitude()<=131)){
                    mMapController.setMapCenter(mLocation.getLongitude(), mLocation.getLatitude());
                }
                else{
                    Toast.makeText(MapActivity.this, "아직 위치가 잡히지 않았습니다.", Toast.LENGTH_LONG).show();
                }

            }
        });
        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 거리 바 조절
        dist_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (i < 1000) {
                    dist_text.setText(Integer.toString(i) + "m 반경 내에 가게를 검색합니다.");
                }
                else {
                    dist_text.setText(Double.toString(i/1000) + "km 반경 내에 가게를 검색합니다.");
                }
                dist = i;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 거리 검색 버튼
        searchdist_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Store> find_store = new ArrayList<Store>();
                for (int i=0; i<store.size(); i++){
                    Location store_dist = new Location("Store Location");
                    store_dist.setLongitude(store.get(i).store_longitude);
                    store_dist.setLatitude(store.get(i).store_latitude);
                    if (mLocation.distanceTo(store_dist) < dist){
                        find_store.add(store.get(i));
                    }
                }
                if (dist < 1000){
                    dist_text.setText(Integer.toString(dist) + "m 반경 내에 " +find_store.size() + "개의 가게가 있습니다.");
                }
                else{
                    dist_text.setText(Double.toString(dist/1000) + "km 반경 내에 " +find_store.size() + "개의 가게가 있습니다.");
                }
                if (find_store.size() != 0) {
                    Markers(find_store);
                }
                else{
                    if (dist < 1000){
                        dist_text.setText(Integer.toString(dist) + "m 반경 내에 가게가 존재하지 않습니다.");
                    }
                    else{
                        dist_text.setText(Double.toString(dist/1000) + "km 반경 내에 가게가 존재하지 않습니다.");
                    }
                }
            }
        });
        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 도, 시 선택바
        Si_selector = (Spinner)findViewById(R.id.si_spinner);
        ArrayAdapter siAdapter = ArrayAdapter.createFromResource(this, R.array.si, android.R.layout.simple_spinner_item);
        siAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Si_selector.setAdapter(siAdapter);

        Si_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSi = Si_selector.getSelectedItem().toString();
                ArrayAdapter altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.서울특별시, android.R.layout.simple_spinner_item);
                if (selectedSi.equals("광주")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.광주, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("대구")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.대구, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("대전")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.대전, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("부산")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.부산, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("울산")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.울산, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("세종")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.세종, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("충청남도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.충청남도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("충청북도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.충청북도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("강원도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.강원도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("경기도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.경기도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("경상남도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.경상남도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("경상북도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.경상북도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("전라남도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.전라남도, android.R.layout.simple_spinner_item);
                }
                else if (selectedSi.equals("전라북도")) {
                    altAdapter = ArrayAdapter.createFromResource(MapActivity.this, R.array.전라북도, android.R.layout.simple_spinner_item);
                }
                altAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                Sgg_selector.setAdapter(altAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        searchname_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Store> find_store = new ArrayList<Store>();
                for (int i=0; i<store.size(); i++){
                    if (store.get(i).siNm.equals(Si_selector.getSelectedItem().toString())){
                        if (store.get(i).sggNm.equals(Sgg_selector.getSelectedItem().toString()) || Sgg_selector.getSelectedItem().toString().equals("모두")){
                            find_store.add(store.get(i));
                        }
                    }
                }
                if (find_store.isEmpty()){
                    Toast.makeText(MapActivity.this, "등록된 가게가 없습니다.", Toast.LENGTH_LONG).show();
                }
                else {
                    Markers(find_store);
                }
            }

        });
        //---------------------------------------------------------------------------------------------------------------//

        //---------------------------------------------------------------------------------------------------------------// 시, 군, 구 선택바
        Sgg_selector = (Spinner)findViewById(R.id.sgg_spinner);
        ArrayAdapter sggAdapter = ArrayAdapter.createFromResource(this, R.array.서울특별시, android.R.layout.simple_spinner_item);
        sggAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Sgg_selector.setAdapter(sggAdapter);

        Sgg_selector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSgg = Sgg_selector.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        mapview.addView(MapContainer); // 레이아웃에 네이버 맵 넣기
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //바깥레이어 클릭시 안닫히게
        if(event.getAction()==MotionEvent.ACTION_OUTSIDE){
            return false;
        }
        return true;
    }

    /* MapView State Change Listener*/
    private final NMapView.OnMapStateChangeListener onMapViewStateChangeListener = new NMapView.OnMapStateChangeListener() {

        @Override
        public void onMapInitHandler(NMapView mapView, NMapError errorInfo) {

            if (errorInfo == null) { // success
                // restore map view state such as map center position and zoom level.
                restoreInstanceState();

            } else { // fail
                Log.e(LOG_TAG, "onFailedToInitializeWithError: " + errorInfo.toString());

                Toast.makeText(MapActivity.this, errorInfo.toString(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onAnimationStateChange(NMapView mapView, int animType, int animState) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onAnimationStateChange: animType=" + animType + ", animState=" + animState);
            }
        }

        @Override
        public void onMapCenterChange(NMapView mapView, NGeoPoint center) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onMapCenterChange: center=" + center.toString());
            }
        }

        @Override
        public void onZoomLevelChange(NMapView mapView, int level) {
            if (DEBUG) {
                Log.i(LOG_TAG, "onZoomLevelChange: level=" + level);
            }
        }

        @Override
        public void onMapCenterChangeFine(NMapView mapView) {

        }
    };

    private final NMapView.OnMapViewTouchEventListener onMapViewTouchEventListener = new NMapView.OnMapViewTouchEventListener() {

        @Override
        public void onLongPress(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onLongPressCanceled(NMapView mapView) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onSingleTapUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onTouchDown(NMapView mapView, MotionEvent ev) {

        }

        @Override
        public void onScroll(NMapView mapView, MotionEvent e1, MotionEvent e2) {
        }

        @Override
        public void onTouchUp(NMapView mapView, MotionEvent ev) {
            // TODO Auto-generated method stub

        }

    };

    private final NMapView.OnMapViewDelegate onMapViewTouchDelegate = new NMapView.OnMapViewDelegate() {

        @Override
        public boolean isLocationTracking() {
            if (mMapLocationManager != null) {
                if (mMapLocationManager.isMyLocationEnabled()) {
                    return mMapLocationManager.isMyLocationFixed();
                }
            }
            return false;
        }

    };

    private void restoreInstanceState() {
        mPreferences = getPreferences(MODE_PRIVATE);

        int longitudeE6 = mPreferences.getInt(KEY_CENTER_LONGITUDE, NMAP_LOCATION_DEFAULT.getLongitudeE6());
        int latitudeE6 = mPreferences.getInt(KEY_CENTER_LATITUDE, NMAP_LOCATION_DEFAULT.getLatitudeE6());
        int level = mPreferences.getInt(KEY_ZOOM_LEVEL, NMAP_ZOOMLEVEL_DEFAULT);
        int viewMode = mPreferences.getInt(KEY_VIEW_MODE, NMAP_VIEW_MODE_DEFAULT);
        boolean trafficMode = mPreferences.getBoolean(KEY_TRAFFIC_MODE, NMAP_TRAFFIC_MODE_DEFAULT);
        boolean bicycleMode = mPreferences.getBoolean(KEY_BICYCLE_MODE, NMAP_BICYCLE_MODE_DEFAULT);

        mMapController.setMapViewMode(viewMode);
        mMapController.setMapViewTrafficMode(trafficMode);
        mMapController.setMapViewBicycleMode(bicycleMode);
        mMapController.setMapCenter(new NGeoPoint(longitudeE6, latitudeE6), level);

        if (mIsMapEnlared) {
            mMapView.setScalingFactor(2.0F);
        } else {
            mMapView.setScalingFactor(1.0F);
        }
    }

    private final NMapOverlayManager.OnCalloutOverlayListener onCalloutOverlayListener = new NMapOverlayManager.OnCalloutOverlayListener() {

        @Override
        public NMapCalloutOverlay onCreateCalloutOverlay(NMapOverlay itemOverlay, NMapOverlayItem overlayItem,
                                                         Rect itemBounds) {

            // handle overlapped items
            if (itemOverlay instanceof NMapPOIdataOverlay) {
                NMapPOIdataOverlay poiDataOverlay = (NMapPOIdataOverlay)itemOverlay;

                // check if it is selected by touch event
                if (!poiDataOverlay.isFocusedBySelectItem()) {
                    int countOfOverlappedItems = 1;

                    NMapPOIdata poiData = poiDataOverlay.getPOIdata();
                    for (int i = 0; i < poiData.count(); i++) {
                        NMapPOIitem poiItem = poiData.getPOIitem(i);

                        // skip selected item
                        if (poiItem == overlayItem) {
                            continue;
                        }

                        // check if overlapped or not
                        if (Rect.intersects(poiItem.getBoundsInScreen(), overlayItem.getBoundsInScreen())) {
                            countOfOverlappedItems++;
                        }
                    }

                    if (countOfOverlappedItems > 1) {
                        String text = countOfOverlappedItems + " overlapped items for " + overlayItem.getTitle();
                        Toast.makeText(MapActivity.this, text, Toast.LENGTH_LONG).show();
                        return null;
                    }
                }
            }

            // use custom old callout overlay
            if (overlayItem instanceof NMapPOIitem) {
                NMapPOIitem poiItem = (NMapPOIitem)overlayItem;

                if (poiItem.showRightButton()) {
                    return new NMapCalloutCustomOldOverlay(itemOverlay, overlayItem, itemBounds,
                            mMapViewerResourceProvider);
                }
            }

            // use custom callout overlay
            return new NMapCalloutCustomOverlay(itemOverlay, overlayItem, itemBounds, mMapViewerResourceProvider);

            // set basic callout overlay
            //return new NMapCalloutBasicOverlay(itemOverlay, overlayItem, itemBounds);
        }

    };

    /* MyLocation Listener */
    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {

        @Override
        public boolean onLocationChanged(NMapLocationManager locationManager, NGeoPoint myLocation) {
/*
            if (mMapController != null) {
                mMapController.animateTo(myLocation);
            }
*/
            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager locationManager) {

            // stop location updating
            //         Runnable runnable = new Runnable() {
            //            public void run() {
            //               stopMyLocation();
            //            }
            //         };
            //         runnable.run();

            Toast.makeText(MapActivity.this, "Your current location is temporarily unavailable.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager locationManager, NGeoPoint myLocation) {

            Toast.makeText(MapActivity.this, "Your current location is unavailable area.", Toast.LENGTH_LONG).show();

            stopMyLocation();
        }

    };

    private void stopMyLocation() {
        if (mMyLocationOverlay != null) {
            mMapLocationManager.disableMyLocation();

            if (mMapView.isAutoRotateEnabled()) {
                mMyLocationOverlay.setCompassHeadingVisible(false);

                mMapCompassManager.disableCompass();

                mMapView.setAutoRotateEnabled(false, false);

                MapContainer.requestLayout();
            }
        }
    }

    private void startMyLocation() {

        if (mMyLocationOverlay != null) {
            if (!mOverlayManager.hasOverlay(mMyLocationOverlay)) {
                mOverlayManager.addOverlay(mMyLocationOverlay);
            }

            if (mMapLocationManager.isMyLocationEnabled()) {
                mMapView.postInvalidate();
            } else {
                boolean isMyLocationEnabled = mMapLocationManager.enableMyLocation(true);
                if (!isMyLocationEnabled) {
                    Toast.makeText(MapActivity.this, "Please enable a My Location source in system settings",
                            Toast.LENGTH_LONG).show();

                    Intent goToSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(goToSettings);

                    return;
                }
            }
        }
    }

    private final LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            //여기서 위치값이 갱신되면 이벤트가 발생한다.
            //값은 Location 형태로 리턴되며 좌표 출력 방법은 다음과 같다.

            Log.d("test", "onLocationChanged, location:" + location);
            double longitude = location.getLongitude(); //경도
            double latitude = location.getLatitude();   //위도
            double altitude = location.getAltitude();   //고도
            float accuracy = location.getAccuracy();    //정확도
            String provider = location.getProvider();   //위치제공자
            //Gps 위치제공자에 의한 위치변화. 오차범위가 좁다.
            //Network 위치제공자에 의한 위치변화
            //Network 위치는 Gps에 비해 정확도가 많이 떨어진다.
            startMyLocation();

            mLocation.setLongitude(longitude);
            mLocation.setLatitude(latitude);
        }

        public void onProviderDisabled(String provider) {
            // Disabled시
            Log.d("test", "onProviderDisabled, provider:" + provider);
        }

        public void onProviderEnabled(String provider) {
            // Enabled시
            Log.d("test", "onProviderEnabled, provider:" + provider);
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            // 변경시
            Log.d("test", "onStatusChanged, provider:" + provider + ", status:" + status + " ,Bundle:" + extras);
        }
    };

    private class MapContainerView extends ViewGroup {

        public MapContainerView(Context context) {
            super(context);
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            final int width = getWidth();
            final int height = getHeight()  ;
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);
                final int childWidth = view.getMeasuredWidth();
                final int childHeight = view.getMeasuredHeight();
                ;
                final int childLeft = (width - childWidth) / 2;
                final int childTop = (height - childHeight) / 2;
                view.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
            }

            if (changed) {
                mOverlayManager.onSizeChanged(width, height);
            }
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int w = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
            int h = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
            int sizeSpecWidth = widthMeasureSpec;
            int sizeSpecHeight = heightMeasureSpec;

            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View view = getChildAt(i);

                if (view instanceof NMapView) {
                    if (mMapView.isAutoRotateEnabled()) {
                        int diag = (((int) (Math.sqrt(w * w + h * h)) + 1) / 2 * 2);
                        sizeSpecWidth = MeasureSpec.makeMeasureSpec(diag, MeasureSpec.EXACTLY);
                        sizeSpecHeight = sizeSpecWidth;
                    }
                }

                view.measure(sizeSpecWidth, sizeSpecHeight);
            }
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void Markers(List<Store> store){

        mOverlayManager.removeOverlay(poidataOverlay);

        poidata = new NMapPOIdata(2, mMapViewerResourceProvider);

        markerId = NMapPOIflagType.PIN;

        poidata.beginPOIdata(store.size());
        for (int i = 0; i < store.size(); i++) {
            poidata.addPOIitem(store.get(i).store_longitude, store.get(i).store_latitude, store.get(i).store_name, markerId, 0);
        }
        poidata.endPOIdata();

        poidataOverlay = mOverlayManager.createPOIdataOverlay(poidata, null);

        poidataOverlay.showAllPOIdata(0);

        poidataOverlay.setOnStateChangeListener(onPOIdataStateChangeListener);

        mOverlayManager.setOnCalloutOverlayListener(onCalloutOverlayListener);
    }

    private class Store {
        String store_name;

        String siNm; // 시도명
        String sggNm; // 시군구명
        String emdNm; // 읍면동명
        String rNm; // 도로명
        String speciNm; // 상세주소;

        double store_longitude;
        double store_latitude;

        Store() {
            store_name = "";

            double store_longitude;
            double store_latitude;
        }

        Store(Store store){
            this.store_longitude = store.store_longitude;
            this.store_latitude = store.store_latitude;
            this.store_name = store.store_name;
            this.siNm = store.siNm;
            this.sggNm = store.sggNm;
            this.emdNm = store.emdNm;
            this.rNm = store.rNm;
            this.speciNm = store.speciNm;
        }

        Store(double store_longitude, double store_latitude){
            this.store_longitude = store_longitude;
            this.store_latitude = store_latitude;
        }

        Store(String name, String siNm, String sggNm, String emdNm, String rNm, String speciNm, double longitude, double latitude) {
            this.store_name = name;
            this.store_longitude = longitude;
            this.store_latitude = latitude;
            this.siNm = siNm;
            this.sggNm = sggNm;
            this.emdNm = emdNm;
            this.rNm = rNm;
            this.speciNm = speciNm;
        }
    }

    public void addStore(String name, String address, double longitude, double latitude) {

        String[] arr = address.split(" ");

        String siNm = ""; // 시도명
        String sggNm = ""; // 시군구명
        String emdNm = "도로명"; // 읍면동명
        String rNm = "지번명"; // 도로명
        String speciNm = ""; // 상세주소;

        for (int i=0; i<arr.length; i++){
            if (i == 0) {
                String str = "" + arr[0].charAt(0) + arr[0].charAt(1);
                if (arr[0].equals("서울") || str.equals("서울")) siNm = "서울특별시";
                else siNm = arr[i];
            }
            else if (i == 1) sggNm = arr[i];
            else if (i == 2){
                if (arr[arr[i].length()-1] == "로"){
                    rNm = arr[i];
                }
                else{
                    emdNm = arr[i];
                }
            }
            else{
                if (speciNm != "" ) speciNm += " ";
                speciNm += arr[i];
            }
        }

        store.add(new Store(name, siNm, sggNm, emdNm, rNm, speciNm, longitude, latitude));

    }
    RequestQueue mQueue;
    SharedPreferences pref;
    String token;
    // 가게 불러오기
    void loadStore(){
        mQueue=Volley.newRequestQueue(this);
        pref=getSharedPreferences("pref", MODE_PRIVATE);
        token=pref.getString("token",null);
        String url="http://54.180.90.90:3000/api/supplier";
        final JsonArrayRequest request=new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        //Toast.makeText(getApplicationContext(),response.toString(),Toast.LENGTH_SHORT).show();
                        try {
                            for(int i=0;i<response.length();i++){
                                //json 오브젝트 받아오기
                                JSONObject result=response.getJSONObject(i);

                                String rName=result.getString("rname"); // 가게 이름
                                String address=result.getString("address"); // 가게 주소
                                double longitude=result.getDouble("longitude"); // 경도
                                double latitude=result.getDouble("latitude"); //위도
                                if((latitude>=33 && latitude<=38) && (longitude>=125 && longitude<=131)){
                                    addStore(rName,address,longitude,latitude);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Authorization","Bearer "+token);
                return headers;
            }
        };
        mQueue.add(request);
    }
}