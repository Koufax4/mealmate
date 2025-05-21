# MealMate - Development Tasks

## Phase 0: Project Setup & Initial Documentation (Partially Done)

- [x] Basic Android Project Setup
- [x] Firebase Integration (Auth, Firestore, Storage dependencies added)
- [x] Initial `LoginFragment` UI created (`fragment_login.xml`)
- [x] Basic Navigation Graph setup (`mobile_navigation.xml`)

## Phase 1: Core Authentication (PRD: F2, US1, US2)

- [ ] **Login Functionality:**
  - [ ] Implement `LoginFragment.java` logic:
    - [ ] Connect UI elements (EditTexts, Button, ProgressBar).
    - [ ] Create `AuthViewModel.java` (extends `androidx.lifecycle.ViewModel`).
    - [ ] Create `AuthRepository.java` to handle Firebase Auth calls.
    - [ ] Implement email/password login using `FirebaseAuth.signInWithEmailAndPassword()`.
    - [ ] Use `LiveData` in `AuthViewModel` to communicate login state (loading, success, error) to `LoginFragment`.
    - [ ] Navigate to `navigation_home` on successful login.
    - [ ] Display error messages (e.g., Toast, Snackbar) on failure.
  - [ ] Handle "Forgot Password?" navigation/placeholder.
- [ ] **Registration Functionality:**
  - [ ] Create `RegisterFragment.java` and `fragment_register.xml`.
    - [ ] UI for email, password, confirm password.
  - [ ] Update `mobile_navigation.xml`:
    - [ ] Change `navigation_register` to point to `com.example.mealmate.ui.auth.RegisterFragment`.
    - [ ] Add action from `LoginFragment` to `navigation_register`.
    - [ ] Add action from `RegisterFragment` back to `navigation_login` or to `navigation_home` upon successful registration.
  - [ ] Implement `RegisterFragment.java` logic:
    - [ ] Connect UI elements.
    - [ ] Reuse or extend `AuthViewModel` and `AuthRepository`.
    - [ ] Implement email/password registration using `FirebaseAuth.createUserWithEmailAndPassword()`.
    - [ ] Store basic user profile info in Firestore (`/users/{userId}/profileInfo`) upon successful registration (e.g., email, creation date).
    - [ ] Navigate appropriately after registration.
- [ ] **Auth State Management:**
  - [ ] In `MainActivity` or a `SplashFragment` (if created), check current `FirebaseAuth.getCurrentUser()`.
    - [ ] If user is logged in, navigate to `navigation_home`.
    - [ ] If not, navigate to `navigation_login`.
  - [ ] Implement Logout functionality (e.g., in a settings menu or profile screen later).
- [ ] **UI/UX Refinements for Auth:**
  - [ ] Ensure `BottomNavigationView` in `MainActivity` is hidden during Login/Register flow.
  - [ ] Add input validation for email and password fields.

## Phase 2: Core App Structure & Home Screen (PRD: F1)

- [ ] **Home Screen (`HomeFragment.java`):**
  - [ ] Define layout `fragment_home.xml` for the main landing page after login.
  - [ ] Potentially display quick links or summaries (e.g., "Create Recipe", "View Meal Plan", "View Grocery List"). This will evolve as other features are built.
  - [ ] Ensure `BottomNavigationView` is visible and functional for Home, Dashboard, Notifications.
- [ ] **Data Models (POJOs - as per `doc/architecture.md` & `doc/technical.md`):**
  - [ ] Create `com.example.mealmate.data.model.User.java`.
  - [ ] Create `com.example.mealmate.data.model.Recipe.java`.
  - [ ] Create `com.example.mealmate.data.model.Ingredient.java`.
  - [ ] Create `com.example.mealmate.data.model.GroceryItem.java`.
  - [ ] Create `com.example.mealmate.data.model.MealPlan.java`.

## Phase 3: Recipe Management (PRD: F4, US4)

- [ ] **Recipe Creation (`AddRecipeFragment`):**
  - [ ] Create `AddRecipeFragment.java` and `fragment_add_recipe.xml`.
  - [ ] UI for recipe name, ingredients (dynamic list), instructions, image upload.
  - [ ] Implement `RecipeRepository.java` for Firestore operations related to recipes.
  - [ ] Implement `RecipeViewModel.java` to manage recipe data and interactions.
  - [ ] Logic for adding/removing ingredients dynamically in `AddRecipeFragment` (e.g., using a RecyclerView).
  - [ ] Image picking from gallery (`ActivityResultLauncher`).
  - [ ] Upload image to Firebase Storage (e.g., `/recipe_images/{userId}/{recipeId_or_imageId}`).
  - [ ] Save recipe data (name, ingredients list, instructions, imageURL) to Firestore under `/users/{userId}/recipes/{recipeId}`.
