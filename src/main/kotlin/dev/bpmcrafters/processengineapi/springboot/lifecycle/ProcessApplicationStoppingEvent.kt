package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published before process application resources are released.
 *
 * @since 1.8.0
 */
class ProcessApplicationStoppingEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.STOPPING)
