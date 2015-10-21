package in.foodmash.app.custom;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by sarav on Sep 10 2015.
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
    public int getGroupSize() { return groupSize; }
    public int getNoOfPurchases() { return noOfPurchases; }
    public String getLabel() { return label; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSpecial() { return special; }
    public boolean isAvailable() { return available; }
    public ArrayList<ComboDish> getComboDishes() { return comboDishes; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
    public float getPrice() { return price; }
    public float calculatePrice() {
        float price = 0;
        for (ComboDish comboDish : this.getComboDishes()) price+=(comboDish.getDish().getPrice()*comboDish.getCount());
        for (ComboOption comboOption : this.getComboOptions()) price+=(comboOption.getSelectedDish().getDish().getPrice()*comboOption.getCount());
        return price;
    }
    public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.getComboOptions())
            dishNames += comboOption.getSelectedDish().getDish().getName() + (comboOption.isFromSameRestaurant()?"":" ("+comboOption.getSelectedDish().getDish().getRestaurant().getName()+") ") + ", ";
        for (ComboDish comboDish : this.getComboDishes())
            dishNames += comboDish.getDish().getName() + ((comboDish.getCount()==1)?"":(" x " + comboDish.getCount())) + ", ";
        return dishNames.substring(0,dishNames.length()-2);
    }

    public TreeMap<Integer,String> getContents() {
        TreeMap<Integer,String> contents = new TreeMap<>();
        for (ComboOption comboOption : this.getComboOptions()) {
            String comboOptions = "";
            for(ComboDish comboDish: comboOption.getComboOptionDishes())
                comboOptions += comboDish.getDish().getName() + "/ ";
            contents.put(comboOption.getPriority(),comboOptions.substring(0,comboOptions.length()-2));
        }
        for (ComboDish comboDish : this.getComboDishes())
            contents.put(comboDish.getPriority(),comboDish.getDish().getName());
        return contents;
    }

    public ArrayList<ComboDish> getSelectedComboDishes() {
        ArrayList<ComboDish> selectedList = new ArrayList<>();
        for (ComboOption comboOption : this.getComboOptions())
            selectedList.add(comboOption.getSelectedComboDish());
        return selectedList;
    }

    public void setId(int id) { this.id = id; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }
    public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    public void setLabel(String label) { this.label = label; }
    public void setName(String name) { this.name = name; }
    public void setPrice(float price) { this.price = price; }
    public void setDescription(String description) { this.description = description; }
    public void setSpecial(boolean special) { this.special = special; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setComboDishes(ArrayList<ComboDish> comboDishes) { this.comboDishes = comboDishes; }
    public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }

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
            hash = 3*hash + comboOption.getSelectedComboDish().getId();
        for(ComboDish comboDish: comboDishes)
            hash = 3*hash + comboDish.getCount();
        return hash;
    }
}
