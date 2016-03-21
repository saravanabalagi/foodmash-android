package in.foodmash.app.custom;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Zeke on Sep 10 2015.
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
    public int getCount(Combo combo) { return (orders.containsKey(combo))?orders.get(combo):0; }

    public float getTotal() {
        float total = 0;
        for (HashMap.Entry<Combo,Integer> order: orders.entrySet() )
            total += order.getKey().calculatePrice() * order.getValue();
        return total;
    }


    public void addToCart(Combo combo) {
        timestamps.put(System.currentTimeMillis(),combo);
        if(orders.containsKey(combo)) orders.put(combo, orders.get(combo) + 1);
        else orders.put(combo, 1);
        printOrdersContents();
        printTimestampsContents();
    }

    public boolean hasCombo(Combo combo) {
        for(Combo entry: this.orders.keySet())
            if(entry.getId()==combo.getId()) return true;
        return false;
    }

    public int decrementFromCart(int comboId) {
        for(Long timestamp: timestamps.descendingKeySet()) {
            Combo comboEntry = timestamps.get(timestamp);
            if (comboEntry.getId() == comboId) {
                if (orders.get(comboEntry) - 1 == 0) orders.remove(comboEntry);
                else orders.put(comboEntry, orders.get(comboEntry) - 1);
                timestamps.remove(timestamp);
                printOrdersContents();
                printTimestampsContents();
                break;
            }
        }
        return getCount(comboId);
    }

    public int decrementFromCart(Combo combo) {
        for(Long timestamp: timestamps.descendingKeySet()) {
            Combo comboEntry = timestamps.get(timestamp);
            if (combo.hashCode() == comboEntry.hashCode()) {
                if (orders.get(comboEntry) - 1 == 0) orders.remove(comboEntry);
                else orders.put(comboEntry, orders.get(comboEntry) - 1);
                timestamps.remove(timestamp);
                printOrdersContents();
                printTimestampsContents();
                break;
            }
        }
        return getCount(combo);
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

    public boolean areCombosAvailableIn(String comboJsonArrayString) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        try {
            List<Combo> combos = Arrays.asList(objectMapper.readValue(comboJsonArrayString, Combo[].class));
            boolean areCombosAvailable = true;
            ArrayList<Combo> combosToBeDeleted = new ArrayList<>();
            for (Combo combo : orders.keySet()) {
                boolean isComboAvailable = false;
                for (Combo comboEntry : combos)
                    if (combo.getId() == comboEntry.getId() && comboEntry.isAvailable()) {
                        isComboAvailable = true;
                        break;
                    }
                if(!isComboAvailable) {
                    combosToBeDeleted.add(combo);
                    areCombosAvailable = false;
                }
            }
            for (Combo combo: combosToBeDeleted)
                removeOrder(combo);
            return areCombosAvailable;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public JSONArray getCartOrders() {
        JSONArray cartJsonArray = new JSONArray();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.CAMEL_CASE_TO_LOWER_CASE_WITH_UNDERSCORES);
        for (HashMap.Entry<Combo, Integer> entry : orders.entrySet()) {
            try { cartJsonArray.put(new JSONObject(objectMapper.writeValueAsString(entry.getKey())).put("quantity",entry.getValue())); }
            catch (Exception e) { e.printStackTrace(); }
        }
        return cartJsonArray;
    }

    public void printTimestampsContents() {
        Log.i("Cart","Treemap contents: ");
        for (TreeMap.Entry<Long, Combo> entry : timestamps.entrySet())
            Log.i("Cart","Timestamp: "+entry.getKey()+" Combo Hash: "+entry.getValue().hashCode() +" Combo ID: "+entry.getValue().getId());
    }

    public void printOrdersContents() {
        Log.i("Cart","Orders hashmap contents: ");
        for (HashMap.Entry<Combo, Integer> entry: orders.entrySet())
            Log.i("Cart","Combo Hash: "+entry.getKey().hashCode()+ " Quantity: "+entry.getValue()+"\n"+entry.getKey().getDishNames());
    }

}