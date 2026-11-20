package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleContext
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleParticipant
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.lifecycle.TaskSubscriptionRegistrationContext
import org.springframework.context.ApplicationEventPublisher

/**
 * Projects named Process Engine API lifecycle phases as synchronous Spring events.
 *
 * @since 1.8.0
 */
class SpringProcessApplicationLifecycleEventPublisher(
  lifecycle: ProcessApplicationLifecycle,
  private val applicationEventPublisher: ApplicationEventPublisher
) {

  private companion object {
    val namedPhases = listOf(
      ProcessApplicationPhase.INFRASTRUCTURE_READY,
      ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION,
      ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED,
      ProcessApplicationPhase.RUNNING,
      ProcessApplicationPhase.STOPPING,
      ProcessApplicationPhase.STOPPED
    )
  }

  init {
    namedPhases.forEach { phase -> lifecycle.register(EventParticipant(phase)) }
  }

  private inner class EventParticipant(
    private val phase: Int
  ) : ProcessApplicationLifecycleParticipant {

    override fun getPhase(): Int = phase

    override fun onPhase(context: ProcessApplicationLifecycleContext) {
      when (phase) {
        ProcessApplicationPhase.INFRASTRUCTURE_READY ->
          applicationEventPublisher.publishEvent(ProcessApplicationInfrastructureReadyEvent())
        ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION -> {
          val registrationContext = context as TaskSubscriptionRegistrationContext
          applicationEventPublisher.publishEvent(
            TaskSubscriptionRegistrationEvent(registrationContext.getTaskSubscriptionApi())
          )
        }
        ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED ->
          applicationEventPublisher.publishEvent(TaskSubscriptionsRegisteredEvent())
        ProcessApplicationPhase.RUNNING ->
          applicationEventPublisher.publishEvent(ProcessApplicationRunningEvent())
        ProcessApplicationPhase.STOPPING ->
          applicationEventPublisher.publishEvent(ProcessApplicationStoppingEvent())
        ProcessApplicationPhase.STOPPED ->
          applicationEventPublisher.publishEvent(ProcessApplicationStoppedEvent())
      }
    }
  }
}
