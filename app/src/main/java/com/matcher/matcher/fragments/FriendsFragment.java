package com.matcher.matcher.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

    private static final String TAG = "FriendsFragment";
    private static final String FRIEND_LIST = "friendList";

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private String jsonFriends;
    private ArrayList<FriendItemData> friends;
    private OnFriendListInteractionListener mListener;
    private MyFriendsRecyclerViewAdapter friendsRVA;
    private RecyclerView friendsRV;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public FriendsFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static FriendsFragment newInstance(int columnCount) {
        FriendsFragment fragment = new FriendsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (getArguments() != null) {
            Log.i(TAG, "1:onCreate");
            Log.i(TAG, "2:" + getArguments().toString());
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
            jsonFriends = getArguments().getString(FRIEND_LIST);
        }*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends_list, container, false);
        Context context = view.getContext();
        // Set the adapter
        friends = new ArrayList<FriendItemData>();
        obtenerAmigos();
        friendsRVA = new MyFriendsRecyclerViewAdapter(friends, mListener, getContext());
        friendsRVA.notifyDataSetChanged();
        friendsRV = (RecyclerView) view;
        friendsRV.setLayoutManager(new LinearLayoutManager(context));
        //friendsRV.setLayoutManager(new GridLayoutManager(context, mColumnCount));
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFriendListInteractionListener {
        void onFriendListInteraction(FriendItemData mItem, View view, int position);
    }

    private void obtenerAmigos() {
        try {
            GraphRequest request = GraphRequest.newMyFriendsRequest(
                    AccessToken.getCurrentAccessToken(),
                    new GraphRequest.GraphJSONArrayCallback() {
                        @Override
                        public void onCompleted(JSONArray array, GraphResponse response) {
                            jsonFriends = array.toString();
                        }
                    });
            request.setCallback(new GraphRequest.Callback() {
                @Override
                public void onCompleted(GraphResponse response) {
                    try {
                        JSONArray friendslist = response.getJSONObject().getJSONArray("data");
                        for (int l = 0; l < friendslist.length(); l++) {
                            FriendItemData friendItemData = new FriendItemData(friendslist.getJSONObject(l));
                            friends.add(friendItemData);
                        }
                        //get next batch of results of exists
                        GraphRequest nextRequest = response.getRequestForPagedResults(GraphResponse.PagingDirection.NEXT);
                        if(nextRequest != null){
                            nextRequest.setCallback(this);
                            nextRequest.executeAndWait();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    friendsRVA.notifyDataSetChanged();
                }
            });
            request.executeAsync();
        } catch (FacebookException e) {
            e.printStackTrace();
        }
    }
}
