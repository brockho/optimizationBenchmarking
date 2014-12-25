package org.optimizationBenchmarking.utils.io.structured.spec;

/**
 * A tool for reading file input
 * 
 * @param <S>
 *          the destination data type
 */
public interface IFileInputTool<S> extends IIOTool {

  /** {@inheritDoc} */
  @Override
  public abstract IFileInputJobBuilder<S> use();
}
