package org.optimizationBenchmarking.experimentation.attributes.functions.statisticalParameter2D;

import org.optimizationBenchmarking.experimentation.attributes.functions.FunctionAttribute;
import org.optimizationBenchmarking.experimentation.attributes.statistics.parameters.StatisticalParameter;
import org.optimizationBenchmarking.experimentation.data.spec.EAttributeType;
import org.optimizationBenchmarking.experimentation.data.spec.IDimension;
import org.optimizationBenchmarking.experimentation.data.spec.IElementSet;
import org.optimizationBenchmarking.utils.comparison.EComparison;
import org.optimizationBenchmarking.utils.document.impl.FunctionToMathBridge;
import org.optimizationBenchmarking.utils.document.spec.IMath;
import org.optimizationBenchmarking.utils.hash.HashUtils;
import org.optimizationBenchmarking.utils.math.functions.UnaryFunction;
import org.optimizationBenchmarking.utils.text.ETextCase;
import org.optimizationBenchmarking.utils.text.textOutput.ITextOutput;
import org.optimizationBenchmarking.utils.text.textOutput.MemoryTextOutput;

/**
 * the base class for 2D statistical parameter curve
 *
 * @param <T>
 *          the element set type
 */
abstract class _StatisticalParameter2DBase<T extends IElementSet> extends
    FunctionAttribute<T> {

  /** create the raw statistics parameter curve */
  _StatisticalParameter2DBase() {
    super(EAttributeType.TEMPORARILY_STORED);
  }

  /**
   * Get the dimension of the {@code x}-axis of the curves
   *
   * @return the dimension of the {@code x}-axis of the curves
   */
  public abstract IDimension getXDimension();

  /**
   * Get the transformation to be applied to the values of {@code x}-axis
   * of the curves.
   *
   * @return the transformation to be applied to the values of {@code x}
   *         -axis of the curves, or {@code null} if the values are used
   *         as-is
   */
  public abstract UnaryFunction getXTransformation();

  /**
   * Get the dimension of the {@code y}-axis of the curves
   *
   * @return the dimension of the {@code y}-axis of the curves
   */
  public abstract IDimension getYDimension();

  /**
   * Get the transformation to be applied to the values of {@code y}-axis
   * of the curves <em>before</em> the {@link #getStatisticalParameter()
   * statistical parameter} is computed.
   *
   * @return the transformation to be applied to the values of {@code y}
   *         -axis of the curves <em>before</em> the
   *         {@link #getStatisticalParameter() statistical parameter} is
   *         computed, or {@code null} if the values are used as-is
   */
  public abstract UnaryFunction getYTransformation();

  /**
   * Get the statistical parameter to be computed over the
   * {@link #getYTransformation() transformed} values of the
   * {@link #getYDimension() y}-axis.
   *
   * @return the statistical parameter to be computed over the
   *         {@link #getYTransformation() transformed} values of the
   *         {@link #getYDimension() y}-axis.
   */
  public abstract StatisticalParameter getStatisticalParameter();

  /**
   * Get the statistical parameter to be computed over the
   * {@link #getStatisticalParameter() statistics} obtained from an
   * instance run set.
   *
   * @return the statistical parameter to be computed over the
   *         {@link #getYTransformation() transformed} values of the
   *         {@link #getYDimension() y}-axis.
   */
  public abstract StatisticalParameter getSecondaryStatisticalParameter();

  /** {@inheritDoc} */
  @Override
  public String getPathComponentSuggestion() {
    final MemoryTextOutput mto;
    final StatisticalParameter second;
    UnaryFunction func;

    mto = new MemoryTextOutput();

    second = this.getSecondaryStatisticalParameter();
    if (second != null) {
      mto.append(second.getPathComponentSuggestion());
      mto.append('_');
    }
    mto.append(this.getStatisticalParameter().getPathComponentSuggestion());
    mto.append('_');
    func = this.getYTransformation();
    if (func != null) {
      mto.append(func.toString().toLowerCase());
      mto.append('_');
    }
    mto.append(this.getYDimension().getPathComponentSuggestion());
    mto.append('_');
    func = this.getXTransformation();
    if (func != null) {
      mto.append(func.toString().toLowerCase());
      mto.append('_');
    }
    mto.append(this.getXDimension().getPathComponentSuggestion());

    return mto.toString();
  }

  /** {@inheritDoc} */
  @Override
  protected ETextCase appendXAxisTitlePlain(final ITextOutput textOut,
      final ETextCase textCase) {
    final UnaryFunction func;
    ETextCase use;

    use = ETextCase.ensure(textCase);
    func = this.getXTransformation();
    if (func != null) {
      use = use.appendWord(func.toString(), textOut);
      textOut.append(' ');
    }

    return this.getXDimension().appendName(textOut, use);
  }

  /** {@inheritDoc} */
  @Override
  public void appendXAxisTitle(final IMath math) {
    try (final IMath inner = FunctionToMathBridge.bridge(
        this.getXTransformation(), math)) {
      this.getXDimension().appendName(inner);
    }
  }

  /** {@inheritDoc} */
  @Override
  protected ETextCase appendYAxisTitlePlain(final ITextOutput textOut,
      final ETextCase textCase) {
    final UnaryFunction func;
    final StatisticalParameter second;
    ETextCase use;

    use = ETextCase.ensure(textCase);

    second = this.getSecondaryStatisticalParameter();
    if (second != null) {
      use = ETextCase.ensure(second.appendName(textOut, use));
      textOut.append(' ');
    }

    use = ETextCase.ensure(this.getStatisticalParameter().appendName(
        textOut, use));
    textOut.append(' ');
    func = this.getYTransformation();
    if (func != null) {
      use = use.appendWord(func.toString(), textOut);
      textOut.append(' ');
    }

    return this.getXDimension().appendName(textOut, use);
  }

  /** {@inheritDoc} */
  @SuppressWarnings("resource")
  @Override
  public void appendYAxisTitle(final IMath math) {
    final StatisticalParameter second;
    final IMath use;

    second = this.getSecondaryStatisticalParameter();
    if (second != null) {
      use = second.asFunction(math);
    } else {
      use = math;
    }

    try (final IMath statPar = this.getStatisticalParameter().asFunction(
        use)) {
      try (final IMath inner = FunctionToMathBridge.bridge(
          this.getXTransformation(), math)) {
        this.getXDimension().appendName(inner);
      }
    }

    if (use != math) {
      use.close();
    }
  }

  /** {@inheritDoc} */
  @Override
  @SuppressWarnings("rawtypes")
  public final boolean equals(final Object o) {
    final _StatisticalParameter2DBase other;

    if (o == this) {
      return true;
    }

    if (o instanceof _StatisticalParameter2DBase) {
      other = ((_StatisticalParameter2DBase) o);

      return ((this.getXDimension().getIndex() == //
          other.getXDimension().getIndex())//
          && (this.getYDimension().getIndex() == //
          other.getYDimension().getIndex()) && //
          EComparison.equals(this.getXTransformation(),
              other.getXTransformation()) && //
          EComparison.equals(this.getYTransformation(),
              other.getYTransformation()) && //
          EComparison.equals(this.getStatisticalParameter(),
              other.getStatisticalParameter()) && //
      EComparison.equals(this.getSecondaryStatisticalParameter(),
          other.getStatisticalParameter()));
    }

    return false;
  }

  /** {@inheritDoc} */
  @Override
  protected final int calcHashCode() {
    return HashUtils.combineHashes(//
        HashUtils.combineHashes(//
            HashUtils.combineHashes(//
                HashUtils.hashCode(this.getXDimension().getIndex()),//
                HashUtils.hashCode(this.getXTransformation())),//
            HashUtils.combineHashes(//
                HashUtils.hashCode(this.getYDimension().getIndex()),//
                HashUtils.hashCode(this.getYTransformation()))),//
        HashUtils.combineHashes(//
            HashUtils.hashCode(this.getStatisticalParameter()),//
            HashUtils.hashCode(this.getSecondaryStatisticalParameter())));
  }
}
