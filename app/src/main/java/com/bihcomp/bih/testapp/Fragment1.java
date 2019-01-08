package com.bihcomp.bih.testapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static com.bihcomp.bih.testapp.MainActivity.making;
import static com.bihcomp.bih.testapp.MainActivity.name_x;
import static com.bihcomp.bih.testapp.MainActivity.phonenumber_x;
import static com.bihcomp.bih.testapp.MainActivity.token_x;
import static java.lang.Math.min;


public class Fragment1 extends Fragment {

    private ArrayList<String> mList;
    private ListView mListView;
    private ArrayAdapter mAdapter;



    ArrayAdapter<ContactEntry> contactAdapter;
    final ArrayList<ContactEntry> contactList = new ArrayList<ContactEntry>();
    String contactStr = "";
    Context ctx = this.getContext();

    final String[] username = new String[1];
    final String[] userphonenumber = new String[1];
    final boolean[] alertstatus = new boolean[1];
    final View[] tempview = new View[1];


    static String phonenumber_2x = "010-0000-0000";






    public static Fragment1 newInstance(){
        Fragment1 fragment = new Fragment1();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 어뎁터 설정
        contactAdapter = new contactArrayAdapter(getContext(), R.layout.listview_contact, R.id.eName, contactList);







    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_fragment1,container,false);
        tempview[0] = view;



        // 리스트 클릭 가능하게 만들기
        mListView = (ListView) view.findViewById(R.id.listview_tab1);

