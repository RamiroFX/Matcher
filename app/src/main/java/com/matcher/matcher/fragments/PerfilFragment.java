package com.matcher.matcher.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.matcher.matcher.R;
import com.matcher.matcher.Utils.DBContract;
import com.matcher.matcher.Utils.RequestCode;
import com.matcher.matcher.activities.EditAboutUserActivity;
import com.matcher.matcher.activities.EditProfileActivity;
import com.matcher.matcher.activities.MainActivity;
import com.matcher.matcher.dialogs.ConfirmLogoutDialog;
import com.matcher.matcher.dialogs.SelectSportsDialog;
import com.matcher.matcher.entities.Sports;
import com.matcher.matcher.entities.SportsCategories;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link //PerfilFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PerfilFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PerfilFragment extends Fragment implements View.OnClickListener, ConfirmLogoutDialog.confirmLogoutDialogListener, SelectSportsDialog.SelectSportsDialogListener {

    private static final String TAG = "PerfilFragment";
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;
    private TextView tvSportContent, tvAboutContent;
    private TextView tvNanme, tvNickName, tvGender, tvDOB, tvEmail, tvEducation;
    private ImageView ivEditProfile, ivProfilePicture;
    private Button btnSignOut;
    private RelativeLayout layout;
    private ProgressBar progressBar;

    //Firebase vars
    private FirebaseDatabase mDatabase;
    private DatabaseReference mUserDatabaseRef;
    private StorageReference profileRef;
    //User variables
    private String nickName;
    private String aboutUser;
    private String favoriteSports;
    private Uri filePath;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    //private static final String ARG_PARAM1 = "param1";
    //private static final String ARG_PARAM2 = "param2";

    /* TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;*/

    //private OnFragmentInteractionListener mListener;

    public PerfilFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PerfilFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PerfilFragment newInstance(String param1, String param2) {
        Log.i(TAG, "newInstance");
        PerfilFragment fragment = new PerfilFragment();
        /*Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);*/
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = storage.getReference();
        profileRef = storageRef.child("profile");

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseRef = mDatabase.getReference(DBContract.UserTable.TABLE_NAME).child(uid);
        progressBar = new ProgressBar(this.getContext(), null, android.R.attr.progressBarStyleLarge);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_enabled}, // enabled
                new int[]{-android.R.attr.state_enabled}, // disabled
                new int[]{-android.R.attr.state_checked}, // unchecked
                new int[]{android.R.attr.state_pressed}  // pressed
        };

        int[] colors = new int[]{
                Color.BLACK,
                Color.RED,
                Color.GREEN,
                Color.BLUE
        };

        ColorStateList myList = new ColorStateList(states, colors);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            progressBar.setProgressBackgroundTintList(myList);
        }

        //progressBar.setVisibility(View.VISIBLE);  //To show ProgressBar
        progressBar.setVisibility(View.GONE);     // To Hide ProgressBar
        getUserProfileDataFacebook();
        getUserProfileDataFirebase();
        getUserPictureProfile();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View RootView = inflater.inflate(R.layout.fragment_perfil, container, false);
        tvSportContent = RootView.findViewById(R.id.tvSportsContent);
        tvSportContent.setOnClickListener(this);
        tvAboutContent = RootView.findViewById(R.id.tvAboutContent);
        tvAboutContent.setOnClickListener(this);
        tvNanme = RootView.findViewById(R.id.tvName);
        tvGender = RootView.findViewById(R.id.tvGenderContent);
        tvNickName = RootView.findViewById(R.id.tvAlias);
        tvDOB = RootView.findViewById(R.id.tvDOBContent);
        tvEmail = RootView.findViewById(R.id.tvEmailContent);
        tvEducation = RootView.findViewById(R.id.tvEducationContent);
        btnSignOut = RootView.findViewById(R.id.btnSignOut);
        btnSignOut.setOnClickListener(this);
        ivEditProfile = RootView.findViewById(R.id.edit);
        ivEditProfile.setOnClickListener(this);
        ivProfilePicture = RootView.findViewById(R.id.profile);
        ivProfilePicture.setOnClickListener(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(100, 100);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        //layout = RootView.findViewById(R.id.layout);
        //layout.addView(progressBar, params);
        return RootView;
    }

    /*
        // TODO: Rename method, update argument and hook method into UI event
        public void onButtonPressed(Uri uri) {
            if (mListener != null) {
                mListener.onFragmentInteraction(uri);
            }
        }
    */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.i(TAG, "onAttach");
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignOut: {
                signOut();
                break;
            }
            case R.id.edit: {
                editProfile();
                break;
            }
            case R.id.tvSportsContent: {
                editFavoriteSportsProfile();
                break;
            }
            case R.id.tvAboutContent: {
                editAboutUserProfile();
                break;
            }
            case R.id.profile: {
                setProfilePicture();
                break;
            }
        }
    }

    @Override
    public void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog) {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        ((MainActivity) getActivity()).closeApp();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog, final ArrayList sports) {
        final Context context = this.getContext();
        mUserDatabaseRef
                .child(DBContract.UserTable.COL_NAME_SPORTS)
                .setValue(sports, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            Toast.makeText(context, "Some error occured, try again", Toast.LENGTH_SHORT).show();
                        } else {
                            int sportCount = 1;
                            favoriteSports = "";
                            tvSportContent.setText(favoriteSports);
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
                            tvSportContent.setText(favoriteSports);
                        }
                    }
                });
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     *//*
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
*/
    private void getUserProfileDataFacebook() {
        // App code
        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.v(TAG, response.toString());
                        Log.v(TAG, object.toString());
                        // Application code
                        try {
                            String name = object.getString("name");
                            String email = object.getString("email");
                            String gender = object.getString("gender");
                            String birthday = object.getString("birthday");
                            /*JSONArray education = object.getJSONArray("education");
                            if (education != null && education.length() > 0) {
                                for (int i = 0; i < education.length(); i++) {
                                    if (education.getJSONObject(i).getString("type").equals("College")) {
                                        JSONObject school = education.getJSONObject(i).getJSONObject("school");
                                        String school_name = school.getString("name");
                                        tvEducation.setText(school_name);
                                    }
                                }
                            }*/
                            //JSONObject educationChild = education.getJSONObject(0);
                            //JSONObject school = educationChild.getJSONObject("school");
                            //String school_name = school.getString("name");
                            tvNanme.setText(name);
                            tvGender.setText(gender);
                            tvEmail.setText(email);
                            tvDOB.setText(birthday);
                            //tvEducation.setText(school_name);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,education");
        request.setParameters(parameters);
        request.executeAsync();

    }

    private void getFriendProfileDataFacebook(String facebookId) {
        GraphRequest request = GraphRequest.newGraphPathRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + facebookId,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        JSONObject object = response.getJSONObject();
                        try {
                            String name = object.getString("name");
                            String email = object.getString("email");
                            String gender = object.getString("gender");
                            String birthday = object.getString("birthday");
                            JSONArray education = object.getJSONArray("education");
                            if (education != null && education.length() > 0) {
                                for (int i = 0; i < education.length(); i++) {
                                    if (education.getJSONObject(i).getString("type").equals("College")) {
                                        JSONObject school = education.getJSONObject(i).getJSONObject("school");
                                        String school_name = school.getString("name");
                                        tvEducation.setText(school_name);
                                    }
                                }
                            }
                            tvNanme.setText(name);
                            tvGender.setText(gender);
                            tvEmail.setText(email);
                            tvDOB.setText(birthday);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,email,gender,birthday,education");
        request.setParameters(parameters);
        request.executeAsync();
    }


    private void fillFriendData(String jsonFriend) {
        try {
            JSONObject anUser = new JSONObject(jsonFriend);
            JSONObject userSports = anUser.getJSONObject(DBContract.UserTable.COL_NAME_SPORTS);
            String name = anUser.getString(DBContract.UserTable.COL_NAME_FULLNAME);
            String nickName = anUser.getString(DBContract.UserTable.COL_NAME_NICKNAME);
            String about = anUser.getString(DBContract.UserTable.COL_NAME_ABOUT);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }
    }

    private void getUserProfileDataFirebase() {
        mUserDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                nickName = dataSnapshot.child(DBContract.UserTable.COL_NAME_NICKNAME).getValue(String.class);
                tvNickName.setText("(" + nickName + ")");
                aboutUser = dataSnapshot.child(DBContract.UserTable.COL_NAME_ABOUT).getValue(String.class);
                tvAboutContent.setText(aboutUser);
                if (dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getValue() != null) {
                    favoriteSports = "";
                    long childrenSport = dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildrenCount();
                    long childrenCount = 1;
                    //tvSportContent.setText(favoriteSports);
                    for (DataSnapshot childSnapshot : dataSnapshot.child(DBContract.UserTable.COL_NAME_SPORTS).getChildren()) {
                        Long sportIndex = (Long) childSnapshot.getValue();
                        List<String> myArrayList = Arrays.asList(getResources().getStringArray(R.array.sports_array));
                        for (int i = 0; i <= myArrayList.size(); i++) {
                            if (sportIndex == i) {
                                if (childrenCount < childrenSport) {
                                    favoriteSports = favoriteSports + myArrayList.get(i) + " - ";
                                    childrenCount++;
                                } else {
                                    favoriteSports = favoriteSports + myArrayList.get(i);
                                    childrenCount++;
                                }
                                break;
                            }
                        }
                    }
                    tvSportContent.setText(favoriteSports);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                throw databaseError.toException();
            }
        });
    }

    private void getUserPictureProfile(){
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        final Context context = this.getContext();
        profileRef.child(uid).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Uri downloadURI = task.getResult();
                    Glide.with(context)
                            .load(downloadURI)
                            .into(ivProfilePicture);
                }
            }
        });
    }

    private void signOut() {
        DialogFragment dialog = ConfirmLogoutDialog.newInstance(this);
        dialog.show(getChildFragmentManager(), TAG);
    }

    private void test() {
        SportsCategories sc1 = new SportsCategories("acuáticos", "Son aquellos que deben ser realizados en el agua");
        SportsCategories sc2 = new SportsCategories("aéreos", "Son aquellos que deben ser realizados en el aire");
        SportsCategories sc3 = new SportsCategories("agarre", "Son aquellos que involucran el contacto de las manos");
        SportsCategories sc4 = new SportsCategories("animales", "Son aquellos que involucran la actividad con animales");
        SportsCategories sc5 = new SportsCategories("atletismo", "Abarca numerosas disciplinas agrupadas en carreras, saltos, lanzamientos, pruebas combinadas y marcha");
        SportsCategories sc6 = new SportsCategories("motor", "Son el conjunto de disciplinas deportivas practicadas con vehículos motorizados");
        SportsCategories sc7 = new SportsCategories("ciclismo", "Se utiliza una bicicleta para recorrer circuitos al aire libre, en pista cubierta, o que engloba diferentes especialidades");
        SportsCategories sc8 = new SportsCategories("combate", "Deporte competitivo de contacto donde dos contrincantes luchan uno contra el otro usando ciertas reglas de contacto, con el objetivo de simular algunas de las técnicas y tácticas");
        SportsCategories sc9 = new SportsCategories("equipo", "La prueba se realiza entre dos equipos rivales, cada uno compuesto por la misma cantidad de jugadores");
        Sports s1 = new Sports("Volley acuático", "sin descripción", sc1);
        Sports s2 = new Sports("Parapente", "sin descripción", sc2);
        Sports s3 = new Sports("Escalada", "sin descripción", sc3);
    }

    private void editProfile() {
        Log.i(TAG, "editProfile");
        Intent i = new Intent(getActivity(), EditProfileActivity.class);
        if (!TextUtils.isEmpty(nickName)) {
            i.putExtra(DBContract.UserTable.COL_NAME_NICKNAME, nickName);
        }
        startActivityForResult(i, RequestCode.BTN_EDIT_PROFILE.getCode());
    }

    private void editFavoriteSportsProfile() {
        Log.i(TAG, "editFavoriteSportsProfile");
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentManager fm2 = getChildFragmentManager();
        DialogFragment dialog = SelectSportsDialog.newInstance(this);
        dialog.show(fm2, TAG);
    }

    private void editAboutUserProfile() {
        Log.i(TAG, "editAboutUserProfile");
        Intent i = new Intent(getActivity(), EditAboutUserActivity.class);
        if (!TextUtils.isEmpty(aboutUser)) {
            i.putExtra(DBContract.UserTable.COL_NAME_ABOUT, aboutUser);
        }
        startActivityForResult(i, RequestCode.BTN_EDIT_ABOUT_USER.getCode());
    }

    private void setProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), RequestCode.IV_PROFILE_PICTURE.getCode());
    }

    private void profilePictureHandler(Intent data) {
        Log.d(TAG, "profilePictureHandler");
        /*Uri uri = data.getData();
        profileRef.child(uri.getLastPathSegment());
        profileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                mUserDatabaseRef.child("profileImage").setValue(downloadUrl);
            }
        });*/
        filePath = data.getData();
        Bitmap bitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(), filePath);
            ivProfilePicture.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Context context = this.getContext();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        progressBar.setVisibility(View.VISIBLE);
        StorageReference ref = profileRef.child(uid);
        ref.putFile(filePath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(context, "Uploaded", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressBar.setVisibility(View.INVISIBLE);
                        Toast.makeText(context, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                .getTotalByteCount());
                        progressBar.setProgress((int) progress);
                    }
                });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RequestCode.BTN_EDIT_PROFILE.getCode() && resultCode == Activity.RESULT_OK) {
            String nickName = data.getExtras().getString(RequestCode.RESULT.getDescription());
            tvNickName.setText("(" + nickName + ")");
            this.nickName = nickName;
        } else if (requestCode == RequestCode.BTN_EDIT_ABOUT_USER.getCode() && resultCode == Activity.RESULT_OK) {
            String aboutUser = data.getExtras().getString(RequestCode.RESULT.getDescription());
            tvAboutContent.setText("(" + aboutUser + ")");
            this.aboutUser = aboutUser;
        } else if (requestCode == RequestCode.IV_PROFILE_PICTURE.getCode() && resultCode == Activity.RESULT_OK
                && data != null && data.getData() != null)
            profilePictureHandler(data);
    }


}
