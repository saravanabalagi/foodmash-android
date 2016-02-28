package in.foodmash.app.custom;

/**
 * Created by Zeke on Sep 11 2015.
 */
public class ComboDish {

    private int id;
    private int priority;
    private int minCount;
    private int quantity;
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
    public int getPriority() { return priority; }
    public int getMinCount() { return minCount; }
    public int getQuantity() { return quantity; }

    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDish(Dish dish) { this.dish = dish; }
    public void setMinCount(int minCount) { this.minCount = minCount; this.quantity = minCount; }
    public boolean incrementQuantity() { if(quantity +1<10) { quantity++; return true;} else return false; }
    public boolean decrementQuantity() { if(quantity -1< minCount) return false; else { quantity--; return true; } }
    public void resetQuantity() { quantity= minCount; }

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
