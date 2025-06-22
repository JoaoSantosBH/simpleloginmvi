# simpleloginmvi
["home"](/img/login.png)
This example demonstrates a complete MVI implementation for a login screen with the following key components:

## MVI Architecture Components:

1.  **LoginIntent** - Sealed class representing all user actions (email change, password change, login click, etc.)
2.  **LoginViewState** - Data class holding the complete UI state (form data, loading states, errors)
3.  **LoginViewModel** - Processes intents and updates state using StateFlow
4.  **LoginScreen** - Compose UI that observes state and sends intents

## Key Features:

-   **Form validation** with real-time error display
-   **Loading states** with disabled inputs during API calls
-   **Password visibility toggle**
-   **Error handling** for validation and network errors
-   **Reactive UI** that updates based on state changes
-   **Demo credentials** for testing ([demo@example.com](mailto:demo@example.com) / password123)

## Dependencies you'll need in your `build.gradle`:

kotlin

```kotlin
dependencies {
    implementation 'androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0'
    implementation 'androidx.compose.material3:material3:1.2.0'
    implementation 'androidx.compose.material:material-icons-extended:1.6.0'
}
```

The MVI pattern ensures unidirectional data flow: User interactions create Intents → ViewModel processes Intents → State updates → UI recomposes. This makes the app predictable and easy to test.