package com.example.mealmate.ui.grocery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.mealmate.data.model.GroceryItem;
import com.example.mealmate.databinding.FragmentGroceryListBinding;

import java.util.List;

/**
 * Fragment for displaying and managing grocery lists.
 */
public class GroceryListFragment extends Fragment implements GroceryItemAdapter.OnItemClickListener {

    private static final String ARG_MEAL_PLAN_ID = "meal_plan_id";

    private FragmentGroceryListBinding binding;
    private GroceryViewModel groceryViewModel;
    private GroceryItemAdapter adapter;

    private String mealPlanId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mealPlanId = getArguments().getString(ARG_MEAL_PLAN_ID);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGroceryListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        groceryViewModel = new ViewModelProvider(this).get(GroceryViewModel.class);

        setupRecyclerView();
        setupClickListeners();
        observeViewModel();

        // Generate grocery list from the meal plan ID
        if (mealPlanId != null) {
            groceryViewModel.generateGroceryListFromPlanId(mealPlanId);
        } else {
            showError("No meal plan provided");
        }
    }

    private void setupRecyclerView() {
        adapter = new GroceryItemAdapter();
        adapter.setOnItemClickListener(this);
        binding.recyclerViewGroceryItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewGroceryItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void observeViewModel() {
        // Observe grocery list data
        groceryViewModel.getGroceryListLiveData().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        hideLoading();
                        if (resource.data != null && !resource.data.isEmpty()) {
                            showContent(resource.data);
                        } else {
                            showEmpty();
                        }
                        break;
                    case ERROR:
                        hideLoading();
                        showError("Failed to load grocery list: " + resource.message);
                        break;
                    case LOADING:
                        showLoading();
                        break;
                }
            }
        });

        // Observe delete item result
        groceryViewModel.getDeleteItemResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        showSuccess("Item removed from list");
                        groceryViewModel.clearDeleteItemResult();
                        // Refresh the list to show updated state
                        groceryViewModel.loadGroceryList();
                        break;
                    case ERROR:
                        showError("Failed to remove item: " + resource.message);
                        groceryViewModel.clearDeleteItemResult();
                        break;
                }
            }
        });

        // Other observers for updateItemResult, generateListResult can be added if specific UI feedback is needed
    }

    private void showLoading() {
        binding.layoutLoading.setVisibility(View.VISIBLE);
        binding.layoutContent.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.GONE);
    }

    private void hideLoading() {
        binding.layoutLoading.setVisibility(View.GONE);
    }

    private void showContent(List<GroceryItem> items) {
        binding.layoutContent.setVisibility(View.VISIBLE);
        binding.layoutEmpty.setVisibility(View.GONE);
        adapter.updateItems(items);
        updateSummary(items);
    }

    private void showEmpty() {
        binding.layoutContent.setVisibility(View.GONE);
        binding.layoutEmpty.setVisibility(View.VISIBLE);
    }

    private void updateSummary(List<GroceryItem> items) {
        int totalItems = items.size();
        long purchasedItems = items.stream().filter(GroceryItem::isPurchased).count();
        binding.textTotalItems.setText(String.valueOf(totalItems));
        binding.textPurchasedItems.setText(String.valueOf(purchasedItems));
    }

    private void showError(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccess(String message) {
        if(getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPurchasedToggle(GroceryItem item, boolean purchased) {
        groceryViewModel.updateItem(item);
    }

    @Override
    public void onDeleteClick(GroceryItem item) {
        groceryViewModel.deleteItem(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}