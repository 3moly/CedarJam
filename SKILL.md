---
name: metro-di-mobile
description: >-
  Guides compile-time dependency injection with Metro for Kotlin Multiplatform:
  @DependencyGraph, @Provides, @Inject, @BindingContainer, multi-module graphs,
  scopes, assisted injection, Lazy/Provider, multibindings, and Decompose-friendly
  factories. Use when working with Metro DI, KMP dependency injection, DI graphs,
  providers, multi-module setup, or comparing Metro to Koin.
---

# Metro DI for Kotlin Multiplatform

Compile-time DI for KMP ([Metro](https://github.com/ZacSweers/metro); production use includes Cash App). Prefer constructor `@Inject` over `@Provides` when possible; use graphs as the single composition root per platform entry point.

## Setup

**`build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.metro)
}
```

**`libs.versions.toml`**

```toml
[versions]
metro = "0.1.1"

[plugins]
metro = { id = "dev.zacsweers.metro", version.ref = "metro" }
```

Apply the Metro plugin to modules that participate in DI.

## Core API (quick reference)

| Construct | Role |
|-----------|------|
| `@DependencyGraph` | Root graph; expose `val` dependencies and factory methods. |
| `@BindingContainer` | Module grouping `@Provides` methods (feature/layer/platform). |
| `@Provides` | Factory for a type (use when construction logic belongs in the graph/module). |
| `@Inject` | Mark class for constructor injection. |
| `@Assisted` / `@AssistedFactory` | Runtime parameters (e.g. `ComponentContext`, IDs). |
| `createGraph<YourGraph>()` | Entry point to obtain the graph instance. |

**Minimal graph**

```kotlin
@DependencyGraph
interface AppGraph {
    val authRepository: AuthRepository

    @Provides
    fun provideHttpClient(): HttpClient = HttpClient(CIO) { /* ... */ }

    @Provides
    fun provideApiService(httpClient: HttpClient): ApiService =
        ApiServiceImpl(httpClient, "https://api.example.com")
}
```

**Injected implementation**

```kotlin
@Inject
class AuthRepositoryImpl(
    private val api: ApiService,
    private val tokenStorage: TokenStorage
) : AuthRepository
```

Graph exposes `AuthRepository`; Metro binds `AuthRepositoryImpl` when that is the injectable implementation.

## Multi-module pattern

- Put `@BindingContainer` classes next to implementations (per feature or layer).
- Assemble with `@DependencyGraph(bindingContainers = [ ... ])` on the app (or platform) graph.
- Do not duplicate graphs per platform unnecessarily—use platform modules only for `expect`/`actual` or Android/iOS-specific bindings.

See [reference.md](.cursor/skills/metro-di-mobile/reference.md) for feature modules, Android/iOS graph split, and full assembly example.

## Advanced (pointers)

- **Scopes**: `@DependencyGraph(scope = ..., additionalScopes = ...)` with `@Scope("...")` on `@Provides` as needed.
- **Assisted**: `@AssistedFactory` interface on the graph; factories for components needing runtime args.
- **Lazy / Provider**: `Lazy<T>` for deferred init; `Provider<T>` for new instance per resolution (see Metro docs for exact semantics).
- **Multibinds**: `@Multibinds` on the graph; `@ContributesIntoSet` / `@ContributesIntoMap` for contributors.

Full snippets: [reference.md](.cursor/skills/metro-di-mobile/reference.md).

## Decompose

Model UI components with `@Inject` constructors, `@Assisted` `ComponentContext`, and an `@AssistedFactory` per component. Root component receives child factories from the graph and creates children in navigation. See [reference.md](.cursor/skills/metro-di-mobile/reference.md) for `DefaultHomeComponent`, `DefaultRootComponent`, and `MainActivity` wiring.

## Testing

Replace network or data edges with `@BindingContainer` test modules and a `TestAppGraph` that swaps them in `bindingContainers`. Use `createGraph<TestAppGraph>()` in tests.

## Do / Don’t

**Do:** one primary `@DependencyGraph` per platform entry; keep platform types out of common containers; expose interfaces from graphs; use `Lazy<T>` for expensive deps.

**Don’t:** multiple competing graphs for the same entry; `@Provides` when `@Inject` suffices; leak `Context` into common; expose concrete impl types from graphs.

## Koin vs Metro (summary)

Metro: compile-time validation, generated wiring, Dagger-like mental model. Koin: runtime DSL, fewer compile guarantees. Pick Metro when you want graph errors at build time and a single explicit composition root.

## Resources

- [Metro GitHub](https://github.com/ZacSweers/metro)
- [Metro documentation](https://zacsweers.github.io/metro/)
- [Cash App on Metro](https://code.cash.app/cash-android-moves-to-metro)

## Additional detail

- Patterns, platform graphs, Decompose samples, multibinds, and assisted examples: [reference.md](.cursor/skills/metro-di-mobile/reference.md)
