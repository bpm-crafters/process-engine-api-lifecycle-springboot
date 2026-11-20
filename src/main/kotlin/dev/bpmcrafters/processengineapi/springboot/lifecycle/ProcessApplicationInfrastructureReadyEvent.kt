package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published when Process Engine API infrastructure is ready.
 *
 * @since 1.8.0
 */
class ProcessApplicationInfrastructureReadyEvent :
  ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.INFRASTRUCTURE_READY)
