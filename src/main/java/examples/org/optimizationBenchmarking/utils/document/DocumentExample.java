package examples.org.optimizationBenchmarking.utils.document;

import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.document.spec.IDocument;

/**
 * The base class for document examples.
 */
public abstract class DocumentExample implements Runnable {

  /** the document */
  final IDocument m_doc;

  /**
   * create
   * 
   * @param doc
   *          the document
   */
  public DocumentExample(final IDocument doc) {
    super();
    this.m_doc = doc;
  }

  /**
   * get the logger
   * 
   * @return the logger
   */
  static final Logger _getLogger() {
    final Filter f;
    Logger ret, c;
    Handler[] hh;

    f = new Filter() {
      @Override
      public final boolean isLoggable(final LogRecord record) {
        return (!(record.getSourceClassName().startsWith("sun."))); //$NON-NLS-1$
      }
    };

    ret = Logger.getGlobal();
    for (c = ret; c != null; c = c.getParent()) {
      c.setLevel(Level.ALL);
      c.setFilter(f);
      hh = c.getHandlers();
      if (hh != null) {
        for (final Handler h : hh) {
          h.setFilter(f);
          h.setLevel(Level.ALL);
        }
      }
    }
    return ret;
  }
}
