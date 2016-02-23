package in.foodmash.app.custom;

/**
 * Created by Zeke on Feb 23, 2016.
 */
public class Area {
    int id;
    int packagingCentreId;
    String name;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPackagingCentreId() { return packagingCentreId; }

    public void setPackagingCentreId(int packagingCentreId) { this.packagingCentreId = packagingCentreId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
