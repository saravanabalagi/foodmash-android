package in.foodmash.app.custom;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sarav on Sep 11 2015.
 */
public class ComboOption {

    private int id;
    private int priority;
    private String name;
    private String description;
    private int selected = 0;
    private ArrayList<ComboDish> comboOptionDishes = new ArrayList<>();

    public int getId() { return id; }
    public int getPriority() { return priority; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getSelected() { return selected; }
    public ArrayList<ComboDish> getComboOptionDishes() { return comboOptionDishes; }
    public boolean isFromSameRestaurant() {
        Set<Integer> restaurantIdSet = new HashSet<>();
        for (ComboDish comboDish:comboOptionDishes)
            restaurantIdSet.add(comboDish.getDish().getRestaurant().getId());
        return restaurantIdSet.size()==1;
    }
    public String getSelectedDishName() {
        for (ComboDish comboDish:comboOptionDishes)
            if(selected==comboDish.getId())
                return comboDish.getDish().getName();
        return null;
    }

    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setSelected(int selected) { this.selected = selected; }
    public void setComboOptionDishes(ArrayList<ComboDish> comboOptionDishes) { this.comboOptionDishes = comboOptionDishes; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboOption)) return false;
        if (o == this) return true;
        ComboOption comboOption = (ComboOption) o;
        if (this.comboOptionDishes.size() != comboOption.comboOptionDishes.size()) return false;
        comboOption.comboOptionDishes.removeAll(this.comboOptionDishes);
        return comboOption.comboOptionDishes.size() == 0 && selected == comboOption.selected;
    }

}
