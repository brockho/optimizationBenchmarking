package org.optimizationBenchmarking.utils.config;

import org.optimizationBenchmarking.utils.collections.maps.ObjectMapEntry;

/**
 * A map entry for {@link java.lang.Object} keys and values
 */
final class _ConfigMapEntry extends ObjectMapEntry<String, Object> {
  /** the serial version uid */
  private static final long serialVersionUID = 1L;

  /** @serial the entry's lock state */
  volatile int m_state;

  /** instantiate */
  _ConfigMapEntry() {
    super();
  }
}