package com.example.shoppingapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<ShoppingItem> shoppingItemList;
    DatabaseHelper databaseHelper;
    CustomAdapter adapter;
    private double maxBudget = 0; 
    private double currentTotal = 0; 

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        databaseHelper = new DatabaseHelper(this);
        shoppingItemList = databaseHelper.getAllItems();

       
        ListView listView = findViewById(R.id.listView);
        adapter = new CustomAdapter(this, shoppingItemList);
        listView.setAdapter(adapter);


        listView.setOnItemLongClickListener((parent, view, position, id) -> {
            ShoppingItem item = shoppingItemList.get(position);
            Log.d("MainActivity", "Long pressed item: " + item.getProductName());
            showEditProductDialog(item);
            return true;
        });


        if (maxBudget == 0) {

            showSetBudgetDialog();
        } else {
            
            TextView maxBudgetTextView = findViewById(R.id.maxBudgetTextView);
            maxBudgetTextView.setText("Max Budget: ₱" + maxBudget);
            updateCurrentTotal();
        }
    }


    private void updateCurrentTotal() {
        currentTotal = 0.0;
        for (ShoppingItem item : shoppingItemList) {
            currentTotal += Double.parseDouble(item.getPrice());
        }
        updateRemainingBudget();
    }

    private void updateRemainingBudget() {
        double remainingBudget = maxBudget - currentTotal;
        TextView remainingBudgetTextView = findViewById(R.id.remainingBudgetTextView);
        remainingBudgetTextView.setText("Remaining Budget: ₱" + remainingBudget);
    }


    private void showSetBudgetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_budget, null);
        builder.setView(dialogView);

        final EditText budgetInput = dialogView.findViewById(R.id.budgetInput);

        builder.setTitle("Set Maximum Budget")
                .setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String budgetStr = budgetInput.getText().toString();
                        if (!budgetStr.isEmpty()) {
                            maxBudget = Double.parseDouble(budgetStr);
                            Toast.makeText(MainActivity.this, "Maximum budget set", Toast.LENGTH_SHORT).show();
                            
                            TextView maxBudgetTextView = findViewById(R.id.maxBudgetTextView);
                            maxBudgetTextView.setText("Max Budget: ₱" + maxBudget);
                            updateRemainingBudget();
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter a valid budget", Toast.LENGTH_SHORT).show();
                            
                            showSetBudgetDialog();
                        }
                    }
                })
                .setCancelable(false); 

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }



    public void addItem(View view) {
        showAddProductDialog(view);
    }

    public void showAddProductDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_product, null);
        builder.setView(dialogView);

        final EditText productNameInput = dialogView.findViewById(R.id.productNameInput);
        final EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        final EditText priceInput = dialogView.findViewById(R.id.priceInput);

        builder.setTitle("Add Product")
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int dialogId) {
                        String productName = productNameInput.getText().toString();
                        String quantity = quantityInput.getText().toString();
                        String priceStr = priceInput.getText().toString();
                        if (!productName.isEmpty() && !quantity.isEmpty() && !priceStr.isEmpty()) {
                            double price = Double.parseDouble(priceStr);
                            if (currentTotal + price > maxBudget) {
                                Toast.makeText(MainActivity.this, "Maximum budget reached", Toast.LENGTH_SHORT).show();
                            } else {
                                long itemId = databaseHelper.addItem(productName, quantity, priceStr);
                                shoppingItemList.clear();
                                shoppingItemList.addAll(databaseHelper.getAllItems());
                                updateCurrentTotal();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "Item added", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter product name, quantity, and price", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void showEditProductDialog(final ShoppingItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_product, null);
        builder.setView(dialogView);

        final EditText productNameInput = dialogView.findViewById(R.id.editProductNameInput);
        final EditText quantityInput = dialogView.findViewById(R.id.editQuantityInput);
        final EditText priceInput = dialogView.findViewById(R.id.editPriceInput);

        productNameInput.setText(item.getProductName());
        quantityInput.setText(item.getQuantity());
        priceInput.setText(item.getPrice());

        builder.setTitle("Edit Product")
                .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int dialogId) {
                        String productName = productNameInput.getText().toString();
                        String quantity = quantityInput.getText().toString();
                        String priceStr = priceInput.getText().toString();
                        if (!productName.isEmpty() && !quantity.isEmpty() && !priceStr.isEmpty()) {
                            double newPrice = Double.parseDouble(priceStr);
                            double oldPrice = Double.parseDouble(item.getPrice());
                            if (currentTotal - oldPrice + newPrice > maxBudget) {
                                Toast.makeText(MainActivity.this, "Maximum budget reached", Toast.LENGTH_SHORT).show();
                            } else {
                                item.setProductName(productName);
                                item.setQuantity(quantity);
                                item.setPrice(priceStr);
                                databaseHelper.updateItem(item);
                                shoppingItemList.clear();
                                shoppingItemList.addAll(databaseHelper.getAllItems());
                                updateCurrentTotal();
                                adapter.notifyDataSetChanged();
                                Toast.makeText(MainActivity.this, "Item updated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "Please enter product name, quantity, and price", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void removeItem(int position) {
        ShoppingItem removedItem = shoppingItemList.remove(position);
        databaseHelper.deleteItem(removedItem.getId());
        updateCurrentTotal();
        adapter.notifyDataSetChanged();
        Toast.makeText(this, "Item removed", Toast.LENGTH_SHORT).show();
    }

}