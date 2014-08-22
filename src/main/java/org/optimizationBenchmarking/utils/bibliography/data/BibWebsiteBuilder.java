package org.optimizationBenchmarking.utils.bibliography.data;

import org.optimizationBenchmarking.utils.hierarchy.HierarchicalFSM;

/** A builder for website objects. */
public final class BibWebsiteBuilder extends BibRecordWithPublisherBuilder {

  /** create the website builder */
  public BibWebsiteBuilder() {
    this(null);
  }

  /**
   * create the website builder
   * 
   * @param owner
   *          the owner
   */
  BibWebsiteBuilder(final HierarchicalFSM owner) {
    super(owner);
    this.open();
  }

  /** {@inheritDoc} */
  @Override
  public final synchronized BibDateBuilder setDate() {
    return super.setDate();
  }

  /** {@inheritDoc} */
  @Override
  public final synchronized void setDate(final BibDate date) {
    super.setDate(date);
  }

  /** {@inheritDoc} */
  @Override
  final BibWebsite _compile() {
    this.fsmFlagsAssertTrue(BibRecordBuilder.FLAG_URL_SET
        | BibRecordBuilder.FLAG_TITLE_SET | BibRecordBuilder.FLAG_DATE_SET);

    return new BibWebsite(true, this.m_authors, this.m_title, this.m_date,
        this.m_publisher, this.m_url, this.m_doi);
  }

  /** {@inheritDoc} */
  @Override
  public final synchronized BibWebsite getResult() {
    return ((BibWebsite) (super.getResult()));
  }
}
