package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessEngineApiLifecycleEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessApplicationInfrastructureReadyEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessApplicationRunningEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessApplicationStoppedEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessApplicationStoppingEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.TaskSubscriptionRegistrationEvent
import dev.bpmcrafters.processengineapi.springboot.lifecycle.TaskSubscriptionsRegisteredEvent
import dev.bpmcrafters.processengineapi.task.SubscribeForTaskCmd
import dev.bpmcrafters.processengineapi.task.TaskSubscription
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import dev.bpmcrafters.processengineapi.task.UnsubscribeFromTaskCmd
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.getBean
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.event.EventListener
import java.util.concurrent.CompletableFuture

internal class SpringProcessApplicationLifecycleEventsIntegrationTest {

  @Test
  fun `publishes lifecycle events in phase order`() {
    val context = SpringApplicationBuilder(TestApplication::class.java)
      .web(WebApplicationType.NONE)
      .logStartupInfo(false)
      .run()

    val recorder = context.getBean<LifecycleEventRecorder>()
    assertThat(recorder.events.map { it.javaClass.simpleName }).containsExactly(
      ProcessApplicationInfrastructureReadyEvent::class.java.simpleName,
      TaskSubscriptionRegistrationEvent::class.java.simpleName,
      TaskSubscriptionsRegisteredEvent::class.java.simpleName,
      ProcessApplicationRunningEvent::class.java.simpleName
    )
    assertThat(recorder.events.map { it.getPhase() }).containsExactly(
      ProcessApplicationPhase.INFRASTRUCTURE_READY,
      ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION,
      ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED,
      ProcessApplicationPhase.RUNNING
    )
    assertThat(recorder.runningEvents.map { it.getPhase() })
      .containsExactly(ProcessApplicationPhase.RUNNING)

    context.close()

    assertThat(recorder.events.map { it.javaClass.simpleName }).containsExactly(
      ProcessApplicationInfrastructureReadyEvent::class.java.simpleName,
      TaskSubscriptionRegistrationEvent::class.java.simpleName,
      TaskSubscriptionsRegisteredEvent::class.java.simpleName,
      ProcessApplicationRunningEvent::class.java.simpleName,
      ProcessApplicationStoppingEvent::class.java.simpleName,
      ProcessApplicationStoppedEvent::class.java.simpleName
    )
    assertThat(recorder.events.map { it.getPhase() }).containsExactly(
      ProcessApplicationPhase.INFRASTRUCTURE_READY,
      ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION,
      ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED,
      ProcessApplicationPhase.RUNNING,
      ProcessApplicationPhase.STOPPING,
      ProcessApplicationPhase.STOPPED
    )
  }

  @SpringBootApplication
  class TestApplication {

    @Bean
    fun taskSubscriptionApi(): TaskSubscriptionApi = TestTaskSubscriptionApi()

    @Bean
    fun lifecycleEventRecorder(): LifecycleEventRecorder = LifecycleEventRecorder()
  }

  class LifecycleEventRecorder {

    val events = mutableListOf<ProcessEngineApiLifecycleEvent>()
    val runningEvents = mutableListOf<ProcessEngineApiLifecycleEvent>()

    @EventListener
    fun record(event: ProcessEngineApiLifecycleEvent) {
      events += event
    }

    @EventListener(condition = "#event.phase == T(dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase).RUNNING")
    fun recordRunning(event: ProcessEngineApiLifecycleEvent) {
      runningEvents += event
    }
  }

  class TestTaskSubscriptionApi : TaskSubscriptionApi {

    override fun subscribeForTask(cmd: SubscribeForTaskCmd): CompletableFuture<TaskSubscription> =
      CompletableFuture.failedFuture(UnsupportedOperationException())

    override fun unsubscribe(cmd: UnsubscribeFromTaskCmd): CompletableFuture<Empty> =
      CompletableFuture.failedFuture(UnsupportedOperationException())

    override fun meta(instance: MetaInfoAware): MetaInfo = object : MetaInfo {}

    override fun getSupportedRestrictions(): Set<String> = emptySet()
  }
}
