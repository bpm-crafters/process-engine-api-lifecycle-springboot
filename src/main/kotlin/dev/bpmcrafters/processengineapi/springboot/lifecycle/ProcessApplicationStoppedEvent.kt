package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published after process application resources have been released.
 *
 * @since 2026.11.1
 */
class ProcessApplicationStoppedEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.STOPPED)
