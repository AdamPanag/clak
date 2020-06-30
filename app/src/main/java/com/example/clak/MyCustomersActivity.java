package com.example.clak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.paging.PagedList;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class MyCustomersActivity extends AppCompatActivity {

    private final static String TAG = "_MyClients_";

    private DocumentReference current_org_ref;
    private String organization_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_customers);
        fetchCustomers();
    }

    private void fetchCustomers() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        organization_id = user.getUid();
        current_org_ref = FirebaseFirestore.getInstance().collection("organizations").document(organization_id);
        current_org_ref.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        List<String> customer_uids = (List<String>) doc.getData().get("clients");
                        populateList(customer_uids);
                    } else {
                        Toast.makeText(MyCustomersActivity.this, R.string.error_fetch, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(MyCustomersActivity.this, R.string.error_fetch, Toast.LENGTH_LONG).show();
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
                                current_org_ref.update("clients", FieldValue.arrayRemove(customer_uids.get(position)))
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
                        });

                        // Calculate times claked
                        holder.clak_count.setText(R.string.counting_claks);
                        FirebaseFirestore.getInstance().collection("claks")
                                .whereEqualTo("customer_id", customer_uids.get(position))
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
}