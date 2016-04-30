package in.foodmash.app.models;

import android.util.Pair;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Zeke on Sep 10 2015.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Combo {

    public enum Category { REGULAR, BUDGET, CORPORATE, HEALTH }
    public enum Size { MICRO, MEDIUM, MEGA }

    private int id;
    private int groupSize;
    private int noOfPurchases;
    private Size size;
    private Dish.Label label;
    private Category category;
    private String name;
    private String description;
    private String note;
    private boolean available = false;
    private boolean customizable = false;
    private float price;
    private String picture;
    private ArrayList<ComboOption> comboOptions = new ArrayList<>();

    public Combo() {}
    public Combo(Combo c) {
        this.id = c.id;
        this.groupSize = c.groupSize;
        this.noOfPurchases = c.noOfPurchases;
        this.size = c.size;
        this.label = c.label;
        this.category = c.category;
        this.name = c.name;
        this.description = c.description;
        this.available = c.available;
        this.customizable = c.customizable;
        this.price = c.price;
        this.picture = c.picture;
        this.comboOptions = new ArrayList<>();
        for (ComboOption comboOption: c.comboOptions)
            this.comboOptions.add(new ComboOption(comboOption));
    }

    public int getId() { return id; }
    public int getGroupSize() { return groupSize; }
    public int getNoOfPurchases() { return noOfPurchases; }
    public Size getSize() { return size; }
    public Dish.Label getLabel() { return label; }
    public Category getCategory() { return category; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getNote() { return note; }
    public boolean isAvailable() { return available; }
    public boolean isCustomizable() { return customizable; }
    public float getPrice() { return price; }
    public String getPicture() { return picture; }
    public ArrayList<ComboOption> getComboOptions() { return comboOptions; }
    public String getType() { return this.getClass().getSimpleName(); }

    public void setId(int id) { this.id = id; }
    public void setNoOfPurchases(int noOfPurchases) { this.noOfPurchases = noOfPurchases; }
    public void setSize(Size size) { this.size = size; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setNote(String note) { this.note = note; }
    public void setAvailable(boolean available) { this.available = available; }
    public void setCustomizable(boolean customizable) { this.customizable = customizable; }
    public void setPrice(float price) { this.price = price; }
    public void setPicture(String picture) { this.picture = picture; }
    public void setComboOptions(ArrayList<ComboOption> comboOptions) { this.comboOptions = comboOptions; }

    public float calculatePrice() {
        float price = 0;
        for (ComboOption comboOption : this.comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                price+=(comboDish.getDish().getPrice()*comboDish.getQuantity());
        return price;
    }
    public String getDishNames() {
        String dishNames = "";
        for (ComboOption comboOption : this.comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                dishNames += ((comboDish.getQuantity()==1)?"":(comboDish.getQuantity() + " x ")) + comboDish.getDish().getName() + " ("+comboDish.getDish().getRestaurant().getName()+") " +  "\n";
        if(!isMandatoryComboOptionsSelected()) dishNames += "Select at least one "+getUnselectedMandatoryComboOptionNames()+"\n";
        if(isCustomizable()) {
            if(Cart.getInstance().getCount()>1 || (Cart.getInstance().getCount()==1 && !Cart.getInstance().hasCombo(this.getId()))) {
                if (!isOneFromOptionalComboOptionsSelected())
                    dishNames += "Select at least one dish from any category" + "\n";
            } else if (!isOneFromOptionalComboOptionsSelected())
                dishNames += "Select dishes from at least two categories"+"\n";
            else if (!isDishesFromAtLeastTwoDifferentOptionalComboOptionsSelected())
                dishNames += "Select one more dish from a different category"+"\n";
        } else if(!isOneFromOptionalComboOptionsSelected())
            dishNames += "Select at least one "+getOptionalComboOptionsNames()+"\n";
        return (dishNames.length()==0)?"":dishNames.substring(0,dishNames.length()-1);
    }

    public ArrayList<String> getImages() {
        ArrayList<String> imageArrayList = new ArrayList<>();
        for(ComboOption comboOption: comboOptions)
            for(ComboOptionDish comboDish: comboOption.getComboOptionDishes())
                imageArrayList.add(comboDish.getDish().getPicture());
        return imageArrayList;
    }

    public void setGroupSize(int groupSize) {
        this.groupSize = groupSize;
        if( groupSize==1) size = Size.MICRO;
        else if( groupSize>=2 && groupSize<=3 ) size = Size.MEDIUM;
        else if( groupSize>3) size = Size.MEGA;
    }
    public void setLabel(String label) {
        switch (label) {
            case "egg": this.label = Dish.Label.EGG; break;
            case "veg": this.label = Dish.Label.VEG; break;
            case "non-veg": this.label = Dish.Label.NON_VEG; break;
        }
    }
    public void setCategory(String category) {
        switch (category) {
            case "Regular": this.category = Category.REGULAR; break;
            case "Budget": this.category = Category.BUDGET; break;
            case "Corporate": this.category = Category.CORPORATE; break;
            case "Health": this.category = Category.HEALTH; break;
        }
    }

    public void removeAllSelected() { for (ComboOption comboOption: this.comboOptions) comboOption.removeAllSelectedComboOptionDishes(); }
    public String getUnselectedMandatoryComboOptionNames() {
        String mandatoryComboOptions = "";
        if(comboOptions!=null)
            for(ComboOption comboOption: this.comboOptions)
                if(comboOption.getMinCount()>0 && comboOption.getComprisedDishesQuantity()<comboOption.getMinCount()) mandatoryComboOptions += comboOption.getName() + ", ";
        return (mandatoryComboOptions.length()==0)?"":mandatoryComboOptions.substring(0, mandatoryComboOptions.length()-2);
    }
    public String getMandatoryComboOptionNames() {
        String mandatoryComboOptions = "";
        if(comboOptions!=null)
            for(ComboOption comboOption: this.comboOptions)
                if(comboOption.getMinCount()>0) mandatoryComboOptions += comboOption.getName() + ", ";
        return (mandatoryComboOptions.length()==0)?"":mandatoryComboOptions.substring(0, mandatoryComboOptions.length()-2);
    }
    public String getOptionalComboOptionsNames() {
        String optionalComboOptions = "";
        if(comboOptions!=null)
            for (ComboOption comboOption: comboOptions)
                if(comboOption.getMinCount()==0) optionalComboOptions += comboOption.getName() + " / ";
        return (optionalComboOptions.length()==0)?"":optionalComboOptions.substring(0, optionalComboOptions.length()-3);
    }
    public boolean isMandatoryComboOptionsSelected() {
        if(comboOptions!=null)
            for (ComboOption comboOption: comboOptions)
                if(comboOption.getMinCount()>0)
                    if(comboOption.getComprisedDishesQuantity()<comboOption.getMinCount())
                        return false;
        return true;
    }
    public boolean isOneFromOptionalComboOptionsSelected() {
        ArrayList<ComboOption> comboOptionsMinCountZero = new ArrayList<>();
        for (ComboOption comboOption : comboOptions)
            if(comboOption.getMinCount() == 0) comboOptionsMinCountZero.add(comboOption);
        if(comboOptionsMinCountZero.size()==0) return true;
        int comboOptionsMinCountZeroQuantity = 0;
        for(ComboOption comboOption: comboOptionsMinCountZero)
            comboOptionsMinCountZeroQuantity += comboOption.getComprisedDishesQuantity();
        return comboOptionsMinCountZeroQuantity > 0;
    }
    public boolean isDishesFromAtLeastTwoDifferentOptionalComboOptionsSelected() {
        ArrayList<ComboOption> comboOptionsMinCountZero = new ArrayList<>();
        for (ComboOption comboOption : comboOptions)
            if(comboOption.getMinCount() == 0) comboOptionsMinCountZero.add(comboOption);
        if(comboOptionsMinCountZero.size()==0) return true;
        int comboOptionsMinCountZeroQuantity = 0;
        int noOfDifferentComboOptionsSelected = 0;
        for(ComboOption comboOption: comboOptionsMinCountZero) {
            comboOptionsMinCountZeroQuantity += comboOption.getComprisedDishesQuantity();
            if(comboOption.getComprisedDishesQuantity()>0) noOfDifferentComboOptionsSelected++;
        }
        return (comboOptionsMinCountZeroQuantity >= 2) && noOfDifferentComboOptionsSelected >= 2;
    }
    public boolean isValid() {
        if(isCustomizable()) {
            if (Cart.getInstance().getCount() > 0 && !Cart.getInstance().hasCombo(this.getId()))
                return isMandatoryComboOptionsSelected() && isOneFromOptionalComboOptionsSelected();
            else return isMandatoryComboOptionsSelected() && isDishesFromAtLeastTwoDifferentOptionalComboOptionsSelected();
        } else return isMandatoryComboOptionsSelected() && isOneFromOptionalComboOptionsSelected();
    }

    public void makeValid() {
        if(!isMandatoryComboOptionsSelected())
            for (ComboOption comboOption : comboOptions)
                if (comboOption.getComprisedDishesQuantity() < comboOption.getMinCount())
                    comboOption.resetSelectedComboOptionDishes();
        if(!isOneFromOptionalComboOptionsSelected()) {
            ArrayList<ComboOption> comboOptionsMinCountZero = new ArrayList<>();
            for (ComboOption comboOption : comboOptions)
                if (comboOption.getMinCount() == 0) comboOptionsMinCountZero.add(comboOption);
            if (comboOptionsMinCountZero.size() == 0) return;
            int comboOptionsMinCountZeroQuantity = 0;
            for (ComboOption comboOption : comboOptionsMinCountZero)
                comboOptionsMinCountZeroQuantity += comboOption.getComprisedDishesQuantity();
            if (comboOptionsMinCountZeroQuantity == 0) {
                ArrayList<Pair<ComboOption, ComboOptionDish>> comboOptionDishesFromComboOptionsMinCountZero = new ArrayList<>();
                for (ComboOption comboOption : comboOptionsMinCountZero)
                    comboOptionDishesFromComboOptionsMinCountZero.add(new Pair<>(comboOption, comboOption.getComboOptionDishes().get(0)));
                Collections.sort(comboOptionDishesFromComboOptionsMinCountZero, new Comparator<Pair<ComboOption, ComboOptionDish>>() {
                    @Override
                    public int compare(Pair<ComboOption, ComboOptionDish> lhs, Pair<ComboOption, ComboOptionDish> rhs) {
                        return Float.compare(lhs.second.getDish().getPrice(), rhs.second.getDish().getPrice());
                    }
                });
                comboOptionDishesFromComboOptionsMinCountZero.get(0).first.addToSelected(comboOptionDishesFromComboOptionsMinCountZero.get(0).second);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Combo)) return false;
        if (o == this) return true;
        Combo combo = (Combo) o;
        if(this.id != combo.id) return false;
        if(this.comboOptions!=null && combo.comboOptions!=null)
            for (ComboOption comboOption : this.comboOptions)
                if (!(combo.comboOptions.contains(comboOption))) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = 31*hash + this.id;
        for(ComboOption comboOption: comboOptions)
            for(ComboOptionDish comboDish: comboOption.getSelectedComboOptionDishes())
                hash = 31 * hash + comboDish.hashCode();
        return hash;
    }
}
