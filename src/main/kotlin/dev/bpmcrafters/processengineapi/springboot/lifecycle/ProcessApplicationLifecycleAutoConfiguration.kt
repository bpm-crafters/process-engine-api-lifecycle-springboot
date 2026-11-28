package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.impl.lifecycle.DefaultProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.springframework.beans.factory.BeanFactoryUtils
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered

/**
 * Registers the Spring Boot bridge for the Process Engine API lifecycle.
 *
 * @since 2026.11.3
 */
@AutoConfiguration
class ProcessApplicationLifecycleAutoConfiguration {

  /**
   * Creates the portable lifecycle coordinator when an application has not supplied one.
   */
  @Bean
  @ConditionalOnMissingBean
  fun processApplicationLifecycle(): ProcessApplicationLifecycle = DefaultProcessApplicationLifecycle()

  /**
   * Projects named lifecycle phases as Spring application events when task subscriptions are supported.
   */
  @Bean
  @ConditionalOnBean(TaskSubscriptionApi::class)
  fun springProcessApplicationLifecycleEventPublisher(
    lifecycle: ProcessApplicationLifecycle,
    applicationEventPublisher: ApplicationEventPublisher
  ): SpringProcessApplicationLifecycleEventPublisher =
    SpringProcessApplicationLifecycleEventPublisher(lifecycle, applicationEventPublisher)

  /**
   * Drives lifecycle startup and shutdown when task subscriptions are supported.
   */
  @Bean
  @ConditionalOnBean(TaskSubscriptionApi::class)
  fun springProcessApplicationLifecycleDriver(
    lifecycle: ProcessApplicationLifecycle,
    taskSubscriptionApiProvider: ObjectProvider<TaskSubscriptionApi>,
    eventPublisher: SpringProcessApplicationLifecycleEventPublisher
  ): SpringProcessApplicationLifecycleDriver =
    SpringProcessApplicationLifecycleDriver(lifecycle, taskSubscriptionApiProvider)

  /**
   * Checks the refreshed context even if the condition on the driver missed the subscription API.
   */
  @Bean
  fun processApplicationLifecycleStartupCheck(
    lifecycle: ProcessApplicationLifecycle,
    applicationContext: ApplicationContext
  ): ApplicationRunner = object : ApplicationRunner, Ordered {

    override fun getOrder(): Int = Ordered.HIGHEST_PRECEDENCE

    override fun run(args: ApplicationArguments) {
      if (BeanFactoryUtils.beanNamesForTypeIncludingAncestors(applicationContext, TaskSubscriptionApi::class.java).isEmpty()) {
        return
      }

      val phase = lifecycle.getPhase()
      check(phase in ProcessApplicationPhase.RUNNING until ProcessApplicationPhase.STOPPING) {
        val phaseName = ProcessApplicationPhase.getName(phase)?.let { "$it ($phase)" } ?: "custom phase ($phase)"
        "Process application lifecycle must reach RUNNING before application readiness when a TaskSubscriptionApi is present. " +
          "The current phase is $phaseName. Check that the lifecycle driver is active and that the adapter's " +
          "auto-configuration runs before ProcessApplicationLifecycleAutoConfiguration."
      }
    }
  }
}
