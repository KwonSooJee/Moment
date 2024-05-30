package org.techtown.moment;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

public class WriteDiaryActivity extends AppCompatActivity {
    private static final String TAG="WriteDiary";

    ImageButton cameraBtn,imageBtn,mapBtn;
    ImageView imageView;
    File file;
    Button saveBtn;
    FloatingActionButton deleteBtn;
    LinearLayout pictureLayout;

    Bitmap resultBitmap;
    Context context;
    OnRequestListener requestListener;
    EditText contentsInput,addressInput;
    double latitude;
    double longitude;

    int REQUEST_IMAGE_CODE=111;
    int REQUEST_CAMERA_CODE=112;

    //final Geocoder geocoder=new Geocoder(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_diary);
        context=this;

        if (context instanceof OnRequestListener) {
            requestListener = (OnRequestListener) context;
        }

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        contentsInput=findViewById(R.id.writeDiaryEditText);
        imageView=findViewById(R.id.imageView);
        pictureLayout=findViewById(R.id.pictureLayout);
        addressInput=findViewById(R.id.addressEditWrite);
        cameraBtn=findViewById(R.id.btn_camera);
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        imageBtn=findViewById(R.id.btn_image);
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
/*
       mapBtn=findViewById(R.id.btn_map);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map map=new Map();
                String address =map.getAddress();
                if(address!=null) {
                    addressInput.setText(address);
                }
            }
        });
        */

        saveBtn=findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDiary();
            }
        });

        deleteBtn=findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deletePicture();
            }
        });
    }

    /*================File===============================*/


    private File createFile() {
        String filename = "capture.jpg";
        File databaseStr = new File(getExternalCacheDir(), filename);

        return databaseStr;
    }

    private String createFilename() {
        Date curDate = new Date();
        String curDateStr = String.valueOf(curDate.getTime());

        return curDateStr;
    }

    /*==============================Camera=========================================*/

    public void takePicture(){
        if (file==null){
            file=createFile();
        }

        Uri fileUri= FileProvider.getUriForFile(this,"org.techtown.moment.fileprovider",file);

        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        if(intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_CAMERA_CODE);
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        resultBitmap=null;


        if(requestCode==REQUEST_CAMERA_CODE&&resultCode==RESULT_OK){



            BitmapFactory.Options options=new BitmapFactory.Options();
            options.inSampleSize=6;
            resultBitmap=BitmapFactory.decodeFile(file.getAbsolutePath(),options);
            pictureLayout.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(resultBitmap);

            Uri uri=data.getData();
            Glide.with(this).load(uri).into(imageView);
            //나중에 해보기


            Log.d("Picture", "picture 실행" );

        }else if(requestCode==REQUEST_IMAGE_CODE&&resultCode==RESULT_OK){

            resultBitmap=null;
            Uri fileUri=data.getData();
            ContentResolver resolver=getContentResolver();

            try {
                InputStream inStream=resolver.openInputStream(fileUri);
                resultBitmap= BitmapFactory.decodeStream(inStream);

                pictureLayout.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(resultBitmap);
                Log.d("Picture", "picture 실행" );

                inStream.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }


    }

    /*========================Image===============================*/

    public void openGallery(){
        if (file==null){
            file=createFile();
        }
        Uri fileUri= FileProvider.getUriForFile(this,"org.techtown.moment.fileprovider",file);

        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,fileUri);

        startActivityForResult(intent,REQUEST_IMAGE_CODE);

    }

    /*====================Save===========================*/

    public String savePicture(){
        if(resultBitmap==null){
            return "";
        }

        File photoFolder =new File(AppConstants.FOLDER_PHOTO);

        if(!photoFolder.isDirectory()){
            Log.d(TAG,"creating photo folder : "+photoFolder);
            photoFolder.mkdir();
        }

        String photoFilename= createFilename();
        String picturePath=photoFolder+File.separator+photoFilename;

        try{
            FileOutputStream outStream=new FileOutputStream(picturePath);
            resultBitmap.compress(Bitmap.CompressFormat.PNG, 100,outStream);

            outStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        return picturePath;
    }



    private void saveDiary() {


        String address = addressInput.getText().toString();
        if(addressInput.getText().toString().equals("주소")){
            address="";
        }

        String contents = contentsInput.getText().toString();

        String picturePath = savePicture();

        String sql = "Insert into " + DiaryDatabase.TABLE_DIARY +
                " ('ADDRESS', 'CONTENTS', 'PICTURE') values(" +
                "'"+ address + "', " +
                "'"+ contents + "', " +
                "'"+ picturePath + "');";

        Log.d(TAG, "sql : " + sql);
        DiaryDatabase database = DiaryDatabase.getInstance(context);
        database.exeSQL(sql);

        this.finish();
    }

    /*========================delete picture===========================*/


    public void deletePicture(){

        imageView.setImageBitmap(null);
        pictureLayout.setVisibility(View.GONE);

    }
    /*====================DiaryData====================

        public void setAddress(String data){
            locationTextView.setText(data);
        }

        public void setItem(DiaryData item){
            this.item=item;
        }

        public void setContents(String data) {
            contextsInput.setText(data);
        }

        public void setPicture(String picturePath,int SampleSize){
            BitmapFactory.Options options =new BitmapFactory.Options();
            options.inSampleSize=SampleSize;
            resultBitmap=BitmapFactory.decodeFile(picturePath,options);

            imageView.setImageBitmap(resultBitmap);
        }

        public void applyItem(){

            AppConstants.println("applyItem called");
            if (item != null) {
                mMode = AppConstants.MODE_MODIFY;

                setAddress(item.getAddress());
                setContents(item.getContents());

                String picturePath = item.getPicture();
                if (picturePath == null || picturePath.equals("")) {
                    imageView.setImageBitmap(null);
                } else {
                    setPicture(item.getPicture(), 1);
                }


            } else {
                mMode = AppConstants.MODE_INSERT;

                setAddress("");

                Date currentDate = new Date();
                String currentDateString = AppConstants.dateFormat3.format(currentDate);

                contextsInput.setText("");

                imageView.setImageBitmap(null);
            }
        }
        */
    /*==========================Map===========================*/
/*
public class Map{

        String Address;

        List<android.location.Address> list=null;


        public Map(){

        }

        public String getAddress(){
            startLocationService();
            ReverseGeo();
            return Address;
        }


        class GPSListener implements LocationListener {
            public void onLocationChanged(Location location){
                showCurrentLocation(location);
            }
        }

        public void startLocationService(){
            LocationManager manager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);

            long minTime=10000;
            float minDistance=0;
            GPSListener gpsListener=new GPSListener();

            try {
                manager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        minTime,
                        minDistance,
                        gpsListener);
            }catch (SecurityException e){
                e.printStackTrace();
            }
        }

        public void showCurrentLocation(Location location){

            latitude=location.getLatitude();
            longitude=location.getLongitude();

            String message = "내 위치 -> Latitude : "+ latitude + "\nLongitude:"+ longitude;
            Log.d("Map", message);
        }
        public void ReverseGeo(){
            try {
                list =geocoder.getFromLocation(
                        latitude,
                        longitude,
                        10
                );
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Map","입출력 오류 - 서버에서 주소 변환 에러");
            }
            if(list!=null){
                if(list.size()==0){
                    showToast("주소를 불러오는데 실패했습니다.");
                }
                else if(list.get(1)!=null){
                    Address=list.get(1).toString();
                }
                else{
                    Address=list.get(0).toString();
                }
            }
        }


    }

 */

    public void showToast(String message){
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
}




