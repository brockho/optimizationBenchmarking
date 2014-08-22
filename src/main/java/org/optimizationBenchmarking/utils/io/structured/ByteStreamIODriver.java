package org.optimizationBenchmarking.utils.io.structured;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.ErrorUtils;
import org.optimizationBenchmarking.utils.io.encoding.StreamEncoding;
import org.optimizationBenchmarking.utils.text.TextUtils;
import org.optimizationBenchmarking.utils.text.textOutput.ITextOutput;

/**
 * A base class for stream-based experiment I/O
 * 
 * @param <S>
 *          the object to store
 * @param <L>
 *          the context object to load into
 */
public abstract class ByteStreamIODriver<S, L> extends FileIODriver<S, L> {

  /** create */
  protected ByteStreamIODriver() {
    super();
  }

  /**
   * Load an object from a stream, while writing logging information to the
   * given logger. The specified encoding {@code defaultEncoding} is
   * assumed if no encoding can automatically detected.
   * 
   * @param loadContext
   *          the object to store the load data into
   * @param stream
   *          the input stream
   * @param logger
   *          the logger for log output
   * @param defaultEncoding
   *          the expected default encoding
   * @throws IOException
   *           if I/O fails
   */
  public final void loadStream(final L loadContext,
      final InputStream stream, final Logger logger,
      final StreamEncoding<?, ?> defaultEncoding) throws IOException {

    try {
      this.doLoadStream(loadContext, stream, logger, defaultEncoding);
    } catch (final IOException t) {
      if ((logger != null) && (logger.isLoggable(Level.SEVERE))) {
        logger.log(//
            Level.SEVERE,//
            ("Error during doLoadStream of " + //$NON-NLS-1$
                TextUtils.className(this.getClass()) + '.'),//
            t);
      }
      throw t;
    }

  }

  /** {@inheritDoc} */
  @SuppressWarnings("resource")
  @Override
  protected void doLoadURL(final L loadContext, final URL url,
      final Logger logger, final StreamEncoding<?, ?> defaultEncoding)
      throws IOException {
    InputStream is;

    is = null;
    try {
      is = url.openStream();
    } catch (final Throwable tt) {
      try {
        super.doLoadURI(loadContext, url.toURI(), logger, defaultEncoding);
      } catch (final Throwable xx) {
        ErrorUtils.throwAsIOException(xx, tt);
      }
      return;
    }

    try {
      this.loadStream(loadContext, is, logger, defaultEncoding);
    } finally {
      is.close();
    }
  }

  /** {@inheritDoc} */
  @Override
  protected void doLoadURI(final L loadContext, final URI uri,
      final Logger logger, final StreamEncoding<?, ?> defaultEncoding)
      throws IOException {
    Throwable recoverable;
    URL u;

    u = null;
    recoverable = null;
    try {
      u = uri.toURL();
    } catch (final Throwable a) {
      recoverable = a;
      u = null;
    }

    if (u != null) {
      try {
        this.loadURL(loadContext, uri.toURL(), logger, defaultEncoding);
      } catch (final Throwable t) {
        ErrorUtils.throwAsIOException(t, recoverable);
      }
    } else {
      super.doLoadURI(loadContext, uri, logger, defaultEncoding);
    }
  }

  /**
   * Load an object from a stream,.
   * 
   * @param loadContext
   *          the object to store the load data into
   * @param stream
   *          the input stream
   * @throws IOException
   *           if I/O fails
   */
  public final void loadStream(final L loadContext,
      final InputStream stream) throws IOException {
    this.loadStream(loadContext, stream, null, StreamEncoding.UNKNOWN);
  }

  /**
   * Store the given object in a stream, while writing log information to
   * the given logger and using the specified {@code defaultEncoding} if
   * the file format allows to do so.
   * 
   * @param data
   *          the object
   * @param dest
   *          the destination stream
   * @param logger
   *          the logger
   * @param defaultEncoding
   *          the encoding to be used by default
   * @throws IOException
   *           if I/O fails
   */
  public final void storeStream(final S data, final OutputStream dest,
      final Logger logger, final StreamEncoding<?, ?> defaultEncoding)
      throws IOException {
    try {
      this.doStoreStream(data, dest, logger, defaultEncoding);
    } catch (final IOException t) {
      if ((logger != null) && (logger.isLoggable(Level.SEVERE))) {
        logger.log(//
            Level.SEVERE,//
            ("Error during doStoreStream of " + //$NON-NLS-1$
                TextUtils.className(this.getClass()) + '.'),//
            t);
      }
      throw t;
    }
  }

