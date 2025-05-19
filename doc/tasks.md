*Project Task Management: MealMate (Mandatory Features)*

**Legend:**
*   **ID:** Unique Task Identifier
*   **Task:** Description of the task.
*   **Depends On:** Task IDs that need to be completed before this one can start.
*   **Status:** To Do / In Progress / Done
*   **Assigned To:** (Your Name/UserID)
*   **Notes:** Any relevant details or observations.

---

**I. Core Requirements - Task Breakdown**

| ID    | Task                                                                 | Depends On | Status | Assigned To | Notes                                                                                                |
| :---- | :------------------------------------------------------------------- | :--------- | :----- | :---------- | :--------------------------------------------------------------------------------------------------- |
| **1. Project & Firebase Setup**                                                                                       |            |        |             |                                                                                                      |
| 1.1   | Create Firebase Project                                              | -          | Done   |             | In Firebase Console.                                                                                 |
| 1.2   | Add Firebase SDKs (Auth, Firestore, Storage) to Android Project      | 1.1        | Done   |             | Update `build.gradle.kts` (app & project), `libs.versions.toml`, add `google-services.json`.         |
| 1.3   | Configure Firebase Security Rules (Basic - user-only access)         | 1.2        | Done   |             | For Firestore & Storage.                                                                             |
| **2. User Authentication**                                                                                            |            |        |             |                                                                                                      |
| 2.1   | Design Login Screen UI (`fragment_login.xml`)                        | -          | To Do  |             | Email, Password fields, Login button, "Register" link.                                               |
| 2.2   | Implement Login Logic (`LoginFragment.java`, `AuthViewModel.java`)   | 1.3, 2.1   | To Do  |             | Firebase `signInWithEmailAndPassword`. Navigate to Home on success. Show errors.                     |
| 2.3   | Design Registration Screen UI (`fragment_register.xml`)                | -          | To Do  |             | Email, Password, Confirm Password fields, Register button.                                           |
| 2.4   | Implement Registration Logic (`RegisterFragment.java`, `AuthViewModel.java`) | 1.3, 2.3   | To Do  |             | Firebase `createUserWithEmailAndPassword`. Navigate to Home/Login on success. Show errors.         |
| 2.5   | Handle User Sessions (Check if user is already logged in on app start) | 2.2, 2.4   | To Do  |             | In `MainActivity` or a `SplashFragment`, navigate to Home or Login.                                |
| 2.6   | Implement Logout Functionality                                       | 2.5        | To Do  |             | Typically in an options menu or settings screen. Call `FirebaseAuth.signOut()`.                      |
| **3. Home Screen**                                                                                                    |            |        |             |                                                                                                      |
| 3.1   | Define Home Screen Layout (`fragment_home.xml`)                      | 2.5        | To Do  |             | This is the entry point after login. Plan navigation to other features (Recipes, Meal Plan, Grocery). |
| 3.2   | Setup Bottom Navigation for Core Features                            | 3.1        | To Do  |             | Update `bottom_nav_menu.xml`, `mobile_navigation.xml` for Recipes, Meal Plan, Grocery List etc.    |
| **4. Create a Meal (Recipe)**                                                                                         |            |        |             |                                                                                                      |
| 4.1   | Define Recipe Data Model (`Recipe.java` POJO)                        | -          | To Do  |             | Name, ingredients list (String or custom Ingredient object), instructions, imageURL (optional).         |
| 4.2   | Design "Add/Edit Recipe" Screen UI (`fragment_add_edit_recipe.xml`)    | 4.1        | To Do  |             | Fields for name, ingredients (dynamic list), instructions, image upload button.                      |
| 4.3   | Implement Logic to Add/Edit Ingredients Dynamically on UI            | 4.2        | To Do  |             | Add/remove ingredient input fields.                                                                  |
| 4.4   | Implement Image Picking from Gallery & Upload to Firebase Storage    | 1.3, 4.2   | To Do  |             | Get image URI, upload to Firebase Storage, get download URL. Handle permissions.                     |
| 4.5   | Implement Save Recipe Logic (`AddEditRecipeViewModel.java`, `RecipeRepository.java`) | 1.3, 4.1, 4.4 | To Do  |             | Save recipe data (including image URL) to Firestore under the current user.                          |
| 4.6   | Implement Display List of Saved Recipes (`RecipeListFragment.xml`, `RecipeAdapter.java`, `RecipeListViewModel.java`) | 4.5        | To Do  |             | Fetch recipes from Firestore for current user and display in a RecyclerView.                         |
| 4.7   | Implement View Recipe Details (`RecipeDetailFragment.xml`)           | 4.6        | To Do  |             | Show selected recipe's name, image, ingredients, instructions.                                       |
| 4.8   | Implement Edit Recipe Functionality                                  | 4.5, 4.7   | To Do  |             | Navigate to "Add/Edit Recipe" screen pre-filled with recipe data.                                    |
| 4.9   | Implement Delete Recipe Functionality                                | 4.5, 4.6   | To Do  |             | Delete recipe from Firestore and Firebase Storage (image). Confirm before delete.                      |
| **5. Manage My Items (Grocery List)**                                                                                 |            |        |             |                                                                                                      |
| 5.1   | Define Grocery Item Data Model (`GroceryItem.java` POJO)             | -          | To Do  |             | Name, quantity, unit, category (optional), isPurchased, notes (optional).                            |
| 5.2   | Design Grocery List Screen UI (`fragment_grocery_list.xml`)            | 5.1        | To Do  |             | RecyclerView to display items, "Add Item" button (optional manual add), "Delegate" button.         |
| 5.3   | Implement "Generate Grocery List" from Selected Meal Plan (Stub for now, will integrate with Meal Planner later) | 4.6, 5.1   | To Do  |             | For now, allow manual adding of grocery items or a simple way to create a list to test management. |
| 5.4   | Implement Add Grocery Item Logic (`GroceryListViewModel.java`, `GroceryRepository.java`) | 1.3, 5.1, 5.3 | To Do  |             | Save new grocery item to Firestore under the current user's list.                                  |
| 5.5   | Implement Display Grocery List (`GroceryListAdapter.java`)           | 5.4        | To Do  |             | Fetch items from Firestore and display in RecyclerView.                                              |
| 5.6   | Implement "Mark Item as Purchased" Logic                             | 5.5        | To Do  |             | Update `isPurchased` field in Firestore. UI should reflect change (e.g., strikethrough).          |
| 5.7   | Implement "Edit Item" Logic                                          | 5.5        | To Do  |             | Allow editing name, quantity, unit, notes. Update in Firestore.                                      |
| 5.8   | Implement "Delete Item" Logic                                        | 5.5        | To Do  |             | Remove item from Firestore.                                                                          |
| **6. Item Delegation (SMS)**                                                                                          |            |        |             |                                                                                                      |
| 6.1   | Add "Delegate List" Button to Grocery List Screen                  | 5.2        | To Do  |             |                                                                                                      |
| 6.2   | Implement UI to Select Contact/Enter Phone Number                    | 6.1        | To Do  |             | Could be a simple EditText or an Intent to pick from contacts (more complex).                        |
| 6.3   | Format Grocery List Data into a String for SMS                       | 5.5, 6.2   | To Do  |             | Iterate through grocery items and create a readable string.                                            |
| 6.4   | Implement SMS Sending Logic                                          | 6.3        | To Do  |             | Use `SmsManager` (requires permission) or `Intent.ACTION_SENDTO` (preferred for UX). Handle SMS permission. |
| **7. Meal Planning (Core link between Recipes & Grocery List)**                                                     |            |        |             |                                                                                                      |
| 7.1   | Design Meal Plan Screen UI (`fragment_meal_plan.xml`)                | 4.6        | To Do  |             | Allow user to select recipes for the week (e.g., using a RecyclerView of their recipes with checkboxes). |
| 7.2   | Implement Save Meal Plan Logic                                       | 7.1        | To Do  |             | Store selected recipe IDs/references for the week in Firestore.                                      |
| 7.3   | Implement "Generate Grocery List from Meal Plan" (Actual Implementation) | 4.1, 5.1, 7.2 | To Do  |             | Consolidate ingredients from all recipes in the current meal plan. Update/Create the grocery list in Firestore. |
| 7.4   | Link "Generate Grocery List" button/action to this logic           | 5.2, 7.3   | To Do  |             | Update the stub from 5.3.                                                                            |
| **8. Testing & Refinement (Iterative)**                                                                               |            |        |             |                                                                                                      |
| 8.1   | Unit Test Key ViewModel Logic (e.g., Auth, Recipe data handling)     | *Ongoing*  | To Do  |             |                                                                                                      |
| 8.2   | Instrumented Test for Core User Flows (Login, Add Recipe, View List) | *Ongoing*  | To Do  |             |                                                                                                      |
| 8.3   | Manual Testing and Bug Fixing for All Core Features                | *Ongoing*  | To Do  |             |                                                                                                      |