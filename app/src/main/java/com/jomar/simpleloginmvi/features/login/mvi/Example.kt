

// ===========================================
// ESTRUTURA DO PROJETO
// ===========================================

/*
MyApp/
├── app/
├── core/
│   ├── data/
│   ├── domain/
│   ├── network/
│   ├── ui/
│   └── navigation/
├── features/
│   ├── login/
│   └── home/
└── buildSrc/
*/
// ===========================================
// 5. CORE - NETWORK MODULE
// ===========================================

// core/network/build.gradle

// core/network/src/main/java/com/myapp/core/network/ApiService.kt

interface ApiService {
    suspend fun login(email: String, password: String): LoginResponse
    suspend fun getUserData(): UserDataResponse
}

data class LoginResponse(
    val token: String,
    val user: UserData
)

data class UserDataResponse(
    val user: UserData
)

data class UserData(
    val id: String,
    val name: String,
    val email: String
)

// core/network/src/main/java/com/myapp/core/network/RetrofitApiService.kt


interface RetrofitApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("user/me")
    suspend fun getUserData(): UserDataResponse
}

data class LoginRequest(
    val email: String,
    val password: String
)

// core/network/src/main/java/com/myapp/core/network/KtorApiService.kt



// core/network/src/main/java/com/myapp/core/network/di/NetworkModule.kt


val networkModule = module {

    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl("https://api.myapp.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(RetrofitApiService::class.java) }

    // Ktor
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    single<ApiService> { KtorApiService(get()) }
}

// ===========================================
// 6. CORE - DATA MODULE
// ===========================================

// core/data/src/main/java/com/myapp/core/data/repository/UserRepository.kt


interface UserRepository {
    suspend fun login(email: String, password: String): Result<UserData>
    suspend fun getUserData(): Result<UserData>
    suspend fun saveUserToken(token: String)
    suspend fun getUserToken(): String?
}

class UserRepositoryImpl(
    private val apiService: ApiService,
    private val localDataSource: LocalDataSource
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<UserData> {
        return try {
            val response = apiService.login(email, password)
            saveUserToken(response.token)
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getUserData(): Result<UserData> {
        return try {
            val response = apiService.getUserData()
            Result.success(response.user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveUserToken(token: String) {
        localDataSource.saveToken(token)
    }

    override suspend fun getUserToken(): String? {
        return localDataSource.getToken()
    }
}

// core/data/src/main/java/com/myapp/core/data/local/LocalDataSource.kt


interface LocalDataSource {
    suspend fun saveToken(token: String)
    suspend fun getToken(): String?
}

class LocalDataSourceImpl(
    private val sharedPreferences: SharedPreferences
) : LocalDataSource {

    override suspend fun saveToken(token: String) {
        sharedPreferences.edit().putString("user_token", token).apply()
    }

    override suspend fun getToken(): String? {
        return sharedPreferences.getString("user_token", null)
    }
}

// core/data/src/main/java/com/myapp/core/data/di/DataModule.kt


val dataModule = module {
    single {
        androidContext().getSharedPreferences("myapp_prefs", Context.MODE_PRIVATE)
    }

    single<LocalDataSource> { LocalDataSourceImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get(), get()) }
}

// ===========================================
// 7. CORE - DOMAIN MODULE
// ===========================================

// core/domain/src/main/java/com/myapp/core/domain/usecase/LoginUseCase.kt




// core/domain/src/main/java/com/myapp/core/domain/usecase/GetUserDataUseCase.kt


// core/domain/src/main/java/com/myapp/core/domain/di/DomainModule.kt


val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { GetUserDataUseCase(get()) }
}

// ===========================================
// 8. CORE - UI MODULE
// ===========================================

// core/ui/src/main/java/com/myapp/core/ui/components/CommonButton.kt


@Composable
fun CommonButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled
    ) {
        Text(text = text)
    }
}

// core/ui/src/main/java/com/myapp/core/ui/components/CommonTextField.kt


@Composable
fun CommonTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        enabled = enabled,
        singleLine = true
    )
}

// core/ui/src/main/java/com/myapp/core/ui/theme/Theme.kt


// ===========================================
// 9. CORE - NAVIGATION MODULE
// ===========================================

// core/navigation/src/main/java/com/myapp/core/navigation/AppNavigation.kt

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
    }
}

