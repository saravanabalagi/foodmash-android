package in.foodmash.app.custom;

import java.util.ArrayList;

/**
 * Created by Zeke on Feb 23, 2016.
 */
public class City {
    int id;
    String name;
    ArrayList<Area> areas;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public String toString() { return getName()+": "+getAreaStringArrayList().toString(); }
    public ArrayList<String> getAreaStringArrayList() {
        ArrayList<String> areaStringArrayList = new ArrayList<>();
        for (Area area : areas) areaStringArrayList.add(area.getName());
        return areaStringArrayList;
    }
    public int getPackagingCentreId(String areaName) {
        for(Area area: areas)
            if(area.getName().equals(areaName))
                return area.getPackagingCentreId();
        return -1;
    }

    public void setName(String name) { this.name = name; }
    public ArrayList<Area> getAreas() { return areas; }
    public void setAreas(ArrayList<Area> areas) { this.areas = areas; }
}
