package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.impl.lifecycle.DefaultProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean

/**
 * Registers the Spring Boot bridge for the Process Engine API lifecycle.
 *
 * @since 1.8.0
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
}
