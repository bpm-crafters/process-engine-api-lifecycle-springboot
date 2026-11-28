# Spring Boot lifecycle bridge

`process-engine-api-lifecycle-spring-boot` integrates the portable [process application lifecycle](lifecycle.md) with Spring Boot.

## Auto-configuration

The module registers `ProcessApplicationLifecycleAutoConfiguration` through Spring Boot auto-configuration imports.

It creates a `DefaultProcessApplicationLifecycle` only when the application has not supplied its own `ProcessApplicationLifecycle` bean. Supplying a custom lifecycle bean is therefore the extension point for applications that need different coordination behavior.

When a `TaskSubscriptionApi` bean is available, the bridge also configures:

- `SpringProcessApplicationLifecycleEventPublisher`, which projects named lifecycle phases as synchronous Spring application events.
- `SpringProcessApplicationLifecycleDriver`, which drives startup from `ApplicationStartedEvent` and shutdown through Spring's `SmartLifecycle` callback.

The driver uses `Int.MAX_VALUE - 1000` as its Spring `SmartLifecycle` phase. This leaves room for application components that must stop before or after the bridge.

## Startup and shutdown

At `ApplicationStartedEvent`, the driver obtains `TaskSubscriptionApi` and makes one lifecycle request:

```kotlin
lifecycle.transitionTo(
  ProcessApplicationPhase.RUNNING,
  TaskSubscriptionRegistrationContext(taskSubscriptionApi)
)
```

The lifecycle expands that request into all missing named startup transitions in order. During context shutdown, the driver requests `STOPPED`, which transitions through `STOPPING` first. This lets the lifecycle collect shutdown failures while still invoking every shutdown participant. If startup fails, the driver requests the same shutdown transition during Spring context teardown, allowing participants to release partially started resources.

The bridge does not create or require a task-subscription repository while the lifecycle is still `NOT_STARTED`. The `TaskSubscriptionApi` is obtained only when the startup transition crosses `SUBSCRIPTION_REGISTRATION`.

### Startup validation

After `ApplicationStartedEvent` has been dispatched and before `ApplicationReadyEvent`, the bridge checks the lifecycle in an early `ApplicationRunner`.
If the refreshed application context contains a `TaskSubscriptionApi`, the lifecycle must have reached `RUNNING` and must be below `STOPPING`.
Spring Boot fails startup and reports the current phase when that requirement is violated. Applications without a `TaskSubscriptionApi` skip this check.

If the error reports `NOT_STARTED`, check that the lifecycle driver is active and that the adapter's auto-configuration runs before
`ProcessApplicationLifecycleAutoConfiguration`. The bridge's bean condition can skip the driver if the adapter declares the subscription API later.

## Spring events

The event publisher emits the following synchronous application events at their matching named phases:

| Lifecycle phase | Spring event | Typical listener action |
| --- | --- | --- |
| `INFRASTRUCTURE_READY` | `ProcessApplicationInfrastructureReadyEvent` | Prepare adapter-dependent infrastructure. |
| `SUBSCRIPTION_REGISTRATION` | `TaskSubscriptionRegistrationEvent` | Register initial subscriptions using `getTaskSubscriptionApi()`. |
| `SUBSCRIPTIONS_REGISTERED` | `TaskSubscriptionsRegisteredEvent` | Start components that depend on initial subscriptions. |
| `RUNNING` | `ProcessApplicationRunningEvent` | Start normal application work. |
| `STOPPING` | `ProcessApplicationStoppingEvent` | Stop new work and begin shutdown. |
| `STOPPED` | `ProcessApplicationStoppedEvent` | Perform final cleanup. |

For example, an application can register initial subscriptions with a synchronous event listener:

```kotlin
@Component
class OrderTaskSubscriptions {

  @EventListener
  fun register(event: TaskSubscriptionRegistrationEvent) {
    event.getTaskSubscriptionApi().subscribeForTask(orderTaskSubscription)
  }
}
```

Application-event listeners run inside the lifecycle transition. A startup exception prevents that target phase from being reached and makes the lifecycle unusable. During shutdown, listener failures are collected after all shutdown phases have run. Keep listeners deterministic and avoid blocking work that belongs after `RUNNING`.

## Generic lifecycle event listeners

Every event in the table extends `ProcessEngineApiLifecycleEvent`. The abstract base class carries the integer lifecycle phase and exposes it through `getPhase()`. A listener can therefore handle all Process Engine API lifecycle events without subscribing to unrelated Spring events.

```kotlin
@Component
class LifecycleAuditListener {

  @EventListener
  fun onLifecycleEvent(event: ProcessEngineApiLifecycleEvent) {
    audit.record(event.getPhase(), event.javaClass.simpleName)
  }
}
```

The phase is also visible to Spring Expression Language as `event.phase`, so a generic listener can be restricted to a phase or a group of phases.

```kotlin
@EventListener(
  condition = "#event.phase == T(dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase).RUNNING"
)
fun onRunning(event: ProcessEngineApiLifecycleEvent) {
  startApplicationWork()
}
```

## Custom lifecycle participants in a Spring application

For precise placement between named boundaries, register portable lifecycle participants from configuration or from a Spring bean that calls `ProcessApplicationLifecycle.register` before startup begins. A component that is only created during normal runtime may also register a participant for a future phase such as `STOPPING`.

```kotlin
@Component
class ShutdownRegistration(
  private val lifecycle: ProcessApplicationLifecycle,
  private val resource: Resource
) {

  fun registerShutdown() {
    lifecycle.register(object : ProcessApplicationLifecycleParticipant {
      override fun getPhase(): Int = ProcessApplicationPhase.STOPPING

      override fun onPhase(context: ProcessApplicationLifecycleContext) {
        resource.close()
      }
    })
  }
}
```

The participant must be registered before `STOPPING` is reached and not while another lifecycle transition is dispatching participants.
