//package com.example.tallycat;
//
//import android.os.Bundle;
//import android.text.TextUtils;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//
//import com.google.android.gms.tasks.Task;
//import com.google.firebase.firestore.DocumentReference;
//import com.google.firebase.firestore.FirebaseFirestore;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class AdminActivity extends AppCompatActivity {
//
//    private EditText ItemId, Name, Description, Category, Status, QrCode;
//    private Button btnAdd, btnUpdate, btnDelete;
//
//    private FirebaseFirestore db;
//
//    @Override protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admin);
//
//        ItemId = findViewById(R.id.etItemId);
//        Name = findViewById(R.id.etName);
//        Description = findViewById(R.id.etDescription);
//        Category = findViewById(R.id.etCategory);
//        Status = findViewById(R.id.Status);
//        QrCode = findViewById(R.id.QrCode); // disabled in XML
//
//        btnAdd = findViewById(R.id.btnAdd);
//        btnUpdate = findViewById(R.id.btnUpdate);
//        btnDelete = findViewById(R.id.btnDelete);
//
//        //initialize firebase
//        db = FirebaseFirestore.getInstance();
//
//        btnAdd.setOnClickListener(v -> {
//            ItemData data = readForm(true);
//            if (data == null) return;
//            upsert(data)
//                    .addOnSuccessListener(unused -> {
//                        toast("Added");
//                        clearForm();
//                    })
//                    .addOnFailureListener(e -> toast("Add failed: " + e.getMessage()));
//        });
//
//        btnUpdate.setOnClickListener(v -> {
//            ItemData data = readForm(false);
//            if (data == null) return;
//            upsert(data)
//                    .addOnSuccessListener(unused -> {
//                        toast("Updated");
//                        clearForm();
//                    })
//                    .addOnFailureListener(e -> toast("Update failed: " + e.getMessage()));
//        });
//
//        btnDelete.setOnClickListener(v -> {
//            String id = ItemId.getText().toString().trim();
//            if (TextUtils.isEmpty(id)) {
//                toast("Enter ItemID to delete");
//                return;
//            }
//            db.collection("inventory").document(id).delete()
//                    .addOnSuccessListener(unused -> {
//                        toast("Deleted");
//                        clearForm();
//                    })
//                    .addOnFailureListener(e -> toast("Delete failed: " + e.getMessage()));
//        });
//
//    }
//    private static class ItemData {
//        String itemId, name, description, category, status, qrCode;
//    }
//
//    private ItemData readForm(boolean requireAll) {
//        String id = ItemId.getText().toString().trim();
//        String name = Name.getText().toString().trim();
//        String desc = Description.getText().toString().trim();
//        String cat = Category.getText().toString().trim();
//        String status = Status.getText().toString().trim();
//
//        if (TextUtils.isEmpty(id)) {
//            toast("ItemID is required");
//            return null;
//        }
//
//        //validate data
//        if (requireAll) {
//            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(desc) ||
//                    TextUtils.isEmpty(cat) || TextUtils.isEmpty(status)) {
//                toast("Fill name, description, category, status");
//                return null;
//            }
//        }
//
//        ItemData d = new ItemData();
//        d.itemId = id;
//        d.name = name;
//        d.description = desc;
//        d.category = cat;
//        d.status = status;
//        d.qrCode = ""; // empty for now
//        return d;
//    }
//
//    /** Insert/overwrite with document id = itemId for easy retrieval later. */
//    private Task<Void> upsert(ItemData d) {
//        Map<String, Object> doc = new HashMap<>();
//        doc.put("itemId", d.itemId);
//        doc.put("name", d.name);
//        doc.put("description", d.description);
//        doc.put("category", d.category);
//        doc.put("status", d.status);
//        doc.put("qrCode", d.qrCode); // left empty
//
//        DocumentReference ref = db.collection("inventory").document(d.itemId);
//        return ref.set(doc); // set() is idempotent: creates or overwrites
//    }
//
//    private void clearForm() {
//        ItemId.setText("");
//        Name.setText("");
//        Description.setText("");
//        Category.setText("");
//        Status.setText("");
//        QrCode.setText("");
//    }
//
//    private void toast(String s) {
//        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
//    }
//}


// AdminActivity.java
package com.example.tallycat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button btnGoAdd = findViewById(R.id.btnAdd);
        Button btnGoEdit = findViewById(R.id.btnEdit);
        Button btnGoDelete = findViewById(R.id.btnDelete);
        Button btnSignOut = findViewById(R.id.btnSignOut);
        Button btnGoSearch = findViewById(R.id.btnSearch);
        Button viewDataButton = findViewById(R.id.btnViewData);

        btnGoAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddInv.class)));

        btnGoEdit.setOnClickListener(v ->
                startActivity(new Intent(this, EditInv.class)));

        btnGoDelete.setOnClickListener(v ->
                startActivity(new Intent(this, DeleteInv.class)));

        btnGoSearch.setOnClickListener(v ->
                startActivity(new Intent(this, SearchActivity.class)));

        btnSignOut.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();

            Intent i = new Intent(this, LoginActivity.class);
            // Clear the back stack so user can't return to Admin with back button
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish(); //just in case
        });

        viewDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, DashboardActivity.class);
            startActivity(intent);
        });
    }
}
