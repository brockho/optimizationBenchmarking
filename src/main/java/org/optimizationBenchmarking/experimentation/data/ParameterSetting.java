package org.optimizationBenchmarking.experimentation.data;

/** a concrete parameter setting */
public final class ParameterSetting extends
    _PropertySetting<ParameterValue, Parameter> {

  /** the serial version uid */
  private static final long serialVersionUID = 1l;

  /**
   * create
   * 
   * @param params
   *          the parameter values
   * @param isGeneral
   *          is this setting generalized?
   */
  ParameterSetting(final _PropertyValue<?>[] params,
      final boolean isGeneral) {
    super(params, isGeneral);
  }

}
