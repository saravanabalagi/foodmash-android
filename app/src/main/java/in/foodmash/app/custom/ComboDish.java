package in.foodmash.app.custom;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class ComboDish {

    private int id;
    private int priority;
    private int minCount=1;
    private int quantity=1;
    private Dish dish;

    public ComboDish() {}
    public ComboDish(ComboDish c) {
        this.id = c.id;
        this.priority = c.priority;
        this.minCount = c.minCount;
        this.quantity = c.quantity;
        this.dish = c.dish;
    }

    public int getId() { return id; }
    public Dish getDish() { return dish; }
    @JsonIgnore public int getPriority() { return priority; }
    @JsonIgnore public int getMinCount() { return minCount; }
    public int getQuantity() { return quantity; }

    @JsonProperty public void setId(int id) { this.id = id; }
    @JsonProperty public void setPriority(int priority) { this.priority = priority; }
    @JsonProperty public void setDish(Dish dish) { this.dish = dish; }
    @JsonProperty public void setMinCount(int minCount) { this.minCount = minCount; this.quantity = minCount; }
    public boolean incrementQuantity() { if(quantity +1<10) { quantity++; return true;} else return false; }
    public boolean decrementQuantity() { if(quantity -1<minCount) return false; else { quantity--; return true; } }
    public void resetQuantity() { quantity=minCount; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboDish)) return false;
        if (o == this) return true;
        ComboDish comboDish = (ComboDish) o;
        return this.id == comboDish.id && this.quantity == comboDish.quantity;
    }

    @Override
    public int hashCode() {
        int hash =7;
        hash = 3*hash + this.getId();
        hash = 3*hash + this.getQuantity();
        return hash;
    }
}
