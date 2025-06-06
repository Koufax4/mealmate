package com.example.mealmate.ui.recipes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.Recipe;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import android.widget.ImageButton;

/**
 * Fragment for displaying detailed recipe information.
 */
public class RecipeDetailFragment extends Fragment {

    private RecipeViewModel recipeViewModel;
    private IngredientDisplayAdapter ingredientDisplayAdapter;

    // Arguments
    private String recipeId;
    private String recipeName;

    // UI Components
    private ImageView imageViewRecipe;
    private TextView textViewRecipeName;
    private TextView textViewPrepTime;
    private TextView textViewCookTime;
    private TextView textViewServings;
    private TextView textViewCategory;
    private RecyclerView recyclerViewIngredients;
    private TextView textViewInstructions;
    private LinearLayout layoutLoading;
    private LinearLayout layoutError;
    private TextView textViewErrorMessage;
    private MaterialButton buttonRetry;
    private ImageButton buttonBack;
    private TextView textViewHeaderTitle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        recipeViewModel = new ViewModelProvider(this).get(RecipeViewModel.class);

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
        return inflater.inflate(R.layout.fragment_recipe_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
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

    private void initializeViews(View view) {
        imageViewRecipe = view.findViewById(R.id.imageViewRecipe);
        textViewRecipeName = view.findViewById(R.id.textViewRecipeName);
        textViewPrepTime = view.findViewById(R.id.textViewPrepTime);
        textViewCookTime = view.findViewById(R.id.textViewCookTime);
        textViewServings = view.findViewById(R.id.textViewServings);
        textViewCategory = view.findViewById(R.id.textViewCategory);
        recyclerViewIngredients = view.findViewById(R.id.recyclerViewIngredients);
        textViewInstructions = view.findViewById(R.id.textViewInstructions);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        layoutError = view.findViewById(R.id.layoutError);
        textViewErrorMessage = view.findViewById(R.id.textViewErrorMessage);
        buttonRetry = view.findViewById(R.id.buttonRetry);
        buttonBack = view.findViewById(R.id.buttonBack);
        textViewHeaderTitle = view.findViewById(R.id.textViewHeaderTitle);
        imageViewRecipe = view.findViewById(R.id.imageViewRecipe);
    }

    private void setupRecyclerView() {
        ingredientDisplayAdapter = new IngredientDisplayAdapter();
        recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewIngredients.setAdapter(ingredientDisplayAdapter);
    }

    private void setupClickListeners() {
        buttonRetry.setOnClickListener(v -> loadRecipeDetail());
        buttonBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
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
    }

    private void showLoadingState() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutError.setVisibility(View.GONE);
        hideRecipeContent();
    }

    private void showRecipeDetail(Recipe recipe) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        showRecipeContent();

        populateRecipeData(recipe);
    }

    private void showErrorState(String errorMessage) {
        layoutLoading.setVisibility(View.GONE);
        layoutError.setVisibility(View.VISIBLE);
        hideRecipeContent();

        textViewErrorMessage.setText(errorMessage != null ? errorMessage : "Unknown error occurred");
    }

    private void hideRecipeContent() {
        imageViewRecipe.setVisibility(View.GONE);
        textViewRecipeName.setVisibility(View.GONE);
        recyclerViewIngredients.setVisibility(View.GONE);
        textViewInstructions.setVisibility(View.GONE);
    }

    private void showRecipeContent() {
        imageViewRecipe.setVisibility(View.VISIBLE);
        textViewRecipeName.setVisibility(View.VISIBLE);
        recyclerViewIngredients.setVisibility(View.VISIBLE);
        textViewInstructions.setVisibility(View.VISIBLE);
    }

    private void populateRecipeData(Recipe recipe) {
        // Set recipe name
        textViewRecipeName.setText(recipe.getName());
        textViewHeaderTitle.setText(recipe.getName());

        // Load recipe image
        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(this)
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_recipe_placeholder_24)
                    .error(R.drawable.ic_recipe_placeholder_24)
                    .centerCrop()
                    .into(imageViewRecipe);
        } else {
            imageViewRecipe.setImageResource(R.drawable.ic_recipe_placeholder_24);
        }

        // Set optional metadata
        setOptionalField(textViewPrepTime, recipe.getPrepTime(), "Prep: ");
        setOptionalField(textViewCookTime, recipe.getCookTime(), "Cook: ");

        if (recipe.getServings() > 0) {
            String servingText = recipe.getServings() == 1 ? "1 serving" : recipe.getServings() + " servings";
            textViewServings.setText(servingText);
            textViewServings.setVisibility(View.VISIBLE);
        } else {
            textViewServings.setVisibility(View.GONE);
        }

        setOptionalField(textViewCategory, recipe.getCategory(), "");

        // Set ingredients
        if (recipe.getIngredients() != null) {
            ingredientDisplayAdapter.updateIngredients(recipe.getIngredients());
        }

        // Set instructions
        if (recipe.getInstructions() != null && !recipe.getInstructions().trim().isEmpty()) {
            textViewInstructions.setText(recipe.getInstructions());
        } else {
            textViewInstructions.setText("No instructions provided.");
        }
    }

    private void setOptionalField(TextView textView, String value, String prefix) {
        if (value != null && !value.trim().isEmpty()) {
            textView.setText(prefix + value);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
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
        // Clear recipe detail data when leaving
        if (recipeViewModel != null) {
            recipeViewModel.clearRecipeDetail();
        }
    }
}