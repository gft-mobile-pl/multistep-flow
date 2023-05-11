# Observable Session

`Session` class represents a concept of a session:
- session can be started and ended multiple times,
- started session always holds some data,
- held data can be atomically updated,
- data updates are possible only when a session is started,
- session can be observed by any number of observers,
- whenever session's data is updated all the observers are notified about the change (with `data` flow),
- whenever a new observer is subscribed it will receive the current data held in the session,
- session's data is disposed when session ends.

> ⚠ Be aware that `data` flow is conflated. If it is important that no item is missed the flow should
> be subscribed using `Dispatchers.Unconfined` dispatcher.<br /> 
> It is also a good idea to start the subscription using `CoroutineStart.UNDISPATCHED` start method - otherwise the subscription
> may not be synchronous.

## Guidelines
- session lifetime should be as short as possible: one should start a session when it is really required and stop it once that held data is obsolete,
- you may (and generally should) have multiple sessions in you application,
- sessions visibility should be narrowed to the domain layer - only use cases should be able to interact with the sessions; using `internal` access modifier should be enough in most cases,
- you should avoid passing raw session payload to other components (e.g. to view model)
- you may keep sensitive data in sessions - session's data is lost once the application dies
- sessions are very often singletons (you may use `singleOf` in Koin)
- when creating use cases that interact with a session try not to expose the concept of a session to other layers, e.g. avoid names like `Start_XXX_SessionUseCase`

## Usage

### Define session and its data
```kotlin
internal class LoginSession : Session<LoginSessionData>()

internal data class LoginSessionData(
    val username: String,
    val password: String
)
```

### Create uses cases that mutate sessions
```kotlin
val domainModule = module {
    ...
    singleOf(::LoginSession)
    ...
}
```

#### Starting session
> ℹ You must always provide initial data while starting session.
```kotlin
class BeginLoginProcessUseCase internal constructor(
    private val loginSession: LoginSession
) {
    operator fun invoke() {
        if (!loginSession.isStarted) {
            loginSession.start(LoginSessionData("", ""))
        }
    }
}
```

#### Updating session's data
```kotlin
class UpdateCollectedUsernameUseCase internal constructor(
    private val loginSession: LoginSession
) {
    operator fun invoke(username: String) {
        loginSession.update { sessionData ->
            sessionData.copy(username = username)
        }
    }
}
```

#### Getting data from session (synchronously)
```kotlin
class GetAccessTokenUseCase internal constructor(
    private val loggedInSession: LoggedInSession
) {
    operator fun invoke(): String = loggedInSession.requireData().accessToken
}
```

#### Streaming data from session
```kotlin
class GetCollectedUsernameUseCase internal constructor(
    private val loginSession: LoginSession
) {
    operator fun invoke(): Flow<String> = loginSession.data.map { data -> data!!.username }
}
```
```kotlin
class IsCollectedUsernameFormatValidUseCase internal constructor(
    private val loginSession: LoginSession
) {
    operator fun invoke(): Flow<UsernameValidationResult> = loginSession
        .data
        .map { sessionData ->
            sessionData?.username?.let { username ->
                val length = username.length
                when {
                    length == 0 -> NOT_AVAILABLE
                    length < 6 -> TOO_SHORT
                    length > 12 -> TOO_LONG
                    else -> VALID
                }
            } ?: NOT_AVAILABLE
        }
}

enum class UsernameValidationResult {
    NOT_AVAILABLE,
    TOO_SHORT,
    TOO_LONG,
    VALID
}
```

#### Closing session
```kotlin
class AbortLoginProcessUseCase internal constructor(
    private val loginSession: LoginSession
) {
    operator fun invoke() {
        if (loginSession.isStarted) {
            loginSession.end()
        }
    }
}
```
