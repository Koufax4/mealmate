*Technical Documentation - MealMate*

**1. Project Structure (Example Packages)**
```
com.example.mealmate/
├── ui/
│   ├── activities/             // MainActivity
│   ├── fragments/              // HomeFragment, LoginFragment, RegisterFragment, RecipeListFragment, RecipeDetailFragment, AddRecipeFragment, GroceryListFragment, MapFragment (for geotagging), etc.
│   ├── adapters/               // RecipeAdapter, IngredientAdapter, GroceryItemAdapter
│   └── viewholders/
├── viewmodels/                 // AuthViewModel, RecipeViewModel, GroceryListViewModel, MealPlanViewModel, StoreViewModel
├── data/
│   ├── model/                  // User.java, Recipe.java, Ingredient.java, GroceryItem.java, MealPlan.java, StoreLocation.java (POJOs)
│   ├── repository/             // AuthRepository.java, RecipeRepository.java, GroceryRepository.java, StoreRepository.java
│   └── remote/                 // Firebase wrappers or direct calls if simple
├── utils/                      // Helper classes, Constants.java, PermissionUtils.java
└── di/                         // (If using Hilt/Dagger - for dependency injection)
```

**2. Firebase Setup & Configuration**
*   **Project Creation:** Create a Firebase project in the Firebase console.
*   **App Registration:** Register the Android app (package name, SHA-1 certificate).
*   **Configuration File:** Add `google-services.json` to the `app/` directory.
*   **SDK Integration:** Add Firebase BoM and necessary dependencies (Auth, Firestore, Storage) to `build.gradle` files.
*   **Firebase Rules (Firestore - Initial Draft):**
    ```
    rules_version = '2';
    service cloud.firestore {
      match /databases/{database}/documents {
        // Users can only read/write their own data
        match /users/{userId}/{document=**} {
          allow read, write: if request.auth != null && request.auth.uid == userId;
        }
        // Potentially a public recipes collection for samples (read-only for users)
        match /publicRecipes/{recipeId} {
          allow read: if true;
          allow write: if false; // Or admin only
        }
      }
    }
    ```
*   **Firebase Rules (Storage - Initial Draft):**
    ```
    rules_version = '2';
    service firebase.storage {
      match /b/{bucket}/o {
        // Users can only upload to their own path, and only if authenticated
        // Images should be publicly readable if URLs are shared/used in app
        match /recipe_images/{userId}/{imageId} {
          allow read: if true; // Or if request.auth != null for private images
          allow write: if request.auth != null && request.auth.uid == userId;
        }
      }
    }
    ```

**3. Core Feature Implementation Details (High-Level)**

*   **F1: Home Screen (`HomeFragment.java`, `fragment_home.xml`)**
    *   Likely uses a `BottomNavigationView` or `NavigationDrawer` for navigation.
    *   Displays summaries or quick access points (e.g., "Today's Meals," "Current Grocery List").
*   **F2: User Registration & Login (`RegisterFragment.java`, `LoginFragment.java`, `AuthViewModel.java`, `AuthRepository.java`)**
    *   `AuthViewModel` uses `AuthRepository` which calls `FirebaseAuth.createUserWithEmailAndPassword()` and `FirebaseAuth.signInWithEmailAndPassword()`.
    *   `LiveData` in `AuthViewModel` to report success/failure/loading state to Fragments.
    *   Store basic user profile info in Firestore under `/users/{userId}/profileInfo`.
*   **F3: Manage My Items (Grocery List - `GroceryListFragment.java`, `GroceryItemAdapter.java`, `GroceryViewModel.java`, `GroceryRepository.java`)**
    *   `RecyclerView` with `GroceryItemAdapter`.
    *   `GroceryViewModel` fetches list from `GroceryRepository` (which reads from Firestore).
    *   **Delete:** Swipe-to-delete (using `ItemTouchHelper`) or long-press context menu. ViewModel calls Repository to update Firestore.
    *   **Edit:** DialogFragment for editing item details. ViewModel calls Repository.
    *   **Mark Purchased:** CheckBox in list item. OnStateChange updates ViewModel, which updates Repository/Firestore.
