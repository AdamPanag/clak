package com.example.clak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clak.classes.Customer;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingAdapter;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static android.widget.Toast.makeText;

public class MyClientsActivity extends AppCompatActivity {

    private final static String TAG = "_MyClients_";
    private Toolbar toolbar;

    private String organization_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_clients);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        organization_id = user.getUid();
        fetchCustomers();
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

    private void fetchCustomers() {
        FirebaseFirestore.getInstance()
                .collection("connections")
                .whereEqualTo("organization_id", organization_id)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot qs = task.getResult();
                    List<String> customer_uids = new ArrayList<String>();
                    List<DocumentSnapshot> docs = qs.getDocuments();
                    for (DocumentSnapshot doc : docs) {
                        customer_uids.add(doc.getString("customer_id"));
                    }
                    populateList(customer_uids);
                } else {
                    Toast.makeText(MyClientsActivity.this, R.string.error_fetch, Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void populateList(final List<String> customer_uids) {
        if (customer_uids.size() == 0) {
            // Handle case where organization has no customers
            Toast.makeText(this, "No customers", Toast.LENGTH_SHORT).show();
            return;
        }

        Query query = FirebaseFirestore.getInstance()
                .collection("customers")
                .whereIn(FieldPath.documentId(), customer_uids);

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build();


        FirestorePagingOptions<Customer> options = new FirestorePagingOptions.Builder<Customer>()
                .setQuery(query, config, new SnapshotParser<Customer>() {
                    @NonNull
                    @Override
                    public Customer parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        return new Customer(snapshot.getId(), snapshot.getString("email"),
                                snapshot.getString("name"), snapshot.getString("surname"));
                    }
                })
                .build();



        class CustomerViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView clak_count;
            Button remove_btn;

            public CustomerViewHolder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.client_name);
                clak_count = itemView.findViewById(R.id.client_times);
                remove_btn = itemView.findViewById(R.id.client_remove_btn);

            }
        }

        final FirestorePagingAdapter<Customer, CustomerViewHolder> adapter =
                new FirestorePagingAdapter<Customer, CustomerViewHolder>(options) {
                    @NonNull
                    @Override
                    public CustomerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.myclients_layout, parent, false);
                        CustomerViewHolder holder = new CustomerViewHolder(view);
                        return holder;
                    }

                    @Override
                    protected void onBindViewHolder(@NonNull final CustomerViewHolder holder,
                                                    final int position,
                                                    @NonNull final Customer model) {
                        holder.name.setText(model.getFullName());
                        holder.remove_btn.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                holder.remove_btn.setOnClickListener(null); // Prevent more clicks
                                holder.remove_btn.setText(R.string.deleting);
                                final CollectionReference connectionRef = FirebaseFirestore.getInstance().collection("connections");
                                connectionRef
                                        .whereEqualTo("customer_id", model.getId())
                                        .whereEqualTo("organization_id", organization_id)
                                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if (task.isSuccessful()) {
                                            for (DocumentSnapshot document : task.getResult()) {
                                                connectionRef.document(document.getId()).delete()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    holder.remove_btn.setText(R.string.deleted);
                                                                    holder.remove_btn.setBackgroundColor(getResources().getColor(R.color.green));
                                                                } else {
                                                                    holder.remove_btn.setText(R.string.error);
                                                                    holder.remove_btn.setBackgroundColor(getResources().getColor(R.color.red));
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    }
                                });

                            }
                        });

                        // Calculate times claked
                        holder.clak_count.setText(R.string.counting_claks);
                        FirebaseFirestore.getInstance().collection("claks")
                                .whereEqualTo("customer_id", model.getId())
                                .whereEqualTo("organization_id", organization_id)
                                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    QuerySnapshot qs = task.getResult();
                                    holder.clak_count.setText(String.format(getResources().getString(R.string.visited_times), qs.size()));
                                } else {
                                    holder.clak_count.setText(R.string.error);
                                }
                            }
                        });
                    }

                };

        RecyclerView recyclerView = findViewById(R.id.client_recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter.startListening();
    }

    private void logoutUser() {
        startActivity(new Intent(MyClientsActivity.this, LoginActivity.class));
        FirebaseAuth.getInstance().signOut();
        makeText(MyClientsActivity.this, R.string.see_you_soon, Toast.LENGTH_SHORT).show();
    }
}