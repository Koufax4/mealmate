package com.example.mealmate.ui.mealplan;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.data.model.Recipe;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog fragment for selecting a recipe to add to a meal plan.
 */
public class RecipeSelectionDialogFragment extends DialogFragment {

    private static final String ARG_RECIPES = "recipes";

    private List<Recipe> recipes;
    private OnRecipeSelectedListener listener;

    public interface OnRecipeSelectedListener {
        void onRecipeSelected(Recipe recipe);
    }

    public static RecipeSelectionDialogFragment newInstance(List<Recipe> recipes) {
        RecipeSelectionDialogFragment fragment = new RecipeSelectionDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPES, new ArrayList<>(recipes));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipes = (List<Recipe>) getArguments().getSerializable(ARG_RECIPES);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_recipe_selection, null);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewRecipes);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new RecipeSelectionAdapter());

        return new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Recipe")
                .setView(view)
                .setNegativeButton("Cancel", null)
                .create();
    }

    public void setOnRecipeSelectedListener(OnRecipeSelectedListener listener) {
        this.listener = listener;
    }

    private class RecipeSelectionAdapter extends RecyclerView.Adapter<RecipeSelectionAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_recipe_selection, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Recipe recipe = recipes.get(position);
            holder.bind(recipe);
        }

        @Override
        public int getItemCount() {
            return recipes != null ? recipes.size() : 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final MaterialCardView cardView;
            private final TextView textRecipeName;
            private final TextView textRecipeCategory;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                cardView = itemView.findViewById(R.id.cardRecipe);
                textRecipeName = itemView.findViewById(R.id.textRecipeName);
                textRecipeCategory = itemView.findViewById(R.id.textRecipeCategory);
            }

            public void bind(Recipe recipe) {
                textRecipeName.setText(recipe.getName());

                String category = recipe.getCategory();
                if (category != null && !category.isEmpty()) {
                    textRecipeCategory.setText(category);
                    textRecipeCategory.setVisibility(View.VISIBLE);
                } else {
                    textRecipeCategory.setVisibility(View.GONE);
                }

                cardView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onRecipeSelected(recipe);
                    }
                    dismiss();
                });
            }
        }
    }
}