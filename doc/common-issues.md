# Common Crash and ANR Fixes

This document records critical bugs that have been fixed during development. It serves as a reference to understand past issues and prevent them from recurring.

---

### 1. `InflateException` due to Missing Resources or Mismatched Styles

**Symptom:**
The application crashes immediately upon trying to display a new screen. The logcat shows a `FATAL EXCEPTION` with `android.view.InflateException`.

**Root Cause Analysis:**
This crash occurred for two main reasons:

1.  **Missing Resources:** An XML layout file (`.xml`) referenced a resource that did not exist in the `res/` directory. This included misspelled color names (`@color/grey_` instead of `@color/gray_`), or missing drawables (`@drawable/ic_add_24`).
2.  **Mismatched Styles:** A layout (`item_ingredient.xml`) used a Material 3 style (`@style/Widget.Material3...`) while the application's theme was based on Material 2 (`Theme.MaterialComponents...`). The two style systems are incompatible and cannot be mixed, which results in a crash when the system tries to find required theme attributes.

**Solution:**

1.  Verify that every resource ID (`@color/`, `@drawable/`, `@style/`) used in an XML layout corresponds to an actual file or definition in the `res/` directory.
2.  Ensure that all styles used in layouts are compatible with the base application theme. For our `Theme.MaterialComponents` theme, all component styles should also come from `Widget.MaterialComponents...`.

**Key Takeaway / Prevention:**
Always double-check resource names for typos. When using styled components, ensure the style version (e.g., Material 2 vs. Material 3) is consistent with the app's base theme.

---

### 2. `Resources$NotFoundException` due to Incorrect Resource Type

**Symptom:**
The app crashes when programmatically creating and adding views to a layout. The logcat shows a `FATAL EXCEPTION` with `android.content.res.Resources$NotFoundException`.

**Root Cause Analysis:**
The code was attempting to load a theme attribute (`android.R.attr.selectableItemBackground`) using a method intended for drawable resources (`getResources().getDrawable()`). Theme attributes and drawable resources are different types, and this incorrect usage results in the system being unable to find the specified resource ID.

**Solution:**
The problematic line of code was removed. The desired ripple effect was already being applied correctly because the `MaterialCardView` was set as `clickable` and `focusable`.

**Key Takeaway / Prevention:**
Do not load theme attributes (`R.attr...`) with methods designed for drawables (`R.drawable...`). Understand the difference between resource types to use the correct APIs.

---

### 3. ANR (Application Not Responding) due to `LiveData` Infinite Loop

**Symptom:**
The application freezes completely, especially when navigating between weeks in the meal planner. Logcat reports an `ANR` because the main UI thread is blocked.

**Root Cause Analysis:**
A `LiveData` observer in `MealPlanFragment` was triggering a method call in `MealPlanViewModel`. This method then posted a new value back to the _exact same_ `LiveData` that the fragment was observing. This created a recursive infinite loop (`observe -> update -> observe -> update...`), which blocked the main thread and caused the ANR.

**Solution:**
The logic was refactored to break the feedback loop.

- The `MealPlanFragment`'s observer was simplified to only react to data.
- If an action was needed (like creating a new plan when none was found), it calls a separate method in the ViewModel that does not directly re-post to the same `LiveData` stream in a looping manner. The ViewModel now manages its own state more cleanly.

**Key Takeaway / Prevention:**
A `LiveData` observer should **never** trigger an action that directly or indirectly causes the same `LiveData` it is observing to be updated again within the same logical chain. This is a classic recipe for an infinite loop.

---

### 4. ANR (Application Not Responding) due to `observeForever` Misuse

**Symptom:**
The application would become sluggish and eventually freeze with an ANR after navigating to and from a screen multiple times.

**Root Cause Analysis:**
The `MealPlanViewModel` was using `recipesLiveData.observeForever(...)` to populate its internal `recipeMap`. `observeForever` attaches a listener that is **never automatically removed**. Each time the ViewModel was instantiated or the method was called, a new, permanent listener was added. When recipe data was loaded, all of these leaked listeners would fire at once, overwhelming the main thread.

**Solution:**
The `observeForever` call was replaced with `Transformations.map`. This creates a new `LiveData` stream that is derived from the original. The transformation logic (populating the map) is executed automatically and efficiently whenever the source `LiveData` changes, without creating memory leaks or manual observer management.

**Key Takeaway / Prevention:**
Avoid using `observeForever` in ViewModels unless you have a very specific use case and are manually removing the observer in `onCleared()`. For reacting to `LiveData` changes, strongly prefer using `Transformations.map` or `Transformations.switchMap`, as they are lifecycle-aware and safer.

## Grocery List Refactoring - Common Issues & Solutions

**Issue: Transitioning from Meal Plan-Based to Persistent Grocery Lists**

- **Problem:** Original design generated temporary grocery lists from meal plans, but users needed a persistent list that accumulates ingredients from multiple recipes.
- **Solution:**
  - Refactored `GroceryViewModel.java` to remove meal plan dependencies
  - Implemented persistent list with hardcoded ID "main_list"
  - Added smart ingredient consolidation logic
  - Created FloatingActionButton in `RecipeDetailFragment` for adding ingredients

**Issue: Data Type Inconsistencies**

- **Problem:** Mixed use of String vs double for quantities, and checked vs purchased states
- **Solution:**
  - Standardized on String for quantities to handle mixed units ("2 cups", "1 lb")
  - Unified on "purchased" field throughout the system
  - Updated all model classes and database operations consistently

**Issue: Grocery List Counts Not Updating**

- **Problem:** When users checked/unchecked items, the summary counts didn't update immediately
- **Solution:**
  - Added missing observer for `updateItemResult` in `GroceryListFragment`
  - Implemented immediate UI feedback in `onPurchasedToggle` method
  - Added `getCurrentItems()` method to `GroceryItemAdapter` for accessing current state
  - Avoided unnecessary full list refreshes for simple state changes

**Issue: Inconsistent Action Bar Styling**

- **Problem:** `GroceryListFragment` used AppBarLayout while other fragments used custom headers
- **Solution:**
  - Replaced AppBarLayout with custom RelativeLayout header matching other fragments
  - Updated navigation click handlers from toolbar to buttonBack
  - Applied consistent purple theme and elevation effects
  - Used `fitsSystemWindows="true"` for proper status bar handling

**Issue: Navigation Flow Updates**

- **Problem:** Navigation actions and arguments were still tied to meal plan-based grocery list generation
- **Solution:**
  - Removed `mealPlanFragment` to `groceryListFragment` navigation action
  - Added direct `navigation_home` to `groceryListFragment` action
  - Removed `meal_plan_id` argument from grocery list destinations
  - Made recipe cards in meal plan clickable for navigation to recipe details

**Issue: Smart Ingredient Consolidation**

- **Problem:** Need to combine duplicate ingredients when adding from multiple recipes
- **Solution:**
  - Implemented case-insensitive name comparison
  - Added quantity summation for compatible units
  - Preserved purchased state of existing items during consolidation
  - Handled edge cases for incompatible units (keep as separate entries)
