package org.optimizationBenchmarking.utils.bibliography.data;

import org.optimizationBenchmarking.utils.collections.lists.ArraySetView;

/**
 * An enumeration of quarters.
 */
public enum EBibQuarter {

  /** spring */
  SPRING("spr", "Spring"), //$NON-NLS-1$//$NON-NLS-2$)
  /** summer */
  SUMMER("sum", "Summer"), //$NON-NLS-1$//$NON-NLS-2$)
  /** fall */
  FALL("fal", "Fall"), //$NON-NLS-1$//$NON-NLS-2$)
  /** winter */
  WINTER("win", "Winter"); //$NON-NLS-1$//$NON-NLS-2$)

  /** the serial version uid */
  private static final long serialVersionUID = 1L;

  /** the values */
  public static final ArraySetView<EBibQuarter> QUARTERS = new ArraySetView<>(
      EBibQuarter.values());

  /** the shortcut */
  final String m_short;

  /** the shortcut */
  final String m_full;

  /**
   * create
   *
   * @param sh
   *          the short name
   * @param lo
   *          the long name
   */
  EBibQuarter(final String sh, final String lo) {
    this.m_short = sh;
    this.m_full = lo;
  }

  /**
   * Get the short name
   *
   * @return the short name
   */
  public final String getShortName() {
    return this.m_short;
  }

  /**
   * Get the long name
   *
   * @return the long name
   */
  public final String getLongName() {
    return this.m_full;
  }
}
