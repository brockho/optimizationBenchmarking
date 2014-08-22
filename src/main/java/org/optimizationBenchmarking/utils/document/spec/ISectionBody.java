package org.optimizationBenchmarking.utils.document.spec;

import java.net.URI;

/**
 * The basic interface for blocks of texts.
 */
public interface ISectionBody extends IStructuredText, ISectionContainer {

  /**
   * Create a table in this block of text.
   * 
   * @param useLabel
   *          a label to use for the table,
   *          {@link org.optimizationBenchmarking.utils.document.spec.ELabelType#AUTO}
   *          if a label should be created, or {@code null} if this
   *          component should not be labeled
   * @param spansAllColumns
   *          will the table span all columns ({@code true}) or not?
   * @param cells
   *          the table cell definitions
   * @return the table object
   */
  public abstract ITable table(final ILabel useLabel,
      final boolean spansAllColumns, final TableCellDef... cells);

  /**
   * Create a figure in this block of text.
   * 
   * @param useLabel
   *          a label to use for the figure,
   *          {@link org.optimizationBenchmarking.utils.document.spec.ELabelType#AUTO}
   *          if a label should be created, or {@code null} if this
   *          component should not be labeled
   * @param size
   *          the figure size
   * @param path
   *          a relative path used for creating an output path to store the
   *          document under
   * @return the figure object
   */
  public abstract IFigure figure(final ILabel useLabel,
      final EFigureSize size, final URI path);

  /**
   * Create a series of figures in this block of text.
   * 
   * @param useLabel
   *          a label to use for the figure,
   *          {@link org.optimizationBenchmarking.utils.document.spec.ELabelType#AUTO}
   *          if a label should be created, or {@code null} if this
   *          component should not be labeled
   * @param size
   *          the size of the sub-figures
   * @param path
   *          a relative path used for creating an output path to store the
   *          document under
   * @return the figure series
   */
  public abstract IFigureSeries figureSeries(final ILabel useLabel,
      final EFigureSize size, final URI path);

  /**
   * Create an equation
   * 
   * @param useLabel
   *          a label to use for the equation,
   *          {@link org.optimizationBenchmarking.utils.document.spec.ELabelType#AUTO}
   *          if a label should be created, or {@code null} if this
   *          component should not be labeled
   * @return the maths context to write the equation to
   */
  public abstract IMath equation(final ILabel useLabel);

}
