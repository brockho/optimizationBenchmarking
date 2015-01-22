package org.optimizationBenchmarking.experimentation.evaluation.data;

/**
 * A parameter value.
 */
public final class ParameterValue extends _PropertyValue<Parameter> {
  /** the serial version uid */
  private static final long serialVersionUID = 1L;

  /**
   * Create the parameter value
   * 
   * @param name
   *          the string representation of this value
   * @param desc
   *          the description
   * @param value
   *          the value
   */
  ParameterValue(final String name, final String desc, final Object value) {
    super(name, desc, value);
  }

  /**
   * Is this parameter value unspecified?
   * 
   * @return {@code true} if and only if
   *         <code>this.{@link #getValue() getValue()}=={@link org.optimizationBenchmarking.experimentation.evaluation.data._PropertyValueUnspecified#INSTANCE}</code>
   *         , {@code false} otherwise
   */
  public final boolean isUnspecified() {
    return (this.m_value == _PropertyValueUnspecified.INSTANCE);
  }
}