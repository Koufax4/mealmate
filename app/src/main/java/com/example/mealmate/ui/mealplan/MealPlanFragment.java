package com.example.mealmate.ui.mealplan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.SnapHelper;

import com.example.mealmate.R;
import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.MealPlan;
import com.example.mealmate.data.model.Recipe;
import com.example.mealmate.databinding.FragmentMealPlanBinding;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * MealPlanFragment displays the weekly meal planning interface.
 * Users can assign recipes to different days of the week and save their meal
 * plans.
 */
public class MealPlanFragment extends Fragment {

    private FragmentMealPlanBinding binding;
    private MealPlanViewModel mealPlanViewModel;
    private final List<View> weekDayViews = new ArrayList<>();

    private Map<String, LinearLayout> dayLayouts;
    private Map<String, LinearLayout> addButtons;
    private String selectedDay;
    private List<Recipe> availableRecipes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMealPlanBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mealPlanViewModel = new ViewModelProvider(this).get(MealPlanViewModel.class);

        setupWeekView();
        initializeDayLayouts();
        setupClickListeners();
        observeViewModel();

        mealPlanViewModel.loadUserRecipes();
    }

    private void setupWeekView() {
        String[] dayNames = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        binding.weekDaysContainer.removeAllViews();
        weekDayViews.clear();

        for (String dayName : dayNames) {
            View dayView = getLayoutInflater().inflate(R.layout.item_week_day, binding.weekDaysContainer, false);
            ((TextView) dayView.findViewById(R.id.text_day_of_week)).setText(dayName);
            weekDayViews.add(dayView);
            binding.weekDaysContainer.addView(dayView);
        }
    }


    private void initializeDayLayouts() {
        dayLayouts = new HashMap<>();
        addButtons = new HashMap<>();

        View monday = binding.mondayCard.getRoot();
        dayLayouts.put("Monday", monday.findViewById(R.id.layout_meals));
        addButtons.put("Monday", monday.findViewById(R.id.button_add_meal));
        ((TextView) monday.findViewById(R.id.text_day_title)).setText("Monday");

        View tuesday = binding.tuesdayCard.getRoot();
        dayLayouts.put("Tuesday", tuesday.findViewById(R.id.layout_meals));
        addButtons.put("Tuesday", tuesday.findViewById(R.id.button_add_meal));
        ((TextView) tuesday.findViewById(R.id.text_day_title)).setText("Tuesday");

        View wednesday = binding.wednesdayCard.getRoot();
        dayLayouts.put("Wednesday", wednesday.findViewById(R.id.layout_meals));
        addButtons.put("Wednesday", wednesday.findViewById(R.id.button_add_meal));
        ((TextView) wednesday.findViewById(R.id.text_day_title)).setText("Wednesday");

        View thursday = binding.thursdayCard.getRoot();
        dayLayouts.put("Thursday", thursday.findViewById(R.id.layout_meals));
        addButtons.put("Thursday", thursday.findViewById(R.id.button_add_meal));
        ((TextView) thursday.findViewById(R.id.text_day_title)).setText("Thursday");

        View friday = binding.fridayCard.getRoot();
        dayLayouts.put("Friday", friday.findViewById(R.id.layout_meals));
        addButtons.put("Friday", friday.findViewById(R.id.button_add_meal));
        ((TextView) friday.findViewById(R.id.text_day_title)).setText("Friday");

        View saturday = binding.saturdayCard.getRoot();
        dayLayouts.put("Saturday", saturday.findViewById(R.id.layout_meals));
        addButtons.put("Saturday", saturday.findViewById(R.id.button_add_meal));
        ((TextView) saturday.findViewById(R.id.text_day_title)).setText("Saturday");

        View sunday = binding.sundayCard.getRoot();
        dayLayouts.put("Sunday", sunday.findViewById(R.id.layout_meals));
        addButtons.put("Sunday", sunday.findViewById(R.id.button_add_meal));
        ((TextView) sunday.findViewById(R.id.text_day_title)).setText("Sunday");
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonPreviousWeek.setOnClickListener(v -> mealPlanViewModel.goToPreviousWeek());
        binding.buttonNextWeek.setOnClickListener(v -> mealPlanViewModel.goToNextWeek());
        binding.fabSaveMealPlan.setOnClickListener(v -> mealPlanViewModel.saveMealPlan());

        for (Map.Entry<String, LinearLayout> entry : addButtons.entrySet()) {
            String day = entry.getKey();
            entry.getValue().setOnClickListener(v -> showRecipeSelectionDialog(day));
        }
    }

    private void observeViewModel() {
        mealPlanViewModel.getRecipesLiveData().observe(getViewLifecycleOwner(), (Observer<AuthResource<List<Recipe>>>) resource -> {
            if (resource != null && resource.status == AuthResource.Status.SUCCESS) {
                availableRecipes = resource.data;
                mealPlanViewModel.populateRecipeMap(availableRecipes);
                mealPlanViewModel.loadCurrentWeekMealPlan();
            } else if (resource != null && resource.status == AuthResource.Status.ERROR) {
                showError("Failed to load recipes: " + resource.message);
            }
        });

        mealPlanViewModel.getCurrentMealPlanLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        if (resource.data != null) {
                            mealPlanViewModel.setCurrentMealPlan(resource.data);
                            updateMealPlanUI(resource.data);
                        } else {
                            mealPlanViewModel.createNewMealPlan();
                        }
                        break;
                    case ERROR:
                        showError("Failed to load meal plan: " + resource.message);
                        break;
                    case LOADING:
                        updateMealPlanUI(null);
                        break;
                }
            }
        });

        mealPlanViewModel.getSaveMealPlanResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                if (resource.status == AuthResource.Status.SUCCESS) {
                    showSuccess("Meal plan saved successfully!");
                    mealPlanViewModel.clearSaveMealPlanResult();
                } else if (resource.status == AuthResource.Status.ERROR) {
                    showError("Failed to save meal plan: " + resource.message);
                }
            }
        });
    }

    private void updateMealPlanUI(MealPlan mealPlan) {
        binding.textWeekRange.setText(mealPlanViewModel.getFormattedWeekStart());
        updateWeekView(mealPlanViewModel.getCurrentWeekDates());

        for (LinearLayout dayLayout : dayLayouts.values()) {
            clearDayRecipes(dayLayout);
        }

        if (mealPlan == null) return;

        if (mealPlan.getDays() != null) {
            for (Map.Entry<String, LinearLayout> entry : dayLayouts.entrySet()) {
                String day = entry.getKey();
                LinearLayout dayLayout = entry.getValue();
                List<String> dayRecipes = mealPlan.getDays().get(day);
                if (dayRecipes != null) {
                    for (String recipeId : dayRecipes) {
                        addRecipeToDay(dayLayout, recipeId, day);
                    }
                }
            }
        }
    }

    private void updateWeekView(List<Date> weekDates) {
        Calendar today = Calendar.getInstance();
        for (int i = 0; i < weekDates.size(); i++) {
            View dayView = weekDayViews.get(i);
            TextView dayOfMonthText = dayView.findViewById(R.id.text_day_of_month);
            TextView dayOfWeekText = dayView.findViewById(R.id.text_day_of_week);

            Calendar itemCal = Calendar.getInstance();
            itemCal.setTime(weekDates.get(i));

            dayOfMonthText.setText(new SimpleDateFormat("d", Locale.getDefault()).format(weekDates.get(i)));

            boolean isSelected = today.get(Calendar.DAY_OF_YEAR) == itemCal.get(Calendar.DAY_OF_YEAR) &&
                    today.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR);

            if (isSelected) {
                dayOfMonthText.setBackgroundResource(R.drawable.day_selected_background);
                dayOfMonthText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                dayOfWeekText.setTextColor(ContextCompat.getColor(requireContext(), R.color.purple_700));
            } else {
                dayOfMonthText.setBackgroundResource(android.R.color.transparent);
                dayOfMonthText.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                dayOfWeekText.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_600));
            }
        }
    }

    private void clearDayRecipes(LinearLayout dayLayout) {
        for (int i = dayLayout.getChildCount() - 1; i >= 0; i--) {
            View child = dayLayout.getChildAt(i);
            if (child.getId() != R.id.button_add_meal) {
                dayLayout.removeViewAt(i);
            }
        }
    }

    private void addRecipeToDay(LinearLayout dayLayout, String recipeId, String day) {
        Recipe recipe = mealPlanViewModel.getRecipeById(recipeId);
        String recipeName = (recipe != null) ? recipe.getName() : "Unknown Recipe";

        View recipeView = LayoutInflater.from(getContext()).inflate(R.layout.item_meal_plan_recipe, dayLayout, false);
        ((TextView) recipeView.findViewById(R.id.text_recipe_name)).setText(recipeName);

        recipeView.findViewById(R.id.button_remove_meal).setOnClickListener(v -> {
            mealPlanViewModel.removeRecipeFromDay(day, recipeId);
        });

        recipeView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("recipeId", recipeId);
            NavHostFragment.findNavController(this).navigate(R.id.action_mealPlanFragment_to_recipeDetailFragment, args);
        });

        dayLayout.addView(recipeView, dayLayout.getChildCount() - 1);
    }

    private void showRecipeSelectionDialog(String day) {
        if (availableRecipes == null || availableRecipes.isEmpty()) {
            showError("No recipes available. Please create some recipes first.");
            return;
        }
        selectedDay = day;
        RecipeSelectionDialogFragment dialog = RecipeSelectionDialogFragment.newInstance(availableRecipes);
        dialog.setOnRecipeSelectedListener(recipe -> mealPlanViewModel.assignRecipeToDay(selectedDay, recipe.getRecipeId()));
        dialog.show(getParentFragmentManager(), "RecipeSelectionDialog");
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}