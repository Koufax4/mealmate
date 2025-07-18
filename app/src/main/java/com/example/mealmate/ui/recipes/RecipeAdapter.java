// app/src/main/java/com/example/mealmate/ui/recipes/RecipeAdapter.java
package com.example.mealmate.ui.recipes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mealmate.R;
import com.example.mealmate.data.model.Recipe;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying recipes in a RecyclerView.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipes;
    private OnRecipeClickListener onRecipeClickListener;
    private OnRecipeMenuClickListener onRecipeMenuClickListener;

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    public interface OnRecipeMenuClickListener {
        void onRecipeMenuClick(Recipe recipe, View view);
    }

    public RecipeAdapter() {
        this.recipes = new ArrayList<>();
    }

    public void setOnRecipeClickListener(OnRecipeClickListener listener) {
        this.onRecipeClickListener = listener;
    }

    public void setOnRecipeMenuClickListener(OnRecipeMenuClickListener listener) {
        this.onRecipeMenuClickListener = listener;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    /**
     * Updates the recipe list.
     */
    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes.clear();
        if (newRecipes != null) {
            this.recipes.addAll(newRecipes);
        }
        notifyDataSetChanged();
    }

    /**
     * Removes a recipe from the list.
     */
    public void removeRecipe(Recipe recipe) {
        int position = recipes.indexOf(recipe);
        if (position != -1) {
            recipes.remove(position);
            notifyItemRemoved(position);
        }
    }

    class RecipeViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imageViewRecipe;
        private final TextView textViewRecipeName;
        private final TextView textViewIngredientsCount;
        private final TextView textViewPrepTime;
        private final TextView textViewCookTime;
        private final TextView textViewServings;
        private final TextView textViewCategory;
        private final MaterialButton buttonMenuMore;
        private final View prepTimeSeparator, cookTimeSeparator, servingsSeparator;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewRecipe = itemView.findViewById(R.id.imageViewRecipe);
            textViewRecipeName = itemView.findViewById(R.id.textViewRecipeName);
            textViewIngredientsCount = itemView.findViewById(R.id.textViewIngredientsCount);
            textViewPrepTime = itemView.findViewById(R.id.textViewPrepTime);
            textViewCookTime = itemView.findViewById(R.id.textViewCookTime);
            textViewServings = itemView.findViewById(R.id.textViewServings);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            buttonMenuMore = itemView.findViewById(R.id.buttonMenuMore);
            prepTimeSeparator = itemView.findViewById(R.id.prepTimeSeparator);
            cookTimeSeparator = itemView.findViewById(R.id.cookTimeSeparator);
            servingsSeparator = itemView.findViewById(R.id.servingsSeparator);


            // Set up click listeners
            itemView.setOnClickListener(v -> {
                if (onRecipeClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onRecipeClickListener.onRecipeClick(recipes.get(position));
                    }
                }
            });

            buttonMenuMore.setOnClickListener(v -> {
                if (onRecipeMenuClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onRecipeMenuClickListener.onRecipeMenuClick(recipes.get(position), v);
                    }
                }
            });
        }

        public void bind(Recipe recipe) {
            // Set recipe name
            textViewRecipeName.setText(recipe.getName());

            // Set ingredients count (number only)
            int ingredientCount = recipe.getIngredients() != null ? recipe.getIngredients().size() : 0;
            textViewIngredientsCount.setText(String.valueOf(ingredientCount));

            // Set optional fields and their separators
            boolean prepTimeVisible = setNumericField(textViewPrepTime, recipe.getPrepTime());
            prepTimeSeparator.setVisibility(prepTimeVisible ? View.VISIBLE : View.GONE);

            boolean cookTimeVisible = setNumericField(textViewCookTime, recipe.getCookTime());
            cookTimeSeparator.setVisibility(cookTimeVisible ? View.VISIBLE : View.GONE);

            boolean servingsVisible = false;
            if (recipe.getServings() > 0) {
                textViewServings.setText(String.valueOf(recipe.getServings()));
                textViewServings.setVisibility(View.VISIBLE);
                servingsVisible = true;
            } else {
                textViewServings.setVisibility(View.GONE);
            }
            servingsSeparator.setVisibility(servingsVisible ? View.VISIBLE : View.GONE);

            // Set optional text field (category)
            setOptionalTextField(textViewCategory, recipe.getCategory());

            // Load recipe image
            if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(recipe.getImageUrl())
                        .placeholder(R.drawable.ic_recipe_placeholder_24)
                        .error(R.drawable.ic_recipe_placeholder_24)
                        .centerCrop()
                        .into(imageViewRecipe);
            } else {
                imageViewRecipe.setImageResource(R.drawable.ic_recipe_placeholder_24);
            }
        }

        private void setOptionalTextField(TextView textView, String value) {
            if (value != null && !value.trim().isEmpty()) {
                textView.setText(value);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }

        private boolean setNumericField(TextView textView, String value) {
            if (value != null && !value.trim().isEmpty()) {
                // Replaces all non-digit characters with an empty string
                String numberOnly = value.replaceAll("\\D+", "");
                if (!numberOnly.isEmpty()) {
                    textView.setText(numberOnly);
                    textView.setVisibility(View.VISIBLE);
                    return true;
                } else {
                    textView.setVisibility(View.GONE);
                    return false;
                }
            } else {
                textView.setVisibility(View.GONE);
                return false;
            }
        }
    }
}