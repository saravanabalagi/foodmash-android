package in.foodmash.app.custom;

/**
 * Created by sarav on Sep 11 2015.
 */
public class Restaurant {

    private int id;
    private String name;

    public int getId() { return id; }
    public String getName() { return name; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Restaurant && (o == this || ((Restaurant) o).id == this.id);
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 23*hash + this.getId();
        return hash;
    }
}
