package com.replon.www.gaurdmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.lang.Boolean.FALSE;

public class AddGuestActivity extends AppCompatActivity implements MultiSpinner.OnMultipleItemsSelectedListener{

    public static final String TAG = "AddGuestActivity";
    ImageView back_add_guest;
    Button btn_add_vehicle, btn_add_guest, btn_get_otp;
    MultiSpinner multiSpinner;
    Spinner spinner;

    EditText et_name,et_phoneNumber,veh_num,put_otp;
    String purpose,phone;
    ImageView veh_img;
    TextView veh_type,verify_otp;
    private String mVerificationId,mResendToken;

    private FirebaseAuth mAuth;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;


    ArrayList<ContentAddGuest> guestArrayList;
    List<String> flat_list;

    ProgressBar mProgressBar;
    private int mRequestCode = 100;

    String vehicle_number="--NA--";
    String car_type= "--NA--";
    String imageURL="";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_guest);

        String[] array = {"A - 101", "A - 102","A - 103","A - 104","A - 201","A - 202","A - 203","A - 204","A - 301","A - 302","A - 303","A - 304","A - 401","A - 402","A - 403","A - 404"};
        multiSpinner =  (MultiSpinner) findViewById(R.id.mySpinner);
        multiSpinner.setItems(array);
        multiSpinner.setSelection(new int[]{});
        multiSpinner.setListener(this);
        spinner = findViewById(R.id.spinner1);

        List<String> purposes = new ArrayList<String>();
        purposes.add("Business");
        purposes.add("Personal");
        purposes.add("Delivery");
        purposes.add("Others");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, purposes);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                purpose = String.valueOf(spinner.getSelectedItem());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });



        back_add_guest = (ImageView) findViewById(R.id.back_add_guest);
        btn_add_vehicle = (Button) findViewById(R.id.add_veh);

        et_name=(EditText)findViewById(R.id.name);
        et_phoneNumber=(EditText)findViewById(R.id.phoneNumber);
        put_otp=(EditText) findViewById(R.id.put_otp);
        verify_otp = findViewById(R.id.verify_otp);
        put_otp.setVisibility(View.GONE);
        verify_otp.setVisibility(View.GONE);
        mAuth = FirebaseAuth.getInstance();


            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                @Override
                public void onVerificationCompleted(PhoneAuthCredential credential) {
                    Toast.makeText(getApplicationContext(), "Verification Complete", Toast.LENGTH_SHORT).show();

                    showMessage("Success!!","OTP verified!");
                    btn_add_guest.setEnabled(true);

                }

                @Override
                public void onVerificationFailed(FirebaseException e) {
                    Toast.makeText(getApplicationContext(), "Verification Failed", Toast.LENGTH_SHORT).show();
                    Log.i(TAG,"Error is"+e.getMessage());
                }

                @Override
                public void onCodeSent(String verificationId,
                                       PhoneAuthProvider.ForceResendingToken token) {
                    Toast.makeText(getApplicationContext(), "Code Sent", Toast.LENGTH_SHORT).show();
                    mVerificationId = verificationId;
                    //mResendToken = token;
                    btn_add_guest.setEnabled(false);
                }
            };


        mProgressBar=(ProgressBar)findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        veh_num = (EditText) findViewById(R.id.veh_num);
        veh_type = (TextView) findViewById(R.id.veh_type);

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainrel);
        relativeLayout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                InputMethodManager imm =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

            }
        });



        back_add_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_add_guest =(Button)findViewById(R.id.add_guest);
        btn_get_otp = (Button) findViewById(R.id.btn_get_otp);

        btn_get_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                put_otp.setVisibility(View.VISIBLE);
                verify_otp.setVisibility(View.VISIBLE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber("+91 "+et_phoneNumber.getText().toString(),120, TimeUnit.SECONDS,AddGuestActivity.this,mCallbacks);


            }
        });

        verify_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(put_otp.getText().toString().equals("")){
                    showMessage("Error!","Enter The OTP Provided");
                }
                else{

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, put_otp.getText().toString());
                    mAuth.signInWithCredential(credential).addOnCompleteListener(AddGuestActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(AddGuestActivity.this, "Verification Success", Toast.LENGTH_SHORT).show();
                                btn_add_guest.setEnabled(true);
                            } else {
                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(AddGuestActivity.this, "Verification Failed, Invalid credentials", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });

                }
            }
        });


        btn_add_guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                phone = et_phoneNumber.getText().toString();
                Log.i(TAG,"ADD GUEST PRESSED");


                if(et_name.getText().toString().isEmpty()){

                    showMessage("Error!","Please enter all details");
                }

                else if(multiSpinner.getSelectedIndices().isEmpty()){

                    showMessage("Error!","Please Enter Flat Number(s)");
                }
                if(!phone.equals("")){
                    Character ch = phone.charAt(0);

                    if(phone.length()!=10 || (!ch.equals('9') && !ch.equals('8') && !ch.equals('7'))){


                        showMessage("Error!","Please enter a correct Phone Number");}

                }
                else{
                    Log.i(TAG,"DATA MALE CHE");

                    addData();
                }

            }
        });


        btn_add_vehicle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), AddVehicleActivity.class);
                startActivityForResult(intent,mRequestCode);

            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == mRequestCode && resultCode == RESULT_OK){
            vehicle_number= data.getStringExtra("vehicle_number");
            car_type= data.getStringExtra("car_type");
            imageURL= data.getStringExtra("imageURL");

//            if(data.hasExtra("byteArray")) {
////                ImageView _imv= new ImageView(this);
//                Bitmap _bitmap = BitmapFactory.decodeByteArray(
//                        getIntent().getByteArrayExtra("byteArray"),0,data.getByteArrayExtra("byteArray").length);
//                veh_img.setImageBitmap(_bitmap);
//            }


            veh_num.setText(vehicle_number);
            veh_type.setText(car_type);
            btn_add_vehicle.setVisibility(View.GONE);

            Log.i(TAG,"ImageURL is "+imageURL);
        }
    }




    @Override
    public void selectedIndices(List<Integer> indices) {

        Log.i(TAG,"The selected indices are "+indices);

        if(indices.isEmpty()){
            showMessage("Error!","Please select a flat");

        }

    }

    @Override
    public void selectedStrings(List<String> flats) {
        Log.i(TAG, "Selected flats are " + flats);
        flat_list=flats;

    }


    private void addData() {



        et_name.setEnabled(false);
        et_phoneNumber.setEnabled(false);
        multiSpinner.setEnabled(false);
        veh_num.setEnabled(false);
        spinner.setEnabled(false);


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String guestData = getString(R.string.guestData);
        final String user = getString(R.string.user);
        final String guest_name = getString(R.string.name);
        final String guest_phone_number = getString(R.string.phone_number);
        final String guest_purpose = getString(R.string.purpose);
        final String guest_flat_no = getString(R.string.flat_no);
        final String date_created = getString(R.string.date_created);
        final String guest_user_id = getString(R.string.user_id);
        final String document_id = getString(R.string.document_id);
        final String document_ref="document_ref";
        final String vehicle_number_fb = getString(R.string.vehicle_number);
        final String car_type_fb = getString(R.string.car_type);
        final String image_url = getString(R.string.image_url);
        final String user_id = currentFirebaseUser.getUid();

        guestArrayList = new ArrayList<ContentAddGuest>();



        DocumentReference docRef = db.collection(user).document(user_id);
        mProgressBar.setVisibility(View.VISIBLE);

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot snapshot,
                                @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    return;
                }
                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if (snapshot != null && snapshot.exists()) {
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());


                    final DocumentReference soc_id_ref = (DocumentReference) snapshot.get("society_id");

                    Log.i(TAG, "Society id is " + soc_id_ref);

                    String name = et_name.getText().toString();
                    phone = et_phoneNumber.getText().toString();
                    Long phoneNumber;
                    if(!phone.equals("")){
                    phoneNumber = Long.parseLong(et_phoneNumber.getText().toString());}
                    else {
                        phoneNumber = Long.parseLong("0");
                    }


