package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.Empty
import dev.bpmcrafters.processengineapi.MetaInfo
import dev.bpmcrafters.processengineapi.MetaInfoAware
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleContext
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycleParticipant
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
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
import java.util.concurrent.CompletableFuture

internal class SpringProcessApplicationLifecycleIntegrationTest {

  @Test
  fun `starts and stops participants in integer phase order`() {
    val context = SpringApplicationBuilder(TestApplication::class.java)
      .web(WebApplicationType.NONE)
      .logStartupInfo(false)
      .run()

    val recorder = context.getBean<PhaseRecorder>()
    assertThat(recorder.entries).containsExactly(
      "before-infrastructure",
      "infrastructure",
      "before-registration",
      "registration",
      "before-subscriptions-registered",
      "subscriptions-registered",
      "before-running",
      "running"
    )

    context.close()

    assertThat(recorder.entries).containsExactly(
      "before-infrastructure",
      "infrastructure",
      "before-registration",
      "registration",
      "before-subscriptions-registered",
      "subscriptions-registered",
      "before-running",
      "running",
      "before-stopping",
      "stopping",
      "before-stopped",
      "stopped"
    )
  }

  @SpringBootApplication
  class TestApplication {

    @Bean
    fun taskSubscriptionApi(): TaskSubscriptionApi = TestTaskSubscriptionApi()

    @Bean
    fun phaseRecorder(lifecycle: ProcessApplicationLifecycle): PhaseRecorder = PhaseRecorder(lifecycle)
  }

  class PhaseRecorder(lifecycle: ProcessApplicationLifecycle) {

    val entries = mutableListOf<String>()

    init {
      register(lifecycle, ProcessApplicationPhase.NOT_STARTED + 1, "before-infrastructure")
      register(lifecycle, ProcessApplicationPhase.INFRASTRUCTURE_READY, "infrastructure")
      register(lifecycle, ProcessApplicationPhase.INFRASTRUCTURE_READY + 1, "before-registration")
      register(lifecycle, ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION, "registration")
      register(lifecycle, ProcessApplicationPhase.SUBSCRIPTION_REGISTRATION + 1, "before-subscriptions-registered")
      register(lifecycle, ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED, "subscriptions-registered")
      register(lifecycle, ProcessApplicationPhase.SUBSCRIPTIONS_REGISTERED + 1, "before-running")
      register(lifecycle, ProcessApplicationPhase.RUNNING, "running")
      register(lifecycle, ProcessApplicationPhase.RUNNING + 1, "before-stopping")
      register(lifecycle, ProcessApplicationPhase.STOPPING, "stopping")
      register(lifecycle, ProcessApplicationPhase.STOPPING + 1, "before-stopped")
      register(lifecycle, ProcessApplicationPhase.STOPPED, "stopped")
    }

    private fun register(lifecycle: ProcessApplicationLifecycle, phase: Int, entry: String) {
      lifecycle.register(object : ProcessApplicationLifecycleParticipant {
        override fun getPhase(): Int = phase

        override fun onPhase(context: ProcessApplicationLifecycleContext) {
          entries += entry
        }
      })
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
