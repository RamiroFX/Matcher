package com.matcher.matcher.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.facebook.AccessToken;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.matcher.matcher.R;
import com.matcher.matcher.adapters.MyFriendsRecyclerViewAdapter;
import com.matcher.matcher.entities.FriendItemData;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFriendListInteractionListener}
 * interface.
 */
public class FriendsFragment extends Fragment {


    public interface OnFriendListInteractionListener {
        void onFriendListInteraction(FriendItemData mItem, View view, int position);
    }

    private static final String TAG = "FriendsFragment";
    private ArrayList<FriendItemData> friends;
    private OnFriendListInteractionListener mListener;
    private MyFriendsRecyclerViewAdapter friendsRVA;
    private RecyclerView friendsRV;

    public FriendsFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.friends = new ArrayList<>();
        obtenerAmigos();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        Context context = view.getContext();
        friendsRVA = new MyFriendsRecyclerViewAdapter(friends, mListener);
        friendsRV = (RecyclerView) view;
        friendsRV.setLayoutManager(new LinearLayoutManager(context));
        friendsRV.setAdapter(friendsRVA);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFriendListInteractionListener) {
            mListener = (OnFriendListInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFriendListInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void obtenerAmigos() {
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
                                FriendItemData friendItemData = new FriendItemData(friendslist.getJSONObject(l));
                                friends.add(friendItemData);
                            }
                            //get next batch of results of exists
                            GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                            if (nextRequest != null) {
                                nextRequest.setCallback(this);
                                nextRequest.executeAndWait();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        friendsRVA.notifyDataSetChanged();
                    }else {
                        Log.e(TAG,"obtenerAmigos: response.getJSONObject() == null");
                    }
                }
            });
            request.executeAsync();
        } catch (FacebookException e) {
            e.printStackTrace();
        }
    }
}
