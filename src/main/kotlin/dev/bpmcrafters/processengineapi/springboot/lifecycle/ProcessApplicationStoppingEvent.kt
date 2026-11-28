package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published before process application resources are released.
 *
 * @since 2026.11.1
 */
class ProcessApplicationStoppingEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.STOPPING)
