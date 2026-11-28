package dev.bpmcrafters.processengineapi.springboot.startup

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationLifecycle
import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase
import dev.bpmcrafters.processengineapi.springboot.lifecycle.ProcessApplicationLifecycleAutoConfiguration
import dev.bpmcrafters.processengineapi.task.TaskSubscriptionApi
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

internal class SpringProcessApplicationLifecycleStartupTest {

  @Test
  fun `fails startup when the subscription API is present but the lifecycle driver was skipped`() {
    assertThatExceptionOfType(IllegalStateException::class.java)
      .isThrownBy {
        SpringApplicationBuilder(LateAdapterApplication::class.java)
          .web(WebApplicationType.NONE)
          .logStartupInfo(false)
          .run()
          .close()
      }
      .withStackTraceContaining("Process application lifecycle must reach RUNNING before application readiness")
      .withStackTraceContaining("current phase is NOT_STARTED")
  }

  @Test
  fun `starts without a subscription API`() {
    SpringApplicationBuilder(BridgeOnlyApplication::class.java)
      .web(WebApplicationType.NONE)
      .logStartupInfo(false)
      .run().use { context ->
        assertThat(context.getBean(ProcessApplicationLifecycle::class.java).getPhase())
          .isEqualTo(ProcessApplicationPhase.NOT_STARTED)
      }
  }

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration(ProcessApplicationLifecycleAutoConfiguration::class)
  class BridgeOnlyApplication

  @Configuration(proxyBeanMethods = false)
  @ImportAutoConfiguration(ProcessApplicationLifecycleAutoConfiguration::class, LateAdapterAutoConfiguration::class)
  class LateAdapterApplication

  @AutoConfiguration(after = [ProcessApplicationLifecycleAutoConfiguration::class])
  class LateAdapterAutoConfiguration {

    @Bean
    fun taskSubscriptionApi(): TaskSubscriptionApi = mock()
  }
}
