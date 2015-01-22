package test.junit.org.optimizationBenchmarking.experimentation.evaluation.data;

import java.util.Random;

import org.optimizationBenchmarking.experimentation.evaluation.data.DimensionContext;
import org.optimizationBenchmarking.experimentation.evaluation.data.EDimensionDirection;
import org.optimizationBenchmarking.experimentation.evaluation.data.EDimensionType;
import org.optimizationBenchmarking.experimentation.evaluation.data.ExperimentSetContext;
import org.optimizationBenchmarking.utils.math.random.RandomUtils;
import org.optimizationBenchmarking.utils.parsers.DoubleParser;
import org.optimizationBenchmarking.utils.parsers.LongParser;

/** A class for creating experiment sets */
public class ExperimentSetCreatorExample2Random extends
    ExperimentSetCreatorRandom {

  /** create */
  public ExperimentSetCreatorExample2Random() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  void _createDimensionSet(final ExperimentSetContext dsc, final Random r) {

    do {
      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(LongParser.INSTANCE);
        dc.setType(EDimensionType.ITERATION_FE);
        dc.setDirection(EDimensionDirection.INCREASING_STRICTLY);
      }

      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(LongParser.INSTANCE);
        dc.setType(EDimensionType.ITERATION_SUB_FE);
        dc.setDirection(EDimensionDirection.DECREASING);
      }

      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(LongParser.INSTANCE);
        dc.setType(EDimensionType.RUNTIME_CPU);
        dc.setDirection(EDimensionDirection.INCREASING);
      }

      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(DoubleParser.INSTANCE);
        dc.setType(EDimensionType.RUNTIME_NORMALIZED);
        dc.setDirection(EDimensionDirection.INCREASING);
      }

      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(LongParser.INSTANCE);
        dc.setType(EDimensionType.QUALITY_PROBLEM_DEPENDENT);
        dc.setDirection(EDimensionDirection.DECREASING);
      }

      try (DimensionContext dc = dsc.createDimension()) {
        dc.setName(RandomUtils.longToString(
            ExperimentSetCreatorRandom.NAMING, this.m_v++));
        dc.setParser(DoubleParser.INSTANCE);
        dc.setType(EDimensionType.QUALITY_PROBLEM_INDEPENDENT);
        dc.setDirection(EDimensionDirection.INCREASING);
      }
    } while (r.nextBoolean());
  }

}