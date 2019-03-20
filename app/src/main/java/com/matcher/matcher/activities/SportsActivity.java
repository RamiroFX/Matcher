package com.matcher.matcher.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.Constants;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.SharedPreferenceHelper;
import com.matcher.matcher.adapters.SelectableSportViewHolder;
import com.matcher.matcher.adapters.SportsAdapter;
import com.matcher.matcher.entities.SelectableSport;
import com.matcher.matcher.entities.Sports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SportsActivity extends AppCompatActivity implements SelectableSportViewHolder.OnSportSelectedListener {

    private static final String TAG = "SportsActivity";

    private RecyclerView sportsRV;
    private MenuItem itemToHide;
    private int requestType;
    private int sportsCount;
    private String myUID;

    //Selectable adapter
    private SportsAdapter selectableFriendAdapter;
    private List<SelectableSport> selectableItems;
    private List<SelectableSport> selectedItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sports);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
        this.myUID = sharedPreferenceHelper.getUser().getUid();
        sportsRV = this.findViewById(R.id.rv_sports_list);
        selectedItems = new ArrayList<>();
        selectableItems = new ArrayList<>();
        sportsCount = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            requestType = extras.getInt(Constants.SPORTS_ACTIVITY_TYPE, 0);
            switch (requestType) {
                case Constants.SPORTS_ACTIVITY_TYPE_PROFILE: {
                    selectableFriendAdapter = new SportsAdapter(this, selectableItems, true);
                    break;
                }
                case Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE: {
                    selectableFriendAdapter = new SportsAdapter(this, selectableItems, false);
                    break;
                }
                default: {
                    finish();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sportsRV.setAdapter(selectableFriendAdapter);
        sportsRV.setLayoutManager(new LinearLayoutManager(this));
        getSports();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invite_friends, menu);
        if (requestType == Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE) {
            itemToHide = menu.findItem(R.id.select_invited_friends);
            itemToHide.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.select_invited_friends:
                seleccionarDeportes();
                break;
            case android.R.id.home:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public boolean onNavigateUp() {
        this.finish();
        return super.onNavigateUp();
    }

    @Override
    public boolean onSupportNavigateUp() {
        this.finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        this.finish();
        super.onBackPressed();
    }

    @Override
    public void onSportSelected(SelectableSport item, final View view) {
        switch (requestType) {
            case Constants.SPORTS_ACTIVITY_TYPE_PROFILE: {
                selectedItems = selectableFriendAdapter.getSelectedItems();
                Snackbar.make(this.sportsRV, "Selected sport is " + item.getName() +
                        ", Totally  selected item count is " + selectedItems.size(), Snackbar.LENGTH_LONG).show();
                if (selectedItems.size() > 2) {
                    seleccionarDeportes();
                }
                break;
            }
            case Constants.SPORTS_ACTIVITY_TYPE_CHALLENGE: {
                /*if (view instanceof ImageView) {
                    ViewFriendProfile(item);
                } else if (view instanceof TextView) {*/
                challengeFriend(item);
                //}
                break;
            }
        }
    }

    private void getSports() {
        Log.d(TAG, "getSports");
        int[] imgs = {R.drawable.icons8_futbol_50,
                R.drawable.icons8_voleibol_50,
                R.drawable.icons8_baloncesto_50,
                R.drawable.icons8_tenis_50,
                R.drawable.icons8_ping_pong_50,
                R.drawable.icons8_running_black_50,
                R.drawable.icons8_chess_50,
                R.drawable.icons8_cycling_50,
                R.drawable.icons8_game_50,
                R.drawable.icons8_handball_50,
                R.drawable.icons8_golf_50,
                R.drawable.icons8_squash_50,
                R.drawable.icons8_swimming_50,
                R.drawable.icons8_rowing_50,
                R.drawable.icons8_taekwondo_40};
        String sportsArray[] = getResources().getStringArray(R.array.sports_array);
        for (int i = 0; i < sportsArray.length; i++) {
            Log.d(TAG, "imgs[" + i + "]" + imgs[i]);
            selectableFriendAdapter.onSportAdded(new SelectableSport(new Sports(i, sportsArray[i], imgs[i]), false));
        }
    }

    private void seleccionarDeportes() {
        if (!selectedItems.isEmpty()) {
            final ArrayList sports = new ArrayList();
            for (Sports sport : selectedItems) {
                sports.add(sport.getUid());
            }
            DatabaseReference mUserDatabaseRef = FirebaseDatabase.getInstance().getReference().child(DBContract.UserTable.TABLE_NAME).child(myUID);
            mUserDatabaseRef
                    .child(DBContract.UserTable.COL_NAME_SPORTS)
                    .setValue(sports, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError != null) {
                                Toast.makeText(getApplicationContext(), R.string.error_ocurrred_try_again_message, Toast.LENGTH_SHORT).show();
                            } else {
                                int sportCount = 1;
                                String favoriteSports = "";
                                for (int i = 0; i < sports.size(); i++) {
                                    int sportIndex = (int) sports.get(i);
                                    List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                                    for (int x = 0; x < myArrayList.size(); x++) {
                                        if (sportIndex == x) {
                                            if (sportCount < sports.size()) {
                                                favoriteSports = favoriteSports + myArrayList.get(x) + " - ";
                                                sportCount++;
                                            } else {
                                                favoriteSports = favoriteSports + myArrayList.get(x);
                                                sportCount++;
                                            }
                                            break;
                                        }
                                    }

                                }
                                SharedPreferenceHelper sharedPreferenceHelper = SharedPreferenceHelper.getInstance(getApplicationContext());
                                sharedPreferenceHelper.setParameter(DBContract.UserTable.COL_NAME_SPORTS, favoriteSports);
                            }
                            finish();
                        }
                    });
            //
        } else {
            String message = getString(R.string.sport_activity_on_empty_list);
            Snackbar.make(this.sportsRV, message, Snackbar.LENGTH_LONG).show();
        }
    }

    private void challengeFriend(SelectableSport friend) {
        int uid = friend.getUid();
        String fullName = friend.getName();
        Intent i = new Intent();
        i.putExtra(Constants.SPORT_NAME, fullName);
        i.putExtra(Constants.SPORT_ID, uid);
        setResult(RESULT_OK, i);
        finish();
    }
}
