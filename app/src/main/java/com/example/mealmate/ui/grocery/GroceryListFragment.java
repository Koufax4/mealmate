package com.example.mealmate.ui.grocery;

import android.content.Intent;
import android.net.Uri;
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

import com.example.mealmate.data.model.AuthResource;
import com.example.mealmate.data.model.GroceryItem;
import com.example.mealmate.databinding.FragmentGroceryListBinding;

import java.util.List;

/**
 * Fragment for displaying and managing grocery lists.
 */
public class GroceryListFragment extends Fragment implements GroceryItemAdapter.OnItemClickListener {

    private FragmentGroceryListBinding binding;
    private GroceryViewModel groceryViewModel;
    private GroceryItemAdapter adapter;

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

        // Load the main grocery list
        groceryViewModel.loadGroceryList();
    }

    private void setupRecyclerView() {
        adapter = new GroceryItemAdapter();
        adapter.setOnItemClickListener(this);
        binding.recyclerViewGroceryItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewGroceryItems.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.buttonBack.setOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        binding.buttonShare.setOnClickListener(v -> shareGroceryList());
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

        // Observe update item result
        groceryViewModel.getUpdateItemResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource != null) {
                switch (resource.status) {
                    case SUCCESS:
                        groceryViewModel.clearUpdateItemResult();
                        // The UI for the checkbox is updated instantly. The summary is updated in
                        // onPurchasedToggle.
                        // No full refresh is needed, which provides a smoother experience.
                        break;
                    case ERROR:
                        showError("Failed to update item: " + resource.message);
                        groceryViewModel.clearUpdateItemResult();
                        // On error, refresh to revert any local changes
                        groceryViewModel.loadGroceryList();
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
    public void onPurchasedToggle(GroceryItem item, boolean purchased) {
        // Update the item in the backend
        groceryViewModel.updateItem(item);

        // Immediately update the summary to provide instant feedback
        updateSummary(adapter.getCurrentItems());
    }

    @Override
    public void onDeleteClick(GroceryItem item) {
        groceryViewModel.deleteItem(item);
    }

    /**
     * Shares the current grocery list via SMS
     */
    private void shareGroceryList() {
        // Get the current list from the LiveData for robustness
        AuthResource<List<GroceryItem>> resource = groceryViewModel.getGroceryListLiveData().getValue();
        if (resource == null || resource.status != AuthResource.Status.SUCCESS || resource.data == null
                || resource.data.isEmpty()) {
            Toast.makeText(getContext(), "Your list is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        List<GroceryItem> items = resource.data;
        String message = formatGroceryListForSharing(items);

        // Create SMS intent
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:")); // Use "smsto:" without a number to let the user choose
        intent.putExtra("sms_body", message);

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "No SMS app found", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Formats the grocery list into a clean, readable text format for sharing
     */
    private String formatGroceryListForSharing(List<GroceryItem> items) {
        StringBuilder messageBuilder = new StringBuilder();

        // Header with summary
        long purchasedItems = items.stream().filter(GroceryItem::isPurchased).count();
        messageBuilder.append("🛒 GROCERY LIST\n");
        messageBuilder.append("═══════════════\n");
        messageBuilder.append("Items: ").append(items.size());
        messageBuilder.append(" | Done: ").append(purchasedItems).append("\n\n");

        // Separate items into categories for better organization
        List<GroceryItem> pendingItems = items.stream()
                .filter(item -> !item.isPurchased())
                .collect(java.util.stream.Collectors.toList());

        List<GroceryItem> completedItems = items.stream()
                .filter(GroceryItem::isPurchased)
                .collect(java.util.stream.Collectors.toList());

        // Pending items first (most important)
        if (!pendingItems.isEmpty()) {
            messageBuilder.append("📋 TO BUY:\n");
            for (GroceryItem item : pendingItems) {
                messageBuilder.append("☐ ").append(formatItemText(item)).append("\n");
            }
        }

        // Completed items (if any)
        if (!completedItems.isEmpty()) {
            messageBuilder.append("\n✅ COMPLETED:\n");
            for (GroceryItem item : completedItems) {
                messageBuilder.append("☑ ").append(formatItemText(item)).append("\n");
            }
        }

        // Footer
        messageBuilder.append("\n📱 Sent from MealMate");

        return messageBuilder.toString();
    }

    /**
     * Formats individual item text with quantity and unit
     */
    private String formatItemText(GroceryItem item) {
        StringBuilder itemText = new StringBuilder();
        itemText.append(item.getName());

        // Format quantity and unit
        if (item.getQuantity() > 0) {
            String formattedQuantity;
            // If the quantity is a whole number, display without decimal
            if (item.getQuantity() == Math.floor(item.getQuantity())) {
                formattedQuantity = String.valueOf((int) item.getQuantity());
            } else {
                formattedQuantity = String.valueOf(item.getQuantity());
            }

            String unit = item.getUnit() != null && !item.getUnit().trim().isEmpty()
                    ? " " + item.getUnit().trim()
                    : "";
            itemText.append(" (").append(formattedQuantity).append(unit).append(")");
        }

        return itemText.toString();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}