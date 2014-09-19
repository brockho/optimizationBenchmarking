package org.optimizationBenchmarking.utils.document.impl.latex;

import org.optimizationBenchmarking.utils.document.impl.abstr.Figure;
import org.optimizationBenchmarking.utils.document.spec.EFigureSize;
import org.optimizationBenchmarking.utils.document.spec.ILabel;

/** a figure in a LaTeX document */
final class _LaTeXFigure extends Figure {
  /**
   * Create a new figure
   * 
   * @param owner
   *          the owning section body
   * @param index
   *          the figure index in the owning section
   * @param useLabel
   *          the label to use
   * @param size
   *          the figure size
   * @param path
   *          the path suggestion
   */
  public _LaTeXFigure(final _LaTeXSectionBody owner,
      final ILabel useLabel, final EFigureSize size, final String path,
      final int index) {
    super(owner, useLabel, size, path, index);
    this.open();
  }
}
