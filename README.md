# MVI-architecture-with-Hilt-and-StateFlow

├── di/
│   └── NetworkModule.kt          ← Hilt provides Retrofit, Repo
├── data/
│   ├── remote/
│   │   ├── ApiService.kt         ← Retrofit interface
│   │   └── dto/PostDto.kt        ← Raw API response
│   └── repository/
│       └── PostRepositoryImpl.kt ← implements domain interface
├── domain/
│   ├── model/Post.kt             ← clean domain model
│   ├── repository/PostRepository.kt ← interface (no Android imports!)
│   └── usecase/GetPostsUseCase.kt
├── presentation/
│   ├── PostViewModel.kt          ← MVI brain
│   ├── PostContract.kt           ← UiState, UiIntent, UiEffect
│   └── PostScreen.kt             ← Compose UI
└── MainActivity.kt