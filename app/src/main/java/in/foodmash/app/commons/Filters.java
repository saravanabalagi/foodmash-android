package in.foodmash.app.commons;

import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import in.foodmash.app.R;

/**
 * Created by Zeke on Feb 24, 2016.
 */
public class Filters extends RecyclerView.Adapter {
    public enum Type { REGULAR, HEADER }
    private ArrayList<Integer> headerPositions = new ArrayList<>();
    private Set<Integer> selectedPositions = new HashSet<>();
    private ArrayList<Pair<String, Integer>> filters = new ArrayList<>();

    public Filters() { }
    public void changeLocation(String location) {
        Integer icon = filters.get(1).second;
        filters.remove(1);
        filters.add(1, new Pair<>(location, icon));
    }
    public void addFilter(String filter, int iconResource) { filters.add(new Pair<>(filter, iconResource)); }
    public void addHeader(String header) { filters.add(new Pair<>(header, -1)); headerPositions.add(filters.size() - 1); }
    public void setSelected(Integer position) { selectedPositions.add(position); }
    public void removeSelected(Integer position) { selectedPositions.remove(position); }
    public void clearAllSelected() { selectedPositions.clear(); }

    class ViewHolder extends RecyclerView.ViewHolder {
        Type type;
        TextView header;
        TextView text;
        ImageView icon;
        public ViewHolder(View itemView, Type type) {
            super(itemView);
            this.type = type;
            if(type == Type.REGULAR) {
                text = (TextView) itemView.findViewById(R.id.text);
                icon = (ImageView) itemView.findViewById(R.id.icon);
            } else header = (TextView) itemView.findViewById(R.id.header);
        }
    }

    @Override public int getItemViewType(int position) { return headerPositions.contains(position)? Type.HEADER.ordinal(): Type.REGULAR.ordinal(); }
    @Override public int getItemCount() { return filters.size(); }
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == Type.HEADER.ordinal()) return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_drawer_row_header,parent,false),Type.HEADER);
        else return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_drawer_row,parent,false),Type.REGULAR);
    }
    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Pair<String,Integer> filterIconPair = filters.get(position);
        if(((ViewHolder) holder).type==Type.REGULAR) {
            ((ViewHolder) holder).text.setText(filterIconPair.first);
            ((ViewHolder) holder).icon.setImageResource(filterIconPair.second);
            if(selectedPositions.contains(position)) holder.itemView.setActivated(true);
            else holder.itemView.setActivated(false);
        } else ((ViewHolder) holder).header.setText(filterIconPair.first);
    }
}
