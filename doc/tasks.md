# MealMate - Development Tasks

## Phase 0: Project Setup & Initial Documentation (Partially Done)

- [x] Basic Android Project Setup
- [x] Firebase Integration (Auth, Firestore, Storage dependencies added)
- [x] Initial `LoginFragment` UI created (`fragment_login.xml`)
- [x] Basic Navigation Graph setup (`mobile_navigation.xml`)

## Phase 1: Core Authentication (PRD: F2, US1, US2)

- [x] **Login Functionality:**
  - [x] Implement `LoginFragment.java` logic:
    - [x] Connect UI elements (EditTexts, Button, ProgressBar).
    - [x] Create `AuthViewModel.java` (extends `androidx.lifecycle.ViewModel`).
    - [x] Create `AuthRepository.java` to handle Firebase Auth calls.
    - [x] Implement email/password login using `FirebaseAuth.signInWithEmailAndPassword()`.
    - [x] Use `LiveData` in `AuthViewModel` to communicate login state (loading, success, error) to `LoginFragment`.
    - [x] Navigate to `navigation_home` on successful login.
    - [x] Display error messages (e.g., Toast, Snackbar) on failure.
  - [x] Handle "Forgot Password?" navigation/placeholder.
    - [x] Create `ForgotPasswordFragment.java` and `fragment_forgot_password.xml`.
    - [x] Implement logic for sending password reset email.
- [x] **Registration Functionality:**
  - [x] Create `RegisterFragment.java` and `fragment_register.xml`.
    - [x] UI for email, password, confirm password.
  - [x] Update `mobile_navigation.xml`:
    - [x] Change `navigation_register` to point to `com.example.mealmate.ui.auth.RegisterFragment`.
    - [x] Add action from `LoginFragment` to `navigation_register`.
    - [x] Add action from `RegisterFragment` back to `navigation_login` or to `navigation_home` upon successful registration.
  - [x] Implement `RegisterFragment.java` logic:
    - [x] Connect UI elements.
    - [x] Reuse `AuthViewModel` and `AuthRepository`.
    - [x] Implement email/password registration using `FirebaseAuth.createUserWithEmailAndPassword()`.
    - [x] Store basic user profile info in Firestore (`/users/{userId}/profileInfo/details`) upon successful registration (e.g., email, creation date).
    - [x] Navigate appropriately after registration.
    - [x] Delete Firebase Auth user if Firestore profile creation fails.
- [x] **Auth State Management:**
  - [x] In `MainActivity`, check current `FirebaseAuth.getCurrentUser()` on start.
    - [x] If user is logged in, navigate to `navigation_home`.
    - [x] If not, navigate to `navigation_login` (handled by startDestination).
  - [x] Implement Logout functionality (basic version in `HomeFragment`).
- [x] **UI/UX Refinements for Auth:**
  - [x] Ensure `BottomNavigationView` in `MainActivity` is hidden during Login/Register/Forgot Password flow.
  - [x] Add input validation for email and password fields.
  - [x] Applied custom rounded purple border style to `TextInputLayout` elements in auth screens.

## Phase 2: Core App Structure & Home Screen (PRD: F1)

- [x] **Home Screen (`HomeFragment.java`):**
  - [x] Define layout `fragment_home.xml` for the main landing page after login - _New modern UI implemented with dynamic greeting, hero card, navigation grid, and quick stats._
  - [x] Potentially display quick links or summaries (e.g., "Create Recipe", "View Meal Plan", "View Grocery List"). - _Implemented as navigation cards._
  - [x] Ensure `BottomNavigationView` is visible and functional for Home, Dashboard, Notifications.
- [x] **Data Models (POJOs - as per `doc/architecture.md` & `doc/technical.md`):**
  - [x] Create `com.example.mealmate.data.model.User.java`.
  - [x] Create `com.example.mealmate.data.model.Recipe.java`.
  - [x] Create `com.example.mealmate.data.model.Ingredient.java`.
  - [x] Create `com.example.mealmate.data.model.GroceryItem.java`.
  - [x] Create `com.example.mealmate.data.model.MealPlan.java`.
  - [x] Create `com.example.mealmate.data.model.StoreLocation.java`.

## Phase 3: Recipe Management (PRD: F4, US4)

- [x] **Recipe Creation (`AddRecipeFragment`):**
  - [x] Create `AddRecipeFragment.java` and `fragment_add_recipe.xml`.
  - [x] UI for recipe name, ingredients (dynamic list), instructions, image upload.
  - [x] Implement `RecipeRepository.java` for Firestore operations related to recipes.
  - [x] Implement `RecipeViewModel.java` to manage recipe data and interactions.
  - [x] Logic for adding/removing ingredients dynamically in `AddRecipeFragment` (using a RecyclerView).
  - [x] Image picking from gallery (`ActivityResultLauncher`).
  - [x] Upload image to Firebase Storage (e.g., `/recipe_images/{userId}/{recipeId_or_imageId}`).
  - [x] Save recipe data (name, ingredients list, instructions, imageURL) to Firestore under `/users/{userId}/recipes/{recipeId}`.
- [x] **View Recipes:**
  - [x] Create `RecipeListFragment.java` and `fragment_recipe_list.xml`.
    - [x] Use `RecyclerView` with `RecipeAdapter.java` to display user's recipes fetched from Firestore.
  - [x] Create `RecipeDetailFragment.java` and `fragment_recipe_detail.xml`.
    - [x] Display full details of a selected recipe.
  - [x] Add navigation to these fragments.
- [x] **Delete Recipes:**
  - [x] Implement recipe deletion from `RecipeListFragment`.

## Phase 4: Meal Planning (PRD: US5)

