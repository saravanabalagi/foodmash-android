package in.foodmash.app.custom;

import java.util.ArrayList;

/**
 * Created by sarav on Sep 10 2015.
 */
public class Combo {
    int id;
    int groupSize;
    float price;
    String label;
    String name;
    String description;
    boolean special;
    ArrayList<ComboDish> comboDishes;
    ArrayList<ComboOption> comboOptions;
}

class ComboDish {
    int id;
    int priority;
    Dish dish;
}

class Dish {
    int id;
    String name;
    Restaurant restaurant;
}

class ComboOption {
    int id;
    int priority;
    String name;
    String description;
    int selected = 0;
    ArrayList<ComboDish> comboDishes;
    public int getSelected() { return selected; }
    public void setSelected(int selected) { this.selected = selected; }
}

class Restaurant {
    int id;
    String name;
}
