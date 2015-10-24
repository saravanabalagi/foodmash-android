package in.foodmash.app.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by sarav on Sep 11 2015.
 */
public class ComboOption {

    private int id;
    private int priority;
    private int quantity=1;
    private int minCount=1;
    private String name;
    private String description;
    private ComboDish selectedComboDish;
    private ArrayList<ComboDish> comboOptionDishes = new ArrayList<>();

    public ComboOption() {}
    public ComboOption(ComboOption c) {
        this.id = c.id;
        this.priority = c.priority;
        this.name = c.name;
        this.description = c.description;
        this.quantity = c.quantity;
        this.minCount = c.minCount;
        this.selectedComboDish  = c.selectedComboDish;
        this.comboOptionDishes = c.comboOptionDishes;
    }

    @JsonIgnore public int getMinCount() { return minCount; }
    public int getQuantity() { return quantity; }
    public int getId() { return id; }
    @JsonIgnore public int getPriority() { return priority; }
    @JsonIgnore public String getName() { return name; }
    @JsonIgnore public String getDescription() { return description; }
    @JsonProperty("ComboOptionDish") public ComboDish getSelectedComboDish() { return selectedComboDish; }
    @JsonIgnore public ArrayList<ComboDish> getComboOptionDishes() { return new ArrayList<>(comboOptionDishes); }
    @JsonIgnore public boolean isFromSameRestaurant() {
        Set<Integer> restaurantIdSet = new HashSet<>();
        for (ComboDish comboDish:comboOptionDishes)
            restaurantIdSet.add(comboDish.getDish().getRestaurant().getId());
        return restaurantIdSet.size()==1;
    }

    @JsonProperty public void setMinCount(int minCount) { this.minCount = minCount; this.quantity = minCount; }
    public boolean incrementQuantity() { if(quantity +1<10) { quantity++; return true;} else return false; }
    public boolean decrementQuantity() { if(quantity -1<minCount) return false; else { quantity--; return true; }}
    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setPriority(int priority) { this.priority = priority; }
    @JsonProperty public void setName(String name) { this.name = name; }
    @JsonProperty public void setDescription(String description) { this.description = description; }
    public void setSelected(ComboDish selectedComboDish) { this.selectedComboDish = selectedComboDish; }
    @JsonProperty public void setComboOptionDishes(ArrayList<ComboDish> comboOptionDishes) {
        this.comboOptionDishes = comboOptionDishes;
        this.selectedComboDish = comboOptionDishes.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboOption)) return false;
        if (o == this) return true;
        ComboOption comboOption = (ComboOption) o;
        if (this.comboOptionDishes.size() != comboOption.comboOptionDishes.size()) return false;
        ArrayList<ComboDish> compareComboOptionDishes = comboOption.getComboOptionDishes();
        compareComboOptionDishes.removeAll(this.comboOptionDishes);
        return compareComboOptionDishes.size() == 0 && selectedComboDish == comboOption.selectedComboDish;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 3*hash + this.getId();
        hash = 3*hash + this.getQuantity();
        hash = 3*hash + this.selectedComboDish.getId();
        return hash;
    }
}
