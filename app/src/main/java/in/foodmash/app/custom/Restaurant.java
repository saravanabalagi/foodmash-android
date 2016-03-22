package in.foodmash.app.custom;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class Restaurant {

    private int id;
    private String name;
    private String logo;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getLogo() { return logo; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLogo(String logo) { this.logo = logo; }

    @Override public boolean equals(Object o) { return o instanceof Restaurant && (o == this || ((Restaurant) o).id == this.id); }
    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31*hash + this.getId();
        return hash;
    }
}
