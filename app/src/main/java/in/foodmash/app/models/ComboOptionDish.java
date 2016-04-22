package in.foodmash.app.models;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class ComboOptionDish {

    private int id;
    private int priority;
    private int minCount = 0;
    private int quantity = 0;
    private Dish dish;

    public ComboOptionDish() {}
    public ComboOptionDish(ComboOptionDish c) {
        this.id = c.id;
        this.priority = c.priority;
        this.minCount = c.minCount;
        this.quantity = c.quantity;
        this.dish = c.dish;
    }

    public int getId() { return id; }
    public Dish getDish() { return dish; }
    public int getPriority() { return priority; }
    public int getMinCount() { return minCount; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDish(Dish dish) { this.dish = dish; }
    public void setMinCount(int minCount) { this.minCount = minCount; this.quantity = minCount; }
    public boolean incrementQuantity() { if(quantity +1<10) { quantity++; return true;} else return false; }
    public boolean decrementQuantity() { if(quantity -1< minCount) return false; else { quantity--; return true; } }
    public void resetQuantity() { quantity = minCount; }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboOptionDish)) return false;
        if (o == this) return true;
        ComboOptionDish comboDish = (ComboOptionDish) o;
        return this.getId() == comboDish.getId() && this.getQuantity() == comboDish.getQuantity();
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31*hash + this.getId();
        hash = 31*hash + this.getQuantity();
        return hash;
    }
}
