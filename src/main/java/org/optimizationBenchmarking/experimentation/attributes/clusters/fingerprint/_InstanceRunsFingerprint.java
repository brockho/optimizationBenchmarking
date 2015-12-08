package org.optimizationBenchmarking.experimentation.attributes.clusters.fingerprint;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.optimizationBenchmarking.experimentation.data.spec.Attribute;
import org.optimizationBenchmarking.experimentation.data.spec.EAttributeType;
import org.optimizationBenchmarking.experimentation.data.spec.IDataPoint;
import org.optimizationBenchmarking.experimentation.data.spec.IDimension;
import org.optimizationBenchmarking.experimentation.data.spec.IInstanceRuns;
import org.optimizationBenchmarking.experimentation.data.spec.IRun;
import org.optimizationBenchmarking.utils.collections.lists.ArrayListView;
import org.optimizationBenchmarking.utils.math.matrix.IMatrix;
import org.optimizationBenchmarking.utils.math.matrix.impl.DoubleMatrix1D;
import org.optimizationBenchmarking.utils.math.matrix.impl.MatrixBuilder;
import org.optimizationBenchmarking.utils.ml.fitting.impl.lssimplex.LSSimplexFitter;
import org.optimizationBenchmarking.utils.ml.fitting.models.LogisticModelOverLogX;

/**
 * <p>
 * An attribute to compute fingerprints from instance run sets. A
 * fingerprint is a row matrix which contains data describing the
 * performance of an algorithm setup on the given instance. In order to
 * obtain such data, we fit model functions to the algorithm, then use the
 * fitted parameters as data.
 * </p>
 */
final class _InstanceRunsFingerprint
    extends Attribute<IInstanceRuns, IMatrix> {

  /** the globally shared instance */
  static final _InstanceRunsFingerprint INSTANCE = new _InstanceRunsFingerprint();

  /** create */
  private _InstanceRunsFingerprint() {
    super(EAttributeType.TEMPORARILY_STORED);
  }

  /**
   * Append the parameters of a curve fitted to the given data
   *
   * @param dimensions
   *          the dimensions
   * @param data
   *          the data
   * @param builder
   *          the builder
   * @param logger
   *          the logger
   */
  private static final void __appendCurves(final IInstanceRuns data,
      final ArrayListView<? extends IDimension> dimensions,
      final MatrixBuilder builder, final Logger logger) {
    final int size;
    final LogisticModelOverLogX model;
    final double[] rawPoints;
    final DoubleMatrix1D rawMatrix;
    IDimension dimensionA, dimensionB, tempDim;
    ArrayListView<? extends IRun> runs;
    double[] fittingResult;
    int dimA, dimB, useDimA, useDimB, totalPoints, index;
    boolean isTimeA, isTimeB;

    model = new LogisticModelOverLogX();

    // We allocate a re-usable data store.
    totalPoints = 0;
    for (final IRun run : data.getData()) {
      totalPoints += run.getData().size();
    }
    rawPoints = new double[totalPoints << 1];
    rawMatrix = new DoubleMatrix1D(rawPoints, totalPoints, 2);

    size = dimensions.size();
    for (dimA = 0; dimA < size; dimA++) {
      dimensionA = dimensions.get(dimA);
      isTimeA = dimensionA.getDimensionType().isTimeMeasure();

      for (dimB = dimA; (++dimB) < size;) {
        dimensionB = dimensions.get(dimB);
        isTimeB = dimensionB.getDimensionType().isTimeMeasure();

        // We ignore all time-time and objective-objective relationships.
        // Since we catch all time-objective relationships, they are
        // implicitly contained.
        if (isTimeB == isTimeA) {
          continue;
        }

        // we prefer time-quality over quality-time
        if (isTimeB && (!isTimeA)) {
          isTimeB = false;
          useDimA = dimB;
          useDimB = dimA;
          tempDim = dimensionA;
          dimensionA = dimensionB;
          dimensionB = tempDim;
        } else {
          useDimA = dimA;
          useDimB = dimB;
        }

        runs = data.getData();

        index = 0;
        for (final IRun run : runs) {
          for (final IDataPoint point : run.getData()) {
            rawPoints[index++] = point.getDouble(useDimA);
            rawPoints[index++] = point.getDouble(useDimB);
          }
        }

        if ((logger != null) && (logger.isLoggable(Level.FINEST))) {
          logger.finest("Fitting model '" + //$NON-NLS-1$
              model.getClass().getSimpleName() + "' to dimensions '" + //$NON-NLS-1$
              dimensionA.getName() + "' (input) and '" + //$NON-NLS-1$
              dimensionB.getName() + "' (output)" + //$NON-NLS-1$
              _InstanceRunsFingerprint.__instanceRunsName(data));
        }

        // get all the points
        fittingResult = LSSimplexFitter.getInstance().use()
            .setFunctionToFit(model).setPoints(rawMatrix).setLogger(logger)
            .create().call().getFittedParametersRef();

        builder.append(fittingResult);
      }
    }
  }

  /**
   * get the name of an instance runs set
   *
   * @param data
   *          the data
   * @return the name
   */
  private static final String __instanceRunsName(
      final IInstanceRuns data) {
    return ((((" for runs of experiment '" + //$NON-NLS-1$
        data.getOwner().getName()) + "' on instance '") + //$NON-NLS-1$
        data.getInstance().getName()) + '\'' + '.');
  }

  /** Compute the attribute */
  @Override
  protected final IMatrix compute(final IInstanceRuns data,
      final Logger logger) {
    final MatrixBuilder builder;
    final IMatrix result;
    final ArrayListView<? extends IDimension> dimensions;

    if ((logger != null) && (logger.isLoggable(Level.FINER))) {
      logger.finer("Beginning to compute fingerprint" + //$NON-NLS-1$
          _InstanceRunsFingerprint.__instanceRunsName(data));
    }

    builder = new MatrixBuilder();
    builder.setM(1);

    dimensions = data.getOwner().getOwner().getDimensions().getData();
    _InstanceRunsFingerprint.__appendCurves(data, dimensions, builder,
        logger);
    result = builder.make();

    if ((logger != null) && (logger.isLoggable(Level.FINER))) {
      logger.finer((((("Computed an " + //$NON-NLS-1$
          result.m()) + '*') + result.n()) + //
          " matrix as fingerprint") + //$NON-NLS-1$
          _InstanceRunsFingerprint.__instanceRunsName(data));
    }

    return result;
  }
}