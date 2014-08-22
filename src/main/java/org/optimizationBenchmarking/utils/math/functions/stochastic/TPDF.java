package org.optimizationBenchmarking.utils.math.functions.stochastic;

import org.optimizationBenchmarking.utils.math.functions.BinaryFunction;
import org.optimizationBenchmarking.utils.math.functions.arithmetic.Absolute;

/**
 * <p>
 * The PDF of the t-distribution.
 * </p>
 */
public final class TPDF extends BinaryFunction {

  /** the serial version uid */
  private static final long serialVersionUID = 1L;

  /** the globally shared instance */
  public static final TPDF INSTANCE = new TPDF();

  /** instantiate */
  private TPDF() {
    super();
  }

  /**
   * Returns the PDF of the t-distribution
   * 
   * @param dof
   *          degrees of freedom
   * @param x
   *          x-value
   * @return The sum of the first k terms of the t distribution.
   */

  public static final double tPDF(final long dof, final double x) {
    if (x <= Double.NEGATIVE_INFINITY) {
      return 0d;
    }
    if (x >= Double.POSITIVE_INFINITY) {
      return 0d;
    }

    return TCDF._distribution(dof).density(x);
  }

  /** {@inheritDoc} */
  @Override
  public final double compute(final double x1, final double x2) {
    return TPDF.tPDF(Math.round(x1), x2);
  }

  // default, automatic serialization replacement and resolve routines for
  // singletons
  //
  /**
   * Write replace: the instance this method is invoked on will be replaced
   * with the singleton instance {@link #INSTANCE} for serialization, i.e.,
   * when the instance is written with
   * {@link java.io.ObjectOutputStream#writeObject(Object)}.
   * 
   * @return the replacement instance (always {@link #INSTANCE})
   */
  private final Object writeReplace() {
    return Absolute.INSTANCE;
  }

  /**
   * Read resolve: The instance this method is invoked on will be replaced
   * with the singleton instance {@link #INSTANCE} after serialization,
   * i.e., when the instance is read with
   * {@link java.io.ObjectInputStream#readObject()}.
   * 
   * @return the replacement instance (always {@link #INSTANCE})
   */
  private final Object readResolve() {
    return Absolute.INSTANCE;
  }
}