*   **F4: Create a Meal (Recipe - `AddRecipeFragment.java`, `RecipeViewModel.java`, `RecipeRepository.java`)**
    *   Input fields for name, instructions.
    *   `RecyclerView` (or dynamic `LinearLayout`) for adding/editing ingredients. Each ingredient is a small object/map.
    *   Image picking using `ActivityResultLauncher` for `ACTION_GET_CONTENT`. Upload to Firebase Storage, save URL in Firestore recipe document.
    *   `RecipeViewModel` manages recipe object creation and saving via `RecipeRepository`.
*   **F5: Item Delegation (SMS - `GroceryListFragment.java` or a dedicated delegation screen)**
    *   User selects a contact (e.g., using `Intent.ACTION_PICK` for contacts or manual phone number input).
    *   Format grocery list data (items, quantities, categories, notes) into a String.
    *   **Option 1 (Direct Send):**
        *   Use `SmsManager.getDefault().sendTextMessage(phoneNumber, null, message, null, null);`
        *   Requires `android.permission.SEND_SMS`. Request at runtime.
    *   **Option 2 (Intent to SMS App - Recommended for better UX & no direct send permission):**
        ```
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber)); // This ensures only SMS apps respond
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Handle case where no SMS app is available
            Toast.makeText(this, "No SMS app found.", Toast.LENGTH_SHORT).show();
        }
        ```

**4. Optional Desirable Feature Implementation (Geotagging a Store)**
*   **UI (`AddStoreFragment.java`, `MapFragment.java`, `StoreViewModel.java`, `StoreRepository.java`)**
    *   `AddStoreFragment`: Input for store name. Button to "Pin on Map" or "Use Current Location."
    *   `MapFragment`: Integrates Google Maps SDK.
        *   Displays a map.
        *   Allows user to long-press to drop a pin or shows current location.
        *   On pin confirmation, extracts latitude/longitude.
*   **Logic:**
    *   `StoreViewModel` handles:
        *   Fetching current location using `FusedLocationProviderClient` (requires `ACCESS_FINE_LOCATION` permission).
        *   Saving store data (name, lat, lng) to Firestore via `StoreRepository`.
        *   Fetching saved stores to display markers on `MapFragment`.
*   **Permissions:** `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` in `AndroidManifest.xml`. Request at runtime.
*   **Google Maps API Key:**
    *   Obtain from Google Cloud Console.
    *   Add to `AndroidManifest.xml` and restrict it to your app's package name and SHA-1.
    *   Include `play-services-maps` dependency.

**5. External APIs & Libraries**
*   **Firebase Suite:** As detailed.
*   **Google Play Services Location:** For geotagging.
*   **Google Maps SDK:** For map display (geotagging).
*   **Glide:** For image loading.

**6. Error Handling Strategy**
*   Use `try-catch` blocks for operations that might throw exceptions (e.g., I/O, network).
*   Firebase Task Listeners: `addOnSuccessListener`, `addOnFailureListener` for all Firebase operations.
*   User Feedback:
    *   `Toast` messages for minor notifications or errors.
    *   `Snackbar` for actionable feedback or more prominent errors.
    *   Loading indicators (e.g., `ProgressBar`) during asynchronous operations.
    *   Clear error messages in UI forms (e.g., `setError()` on `TextInputEditText`).

**7. Permissions Handling**
*   Declare permissions in `AndroidManifest.xml`:
    *   `<uses-permission android:name="android.permission.INTERNET" />` (for Firebase, Glide)
    *   `<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />` (to check connectivity)
    *   `<uses-permission android:name="android.permission.SEND_SMS" />` (if using direct SMS sending)
    *   `<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />` (for geotagging)
    *   `<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />` (for geotagging)
    *   `<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />` (for picking images, if targeting older APIs or not using `ACTION_GET_CONTENT` effectively)
*   Runtime Permissions: For `SEND_SMS`, `ACCESS_FINE_LOCATION`, `READ_EXTERNAL_STORAGE` (if needed).
    *   Check for permission using `ContextCompat.checkSelfPermission()`.
    *   Request permission using `ActivityCompat.requestPermissions()`.
    *   Handle the result in `onRequestPermissionsResult()`.
    *   Provide rationale if permission was denied previously using `shouldShowRequestPermissionRationale()`.

**8. Material Design Implementation**
*   Use `Theme.MaterialComponents.*` as the base app theme.
*   Utilize components from the Material Components library (Buttons, TextFields, Cards, BottomNavigationView, FABs).
*   Follow Material Design guidelines for color palettes, typography, spacing, and elevation.
*   Implement meaningful motion and transitions where appropriate (e.g., shared element transitions, ripple effects).