        mListView.setOnItemClickListener(
                new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> arg0, View view,int pos, long id) {
                        final int index = pos;
                        final ContactEntry ent = contactAdapter.getItem(pos);
                        final String name = ent.getName();
                        final String pn = ent.getPhoneNo().replaceAll("-", "");
                        final String phonn = ent.getPhoneNo();
                        final String photoname = ent.getPhotoName();
                        final int personvalue = ent.getPersonValue();
                        final int _photo = ent.getPhotoId()!=-1?ent.getPhotoId():R.drawable.photo;
                        final int posinstr[] = new int[2];

                        new AlertDialog.Builder(getContext()).setTitle(ent.getName())
                                .setMessage("           " + phonn)
                                .setIcon(ent.getPhotoId())
                                .setNegativeButton("수정", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {

                                        String tempstr = readFromContactFile();
                                        String expectedstr = "{\"name\":\"" + name + " \",\"phonenumber\":\"" + phonn + " \",\"photo\":";
                                        int currentpos = tempstr.indexOf(expectedstr);
                                        int value = 0;
                                        for (int i = currentpos + 36; i < min(currentpos + 100,tempstr.length()); i++){
                                            if (tempstr.charAt(i) == 'v' && tempstr.charAt(i+1) == 'a' && tempstr.charAt(i+2) == 'l' && tempstr.charAt(i+3) == 'u' && tempstr.charAt(i+4) == 'e')
                                            {
                                                posinstr[0] = i + 7;
                                                if (Character.getNumericValue(tempstr.charAt(i+7)) == -1)
                                                {
                                                    for (int j = i+8; tempstr.charAt(j) != '}'; j++)
                                                    {
                                                        value = value * 10 + Character.getNumericValue(tempstr.charAt(j));
                                                        posinstr[1] = j;
                                                        Log.d("getvalue", "" + value);
                                                    }
                                                    value = -value;
                                                    Log.d("getvalue", "" + value);
                                                }
                                                else
                                                {
                                                    for (int j = i+7; tempstr.charAt(j) != '}'; j++)
                                                    {
                                                        value = value * 10 + Character.getNumericValue(tempstr.charAt(j));
                                                        posinstr[1] = j;
                                                        Log.d("getvalue", "" + value);
                                                    }
                                                }

                                                break;
                                            }
                                        }
                                        final int fvalue = value;

                                        AlertDialog.Builder cnalert = new AlertDialog.Builder(getContext());

                                        cnalert.setTitle("연락처 수정");
                                        cnalert.setMessage("수정할 이름과 전화번호를 입력해주세요.");

                                        final LinearLayout alertlayout = new LinearLayout(getContext());
                                        alertlayout.setOrientation(LinearLayout.VERTICAL);

                                        final TextView textViewName = new TextView(getContext());
                                        textViewName.setText("이름 : ");
                                        alertlayout.addView(textViewName);

                                        final EditText editTextName = new EditText(getContext());
                                        editTextName.setHint("이름을 입력하세요.                    ");
                                        editTextName.setText(name);
                                        alertlayout.addView(editTextName);

                                        final TextView textViewPhonenumber = new TextView(getContext());
                                        textViewPhonenumber.setText("전화번호 : ");
                                        alertlayout.addView(textViewPhonenumber);

                                        final EditText editTextPhonenumber = new EditText(getContext());
                                        editTextPhonenumber.setHint("전화번호를 입력하세요.                    ");
                                        editTextPhonenumber.setText(phonn);
                                        alertlayout.addView(editTextPhonenumber);


                                        final TextView textViewtemp = new TextView(getContext());
                                        textViewtemp.setText("        ");


                                        final LinearLayout wraplayout = new LinearLayout(getContext());
                                        wraplayout.setOrientation(LinearLayout.HORIZONTAL);
                                        wraplayout.addView(textViewtemp);
                                        wraplayout.addView(alertlayout);

                                        cnalert.setView(wraplayout);

                                        cnalert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                String username = editTextName.getText().toString();
                                                String userphonenumber = editTextPhonenumber.getText().toString();

                                                if (username.length() == 0 || userphonenumber.length() == 0){
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("알림");
                                                    builder.setMessage("이름과 전화번호를 모두 입력하세요.");
                                                    builder.setNegativeButton("확인",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {

                                                                }
                                                            });
                                                    builder.show();
                                                } else {
                                                    String tempstr = readFromContactFile();
                                                    String expectedstr1 = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + photoname + "\",\"value\":" + fvalue + "}";
                                                    String expectedstrs[] = {expectedstr1};
                                                    Log.i("JSON", expectedstr1);

                                                    String changedstr1 = "{\"name\":\"" + username + "\",\"phonenumber\":\"" + userphonenumber + "\",\"photo\":\"" + photoname + "\",\"value\":" + fvalue + "}";
                                                    String changedstrs[] = {changedstr1};
                                                    Log.i("JSON", changedstr1);

                                                    boolean change = false;

                                                    Log.i("JSON", tempstr);

                                                    for (int i = 0; i < expectedstrs.length; i++) {
                                                        if (tempstr.indexOf(expectedstrs[i]) > -1) {
                                                            if (tempstr.indexOf(expectedstrs[i]) == 1){
                                                                tempstr = tempstr.replace(expectedstrs[i], changedstrs[i]);
                                                            } else {
                                                                tempstr = tempstr.replace(expectedstrs[i], changedstrs[i]);
                                                            }
                                                            writeToContactFile(tempstr);

                                                            contactAdapter.notifyDataSetChanged();
                                                            change = true;
                                                        }
                                                    }

                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                    builder.setTitle("알림");
                                                    if (change) {//change
                                                        builder.setMessage("정상적으로 수정되었습니다.");
                                                        name_x = username;
                                                        phonenumber_x = phonn;
                                                        phonenumber_2x = userphonenumber;
                                                        making = 5;
                                                        new JSONTask_3().execute("http://socrip4.kaist.ac.kr:4180/post");//AsyncTask 시작시킴
                                                    }


                                                    else
                                                        builder.setMessage("값이 변경되지 않았습니다.");
                                                    builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // do nothing
                                                        }
                                                    });
                                                    builder.show();


                                                    EditText editTexttemp = (EditText) tempview[0].findViewById(R.id.editTextContact);
                                                    LinearLayout searchLinearLayouttemp = tempview[0].findViewById(R.id.searchLinearLayout);
                                                    if (searchLinearLayouttemp.getVisibility() == View.VISIBLE)
                                                        refreshContactList(editTexttemp.getText().toString());
                                                    else
                                                        refreshContactList();

