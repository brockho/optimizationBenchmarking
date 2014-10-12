package org.optimizationBenchmarking.utils.document.spec;

/**
 * A section container. This interface is must support parallel invocation
 * of {@link #section(ILabel)} by definition.
 */
public interface ISectionContainer extends IDocumentElement {
  /**
   * Create a new section.
   * 
   * @param useLabel
   *          a label to use for the table,
   *          {@link org.optimizationBenchmarking.utils.document.spec.ELabelType#AUTO}
   *          if a label should be created, or {@code null} if this
   *          component should not be labeled
   * @return the new section
   */
  public abstract ISection section(final ILabel useLabel);
}
