package in.foodmash.app.commons;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import in.foodmash.app.R;

/**
 * Created by Zeke on Feb 24, 2016.
 */
public class Filters extends RecyclerView.Adapter {
    public enum Type { REGULAR, HEADER }
    private ArrayList<Integer> headerPositions = new ArrayList<>();
    private LinkedHashMap<String, Integer> filters = new LinkedHashMap<>();

    public Filters() { }
    public void addFilter(String filter, int iconResource) { filters.put(filter,iconResource); }
    public void addHeader(String header) { filters.put(header,-1); headerPositions.add(filters.size() - 1); }

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
        String filter = (new ArrayList<String>(filters.keySet())).get(position);
        if(((ViewHolder) holder).type==Type.REGULAR) {
            ((ViewHolder) holder).text.setText(filter);
            ((ViewHolder) holder).icon.setImageResource(filters.get(filter));
            if(position==16 || position==1) holder.itemView.setActivated(true);
        } else ((ViewHolder) holder).header.setText(filter);
    }
}
