package com.example.mealmate.ui.recipes;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.data.model.Ingredient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for managing dynamic ingredients list in recipe creation.
 */
public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {

    private List<Ingredient> ingredients;
    private OnIngredientRemovedListener onIngredientRemovedListener;

    public interface OnIngredientRemovedListener {
        void onIngredientRemoved(int position);
    }

    public IngredientAdapter() {
        this.ingredients = new ArrayList<>();
        // Add initial empty ingredient
        this.ingredients.add(new Ingredient("", 0, ""));
    }

    public void setOnIngredientRemovedListener(OnIngredientRemovedListener listener) {
        this.onIngredientRemovedListener = listener;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        Ingredient ingredient = ingredients.get(position);
        holder.bind(ingredient, position);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    /**
     * Adds a new empty ingredient to the list.
     */
    public void addIngredient() {
        ingredients.add(new Ingredient("", 0, ""));
        notifyItemInserted(ingredients.size() - 1);
    }

    /**
     * Removes an ingredient from the list.
     */
    public void removeIngredient(int position) {
        if (position >= 0 && position < ingredients.size() && ingredients.size() > 1) {
            ingredients.remove(position);
            notifyItemRemoved(position);
            if (onIngredientRemovedListener != null) {
                onIngredientRemovedListener.onIngredientRemoved(position);
            }
        }
    }

    /**
     * Gets all ingredients with valid data (non-empty name).
     */
    public List<Ingredient> getValidIngredients() {
        List<Ingredient> validIngredients = new ArrayList<>();
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getName() != null && !ingredient.getName().trim().isEmpty()) {
                validIngredients.add(ingredient);
            }
        }
        return validIngredients;
    }

    /**
     * Gets all ingredients including empty ones.
     */
    public List<Ingredient> getAllIngredients() {
        return new ArrayList<>(ingredients);
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {

        private final TextInputEditText editTextName;
        private final TextInputEditText editTextQuantity;
        private final TextInputEditText editTextUnit;
        private final MaterialButton buttonRemove;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            editTextName = itemView.findViewById(R.id.editTextIngredientName);
            editTextQuantity = itemView.findViewById(R.id.editTextQuantity);
            editTextUnit = itemView.findViewById(R.id.editTextUnit);
            buttonRemove = itemView.findViewById(R.id.buttonRemoveIngredient);
        }

        public void bind(Ingredient ingredient, int position) {
            // Clear previous listeners to avoid conflicts
            editTextName.removeTextChangedListener(nameWatcher);
            editTextQuantity.removeTextChangedListener(quantityWatcher);
            editTextUnit.removeTextChangedListener(unitWatcher);

            // Set current values
            editTextName.setText(ingredient.getName());
            editTextQuantity.setText(ingredient.getQuantity() > 0 ? String.valueOf(ingredient.getQuantity()) : "");
            editTextUnit.setText(ingredient.getUnit());

            // Set up text watchers
            nameWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ingredient.setName(s.toString());
                }
            };

            quantityWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    try {
                        double quantity = s.toString().isEmpty() ? 0 : Double.parseDouble(s.toString());
                        ingredient.setQuantity(quantity);
                    } catch (NumberFormatException e) {
                        ingredient.setQuantity(0);
                    }
                }
            };

            unitWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    ingredient.setUnit(s.toString());
                }
            };

            // Add text watchers
            editTextName.addTextChangedListener(nameWatcher);
            editTextQuantity.addTextChangedListener(quantityWatcher);
            editTextUnit.addTextChangedListener(unitWatcher);

            // Set up remove button
            buttonRemove.setOnClickListener(v -> removeIngredient(getAdapterPosition()));

            // Hide remove button if it's the only ingredient
            buttonRemove.setVisibility(ingredients.size() > 1 ? View.VISIBLE : View.INVISIBLE);
        }

        private TextWatcher nameWatcher;
        private TextWatcher quantityWatcher;
        private TextWatcher unitWatcher;
    }
}