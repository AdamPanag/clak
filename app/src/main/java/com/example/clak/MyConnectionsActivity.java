package com.example.clak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clak.classes.Organization;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class MyConnectionsActivity extends AppCompatActivity {

    private final static String TAG = "MY CONNECTIONS";

    private Toolbar toolbar;
    private RecyclerView dataList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private DocumentReference current_customer_ref;
    private String customer_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_connections);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        dataList = findViewById(R.id.organizations_recycler);

        fetchOrganizations();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.customer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout_toolbar:
                logoutUser();

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }

    private void fetchOrganizations() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        customer_id = user.getUid();
        FirebaseFirestore.getInstance()
                .collection("connections")
                .whereEqualTo("customer_id", user.getUid())
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    List<String> organizations_uids = new ArrayList<String>();
                    List<DocumentSnapshot> docs = qs.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        organizations_uids.add(doc.getString("organization_id"));
                    }
                    populateList(organizations_uids);
                } else {
                    Toast.makeText(MyConnectionsActivity.this, R.string.error_fetch, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void populateList(final List<String> organizations_uids) {
        if (organizations_uids.size() == 0) {
            // Handle case where the customer has no connections
            Toast.makeText(this, "No connected organizations", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = FirebaseFirestore.getInstance()
                .collection("organizations")
                .whereIn(FieldPath.documentId(), organizations_uids);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();

        FirestorePagingOptions<Organization> options = new FirestorePagingOptions.Builder<Organization>()
                .setQuery(query, config, new SnapshotParser<Organization>() {
                    @NonNull
                    @Override
                    public Organization parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        return new Organization(snapshot.getId(), snapshot.getString("email"),
                                snapshot.getString("orgName"));
                    }
                })
                .build();

        class OrganizationViewHolder extends RecyclerView.ViewHolder {

            TextView orgName;

            public OrganizationViewHolder(@NonNull View itemView) {
                super(itemView);
                orgName = itemView.findViewById(R.id.orgName);

            }
        }

        final FirestorePagingAdapter<Organization, OrganizationViewHolder> adapter =
                new FirestorePagingAdapter<Organization, OrganizationViewHolder>(options) {
                    @NonNull
                    @Override
                    public OrganizationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_connections_grid_layout, parent, false);
                        OrganizationViewHolder holder = new OrganizationViewHolder(view);
                        return holder;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final OrganizationViewHolder holder,
                                                    final int position,
                                                    @NonNull final Organization model) {
                        holder.orgName.setText(model.getOrgName());

                    }
                };

        RecyclerView recyclerView = findViewById(R.id.organizations_recycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2,GridLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void logoutUser() {
        startActivity(new Intent(MyConnectionsActivity.this, LoginActivity.class));
        FirebaseAuth.getInstance().signOut();
        makeText(MyConnectionsActivity.this, R.string.see_you_soon, Toast.LENGTH_SHORT).show();
    }

}