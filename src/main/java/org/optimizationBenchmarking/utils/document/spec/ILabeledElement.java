package org.optimizationBenchmarking.utils.document.spec;

/**
 * A labeled element.
 */
public interface ILabeledElement extends IDocumentPart {

  /**
   * Obtain the label of this element, or {@code null} if the element is
   * not labeled
   * 
   * @return the label of this element, or {@code null} if the element is
   *         not labeled
   */
  public abstract ILabel getLabel();

}
