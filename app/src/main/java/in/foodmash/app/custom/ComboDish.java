package in.foodmash.app.custom;

/**
 * Created by sarav on Sep 11 2015.
 */
public class ComboDish {

    private int id;
    private int priority;
    private int minCount=1;
    private int count=1;
    private Dish dish;

    public ComboDish() {}
    public ComboDish(ComboDish c) {
        this.id = c.id;
        this.priority = c.priority;
        this.minCount = c.minCount;
        this.count = c.count;
        this.dish = c.dish;
    }

    public int getId() { return id; }
    public int getPriority() { return priority; }
    public Dish getDish() { return dish; }
    public int getMinCount() { return minCount; }
    public int getCount() { return count; }

    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDish(Dish dish) { this.dish = dish; }
    public void setMinCount(int count) { this.minCount = count; this.count =count; }
    public boolean incrementCount() { if(count+1<10) { count++; return true;} else return false; }
    public boolean decrementCount() { if(count-1<minCount) return false; else { count--; return true; } }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ComboDish)) return false;
        if (o == this) return true;
        ComboDish comboDish = (ComboDish) o;
        return this.id == comboDish.id && this.count == comboDish.count;
    }

    @Override
    public int hashCode() {
        int hash =13;
        hash = 7*hash + this.getId();
        hash = 7*hash + this.getCount();
        return hash;
    }
}
