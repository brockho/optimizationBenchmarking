package org.optimizationBenchmarking.utils.tools.impl.process;

import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.ErrorUtils;

/**
 * A thread shoveling data from an {@link java.io.InputStream} to the
 * Nirvana, by {@link java.io.InputStream#skip(long) skipping} over it as
 * long as <code>{@link #m_mode}&leq;1</code>. As soon as
 * <code>{@link #m_mode}=2</code>, it will cease all activity.
 */
final class _DiscardInputStream extends _WorkerThread {

  /** the source */
  private final InputStream m_source;

  /**
   * create
   * 
   * @param source
   *          the source
   * @param log
   *          the logger
   */
  _DiscardInputStream(final InputStream source, final Logger log) {
    super("Discard-InputStream", log); //$NON-NLS-1$
    this.m_source = source;
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    byte[] buffer;
    try {
      buffer = new byte[4096];
      while (this.m_mode < 2) {
        if (this.m_source.read(buffer) <= 0) {
          break;
        }
      }
      buffer = null;
    } catch (final Throwable t) {
      if ((this.m_log != null) && (this.m_log.isLoggable(Level.SEVERE))) {
        this.m_log.log(Level.SEVERE,
            "Error during discarding input stream (by skipping).", //$NON-NLS-1$
            t);
      }
      ErrorUtils.throwAsRuntimeException(t);
    }
  }
}
