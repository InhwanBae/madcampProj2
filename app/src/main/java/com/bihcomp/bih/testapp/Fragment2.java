package com.bihcomp.bih.testapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

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
import java.util.ArrayList;

import static com.bihcomp.bih.testapp.MainActivity.token_x;



public class Fragment2 extends Fragment {

    public static Fragment2 newInstance(){
        Fragment2 fragment = new Fragment2();
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_fragment2,container,false);
        GridView gridView = view.findViewById(R.id.grid_view);
        final GridViewAdapter ga = new GridViewAdapter(getActivity());
        gridView.setAdapter(ga);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ga.callImageViewer(position);
            }
        });


        // 사진 없을 때 처리
        String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");
        if (!pathname.equals(""))
            view.findViewById(R.id.LayoutGalleryNotFound).setVisibility(View.GONE);
        else
            view.findViewById(R.id.LayoutGalleryNotFound).setVisibility(View.VISIBLE);

        return view;
    }


    /**==========================================
     *              Adapter class
     * ==========================================*/
    public class GridViewAdapter extends BaseAdapter {
        private String imgData;
        private String geoData;
        private ArrayList<String> thumbsDataList;
        private ArrayList<String> thumbsIDList;
        private ArrayList<String> thumbsUrlList;
        private Context mContext;

        // Constructor
        public GridViewAdapter(Context c){
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

            new JSONTask().execute("http://socrip4.kaist.ac.kr:3480/photos?token=" + token_x);


            // 문자열 파싱
            String pathname = readFromSettingFile("GALLERY_SELECTED_IMAGE_LIST");

            //pathname = "/mnt/sdcard/trash/wallet.png///SPLIT////mnt/sdcard/trash/transaction.png///SPLIT////mnt/sdcard/trash/protection.png";
            String[] pathnames = pathname.split("///SPLIT///");

            for (String str:pathnames)
            {
                String[] tempstr = str.split("/");
                thumbsIDs.add("empty");
                thumbsDatas.add("empty");
                thumbsUrlList.add("http://socrip4.kaist.ac.kr:3480/photos/" + tempstr[tempstr.length - 1]);
                Log.i("ParsedURL", "http://socrip4.kaist.ac.kr:3480/photos/" + tempstr[tempstr.length - 1]);
            }
            return;
        }

        private String getImageInfo(String ImageData, String Location, String thumbID){
            String imageDataPath = null;
            String[] proj = {MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.SIZE};
            Cursor imageCursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
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
    }

    private void writeToSettingFile(String title, String data) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput(title + ".cfg", Context.MODE_PRIVATE));
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
            InputStream inputStream = getContext().openFileInput(title + ".cfg");
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




    // 서버에서 이미지 배열 가져오기
    public class JSONTask extends AsyncTask<String, String, String>{

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