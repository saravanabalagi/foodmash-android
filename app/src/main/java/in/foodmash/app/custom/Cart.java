package in.foodmash.app.custom;

import java.util.HashMap;

/**
 * Created by sarav on Sep 10 2015.
 */
public class Cart {

    float total;
    HashMap<Combo,Integer> orders = new HashMap<>();

    private static Cart mInstance;
    public synchronized static Cart getInstance() {
        if(mInstance==null) { mInstance = new Cart(); }
        return mInstance;
    }

    public int getCount() {
        int count = 0;
        for (Integer value : orders.values())
            count+=value;
        return count;
    }

    public void addToCart(Combo combo) {
        if(orders.containsKey(combo)) { orders.put(combo,orders.get(combo)+1); }
        else orders.put(combo, 1);
    }
}