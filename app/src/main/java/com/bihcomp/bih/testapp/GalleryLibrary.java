package com.bihcomp.bih.testapp;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import static com.bihcomp.bih.testapp.MainActivity.making;
import static com.bihcomp.bih.testapp.MainActivity.name_x;
import static com.bihcomp.bih.testapp.MainActivity.phonenumber_x;
import static com.bihcomp.bih.testapp.MainActivity.token_x;

public class GalleryLibrary extends AppCompatActivity {

    private Context mContext;

    public GridView selectedGridView;
    public GridView allGridView;
    public GridView serverGridView;

    public final int[] Scrollposition = new int[2];


    final String uploadFilePath = "/storage/emulated/0/DCIM/Camera/";
    final String uploadFileName = "IMG_20190106_084159.jpg";
    TextView messageText;
    Button uploadButton;
    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    Toolbar toolbar;

    static String path_url;

    static String img_name;
    static String img_path;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_library);
        mContext = this;

        new GalleryLibrary.JSONTask_GetServerList().execute("http://socrip4.kaist.ac.kr:3480/photos?token=" + token_x);

        uploadButton = (Button) findViewById(R.id.uploadButton);
        messageText = (TextView) findViewById(R.id.messageText);

        messageText.setText("Uploading file path :- '/mnt/shared/sdcard/" + uploadFileName + "'");



        /************* Php script path ****************/
        //upLoadServerUri = "http://143.248.39.118:3000/api/photo";
        upLoadServerUri = "http://socrip4.kaist.ac.kr:3480/api/photo";


        //getSupportActionBar().setTitle("이미지 선택");
        //getSupportActionBar().hide();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("이미지 선택");




        // 하단 갤러리뷰
        GridView gv = (GridView)findViewById(R.id.gallery_view);
        final GridViewAdapter ga[] = new GridViewAdapter[1];
        GridViewAdapter gacontext = new GridViewAdapter(this);
        ga[0] = gacontext;
        gv.setAdapter(ga[0]);

        // 상단 갤러리뷰
        GridView gvsel = (GridView)findViewById(R.id.gallery_view_selected);
        final SelectedGridViewAdapter gasel[] = new SelectedGridViewAdapter[1];
        SelectedGridViewAdapter gaselcontext = new SelectedGridViewAdapter(this);
        gasel[0] = gaselcontext;
        gvsel.setAdapter(gasel[0]);

        // 상단 서버 갤러리뷰
        GridView gvserver = (GridView)findViewById(R.id.gallery_view_server);
        final ServerGridViewAdapter gaserver[] = new ServerGridViewAdapter[1];
        ServerGridViewAdapter gaservercontext = new ServerGridViewAdapter(this);
        gaserver[0] = gaservercontext;
        gvserver.setAdapter(gaserver[0]);



        gv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id){

                // 이미지 선택 시 리스트 추가
                String beforestring = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                String afterstring = "";
                if (beforestring.equals("") && !beforestring.contains(ga[0].getImgPath(position)))
                {
                    afterstring = ga[0].getImgPath(position);
                    writeToSettingFile("GALLERY_SELECTED_IMAGE_LIST", afterstring);
                }
                else if (!beforestring.contains(ga[0].getImgPath(position)))
                {
                    afterstring = beforestring + "///SPLIT///" + ga[0].getImgPath(position);
                    writeToSettingFile("GALLERY_SELECTED_IMAGE_LIST", afterstring);
                }
                Log.d("afterList","galleryList: " + afterstring);

                int index = allGridView.getFirstVisiblePosition() * 120;

                SelectedGridViewAdapter tgasel = new SelectedGridViewAdapter(GalleryLibrary.this);
                GridViewAdapter tga = new GridViewAdapter(GalleryLibrary.this);
                gasel[0] = tgasel;
                ga[0] = tga;
                selectedGridView.setAdapter(tgasel);
                allGridView.setAdapter(tga);
                tgasel.notifyDataSetInvalidated();
                tga.notifyDataSetInvalidated();
                gridViewSetting();
                serverGridViewSetting();

            }
        });

        gvsel.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id){

                // 이미지 선택 시 리스트 제거
                String beforestring = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                String removestring = "";

                Log.d("removetext:beforestring", beforestring);

                Log.d("removetext:removestring", gasel[0].getImgPath(position));
                removestring = beforestring.replace(gasel[0].getImgPath(position), "");
                removestring = removestring.replace("///SPLIT//////SPLIT///", "///SPLIT///");
                if (removestring.indexOf("///SPLIT///") == 0)
                    removestring = removestring.replaceFirst("///SPLIT///", "");

                Log.d("removetext:removestring", removestring);

                writeToSettingFile("GALLERY_SELECTED_IMAGE_LIST", removestring);
                Log.d("galleryList","galleryList: " + removestring);

                gasel[0].removeItem(position);
                gasel[0].notifyDataSetInvalidated();
                gridViewSetting();
                serverGridViewSetting();

                GridViewAdapter tga = new GridViewAdapter(GalleryLibrary.this);
                ga[0] = tga;
                allGridView.setAdapter(tga);
                tga.notifyDataSetInvalidated();

            }
        });

        gvserver.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView parent, View v, int position, long id){

                // 이미지 선택 시 리스트 제거
                String beforestring = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                String removestring = "";

                Log.d("removetext:beforestring", beforestring);

                Log.d("removetext:removestring", gaserver[0].getImgPath(position));
                removestring = beforestring.replace(gaserver[0].getImgPath(position), "");
                removestring = removestring.replace("///SPLIT//////SPLIT///", "///SPLIT///");
                if (removestring.indexOf("///SPLIT///") == 0)
                    removestring = removestring.replaceFirst("///SPLIT///", "");

                Log.d("removetext:removestring", removestring);

                writeToSettingFile("GALLERY_SELECTED_IMAGE_LIST", removestring);
                Log.d("galleryList","galleryList: " + removestring);

                gaserver[0].removeItem(position);
                gaserver[0].notifyDataSetInvalidated();
                gridViewSetting();
                serverGridViewSetting();

                ServerGridViewAdapter tsga = new ServerGridViewAdapter(GalleryLibrary.this);
                gaserver[0] = tsga;
                serverGridView.setAdapter(tsga);
                tsga.notifyDataSetInvalidated();

            }
        });



        selectedGridView = (GridView)findViewById(R.id.gallery_view_selected);
        allGridView = (GridView)findViewById(R.id.gallery_view);
        serverGridView = (GridView)findViewById(R.id.gallery_view_server);

        Scrollposition[0] = allGridView.getScrollX();
        Scrollposition[1] = allGridView.getScrollY();

        gridViewSetting();
        serverGridViewSetting();


        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*
                dialog = ProgressDialog.show(GalleryLibrary.this, "", "Uploading file...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });


                        String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                        String[] pathnames = pathname.split("///SPLIT///");

                        making=7;
                        new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴
                        Log.i("upload file","making7");
                        Log.i("upload file","this[" + pathname + "]");

                        if (!pathname.equals(""))
                        {
                            for (int j=0; j<pathnames.length;j++){
                                Log.i("upload file","making6");
                                uploadFile(pathnames[j]);
                                String[] words=pathnames[j].split("/");
                                img_name=words[words.length-1];
                                String b=pathnames[j];
                                String b2=b.replace(img_name,"");
                                img_path=b2;

                                making=6;
                                new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴
                            }

                        }

                        dialog.dismiss();


                    }
                }).start();
                */
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_gallery, menu);
        return true;
    }

    //추가된 소스, ToolBar에 추가된 항목의 select 이벤트를 처리하는 함수
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send_server:

                dialog = ProgressDialog.show(GalleryLibrary.this, "", "이미지 동기화 중...", true);

                new Thread(new Runnable() {
                    public void run() {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                messageText.setText("uploading started.....");
                            }
                        });


                        String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                        String[] pathnames = pathname.split("///SPLIT///");

                        making=7;
                        new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴
                        Log.i("upload file","making7");
                        Log.i("upload file","this[" + pathname + "]");

                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!pathname.equals(""))
                        {
                            for (int j=0; j<pathnames.length;j++){
                                Log.i("upload file","making6");
                                uploadFile(pathnames[j]);
                                String[] words=pathnames[j].split("/");
                                img_name=words[words.length-1];
                                String b=pathnames[j];
                                String b2=b.replace(img_name,"");
                                img_path=b2;

                                making=6;
                                new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/post");//AsyncTask 시작시킴

                                try {
                                    Thread.sleep(50);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        dialog.dismiss();
                        Snackbar.make(findViewById(R.id.all_gallery_view), "이미지 동기화가 완료되었습니다.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                    }
                }).start();



                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    /**==========================================
     *              하단 Adapter class
     * ==========================================*/
    public class GridViewAdapter extends BaseAdapter {
        private String imgData;
        private String geoData;
        private ArrayList<String> thumbsDataList;
        private ArrayList<String> thumbsIDList;
        private ArrayList<String> thumbsStateList;

        public GridViewAdapter(Context c){
            mContext = c;
            thumbsDataList = new ArrayList<String>();
            thumbsIDList = new ArrayList<String>();
            thumbsStateList = new ArrayList<String>();
            getThumbInfo(thumbsIDList, thumbsDataList);
        }

        public final void callImageViewer(int selectedIndex){
            Intent i = new Intent(mContext, ImagePopup.class);
            String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
            i.putExtra("filename", imgPath);
            startActivityForResult(i, 1);
        }

        public boolean deleteSelected(int sIndex){
            return true;
        }

        public int getCount() {
            return thumbsIDList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {





            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(480, 270));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            }else{
                imageView = (ImageView) convertView;
            }
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inSampleSize = 8;
            Bitmap bmp = BitmapFactory.decodeFile(thumbsDataList.get(position), bo);
            Bitmap resized = Bitmap.createScaledBitmap(bmp, 200, 200, true);


            if (thumbsStateList.get(position).equals("yes")){
                // 최종 Bitmap
                Bitmap resultimage = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);

                // 단색 Bitmap
                Bitmap image = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
                image.eraseColor(android.graphics.Color.rgb(52, 73, 94));

                Paint paint = new Paint();
                paint.setAlpha(100);

                Canvas canvas = new Canvas(resultimage);
                canvas.drawBitmap(resized, new Matrix(), null);
                canvas.drawBitmap(image, new Matrix(), paint);

                Bitmap crop = Bitmap.createBitmap(resultimage, 6, 49, 188, 102);
                //crop = Bitmap.createScaledBitmap(crop, 200, 200, true);

                Canvas canvas2 = new Canvas(resultimage);
                canvas2.drawColor(android.graphics.Color.rgb(52, 73, 94));
                canvas2.drawBitmap(crop, 6,49, null);
                imageView.setImageBitmap(resultimage);
            } else{
                imageView.setImageBitmap(resized);
            }

            return imageView;
        }

        private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas){
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};

            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                String title;
                String thumbsID;
                String thumbsImageID;
                String thumbsData;
                String data;
                String imgSize;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int num = 0;

                // 문자열 파싱
                String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                String[] pathnames = pathname.split("///SPLIT///");


                do {

                    // 문자열 내에 단어가 포함될 경우에만 추가
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    String imageDataPath = imageCursor.getString(imgData);

                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsData = imageCursor.getString(thumbsDataCol);
                    thumbsImageID = imageCursor.getString(thumbsImageIDCol);
                    imgSize = imageCursor.getString(thumbsSizeCol);
                    num++;
                    if (thumbsImageID != null){
                        boolean isItSelected = false;
                        for (String str:pathnames)
                        {
                            if (str.equals(imageDataPath))
                            {
                                isItSelected = true;
                                break;
                            }
                        }
                        thumbsStateList.add(isItSelected?"yes":"no");
                        thumbsIDs.add(thumbsID);
                        thumbsDatas.add(thumbsData);
                    }
                }while (imageCursor.moveToNext());
            }
            imageCursor.close();
            return;
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, "_ID='" + thumbID + "'", null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                if (imageCursor.getCount() > 0){
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = imageCursor.getString(imgData);
                }
            }
            imageCursor.close();
            return imageDataPath;
        }

        public String getImgPath(int selectedIndex){
            String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
            return imgPath;
        }

        public void removeItem(int position) {

            thumbsDataList.remove(position);
            thumbsIDList.remove(position);
            notifyDataSetChanged();
        }
    }





    /**==========================================
     *              상단 Adapter class
     * ==========================================*/
    public class SelectedGridViewAdapter extends BaseAdapter {
        private String imgData;
        private String geoData;
        private ArrayList<String> thumbsDataList;
        private ArrayList<String> thumbsIDList;

        public SelectedGridViewAdapter(Context c){
            mContext = c;
            thumbsDataList = new ArrayList<String>();
            thumbsIDList = new ArrayList<String>();
            getThumbInfo(thumbsIDList, thumbsDataList);
        }

        public final void callImageViewer(int selectedIndex){
            Intent i = new Intent(mContext, ImagePopup.class);
            String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
            i.putExtra("filename", imgPath);
            startActivityForResult(i, 1);
        }

        public boolean deleteSelected(int sIndex){
            return true;
        }

        public int getCount() {
            return thumbsIDList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null){
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(270, 270));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setPadding(2, 2, 2, 2);
            }else{
                imageView = (ImageView) convertView;
            }
            BitmapFactory.Options bo = new BitmapFactory.Options();
            bo.inSampleSize = 8;
            Bitmap bmp = BitmapFactory.decodeFile(thumbsDataList.get(position), bo);
            Bitmap crop = Bitmap.createBitmap(bmp, bmp.getWidth()/8, 0, bmp.getWidth()/4*3, bmp.getHeight());
            Bitmap resized = Bitmap.createScaledBitmap(crop, 95, 95, true);
            imageView.setImageBitmap(resized);

            return imageView;
        }

        private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas){
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};

            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, null, null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                String title;
                String thumbsID;
                String thumbsImageID;
                String thumbsData;
                String data;
                String imgSize;

                int thumbsIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media._ID);
                int thumbsDataCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                int thumbsImageIDCol = imageCursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
                int thumbsSizeCol = imageCursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int num = 0;

                // 문자열 파싱
                String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
                String[] pathnames = pathname.split("///SPLIT///");


                do {
                    thumbsID = imageCursor.getString(thumbsIDCol);
                    thumbsData = imageCursor.getString(thumbsDataCol);
                    thumbsImageID = imageCursor.getString(thumbsImageIDCol);
                    imgSize = imageCursor.getString(thumbsSizeCol);
                    num++;
                    if (thumbsImageID != null){

                        // 문자열 내에 단어가 포함될 경우에만 추가
                        int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                        String imageDataPath = imageCursor.getString(imgData);

                        for (String str:pathnames)
                        {
                            if (str.equals(imageDataPath))
                            {
                                File files = new File(str);

                                if(files.exists()==true) {
                                    //파일이 있을시
                                    thumbsIDs.add(thumbsID);
                                    thumbsDatas.add(thumbsData);
                                } else {
                                    //파일이 없을시
                                }

                                break;
                            }
                        }
                    }
                }while (imageCursor.moveToNext());
            }
            imageCursor.close();
            return;
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, "_ID='" + thumbID + "'", null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                if (imageCursor.getCount() > 0){
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = imageCursor.getString(imgData);
                }
            }
            imageCursor.close();
            return imageDataPath;
        }

        public String getImgPath(int selectedIndex){
            String imgPath = getImageInfo(imgData, geoData, thumbsIDList.get(selectedIndex));
            return imgPath;
        }

        public void removeItem(int position) {

            thumbsDataList.remove(position);
            thumbsIDList.remove(position);
            notifyDataSetChanged();
        }
    }







    /**==========================================
     *            상단 서버 Adapter class
     * ==========================================*/
    public class ServerGridViewAdapter extends BaseAdapter {
        private String imgData;
        private String geoData;
        private ArrayList<String> thumbsDataList;
        private ArrayList<String> thumbsIDList;
        private ArrayList<String> thumbsUrlList;
        private Context mContext;

        // Constructor
        public ServerGridViewAdapter(Context c){
            mContext = c;
            thumbsDataList = new ArrayList<String>();
            thumbsIDList = new ArrayList<String>();
            thumbsUrlList = new ArrayList<String>();
            getThumbInfo(thumbsIDList, thumbsDataList);
        }

        public final void callImageViewer(int selectedIndex){
            final Intent i = new Intent(mContext, ImagePopup.class);
            i.putExtra("filename", thumbsUrlList.get(selectedIndex));
            //startActivity(i);
            startActivityForResult(i, 1);
        }

        public boolean deleteSelected(int sIndex) {
            return true;
        }

        public int getCount() {
            return thumbsIDList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(480, 270));
                imageView.setAdjustViewBounds(false);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(2, 2, 2, 2);
            } else {
                imageView = (ImageView) convertView;
            }


            Log.i("picasso", "trying to get image: " + thumbsUrlList.get(position));
            Picasso.with(mContext).load(thumbsUrlList.get(position)).resize(480, 270).centerCrop().into(imageView);
            Log.i("picasso", "finish to get image: " + thumbsUrlList.get(position));

            return imageView;
        }

        private void getThumbInfo(ArrayList<String> thumbsIDs, ArrayList<String> thumbsDatas){

            // 문자열 파싱
            String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");

            //pathname = "/mnt/sdcard/trash/wallet.png///SPLIT////mnt/sdcard/trash/transaction.png///SPLIT////mnt/sdcard/trash/protection.png";
            String[] pathnames = pathname.split("///SPLIT///");

            for (String str:pathnames)
            {
                File files = new File(str);

                if(files.exists()==true) {
                    //파일이 있을시
                } else {
                    //파일이 없을시
                    String[] tempstr = str.split("/");
                    thumbsIDs.add("empty");
                    thumbsDatas.add(str);
                    thumbsUrlList.add("http://socrip4.kaist.ac.kr:3480/photos/" + tempstr[tempstr.length - 1]);
                    Log.i("ParsedURL", "http://socrip4.kaist.ac.kr:3480/photos/" + tempstr[tempstr.length - 1]);
                }



            }
            return;
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    proj, "_ID='"+ thumbID +"'", null, null);

            if (imageCursor != null && imageCursor.moveToFirst()){
                if (imageCursor.getCount() > 0){
                    int imgData = imageCursor.getColumnIndex(MediaStore.Images.Media.DATA);
                    imageDataPath = imageCursor.getString(imgData);
                }
            }
            imageCursor.close();
            return imageDataPath;
        }

        public String getImgPath(int selectedIndex){
            String imgPath = thumbsDataList.get(selectedIndex);
            return imgPath;
        }

        public void removeItem(int position) {

            thumbsDataList.remove(position);
            thumbsIDList.remove(position);
            thumbsUrlList.remove(position);
            notifyDataSetChanged();
        }
    }






    // 수평 그리드뷰 설정
    private void gridViewSetting() {

        String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
        String[] pathnames = pathname.split("///SPLIT///");

        // this is size of your list with data
        int size = 0;
        for (String str:pathnames) {
            File files = new File(str);
            if (files.exists() == true) {
                //파일이 있을시
                size++;
            } else {
                //파일이 없을시
            }
        }

        // Calculated single Item Layout Width for each grid element .. for me it was ~100dp
        int width = 100 ;

        // than just calculate sizes for layout params and use it
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;

        int totalWidth = (int) (width * size * density);
        int singleItemWidth = (int) (width * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                totalWidth, LinearLayout.LayoutParams.MATCH_PARENT);

        selectedGridView.setLayoutParams(params);
        selectedGridView.setColumnWidth(singleItemWidth);
        selectedGridView.setHorizontalSpacing(2);
        selectedGridView.setStretchMode(GridView.STRETCH_SPACING);
        selectedGridView.setNumColumns(size);
    }


    // 서버 수평 그리드뷰 설정
    private void serverGridViewSetting() {

        String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
        String[] pathnames = pathname.split("///SPLIT///");

        // this is size of your list with data
        int size = 0;
        for (String str:pathnames) {
            File files = new File(str);
            if (files.exists() == true) {
                //파일이 있을시
            } else {
                //파일이 없을시
                size++;
            }
        }

        // Calculated single Item Layout Width for each grid element .. for me it was ~100dp
        int width = 100 ;

        // than just calculate sizes for layout params and use it
        DisplayMetrics dm = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(dm);
        float density = dm.density;

        int totalWidth = (int) (width * size * density);
        int singleItemWidth = (int) (width * density);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                totalWidth, LinearLayout.LayoutParams.MATCH_PARENT);

        serverGridView.setLayoutParams(params);
        serverGridView.setColumnWidth(singleItemWidth);
        serverGridView.setHorizontalSpacing(2);
        serverGridView.setStretchMode(GridView.STRETCH_SPACING);
        serverGridView.setNumColumns(size);


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





























    public int uploadFile(String sourceFileUri) {


        String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {

            dialog.dismiss();

            Log.e("uploadFile", "Source File not exist :"
                    + "/storage/emulated/0/DCIM/Camera/IMG_20190106_084159.jpg");

            runOnUiThread(new Runnable() {
                public void run() {
                    messageText.setText("Source File not exist :"
                            + "/storage/emulated/0/DCIM/Camera/IMG_20190106_084159.jpg");
                }
            });

            return 0;

        } else {
            try {
                //JSONObject jsonObject = new JSONObject();
                //jsonObject.accumulate("name", "a");
                //jsonObject.accumulate("phonenumber", "b");
                //jsonObject.accumulate("token", "c");
                //jsonObject.accumulate("making", 1);



                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);
                //dos.writeBytes(jsonObject.toString());
                dos.writeBytes(lineEnd);



                //OutputStream outStream = conn.getOutputStream();
                //BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStream));
                //writer.write(jsonObject.toString());
                //writer.flush();
                //writer.close();//버퍼를 받아줌





                // create a buffer of  maximum size
                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if (serverResponseCode == 200) {
                    BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()),8192);
                    final StringBuilder response = new StringBuilder();
                    String strLine = null;
                    while ((strLine = input.readLine()) != null) {
                        response.append(strLine);
                    }
                    input.close();

                    runOnUiThread(new Runnable() {
                        public void run() {

                            String msg = "File Upload Completed.\n\n See uploaded file here : \n\n"
                                    + response.toString();

                            messageText.setText(msg);

                            //Snackbar.make(findViewById(R.id.all_gallery_view), "이미지 동기화가 완료되었습니다.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        }
                    });
                }

                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (MalformedURLException ex) {

                dialog.dismiss();
                ex.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("MalformedURLException Exception : check script url.");
                        Snackbar.make(findViewById(R.id.all_gallery_view), "업로드 URL이 잘못되었습니다.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                dialog.dismiss();
                e.printStackTrace();

                runOnUiThread(new Runnable() {
                    public void run() {
                        messageText.setText("Got Exception : see logcat ");
                        Snackbar.make(findViewById(R.id.all_gallery_view), "예외가 발생했습니다.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                });
                Log.e("Upload file Exception", "Exception : " + e.getMessage(), e);
            }
            dialog.dismiss();
            return serverResponseCode;

        } // End else block
    }

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
                jsonObject.accumulate("img_name", img_name);
                jsonObject.accumulate("img_path", img_path);


                making = 0;
                //name_x=null;
                //phonenumber_x=null;

                HttpURLConnection con = null;
                BufferedReader reader = null;

                try {
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
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line);
                    }
                    //writeToContactFile(buffer.toString());
                    //Log.i(buffer.toString(), "=============================================");

                    return buffer.toString();//서버로 부터 받은 값을 리턴해줌 아마 OK!!가 들어올것임

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (con != null) {
                        con.disconnect();
                    }
                    try {
                        if (reader != null) {
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
        }

    }


}

