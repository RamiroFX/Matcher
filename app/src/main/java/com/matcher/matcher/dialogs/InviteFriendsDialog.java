package com.matcher.matcher.dialogs;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.matcher.matcher.R;
import com.matcher.matcher.adapters.InviteFriendsAdapter;
import com.matcher.matcher.adapters.MyFriendsRecyclerViewAdapter;
import com.matcher.matcher.adapters.MyRecyclerItemTouchListener;
import com.matcher.matcher.controllers.GetFriendsTask;
import com.matcher.matcher.entities.Friend;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class InviteFriendsDialog extends DialogFragment implements GetFriendsTask.OnFriendInviteListener{

    private static final String TAG ="InviteFriendsDialog";
    private ActionMode actionMode;
    private boolean isMultiSelect = false;
    //i created List of int type to store id of data, you can create custom class type data according to your need.
    private List<Friend> selectedIds = new ArrayList<>();
    private InviteFriendsAdapter adapter;
    private OnFriendSelectedListener listener;
    private OnFriendSelectedListener mListener;
    private ArrayList<Friend> friendList;
    private RecyclerView rvFriendList;

    public void setListener(OnFriendSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void OnFriendInviteInteraction(List<Friend> friends) {
        Log.d(TAG,"OnFriendInviteInteraction");
        friendList.addAll(friends);
        adapter.setList(friendList);
    }

    public interface OnFriendSelectedListener {
        void onFriendSelectedInteraction();
    }

    public static InviteFriendsDialog newInstance(OnFriendSelectedListener mListener) {
        InviteFriendsDialog fragment = new InviteFriendsDialog();
        fragment.setListener(mListener);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.friendList = new ArrayList<>();
        this.adapter = new InviteFriendsAdapter(getContext(),friendList);
        /*GetFriendsTask task = new GetFriendsTask(this);
        task.execute();*/
        getFriendList();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_invite_friends, container, false);
        this.getDialog().setTitle("Friend list");

        final Toolbar toolbar = (Toolbar) v.findViewById(R.id.my_awesome_toolbar);
        toolbar.inflateMenu(R.menu.menu_invite_friends);
        this.rvFriendList = v.findViewById(R.id.rvInviteFriends);
        this.rvFriendList.setAdapter(adapter);
        this.rvFriendList.setLayoutManager(new LinearLayoutManager(getContext()));
        this.rvFriendList.addOnItemTouchListener(new MyRecyclerItemTouchListener(this, rvFriendList, new MyRecyclerItemTouchListener.OnItemClickListener(){
            @Override
            public void onItemClick(View view, int position) {
                if (isMultiSelect){
                    //if multiple selection is enabled then select item on single click else perform normal click on item.
                    multiSelect(position);
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                if (!isMultiSelect){
                    selectedIds = new ArrayList<>();
                    isMultiSelect = true;

                    if (actionMode == null){
                        //actionMode = InviteFriendsDialog.this.getActivity().startActionMode(new ActionMode.Callback() {
                        actionMode = toolbar.startActionMode(new ActionMode.Callback() {
                            @Override
                            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                                MenuInflater inflater = actionMode.getMenuInflater();
                                inflater.inflate(R.menu.menu_invite_friends, menu);
                                return true;
                            }

                            @Override
                            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                                return false;
                            }

                            @Override
                            public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
                                switch (menuItem.getItemId()){
                                    case R.id.select_invited_friends:
                                        //just to show selected items.
                                        StringBuilder stringBuilder = new StringBuilder();
                                        for (Friend data : friendList) {
                                            if (selectedIds.contains(data.getUid()))
                                                stringBuilder.append("\n").append(data.getUsername());
                                        }
                                        Toast.makeText(getContext(), "Selected items are :" + stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                                        return true;
                                }
                                return false;
                            }

                            @Override
                            public void onDestroyActionMode(ActionMode mode) {
                                actionMode = null;
                                isMultiSelect = false;
                                selectedIds = new ArrayList<>();
                                adapter.setSelectedIds(new ArrayList<Friend>());
                            }
                        });
                    }
                }

                multiSelect(position);
            }
        }));
        return v;
    }
    private void multiSelect(int position) {
        Friend data = adapter.getItem(position);
        if (data != null){
            if (actionMode != null) {
                if (selectedIds.contains(data))
                    selectedIds.remove(data);
                else
                    selectedIds.add(data);

                if (selectedIds.size() > 0)
                    actionMode.setTitle(String.valueOf(selectedIds.size())); //show selected item count on action mode.
                else{
                    actionMode.setTitle(""); //remove item count from action mode.
                    actionMode.finish(); //hide action mode.
                }
                adapter.setSelectedIds(selectedIds);

            }
        }
    }

    private void getFriendList(){
        try {
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray array, GraphResponse response) {
                            //jsonFriends = array.toString();
                        }
                    });
            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    if (response != null && response.getJSONObject() != null) {
                        try {
                            JSONArray friendslist = response.getJSONObject().getJSONArray("data");
                            for (int l = 0; l < friendslist.length(); l++) {
                                String name = friendslist.getJSONObject(l).getString("name");
                                String facebookId = friendslist.getJSONObject(l).getString("id");
                                Friend friendItemData = new Friend(facebookId, name);
                                friendList.add(friendItemData);
                                Log.d(TAG,"callback: "+friendItemData);
                            }
                            //get next batch of results of exists
                            GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                            if (nextRequest != null) {
                                nextRequest.setCallback(this);
                                nextRequest.executeAndWait();
                            }
                            rvFriendList.getAdapter().notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            request.executeAsync();
        } catch (FacebookException e) {
            Log.d(TAG,"FacebookException: "+e);
        }
    }
/*
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.menu_invite_friends, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.select_invited_friends:
                //just to show selected items.
                StringBuilder stringBuilder = new StringBuilder();
                for (Friend data : friendList) {
                    if (selectedIds.contains(data.getUid()))
                        stringBuilder.append("\n").append(data.getUsername());
                }
                Toast.makeText(getContext(), "Selected items are :" + stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                return true;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        actionMode = null;
        isMultiSelect = false;
        selectedIds = new ArrayList<>();
        adapter.setSelectedIds(new ArrayList<String>());
    }*/
}