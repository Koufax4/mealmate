package com.example.mealmate.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.Recipe;
import android.widget.ImageButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Fragment for displaying the user's recipe list with various UI states.
 */
public class RecipeListFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private RecipeAdapter recipeAdapter;

    // UI Components
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewRecipes;
    private TextView textViewRecipeCount;
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private TextView textViewErrorMessage;
    private MaterialButton buttonCreateFirstRecipe;
    private MaterialButton buttonRetry;
    private FloatingActionButton fabAddRecipe;
    private ImageButton buttonBack;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipe_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Load recipes on first load
        loadRecipes();
    }

    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerViewRecipes = view.findViewById(R.id.recyclerViewRecipes);
        textViewRecipeCount = view.findViewById(R.id.textViewRecipeCount);
        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutError = view.findViewById(R.id.layoutError);
        textViewErrorMessage = view.findViewById(R.id.textViewErrorMessage);
        buttonCreateFirstRecipe = view.findViewById(R.id.buttonCreateFirstRecipe);
        buttonRetry = view.findViewById(R.id.buttonRetry);
        fabAddRecipe = view.findViewById(R.id.fabAddRecipe);
        buttonBack = view.findViewById(R.id.buttonBack);
    }

    private void setupRecyclerView() {
        recipeAdapter = new RecipeAdapter();
        recyclerViewRecipes.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewRecipes.setAdapter(recipeAdapter);

        // Set up recipe click listener
        recipeAdapter.setOnRecipeClickListener(recipe -> {
            // Navigate to recipe detail
            Bundle args = new Bundle();
            args.putString("recipeId", recipe.getRecipeId());
            args.putString("recipeName", recipe.getName());
            Navigation.findNavController(requireView())
                    .navigate(R.id.action_recipeListFragment_to_recipeDetailFragment, args);
        });

        // Set up recipe menu click listener
        recipeAdapter.setOnRecipeMenuClickListener(this::showRecipeMenu);
    }

    private void setupClickListeners() {
        // Pull to refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadRecipes);

        // FAB and Create First Recipe button
        fabAddRecipe.setOnClickListener(v -> navigateToAddRecipe());
        buttonCreateFirstRecipe.setOnClickListener(v -> navigateToAddRecipe());

        // Retry button
        buttonRetry.setOnClickListener(v -> loadRecipes());
        buttonBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

    }

    private void navigateToAddRecipe() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_recipeListFragment_to_addRecipeFragment);
    }

    private void loadRecipes() {
        recipeViewModel.loadUserRecipes();
    }

    private void showRecipeMenu(Recipe recipe, View anchorView) {
        PopupMenu popupMenu = new PopupMenu(getContext(), anchorView);
        popupMenu.getMenuInflater().inflate(R.menu.recipe_item_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_view_recipe) {
                // Navigate to recipe detail
                Bundle args = new Bundle();
                args.putString("recipeId", recipe.getRecipeId());
                args.putString("recipeName", recipe.getName());
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_recipeListFragment_to_recipeDetailFragment, args);
                return true;
            } else if (itemId == R.id.menu_delete_recipe) {
                deleteRecipe(recipe);
                return true;
            }
            return false;
        });

        popupMenu.show();
    }

    private void deleteRecipe(Recipe recipe) {
        // Show confirmation and delete
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Recipe")
                .setMessage("Are you sure you want to delete \"" + recipe.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    recipeViewModel.deleteRecipe(recipe);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void observeViewModel() {
        // Observe recipes list
        recipeViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), resource -> {
            swipeRefreshLayout.setRefreshing(false);

            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        if (recipeAdapter.getItemCount() == 0) {
                            showLoadingState();
                        }
                        break;

                    case SUCCESS:
                        List<Recipe> recipes = resource.data;
                        if (recipes != null && !recipes.isEmpty()) {
                            showRecipesState(recipes);
                        } else {
                            showEmptyState();
                        }
                        break;

                    case ERROR:
                        if (recipeAdapter.getItemCount() == 0) {
                            showErrorState(resource.message);
                        } else {
                            showMessage("Failed to refresh recipes: " + resource.message);
                        }
                        break;
                }
            }
        });

        // Observe delete result
        recipeViewModel.getDeleteRecipeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        // Could show a progress indicator
                        break;

                    case SUCCESS:
                        showMessage("Recipe deleted successfully");
                        recipeViewModel.clearDeleteRecipeResult();
                        // Reload recipes to update the list
                        loadRecipes();
                        break;

                    case ERROR:
                        showMessage("Failed to delete recipe: " + resource.message);
                        recipeViewModel.clearDeleteRecipeResult();
                        break;
                }
            }
        });
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        recyclerViewRecipes.setVisibility(View.GONE);
    }

    private void showRecipesState(List<Recipe> recipes) {
        layoutLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        recyclerViewRecipes.setVisibility(View.VISIBLE);

        recipeAdapter.updateRecipes(recipes);
        updateRecipeCount(recipes.size());
    }

    private void showEmptyState() {
        layoutLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        recyclerViewRecipes.setVisibility(View.GONE);

        updateRecipeCount(0);
    }

    private void showErrorState(String errorMessage) {
        layoutLoading.setVisibility(View.GONE);
        layoutEmptyState.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        recyclerViewRecipes.setVisibility(View.GONE);

        textViewErrorMessage.setText(errorMessage != null ? errorMessage : "Unknown error occurred");
        updateRecipeCount(0);
    }

    private void updateRecipeCount(int count) {
        String countText = count == 1 ? "1 recipe" : count + " recipes";
        textViewRecipeCount.setText(countText);
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh recipes when returning to this fragment
        loadRecipes();
    }
}