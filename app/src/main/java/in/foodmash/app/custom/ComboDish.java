package in.foodmash.app.custom;

/**
 * Created by sarav on Sep 11 2015.
 */
public class ComboDish {

    private int id;
    private int priority;
    private Dish dish;

    public int getId() { return id; }
    public int getPriority() { return priority; }
    public Dish getDish() { return dish; }

    public void setId(int id) { this.id = id; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setDish(Dish dish) { this.dish = dish; }

}
