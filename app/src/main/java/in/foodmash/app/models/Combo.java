package in.foodmash.app.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * Created by Zeke on Sep 10 2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Combo {

    private int id;
    private int groupSize;
    private int noOfPurchases;
    private Size size;
    private Dish.Label label;
    private Category category;
    private String name;
    private String description;
    private String note;
    private boolean available = false;
    private boolean customizable = false;
    private float price;
    public enum Category { REGULAR, BUDGET, CORPORATE, HEALTH }
    public enum Size { MICRO, MEDIUM, MEGA }

    private String picture;
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
        this.comboOptions = new ArrayList<>();
        for (ComboOption comboOption: c.comboOptions)
            this.comboOptions.add(new ComboOption(comboOption));
    }

    public int getId() { return id; }
    public int getGroupSize() { return groupSize; }
    public int getNoOfPurchases() { return noOfPurchases; }
    public Size getSize() { return size; }
    public Dish.Label getLabel() { return label; }
    public Category getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getNote() { return note; }
    public boolean isAvailable() { return available; }
    public boolean isCustomizable() { return customizable; }
    public float getPrice() { return price; }
    public String getPicture() { return picture; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
    public String getType() { return this.getClass().getSimpleName(); }

    public void setId(int id) { this.id = id; }
    public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    public void setSize(Size size) { this.size = size; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setNote(String note) { this.note = note; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setCustomizable(boolean customizable) { this.customizable = customizable; }
    public void setPrice(float price) { this.price = price; }
    public void setPicture(String picture) { this.picture = picture; }
    public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }

    public float calculatePrice() {
        float price = 0;
        for (ComboOption comboOption : this.comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                price+=(comboDish.getDish().getPrice()*comboDish.getQuantity());
        return price;
    }
    public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                dishNames += ((comboDish.getQuantity()==1)?"":(comboDish.getQuantity() + " x ")) + comboDish.getDish().getName() + " ("+comboDish.getDish().getRestaurant().getName()+") " +  "\n";
        return dishNames.substring(0,dishNames.length()-1);
    }

    public ArrayList<String> getImages() {
        ArrayList<String> imageArrayList = new ArrayList<>();
        for(ComboOption comboOption: comboOptions)
            for(ComboOptionDish comboDish: comboOption.getComboOptionDishes())
                imageArrayList.add(comboDish.getDish().getPicture());
        return imageArrayList;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
        if( groupSize==1) size = Size.MICRO;
        else if( groupSize>=2 && groupSize<=3 ) size = Size.MEDIUM;
        else if( groupSize>3) size = Size.MEGA;
    }
    public void setLabel(String label) {
        switch (label) {
            case "egg": this.label = Dish.Label.EGG; break;
            case "veg": this.label = Dish.Label.VEG; break;
            case "non-veg": this.label = Dish.Label.NON_VEG; break;
        }
    }
    public void setCategory(String category) {
        switch (category) {
            case "Regular": this.category = Category.REGULAR; break;
            case "Budget": this.category = Category.BUDGET; break;
            case "Corporate": this.category = Category.CORPORATE; break;
            case "Health": this.category = Category.HEALTH; break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combo)) return false;
        if (o == this) return true;
        Combo combo = (Combo) o;
        if(this.id == combo.id) {
            if(this.comboOptions.size()!=0)
                if (!(comboOptions.equals(((Combo) o).comboOptions))) return false;
            return true;
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31*hash + this.id;
        for(ComboOption comboOption: comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                hash = 31 * hash + comboDish.hashCode();
        return hash;
    }
}
