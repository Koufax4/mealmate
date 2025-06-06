package com.example.mealmate.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.data.model.Ingredient;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple adapter for displaying ingredients in recipe detail view (read-only).
 */
public class IngredientDisplayAdapter extends RecyclerView.Adapter<IngredientDisplayAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredients;

    public IngredientDisplayAdapter() {
        this.ingredients = new ArrayList<>();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient_display, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Updates the ingredients list.
     */
    public void updateIngredients(List<Ingredient> newIngredients) {
        this.ingredients.clear();
        if (newIngredients != null) {
            this.ingredients.addAll(newIngredients);
        }
        notifyDataSetChanged();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewIngredient;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewIngredient = itemView.findViewById(R.id.textViewIngredient);
        }

        public void bind(Ingredient ingredient) {
            StringBuilder ingredientText = new StringBuilder();

            // Add quantity and unit if available
            if (ingredient.getQuantity() > 0) {
                ingredientText.append(formatQuantity(ingredient.getQuantity()));
                if (ingredient.getUnit() != null && !ingredient.getUnit().trim().isEmpty()) {
                    ingredientText.append(" ").append(ingredient.getUnit());
                }
                ingredientText.append(" ");
            }

            // Add ingredient name
            ingredientText.append(ingredient.getName());

            textViewIngredient.setText(ingredientText.toString());
        }

        private String formatQuantity(double quantity) {
            // Format quantity to remove unnecessary decimal places
            if (quantity == (int) quantity) {
                return String.valueOf((int) quantity);
            } else {
                return String.valueOf(quantity);
            }
        }
    }
}