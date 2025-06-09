# SMS Sharing Implementation - Phase 6

## Overview

Phase 6 implements Item Delegation (SMS) functionality, allowing users to share their grocery list via SMS without requiring special permissions.

## Implementation Details

### UI Components Added

1. **Share Icon (`ic_share_24.xml`)**

   - Material Design share icon in vector format
   - White tint to match the purple theme

2. **Share Button (`fragment_grocery_list.xml`)**
   - Added to the custom header RelativeLayout
   - Positioned at the end (right side) of the header
   - Uses the purple theme consistent with other UI elements
   - 48dp x 48dp touch target for accessibility

### Functionality

#### Share Logic (`shareGroceryList()` method)

1. **Empty List Check**: Displays "Your list is empty!" toast if no items exist
2. **Message Formatting**: Creates a clean, readable format:
   - Header: "My Grocery List:"
   - Each item: `[x] Item Name (Quantity Unit)` for purchased items
   - Each item: `[ ] Item Name (Quantity Unit)` for unpurchased items
   - Footer: "Sent from MealMate"

#### Data Type Handling

- **Quantity Formatting**: Handles `double` quantities elegantly
  - Whole numbers display without decimals (e.g., "2" instead of "2.0")
  - Decimal quantities display with precision (e.g., "1.5")
  - Zero quantities are omitted from the message

#### SMS Intent Implementation

- **Permission-Free Approach**: Uses `Intent.ACTION_SENDTO` with `smsto:` URI
- **User Control**: Opens the user's default SMS app with pre-filled content
- **Error Handling**: Shows "No SMS app found" toast if no SMS app is available

### Example Output Format

```
My Grocery List:

[ ] Milk (2 gallons)
[x] Bread (1 loaf)
[ ] Apples (3 lbs)
[ ] Chicken Breast (2.5 lbs)

Sent from MealMate
```

## Benefits

- **No Permissions Required**: Users don't need to grant SMS permissions
- **User Privacy**: Users control recipient and can edit message before sending
- **Clean Format**: Professional, readable grocery list format
- **Error Resilient**: Handles edge cases like empty lists and missing apps
- **Consistent UI**: Matches the app's purple theme and design patterns

## Technical Considerations

- Compatible with all Android devices that have SMS capabilities
- Graceful degradation on devices without SMS apps
- Efficient string building for large grocery lists
- Proper handling of null/empty quantities and units
