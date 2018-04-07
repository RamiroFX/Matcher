package com.matcher.matcher.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import com.matcher.matcher.R;
import com.matcher.matcher.activities.ChatActivity;
import com.matcher.matcher.entities.Chat;
import com.matcher.matcher.entities.Users;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Users user;
    private List<Chat> chats = new ArrayList<>();

    public ChatAdapter(Users user) {
        this.user = user;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.element_chat_item, parent, false);
        return new ChatViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ChatViewHolder) holder).bind(chats.get(position), user.getFullName());
    }

    public void onChatAdded(Chat chat) {
        chats.add(chat);
        notifyItemChanged(chats.size() - 1);
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }


    public class ChatViewHolder extends RecyclerView.ViewHolder {

        TextView tvReceive;
        TextView tvSend;

        private View itemView;

        ChatViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            this.tvReceive = itemView.findViewById(R.id.tvReceive);
            this.tvSend = itemView.findViewById(R.id.tvSend);
        }

        void bind(Chat chat, String uid) {
            if (chat.getUid().equals(uid)) {
                tvSend.setVisibility(View.VISIBLE);
                tvSend.setText(chat.getContent());
            } else {
                tvReceive.setVisibility(View.VISIBLE);
                tvReceive.setText(chat.getContent());
            }
        }
    }
}