  /**
   * Store the given object in a stream.
   * 
   * @param data
   *          the object
   * @param dest
   *          the destination stream
   * @throws IOException
   *           if I/O fails
   */
  public final void storeStream(final S data, final OutputStream dest)
      throws IOException {
    this.storeStream(data, dest, null, StreamEncoding.UNKNOWN);
  }

  /**
   * Load an object from a stream, while writing logging information to the
   * given logger. The specified encoding {@code defaultEncoding} is
   * assumed if no encoding can automatically detected.
   * 
   * @param loadContext
   *          the object to store the load data into
   * @param stream
   *          the input stream
   * @param logger
   *          the logger for log output
   * @param defaultEncoding
   *          the expected default encoding
   * @throws IOException
   *           if I/O fails
   */
  protected void doLoadStream(final L loadContext,
      final InputStream stream, final Logger logger,
      final StreamEncoding<?, ?> defaultEncoding) throws IOException {
    super.doLoadStream(loadContext, null, stream, logger, defaultEncoding);
  }

  /** {@inheritDoc} */
  @Override
  protected final void doLoadStream(final L loadContext, final Path file,
      final InputStream stream, final Logger logger,
      final StreamEncoding<?, ?> defaultEncoding) throws IOException {
    this.doLoadStream(loadContext, stream, logger, defaultEncoding);
  }

  /**
   * Load an object from a set of readers, while writing logging
   * information to the given logger.
   * 
   * @param loadContext
   *          the loading context
   * @param reader
   *          the reader
   * @param logger
   *          the logger for log output
   * @throws IOException
   *           if I/O fails
   */
  protected void doLoadReader(final L loadContext,
      final BufferedReader reader, final Logger logger) throws IOException {
    super.doLoadReader(loadContext, null, reader, logger);
  }

  /** {@inheritDoc} */
  @Override
  protected final void doLoadReader(final L loadContext, final Path file,
      final BufferedReader reader, final Logger logger) throws IOException {
    this.doLoadReader(loadContext, reader, logger);
  }

  /**
   * Store the given object in a stream, while writing log information to
   * the given logger and using the specified {@code defaultEncoding} if
   * the file format allows to do so.
   * 
   * @param data
   *          the object
   * @param stream
   *          the destination stream
   * @param logger
   *          the logger
   * @param defaultEncoding
   *          the encoding to be used by default
   * @throws IOException
   *           if I/O fails
   */
  protected void doStoreStream(final S data, final OutputStream stream,
      final Logger logger, final StreamEncoding<?, ?> defaultEncoding)
      throws IOException {
    super.doStoreStream(data, null, stream, logger, defaultEncoding);
  }

  /** {@inheritDoc} */
  @Override
  protected final void doStoreStream(final S data, final Path file,
      final OutputStream stream, final Logger logger,
      final StreamEncoding<?, ?> defaultEncoding) throws IOException {
    this.doStoreStream(data, stream, logger, defaultEncoding);
  }

  /**
   * Store the given object in a
   * {@link org.optimizationBenchmarking.utils.text.textOutput.ITextOutput
   * text output}, while writing log information to the given logger.
   * 
   * @param data
   *          the object
   * @param textOut
   *          the
   *          {@link org.optimizationBenchmarking.utils.text.textOutput.ITextOutput
   *          text output}
   * @param logger
   *          the logger
   */
  protected void doStoreText(final S data, final ITextOutput textOut,
      final Logger logger) {
    super.doStoreText(data, null, textOut, logger);
  }

  /** {@inheritDoc} */
  @Override
  protected final void doStoreText(final S data, final Path file,
      final ITextOutput text, final Logger logger) {
    this.doStoreText(data, text, logger);
  }

}
