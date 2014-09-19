package org.optimizationBenchmarking.utils.document.impl.latex;

import org.optimizationBenchmarking.utils.document.impl.abstr.TableFooterCell;
import org.optimizationBenchmarking.utils.document.spec.TableCellDef;

/** a footer cell of a table in a LaTeX document */
final class _LaTeXTableFooterCell extends TableFooterCell {
  /**
   * Create a footer cell of a table
   * 
   * @param owner
   *          the owning row
   * @param cols
   *          the number of columns occupied by the cell
   * @param rows
   *          the number of rows occupied by the cell
   * @param def
   *          the cell definition
   */
  _LaTeXTableFooterCell(final _LaTeXTableFooterRow owner, final int cols,
      final int rows, final TableCellDef[] def) {
    super(owner, cols, rows, def);
    this.open();
  }
}
