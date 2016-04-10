package com.dmplayer.adapter;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;


import com.dmplayer.R;
import com.dmplayer.models.DrawerItem;

import java.util.ArrayList;

public class DrawerAdapter extends RecyclerView.Adapter<DrawerAdapter.ViewHolder> {

    private ArrayList<DrawerItem> drawerItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case

        public ViewHolder(View view) {
            super(view);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DrawerAdapter(ArrayList<DrawerItem> drawerItems) {
        this.drawerItems = drawerItems;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DrawerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_drawer, parent, false);
        // set the view's size, margins, paddings and layout parameters

        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final TextView title = (TextView) holder.itemView.findViewById(R.id.textViewDrawerItemTitle);
        ImageView icon = (ImageView) holder.itemView.findViewById(R.id.imageViewDrawerIcon);

        FrameLayout item_divider = (FrameLayout) holder.itemView.findViewById(R.id.item_divider);
        item_divider.setVisibility((position == 3) ? View.VISIBLE : View.GONE);

        title.setText(drawerItems.get(position).getTitle());
        icon.setImageDrawable(drawerItems.get(position).getIcon());

//        icon.setVisibility((position < 3) ? View.VISIBLE : View.GONE);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return drawerItems.size();
    }
}