                                                    contactAdapter.notifyDataSetChanged();
                                                    mListView.invalidateViews();

                                                }

                                            }
                                        });

                                        cnalert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                            }
                                        });
                                        cnalert.setNeutralButton("사진 변경",new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                final List<String> ListItems = new ArrayList<>();
                                                ListItems.add("남자1");
                                                ListItems.add("남자2");
                                                ListItems.add("남자3");
                                                ListItems.add("여자1");
                                                ListItems.add("여자2");
                                                ListItems.add("여자3");
                                                final CharSequence[] items =  ListItems.toArray(new String[ ListItems.size()]);

                                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                                builder.setTitle("연락처 사진 선택");
                                                builder.setItems(items, new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int pos) {
                                                        String selectedText = items[pos].toString();
                                                        String currentText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + photoname + "\",\"value\":" + personvalue + "}";
                                                        String changedText = "";
                                                        if (selectedText.equals("남자1")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "man1" + "\",\"value\":" + personvalue + "}";
                                                        } else if (selectedText.equals("남자2")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "man2" + "\",\"value\":" + personvalue + "}";
                                                        } else if (selectedText.equals("남자3")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "man3" + "\",\"value\":" + personvalue + "}";
                                                        } else if (selectedText.equals("여자1")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "woman1" + "\",\"value\":" + personvalue + "}";
                                                        } else if (selectedText.equals("여자2")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "woman2" + "\",\"value\":" + personvalue + "}";
                                                        } else if (selectedText.equals("여자3")) {
                                                            changedText = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + "woman3" + "\",\"value\":" + personvalue + "}";
                                                        }

                                                        Log.d("selectedText", selectedText);
                                                        Log.d("currentText", currentText);
                                                        Log.d("changedText", changedText);

                                                        String getstr = readFromContactFile();

                                                        Log.d("getstr", getstr);

                                                        getstr = getstr.replace(currentText, changedText);
                                                        writeToContactFile(getstr);

                                                        Log.d("getstr", getstr);

                                                        EditText editTexttemp = (EditText) tempview[0].findViewById(R.id.editTextContact);
                                                        LinearLayout searchLinearLayouttemp = tempview[0].findViewById(R.id.searchLinearLayout);
                                                        if (searchLinearLayouttemp.getVisibility() == View.VISIBLE)
                                                            refreshContactList(editTexttemp.getText().toString());
                                                        else
                                                            refreshContactList();

                                                        contactAdapter.notifyDataSetChanged();
                                                        mListView.invalidateViews();




                                                    }
                                                });
                                                builder.show();





                                            }
                                        });

                                        cnalert.show();





                                    }
                                })
                                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {

                                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                        builder.setTitle("알림");
                                        builder.setMessage("이 연락처를 제거하시겠습니까?");
                                        builder.setNegativeButton("취소",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {

                                                    }
                                                });
                                        builder.setPositiveButton("확인",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        String tempstr = readFromContactFile();
                                                        String expectedstr1 = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":\"" + photoname + "\",\"value\":" + 0 + "}";
                                                        String expectedstrs[] = {expectedstr1};
                                                        Log.i("JSON", expectedstr1);

                                                        name_x=name;
                                                        phonenumber_x=phonn;
                                                        making=3;
                                                        new JSONTask_3().execute("http://socrip4.kaist.ac.kr:4180/post");//AsyncTask 시작시킴





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

                                                                //Toast.makeText(getContext(), tempstr, Toast.LENGTH_SHORT).show();

                                                                // 데이터를 모두 지웠을 경우 처리
                                                                if (tempstr.equals("[]"))
                                                                    tempstr = "";

                                                                // 연락처 있을 때 없을 때 처리
                                                                if (tempstr.length() != 0)
                                                                    tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.GONE);
                                                                else
                                                                    tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.VISIBLE);

                                                                writeToContactFile(tempstr);
                                                                contactAdapter.remove(ent);
                                                                contactAdapter.notifyDataSetChanged();
                                                                mListView.invalidateViews();
                                                                change = true;
                                                            }
                                                        }

                                                        if (!change) {
                                                            AlertDialog.Builder debuilder = new AlertDialog.Builder(getContext());
                                                            debuilder.setTitle("알림");
                                                            debuilder.setMessage("금액이 남은 경우 삭제하실 수 없습니다.");
                                                            debuilder.setNegativeButton("확인",
                                                                    new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int which) {

                                                                        }
                                                                    });
                                                            debuilder.show();
                                                        }
                                                    }
                                                });
                                        builder.show();
                                    }
                                })
                                .setNeutralButton("통화", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {
                                        int permissionCheck = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE);

                                        if(permissionCheck== PackageManager.PERMISSION_DENIED){
                                            Intent callintent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel: "+pn));
                                            startActivity(callintent);
                                        }else{
                                            Intent callintent = new Intent(Intent.ACTION_CALL, Uri.parse("tel: "+pn));
                                            startActivity(callintent);
                                        }

                                    }
                                })
                                .show();

                    }
                }
        );
        /*
        mListView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener()
                {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> arg0, View view,int pos, long id) {
                        final int index = pos;
                        final ContactEntry ent = contactAdapter.getItem(pos);
                        final String name = ent.getName();
                        final String pn = ent.getPhoneNo().replaceAll("-", "");
                        final String phonn = ent.getPhoneNo();
                        final int _photo = ent.getPhotoId()!=-1?ent.getPhotoId():R.drawable.photo;
                        final int posinstr[] = new int[2];

                        String tempstr = readFromContactFile();
                        String expectedstr = "{\"name\":\"" + name + "\",\"phonenumber\":\"" + phonn + "\",\"photo\":";
                        int currentpos = tempstr.indexOf(expectedstr);
                        int value = 0;
                        for (int i = currentpos + 36; i < min(currentpos + 100,tempstr.length()); i++){
                            if (tempstr.charAt(i) == 'v' && tempstr.charAt(i+1) == 'a' && tempstr.charAt(i+2) == 'l' && tempstr.charAt(i+3) == 'u' && tempstr.charAt(i+4) == 'e')
                            {
                                posinstr[0] = i + 7;
                                if (Character.getNumericValue(tempstr.charAt(i+7)) == -1)
                                {
                                    for (int j = i+8; tempstr.charAt(j) != '}'; j++)
                                    {
                                        value = value * 10 + Character.getNumericValue(tempstr.charAt(j));
                                        posinstr[1] = j;
                                        Log.d("getvalue", "" + value);
                                    }
                                    value = -value;
                                    Log.d("getvalue", "" + value);
                                }
                                else
                                {
                                    for (int j = i+7; tempstr.charAt(j) != '}'; j++)
                                    {
                                        value = value * 10 + Character.getNumericValue(tempstr.charAt(j));
                                        posinstr[1] = j;
                                        Log.d("getvalue", "" + value);
                                    }
                                }

                                break;
                            }
                        }
                        final int fvalue = value;

                        new AlertDialog.Builder(getContext()).setTitle(ent.getName())
                                .setMessage("           " + "금액 : " + value + "원")
                                .setIcon(ent.getPhotoId())
                                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {

                                    }
                                })
                                .setNegativeButton("수정", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dlg, int sumthin) {

                                        AlertDialog.Builder cvalert = new AlertDialog.Builder(getContext());
                                        cvalert.setTitle(name + " 님의 금액 수정");
                                        cvalert.setMessage("가감할 금액을 입력하세요.");

                                        final EditText editTextInput = new EditText(getContext());
                                        editTextInput.setInputType(InputType.TYPE_CLASS_TEXT);
                                        editTextInput.setRawInputType(Configuration.KEYBOARD_12KEY);
                                        editTextInput.setHint("-1000(원)                                        ");

                                        final TextView textViewtemp = new TextView(getContext());
                                        textViewtemp.setText("        ");

                                        final LinearLayout wraplayout = new LinearLayout(getContext());
                                        wraplayout.setOrientation(LinearLayout.HORIZONTAL);
                                        wraplayout.addView(textViewtemp);
                                        wraplayout.addView(editTextInput);

                                        cvalert.setView(wraplayout);
                                        cvalert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                // 아무것도 입력하지 않았을 때, 오류 방지
                                                if (editTextInput.getText().toString().equals(""))
                                                    return;

                                                int editvalue = Integer.parseInt(editTextInput.getText().toString());
                                                Log.d("editvalue", "" + editvalue);
                                                int finalvalue = fvalue + editvalue;

                                                String fixstr = readFromContactFile();
                                                String firstfixstr = fixstr.substring(0, posinstr[0]);
                                                String secondfixstr = fixstr.substring(posinstr[1] + 1, fixstr.length());

                                                Log.d("finalvalue", "" + finalvalue);
                                                fixstr = firstfixstr + finalvalue + secondfixstr;
                                                Log.d("fixstrvalue", fixstr);
                                                writeToContactFile(fixstr);

                                                EditText editTexttemp = (EditText) tempview[0].findViewById(R.id.editTextContact);
                                                LinearLayout searchLinearLayouttemp = tempview[0].findViewById(R.id.searchLinearLayout);
                                                if (searchLinearLayouttemp.getVisibility() == View.VISIBLE)
                                                    refreshContactList(editTexttemp.getText().toString());
                                                else
                                                    refreshContactList();

                                                contactAdapter.notifyDataSetChanged();
                                                mListView.invalidateViews();

                                                // 수정된 금액 리스트 추가

                                                String contentstring = readFromTextFile(name);
                                                boolean noMoneyNoFile = false;
                                                if(contentstring.length() == 0 && ent.getPersonValue() == 0)
                                                    noMoneyNoFile = true;
                                                if(contentstring.length() == 0 && ent.getPersonValue() != 0)
                                                    contentstring = "기존 금액    " + ent.getPersonValue();

                                                long now = System.currentTimeMillis();
                                                Date date = new Date(now);
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                                                String getTime = sdf.format(date);
                                                if (editvalue > 0)
                                                {
                                                    if (noMoneyNoFile)
                                                        contentstring = getTime + "    +" + editvalue;
                                                    else
                                                        contentstring = contentstring + "[split]" + getTime + "    +" + editvalue;
                                                }
                                                else if (editvalue < 0)
                                                {
                                                    if (noMoneyNoFile)
                                                        contentstring = getTime + "    " + editvalue;
                                                    else
                                                        contentstring = contentstring + "[split]" + getTime + "    " + editvalue;
                                                }



                                                writeToTextFile(name, contentstring);

                                            }
                                        });
                                        cvalert.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int whichButton) {

                                            }
                                        });
                                        cvalert.show();




                                    }
                                })
                                .show();
                        return true;
                    }

                }
        );
        */


        // 리스트 리프레시
        refreshContactList();



        /* 추가 버튼 MainActivity로 이동
        Button addButton = (Button) view.findViewById(R.id.add) ;
        addButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // 값 입력 창 제작
                AlertDialog.Builder alert = new AlertDialog.Builder(getContext());

                alert.setTitle("새 연락처 추가");
                alert.setMessage("이름과 전화번호를 입력해주세요.");


                final LinearLayout alertlayout = new LinearLayout(getContext());
                alertlayout.setOrientation(LinearLayout.VERTICAL);

                final EditText editTextName = new EditText(getContext());
                editTextName.setHint("이름");
                alertlayout.addView(editTextName);

                final EditText editTextPhonenumber = new EditText(getContext());
                editTextPhonenumber.setHint("전화번호");
                alertlayout.addView(editTextPhonenumber);

                alert.setView(alertlayout);

                alert.setPositiveButton("추가", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        username[0] = editTextName.getText().toString();
                        userphonenumber[0] = editTextPhonenumber.getText().toString();

                        alertstatus[0] = true;

                        String tempstr = readFromContactFile();
                        tempstr = tempstr.replace("]"," ");
                        String addstr =  ",{'name':'" + username[0] + "','phonenumber':'" + userphonenumber[0] + "','photo':'man1','value':0}]";
                        tempstr = tempstr + addstr;
                        Toast.makeText(getContext(), addstr, Toast.LENGTH_SHORT).show();
                        writeToContactFile(tempstr);


                    }
                });

                alert.setNegativeButton("취소",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        alertstatus[0] = false;
                    }
                });

                alert.show();


                if(alertstatus[0]) {
                    contactAdapter = new contactArrayAdapter(getContext(), R.layout.listview_contact, R.id.eName, contactList);
                    ContactEntry ne = new ContactEntry(username[0], userphonenumber[0], R.drawable.photo);
                    contactList.add(ne);
                }

                // listview 갱신
                contactAdapter.notifyDataSetChanged();
            }
        }) ;
        */


        final EditText editTextCt = (EditText) view.findViewById(R.id.editTextContact);
        editTextCt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //입력하기 전
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //EditText에 변화가 있을 때

                // 리스트 리프레시
                //refreshContactList(editTextCt.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                //입력이 끝났을 때

                // 리스트 리프레시
                refreshContactList(editTextCt.getText().toString());
                contactAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
            }
        });

        Button searchContactButton = (Button) view.findViewById(R.id.add) ;
        searchContactButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                // 값 입력 창 제작


                // 리스트 리프레시
                refreshContactList(editTextCt.getText().toString());
                contactAdapter.notifyDataSetChanged();
                mListView.invalidateViews();
                editTextCt.clearFocus();
                editTextCt.setSelected(false);

                mListView.requestFocus();

            }
        }) ;








        /*


        // Fragment1 리스트 설정
        // 빈 데이터 리스트 생성.
        final ArrayList<String> items = new ArrayList<String>() ;
        // ArrayAdapter 생성. 아이템 View를 선택(single choice)가능하도록 만듦.
        final ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_single_choice, items) ;

        // listview 생성 및 adapter 지정.
        final ListView listview = (ListView) view.findViewById(R.id.listview_tab1) ;

        listview.setAdapter(adapter) ;

        adapter.add("test1");
        adapter.add("test2");

        // add button에 대한 이벤트 처리.
        Button addButton = (Button) view.findViewById(R.id.add) ;
        addButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count;
                count = adapter.getCount();

                // 아이템 추가.
                items.add("LIST" + Integer.toString(count + 1));

                // listview 갱신
                adapter.notifyDataSetChanged();
            }
        }) ;

        // modify button에 대한 이벤트 처리.
        Button modifyButton = (Button) view.findViewById(R.id.modify) ;
        modifyButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count, checked ;
                count = adapter.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = listview.getCheckedItemPosition();
                    if (checked > -1 && checked < count) {
                        // 아이템 수정
                        items.set(checked, Integer.toString(checked+1) + "번 아이템 수정") ;

                        // listview 갱신
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }) ;

        // delete button에 대한 이벤트 처리.
        Button deleteButton = (Button) view.findViewById(R.id.delete) ;
        deleteButton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                int count, checked ;
                count = adapter.getCount() ;

                if (count > 0) {
                    // 현재 선택된 아이템의 position 획득.
                    checked = listview.getCheckedItemPosition();

                    if (checked > -1 && checked < count) {
                        // 아이템 삭제
                        items.remove(checked) ;

                        // listview 선택 초기화.
                        listview.clearChoices();

                        // listview 갱신.
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }) ;
        */

        /*
        ContactEntry ne1 = new ContactEntry("Mia","01034567890", R.drawable.photo);
        contactList.add(ne1);
        ContactEntry ne2 = new ContactEntry("Aki","01062954351", R.drawable.photo);
        contactList.add(ne2);
        ContactEntry ne3 = new ContactEntry("Snoopy","01091534510", -1);
        contactList.add(ne3);
        ContactEntry ne4 = new ContactEntry("Jenny","01034964501", R.drawable.photo);
        contactList.add(ne4);
        ContactEntry ne5 = new ContactEntry("Kim","01043956103", -1);
        contactList.add(ne5);
        */

        // listview 생성 및 adapter 지정.
        final ListView listview = (ListView) view.findViewById(R.id.listview_tab1) ;
        listview.setAdapter(contactAdapter) ;



        return view;
    }



    private final static Comparator<ContactEntry> myComparatorName= new Comparator<ContactEntry>() {
        private final Collator collator = Collator.getInstance();
        @Override
        public int compare(ContactEntry object1, ContactEntry object2) {
            return collator.compare(object1.getName(), object2.getName());
        }
    };


    private class contactArrayAdapter extends ArrayAdapter<ContactEntry> {
        private ArrayList<ContactEntry> items;
        private int rsrc;



        public contactArrayAdapter(Context ctx, int rsrcId, int txtId, ArrayList<ContactEntry> data) {
            super(ctx, rsrcId, txtId, data);
            this.items = data;
            this.rsrc = rsrcId;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = li.inflate(rsrc, null);
            }
            ContactEntry e = items.get(position);
            if (e != null) {
                ((TextView)v.findViewById(R.id.eName)).setText(e.getName());
                ((TextView)v.findViewById(R.id.ePhoneNo)).setText(e.getPhoneNo());
                if (e.getPhotoId() != -1) {
                    ((ImageView)v.findViewById(R.id.ePhoto)).setImageResource(e.getPhotoId());
                } else {
                    ((ImageView)v.findViewById(R.id.ePhoto)).setImageResource(R.drawable.photo);
                }
            }





            return v;
        }


    }


    private void refreshContactList() {
        contactAdapter.clear();

        // Json 파일 읽어오기
        contactStr = readFromContactFile();

        try {
            JSONArray jarray = new JSONArray(contactStr);   // JSONArray 생성
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String phonenumber = jObject.getString("phonenumber");
                String name = jObject.getString("name");
                String photo = jObject.getString("photo");
                int value = jObject.getInt("value");

                Log.d("JSONArray", "phonenumber " + phonenumber + " name " + name + " photo " + photo + " value " + value);

                if(value == 0)
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                        contactList.add(ne);
                    }
                }
                else if(value > 0)
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_plus);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2_plus);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3_plus);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_plus);
                        contactList.add(ne);
                    }
                }
                else if(value < 0)
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_minus);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2_minus);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3_minus);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_minus);
                        contactList.add(ne);
                    }
                }
                else
                {
                    ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                    contactList.add(ne);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSONArray", "Parsing Error Currupt");
        }

        // 연락처 있을 때 없을 때 처리
        if (contactList.size() != 0 || contactStr.length() != 0 || contactStr.equals("[]"))
            tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.GONE);
        else
            tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.VISIBLE);

        // 어뎁터 설정

        Collections.sort(contactList, myComparatorName);
        contactAdapter = new contactArrayAdapter(getContext(), R.layout.listview_contact, R.id.eName, contactList);
    }


    private void refreshContactList(String filterText) {
        contactAdapter.clear();

        // Json 파일 읽어오기
        contactStr = readFromContactFile();

        try {
            JSONArray jarray = new JSONArray(contactStr);   // JSONArray 생성
            for(int i=0; i < jarray.length(); i++){
                JSONObject jObject = jarray.getJSONObject(i);  // JSONObject 추출
                String phonenumber = jObject.getString("phonenumber");
                String name = jObject.getString("name");
                String photo = jObject.getString("photo");
                int value = jObject.getInt("value");

                Log.d("JSONArray", "phonenumber " + phonenumber + " name " + name + " photo " + photo + " value " + value);

                if(value == 0 && (name.toLowerCase().contains(filterText.toLowerCase()) || phonenumber.toLowerCase().contains(filterText.toLowerCase())))
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                        contactList.add(ne);
                    }
                }
                else if(value > 0 && (name.toLowerCase().contains(filterText.toLowerCase()) || phonenumber.toLowerCase().contains(filterText.toLowerCase())))
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_plus);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2_plus);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2_plus);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3_plus);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_plus);
                        contactList.add(ne);
                    }
                }
                else if(value < 0 && (name.toLowerCase().contains(filterText.toLowerCase()) || phonenumber.toLowerCase().contains(filterText.toLowerCase())))
                {
                    if(photo.equals("man1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_minus);
                        contactList.add(ne);
                    } else if (photo.equals("man2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man2_minus);
                        contactList.add(ne);
                    } else if (photo.equals("man3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man3_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman1")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman1_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman2")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman2_minus);
                        contactList.add(ne);
                    } else if (photo.equals("woman3")) {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.woman3_minus);
                        contactList.add(ne);
                    } else {
                        ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1_minus);
                        contactList.add(ne);
                    }
                }
                else
                {
                    /*
                    ContactEntry ne = new ContactEntry(name, phonenumber, photo, value, R.drawable.man1);
                    contactList.add(ne);
                    */
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("JSONArray", "Parsing Error Currupt");
        }

        // 어뎁터 설정

        // 정렬
        Collections.sort(contactList, myComparatorName);


        // 입력 string과 비슷한 위치가 가장 앞쪽에 있도록 변경
        ArrayList<ContactEntry> tempContactList = new ArrayList<ContactEntry>();

        for (int i = 0; contactList.size() != 0 && i < 100; i++)
        {
            Log.d("contactList", "value: " + i);
            Log.d("contactList", "size: " + contactList.size());

            for (int j = 0; j < contactList.size(); j++)
            {
                if (contactList.get(j).getName().toLowerCase().indexOf(filterText.toLowerCase()) == i)
                {
                    tempContactList.add(contactList.get(j));
                    contactList.remove(j);
                    j--;
                }
            }
        }

        for (int i = 0; contactList.size() != 0 && i < 100; i++)
        {
            Log.d("contactList", "value: " + i);
            Log.d("contactList", "size: " + contactList.size());

            for (int j = 0; j < contactList.size(); j++)
            {
                if (contactList.get(j).getPhoneNo().toLowerCase().indexOf(filterText.toLowerCase()) == i)
                {
                    tempContactList.add(contactList.get(j));
                    contactList.remove(j);
                    j--;
                }
            }
        }

        contactList.clear();
        for (int i = 0; i < tempContactList.size(); i++)
            contactList.add(tempContactList.get(i));


        // 연락처 있을 때 없을 때 처리
        if (contactList.size() != 0 || contactStr.length() != 0 || contactStr.equals("[]"))
            tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.GONE);
        else
            tempview[0].findViewById(R.id.LayoutContactNotFound).setVisibility(View.VISIBLE);


        contactAdapter = new contactArrayAdapter(getContext(), R.layout.listview_contact, R.id.eName, contactList);
    }




    private void writeToContactFile(String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("contact.json", Context.MODE_PRIVATE));
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
            InputStream inputStream = getContext().openFileInput("contact.json");
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

    private void writeToTextFile(String title, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(title + ".txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
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


    public class JSONTask_3 extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
                //JSONObject를 만들고 key value 형식으로 값을 저장해준다.
                JSONObject jsonObject = new JSONObject();
                jsonObject.accumulate("name", name_x);
                jsonObject.accumulate("phonenumber", phonenumber_x);
                jsonObject.accumulate("phonenumber2", phonenumber_2x);
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
            //Toast.makeText(A, result, Toast.LENGTH_SHORT).show();
            //writeToContactFile(result);
        }
    }

}