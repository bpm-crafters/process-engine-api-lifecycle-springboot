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
 * @since 1.8.0
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

    lifecycle.transitionTo(
      ProcessApplicationPhase.RUNNING,
      TaskSubscriptionRegistrationContext(taskSubscriptionApiProvider.getObject())
    )
    started = true
  }

  @Synchronized
  override fun stop() {
    if (!started) {
      return
    }

    lifecycle.transitionTo(ProcessApplicationPhase.STOPPING)
    lifecycle.transitionTo(ProcessApplicationPhase.STOPPED)
    started = false
  }

  override fun stop(callback: Runnable) {
    stop()
    callback.run()
  }

  override fun isRunning(): Boolean = started

  override fun isAutoStartup(): Boolean = true

  override fun getPhase(): Int = SPRING_LIFECYCLE_PHASE


}
