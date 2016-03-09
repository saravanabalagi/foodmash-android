package in.foodmash.app.custom;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by Zeke on Feb 27, 2016.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

    private int id;
    private String name;
    private String line1;
    private String line2;
    private int areaId;
    private double latitude;
    private double longitude;
    private String contactNo;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLine1() { return line1; }
    public String getLine2() { return line2; }
    public int getAreaId() { return areaId; }
    public String getContactNo() { return contactNo; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLine1(String line1) { this.line1 = line1; }
    public void setLine2(String line2) { this.line2 = line2; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAreaId(int areaId) { this.areaId = areaId; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

}