//                    String dateInString = new java.text.SimpleDateFormat("EEEE, dd/MM/yyyy/hh:mm:ss")
//                            .format(cal.getTime())
//                    SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd/MM/yyyy/hh:mm:ss");
//                    Date parsedDate = formatter.parse(dateInString);




                    final Map<String, Object> data = new HashMap<>();
                    data.put(date_created, new Timestamp(new Date()));
                    data.put(guest_name, name);
                    data.put(guest_phone_number, phoneNumber);
                    data.put(guest_purpose, purpose);
                    data.put(guest_flat_no, flat_list);
                    data.put(guest_user_id, user_id);
                    data.put(vehicle_number_fb, veh_num.getText().toString());
                    data.put(car_type_fb, car_type);
                    data.put(image_url, imageURL);
                    data.put("checkout", FALSE);
                    data.put("checkout_time", null);



                    soc_id_ref.collection(guestData).add(data)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                                    // data.put("document_id",documentReference.getId());
                                    String guestDataId = documentReference.getId();
                                    DocumentReference doc_ref=documentReference;

                                    soc_id_ref.collection(guestData).document(guestDataId).update(document_id,doc_ref);
                                    mProgressBar.setVisibility(View.GONE);
                                    showMessage("", "Data added");

                                    et_name.setEnabled(true);
                                    spinner.setEnabled(true);
                                    et_phoneNumber.setEnabled(true);
                                    multiSpinner.setEnabled(true);

                                    et_name.setText("");
                                    et_phoneNumber.setText("");
                                    veh_num.setText("");
                                    veh_type.setText("");
                                    btn_add_vehicle.setVisibility(View.VISIBLE);
                                    multiSpinner.setSelection(new int[]{});

                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "Error adding document", e);
                                    mProgressBar.setVisibility(View.VISIBLE);
                                    showMessage("Error", "unable to add guest");
                                }
                            });

                } else {
                    Log.d(TAG, source + " data: null");
                }
            }
        });
    }


    public void showMessage(String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }
}
