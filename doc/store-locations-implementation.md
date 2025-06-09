# Store Locations Implementation - Phase 7

## Overview

Phase 7 implements an advanced store discovery system that leverages Google Maps and Places APIs to provide users with a full-screen map experience for finding nearby grocery stores.

## Architecture

### User Flow
1. User taps "Store Locations" card on Home screen
2. App navigates to full-screen MapFragment
3. App requests location permissions
4. Upon permission grant, app centers map on user location
5. App uses Google Places API to find nearby grocery stores
6. Stores are displayed as markers on the map
7. User can tap markers to see store names and addresses
8. User can refresh to reload nearby stores

### Technical Implementation

#### Dependencies Added
```kotlin
// Maps and Location Services
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.android.gms:play-services-location:21.2.0")
implementation("com.google.android.libraries.places:places:3.4.0")
```

#### Permissions
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

#### API Key Configuration
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="YOUR_API_KEY" />
```

## File Structure

### New Files Created
1. **`app/src/main/res/layout/fragment_map.xml`**
   - Full-screen map layout with custom header
   - Loading states with progress indicator
   - SupportMapFragment integration

2. **`app/src/main/java/com/example/mealmate/ui/map/MapFragment.java`**
   - Main map functionality implementation
   - Location permission handling
   - Google Places API integration
   - Map marker management

3. **`app/src/main/res/drawable/ic_refresh_24.xml`**
   - Refresh icon for store reload functionality

### Modified Files
1. **`app/build.gradle.kts`**
   - Added Google Play Services dependencies

2. **`app/src/main/AndroidManifest.xml`**
   - Added location permission
   - Added Google Maps API key metadata

3. **`app/src/main/res/navigation/mobile_navigation.xml`**
   - Added MapFragment destination
   - Added navigation action from Home to Map

4. **`app/src/main/java/com/example/mealmate/ui/home/HomeFragment.java`**
   - Updated Store Locations card click handler

## Key Features

### 1. Permission Management
- Uses `ActivityResultLauncher` for modern permission handling
- Graceful degradation when permission is denied
- Clear user messaging about permission requirements

### 2. Location Services
- Integration with `FusedLocationProviderClient`
- Automatic map centering on user location
- Error handling for location service failures

### 3. Store Discovery
- Real-time discovery using Google Places API
- Filtering for grocery stores and supermarkets only
- Smart result processing and marker creation

### 4. Map Functionality
- Custom UI with consistent purple theme
- Zoom controls and my location button
- Marker clustering for better performance
- Refresh capability for updated results

### 5. Error Handling
- Comprehensive error messages for users
- Graceful handling of API failures
- Loading states and user feedback

## Technical Details

### Places API Integration
```java
// Define fields to retrieve
List<Place.Field> placeFields = Arrays.asList(
    Place.Field.NAME,
    Place.Field.LAT_LNG,
    Place.Field.TYPES,
    Place.Field.ADDRESS
);

// Filter for grocery stores
if (place.getTypes() != null && 
    (place.getTypes().contains(Place.Type.GROCERY_OR_SUPERMARKET) ||
     place.getTypes().contains(Place.Type.SUPERMARKET))) {
    // Add marker to map
}
```

### Permission Flow
```java
private ActivityResultLauncher<String> requestPermissionLauncher =
    registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
        if (isGranted) {
            getUserLocationAndFindStores();
        } else {
            // Show permission denied message
        }
    });
```

## Setup Instructions

### 1. Google Cloud Console Setup
1. Create a new project or select existing project
2. Enable Google Maps SDK for Android
3. Enable Places API
4. Create API credentials (API Key)
5. Restrict API key to your package name

### 2. API Key Configuration
Replace `YOUR_API_KEY` in `AndroidManifest.xml` with your actual API key:
```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="your_actual_api_key_here" />
```

### 3. Security Considerations
- **IMPORTANT**: Do not commit API keys to version control
- Use environment variables or secure storage for production
- Restrict API key usage to your package name
- Monitor API usage and set quotas

## Navigation Flow

```
HomeFragment
    ↓ (Store Locations card tap)
MapFragment
    ↓ (Permission request)
Location Services
    ↓ (Location acquired)
Google Places API
    ↓ (Store results)
Map Markers Display
```

## Error Scenarios

1. **Permission Denied**: User sees message explaining need for location access
2. **Location Unavailable**: User prompted to enable location services
3. **Places API Error**: User sees error message with option to retry
4. **No Stores Found**: User informed that no grocery stores were found nearby

## Performance Considerations

- Places API results are cached during the session
- Map markers are efficiently managed
- Loading states prevent multiple simultaneous requests
- Error states prevent unnecessary API calls

## Future Enhancements

1. **Store Filtering**: Add filters for store types or chains
2. **Store Details**: Detailed store information and reviews
3. **Navigation Integration**: Direct navigation to selected stores
4. **Favorites**: Save favorite stores for quick access
5. **Store Hours**: Display current open/closed status

## Testing

### Unit Testing Areas
- Permission handling logic
- Places API response processing
- Error handling scenarios
- Location service integration

### Integration Testing Areas
- End-to-end navigation flow
- API integration with real responses
- Permission flow testing
- Map interaction testing

## Troubleshooting

### Common Issues
1. **Blank Map**: Check API key configuration and restrictions
2. **No Stores Found**: Verify Places API is enabled and location permissions granted
3. **Permission Loops**: Ensure proper permission handling implementation
4. **Build Errors**: Verify all dependencies are properly added

### Debug Tips
- Check Logcat for API error messages
- Verify location permissions in device settings
- Test with different locations for store availability
- Monitor API quota usage in Google Cloud Console 