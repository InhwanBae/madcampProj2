package com.bihcomp.bih.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.os.AsyncTask;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /***************************************************
     * Target API 22 이상으로, 수동 권한 요청
     ***************************************************/
    private static final int MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 3;

    private static final int GET_CONTACT_DATA_REQUEST = 0;
    private static final int GET_GALLERY_DATA_REQUEST = 1;


    private ViewPager mViewPager;
    android.support.v7.app.ActionBar bar;
    private FragmentManager fm;
    private ArrayList<Fragment> fList;

    private Animation fab_open, fab_close;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab, fab1, fab2, fab3;

    // facebook 로그인 관련
    private CallbackManager callbackManager;
    private LoginButton loginButton;

    private FrameLayout loginLayout;


    static String name_x="jaebi";
    static String phonenumber_x = "010-1111-1111";
    static String photo_x ="man1";
    static String token_x="00000000";
    static int making=0;
    static int value=0;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);






        //LoginManager.getInstance().logOut();


        // facebook 로그인 관련
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);

        callbackManager = CallbackManager.Factory.create();

        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        //loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(MainActivity.this, "ONSUCCESS", Toast.LENGTH_LONG).show();
                // App code
            }

            @Override
            public void onCancel() {
                Toast.makeText(MainActivity.this, "ONCANCEL", Toast.LENGTH_LONG).show();
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(MainActivity.this, "ONERROR", Toast.LENGTH_LONG).show();
                // App code
            }
        });


        callbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        // App code
                        GraphRequest request;
                        request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {

                            @Override
                            public void onCompleted(JSONObject user, GraphResponse response) {
                                if (response.getError() != null) {

                                } else {
                                    Log.i("TAG", "user: " + user.toString());
                                    Log.i("TAG", "AccessToken: " + loginResult.getAccessToken().getToken());
                                    setResult(RESULT_OK);
                                    String userName = "";
                                    String userId = "";
                                    // 이름 파싱
                                    try {
                                        JSONObject jObject = new JSONObject(user.toString());
                                        userName = jObject.getString("name");
                                        userId = jObject.getString("id");
                                        token_x=userId.toString();
                                        writeToSettingFile("FACEBOOKID", userId.toString());
                                        writeToSettingFile("FACEBOOKNAME", userName);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    //fab.setVisibility(View.VISIBLE);
                                    loginLayout.setVisibility(View.GONE);
                                    bar.setTitle(userName + " 님의 저장소");

                                    Snackbar.make(mViewPager, userName + " 님으로 로그인되었습니다.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                    //Toast.makeText(MainActivity.this, "user: " + user.toString(), Toast.LENGTH_LONG).show();
                                    //Toast.makeText(MainActivity.this, "AccessToken: " + loginResult.getAccessToken().getToken(), Toast.LENGTH_SHORT).show();
                                    //token_x=user.toString();


                                    Snackbar.make(mViewPager, "서버와 동기화를 진행합니다...", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();


                                    new JSONTask_2().execute("http://socrip4.kaist.ac.kr:3480/contacts?token=" + token_x);
                                    new JSONTask_GetServerList().execute("http://socrip4.kaist.ac.kr:3480/photos?token=" + token_x);

                                    mViewPager.refreshDrawableState();
                                    mViewPager.getAdapter().notifyDataSetChanged();

                                    Snackbar.make(mViewPager, "동기화가 완료되었습니다.", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();








                                }
                            }
                        });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        request.setParameters(parameters);
                        request.executeAsync();


                    }

                    @Override
                    public void onCancel() {
                        //로그인 창에서 취소했을 때
                        //Toast.makeText(MainActivity.this, "CANCEL", Toast.LENGTH_LONG).show();
                        Snackbar.make(mViewPager, "로그인이 취소되었습니다.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        //Toast.makeText(MainActivity.this, "ERROR", Toast.LENGTH_LONG).show();
                        Snackbar.make(mViewPager, "로그인에 실패하였습니다.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        // App code
                    }
                });


        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();









        // floatingActionButton 생성
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Todo: Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */


        /***************************************************
         * Target API 22 이상으로, 수동 권한 요청
         ***************************************************/

        requestAllPermission();






        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_close);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);

        fab.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);

        // 스와이프할 뷰페이저를 정의
        mViewPager = (ViewPager) findViewById(R.id.pager);

        // 프라그먼트 매니져 객체 정의
        fm = getSupportFragmentManager();

        // 액션바 객체 정의
        bar = getSupportActionBar();

        // 액션바 속성 정의
        bar.setDisplayShowTitleEnabled(true);   // 액션바 노출 유무
        bar.setTitle("Buddha\'s Hand");   // 액션바 타이틀 라벨

        // 액션바에 모드 설정 = ActionBar.NAVIGATION_MODE_TABS 로 TAB 모드로 설정
        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // 액션바에 추가될 탭 생성
        ActionBar.Tab tab1 = bar.newTab().setText("연락처").setTabListener(tabListener);
        ActionBar.Tab tab2 = bar.newTab().setText("갤러리").setTabListener(tabListener);
        ActionBar.Tab tab3 = bar.newTab().setText("부다스핸드").setTabListener(tabListener);

        // 액션바에 탭 추가
        bar.addTab(tab1);
        bar.addTab(tab2);
        bar.addTab(tab3 );

        // 각 탭에 들어갈 프라그먼트 생성 및 추가
        fList = new ArrayList<Fragment>();
        fList.add(Fragment1.newInstance());
        fList.add(Fragment2.newInstance());
        fList.add(Fragment3.newInstance());

        // 스와이프로 탭간 이동할 뷰페이저의 리스너 설정
        mViewPager.setOnPageChangeListener(viewPagerListener);

        // 뷰페이져의 아답터 생성 및 연결
        CustomFragmentPagerAdapter fragmentAdapter = new CustomFragmentPagerAdapter(fm, fList);
        mViewPager.setAdapter(fragmentAdapter);


        // 처음 시작시 로그인 관련 정보 이용

        loginLayout = (FrameLayout) findViewById(R.id.layout_login);

        String fb_id = readFromSettingFile("FACEBOOKID");
        String fb_name = readFromSettingFile("FACEBOOKNAME");
        if (fb_id.equals("")) {
            token_x = "00000000";
            loginLayout.setVisibility(View.VISIBLE);
            bar.setTitle("Buddha's Hand");
        } else {
            token_x = fb_id;
            loginLayout.setVisibility(View.GONE);
            bar.setTitle(fb_name + " 님의 저장소");
        }



        making=1;
        new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴


    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.fab:
                anim();
                break;
            case R.id.fab1:
                anim();
                if (mViewPager.getCurrentItem() == 0) {
                    // 값 입력 창 제작
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle("기존 연락처 제거");
                    alert.setMessage("이름과 전화번호를 입력해주세요.");

                    final LinearLayout alertlayout = new LinearLayout(this);
                    alertlayout.setOrientation(LinearLayout.VERTICAL);


                    final TextView textViewName = new TextView(this);
                    textViewName.setText("이름 : ");
                    alertlayout.addView(textViewName);

                    final EditText editTextName = new EditText(this);
                    editTextName.setHint("이름을 입력하세요.                    ");
                    alertlayout.addView(editTextName);

                    final TextView textViewPhonenumber = new TextView(this);
                    textViewPhonenumber.setText("전화번호 : ");
                    alertlayout.addView(textViewPhonenumber);

                    final EditText editTextPhonenumber = new EditText(this);
                    editTextPhonenumber.setHint("전화번호를 입력하세요.                    ");
                    alertlayout.addView(editTextPhonenumber);


                    final TextView textViewtemp = new TextView(this);
                    textViewtemp.setText("        ");


                    final LinearLayout wraplayout = new LinearLayout(this);
                    wraplayout.setOrientation(LinearLayout.HORIZONTAL);
                    wraplayout.addView(textViewtemp);
                    wraplayout.addView(alertlayout);

                    alert.setView(wraplayout);

                    alert.setPositiveButton("제거", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                            String username = editTextName.getText().toString();
                            String userphonenumber = editTextPhonenumber.getText().toString();

                            String tempstr = readFromContactFile();

                            if (username.length() == 0 || userphonenumber.length() == 0){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("알림");
                                builder.setMessage("이름과 전화번호를 모두 입력하세요.");
                                builder.setNegativeButton("확인",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                builder.show();
                            } else if (tempstr.contains(username) && tempstr.contains(userphonenumber)) {
                                String expectedstr1 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man1\",\"value\":0}";
                                String expectedstr2 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man2\",\"value\":0}";
                                String expectedstr3 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man3\",\"value\":0}";
                                String expectedstr4 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man4\",\"value\":0}";
                                String expectedstr5 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man5\",\"value\":0}";
                                String expectedstr6 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man6\",\"value\":0}";
                                String expectedstrs[] = {expectedstr1, expectedstr2, expectedstr3, expectedstr4, expectedstr5, expectedstr6};


                                name_x=username;
                                phonenumber_x=userphonenumber;
                                making=3;
                                new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴


                                boolean change = false;

                                for (int i = 0; i < expectedstrs.length; i++) {
                                    if (tempstr.indexOf(expectedstrs[i]) > -1) {
                                        if (tempstr.indexOf(expectedstrs[i]) == 1){
                                            tempstr = tempstr.replace(expectedstrs[i], "");
                                            tempstr = tempstr.replaceFirst(",", "");
                                        } else {
                                            tempstr = tempstr.replace(expectedstrs[i], "");
                                            tempstr = tempstr.replace(",,", ",");
                                        }

                                        // 데이터를 모두 지웠을 경우 처리
                                        if (tempstr.equals("[]"))
                                            tempstr = "";

                                        writeToContactFile(tempstr);
                                        change = true;
                                    }
                                }

                                if (!change) {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                    builder.setTitle("알림");
                                    builder.setMessage("금액이 남은 경우 삭제하실 수 없습니다.");
                                    builder.setNegativeButton("확인",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {

                                                }
                                            });
                                    builder.show();
                                }
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("알림");
                                builder.setMessage("이름과 전화번호가 존재하지 않습니다.");
                                builder.setNegativeButton("확인",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                builder.show();
                            }

                            mViewPager.getAdapter().notifyDataSetChanged();
                            //mViewPager.setCurrentItem(0);
                        }
                    });

                    alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mViewPager.getAdapter().notifyDataSetChanged();
                            //mViewPager.setCurrentItem(0);
                        }
                    });

                    alert.show();

                    mViewPager.refreshDrawableState();
                    //mViewPager.setCurrentItem(2);
                } else if (mViewPager.getCurrentItem() == 1) {
                    startActivityForResult(new Intent(MainActivity.this, GalleryLibrary.class), GET_GALLERY_DATA_REQUEST);
                } else if (mViewPager.getCurrentItem() == 2) {
                    mViewPager.setCurrentItem(0);
                    Snackbar.make(this.mViewPager, "값을 제거할 연락처를 선택하세요.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.fab2:
                anim();
                if (mViewPager.getCurrentItem() == 0) {
                    // 값 입력 창 제작
                    AlertDialog.Builder alert = new AlertDialog.Builder(this);

                    alert.setTitle("새 연락처 추가");
                    alert.setMessage("이름과 전화번호를 입력해주세요.");

                    final LinearLayout alertlayout = new LinearLayout(this);
                    alertlayout.setOrientation(LinearLayout.VERTICAL);


                    final TextView textViewName = new TextView(this);
                    textViewName.setText("이름 : ");
                    alertlayout.addView(textViewName);

                    final EditText editTextName = new EditText(this);
                    editTextName.setHint("이름을 입력하세요.                    ");
                    alertlayout.addView(editTextName);

                    final TextView textViewPhonenumber = new TextView(this);
                    textViewPhonenumber.setText("전화번호 : ");
                    alertlayout.addView(textViewPhonenumber);

                    final EditText editTextPhonenumber = new EditText(this);
                    editTextPhonenumber.setHint("전화번호를 입력하세요.                    ");
                    alertlayout.addView(editTextPhonenumber);


                    final TextView textViewtemp = new TextView(this);
                    textViewtemp.setText("        ");


                    final LinearLayout wraplayout = new LinearLayout(this);
                    wraplayout.setOrientation(LinearLayout.HORIZONTAL);
                    wraplayout.addView(textViewtemp);
                    wraplayout.addView(alertlayout);

                    alert.setView(wraplayout);

                    alert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String username = editTextName.getText().toString();
                            String userphonenumber = editTextPhonenumber.getText().toString();

                            if (username.length() == 0 || userphonenumber.length() == 0){
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                                builder.setTitle("알림");
                                builder.setMessage("이름과 전화번호를 모두 입력하세요.");
                                builder.setNegativeButton("확인",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {

                                            }
                                        });
                                builder.show();
                                mViewPager.getAdapter().notifyDataSetChanged();
                                //mViewPager.setCurrentItem(0);
                            } else {
                                String tempstr = readFromContactFile();

                                if (tempstr.equals(""))
                                {
                                    tempstr = "[{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man1\",\"value\":0}]";
                                }
                                else
                                {
                                    tempstr = tempstr.replace("]","");
                                    String addstr =  ",{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"man1\",\"value\":0}]";
                                    tempstr = tempstr + addstr;
                                    Log.i("addstr", addstr);
                                    Log.i("tempstr addstr", tempstr);
                                }
                                writeToContactFile(tempstr);
                                mViewPager.getAdapter().notifyDataSetChanged();

                                name_x=username;
                                phonenumber_x=userphonenumber;
                                making=2;
                                new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴



                                //mViewPager.setCurrentItem(0);
                            }

                        }
                    });

                    alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            mViewPager.getAdapter().notifyDataSetChanged();
                            //mViewPager.setCurrentItem(0);
                        }
                    });
                    alert.setNeutralButton("연락처 선택", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent mIntent = new Intent(Intent.ACTION_PICK);
                            mIntent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                            startActivityForResult(mIntent, GET_CONTACT_DATA_REQUEST);

                        }
                    });

                    alert.show();

                    mViewPager.refreshDrawableState();
                    //mViewPager.setCurrentItem(2);
                } else if (mViewPager.getCurrentItem() == 1) {
                    startActivityForResult(new Intent(MainActivity.this, GalleryLibrary.class), GET_GALLERY_DATA_REQUEST);
                } else if (mViewPager.getCurrentItem() == 2) {
                    mViewPager.setCurrentItem(0);
                    Snackbar.make(this.mViewPager, "값을 추가할 연락처를 선택하세요.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
            case R.id.fab3:
                anim();
                if (mViewPager.getCurrentItem() == 0) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    EditText editTextCt = (EditText) findViewById(R.id.editTextContact);
                    LinearLayout searchLinearLayout = findViewById(R.id.searchLinearLayout);

                    if (searchLinearLayout.getVisibility() == View.VISIBLE) {
                        searchLinearLayout.setVisibility(View.GONE);
                        editTextCt.setText("");
                        editTextCt.clearFocus();
                        imm.hideSoftInputFromWindow(editTextCt.getWindowToken(), 0);
                    } else {
                        searchLinearLayout.setVisibility(View.VISIBLE);
                        imm.showSoftInput(editTextCt, 0);
                    }









                } else if (mViewPager.getCurrentItem() == 1) {
                    mViewPager.getAdapter().notifyDataSetChanged();
                    Snackbar.make(this.mViewPager, "갤러리를 새로고칩니다.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } else if (mViewPager.getCurrentItem() == 2) {
                    Snackbar.make(this.mViewPager, "회계장부를 새로고칩니다.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
                break;
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // facebook 로그인 관련
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);





        if(requestCode == GET_CONTACT_DATA_REQUEST)
        {
            if(resultCode == RESULT_OK)
            {
                Cursor cursor = getContentResolver().query(data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER}, null, null, null);
                cursor.moveToFirst();
                String sName   = cursor.getString(0);
                String sNumber = cursor.getString(1);
                cursor.close();

                String tempstr = readFromContactFile();

                if (tempstr.equals(""))
                {
                    tempstr = "[{\"name\":\"" + sName + "\",\"phonenumber\":\"" + sNumber + "\",\"photo\":\"man1\",\"value\":0}]";
                }
                else
                {
                    tempstr = tempstr.replace("]","");
                    String addstr =  ",{\"name\":\"" + sName + "\",\"phonenumber\":\"" + sNumber + "\",\"photo\":\"man1\",\"value\":0}]";
                    tempstr = tempstr + addstr;
                    Log.d("addstr", addstr);
                }

                name_x=sName;
                phonenumber_x=sNumber;
                making=2;
                new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴

                writeToContactFile(tempstr);

                Snackbar.make(this.mViewPager, sName + " 님의 연락처가 추가되었습니다.", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                mViewPager.getAdapter().notifyDataSetChanged();
                //mViewPager.setCurrentItem(0);
            }
        } else if(requestCode == GET_GALLERY_DATA_REQUEST)
        {
            mViewPager.getAdapter().notifyDataSetChanged();

        }


        super.onActivityResult(requestCode, resultCode, data);
    }




    public void anim() {

        if (isFabOpen) {
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
        } else {
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
        }
    }

    ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);

            // 뷰페이저 이동시 해당 탭으로 이동
            bar.setSelectedNavigationItem(position);
        }
    };


    ActionBar.TabListener tabListener = new ActionBar.TabListener() {
        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // 해당 탭에서 벚어났을때 처리
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            // 해당 탭을 선택시 처리
            // 해당 탭으로 뷰페이저도 이동
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            // 해당 탭이 다시 선택됐을때 처리
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            Snackbar.make(this.mViewPager, "서버와 동기화를 진행합니다...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();


            new JSONTask_2().execute("http://socrip4.kaist.ac.kr:3480/contacts?token=" + token_x);
            new JSONTask_GetServerList().execute("http://socrip4.kaist.ac.kr:3480/photos?token=" + token_x);

            mViewPager.refreshDrawableState();
            mViewPager.getAdapter().notifyDataSetChanged();

            Snackbar.make(this.mViewPager, "동기화가 완료되었습니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return true;


        }

        if (id == R.id.action_logout) {
            Snackbar.make(this.mViewPager, "로그아웃하였습니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            LoginManager.getInstance().logOut();
            writeToSettingFile("FACEBOOKID", "");
            writeToSettingFile("FACEBOOKNAME", "noname");
            token_x = "00000000";

            loginLayout.setVisibility(View.VISIBLE);
            bar.setTitle("Buddha's Hand");

            mViewPager.refreshDrawableState();
            mViewPager.getAdapter().notifyDataSetChanged();



            return true;


        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            // Json 파일 테스트용 저장
            String str =
                    "[{'name':'Pak Jeong-Hun','phonenumber':'010-3659-1044','photo':'man1','value':0}," +
                            "{'name':'Won Jung-Eun','phonenumber':'033-8015-2264','photo':'woman3','value':1000}," +
                            "{'name':'Hong Byung-Hoon','phonenumber':'052-1624-1104','photo':'man3','value':-1000}," +
                            "{'name':'Sop Kyong-Su','phonenumber':'010-4728-5356','photo':'man2','value':0}," +
                            "{'name':'Chom Kang-Dae','phonenumber':'062-4032-6077','photo':'man2','value':2000}," +
                            "{'name':'Chong Minjun','phonenumber':'051-7086-4133','photo':'man3','value':2000}," +
                            "{'name':'Chu Song-Ho','phonenumber':'1588-7473','photo':'man1','value':0}," +
                            "{'name':'Chu Chi-Won','phonenumber':'02-2585-1613','photo':'man1','value':0}," +
                            "{'name':'Eoh Ji-Hoon','phonenumber':'02-1265-5822','photo':'man3','value':0}," +
                            "{'name':'An Ji-Won','phonenumber':'040-9425-5912','photo':'woman1','value':0}," +
                            "{'name':'Tan Hyun-Ju','phonenumber':'1588-3958','photo':'man1','value':0}," +
                            "{'name':'Hung Min-Yung','phonenumber':'047-3833-5090','photo':'woman2','value':0}," +
                            "{'name':'Ri Mi-Sook','phonenumber':'068-5790-5717','photo':'woman3','value':0}," +
                            "{'name':'Chegal Suk-Ja','phonenumber':'1588-9206','photo':'woman3','value':0}," +
                            "{'name':'Pong Yi','phonenumber':'1588-9545','photo':'man1','value':0}," +
                            "{'name':'Chung Yu-Ni','phonenumber':'1588-7875','photo':'woman1','value':0}," +
                            "{'name':'Hwan Kyong-Ja','phonenumber':'02-4179-7747','photo':'woman2','value':0}," +
                            "{'name':'Ogum Un-Ju','phonenumber':'010-4485-3333','photo':'woman1','value':0}," +
                            "{'name':'Mangjol Hyon-Ju','phonenumber':'053-8542-5040','photo':'woman3','value':0}," +
                            "{'name':'Mae Tae-Young','phonenumber':'010-9034-0169','photo':'man1','value':0}," +
                            "{'name':'Pom Ji-Hun','phonenumber':'1588-1277','photo':'man3','value':0}," +
                            "{'name':'Ki Chuwon','phonenumber':'02-8037-5149','photo':'man2','value':0}," +
                            "{'name':'Kun Min-Jun','phonenumber':'031-9784-3958','photo':'man3','value':0}," +
                            "{'name':'Chang Kang-Dae','phonenumber':'068-8434-2971','photo':'man2','value':-20000}," +
                            "{'name':'Kim Min-Su','phonenumber':'059-8651-7823','photo':'man1','value':0}," +
                            "{'name':'Pang Min-Kyu','phonenumber':'054-8456-3648','photo':'man1','value':0}," +
                            "{'name':'Chu Kyong-Su','phonenumber':'064-3639-9267','photo':'man3','value':0}," +
                            "{'name':'Nang Suk-Chul','phonenumber':'067-0436-4190','photo':'man2','value':0}," +
                            "{'name':'Ru Dong-Jun','phonenumber':'010-9296-0249','photo':'man3','value':0}," +
                            "{'name':'Sung Sang-Min','phonenumber':'042-3650-9642','photo':'man2','value':0}," +
                            "{'name':'Yun Myung-Hee','phonenumber':'052-4368-7394','photo':'woman1','value':0}," +
                            "{'name':'Min Se-Yeon','phonenumber':'02-4893-7244','photo':'woman2','value':0}," +
                            "{'name':'Pom Mi-Suk','phonenumber':'033-7327-2767','photo':'man3','value':0}," +
                            "{'name':'Yang Ja-Hyun','phonenumber':'054-9152-2803','photo':'woman2','value':0}," +
                            "{'name':'Pung Da-Hee','phonenumber':'010-3425-5002','photo':'woman3','value':0}," +
                            "{'name':'Kye Eun-Ah','phonenumber':'070-5128-4608','photo':'woman1','value':0}," +
                            "{'name':'Kun Sujin','phonenumber':'02-4429-7810','photo':'woman2','value':0}," +
                            "{'name':'Pae Ae','phonenumber':'070-8311-5537','photo':'man1','value':0}," +
                            "{'name':'Ra Hwi-Hyang','phonenumber':'1588-0132','photo':'man3','value':0}," +
                            "{'name':'Sip Eun-Bi','phonenumber':'1588-9463','photo':'woman3','value':0}," +
                            "{'name':'Ogum Yong-Gi','phonenumber':'046-0213-2011','photo':'man2','value':0}," +
                            "{'name':'Chang Seong-Hyeon','phonenumber':'032-9671-4492','photo':'man3','value':0}," +
                            "{'name':'Om Young-Soo','phonenumber':'036-3070-9286','photo':'man1','value':0}," +
                            "{'name':'Hwang Seung-Woo','phonenumber':'036-4224-7732','photo':'man3','value':0}," +
                            "{'name':'Tae Song-Ho','phonenumber':'066-0719-8085','photo':'man2','value':0}," +
                            "{'name':'Pyong Sang-Chul','phonenumber':'02-0421-3789','photo':'man2','value':0}," +
                            "{'name':'Ko Myung-Hee','phonenumber':'053-3212-4003','photo':'woman2','value':0}," +
                            "{'name':'Chegal Jung-Nam','phonenumber':'1588-9258','photo':'man1','value':20000}," +
                            "{'name':'Nae Kwang-Jo','phonenumber':'061-9388-5237','photo':'man3','value':0}," +
                            "{'name':'Kwok Sunghyon','phonenumber':'031-3499-3751','photo':'man2','value':0}]";


            //JSON 파일 리셋
            //writeToContactFile(str);                               /////////////tihs is important

            new JSONTask_2().execute("http://socrip4.kaist.ac.kr:3480/contacts?token="+token_x);//AsyncTask 시작시킴
            mViewPager.refreshDrawableState();
            Snackbar.make(this.mViewPager, "연락처가 초기화되었습니다.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }

        if (id == R.id.action_developers) {
            Snackbar.make(this.mViewPager, "개발자: 배인환, 허재", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void writeToContactFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput("contact.json", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("WriteFile", "Exception: File write failed (contact.json)");
        }
    }


    private String readFromContactFile() {

        String ret = "";

        try {
            InputStream inputStream = this.openFileInput("contact.json");

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
            Log.e("ReadFile", "Exception: File not found (contact.json)");
        } catch (IOException e) {
            Log.e("ReadFile", "Exception: Can not read file (contact.json)");
        }

        return ret;
    }


    private void writeToSettingFile(String title, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.openFileOutput(title + ".cfg", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("WriteFile", "Exception: File write failed (" + title + ")");
        }
    }

    private String readFromSettingFile(String title) {
        String ret = "";
        try {
            InputStream inputStream = this.openFileInput(title + ".cfg");
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


    // 서버에서 연락처 가져오기
    public class JSONTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", name_x);
                jsonObject.accumulate("phonenumber", phonenumber_x);
                jsonObject.accumulate("token", token_x);
                jsonObject.accumulate("making", making);
                making=0;
                //name_x=null;
                //phonenumber_x=null;

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try{
                    //URL url = new URL("http://192.168.25.16:3000/users");
                    URL url = new URL(urls[0]);
                    //연결을 함
                    con = (HttpURLConnection) url.openConnection();


                    con.setRequestMethod("POST");//POST방식으로 보냄
                    con.setRequestProperty("Cache-Control", "no-cache");//캐시 설정
                    con.setRequestProperty("Content-Type", "application/json");//application JSON 형식으로 전송
                    con.setRequestProperty("Accept", "text/html");//서버에 response 데이터를 html로 받음
                    con.setDoOutput(true);//Outstream으로 post 데이터를 넘겨주겠다는 의미
                    con.setDoInput(true);//Inputstream으로 서버로부터 응답을 받겠다는 의미
                    con.connect();

                    //서버로 보내기위해서 스트림 만듬
                    OutputStream outStream = con.getOutputStream();
                    //버퍼를 생성하고 넣음
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                    writer.write(jsonObject.toString());
                    writer.flush();
                    writer.close();//버퍼를 받아줌

                    //서버로 부터 데이터를 받음
                    InputStream stream = con.getInputStream();

                    reader = new BufferedReader(new InputStreamReader(stream));

                    StringBuffer buffer = new StringBuffer();

                    String line = "";
                    while((line = reader.readLine()) != null){
                        buffer.append(line);
                    }
                    //writeToContactFile(buffer.toString());
                    //Log.i(buffer.toString(), "=============================================");

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e){
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if(con != null){
                        con.disconnect();
                    }
                    try {
                        if(reader != null){
                            reader.close();//버퍼를 닫아줌
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
            //writeToContactFile(result);
        }
    }

    public class JSONTask_2 extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... urls) {
            try {


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
                    writeToContactFile(buffer.toString());
                    Log.i("JSON", "Overwrite success");
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
            super.onPostExecute(result);
            writeToContactFile(result);
        }

    }

    // 서버에서 이미지 배열 가져오기
    public class JSONTask_GetServerList extends AsyncTask<String, String, String>{

        @Override
        protected String doInBackground(String... urls) {
            try {
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
            super.onPostExecute(result);

            if (result == null)
                result = "";

            String galleryList = "";

            // 서버에서 가져와 imageselectedlist에 저장
            try {
                JSONArray jarray = new JSONArray(result);   // JSONArray 생성
                for(int i=0; i < jarray.length(); i++){
                    JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                    String id = jObject.getString("id");
                    String img_path = jObject.getString("img_path");
                    String img_name = jObject.getString("img_name");

                    if (galleryList.length() != 0)
                        galleryList = galleryList + "///SPLIT///";
                    galleryList = galleryList + img_path + img_name;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("JSONArray", "Parsing Error Currupt");
            }
            Log.d("galleryList","galleryList: " + galleryList);
            writeToSettingFile("GALLERY_SELECTED_IMAGE_LIST", galleryList);

            mViewPager.refreshDrawableState();
            mViewPager.getAdapter().notifyDataSetChanged();
        }

    }







    /***************************************************
     * Target API 22 이상으로, 수동 권한 요청
     ***************************************************/

    private void requestAllPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) { }
            else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE); }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE)) { }
            else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_CALL_PHONE); }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_CONTACTS)) { }
            else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_REQUEST_READ_CONTACTS); }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXT_STORAGE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { }
                else { }
                return;
            }
            case MY_PERMISSIONS_REQUEST_CALL_PHONE : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { }
                else { }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS : {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { }
                else { }
                return;
            }

        }
    }



}
