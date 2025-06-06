package com.example.mealmate.ui.recipes;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.Ingredient;
import com.example.mealmate.data.model.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

/**
 * Fragment for creating new recipes with dynamic ingredients list and image
 * upload.
 */
public class AddRecipeFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private IngredientAdapter ingredientAdapter;

    // UI Components
    private ImageButton buttonBack;
    private ImageView imageViewRecipe;
    private TextView textViewAddPhoto;
    private TextInputEditText editTextRecipeName;
    private RecyclerView recyclerViewIngredients;
    private MaterialButton buttonAddIngredient;
    private TextInputEditText editTextInstructions;
    private TextInputEditText editTextPrepTime;
    private TextInputEditText editTextCookTime;
    private TextInputEditText editTextServings;
    private TextInputEditText editTextCategory;
    private MaterialButton buttonCancel;
    private MaterialButton buttonSaveRecipe;
    private ProgressBar progressBar;

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
                            imageViewRecipe.setImageURI(imageUri);
                            textViewAddPhoto.setVisibility(View.GONE);
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupClickListeners();
        observeViewModel();
    }

    private void initializeViews(View view) {
        buttonBack = view.findViewById(R.id.buttonBack);
        imageViewRecipe = view.findViewById(R.id.imageViewRecipe);
        textViewAddPhoto = view.findViewById(R.id.textViewAddPhoto);
        editTextRecipeName = view.findViewById(R.id.editTextRecipeName);
        recyclerViewIngredients = view.findViewById(R.id.recyclerViewIngredients);
        buttonAddIngredient = view.findViewById(R.id.buttonAddIngredient);
        editTextInstructions = view.findViewById(R.id.editTextInstructions);
        editTextPrepTime = view.findViewById(R.id.editTextPrepTime);
        editTextCookTime = view.findViewById(R.id.editTextCookTime);
        editTextServings = view.findViewById(R.id.editTextServings);
        editTextCategory = view.findViewById(R.id.editTextCategory);
        buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSaveRecipe = view.findViewById(R.id.buttonSaveRecipe);
        progressBar = view.findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        ingredientAdapter = new IngredientAdapter();
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredients.setAdapter(ingredientAdapter);

        // Set up ingredient removal listener
        ingredientAdapter.setOnIngredientRemovedListener(position -> {
            // Update visibility of remove buttons
            ingredientAdapter.notifyDataSetChanged();
        });
    }

    private void setupClickListeners() {
        buttonBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        // Image selection
        imageViewRecipe.setOnClickListener(v -> openImagePicker());
        textViewAddPhoto.setOnClickListener(v -> openImagePicker());

        // Add ingredient
        buttonAddIngredient.setOnClickListener(v -> {
            ingredientAdapter.addIngredient();
            recyclerViewIngredients.scrollToPosition(ingredientAdapter.getItemCount() - 1);
        });

        // Cancel button
        buttonCancel.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });

        // Save recipe
        buttonSaveRecipe.setOnClickListener(v -> validateAndSaveRecipe());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select Recipe Image"));
    }

    private void validateAndSaveRecipe() {
        String recipeName = editTextRecipeName.getText().toString().trim();
        String instructions = editTextInstructions.getText().toString().trim();

        // Validate required fields
        if (recipeName.isEmpty()) {
            editTextRecipeName.setError("Recipe name is required");
            editTextRecipeName.requestFocus();
            return;
        }

        if (instructions.isEmpty()) {
            editTextInstructions.setError("Instructions are required");
            editTextInstructions.requestFocus();
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
        String prepTime = editTextPrepTime.getText().toString().trim();
        if (!prepTime.isEmpty()) {
            recipe.setPrepTime(prepTime);
        }

        String cookTime = editTextCookTime.getText().toString().trim();
        if (!cookTime.isEmpty()) {
            recipe.setCookTime(cookTime);
        }

        String servingsText = editTextServings.getText().toString().trim();
        if (!servingsText.isEmpty()) {
            try {
                int servings = Integer.parseInt(servingsText);
                recipe.setServings(servings);
            } catch (NumberFormatException e) {
                // Ignore invalid servings input
            }
        }

        String category = editTextCategory.getText().toString().trim();
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
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSaveRecipe.setEnabled(!isLoading);
        buttonCancel.setEnabled(!isLoading);
        buttonAddIngredient.setEnabled(!isLoading);
        imageViewRecipe.setEnabled(!isLoading);
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
    }
}