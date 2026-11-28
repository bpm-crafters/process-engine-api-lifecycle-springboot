package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published when Process Engine API infrastructure is ready.
 *
 * @since 2026.11.1
 */
class ProcessApplicationInfrastructureReadyEvent :
  ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.INFRASTRUCTURE_READY)
