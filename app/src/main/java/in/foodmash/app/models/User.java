package in.foodmash.app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;

import in.foodmash.app.utils.DateUtils;

/**
 * Created by Zeke on May 13, 2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private String name;
    private String email;
    private String mobileNo;
    private Date dob;
    private double mashCash;
    private boolean offers;
    private boolean verified;

    private static User mUser;
    public synchronized static void setInstance(User user) { mUser = user; }
    public synchronized static User getInstance() {
        if(mUser==null) { mUser = new User(); }
        return mUser;
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobileNo() { return mobileNo; }
    public Date getDob() { return dob; }
    public boolean isOffers() { return offers; }
    public boolean isVerified() { return verified; }
    public void setName(String name) { this.name = name; }
    public double getMashCash() { return mashCash; }

    public void setEmail(String email) { this.email = email; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }
    public void setDob(String dob) { if(dob==null) this.dob=null; else this.dob = DateUtils.ddmmyyslashDateStringToJavaDate(dob); }
    public void setOffers(boolean offers) { this.offers = offers; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public void setMashCash(double mashCash) { this.mashCash = mashCash; }
}
