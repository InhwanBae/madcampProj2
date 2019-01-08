package com.bihcomp.bih.testapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.FragmentManager;
import android.widget.TextView;
import android.widget.Toast;

//import com.facebook.places.Places;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static com.bihcomp.bih.testapp.MainActivity.token_x;


public class Fragment3 extends Fragment implements GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient mGoogleApiClient;
    private boolean mLocationPermissionGranted;

    private static final LatLng DEFAULT_LOCATION = new LatLng(36.368, 127.362);
    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 15000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 15000;

    private GoogleMap googleMap = null;
    private MapView mapView = null;
    private GoogleApiClient googleApiClient = null;
    private Marker currentMarker = null;
    private ArrayList<Marker> markers = new ArrayList<Marker>();
    private int selectedMarkerId = 0;
    private boolean isFirstUpdate = true;
    private double movelatitude = 36.36;
    private  double movelongitude = 127.36;
    private LatLng moveLocation = new LatLng(36.36, 127.36);

    private TimerTask timerTask;
    private Timer timer = new Timer();
    private Handler handler = new Handler();
    private static final int REFRESH_TIMER_PERIOD = 500;

    private View marker_root_view;
    private TextView tv_marker;

    private final static int MAXENTRIES = 5;
    private String[] LikelyPlaceNames = null;
    private String[] LikelyAddresses = null;
    private String[] LikelyAttributions = null;
    private LatLng[] LikelyLatLngs = null;



    public static Fragment3 newInstance(){
        Fragment3 fragment = new Fragment3();
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 맵 자동 업데이트할 타이머 생성
        /*
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Log.i("TimerTest", "timerCall");
                locationUpdate();
            }
        };
        */

        /*
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            //Do Something
            @Override
            public void run() {
                Log.i("HandlerTest", "timerCall");
                Location location = new Location("");
                location.setLatitude(LikelyLatLngs[0].latitude);
                location.setLongitude(LikelyLatLngs[0].longitude);
                Log.i("HandlerTest", "yee");
                setCurrentLocation(location, "Generate On Timer", "Success" );
            }
        }, 0, 1000);
        */
    }

    private void locationUpdate() {
        Runnable updater = new Runnable()
        {
            public void run()
            {
                Log.i("HandlerTest", "HandlerCall");
                Location location = new Location("");
                location.setLatitude(LikelyLatLngs[0].latitude);
                location.setLongitude(LikelyLatLngs[0].longitude);
                setCurrentLocation(location, "Generate On Timer", "Success" );
            }
        };
        handler.post(updater);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_fragment3,container,false);

        mapView = (MapView)view.findViewById(R.id.map);
        mapView.getMapAsync(this);


        /*
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getActivity().getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Location location = new Location("");
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);

                setCurrentLocation(location, place.getName().toString(), place.getAddress().toString());
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        */


        /*
        // 회계기록 없을 때 처리
        if (true)
            view.findViewById(R.id.LayoutMoneyListNotFound).setVisibility(View.GONE);
        else
            view.findViewById(R.id.LayoutMoneyListNotFound).setVisibility(View.VISIBLE);
        */



        return view;
    }






    private void writeToTextFile(String title, String data) {
        try {
            if (getContext() != null) {
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(title + ".txt", Context.MODE_PRIVATE));
                outputStreamWriter.write(data);
                outputStreamWriter.close();
            } else {
                Log.e("writeToTextFile", "OutputStreamWriter: Cannot Read File Because Context = null (application closed)");
            }
        }
        catch (IOException e) {
            Log.e("WriteFile", "Exception: File write failed (" + title + ")");
        }
    }

    private String readFromTextFile(String title) {
        String ret = "";
        try {
            InputStream inputStream = getContext().openFileInput(title + ".txt");
            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();
                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }
                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("ReadFile", "Exception: File not found (" + title + ")");
        } catch (IOException e) {
            Log.e("ReadFile", "Exception: Can not read file (" + title + ")");
        }
        return ret;
    }


    private void getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(getContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
        // A step later in the tutorial adds the code to get the device location.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();

        if ( googleApiClient != null && googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();

        if ( googleApiClient != null)
            googleApiClient.connect();

    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if ( googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();

        if ( googleApiClient != null ) {
            googleApiClient.unregisterConnectionCallbacks(this);
            googleApiClient.unregisterConnectionFailedListener(this);

            if ( googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
                googleApiClient.disconnect();
            }
        }

        // 타이머 중지
        //timerTask.cancel();
        timer.cancel();

        // 서버에 핀 삭제 신호 보내기
        String tempToken = String.valueOf(token_x.charAt(0)) + String.valueOf(token_x.charAt(1)) + String.valueOf(token_x.charAt(2)) + String.valueOf(token_x.charAt(3));
        tempToken += String.valueOf(token_x.charAt(4)) + String.valueOf(token_x.charAt(5)) + String.valueOf(token_x.charAt(6)) + String.valueOf(token_x.charAt(7));
        new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/coordinates?isExit=true&id=" + tempToken + "&name=testAppName&latitude=" + "0" + "&longitude=" + "0");
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //액티비티가 처음 생성될 때 실행되는 함수
        MapsInitializer.initialize(getActivity().getApplicationContext());

        if(mapView != null)
        {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // OnMapReadyCallback implements 해야 mapView.getMapAsync(this); 사용가능. this 가 OnMapReadyCallback

        this.googleMap = googleMap;

        googleMap.setOnMapClickListener(this);
        googleMap.setOnMarkerClickListener(this);

        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에 지도의 초기위치를 서울로 이동
        setCurrentLocation(null, "위치정보 가져올 수 없음", "위치 퍼미션과 GPS 활성 여부 확인");

        //나침반이 나타나도록 설정
        googleMap.getUiSettings().setCompassEnabled(true);
        // 매끄럽게 이동함
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        //  API 23 이상이면 런타임 퍼미션 처리 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 사용권한체크
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

            if ( hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                //사용권한이 없을경우
                //권한 재요청
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                //사용권한이 있는경우
                if ( googleApiClient == null) {
                    buildGoogleApiClient();
                }

                if ( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                {
                    googleMap.setMyLocationEnabled(true);
                }
            }
        } else {

            if ( googleApiClient == null) {
                buildGoogleApiClient();
            }

            googleMap.setMyLocationEnabled(true);
        }

        setCustomMarkerView();

    }

    private void buildGoogleApiClient() {
        if(googleApiClient == null)
        {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(Places.GEO_DATA_API)
                    .addApi(Places.PLACE_DETECTION_API)
                    .enableAutoManage(getActivity(), this)
                    .build();
            googleApiClient.connect();
        }
        else
        {
            googleApiClient.connect();
        }
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if ( !checkLocationServicesStatus()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("위치 서비스 비활성화");
            builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n" +
                    "위치 설정을 수정하십시오.");
            builder.setCancelable(true);
            builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent callGPSSettingIntent =
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
                }
            });
            builder.setNegativeButton("취소", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            builder.create().show();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ( ActivityCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi
                        .requestLocationUpdates(googleApiClient, locationRequest, this);
            }
        } else {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(googleApiClient, locationRequest, this);

            this.googleMap.getUiSettings().setCompassEnabled(true);
            this.googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }

    }

    @Override
    public void onConnectionSuspended(int cause) {
        if ( cause ==  CAUSE_NETWORK_LOST )
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED )
            Log.e(TAG,"onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Location location = new Location("");
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude((DEFAULT_LOCATION.longitude));

        setCurrentLocation(location, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS활성 여부 확인");
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "onLocationChanged call..");
        searchCurrentPlaces();
    }

    private void searchCurrentPlaces() {
        @SuppressWarnings("MissingPermission")
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(googleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>(){

            @Override
            public void onResult(@NonNull PlaceLikelihoodBuffer placeLikelihoods) {
                int i = 0;
                LikelyPlaceNames = new String[MAXENTRIES];
                LikelyAddresses = new String[MAXENTRIES];
                LikelyAttributions = new String[MAXENTRIES];
                LikelyLatLngs = new LatLng[MAXENTRIES];

                for(PlaceLikelihood placeLikelihood : placeLikelihoods) {
                    LikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                    LikelyAddresses[i] = (String) placeLikelihood.getPlace().getAddress();
                    LikelyAttributions[i] = (String) placeLikelihood.getPlace().getAttributions();
                    LikelyLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                    i++;
                    if(i > MAXENTRIES - 1 ) {
                        break;
                    }
                }

                placeLikelihoods.release();

                /*
                if (LikelyLatLngs[0] != null)
                {
                    Location location = new Location("");
                    location.setLatitude(LikelyLatLngs[0].latitude);
                    location.setLongitude(LikelyLatLngs[0].longitude);

                    setCurrentLocation(location, LikelyPlaceNames[0], LikelyAddresses[0]);

                    Toast.makeText(getContext(), "latitude: " + LikelyLatLngs[0].latitude + ", longitude: " + LikelyLatLngs[0].longitude, Toast.LENGTH_SHORT);
                }
                */

                // 인터넷 연결 안될 시 null 오류 해결


                Location location = new Location("");
                location.setLatitude(LikelyLatLngs[0].latitude);
                location.setLongitude(LikelyLatLngs[0].longitude);

                setCurrentLocation(location, LikelyPlaceNames[0], LikelyAddresses[0]);

                Toast.makeText(getContext(), "latitude: " + LikelyLatLngs[0].latitude + ", longitude: " + LikelyLatLngs[0].longitude, Toast.LENGTH_SHORT);

            }
        });

    }

    private void setCustomMarkerView() {
        marker_root_view = LayoutInflater.from(getContext()).inflate(R.layout.marker_layout, null);
        tv_marker = (TextView) marker_root_view.findViewById(R.id.tv_marker);
    }


    private Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap); view.draw(canvas);
        return bitmap;
    }



    @Override
    public void onMapClick(LatLng latLng) {
        // 맵 클릭했을 때 사람 선택 해제
        selectedMarkerId = 0;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        googleMap.animateCamera(center);

        String coordinates = readFromTextFile("COORDINATES");

        JSONArray jarray = null;   // JSONArray 생성
        try {
            jarray = new JSONArray(coordinates);
            for(int i=0; i < jarray.length(); i++) {
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                int id = jObject.getInt("id");
                String name = jObject.getString("name");
                Double latitude = jObject.getDouble("latitude");
                Double longitude = jObject.getDouble("longitude");

                if (latitude == marker.getPosition().latitude && longitude == marker.getPosition().longitude)
                {
                    selectedMarkerId = id;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return true;
    }





    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if ( currentMarker != null ) currentMarker.remove();

        if ( markers.size() != 0) markers.remove(0);

        if ( location != null) {
            //현재위치의 위도 경도 가져옴
            LatLng currentLocation = new LatLng( location.getLatitude(), location.getLongitude());









            this.googleMap.clear();

            /*
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentMarker = this.googleMap.addMarker(markerOptions);

            this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            */

            String tempToken = String.valueOf(token_x.charAt(0)) + String.valueOf(token_x.charAt(1)) + String.valueOf(token_x.charAt(2)) + String.valueOf(token_x.charAt(3));
            tempToken += String.valueOf(token_x.charAt(4)) + String.valueOf(token_x.charAt(5)) + String.valueOf(token_x.charAt(6)) + String.valueOf(token_x.charAt(7));
            new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/coordinates?isExit=false&id=" + tempToken + "&name=testAppName&latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude());

            String coordinates = readFromTextFile("COORDINATES");

            JSONArray jarray = null;   // JSONArray 생성
            try {
                jarray = new JSONArray(coordinates);
                for(int i=0; i < jarray.length(); i++) {
                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                    int id = jObject.getInt("id");
                    String name = jObject.getString("name");
                    Double latitude = jObject.getDouble("latitude");
                    Double longitude = jObject.getDouble("longitude");

                    if (id != Integer.parseInt(tempToken))
                    {
                        LatLng peopleLocations = new LatLng( latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(peopleLocations);
                        markerOptions.title(name);
                        markerOptions.snippet("id: "+ id + ", latitude: " + latitude + ", longitude: " + longitude);
                        markerOptions.draggable(true);
                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_facebook));

                        Marker tempmarker =  this.googleMap.addMarker(markerOptions);
                        markers.add(tempmarker);

                        if (id == selectedMarkerId)
                        {
                            tv_marker.setText(name);

                            LatLng peopleInfoLocations = new LatLng( latitude, longitude);
                            MarkerOptions markerInfoOptions = new MarkerOptions();
                            markerInfoOptions.position(peopleInfoLocations);
                            markerInfoOptions.title(name);
                            markerInfoOptions.snippet("id: "+ id + ", latitude: " + latitude + ", longitude: " + longitude);
                            markerInfoOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(getContext(), marker_root_view)));

                            Marker tempInfomarker =  this.googleMap.addMarker(markerInfoOptions);
                            markers.add(tempInfomarker);

                            if (movelatitude != latitude || movelongitude != longitude)
                            {
                                this.googleMap.animateCamera(CameraUpdateFactory.newLatLng(peopleLocations));
                                movelatitude = peopleLocations.latitude;
                                movelongitude = peopleLocations.longitude;
                            }
                        }
                    }

                    if (isFirstUpdate)
                    {
                        isFirstUpdate = false;
                        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));

                        // 일정 시간마다 계속 업데이트
                        timer = new Timer();
                        timer.schedule(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                locationUpdate();
                            }
                        }, 0, REFRESH_TIMER_PERIOD);
                        //Timer timer = new Timer();
                        //timer.schedule(timerTask, 0, 1000);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }




            return;
        }


        /*
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = this.googleMap.addMarker(markerOptions);
        */
        this.googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));
    }


    // 서버에서 연락처 가져오기
    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                //JSONObject jsonObject = new JSONObject();
                //jsonObject.accumulate("user_id", "androidTest");
                //jsonObject.accumulate("name", "yun");

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://192.168.25.16:3000/users");
                    URL url = new URL(urls[0]);//url을 가져온다.
                    con = (HttpURLConnection) url.openConnection();
                    con.connect();//연결 수행

                    //입력 스트림 생성
                    InputStream stream = con.getInputStream();

                    //속도를 향상시키고 부하를 줄이기 위한 버퍼를 선언한다.
                    reader = new BufferedReader(new InputStreamReader(stream));

                    //실제 데이터를 받는곳
                    StringBuffer buffer = new StringBuffer();

                    //line별 스트링을 받기 위한 temp 변수
                    String line = "";

                    //아래라인은 실제 reader에서 데이터를 가져오는 부분이다. 즉 node.js서버로부터 데이터를 가져온다.
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }

                    //다 가져오면 String 형변환을 수행한다. 이유는 protected String doInBackground(String... urls) 니까
                    return buffer.toString();

                    //아래는 예외처리 부분이다.
                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    //종료가 되면 disconnect메소드를 호출한다.
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        //버퍼를 닫아준다.
                        if(reader != null){
                            reader.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }//finally 부분
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        //doInBackground메소드가 끝나면 여기로 와서 텍스트뷰의 값을 바꿔준다.
        @Override
        protected void onPostExecute(String result) {

            // TODO: 파일 덮어 씌우기
            super.onPostExecute(result);

            if (result == null)
                result = "";

            //Toast.makeText(getContext(), result, Toast.LENGTH_SHORT).show();
            writeToTextFile("COORDINATES", result);
        }

    }



}