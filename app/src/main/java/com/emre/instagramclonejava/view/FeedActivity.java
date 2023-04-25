package com.emre.instagramclonejava.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.emre.instagramclonejava.R;
import com.emre.instagramclonejava.adapter.PostAdapter;
import com.emre.instagramclonejava.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    private PostAdapter postAdapter;
    private com.emre.instagramclonejava.databinding.ActivityFeedBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = com.emre.instagramclonejava.databinding.ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);


        postArrayList = new ArrayList<>();

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);
    }

    private void getData() {
        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Toast.makeText(FeedActivity.this, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }

                if (value != null) {

                    for (DocumentSnapshot snapshot : value.getDocuments()) {
                        Map<String, Object> data = snapshot.getData();
                        String useremail = (String) data.get("userEmail");
                        String comment = (String) data.get("comment");
                        String downloadurl = (String) data.get("downloadUrl");

                        Post post = new Post(useremail, comment, downloadurl);
                        postArrayList.add(post);
                    }
                    postAdapter.notifyDataSetChanged();

                }

            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_post) {
            // Upload Activity
            Intent intent = new Intent(FeedActivity.this, UploadActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.signout) {
            // Sign Out

            auth.signOut(); // hesaptan çıkış

            Intent intent = new Intent(FeedActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater(); // xml'deki kodu bağlamak için kullanılır
        menuInflater.inflate(R.menu.option_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}