package com.example.mealmate.ui.recipes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mealmate.data.model.Ingredient;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.databinding.FragmentAddRecipeBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

/**
 * Fragment for creating new recipes with dynamic ingredients list and image
 * upload.
 */
public class AddRecipeFragment extends Fragment {

    private FragmentAddRecipeBinding binding;
    private RecipeViewModel recipeViewModel;
    private IngredientAdapter ingredientAdapter;

    // Image selection launcher
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize RecipeViewModel
        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

        // Initialize image picker launcher
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        if (imageUri != null) {
                            recipeViewModel.setSelectedImageUri(imageUri);
                            binding.imageViewRecipe.setImageURI(imageUri);
                            binding.textViewAddPhoto.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddRecipeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientAdapter();
        binding.recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewIngredients.setAdapter(ingredientAdapter);

        // Set up ingredient removal listener
        ingredientAdapter.setOnIngredientRemovedListener(position -> {
            // Update visibility of remove buttons
            ingredientAdapter.notifyDataSetChanged();
        });
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        // Image selection
        binding.imageViewRecipe.setOnClickListener(v -> openImagePicker());
        binding.textViewAddPhoto.setOnClickListener(v -> openImagePicker());

        // Add ingredient
        binding.buttonAddIngredient.setOnClickListener(v -> {
            ingredientAdapter.addIngredient();
            binding.recyclerViewIngredients.scrollToPosition(ingredientAdapter.getItemCount() - 1);
        });

        // Cancel button
        binding.buttonCancel.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Save recipe
        binding.buttonSaveRecipe.setOnClickListener(v -> validateAndSaveRecipe());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Recipe Image"));
    }

    private void validateAndSaveRecipe() {
        String recipeName = binding.editTextRecipeName.getText().toString().trim();
        String instructions = binding.editTextInstructions.getText().toString().trim();

        // Validate required fields
        if (recipeName.isEmpty()) {
            binding.editTextRecipeName.setError("Recipe name is required");
            binding.editTextRecipeName.requestFocus();
            return;
        }

        if (instructions.isEmpty()) {
            binding.editTextInstructions.setError("Instructions are required");
            binding.editTextInstructions.requestFocus();
            return;
        }

        // Get valid ingredients
        List<Ingredient> validIngredients = ingredientAdapter.getValidIngredients();
        if (validIngredients.isEmpty()) {
            showMessage("Please add at least one ingredient");
            return;
        }

        // Create recipe object
        Recipe recipe = new Recipe();
        recipe.setName(recipeName);
        recipe.setIngredients(validIngredients);
        recipe.setInstructions(instructions);

        // Set optional fields
        String prepTime = binding.editTextPrepTime.getText().toString().trim();
        if (!prepTime.isEmpty()) {
            recipe.setPrepTime(prepTime);
        }

        String cookTime = binding.editTextCookTime.getText().toString().trim();
        if (!cookTime.isEmpty()) {
            recipe.setCookTime(cookTime);
        }

        String servingsText = binding.editTextServings.getText().toString().trim();
        if (!servingsText.isEmpty()) {
            try {
                int servings = Integer.parseInt(servingsText);
                recipe.setServings(servings);
            } catch (NumberFormatException e) {
                // Ignore invalid servings input
            }
        }

        String category = binding.editTextCategory.getText().toString().trim();
        if (!category.isEmpty()) {
            recipe.setCategory(category);
        }

        // Save recipe
        recipeViewModel.saveRecipe(recipe);
    }

    private void observeViewModel() {
        recipeViewModel.getSaveRecipeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case LOADING:
                        setLoading(true);
                        break;

                    case SUCCESS:
                        setLoading(false);
                        showMessage("Recipe saved successfully!");
                        recipeViewModel.clearSaveRecipeResult();
                        Navigation.findNavController(requireView()).navigateUp();
                        break;

                    case ERROR:
                        setLoading(false);
                        showMessage("Failed to save recipe: " + resource.message);
                        break;
                }
            }
        });
    }

    private void setLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonSaveRecipe.setEnabled(!isLoading);
        binding.buttonCancel.setEnabled(!isLoading);
        binding.buttonAddIngredient.setEnabled(!isLoading);
        binding.imageViewRecipe.setEnabled(!isLoading);
    }

    private void showMessage(String message) {
        if (getView() != null) {
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear the selected image URI when leaving the fragment
        if (recipeViewModel != null) {
            recipeViewModel.clearSelectedImageUri();
        }
        binding = null;
    }
}