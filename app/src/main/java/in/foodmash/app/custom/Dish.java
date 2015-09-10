package in.foodmash.app.custom;

/**
 * Created by sarav on Sep 11 2015.
 */
public class Dish {

    private int id;
    private String name;
    private String description;
    private Restaurant restaurant;

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Restaurant getRestaurant() { return restaurant; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }

}
