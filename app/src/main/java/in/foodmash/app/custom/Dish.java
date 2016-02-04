package in.foodmash.app.custom;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class Dish {

    private int id;
    private String name;
    private String description;
    private Restaurant restaurant;
    private float price;
    private String label;
    private String picture;

    public int getId() { return id; }
    @JsonIgnore public String getName() { return name; }
    @JsonIgnore public String getDescription() { return description; }
    @JsonIgnore public Restaurant getRestaurant() { return restaurant; }
    @JsonIgnore public float getPrice() { return price; }
    @JsonIgnore public String getLabel() { return label; }
    @JsonIgnore public String getPicture() { return picture; }

    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setName(String name) { this.name = name; }
    @JsonProperty public void setDescription(String description) { this.description = description; }
    @JsonProperty public void setRestaurant(Restaurant restaurant) { this.restaurant = restaurant; }
    @JsonProperty public void setPrice(int price) { this.price = price; }
    @JsonProperty public void setLabel(String label) { this.label = label; }
    @JsonProperty public void setPicture(String picture) { this.picture = picture; }

}
