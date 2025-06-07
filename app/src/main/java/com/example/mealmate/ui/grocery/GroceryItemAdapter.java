package com.example.mealmate.ui.grocery;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.data.model.GroceryItem;
import com.example.mealmate.databinding.ItemGroceryBinding;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for displaying grocery items.
 */
public class GroceryItemAdapter extends RecyclerView.Adapter<GroceryItemAdapter.GroceryItemViewHolder> {

    private List<GroceryItem> groceryItems = new ArrayList<>();
    private OnItemClickListener listener;

    /**
     * Interface for handling item interactions.
     */
    public interface OnItemClickListener {
        void onPurchasedToggle(GroceryItem item, boolean purchased);

        void onDeleteClick(GroceryItem item);
    }

    /**
     * Sets the click listener for item interactions.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the list of grocery items.
     */
    public void updateItems(List<GroceryItem> newItems) {
        this.groceryItems = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public GroceryItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemGroceryBinding binding = ItemGroceryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new GroceryItemViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryItemViewHolder holder, int position) {
        holder.bind(groceryItems.get(position));
    }

    @Override
    public int getItemCount() {
        return groceryItems.size();
    }

    /**
     * ViewHolder for grocery items.
     */
    class GroceryItemViewHolder extends RecyclerView.ViewHolder {
        private final ItemGroceryBinding binding;

        public GroceryItemViewHolder(@NonNull ItemGroceryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(GroceryItem item) {
            // Set item name
            binding.textItemName.setText(item.getName());

            // Format and set quantity
            String quantityText = formatQuantity(item.getQuantity(), item.getUnit());
            binding.textItemQuantity.setText(quantityText);

            // Set category if available
            if (item.getCategory() != null && !item.getCategory().trim().isEmpty()) {
                binding.textItemCategory.setText(item.getCategory());
                binding.textItemCategory.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.textItemCategory.setVisibility(android.view.View.GONE);
            }

            // Set purchased status without triggering listener
            binding.checkboxPurchased.setOnCheckedChangeListener(null);
            binding.checkboxPurchased.setChecked(item.isPurchased());

            // Apply visual styling based on purchased status
            updateItemAppearance(item.isPurchased());

            // Set up click listeners
            binding.checkboxPurchased.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    item.setPurchased(isChecked);
                    updateItemAppearance(isChecked);
                    listener.onPurchasedToggle(item, isChecked);
                }
            });

            binding.buttonDeleteItem.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(item);
                }
            });
        }

        /**
         * Updates the visual appearance based on purchased status.
         */
        private void updateItemAppearance(boolean purchased) {
            if (purchased) {
                // Apply strikethrough and faded appearance
                binding.textItemName.setPaintFlags(
                        binding.textItemName.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.textItemQuantity.setPaintFlags(
                        binding.textItemQuantity.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);

                binding.textItemName.setAlpha(0.6f);
                binding.textItemQuantity.setAlpha(0.6f);
                binding.textItemCategory.setAlpha(0.6f);
            } else {
                // Remove strikethrough and restore normal appearance
                binding.textItemName.setPaintFlags(
                        binding.textItemName.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                binding.textItemQuantity.setPaintFlags(
                        binding.textItemQuantity.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));

                binding.textItemName.setAlpha(1.0f);
                binding.textItemQuantity.setAlpha(1.0f);
                binding.textItemCategory.setAlpha(1.0f);
            }
        }

        /**
         * Formats quantity and unit for display.
         */
        private String formatQuantity(double quantity, String unit) {
            DecimalFormat df = new DecimalFormat("#.##");
            String formattedQuantity = df.format(quantity);

            if (unit != null && !unit.trim().isEmpty()) {
                return formattedQuantity + " " + unit;
            } else {
                return formattedQuantity;
            }
        }
    }
}