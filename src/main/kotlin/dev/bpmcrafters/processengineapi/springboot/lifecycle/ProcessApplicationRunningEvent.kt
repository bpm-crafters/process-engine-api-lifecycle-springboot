package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published when normal process application delivery is enabled.
 *
 * @since 1.8.0
 */
class ProcessApplicationRunningEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.RUNNING)
