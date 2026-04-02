# Metro DI — extended patterns and examples

## @Provides in graph

```kotlin
@DependencyGraph
interface AppGraph {
    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 30_000
        }
    }

    @Provides
    fun provideApiService(httpClient: HttpClient): ApiService =
        ApiServiceImpl(httpClient, "https://api.your-project.com")

    @Provides
    fun provideAuthRepository(api: ApiService, tokenStorage: TokenStorage): AuthRepository =
        AuthRepositoryImpl(api, tokenStorage)
}
```

## @BindingContainer modules

```kotlin
@BindingContainer
class NetworkModule {
    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
    }

    @Provides
    fun provideApiService(httpClient: HttpClient): ApiService =
        ApiServiceImpl(httpClient)
}

@BindingContainer
class DataModule {
    @Provides
    fun provideTokenStorage(): TokenStorage = TokenStorageImpl()

    @Provides
    fun providePreferencesDataStore(context: PlatformContext): DataStore<Preferences> =
        PreferenceDataStoreFactory.createWithPath(
            produceFile = { Path(createDataStorePath(context)) }
        )
}
```

## Platform-specific graphs

```kotlin
@BindingContainer
class CommonNetworkModule {
    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { json() }
    }
}

@BindingContainer
class CommonDataModule {
    @Provides
    fun provideAuthRepository(api: ApiService, storage: TokenStorage): AuthRepository =
        AuthRepositoryImpl(api, storage)
}

@BindingContainer
class AndroidPlatformModule {
    @Provides
    fun providePlatformContext(context: Context): PlatformContext = context

    @Provides
    fun provideTokenStorage(context: Context): TokenStorage =
        AndroidTokenStorage(context)
}

@DependencyGraph(
    bindingContainers = [
        CommonNetworkModule::class,
        CommonDataModule::class,
        AndroidPlatformModule::class
    ]
)
interface AndroidAppGraph {
    val authRepository: AuthRepository
    fun createRootComponent(context: ComponentContext): RootComponent
}

@BindingContainer
class IosPlatformModule {
    @Provides
    fun providePlatformContext(): PlatformContext = PlatformContext()

    @Provides
    fun provideTokenStorage(): TokenStorage = IosTokenStorage()
}

@DependencyGraph(
    bindingContainers = [
        CommonNetworkModule::class,
        CommonDataModule::class,
        IosPlatformModule::class
    ]
)
interface IosAppGraph {
    val authRepository: AuthRepository
    fun createRootComponent(context: ComponentContext): RootComponent
}
```

## Multi-module feature bindings

```kotlin
@BindingContainer
class AuthModule {
    @Provides
    fun provideAuthRepository(
        api: ApiService,
        tokenStorage: TokenStorage
    ): AuthRepository = AuthRepositoryImpl(api, tokenStorage)

    @Provides
    fun provideLoginUseCase(
        authRepository: AuthRepository
    ): LoginUseCase = LoginUseCase(authRepository)
}

@BindingContainer
class HomeModule {
    @Provides
    fun provideHomeRepository(
        api: ApiService,
        database: AppDatabase
    ): HomeRepository = HomeRepositoryImpl(api, database)
}
```

### Assembly in app graph

```kotlin
@DependencyGraph(
    bindingContainers = [
        CommonNetworkModule::class,
        CommonDataModule::class,
        AndroidPlatformModule::class,
        AuthModule::class,
        HomeModule::class
    ]
)
interface AndroidAppGraph {
    val httpClient: HttpClient
    val authRepository: AuthRepository
    val homeRepository: HomeRepository
    fun createRootComponent(context: ComponentContext): RootComponent
}
```

## Scopes

```kotlin
@DependencyGraph(
    scope = "app",
    additionalScopes = ["activity"]
)
interface AppGraph {
    @Provides
    @Scope("app")
    fun provideAppDatabase(): AppDatabase = AppDatabase()

    @Provides
    @Scope("activity")
    fun provideNavigator(): Navigator = Navigator()
}
```

## Assisted injection

```kotlin
@Inject
class HomeComponent(
    private val repository: HomeRepository,
    @Assisted val componentContext: ComponentContext
) : ComponentContext by componentContext

@AssistedFactory
interface HomeComponentFactory {
    fun create(componentContext: ComponentContext): HomeComponent
}

@DependencyGraph
interface AppGraph {
    val homeComponentFactory: HomeComponentFactory
}
```

## Lazy and Provider

```kotlin
@Inject
class SomeService(
    private val lazyDatabase: Lazy<AppDatabase>,
    private val userProvider: Provider<User>
) {
    fun doWork() {
        val db = lazyDatabase.value
        val user1 = userProvider.get()
        val user2 = userProvider.get()
    }
}
```

