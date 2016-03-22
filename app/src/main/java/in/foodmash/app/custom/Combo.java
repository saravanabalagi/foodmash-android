package in.foodmash.app.custom;

import android.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

/**
 * Created by Zeke on Sep 10 2015.
 */
public class Combo {

    private int id;
    private int groupSize;
    private int noOfPurchases;
    private Size size;
    private Dish.Label label;
    private Category category;
    private String name;
    private String description;
    private boolean available = false;
    private float price;
    public enum Category { REGULAR, BUDGET, CORPORATE, HEALTH }
    public enum Size { MICRO, MEDIUM, MEGA }

    private String picture;
    private ArrayList<ComboDish> comboDishes = new ArrayList<>();
    private ArrayList<ComboOption> comboOptions = new ArrayList<>();

    public Combo() {}
    public Combo(Combo c) {
        this.id = c.id;
        this.groupSize = c.groupSize;
        this.noOfPurchases = c.noOfPurchases;
        this.size = c.size;
        this.label = c.label;
        this.category = c.category;
        this.name = c.name;
        this.description = c.description;
        this.available = c.available;
        this.price = c.price;
        this.picture = c.picture;
        this.comboDishes = new ArrayList<>();
        for (ComboDish comboDish: c.getComboDishes())
            this.comboDishes.add(new ComboDish(comboDish));
        this.comboOptions = new ArrayList<>();
        for (ComboOption comboOption: c.getComboOptions())
            this.comboOptions.add(new ComboOption(comboOption));
    }

    public int getId() { return id; }
    public Size getSize() { return size; }
    @JsonIgnore public int getGroupSize() { return groupSize; }
    @JsonIgnore public int getNoOfPurchases() { return noOfPurchases; }
    @JsonIgnore public Dish.Label getLabel() { return label; }
    @JsonIgnore public Category getCategory() { return category; }
    @JsonIgnore public String getName() { return name; }
    @JsonIgnore public String getDescription() { return description; }
    @JsonIgnore public boolean isAvailable() { return available; }
    public ArrayList<ComboDish> getComboDishes() { return comboDishes; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
    @JsonIgnore public String getPicture() { return picture; }
    @JsonIgnore public float getPrice() { return price; }
    @JsonIgnore public float calculatePrice() {
        float price = 0;
        for (ComboDish comboDish : this.getComboDishes())
            price+=(comboDish.getDish().getPrice()*comboDish.getQuantity());
        for (ComboOption comboOption : this.getComboOptions())
            for(ComboDish comboDish: comboOption.getSelectedComboOptionDishes())
                price+=(comboDish.getDish().getPrice()*comboDish.getQuantity());
        return price;
    }
    @JsonIgnore public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.getComboOptions())
            for(ComboDish comboDish: comboOption.getSelectedComboOptionDishes())
                dishNames += ((comboDish.getQuantity()==1)?"":(comboDish.getQuantity() + " x ")) + comboDish.getDish().getName() + (comboOption.isFromSameRestaurant()?"":" ("+comboDish.getDish().getRestaurant().getName()+") ") +  "\n";
        for (ComboDish comboDish : this.getComboDishes())
            dishNames += ((comboDish.getQuantity()==1)?"":(comboDish.getQuantity() + " x ")) + comboDish.getDish().getName() +  "\n";
        return dishNames.substring(0,dishNames.length()-1);
    }

    @JsonIgnore public ArrayList<Pair<String,Dish.Label>> getContents() {
        ArrayList<Pair<String,Dish.Label>> contents = new ArrayList<>();
        for (ComboOption comboOption : this.getComboOptions()) {
            String comboOptions = "";
            for(ComboDish comboDish: comboOption.getComboOptionDishes())
                comboOptions += comboDish.getDish().getName() + "/ ";
            contents.add(new Pair<>(((comboOption.getMinCount()==1)?"":comboOption.getMinCount()+"x ") + comboOptions.substring(0, comboOptions.length() - 2), comboOption.getLabel()));
        }
        for (ComboDish comboDish : this.getComboDishes())
            contents.add(new Pair<>(((comboDish.getMinCount()==1)?"":comboDish.getMinCount()+"x ")+comboDish.getDish().getName(),comboDish.getDish().getLabel()));
        return contents;
    }

    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
        if( groupSize==1) size = Size.MICRO;
        else if( groupSize>=2 && groupSize<=3 ) size = Size.MEDIUM;
        else if( groupSize>3) size = Size.MEGA;
    }
    @JsonProperty public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    @JsonProperty public void setLabel(String label) {
        switch (label) {
            case "egg": this.label = Dish.Label.EGG; break;
            case "veg": this.label = Dish.Label.VEG; break;
            case "non-veg": this.label = Dish.Label.NON_VEG; break;
        }
    }
    @JsonProperty public void setCategory(String category) {
        switch (category) {
            case "Regular": this.category = Category.REGULAR; break;
            case "Budget": this.category = Category.BUDGET; break;
            case "Corporate": this.category = Category.CORPORATE; break;
            case "Health": this.category = Category.HEALTH; break;
        }
    }
    @JsonProperty public void setName(String name) { this.name = name; }
    @JsonProperty public void setPrice(float price) { this.price = price; }
    @JsonProperty public void setDescription(String description) { this.description = description; }
    @JsonProperty public void setAvailable(boolean available) { this.available = available; }
    @JsonProperty public void setComboDishes(ArrayList<ComboDish> comboDishes) { this.comboDishes = comboDishes; }
    @JsonProperty public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }
    @JsonProperty public void setPicture(String picture) { this.picture = picture; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combo)) return false;
        if (o == this) return true;
        Combo combo = (Combo) o;
        if(this.getId() == combo.getId()) {
            if(!(this.comboDishes.equals(((Combo) o).comboDishes))) return false;
            if(this.comboOptions.size()!=0)
                if (!(comboOptions.equals(((Combo) o).comboOptions))) return false;
            return true;
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31*hash + this.getId();
        for(ComboOption comboOption: comboOptions)
            for(ComboDish comboDish: comboOption.getSelectedComboOptionDishes())
                hash = 31 * hash + comboDish.hashCode();
        for(ComboDish comboDish: comboDishes) hash = 31 * hash + comboDish.hashCode();
        return hash;
    }
}
