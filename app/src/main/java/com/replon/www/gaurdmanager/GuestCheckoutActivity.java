package com.replon.www.gaurdmanager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import javax.annotation.Nullable;

import static java.lang.Boolean.TRUE;

public class GuestCheckoutActivity extends AppCompatActivity {

    public static final String TAG = "GuestCheckoutActivity";
    ImageView back_checkout;
    RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    List<ContentsGuestCheckout> guestList;
    CheckoutAdapter adapter;
    String checkout_time = "--NA--";
    String document_id;
    DocumentReference soc_id_ref;

    TextView remove_filters,start_date,end_date,apply_filters;
    DatePicker datePicker;
    DatePickerDialog pickerDialog;

    int dayStart=0, monthStart, yearStart, dayEnd, monthEnd, yearEnd;
    Calendar cldrStart;
    Date dateStart, dateEnd;



    ArrayList<GuestDataFirebase> guestDataList;


    //firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest_checkout);

        back_checkout = (ImageView) findViewById(R.id.back_checkout);
        back_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        guestList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        remove_filters = findViewById(R.id.remove_filters);
        start_date = findViewById(R.id.start_date);
        end_date = findViewById(R.id.end_date);
        apply_filters =findViewById(R.id.apply_filters);

        remove_filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start_date.setText("Set Start Date");
                end_date.setText("Set End Date");
                dayStart=0;
                getData();

            }
        });


        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cldrStart = Calendar.getInstance();
                dayStart = cldrStart.get(Calendar.DAY_OF_MONTH);
                monthStart = cldrStart.get(Calendar.MONTH);
                yearStart = cldrStart.get(Calendar.YEAR);
                pickerDialog = new DatePickerDialog(GuestCheckoutActivity.this,R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                try {
                                    //All your parse Operations
                                    dateStart=sdf.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                    dateEnd=sdf.parse(dayOfMonth+1 + "/" + (monthOfYear + 1) + "/" + year);
                                } catch (ParseException e) {
                                    //Handle exception here, most of the time you will just log it.
                                    e.printStackTrace();
                                }

                                start_date.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                end_date.setText("Set End Date");
                            }
                        }, yearStart, monthStart, dayStart);

                pickerDialog.show();


            }
        });

        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (dayStart == 0) {
                    showMessage("Error!", "Please Enter Start Date First");

                }
                else {

                dayEnd = dayStart + 1;
                monthEnd = monthStart;
                yearEnd = yearStart;


                pickerDialog = new DatePickerDialog(GuestCheckoutActivity.this, R.style.DialogTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

                                try {
                                    //All your parse Operations
                                    dateEnd=sdf.parse(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                } catch (ParseException e) {
                                    //Handle exception here, most of the time you will just log it.
                                    e.printStackTrace();
                                }

                                if(dateEnd.before(dateStart)){
                                    showMessage("Error","Please Enter the Date after Start Date");
                                }
                                else {
                                    end_date.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year);
                                }
                            }
                        }, yearEnd, monthEnd, dayEnd);
                pickerDialog.show();

            }
            }
        });

        apply_filters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDataWithDates();
                callAdapter();
            }
        });



        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        if (dayStart==0){
            getData();
            callAdapter();
        }




    }

    private void callAdapter(){

        adapter=new CheckoutAdapter(){
        @Override
        public void onBindViewHolder(@NonNull final GuestViewHolder holder, int i) {


            //so this i is position that will give you the specified item from the product list!


            final ContentsGuestCheckout contentsGuestCheckout=guestList.get(i);

            holder.guest_name.setText(String.valueOf(contentsGuestCheckout.getName()));
            holder.guest_purpose.setText(String.valueOf(contentsGuestCheckout.getPurpose()));
            holder.dateAndTime.setText(String.valueOf(contentsGuestCheckout.getDate()));
            holder.guest_flatno.setText(String.valueOf(contentsGuestCheckout.getFlatno()));
            holder.dateAndTime_out.setText(String.valueOf(contentsGuestCheckout.getOut_date()));

            //if Image would have been present then
            // commentsViewHolder.Image.setImageDrawable(mContext. getResources().getDrawable(contentsComments.getImage()),null);


            holder.btn_checkout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG,"Button pressed");
                    checkout(contentsGuestCheckout.getDocument_ref().getId());

                }
            });

            if(contentsGuestCheckout.getCheckout()){
                holder.btn_checkout.setVisibility(View.GONE);
                holder.dateAndTime_out.setText(String.valueOf(contentsGuestCheckout.getOut_date()));

            }

        }
    };




    }

    private void getDataWithDates(){

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestData = getString(R.string.guestData);
        guestDataList = new ArrayList<GuestDataFirebase>();



        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()) {
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
                    soc_id_ref = (DocumentReference) snapshot.get("society_id");


                    soc_id_ref.collection(guestData).orderBy("date_created")
                            .startAt(new Timestamp(dateStart)).endAt(new Timestamp(dateEnd))
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {


                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else{

                                        guestList.clear();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        }
                                        else{

                                            for (QueryDocumentSnapshot document : snapshots) {

                                                if (document.get("checkout_time") != null) {

                                                    Date fb_date_out = ((Timestamp) document.get("checkout_time")).toDate();

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String out_date = sfd.format(fb_date_out);

                                                    guestList.add(new ContentsGuestCheckout(
                                                            document.get("name").toString(),
                                                            document.get("purpose").toString(),
                                                            in_date,
                                                            document.get("flat_no").toString(),
                                                            out_date,
                                                            document.get("car_type").toString(),
                                                            document.get("image_url").toString(),
                                                            Boolean.parseBoolean(document.get("checkout").toString()),
                                                            (DocumentReference) document.get("document_id")
                                                    ));


                                                } else {

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    guestList.add(new ContentsGuestCheckout(
                                                            document.get("name").toString(),
                                                            document.get("purpose").toString(),
                                                            in_date,
                                                            document.get("flat_no").toString(),
                                                            checkout_time,
                                                            document.get("car_type").toString(),
                                                            document.get("image_url").toString(),
                                                            Boolean.parseBoolean(document.get("checkout").toString()),
                                                            (DocumentReference) document.get("document_id")
                                                    ));

                                                }
                                            }

                                            adapter.addAll(guestList);
                                            recyclerView.setAdapter(adapter);

                                        }
                                    }


                                }
                            });
                }
            }
        });
    }






    private void getData() {

        final String user = getString(R.string.user);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String user_id = currentFirebaseUser.getUid();
        DocumentReference docRef = db.collection(user).document(user_id);

        final String guestData = getString(R.string.guestData);
        guestDataList = new ArrayList<GuestDataFirebase>();

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable final DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {

                if(e!=null){
                    Log.i(TAG,"Request failed");
                }

                String source = snapshot != null && snapshot.getMetadata().hasPendingWrites()
                        ? "Local" : "Server";

                if(snapshot!=null && snapshot.exists()){
                    Log.d(TAG, source + " data is here ->data: " + snapshot.getData());
                   soc_id_ref = (DocumentReference) snapshot.get("society_id");

                    Log.i(TAG,"society id " + soc_id_ref);

                    soc_id_ref.collection(guestData).orderBy("date_created", Query.Direction.DESCENDING)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {

                                    if (e != null) {
                                        Log.d(TAG, "Error:" + e.getMessage());
                                    }else {
                                        guestList.clear();
//                                      mAdapter.notifyDataSetChanged();

                                        if (snapshots.getDocuments().isEmpty()) {
                                            Log.i(TAG, "No Guests");
                                        } else {

                                            for (QueryDocumentSnapshot document : snapshots) {
                                                String name=document.get("name").toString();
                                                Log.i(TAG,"name is "+name);

                                                if (document.get("checkout_time") != null) {

                                                    Date fb_date_out = ((Timestamp) document.get("checkout_time")).toDate();

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String out_date = sfd.format(fb_date_out);

                                                    guestList.add(new ContentsGuestCheckout(
                                                            document.get("name").toString(),
                                                            document.get("purpose").toString(),
                                                            in_date,
                                                            document.get("flat_no").toString(),
                                                            out_date,
                                                            document.get("car_type").toString(),
                                                            document.get("image_url").toString(),
                                                            Boolean.parseBoolean(document.get("checkout").toString()),
                                                            (DocumentReference) document.get("document_id")
                                                    ));


                                                } else {

                                                    Date fb_date = ((Timestamp) document.get("date_created")).toDate();

                                                    SimpleDateFormat sfd = new SimpleDateFormat("dd/MM/yy, h:mm a");
                                                    String in_date = sfd.format(fb_date);

                                                    guestList.add(new ContentsGuestCheckout(
                                                            document.get("name").toString(),
                                                            document.get("purpose").toString(),
                                                            in_date,
                                                            document.get("flat_no").toString(),
                                                            checkout_time,
                                                            document.get("car_type").toString(),
                                                            document.get("image_url").toString(),
                                                            Boolean.parseBoolean(document.get("checkout").toString()),
                                                            (DocumentReference) document.get("document_id")
                                                    ));

                                                }

                                            }

                                            adapter.addAll(guestList);
                                            recyclerView.setAdapter(adapter);
                                        }
                                    }

                                }
                            });
                }

            }
        });

    }

    private void checkout(String document_id_checkout) {


        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String guestData = getString(R.string.guestData);
        final String user = getString(R.string.user);
        final String user_id = currentFirebaseUser.getUid();

        Log.i(TAG,"Document id inside checkout is "+document_id);
        Log.i(TAG,"Soc_id_ref "+soc_id_ref.toString());

        soc_id_ref.collection(guestData).document(document_id_checkout).update("checkout", TRUE);
        soc_id_ref.collection(guestData).document(document_id_checkout).update("checkout_time", new Timestamp(new Date()));


    }

    public void showMessage(String title, String message){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();

    }


}