// core/navigation/src/main/java/com/myapp/core/navigation/Routes.kt

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
}

// ===========================================
// 10. FEATURE - LOGIN MODULE
// ===========================================

// features/login/src/main/java/com/myapp/features/login/presentation/LoginStateUI.kt


data class LoginStateUI(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val user: UserData? = null,
    val errorMessage: String? = null
)

sealed class LoginIntent {
    data class UpdateEmail(val email: String) : LoginIntent()
    data class UpdatePassword(val password: String) : LoginIntent()
    object Login : LoginIntent()
    object ClearError : LoginIntent()
}

sealed class LoginEffect {
    object NavigateToHome : LoginEffect()
    data class ShowError(val message: String) : LoginEffect()
}

// features/login/src/main/java/com/myapp/features/login/presentation/LoginViewModel.kt


class LoginViewModel(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(LoginStateUI())
    val state: StateFlow<LoginStateUI> = _state.asStateFlow()

    private val _effects = Channel<LoginEffect>()
    val effects = _effects.receiveAsFlow()

    fun handleIntent(intent: LoginIntent) {
        when (intent) {
            is LoginIntent.UpdateEmail -> {
                _state.value = _state.value.copy(email = intent.email, errorMessage = null)
            }

            is LoginIntent.UpdatePassword -> {
                _state.value = _state.value.copy(password = intent.password, errorMessage = null)
            }

            is LoginIntent.Login -> {
                performLogin()
            }

            is LoginIntent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun performLogin() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val result = loginUseCase(_state.value.email, _state.value.password)

            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = user
                    )
                    _effects.send(LoginEffect.NavigateToHome)
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Login failed"
                    )
                    _effects.send(LoginEffect.ShowError(exception.message ?: "Login failed"))
                }
            )
        }
    }
}

// features/login/src/main/java/com/myapp/features/login/presentation/LoginScreen.kt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToHome: () -> Unit,
    viewModel: LoginViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is LoginEffect.NavigateToHome -> onNavigateToHome()
                is LoginEffect.ShowError -> {
                    // Handle error display if needed
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Login",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        CommonTextField(
            value = state.email,
            onValueChange = { viewModel.handleIntent(LoginIntent.UpdateEmail(it)) },
            label = "Email",
            modifier = Modifier.padding(bottom = 16.dp),
            enabled = !state.isLoading
        )

        CommonTextField(
            value = state.password,
            onValueChange = { viewModel.handleIntent(LoginIntent.UpdatePassword(it)) },
            label = "Password",
            isPassword = true,
            modifier = Modifier.padding(bottom = 24.dp),
            enabled = !state.isLoading
        )

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        CommonButton(
            text = if (state.isLoading) "Loading..." else "Login",
            onClick = { viewModel.handleIntent(LoginIntent.Login) },
            enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
        )
    }
}

// features/login/src/main/java/com/myapp/features/login/di/LoginModule.kt

val loginModule = module {
    viewModel { LoginViewModel(get()) }
}

// ===========================================
// 11. FEATURE - HOME MODULE
// ===========================================

// features/home/src/main/java/com/myapp/features/home/presentation/HomeStateUI.kt


data class HomeStateUI(
    val user: UserData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

sealed class HomeIntent {
    object LoadUserData : HomeIntent()
    object Logout : HomeIntent()
    object ClearError : HomeIntent()
}

sealed class HomeEffect {
    object NavigateToLogin : HomeEffect()
    data class ShowError(val message: String) : HomeEffect()
}

// features/home/src/main/java/com/myapp/features/home/presentation/HomeViewModel.kt


class HomeViewModel(
    private val getUserDataUseCase: GetUserDataUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeStateUI())
    val state: StateFlow<HomeStateUI> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        handleIntent(HomeIntent.LoadUserData)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadUserData -> {
                loadUserData()
            }

            is HomeIntent.Logout -> {
                performLogout()
            }

            is HomeIntent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val result = getUserDataUseCase()

            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = user
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load user data"
                    )
                }
            )
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            _effects.send(HomeEffect.NavigateToLogin)
        }
    }
}

// features/home/src/main/java/com/myapp/features/home/presentation/HomeScreen.kt


@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.NavigateToLogin -> onNavigateToLogin()
                is HomeEffect.ShowError -> {
                    // Handle error display if needed
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Home",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.padding(bottom = 16.dp))
        }

        state.user?.let { user ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Welcome, ${user.name}!",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Email: ${user.email}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        state.errorMessage?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        CommonButton(
            text = "Logout",
            onClick = { viewModel.handleIntent(HomeIntent.Logout) }
        )
    }
}