- [x] **Meal Plan Creation/Viewing:**
  - [x] Create `MealPlanFragment.java` and `fragment_meal_plan.xml`.
  - [x] Implement UI to create/view a weekly meal plan (e.g., assign recipes to days).
  - [x] Implement `MealPlanRepository.java` and `MealPlanViewModel.java`.
  - [x] Store meal plan data in Firestore (e.g., `/users/{userId}/mealPlans/{planId}`).

## Phase 5: Grocery List Management (PRD: F3, US6, US7, US8, US9)

- [x] **Grocery List Generation & Display:**
  - [x] Create `GroceryListFragment.java` and `fragment_grocery_list.xml`.
  - [x] Implement `GroceryRepository.java` and `GroceryViewModel.java`.
  - [x] **REFACTORED:** Changed from meal plan-based temporary lists to persistent accumulative grocery list approach.
    - [x] Persistent grocery list with ID "main_list" that accumulates ingredients from multiple recipes.
    - [x] Smart ingredient consolidation logic that combines duplicates and sums compatible quantities.
    - [x] Removed meal plan dependencies from `GroceryViewModel.java`.
  - [x] Display grocery list using `RecyclerView` with `GroceryItemAdapter.java`.
  - [x] Store grocery list in Firestore (e.g., `/users/{userId}/groceryLists/main_list`).
- [x] **Recipe Detail Integration:**
  - [x] Added FloatingActionButton to `RecipeDetailFragment` for "Add to Grocery List" functionality.
  - [x] Integrated `GroceryViewModel` into `RecipeDetailFragment` for ingredient addition.
  - [x] Implemented automatic ingredient consolidation when adding from recipes.
  - [x] Added user feedback via Snackbar for successful/failed operations.
- [x] **Navigation Updates:**
  - [x] Removed meal plan to grocery list navigation action.
  - [x] Added direct navigation from Home to Grocery List.
  - [x] Updated meal plan fragment to remove grocery list generation FAB.
  - [x] Made recipe cards in meal plan clickable for navigation to recipe details.
- [x] **Manage Grocery Items (PRD: F3):**
  - [x] **F3.1 Delete Items:** Implement item deletion (e.g., swipe-to-delete or button).
  - [ ] **F3.2 Edit Items:** Implement item editing (e.g., name, quantity, notes via DialogFragment).
  - [x] **F3.3 Mark Items as Purchased:** Implement checkbox or toggle for purchase status. Update Firestore accordingly.
  - [x] **F3.4 Immediate UI Updates:** Fixed count updates to refresh immediately when items are checked/unchecked.
- [x] **UI Consistency:**
  - [x] Applied consistent custom action bar styling across fragments matching the purple theme.
  - [x] Replaced AppBarLayout with custom RelativeLayout headers for consistent design.
  - [x] Updated click handlers from toolbar to buttonBack navigation.

## Phase 6: Item Delegation (SMS) (PRD: F5, US10)

- [x] **SMS Delegation in `GroceryListFragment`:**
  - [x] Add a button/menu option to "Send List via SMS".
    - [x] Created `ic_share_24.xml` drawable for the share icon.
    - [x] Added share button to the custom header in `fragment_grocery_list.xml`.
  - [x] Format the grocery list content into a string.
    - [x] Implemented clean formatting: `[ ] Item Name (Quantity Unit)` for unchecked items.
    - [x] Used `[x]` for purchased items to show completion status.
    - [x] Added professional signature line "Sent from MealMate".
  - [x] Implement sending SMS using `Intent.ACTION_SENDTO` (recommended) approach.
    - [x] No permissions needed - uses implicit intent to launch user's default SMS app.
    - [x] Pre-populates message body with formatted grocery list.
    - [x] Added proper error handling for devices without SMS apps.

## Phase 7: Advanced Geotagging - In-App Store Discovery (REFACTORED)

**OBSOLETE TASKS (Old approach):**
~~- Data Model creation for manual store entry~~
~~- Store Repository and ViewModel for user-saved stores~~
~~- Manual pin dropping and store saving functionality~~

**NEW IMPLEMENTATION - Advanced Store Discovery:**

- [x] **Dependencies and Setup:**
  - [x] Add Google Maps SDK dependency (`play-services-maps:18.2.0`)
  - [x] Add Google Places SDK dependency (`places:3.4.0`)
  - [x] Add Location Services dependency (`play-services-location:21.2.0`)
  - [x] Add `ACCESS_FINE_LOCATION` permission to AndroidManifest.xml
  - [x] Add Google Maps API key placeholder to AndroidManifest.xml
- [x] **Full-Screen Map UI:**
  - [x] Create `fragment_map.xml` with custom header and SupportMapFragment
  - [x] Create `ic_refresh_24.xml` drawable for refresh functionality
  - [x] Implement loading states and user feedback
- [x] **MapFragment Implementation:**
  - [x] Create `MapFragment.java` implementing OnMapReadyCallback
  - [x] Initialize Places SDK and location services
  - [x] Handle location permissions with ActivityResultLauncher
  - [x] Integrate with FusedLocationProviderClient for user location
  - [x] Implement Google Places API integration for store discovery
  - [x] Filter results for grocery stores and supermarkets only
  - [x] Display stores as map markers with names and addresses
  - [x] Add refresh functionality to reload nearby stores
- [x] **Navigation Integration:**
  - [x] Add MapFragment destination to `mobile_navigation.xml`
  - [x] Create navigation action from HomeFragment to MapFragment
  - [x] Update HomeFragment Store Locations card click handler
- [x] **Advanced Features:**
  - [x] Real-time store discovery using Google Places API
  - [x] Automatic filtering for grocery/supermarket types
  - [x] User location centering with appropriate zoom level
  - [x] Comprehensive error handling and user feedback
  - [x] Permission-based flow with graceful degradation
