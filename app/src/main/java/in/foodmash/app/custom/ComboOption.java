package in.foodmash.app.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class ComboOption {

    private int id;
    private int priority;
    private int minCount=1;
    private String name;
    private String description;
    private ArrayList<ComboDish> selectedComboOptionDishes;
    private ArrayList<ComboDish> comboOptionDishes = new ArrayList<>();

    public ComboOption() {}
    public ComboOption(ComboOption c) {
        this.id = c.id;
        this.priority = c.priority;
        this.name = c.name;
        this.description = c.description;
        this.minCount = c.minCount;
        this.selectedComboOptionDishes = c.selectedComboOptionDishes;
        this.comboOptionDishes = c.comboOptionDishes;
    }

    @JsonIgnore public int getMinCount() { return minCount; }
    public int getQuantity() {
        int quantity=0;
        for(ComboDish comboDish: this.selectedComboOptionDishes)
            quantity+=comboDish.getQuantity();
        if(quantity<minCount) return -1;
        return quantity;
    }
    public Dish.Label getLabel() {
        int veg = 0;
        int non_veg = 0;
        int egg = 0;
        for (ComboDish comboDish: comboOptionDishes) {
            switch (comboDish.getDish().getLabel()) {
                case EGG: egg++;
                case VEG: veg++;
                case NON_VEG: non_veg++;
            }
        }
        if(non_veg==0)
            if(egg==0) return Dish.Label.VEG;
            else return Dish.Label.EGG;
        else return Dish.Label.NON_VEG;
    }
    public int getId() { return id; }
    @JsonIgnore public int getPriority() { return priority; }
    @JsonIgnore public String getName() { return name; }
    @JsonIgnore public String getDescription() { return description; }
    @JsonProperty("ComboOptionDishes") public ArrayList<ComboDish> getSelectedComboOptionDishes() { return selectedComboOptionDishes; }
    @JsonIgnore public ArrayList<ComboDish> getComboOptionDishes() { return new ArrayList<>(comboOptionDishes); }
    @JsonIgnore public boolean isFromSameRestaurant() {
        Set<Integer> restaurantIdSet = new HashSet<>();
        for (ComboDish comboDish:comboOptionDishes)
            restaurantIdSet.add(comboDish.getDish().getRestaurant().getId());
        return restaurantIdSet.size()==1;
    }

    @JsonProperty public void setMinCount(int minCount) { this.minCount = minCount; }
    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setPriority(int priority) { this.priority = priority; }
    @JsonProperty public void setName(String name) { this.name = name; }
    @JsonProperty public void setDescription(String description) { this.description = description; }
    public ComboDish fetch(int id) {
        ComboDish requiredComboDish = null;
        for(ComboDish comboDish: this.comboOptionDishes)
            if(comboDish.getId()==id) requiredComboDish = comboDish;
        return requiredComboDish;
    }
    @JsonIgnore public String getContents(){
        String contents = "";
        for(ComboDish comboDish: this.comboOptionDishes)
            contents += comboDish.getDish().getName() + (this.isFromSameRestaurant()?"":"("+ comboDish.getDish().getRestaurant().getName() +")") + "/ ";
        return contents.substring(0,contents.length()-2);
    }
    public boolean incrementQuantity(ComboDish comboDish) {
        if(this.getQuantity()+1>=10) return false;
        if(!getSelectedComboOptionDishes().contains(comboDish)) return false;
        comboDish.incrementQuantity();
        return true;
    }
    public boolean decrementQuantity(ComboDish comboDish) {
        if(this.getQuantity()-1<minCount) return false;
        if(!getSelectedComboOptionDishes().contains(comboDish)) return false;
        if(comboDish.getQuantity()==1) return removeFromSelected(comboDish);
        comboDish.decrementQuantity();
        return true;
    }
    public boolean addToSelected(ComboDish comboDish) { if(!this.selectedComboOptionDishes.contains(comboDish)) { this.selectedComboOptionDishes.add(comboDish); return true; } else return false; }
    public boolean addToSelectedAfterClear(ComboDish comboDish) { this.selectedComboOptionDishes.clear(); if(!this.selectedComboOptionDishes.contains(comboDish)) { for(int i=0; i<this.minCount-1; i++) comboDish.incrementQuantity(); this.selectedComboOptionDishes.add(comboDish); return true; } else return false; }
    public boolean removeFromSelected(ComboDish comboDish) { if(this.selectedComboOptionDishes.contains(comboDish))  { this.selectedComboOptionDishes.remove(comboDish); return true; } else return false;}
    public void resetSelectedComboOptionDishes() {
        this.selectedComboOptionDishes.clear();
        comboOptionDishes.get(0).resetQuantity();
        for(int i=0; i<this.minCount-1; i++) comboOptionDishes.get(0).incrementQuantity();
        this.selectedComboOptionDishes.add(comboOptionDishes.get(0));
    }
    @JsonProperty public void setComboOptionDishes(ArrayList<ComboDish> comboOptionDishes) {
        this.selectedComboOptionDishes = new ArrayList<>();
        this.comboOptionDishes = comboOptionDishes;
        this.resetSelectedComboOptionDishes();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboOption)) return false;
        if (o == this) return true;
        ComboOption comboOption = (ComboOption) o;
        if (this.comboOptionDishes.size() != comboOption.comboOptionDishes.size()) return false;
        ArrayList<ComboDish> compareComboOptionDishes = comboOption.getComboOptionDishes();
        compareComboOptionDishes.removeAll(this.comboOptionDishes);
        return compareComboOptionDishes.size() == 0 && selectedComboOptionDishes == comboOption.selectedComboOptionDishes;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 3*hash + this.getId();
        hash = 3*hash + this.getQuantity();
        for (ComboDish comboDish : this.selectedComboOptionDishes)
            hash = 3*hash + comboDish.getId();
        return hash;
    }
}
