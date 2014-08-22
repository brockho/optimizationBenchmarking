package org.optimizationBenchmarking.utils.document.spec;

/**
 * The root interface for the document output API.
 */
public interface IDocument extends IStyleContext {

  /**
   * create the document header
   * 
   * @return the document header
   */
  public abstract IDocumentHeader header();

  /**
   * create the document body
   * 
   * @return the document body
   */
  public abstract IDocumentBody body();

  /**
   * create the document footer
   * 
   * @return the document footer
   */
  public abstract IDocumentFooter footer();

  /**
   * Create a new label to mark a table or figure or section with that is
   * going to be written in the future.
   * 
   * @param type
   *          the label type
   * @return the label to be used in forward references
   */
  public abstract ILabel createLabel(final ELabelType type);
}
