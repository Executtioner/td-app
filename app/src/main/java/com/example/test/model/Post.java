package com.example.test.model;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Post {

    private String booking_number;
    private Integer travelers_present;

    public Post() {

    }

    public Post(String booking_number, Integer travelers_present) {
        this.booking_number = booking_number;
        this.travelers_present = travelers_present;
    }

    public String getBooking_number() {
        return booking_number;
    }

    public void setBooking_number(String booking_number) {
        this.booking_number = booking_number;
    }

    public Integer getTravelers_present() {
        return travelers_present;
    }

    public void setTravelers_present(Integer travelers_present) {
        this.travelers_present = travelers_present;
    }



}
