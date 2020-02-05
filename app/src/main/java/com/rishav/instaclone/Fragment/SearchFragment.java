package com.rishav.instaclone.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.hendraanggrian.appcompat.socialview.Hashtag;
import com.hendraanggrian.appcompat.widget.HashtagArrayAdapter;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;
import com.hendraanggrian.appcompat.widget.SocialView;
import com.rishav.instaclone.Adapter.UserAdapter;
import com.rishav.instaclone.Model.HashTag;
import com.rishav.instaclone.Model.User;
import com.rishav.instaclone.R;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    private List<HashTag> mTagsPost;

    private SocialAutoCompleteTextView search_bar;

    private LinearLayout tagLayout;
    private TextView tag;
    private TextView noOfPosts;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);
        tagLayout = view.findViewById(R.id.tag_layout);
        tag = view.findViewById(R.id.hash_tag);
        noOfPosts = view.findViewById(R.id.no_of_posts);
        mTagsPost = new ArrayList<>();

        mUsers = new ArrayList<>();
        userAdapter = new UserAdapter(getContext() , mUsers , true);
        recyclerView.setAdapter(userAdapter);

        readUsers();
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUser(s.toString());

                List<String> mTag = search_bar.getHashtags();
                if (!mTag.isEmpty()){
                    searchTag(mTag.get(0));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        tagLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Gson gson = new Gson();
                String json = gson.toJson(mTagsPost);

                SharedPreferences.Editor editor = getContext().getSharedPreferences("TagPrefs" , Context.MODE_PRIVATE).edit();
                editor.putString("tagList" , json);
                editor.apply();

                ((FragmentActivity)getContext()).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container ,
                        new TagListFragment()).commit();
            }
        });

        return view;
    }

    private void searchTag(final String s) {

        final DatabaseReference mhashTagRef = FirebaseDatabase.getInstance().getReference().child("HashTags");
        mhashTagRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(s)){
                    mTagsPost.clear();
                    recyclerView.setVisibility(View.GONE);
                    tagLayout.setVisibility(View.VISIBLE);
                    mhashTagRef.child(s).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            noOfPosts.setText(dataSnapshot.getChildrenCount() + " posts");
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                                HashTag hs = snapshot.getValue(HashTag.class);
                                tag.setText("#" + hs.getTag());
                                mTagsPost.add(hs);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void searchUser (String s) {

        recyclerView.setVisibility(View.VISIBLE);
        tagLayout.setVisibility(View.GONE);

        Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("username").startAt(s)
                .endAt(s + "\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    mUsers.add(user);
                }

                userAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUsers () {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (TextUtils.isEmpty(search_bar.getText().toString())){
                    mUsers.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        mUsers.add(user);
                    }

                    userAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();

        final ArrayAdapter<Hashtag> hashtagAdapter = new HashtagArrayAdapter<>(getContext());

        FirebaseDatabase.getInstance().getReference().child("HashTags").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    hashtagAdapter.add(new Hashtag(snapshot.getKey() , (int)snapshot.getChildrenCount()));
                    Log.d("HashTag" , snapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        search_bar.setHashtagAdapter(hashtagAdapter);

        search_bar.setOnHashtagClickListener(new SocialView.OnClickListener() {
            @Override
            public void onClick(@NonNull SocialView view, @NonNull CharSequence text) {
                Log.d("mention", text.toString());
                Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
