package com.matcher.matcher.adapters;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.matcher.matcher.R;
import com.matcher.matcher.entities.Friend;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsAdapter extends RecyclerView.Adapter<InviteFriendsAdapter.MyViewHolder>{
    private Context context;
    private List<Friend> list;
    private List<Friend> selectedIds = new ArrayList<>();

    public InviteFriendsAdapter(Context context,List<Friend> list){
        this.context = context;
        this.list = list;
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.fragment_friends, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.title.setText(list.get(position).getUsername());
        String id = list.get(position).getUid();

        if (selectedIds.contains(id)){
            //if item is selected then,set foreground color of FrameLayout.
            //holder.rootView.setForeground(new ColorDrawable(ContextCompat.getColor(context,R.color.colorControlActivated)));
            holder.rootView.setBackground(new ColorDrawable(ContextCompat.getColor(context,R.color.colorControlActivated)));
        }
        else {
            //else remove selected item color.
            //holder.rootView.setForeground(new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
            holder.rootView.setBackground(new ColorDrawable(ContextCompat.getColor(context,android.R.color.transparent)));
        }
    }

    public void setList(List<Friend> list) {
        this.list = list;
        notifyDataSetChanged();
        Log.d("aasd",list.toString());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
    public Friend getItem(int position){
        return list.get(position);
    }

    public void setSelectedIds(List<Friend> selectedIds) {
        this.selectedIds = selectedIds;
        notifyDataSetChanged();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        TextView title;
        LinearLayout rootView;
        MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tv_name);
            rootView = itemView.findViewById(R.id.frag_friends_container);
        }
    }
}