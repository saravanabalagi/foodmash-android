package in.foodmash.app.custom;

import java.util.ArrayList;

/**
 * Created by sarav on Sep 18 2015.
 */
public class ImmutableCombo {

    final private int id;
    final private int groupSize;
    final private int noOfPurchases;
    final private float price;
    final private String label;
    final private String name;
    final private String description;
    final private boolean special;

    final private ArrayList<ComboDish> comboDishes;
    final private ArrayList<ComboOption> comboOptions;

    public ImmutableCombo(Combo c) {
        this.id = c.getId();
        this.groupSize = c.getGroupSize();
        this.noOfPurchases = c.getNoOfPurchases();
        this.price = c.getFloatPrice();
        this.label = c.getLabel();
        this.name = c.getName();
        this.description = c.getDescription();
        this.special = c.isSpecial();
        this.comboDishes = c.getComboDishes();
        this.comboOptions = c.getComboOptions();
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
    public ArrayList<ComboDish> getComboDishes() { return new ArrayList<>(comboDishes); }
    public ArrayList<ComboOption> getComboOptions() { return new ArrayList<>(comboOptions); }

    public ArrayList<Integer> getSelectedComboDishes() {
        ArrayList<Integer> selectedList = new ArrayList<>();
        for (ComboOption comboOption : this.getComboOptions())
            selectedList.add(comboOption.getSelected());
        return selectedList;
    }

    public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.getComboOptions())
            dishNames += comboOption.getSelectedDishName() + ", ";
        for (ComboDish comboDish : this.getComboDishes())
            dishNames += comboDish.getDish().getName() + ", ";
        return dishNames.substring(0,dishNames.length()-2);
    }

}
