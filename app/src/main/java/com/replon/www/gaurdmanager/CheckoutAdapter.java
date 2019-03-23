package com.replon.www.gaurdmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.replon.www.gaurdmanager.GuestCheckoutActivity.TAG;

public class CheckoutAdapter extends RecyclerView.Adapter<CheckoutAdapter.GuestViewHolder> {


  private Context mContext;
    private SortedList<ContentsGuestCheckout> guestList;

    public CheckoutAdapter() {

        guestList=new SortedList<ContentsGuestCheckout>(ContentsGuestCheckout.class, new SortedList.Callback<ContentsGuestCheckout>() {
            @Override
            public int compare(ContentsGuestCheckout contentsGuestCheckout, ContentsGuestCheckout t21) {
                return contentsGuestCheckout.getDate().compareTo(t21.getDate());
            }

            @Override
            public void onChanged(int position, int count) {

                notifyItemRangeChanged(position, count);

            }

            @Override
            public boolean areContentsTheSame(ContentsGuestCheckout oldItem, ContentsGuestCheckout newItem) {
                return oldItem.getDate().equals(newItem.getDate());
            }

            @Override
            public boolean areItemsTheSame(ContentsGuestCheckout item1, ContentsGuestCheckout item2) {
                return item1.getDate().equals(item2.getDate());
            }

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);

            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);

            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {

                notifyItemMoved(fromPosition, toPosition);
            }
        });

    }

    public void addAll(List<ContentsGuestCheckout> contentsGuestCheckouts) {
        guestList.beginBatchedUpdates();
        for (int i = 0; i < contentsGuestCheckouts.size(); i++) {
            guestList.add(contentsGuestCheckouts.get(i));
        }
        guestList.endBatchedUpdates();
    }

    public ContentsGuestCheckout get(int position) {
        return guestList.get(position);
    }

    public void clear() {
        guestList.beginBatchedUpdates();
        //remove items at end, to avoid unnecessary array shifting
        while (guestList.size() > 0) {
            guestList.removeItemAt(guestList.size() - 1);
        }
        guestList.endBatchedUpdates();
    }


    @NonNull
    @Override
    public GuestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater=LayoutInflater.from(viewGroup.getContext());
        View view=layoutInflater.inflate(R.layout.guestlist_rec,null);
        GuestViewHolder holder=new GuestViewHolder(view);
        return holder;
    }


    //Will bind data to ViewHolder(UI elements)
    @Override
    public void onBindViewHolder(@NonNull GuestViewHolder holder, int i) {


        //so this i is position that will give you the specified item from the product list!


        ContentsGuestCheckout contentsGuestCheckout=guestList.get(i);

        holder.guest_name.setText(String.valueOf(contentsGuestCheckout.getName()));
        holder.guest_purpose.setText(String.valueOf(contentsGuestCheckout.getPurpose()));
        holder.dateAndTime.setText(String.valueOf(contentsGuestCheckout.getDate()));
        holder.guest_flatno.setText(String.valueOf(contentsGuestCheckout.getFlatno()));
        holder.dateAndTime_out.setText(String.valueOf(contentsGuestCheckout.getOut_date()));

        //if Image would have been present then
        // commentsViewHolder.Image.setImageDrawable(mContext. getResources().getDrawable(contentsComments.getImage()),null);

        if(contentsGuestCheckout.getCheckout()){
            holder.btn_checkout.setVisibility(View.GONE);
            holder.dateAndTime_out.setText(String.valueOf(contentsGuestCheckout.getOut_date()));

        }
        holder.btn_checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"Button pressed");

            }
        });

    }


    //Will return the size of the list  ie the number of elements available inside the list that is 5
    @Override
    public int getItemCount() {
        return guestList.size();
    }

    class GuestViewHolder extends RecyclerView.ViewHolder {


        TextView guest_name,guest_purpose,guest_flatno,dateAndTime, dateAndTime_out;
        RelativeLayout row;
        Button btn_checkout;


        public GuestViewHolder(@NonNull View itemView) {
            super(itemView);

            guest_name = itemView.findViewById(R.id.guest_name);
            guest_purpose = itemView.findViewById(R.id.guest_purpose);
            guest_flatno = itemView.findViewById(R.id.guest_flatno);
            dateAndTime = itemView.findViewById(R.id.dateAndTime);
            dateAndTime_out = itemView.findViewById(R.id.dateAndTime_out);
            row = (RelativeLayout)itemView.findViewById(R.id.row);
            btn_checkout=(Button)itemView.findViewById(R.id.btn_checkout);


        }
    }




}