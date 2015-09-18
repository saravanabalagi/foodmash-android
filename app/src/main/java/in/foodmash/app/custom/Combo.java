package in.foodmash.app.custom;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;

/**
 * Created by sarav on Sep 10 2015.
 */
public class Combo {
    private int id;
    private int groupSize;
    private int noOfPurchases;
    private float price;
    private String label;
    private String name;
    private String description;
    private boolean special;

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
        this.comboDishes = c.comboDishes;
        this.comboOptions = new ArrayList<>();
        for (ComboOption entry: c.getComboOptions()) {
            ComboOption comboOption = new ComboOption(entry);
            this.comboOptions.add(comboOption);
        }
    }

    public int getId() { return id; }
    public int getGroupSize() { return groupSize; }
    public int getNoOfPurchases() { return noOfPurchases; }
    public int getIntPrice() { return (int)price; }
    public float getFloatPrice() { return price; }
    public String getStringPrice() { return String.valueOf((int)price); }
    public String getLabel() { return label; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isSpecial() { return special; }
    public ArrayList<ComboDish> getComboDishes() { return comboDishes; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
    public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.getComboOptions())
            dishNames += comboOption.getSelectedDishName() + ", ";
        for (ComboDish comboDish : this.getComboDishes())
            dishNames += comboDish.getDish().getName() + ", ";
        return dishNames.substring(0,dishNames.length()-2);
    }
    public ArrayList<Integer> getSelectedComboDishes() {
        ArrayList<Integer> selectedList = new ArrayList<>();
        for (ComboOption comboOption : this.getComboOptions())
            selectedList.add(comboOption.getSelected());
        return selectedList;
    }

    public void setId(int id) { this.id = id; }
    public void setGroupSize(int groupSize) { this.groupSize = groupSize; }
    public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    public void setPrice(float price) { this.price = price; }
    public void setLabel(String label) { this.label = label; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setSpecial(boolean special) { this.special = special; }
    public void setComboDishes(ArrayList<ComboDish> comboDishes) { this.comboDishes = comboDishes; }
    public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combo)) return false;
        if (o == this) { return true; }
        Combo combo = (Combo) o;
        return this.getId() == combo.getId() && this.getComboOptions().equals(combo.getComboOptions());
    }

    @Override
    public int hashCode() {

        return new HashCodeBuilder(13,29)
                .append(this.getId())
                .append(getSelectedComboDishes().toArray())
                .hashCode();
    }
}
