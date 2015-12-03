package in.foodmash.app.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by Zeke on Sep 10 2015.
 */
public class Combo {
    private int id;
    private int groupSize;
    private int noOfPurchases;
    private String label;
    private String name;
    private String description;
    private boolean special;
    private boolean available;
    private float price;

    private ArrayList<ComboDish> comboDishes = new ArrayList<>();
    private ArrayList<ComboOption> comboOptions = new ArrayList<>();

    public Combo() {}
    public Combo(Combo c) {
        this.id = c.id;
        this.groupSize = c.groupSize;
        this.noOfPurchases = c.noOfPurchases;
        this.price = c.price;
        this.label = c.label;
        this.name = c.name;
        this.description = c.description;
        this.special = c.special;
        this.available = c.available;
        this.comboDishes = new ArrayList<>();
        for (ComboDish entry: c.getComboDishes()) {
            ComboDish comboDish = new ComboDish(entry);
            this.comboDishes.add(comboDish);
        }
        this.comboOptions = new ArrayList<>();
        for (ComboOption entry: c.getComboOptions()) {
            ComboOption comboOption = new ComboOption(entry);
            this.comboOptions.add(comboOption);
        }
    }

    public int getId() { return id; }
    @JsonIgnore public int getGroupSize() { return groupSize; }
    @JsonIgnore public int getNoOfPurchases() { return noOfPurchases; }
    @JsonIgnore public String getLabel() { return label; }
    @JsonIgnore public String getName() { return name; }
    @JsonIgnore public String getDescription() { return description; }
    @JsonIgnore public boolean isSpecial() { return special; }
    @JsonIgnore public boolean isAvailable() { return available; }
    public ArrayList<ComboDish> getComboDishes() { return comboDishes; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
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
                dishNames += comboDish.getDish().getName() + (comboOption.isFromSameRestaurant()?"":" ("+comboDish.getDish().getRestaurant().getName()+") ") + ((comboDish.getQuantity()==1)?"":(" x " + comboDish.getQuantity())) + ", ";
        for (ComboDish comboDish : this.getComboDishes())
            dishNames += comboDish.getDish().getName() + ((comboDish.getQuantity()==1)?"":(" x " + comboDish.getQuantity())) + ", ";
        return dishNames.substring(0,dishNames.length()-2);
    }

    @JsonIgnore public TreeMap<Integer,String> getContents() {
        TreeMap<Integer,String> contents = new TreeMap<>();
        for (ComboOption comboOption : this.getComboOptions()) {
            String comboOptions = "";
            for(ComboDish comboDish: comboOption.getComboOptionDishes())
                comboOptions += comboDish.getDish().getName() + "/ ";
            contents.put(comboOption.getPriority(),comboOptions.substring(0,comboOptions.length()-2));
        }
        for (ComboDish comboDish : this.getComboDishes())
            contents.put(comboDish.getPriority(), comboDish.getDish().getName());
        return contents;
    }

    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setGroupSize(int groupSize) { this.groupSize = groupSize; }
    @JsonProperty public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    @JsonProperty public void setLabel(String label) { this.label = label; }
    @JsonProperty public void setName(String name) { this.name = name; }
    @JsonProperty public void setPrice(float price) { this.price = price; }
    @JsonProperty public void setDescription(String description) { this.description = description; }
    @JsonProperty public void setSpecial(boolean special) { this.special = special; }
    @JsonProperty public void setAvailable(boolean available) { this.available = available; }
    @JsonProperty public void setComboDishes(ArrayList<ComboDish> comboDishes) { this.comboDishes = comboDishes; }
    @JsonProperty public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combo)) return false;
        if (o == this) { return true; }
        Combo combo = (Combo) o;
        if(this.getId() == combo.getId()) {
            if(!(this.comboDishes.equals(((Combo) o).comboDishes))) return false;
            if(this.comboOptions.size()!=0) if (!(comboOptions.equals(((Combo) o).comboOptions))) return false;
            return true;
        } else return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 3*hash + this.getId();
        for(ComboOption comboOption: comboOptions)
            for(ComboDish comboDish: comboOption.getSelectedComboOptionDishes())
                hash = 3*hash + comboDish.getId();
        for(ComboDish comboDish: comboDishes)
            hash = 3*hash + comboDish.getQuantity();
        return hash;
    }
}
