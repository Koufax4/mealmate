*Product Requirements Document (PRD) - MealMate*

**1. Introduction**
MealMate is an Android application designed to assist users in planning their weekly meals and generating corresponding grocery shopping lists. It aims to simplify meal preparation, improve healthy eating habits, and streamline the grocery shopping experience.

**2. Goals & Objectives**
*   To provide a user-friendly platform for creating, storing, and organizing recipes.
*   To enable users to plan meals for the week by selecting from their saved recipes or discovering new ones.
*   To automatically generate a consolidated grocery list based on selected meals.
*   To facilitate delegation of shopping tasks.
*   To help users track purchased items and manage their grocery lists efficiently.
*   To offer an intuitive and visually appealing user experience following Material Design principles.

**3. Target Audience**
*   Busy professionals (like "Emma") seeking to save time on meal planning and grocery shopping.
*   Health-conscious individuals aiming to plan nutritious meals.
*   Families looking to organize shared meal responsibilities.
*   Anyone wanting a more efficient way to manage recipes and shopping.

**4. User Stories & Scenarios**
*(Based on the provided "Typical User Interactions" and "Core Requirements")*

*   **US1 (Registration/Login):** As a new user, I want to register for an account using my email and password so I can securely access my meal plans and recipes.
*   **US2 (Login):** As a returning user, I want to log in with my credentials so I can access my saved data.
*   **US3 (Recipe Browsing - Basic):** As a user, I want to browse a selection of recipes (initially, these might be sample recipes or recipes I've added) so I can find inspiration for my meals.
*   **US4 (Recipe Creation):** As a user, I want to create a new recipe by inputting a name, ingredients (with quantities/units), preparation instructions, and optionally, a picture, so I can store my favorite meals.
*   **US5 (Meal Planning):** As a user, I want to select multiple recipes for my weekly meal plan so that the app knows what I intend to cook.
*   **US6 (Grocery List Generation):** As a user, when I create a meal plan, I want the app to automatically generate a grocery list consolidating all ingredients from the selected recipes, organized by category, so I know what to buy.
*   **US7 (Grocery Item Management - Mark Purchased):** As a user, I want to mark items as purchased on my grocery list so I can track what I still need to buy.
*   **US8 (Grocery Item Management - Edit):** As a user, I want to edit items on my grocery list (e.g., quantity, notes) so I can adjust them as needed.
*   **US9 (Grocery Item Management - Delete):** As a user, I want to delete items from my grocery list if I no longer need them or added them by mistake.
*   **US10 (Item Delegation - SMS):** As a user, I want to send my grocery list (items, optionally prices, store notes) as an SMS to another contact (e.g., Lily) so they can do the shopping for me.
*   **US11 (Recipe Discovery - Share to App - *If this becomes the chosen optional feature*):** As a user, I want to share a recipe link from a food blog directly to MealMate, and have the app attempt to pull in ingredients to add to my recipes and grocery list, so I can easily save new recipes I find online.
*   **US12 (Geotagging Stores - *If this becomes the chosen optional feature*):** As a user, I want to geotag my favorite grocery stores so I can associate items or lists with specific locations and potentially view them on a map.
*   **US13 (Gesture Controls - *If this becomes the chosen optional feature*):** As a user, I want to use swipe gestures (e.g., swipe left/right to delete/edit) on list items for quicker management.

**5. Core Features (Mandatory)**
*   **F1: Home Screen:** The main entry point after login, providing access to key app sections (e.g., My Recipes, Meal Planner, Grocery Lists).
*   **F2: User Registration & Login:** Secure account creation and authentication using Firebase Authentication (Email/Password).
*   **F3: Manage My Items (Grocery List Items):**
    *   **F3.1: Delete Items:** Remove items from the grocery list.
    *   **F3.2: Edit Items:** Modify details of an item on the grocery list (e.g., quantity, name, notes).
    *   **F3.3: Mark Items as Purchased:** Toggle the purchase status of grocery items.
*   **F4: Create a Meal (Recipe):**
    *   Input fields for recipe name, ingredients (name, quantity, unit), preparation instructions.
    *   Ability to upload/associate a picture with the recipe.
*   **F5: Item Delegation (SMS):**
    *   Functionality to select a grocery list and send its content as an SMS message to a chosen phone number/contact.

**6. Optional Desirable Feature (To be chosen and implemented - select ONE)**
*   **(Option A) Geotag a store or location:** So the grocery list can be viewed on a map, or items can be associated with stores.
*   **(Option B) Integration with other apps:** Allow products/recipes from other apps (e.g., via Android Share functionality) to be stored in MealMate.
*   **(Option C) Gesture Controls:** Swipe left/right on list items for actions like delete/edit; potentially shake gesture for an action (e.g., clear purchased items).

    *(**Decision Point:** You must state here which one you will attempt. For this document, let's assume you choose **Option A: Geotag a store or location** for now.)*
    **Chosen Optional Feature: Geotag a store or location**
    *   **OF1.1:** Allow users to save favorite grocery stores with a name and location (captured via map interface or current location).
    *   **OF1.2:** Optionally, allow users to associate specific items or entire grocery lists with a saved store.
    *   **OF1.3:** Display saved stores on a map.

**7. Non-Goals (for this prototype)**
*   Advanced recipe discovery (e.g., AI-powered suggestions, searching external recipe databases).
*   Real-time price tracking or integration with specific grocery store APIs for item availability (beyond user-inputted prices/notes).
*   Social sharing features beyond list delegation.
*   Detailed nutritional information analysis.
*   Offline-first complex synchronization beyond Firebase's default caching for simple read/write.

**8. Success Metrics (for the assignment)**
*   Successful implementation of all core requirements.
*   Successful implementation of the chosen optional desirable feature.
*   A functional, stable prototype that works on an Android phone/tablet.
*   Adherence to Material Design principles.
*   Clear and well-commented code.
*   Positive feedback from the demo.
