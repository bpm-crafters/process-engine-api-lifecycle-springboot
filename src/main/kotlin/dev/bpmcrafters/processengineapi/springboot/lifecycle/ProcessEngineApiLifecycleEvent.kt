package dev.bpmcrafters.processengineapi.springboot.lifecycle

/**
 * Marker for Spring events projected from a Process Engine API lifecycle phase.
 *
 * @since 1.8.0
 */
abstract class ProcessEngineApiLifecycleEvent(
  private val phase: Int
) {

  /**
   * Returns the lifecycle phase represented by this event.
   */
  fun getPhase(): Int = phase
}
