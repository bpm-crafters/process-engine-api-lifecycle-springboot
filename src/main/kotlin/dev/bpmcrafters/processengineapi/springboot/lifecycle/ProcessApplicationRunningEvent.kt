package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published when normal process application delivery is enabled.
 *
 * @since 2026.11.1
 */
class ProcessApplicationRunningEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.RUNNING)
