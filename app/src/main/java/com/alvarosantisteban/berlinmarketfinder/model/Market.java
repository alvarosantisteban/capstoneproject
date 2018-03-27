package com.alvarosantisteban.berlinmarketfinder.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * A second hand market is defined by its location, the days and hours that opens and the person/company
 * that organizes/manages it.
 */
public class Market implements Parcelable{

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
        this.organizerWebsite = ensureWebsiteFormat(organizerWebsite);
        this.otherInfo = otherInfo;
    }

    protected Market(Parcel in) {
        id = in.readString();
        neighborhood = in.readString();
        name = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        openingDays = in.readString();
        openingHours = in.readString();
        organizerNameAndPhone = in.readString();
        organizerEmail = in.readString();
        organizerWebsite = in.readString();
        otherInfo = in.readString();
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

    public String getLatitudeString() {
        return latitude;
    }

    public String getLongitudeString() {
        return longitude;
    }

    public double getLatitude() {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        try {
            Number number = format.parse(latitude);
            return number.doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public double getLongitude() {
        NumberFormat format = NumberFormat.getInstance(Locale.GERMANY);
        try {
            Number number = format.parse(longitude);
            return number.doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
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

    private String ensureWebsiteFormat(@Nullable String websiteUrl) {
        if (websiteUrl != null) {
            if (!websiteUrl.startsWith("http")) {
                return "http://" +websiteUrl;
            }
        }
        return websiteUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Market> CREATOR = new Creator<Market>() {
        @Override
        public Market createFromParcel(Parcel in) {
            return new Market(in);
        }

        @Override
        public Market[] newArray(int size) {
            return new Market[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(neighborhood);
        dest.writeString(name);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(openingDays);
        dest.writeString(openingHours);
        dest.writeString(organizerNameAndPhone);
        dest.writeString(organizerEmail);
        dest.writeString(organizerWebsite);
        dest.writeString(otherInfo);
    }
}
