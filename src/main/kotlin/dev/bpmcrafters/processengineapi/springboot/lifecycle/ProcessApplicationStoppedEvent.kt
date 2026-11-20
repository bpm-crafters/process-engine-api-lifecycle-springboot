package dev.bpmcrafters.processengineapi.springboot.lifecycle

import dev.bpmcrafters.processengineapi.lifecycle.ProcessApplicationPhase

/**
 * Published after process application resources have been released.
 *
 * @since 1.8.0
 */
class ProcessApplicationStoppedEvent : ProcessEngineApiLifecycleEvent(ProcessApplicationPhase.STOPPED)
