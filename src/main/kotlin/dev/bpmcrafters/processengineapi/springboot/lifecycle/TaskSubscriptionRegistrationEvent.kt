package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi

/**
 * Published synchronously while task subscriptions must be registered.
 *
 * @since 2026.11.1
 */
class TaskSubscriptionRegistrationEvent(
  private val taskSubscriptionApi: TaskSubscriptionApi
) : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION) {

  /**
   * Returns the API used to register task subscriptions.
   */
  fun getTaskSubscriptionApi(): TaskSubscriptionApi = taskSubscriptionApi
}