## Multibindings

```kotlin
@DependencyGraph
interface AppGraph {
    @Multibinds
    val interceptors: Set<Interceptor>

    @Multibinds
    val handlers: Map<String, Handler>
}

@ContributesIntoSet(AppGraph::class)
class LoggingInterceptor : Interceptor {
    override fun intercept(chain: Chain) { /* ... */ }
}

@ContributesIntoMap(AppGraph::class, key = "auth")
class AuthHandler : Handler {
    override fun handle(request: Request) { /* ... */ }
}
```

## Decompose: component with DI

```kotlin
interface HomeComponent {
    val state: Value<HomeState>
    fun onItemClick(item: HomeItem)
}

@Inject
class DefaultHomeComponent(
    private val repository: HomeRepository,
    @Assisted componentContext: ComponentContext
) : HomeComponent, ComponentContext by componentContext {

    private val _state = MutableValue<HomeState>(HomeState.Loading)
    override val state: Value<HomeState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        componentScope.launch {
            repository.getItems()
                .onSuccess { _state.value = HomeState.Success(it) }
                .onError { msg, _ -> _state.value = HomeState.Error(msg) }
        }
    }

    override fun onItemClick(item: HomeItem) { }

    @AssistedFactory
    interface Factory {
        fun create(componentContext: ComponentContext): DefaultHomeComponent
    }
}

sealed class HomeState {
    data object Loading : HomeState()
    data class Success(val items: List<HomeItem>) : HomeState()
    data class Error(val message: String) : HomeState()
}
```

## Decompose: root component factory

```kotlin
interface RootComponent {
    val childStack: Value<ChildStack<Config, Child>>

    sealed class Child {
        data class Auth(val component: AuthComponent) : Child()
        data class Home(val component: HomeComponent) : Child()
    }

    @Serializable
    sealed class Config {
        @Serializable data object Auth : Config()
        @Serializable data object Home : Config()
    }
}

@Inject
class DefaultRootComponent(
    private val authComponentFactory: AuthComponent.Factory,
    private val homeComponentFactory: HomeComponent.Factory,
    @Assisted componentContext: ComponentContext
) : RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<RootComponent.Config>()

    override val childStack: Value<ChildStack<RootComponent.Config, RootComponent.Child>> =
        childStack(
            source = navigation,
            serializer = RootComponent.Config.serializer(),
            initialConfiguration = RootComponent.Config.Auth,
            childFactory = ::createChild
        )

    private fun createChild(
        config: RootComponent.Config,
        context: ComponentContext
    ): RootComponent.Child = when (config) {
        RootComponent.Config.Auth -> RootComponent.Child.Auth(
            authComponentFactory.create(context) { navigateToHome() }
        )
        RootComponent.Config.Home -> RootComponent.Child.Home(
            homeComponentFactory.create(context)
        )
    }

    private fun navigateToHome() {
        navigation.replaceAll(RootComponent.Config.Home)
    }

    @AssistedFactory
    interface Factory {
        fun create(componentContext: ComponentContext): DefaultRootComponent
    }
}
```

## App graph with root factory (Android)

```kotlin
@DependencyGraph(
    bindingContainers = [
        NetworkModule::class,
        DataModule::class,
        AuthModule::class,
        HomeModule::class
    ]
)
interface AndroidAppGraph {
    val rootComponentFactory: DefaultRootComponent.Factory
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val graph = createGraph<AndroidAppGraph>()
        val rootComponent = graph.rootComponentFactory.create(
            defaultComponentContext()
        )

        setContent {
            AppTheme {
                RootContent(component = rootComponent)
            }
        }
    }
}
```

## Testing: test modules

```kotlin
@BindingContainer
class TestNetworkModule {
    @Provides
    fun provideFakeApiService(): ApiService = FakeApiService()
}

@DependencyGraph(
    bindingContainers = [
        TestNetworkModule::class,
        DataModule::class
    ]
)
interface TestAppGraph {
    val authRepository: AuthRepository
}

class AuthRepositoryTest {
    private val graph = createGraph<TestAppGraph>()

    @Test
    fun `login returns success`() = runTest {
        val result = graph.authRepository.login("test@test.com", "password")
        assertTrue(result is AppResult.Success)
    }
}
```

## Comparison with Koin

| Feature | Metro | Koin |
|---------|-------|------|
| Type safety | Compile-time | Runtime |
| Error detection | Build time | Runtime crash |
| Performance | No reflection | Some reflection |
| KMP support | Full | Full |
| Learning curve | Medium (Dagger-like) | Low |
| Build speed | 47–56% faster than KAPT (Metro project benchmarks) | No codegen |
