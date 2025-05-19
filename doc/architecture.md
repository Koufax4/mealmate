*Architecture Document - MealMate*

**1. Overview**
MealMate will be a native Android application developed using Java. It will employ the **Model-View-ViewModel (MVVM)** architectural pattern to promote a separation of concerns, testability, and maintainability. Firebase will serve as the backend for authentication, database (Cloud Firestore), and storage.

**2. Architectural Pattern: MVVM (Model-View-ViewModel)**
*   **Model:** Represents the data and business logic. This layer includes:
    *   Data objects (POJOs for User, Recipe, Ingredient, GroceryItem, MealPlan, StoreLocation).
    *   **Repositories:** Abstract data sources. They will handle data operations, deciding whether to fetch data from Firebase or a local cache (if implemented). They provide a clean API for ViewModels.
    *   Firebase SDK interactions (Firestore, Authentication, Storage).
*   **View:** Represents the UI (Activities and Fragments).
    *   Observes `LiveData` exposed by ViewModels to update the UI.
    *   Forwards user interactions to the ViewModel.
    *   Does not contain business logic.
*   **ViewModel:** Acts as a bridge between the View and the Model.
    *   Holds and prepares UI-related data for the View.
    *   Exposes data to the View via `LiveData`, making it observable and lifecycle-aware.
    *   Survives configuration changes (e.g., screen rotation).
    *   Calls methods in the Repository to perform data operations.

**Diagram:**
```
+-------------+       +-----------------+       +-------------------+       +---------------------+
|    View     |<----->|    ViewModel    |<----->|    Repository     |<----->|     Data Sources    |
| (Activity/  |       | (LiveData, Logic) |       | (Abstracts Data)  |       | (Firebase Firestore,|
|  Fragment)  |       +-----------------+       +-------------------+       |  Firebase Storage,  |
+-------------+                                                             |  Firebase Auth)     |
      ^                                                                     +---------------------+
      | User Interactions
      | UI Updates
```

**3. Data Flow Example (Creating a Recipe)**
1.  **View (AddRecipeFragment):** User inputs recipe details and clicks "Save."
2.  **View:** Calls `viewModel.saveRecipe(recipeData)` on its `AddRecipeViewModel`.
3.  **ViewModel (AddRecipeViewModel):** Validates input. Creates a `Recipe` object. Calls `recipeRepository.addRecipe(recipeObject)`. Updates a `LiveData<Boolean>` for saving status.
4.  **Repository (RecipeRepository):** Interacts with `FirebaseFirestore` to save the `recipeObject` to the "recipes" collection for the current user. Returns a `Task` or updates LiveData on completion/failure.
5.  **Firebase:** Stores the data.
6.  **(Feedback Loop):** The `LiveData` in the ViewModel is updated, which the View (Fragment) observes, allowing it to show a success/failure message or navigate away.

**4. Key Technologies & Libraries**
*   **Programming Language:** Java
*   **IDE:** Android Studio
*   **Build System:** Gradle
*   **Minimum SDK:** API 23 (Android 6.0 Marshmallow) - *Decide this based on desired reach vs features.*
*   **Android Jetpack:**
    *   **ViewModel:** For MVVM implementation.
    *   **LiveData:** For observable data.
    *   **Navigation Component:** For managing in-app navigation between fragments.
    *   **RecyclerView:** For displaying lists (recipes, ingredients, grocery items).
    *   **CardView:** For item presentation.
    *   **Material Components for Android:** For UI elements and theming.
    *   **Room Persistence Library:** (Optional, if local caching beyond Firebase's capabilities is deeply needed, but Firestore's offline support might be sufficient for prototype).
*   **Firebase:**
    *   **Firebase Authentication:** For user registration and login.
    *   **Cloud Firestore:** NoSQL database for storing user data, recipes, meal plans, grocery lists, store locations.
    *   **Firebase Storage:** For storing user-uploaded recipe images.
*   **Image Loading:** `Glide` - For efficient loading and caching of images.
*   **Optional Feature (Geotagging):**
    *   **Google Play Services Location APIs:** `FusedLocationProviderClient` for getting current location.
    *   **Google Maps SDK for Android:** For displaying maps and markers.

**5. Data Persistence Strategy**
*   **Primary Data Store:** Cloud Firestore.
    *   **Structure (Conceptual):**
        ```
        users/{userId}/
            - profileInfo (name, email)
            - recipes/{recipeId} (name, ingredients_array, instructions, imageUrl, sourceUrl)
            - mealPlans/{planId} (name, weekStartDate, recipeRefs_array)
            - groceryLists/{listId} (name, creationDate, items_array_of_maps [{name, qty, unit, category, purchased, storeNotes}])
            - favoriteStores/{storeId} (name, address, latitude, longitude)
        ```
    *   **Offline Support:** Firestore provides robust offline data persistence. Data written while offline will be synced when connectivity is restored. Reads can be served from cache.
*   **Image Storage:** Firebase Storage. Recipe images will be uploaded here, and their download URLs will be stored in the corresponding Firestore recipe document.

**6. Navigation**
*   Single-Activity architecture preferred, using the Navigation Component to manage Fragment transactions.
*   A `BottomNavigationView` or a `NavigationDrawer` will likely be used for top-level navigation from the Home screen.
