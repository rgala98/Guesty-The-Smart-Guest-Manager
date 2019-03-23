package com.replon.www.gaurdmanager;


import com.google.firebase.firestore.DocumentReference;

public class ContentsGuestCheckout {

    private String name,purpose,in_date,flatno,out_date;
    private String car_type,image_url,vehicle_no;
    private Boolean checkout;
    private DocumentReference document_ref;



    public ContentsGuestCheckout(String name, String purpose, String in_date, String flatno, String out_date, String car_type, String image_url,
                                Boolean checkout, DocumentReference document_ref) {
        this.name = name;
        this.purpose = purpose;
        this.in_date = in_date;
        this.flatno = flatno;
        this.out_date = out_date;
        this.car_type = car_type;
        this.image_url = image_url;
        this.checkout = checkout;
        this.document_ref = document_ref;
    }


    public String getIn_date() {
        return in_date;
    }

    public void setIn_date(String in_date) {
        this.in_date = in_date;
    }

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }



    public Boolean getCheckout() {
        return checkout;
    }

    public void setCheckout(Boolean checkout) {
        this.checkout = checkout;
    }

    public DocumentReference getDocument_ref() {
        return document_ref;
    }

    public void setDocument_ref(DocumentReference document_ref) {
        this.document_ref = document_ref;
    }

    public String getOut_date() {
        return out_date;
    }

    public void setOut_date(String out_date) {
        this.out_date = out_date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getDate() {
        return in_date;
    }

    public void setDate(String in_date) {
        this.in_date = in_date;
    }

    public String getFlatno() {
        return flatno;
    }

    public void setFlatno(String flatno) {
        this.flatno = flatno;
    }




}
