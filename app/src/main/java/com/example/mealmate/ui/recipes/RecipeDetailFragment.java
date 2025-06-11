package com.example.mealmate.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.mealmate.R;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.databinding.FragmentRecipeDetailBinding;
import com.example.mealmate.ui.grocery.GroceryViewModel;
import com.google.android.material.snackbar.Snackbar;

/**
 * Fragment for displaying detailed recipe information.
 */
public class RecipeDetailFragment extends Fragment {

    private FragmentRecipeDetailBinding binding;
    private RecipeViewModel recipeViewModel;
    private GroceryViewModel groceryViewModel;
    private IngredientDisplayAdapter ingredientDisplayAdapter;

    // Arguments
    private String recipeId;
    private String recipeName;

    // Current recipe data
    private Recipe currentRecipe;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);
        groceryViewModel = new ViewModelProvider(this).get(GroceryViewModel.class);

        // Get arguments
        if (getArguments() != null) {
            recipeId = getArguments().getString("recipeId");
            recipeName = getArguments().getString("recipeName");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Load recipe details
        if (recipeId != null) {
            loadRecipeDetail();
        } else {
            showErrorState("Recipe ID not provided");
        }
    }

    private void setupRecyclerView() {
        ingredientDisplayAdapter = new IngredientDisplayAdapter();
        binding.recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewIngredients.setAdapter(ingredientDisplayAdapter);
    }

    private void setupClickListeners() {
        binding.buttonRetry.setOnClickListener(v -> loadRecipeDetail());
        binding.buttonBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.fabAddToGroceryList.setOnClickListener(v -> addToGroceryList());
    }

    private void loadRecipeDetail() {
        if (recipeId != null) {
            recipeViewModel.loadRecipeById(recipeId);
        }
    }

    private void observeViewModel() {
        recipeViewModel.getRecipeDetailLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        showLoadingState();
                        break;

                    case SUCCESS:
                        if (resource.data != null) {
                            showRecipeDetail(resource.data);
                        } else {
                            showErrorState("Recipe not found");
                        }
                        break;

                    case ERROR:
                        showErrorState(resource.message);
                        break;
                }
            }
        });

        // Observe add ingredients result
        groceryViewModel.getAddIngredientsResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        showMessage("Ingredients added to grocery list!");
                        groceryViewModel.clearAddIngredientsResult();
                        break;
                    case ERROR:
                        showMessage("Failed to add ingredients: " + resource.message);
                        groceryViewModel.clearAddIngredientsResult();
                        break;
                    case LOADING:
                        // Could show a loading indicator on the FAB if desired
                        break;
                }
            }
        });
    }

    private void showLoadingState() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutError.setVisibility(View.GONE);
    }

    private void showRecipeDetail(Recipe recipe) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.GONE);

        currentRecipe = recipe; // Store the recipe for adding to grocery list
        populateRecipeData(recipe);
    }

    private void showErrorState(String errorMessage) {
        binding.layoutLoading.setVisibility(View.GONE);
        binding.layoutError.setVisibility(View.VISIBLE);
        binding.textViewErrorMessage.setText(errorMessage != null ? errorMessage : "Unknown error occurred");
    }

    private void populateRecipeData(Recipe recipe) {
        // Set recipe name
        binding.textViewRecipeName.setText(recipe.getName());
        binding.textViewHeaderTitle.setText(recipe.getName());

        // Load recipe image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_recipe_placeholder_24)
                    .error(R.drawable.ic_recipe_placeholder_24)
                    .centerCrop()
                    .into(binding.imageViewRecipe);
        } else {
            binding.imageViewRecipe.setImageResource(R.drawable.ic_recipe_placeholder_24);
        }

        // Set optional metadata and separators
        boolean prepTimeVisible = setOptionalField(binding.textViewPrepTime, recipe.getPrepTime(), "Prep: ");
        boolean cookTimeVisible = setOptionalField(binding.textViewCookTime, recipe.getCookTime(), "Cook: ");
        boolean servingsVisible;

        if (recipe.getServings() > 0) {
            String servingText = recipe.getServings() == 1 ? "1 serving" : recipe.getServings() + " servings";
            binding.textViewServings.setText(servingText);
            binding.textViewServings.setVisibility(View.VISIBLE);
            servingsVisible = true;
        } else {
            binding.textViewServings.setVisibility(View.GONE);
            servingsVisible = false;
        }

        binding.separator1.setVisibility(prepTimeVisible && (cookTimeVisible || servingsVisible) ? View.VISIBLE : View.GONE);
        binding.separator2.setVisibility(cookTimeVisible && servingsVisible ? View.VISIBLE : View.GONE);

        setOptionalField(binding.textViewCategory, recipe.getCategory(), "");

        // Set ingredients
        if (recipe.getIngredients() != null) {
            ingredientDisplayAdapter.updateIngredients(recipe.getIngredients());
        }

        // Set instructions
        if (recipe.getInstructions() != null && !recipe.getInstructions().trim().isEmpty()) {
            binding.textViewInstructions.setText(recipe.getInstructions());
        } else {
            binding.textViewInstructions.setText("No instructions provided.");
        }
    }

    private boolean setOptionalField(TextView textView, String value, String prefix) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
            return true;
        } else {
            textView.setVisibility(View.GONE);
            return false;
        }
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Adds the current recipe's ingredients to the main grocery list.
     */
    private void addToGroceryList() {
        if (currentRecipe == null) {
            showMessage("No recipe loaded");
            return;
        }

        if (currentRecipe.getIngredients() == null || currentRecipe.getIngredients().isEmpty()) {
            showMessage("This recipe has no ingredients to add");
            return;
        }

        groceryViewModel.addIngredientsToGroceryList(currentRecipe.getIngredients());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear recipe detail data when leaving
        if (recipeViewModel != null) {
            recipeViewModel.clearRecipeDetail();
        }
        binding = null;
    }
}