// features/home/src/main/java/com/myapp/features/home/di/HomeModule.kt


val homeModule = module {
    viewModel { HomeViewModel(get()) }
}

// ===========================================
// 12. MANIFESTO ANDROID
// ===========================================

// app/src/main/AndroidManifest.xml
/*
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:name=".MyApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.MyApp">
        
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.MyApp">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
    </application>
</manifest>
*/

// ===========================================
// 13. CONFIGURAÇÕES GRADLE DOS MÓDULOS
// ===========================================

// settings.gradle
/*
include ':app'
include ':core:data'
include ':core:domain'
include ':core:network'
include ':core:ui'
include ':core:navigation'
include ':features:login'
include ':features:home'
*/

// ===========================================
// 14. BUILD.GRADLE DOS MÓDULOS CORE
// ===========================================

// ===========================================
// 15. BUILD.GRADLE DOS MÓDULOS FEATURES
// ===========================================



// ===========================================
// 16. COMPONENTES ADICIONAIS DE UI
// ===========================================

// core/ui/src/main/java/com/myapp/core/ui/components/LoadingIndicator.kt


@Composable
fun LoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

// core/ui/src/main/java/com/myapp/core/ui/components/ErrorMessage.kt


@Composable
fun ErrorMessage(
    message: String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(bottom = if (onRetry != null) 16.dp else 0.dp)
            )

            onRetry?.let { retry ->
                Button(
                    onClick = retry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

// ===========================================
// 17. MELHORIAS NO NETWORK MODULE
// ===========================================

// core/network/src/main/java/com/myapp/core/network/NetworkResult.kt


sealed class NetworkResult<T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error<T>(val exception: Throwable) : NetworkResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : NetworkResult<T>()
}

// core/network/src/main/java/com/myapp/core/network/ApiClient.kt


abstract class ApiClient {
    protected suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): NetworkResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                NetworkResult.Success(apiCall())
            } catch (exception: Exception) {
                NetworkResult.Error(exception)
            }
        }
    }
}

// core/network/src/main/java/com/myapp/core/network/RetrofitApiClient.kt

class RetrofitApiClient(
    private val retrofitApiService: RetrofitApiService
) : ApiClient(), ApiService {

    override suspend fun login(email: String, password: String): LoginResponse {
        return when (val result = safeApiCall {
            retrofitApiService.login(LoginRequest(email, password))
        }) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> throw result.exception
            is NetworkResult.Loading -> throw Exception("Loading state not expected")
        }
    }

    override suspend fun getUserData(): UserDataResponse {
        return when (val result = safeApiCall {
            retrofitApiService.getUserData()
        }) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> throw result.exception
            is NetworkResult.Loading -> throw Exception("Loading state not expected")
        }
    }
}

// ===========================================
// 18. MELHORIAS NO REPOSITORY
// ===========================================

// core/data/src/main/java/com/myapp/core/data/repository/UserRepositoryImpl.kt


