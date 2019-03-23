package com.replon.www.gaurdmanager;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AddVehicleActivity extends AppCompatActivity {

    public static final String TAG = "AddVehicleActivity";

    ImageView back_add_veh;
    Button add_vehicle;
    EditText et_vehicle_number;
    String car_type;

    //for images
    Uri selectedImageURI;
    Uri downloadUri;
    String imageURL="";
    StorageTask uploadTask;
    FirebaseStorage storage;
    StorageReference storageReference;
    ImageView vehicle_image;
    Bitmap bitmap_photo;
    List<Image> vehicleList;
    View btn_vehicle_add;


    private static final int REQUEST_CAMERA = 100;
    private static final int RESULT_LOAD_IMAGE = 69;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainrel);
        relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });

        back_add_veh = (ImageView) findViewById(R.id.back_add_veh);
        add_vehicle = (Button) findViewById(R.id.add_vehicle);
        et_vehicle_number=(EditText)findViewById(R.id.vehicle_number);


        back_add_veh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        // Spinner element
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);


        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        //categories.add("");
        categories.add("4 - Wheeler");
        categories.add("2 - Wheeler");



        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);




        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                car_type= String.valueOf(spinner.getSelectedItem());
            }


            @Override
            public void onNothingSelected(AdapterView<?> parent) {

                car_type="";


            }
        });

        //add Image
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        vehicle_image=(ImageView)findViewById(R.id.vehicle_image);
        vehicleList=new ArrayList<>();
        btn_vehicle_add=(View) findViewById(R.id.borderview);
        btn_vehicle_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogShowPhoto();
            }
        });



        add_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String vehicle_number=et_vehicle_number.getText().toString().toUpperCase();
                Intent intent=new Intent();
                intent.putExtra("vehicle_number",vehicle_number);
                intent.putExtra("car_type",car_type);
                intent.putExtra("imageURL",imageURL);
//                Bitmap bitmap = bitmap_photo; // your bitmap
//                ByteArrayOutputStream _bs = new ByteArrayOutputStream();
//                bitmap.compress(Bitmap.CompressFormat.PNG, 50, _bs);
//                intent.putExtra("byteArray", _bs.toByteArray());
                setResult(RESULT_OK,intent);
                finish();


            }
        });
    }


    private void uploadImage(){
        final StorageReference ref = storageReference.child("vehicle_images/"+ UUID.randomUUID().toString());
        uploadTask = ref.putFile(selectedImageURI);

        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    downloadUri = task.getResult();
                    imageURL=downloadUri.toString();
                    Log.i(TAG,"The URL for this image is "+downloadUri);
                    Toast.makeText(getApplicationContext(),"Uploaded",Toast.LENGTH_SHORT).show();
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
    }

    public void dialogShowPhoto() {
        String takePhoto = "Take Photo";
        String chooseFromLibrary = "Choose from Gallery";
        final CharSequence[] items = {takePhoto, chooseFromLibrary};
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        final String finalTakephoto = takePhoto;
        final String finalChooseFromLibrary = chooseFromLibrary;

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(finalChooseFromLibrary)) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, RESULT_LOAD_IMAGE);
                }
                else if (items[item].equals(finalTakephoto)) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, REQUEST_CAMERA);
                }
            }
        });
        builder.show();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == Activity.RESULT_OK) {


            bitmap_photo=(Bitmap)data.getExtras().get("data");
            vehicle_image.setImageBitmap(bitmap_photo);
            selectedImageURI=bitmapToUriConverter(bitmap_photo);
            uploadImage();

        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK  && data != null && data.getData() != null) {
                selectedImageURI = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageURI);
               // bitmap_photo = Bitmap.createScaledBitmap(bitmap,50,67,false);
                vehicle_image.setImageBitmap(bitmap);

                Log.i(TAG,"Image set");
                uploadImage();

            } catch (IOException e) {
                e.printStackTrace();
            }


        }
    }

    //to convert bitmap to image uri
    public Uri bitmapToUriConverter(Bitmap mBitmap) {
        Uri uri = null;
        try {
            final BitmapFactory.Options options = new BitmapFactory.Options();
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 100, 100);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            Bitmap newBitmap = Bitmap.createScaledBitmap(mBitmap, 200, 200,
                    true);
            File file = new File(getApplicationContext().getFilesDir(), "Image"
                    + new Random().nextInt() + ".jpeg");
            FileOutputStream out = getApplicationContext().openFileOutput(file.getName(),
                    Context.MODE_PRIVATE);
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            //get absolute path
            String realPath = file.getAbsolutePath();
            File f = new File(realPath);
            uri = Uri.fromFile(f);

        } catch (Exception e) {
            Log.e("Your Error Message", e.getMessage());
        }
        return uri;
    }


    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


}
