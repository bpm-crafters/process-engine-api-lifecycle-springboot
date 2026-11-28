package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published after the one-time task-subscription registration window has closed.
 *
 * @since 2026.11.1
 */
class TaskSubscriptionsRegisteredEvent :
  ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED)
