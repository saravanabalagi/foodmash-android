package in.foodmash.app.custom;

import java.util.List;

/**
 * Created by Zeke on Sep 30 2015.
 */
public class Cache {
    private static List<Combo> combos;

    public static List<Combo> getCombos() { return combos; }
    public static Combo getCombo(int comboId) { for (Combo c : combos) if (c.getId()==comboId) return c; return null; }

    public static void setCombos(List<Combo> combosParams) { if(isNew(combosParams)) combos = combosParams; }
    private static boolean isNew(List<Combo> combosParams) { return combos == null || combosParams.hashCode() != combos.hashCode(); }
}
