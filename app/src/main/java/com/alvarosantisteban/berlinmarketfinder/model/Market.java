package com.alvarosantisteban.berlinmarketfinder.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * A second hand market is defined by its location, the days and hours that opens and the person/company
 * that organizes/manages it.
 */
public class Market {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("bezirk")
    @Expose
    private String neighborhood;
    @SerializedName("location")
    @Expose
    private String name;
    @SerializedName("latitude")
    @Expose
    private String latitude;
    @SerializedName("longitude")
    @Expose
    private String longitude;
    @SerializedName("tage")
    @Expose
    private String openingDays;
    @SerializedName("zeiten")
    @Expose
    private String openingHours;
    @SerializedName("betreiber")
    @Expose
    private String organizerNameAndPhone;
    @SerializedName("email")
    @Expose
    private String organizerEmail;
    @SerializedName("www")
    @Expose
    private String organizerWebsite;
    @SerializedName("bemerkungen")
    @Expose
    private String otherInfo;

    public Market(String id,
                  String neighborhood,
                  String name,
                  String latitude,
                  String longitude,
                  String openingDays,
                  String openingHours,
                  String organizerNameAndPhone,
                  String organizerEmail,
                  String organizerWebsite,
                  String otherInfo) {
        this.id = id;
        this.neighborhood = neighborhood;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingDays = openingDays;
        this.openingHours = openingHours;
        this.organizerNameAndPhone = organizerNameAndPhone;
        this.organizerEmail = organizerEmail;
        this.organizerWebsite = organizerWebsite;
        this.otherInfo = otherInfo;
    }

    public String getId() {
        return id;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getOpeningDays() {
        return openingDays;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public String getOrganizerNameAndPhone() {
        return organizerNameAndPhone;
    }

    public String getOrganizerEmail() {
        return organizerEmail;
    }

    public String getOrganizerWebsite() {
        return organizerWebsite;
    }

    public String getOtherInfo() {
        return otherInfo;
    }
}
