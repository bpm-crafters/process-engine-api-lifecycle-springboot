package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.lifecycle.TaskSubscriptionRegistrationContext
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.SmartLifecycle

/**
 * Drives the portable lifecycle from Spring Boot startup and shutdown callbacks.
 *
 * @since 2026.11.1
 */
class SpringProcessApplicationLifecycleDriver(
  private val lifecycle: ProcessApplicationLifecycle,
  private val taskSubscriptionApiProvider: ObjectProvider<TaskSubscriptionApi>
) : ApplicationListener<ApplicationStartedEvent>, SmartLifecycle {

  companion object {

    const val SPRING_LIFECYCLE_PHASE: Int = Int.MAX_VALUE - 1_000
  }

  @Volatile
  private var started = false

  @Volatile
  private var startupAttempted = false

  override fun start() {
  }

  override fun onApplicationEvent(event: ApplicationStartedEvent) {
    startLifecycle()
  }

  @Synchronized
  fun startLifecycle() {
    if (started) {
      return
    }

    startupAttempted = true
    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApiProvider.getObject())
    )
    started = true
  }

  @Synchronized
  override fun stop() {
    if (!started && !startupAttempted) {
      return
    }

    try {
      lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)
    } finally {
      started = false
      startupAttempted = false
    }
  }

  override fun stop(callback: Runnable) {
    try {
      stop()
    } finally {
      callback.run()
    }
  }

  override fun isRunning(): Boolean = started

  override fun isAutoStartup(): Boolean = true

  override fun getPhase(): Int = SPRING_LIFECYCLE_PHASE


}