class UserRepositoryImpl(
    private val apiService: ApiService,
    private val localDataSource: LocalDataSource
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<UserData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(email, password)
                saveUserToken(response.token)
                Result.success(response.user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getUserData(): Result<UserData> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getUserData()
                Result.success(response.user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun saveUserToken(token: String) {
        withContext(Dispatchers.IO) {
            localDataSource.saveToken(token)
        }
    }

    override suspend fun getUserToken(): String? {
        return withContext(Dispatchers.IO) {
            localDataSource.getToken()
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        return getUserToken() != null
    }
}

// ===========================================
// 19. USE CASES ADICIONAIS
// ===========================================

// core/domain/src/main/java/com/myapp/core/domain/usecase/LogoutUseCase.kt


class LogoutUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            userRepository.saveUserToken("")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// core/domain/src/main/java/com/myapp/core/domain/usecase/CheckAuthUseCase.kt


class CheckAuthUseCase(private val userRepository: UserRepository) {
    suspend operator fun invoke(): Boolean {
        return userRepository.getUserToken() != null
    }
}

// ===========================================
// 20. ATUALIZAÇÃO DOS MÓDULOS DI
// ===========================================

// core/network/src/main/java/com/myapp/core/network/di/NetworkModule.kt


val networkModule = module {

    // OkHttp Client
    single {
        OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    // Retrofit
    single {
        Retrofit.Builder()
            .baseUrl("https://api.myapp.com/")
            .client(get<OkHttpClient>())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    single { get<Retrofit>().create(RetrofitApiService::class.java) }

    // Ktor
    single {
        HttpClient(Android) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    // API Services
    single<ApiService> { RetrofitApiClient(get()) }
    single { KtorApiService(get()) }
}

// core/domain/src/main/java/com/myapp/core/domain/di/DomainModule.kt


val domainModule = module {
    factory { LoginUseCase(get()) }
    factory { GetUserDataUseCase(get()) }
    factory { LogoutUseCase(get()) }
    factory { CheckAuthUseCase(get()) }
}

// ===========================================
// 21. MELHORIAS NAS FEATURES
// ===========================================

// features/home/src/main/java/com/myapp/features/home/presentation/HomeViewModel.kt


class HomeViewModel(
    private val getUserDataUseCase: GetUserDataUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(HomeStateUI())
    val state: StateFlow<HomeStateUI> = _state.asStateFlow()

    private val _effects = Channel<HomeEffect>()
    val effects = _effects.receiveAsFlow()

    init {
        handleIntent(HomeIntent.LoadUserData)
    }

    fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadUserData -> {
                loadUserData()
            }

            is HomeIntent.Logout -> {
                performLogout()
            }

            is HomeIntent.ClearError -> {
                _state.value = _state.value.copy(errorMessage = null)
            }
        }
    }

    private fun loadUserData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            val result = getUserDataUseCase()

            result.fold(
                onSuccess = { user ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        user = user
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Failed to load user data"
                    )
                }
            )
        }
    }

    private fun performLogout() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val result = logoutUseCase()

            result.fold(
                onSuccess = {
                    _state.value = _state.value.copy(isLoading = false)
                    _effects.send(HomeEffect.NavigateToLogin)
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Logout failed"
                    )
                }
            )
        }
    }
}

// features/home/src/main/java/com/myapp/features/home/di/HomeModule.kt


val homeModule = module {
    viewModel { HomeViewModel(get(), get()) }
}

// ===========================================
// 22. TESTES UNITÁRIOS (EXEMPLO)
// ===========================================

// features/login/src/test/java/com/myapp/features/login/LoginViewModelTest.kt
/*
package com.myapp.features.login

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.myapp.core.domain.usecase.LoginUseCase
import com.myapp.core.network.UserData
import com.myapp.features.login.presentation.*
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class LoginViewModelTest {
    
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    
    private val testDispatcher = StandardTestDispatcher()
    private val loginUseCase = mockk<LoginUseCase>()
    private lateinit var viewModel: LoginViewModel
    
    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LoginViewModel(loginUseCase)
    }
    
    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    @Test
    fun `when login is successful, should update state with user data`() = runTest {
        // Given
        val userData = UserData("1", "Test User", "test@example.com")
        coEvery { loginUseCase("test@example.com", "password") } returns Result.success(userData)
        
        // When
        viewModel.handleIntent(LoginIntent.UpdateEmail("test@example.com"))
        viewModel.handleIntent(LoginIntent.UpdatePassword("password"))
        viewModel.handleIntent(LoginIntent.Login)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.state.first()
        assertEquals(userData, state.user)
        assertFalse(state.isLoading)
    }
    
    @Test
    fun `when login fails, should update state with error message`() = runTest {
        // Given
        val errorMessage = "Invalid credentials"
        coEvery { loginUseCase("test@example.com", "wrong") } returns Result.failure(Exception(errorMessage))
        
        // When
        viewModel.handleIntent(LoginIntent.UpdateEmail("test@example.com"))
        viewModel.handleIntent(LoginIntent.UpdatePassword("wrong"))
        viewModel.handleIntent(LoginIntent.Login)
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Then
        val state = viewModel.state.first()
        assertEquals(errorMessage, state.errorMessage)
        assertFalse(state.isLoading)
    }
}
*/

// ===========================================
// 23. PROGUARD RULES
// ===========================================

// app/proguard-rules.pro
/*
# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }

# Koin
-keep class org.koin.** { *; }
-keep class kotlin.Metadata { *; }

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Model classes
-keep class com.myapp.core.network.** { *; }
*/