- [ ] **View Recipes:**
  - [ ] Create `RecipeListFragment.java` and `fragment_recipe_list.xml`.
    - [ ] Use `RecyclerView` with `RecipeAdapter.java` to display user's recipes fetched from Firestore.
  - [ ] Create `RecipeDetailFragment.java` and `fragment_recipe_detail.xml`.
    - [ ] Display full details of a selected recipe.
  - [ ] Add navigation to these fragments.

## Phase 4: Meal Planning (PRD: US5)

- [ ] **Meal Plan Creation/Viewing:**
  - [ ] Create `MealPlanFragment.java` and `fragment_meal_plan.xml`.
  - [ ] Implement UI to create/view a weekly meal plan (e.g., assign recipes to days).
  - [ ] Implement `MealPlanRepository.java` and `MealPlanViewModel.java`.
  - [ ] Store meal plan data in Firestore (e.g., `/users/{userId}/mealPlans/{planId}`).

## Phase 5: Grocery List Management (PRD: F3, US6, US7, US8, US9)

- [ ] **Grocery List Generation & Display:**
  - [ ] Create `GroceryListFragment.java` and `fragment_grocery_list.xml`.
  - [ ] Implement `GroceryRepository.java` and `GroceryViewModel.java`.
  - [ ] Logic to generate a consolidated grocery list from a selected `MealPlan`.
    - [ ] Aggregate ingredients, handle quantities.
  - [ ] Display grocery list using `RecyclerView` with `GroceryItemAdapter.java`.
  - [ ] Store grocery list in Firestore (e.g., `/users/{userId}/groceryLists/{listId}`).
- [ ] **Manage Grocery Items (PRD: F3):**
  - [ ] **F3.1 Delete Items:** Implement item deletion (e.g., swipe-to-delete or button).
  - [ ] **F3.2 Edit Items:** Implement item editing (e.g., name, quantity, notes via DialogFragment).
  - [ ] **F3.3 Mark Items as Purchased:** Implement checkbox or toggle for purchase status. Update Firestore accordingly.

## Phase 6: Item Delegation (SMS) (PRD: F5, US10)

- [ ] **SMS Delegation in `GroceryListFragment`:**
  - [ ] Add a button/menu option to "Send List via SMS".
  - [ ] Format the grocery list content into a string.
  - [ ] Implement sending SMS using `Intent.ACTION_SENDTO` (recommended) or `SmsManager`.
  - [ ] Handle `SEND_SMS` permission if using `SmsManager` (request at runtime).

## Phase 7: Optional Feature - Geotagging a Store (PRD: OF1.1, OF1.2, OF1.3)

- [ ] **Data Model:**
  - [ ] Create `com.example.mealmate.data.model.StoreLocation.java`.
- [ ] **Store Management:**
  - [ ] Create `StoreRepository.java` and `StoreViewModel.java`.
  - [ ] Create `AddStoreFragment.java` and `fragment_add_store.xml`.
    - [ ] UI for store name.
    - [ ] Button to "Pin on Map" or "Use Current Location".
  - [ ] Save store data (name, lat, lng) to Firestore (e.g., `/users/{userId}/favoriteStores/{storeId}`).
- [ ] **Map Integration:**
  - [ ] Add Google Maps SDK dependency and API key setup (as per `doc/technical.md`).
  - [ ] Create `MapFragment.java` and `fragment_map.xml`.
    - [ ] Integrate Google Map view.
    - [ ] Logic to get current location (`FusedLocationProviderClient`).
    - [ ] Allow user to drop a pin or select current location.
    - [ ] Display saved stores as markers.
  - [ ] Handle `ACCESS_FINE_LOCATION` permission (request at runtime).
- [ ] **Link Stores to Lists/Items (PRD: OF1.2 - Optional part):**
  - [ ] (Optional) Extend grocery list/item model to include `storeId`.
  - [ ] (Optional) UI to associate lists/items with a saved store.

## Phase 8: Testing & Polish

- [ ] **Unit Tests:**
  - [ ] Write unit tests for ViewModels (logic, LiveData updates).
  - [ ] Write unit tests for Repositories (mocking Firebase interactions if complex).
- [ ] **UI Tests (Espresso):**
  - [ ] Basic UI tests for login flow.
  - [ ] Tests for recipe creation.
  - [ ] Tests for grocery list generation and item management.
- [ ] **UI/UX Polish:**
  - [ ] Ensure consistent Material Design.
  - [ ] Add loading indicators for all async operations.
  - [ ] Improve error handling and user feedback.
  - [ ] Add Glide dependency and integrate for all image loading.
- [ ] **Documentation:**
  - [ ] Update all `doc/*.md` files as features are completed or designs change.
  - [ ] Ensure Javadoc for public APIs in classes.
  - [ ] Populate `fixes/` directory if complex bugs are encountered and solved.

## Backlog / Future Considerations

- [ ] `DashboardFragment` and `NotificationsFragment` actual implementation (currently placeholders).
- [ ] Advanced search/filter for recipes.
- [ ] More sophisticated meal planning UI (drag & drop, calendar view).
- [ ] User profile screen with edit capabilities.
