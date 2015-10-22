package in.foodmash.app.custom;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by sarav on Sep 10 2015.
 */
public class Cart {

    private HashMap<Combo,Integer> orders = new HashMap<>();
    private TreeMap<Long, Combo> timestamps = new TreeMap<>();
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

    public int getCount(int comboId) {
        int count =0;
        for (HashMap.Entry<Combo,Integer> order: orders.entrySet() )
            if(order.getKey().getId()==comboId)
                count += order.getValue();
        return count;
    }

    public String getTotal() {
        float total = 0;
        for (HashMap.Entry<Combo,Integer> order: orders.entrySet() )
            total += order.getKey().calculatePrice() * order.getValue();
        return String.format("%.2f",total);
    }

    public void addToCart(Combo combo) {
        timestamps.put(System.currentTimeMillis(),combo);
        System.out.println("Combo Hash: "+combo.hashCode());
        if(orders.containsKey(combo)) { System.out.println("Existing quantity: "+orders.get(combo)); orders.put(combo, orders.get(combo) + 1); System.out.println("Increasing quantity by 1");}
        else { orders.put(combo, 1); System.out.println("Adding a new order"); }
        printOrdersContents();
        printTimestampsContents();
      }

    public void decrementFromCart(Combo combo) {
        for(Long timestamp: timestamps.descendingKeySet()) {
            Combo comboEntry = timestamps.get(timestamp);
            if (combo.getId() == comboEntry.getId()) {
                if (orders.get(comboEntry) - 1 == 0) orders.remove(comboEntry);
                else orders.put(comboEntry, orders.get(comboEntry) - 1);
                timestamps.remove(timestamp);
                printOrdersContents();
                printTimestampsContents();
                return;
            }
        }
    }

    public HashMap<Combo,Integer> getOrders() { return orders; }
    public void changeQuantity(Combo combo, int quantity) {
        if(orders.get(combo)-quantity>0) {
            int i = 0;
            Iterator<Long> iterator = timestamps.descendingKeySet().iterator();
            while(iterator.hasNext()) {
                Long timestamp = iterator.next();
                if (timestamps.get(timestamp).hashCode() == combo.hashCode()) {
                    if (i == orders.get(combo) - quantity) break; else i++;
                    iterator.remove();
                }
            }
        } else for (int i = 0; i<quantity-orders.get(combo);i++)
                timestamps.put(System.currentTimeMillis()+i,combo);
        orders.put(combo,quantity);
        printOrdersContents(); printTimestampsContents();
    }
    public void removeAllOrders() { orders.clear(); timestamps.clear(); }
    public void removeOrder(Combo combo) {
        orders.remove(combo);
        Iterator<Long> iterator = timestamps.descendingKeySet().iterator();
        while(iterator.hasNext()) {
            Long timestamp = iterator.next();
            if (timestamps.get(timestamp).hashCode() == combo.hashCode())
                iterator.remove();
        }
        printOrdersContents();
        printTimestampsContents();
    }

    public JSONArray getCartOrders() {
        JSONArray cartJsonArray = new JSONArray();
        ObjectMapper mapper = new ObjectMapper();
        for (HashMap.Entry<Combo, Integer> entry : orders.entrySet()) {
            try { cartJsonArray.put(new JSONObject(mapper.writeValueAsString(entry.getKey())).put("quantity",entry.getValue())); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return cartJsonArray;
    }

    public void printTimestampsContents() {
        System.out.println("Treemap contents: ");
        for (TreeMap.Entry<Long, Combo> entry : timestamps.entrySet())
            System.out.println("Timestamp: "+entry.getKey()+" Combo Hash: "+entry.getValue().hashCode()+ " Selected: "+entry.getValue().getSelectedComboDishes()+" Combo ID: "+entry.getValue().getId());
    }

    public void printOrdersContents() {
        System.out.println("Orders hashmap contents: ");
        for (HashMap.Entry<Combo, Integer> entry: orders.entrySet())
            System.out.println("Combo Hash: "+entry.getKey().hashCode()+ " Selected: "+entry.getKey().getSelectedComboDishes()+" Quantity: "+entry.getValue()+" Contents: "+entry.getKey().getDishNames());
    }

}