package com.rishav.instaclone.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rishav.instaclone.Adapter.PostAdapter;
import com.rishav.instaclone.Model.HashTag;
import com.rishav.instaclone.Model.Post;
import com.rishav.instaclone.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TagListFragment extends Fragment {

    private String jsonString;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private List<HashTag> tagList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_detail, container, false);

        SharedPreferences sharedPreferences = getContext().getSharedPreferences("TagPrefs" , Context.MODE_PRIVATE);
        jsonString = sharedPreferences.getString("tagList" , "none");

        Gson gson = new Gson();
        Type type = new TypeToken<List<HashTag>>() {}.getType();
        tagList = gson.fromJson(jsonString , type);

        readMultiplePosts();

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext() , postList);
        recyclerView.setAdapter(postAdapter);

        return view;
    }

    private void readMultiplePosts() {

        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    for (HashTag tag : tagList){
                        if (snapshot.getKey().equals(tag.getPostid())){
                            Post post = snapshot.getValue(Post.class);
                            postList.add(post);
                        }
                    }
                }
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
