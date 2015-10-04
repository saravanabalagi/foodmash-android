package in.foodmash.app.custom;

/**
 * Created by sarav on Sep 11 2015.
 */
public class ComboDish {

    private int id;
    private int priority;
    private int minCount=1;
    private Dish dish;
    private int count;
    private int price;

    public int getId() { return id; }
    public int getPriority() { return priority; }
    public Dish getDish() { return dish; }
    public int getMinCount() { return minCount; }
    public int getCount() { return count; }
    public int getPrice() { return price; }

    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDish(Dish dish) { this.dish = dish; }
    public void setMinCount(int count) { this.minCount = count; this.count =count; }
    public boolean incrementCount() { if(count+1<10) { count++; return true;} else return false; }
    public boolean decrementCount() { if(count-1<minCount) return false; else { count--; return true; } }
    public void setPrice(int price) { this.price = price; }
}
