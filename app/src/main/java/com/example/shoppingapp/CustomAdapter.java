package com.example.shoppingapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import java.util.List;

public class CustomAdapter extends BaseAdapter {
    private Context context;
    private List<ShoppingItem> itemList;
    private LayoutInflater inflater;

    public CustomAdapter(Context context, List<ShoppingItem> itemList) {
        this.context = context;
        this.itemList = itemList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public Object getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return itemList.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            holder = new ViewHolder();
            holder.productNameTextView = convertView.findViewById(R.id.productNameTextView);
            holder.quantityTextView = convertView.findViewById(R.id.quantityTextView);
            holder.priceTextView = convertView.findViewById(R.id.priceTextView); 
            holder.checkBox = convertView.findViewById(R.id.checkBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ShoppingItem item = itemList.get(position);
        holder.productNameTextView.setText(item.getProductName());
        holder.quantityTextView.setText(item.getQuantity());
        holder.priceTextView.setText(item.getPrice()); 
        holder.checkBox.setChecked(item.isSelected());

        convertView.setOnLongClickListener(view -> {
            if (context instanceof MainActivity) {
                ((MainActivity) context).showEditProductDialog(item);
            }
            return true;
        });


        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            item.setSelected(isChecked);
            if (isChecked) {

                itemList.remove(item);
                notifyDataSetChanged(); 
                DatabaseHelper databaseHelper = new DatabaseHelper(context);
                databaseHelper.deleteItem(item.getId()); 
            }
        });

        return convertView;
    }

    static class ViewHolder {
        TextView productNameTextView;
        TextView quantityTextView;
        TextView priceTextView; // Add price TextView
        CheckBox checkBox;
    }
}
