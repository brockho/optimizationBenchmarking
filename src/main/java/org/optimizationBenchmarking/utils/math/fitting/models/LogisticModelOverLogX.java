package org.optimizationBenchmarking.utils.math.fitting.models;

import java.util.Random;

import org.optimizationBenchmarking.utils.document.spec.IMath;
import org.optimizationBenchmarking.utils.math.MathUtils;
import org.optimizationBenchmarking.utils.math.fitting.impl.SampleBasedParameterGuesser;
import org.optimizationBenchmarking.utils.math.fitting.spec.IParameterGuesser;
import org.optimizationBenchmarking.utils.math.matrix.IMatrix;
import org.optimizationBenchmarking.utils.math.text.IMathRenderable;
import org.optimizationBenchmarking.utils.math.text.IParameterRenderer;
import org.optimizationBenchmarking.utils.text.textOutput.ITextOutput;

/**
 * <p>
 * A model function which may be suitable to model how time-objective value
 * relationships of optimization processes behave. Qi Qi has discovered
 * this model: {@code a/(1+b*x^c)}.
 * </p>
 * <p>
 * It is somewhat similar to a logistic model and over a log-scaled
 * {@code x}-axis: Originally, we fitted {@code a/(1+exp(b+c*log(x)))},
 * which corresponds to {@code a/(1+exp(b)*(x^c))}, which, in turn, can be
 * simplified to {@code a/(1+b*x^c)} in the fitting procedure, since
 * {@code exp(b)} is constant with respect to {@code x} and then we can try
 * to find its value directly as well.
 * </p>
 * <p>
 * This model is also somewhat similar to my previous exponential decay
 * model {@code -(exp(a*(x^b))-1)} but seems to fit better and does not
 * require data normalization.
 * </p>
 * <h2>Derivatives</h2>
 * <p>
 * The derivatives have been obtained with http://www.numberempire.com/.
 * </p>
 * <ol>
 * <li>Original function: {@code a/(1+b*x^c)}</li>
 * <li>{@code d/da}: {@code 1/(1+b*x^c)}</li>
 * <li>{@code d/db}: {@code -(a*x^c)/         (1 + 2*b*x^c + b^2*x^(2*c))}
 * </li>
 * <li>{@code d/dc}: {@code -(a*b*x^c*log(x))/(1 + 2*b*x^c + b^2*x^(2*c))}
 * </li>
 * </ol>
 * <h2>Resolution</h2> The resolutions have been obtained with
 * http://www.numberempire.com/ and http://wolframalpha.com/.
 * <h3>One Known Point</h3>
 * <ol>
 * <li>{@code a}: {@code a=(b*x^c+1)*y}</li>
 * <li>{@code b}: {@code b=-(y-a)/(x^c*y)}</li>
 * <li>{@code c}: {@code c=log(a/(b*y)-1/b)/log(x)},
 * {@code c = (log((a-y)/(b*y)))/(log(x))}</li>
 * </ol>
 * <h3>Two Known Points</h3>
 * <ol>
 * <li>{@code a}: {@code a=((x2^c-x1^c)*y1*y2)/(x2^c*y2-x1^c*y1)}</li>
 * <li>{@code b}: {@code b=-(y2-y1)/(x2^c*y2-x1^c*y1)}</li>
 * <li>{@code b}:
 * {@code b=exp((log(x1)*log(a-y2)-log(x2)*log(a-y1)-log(x1)*log(y2)+log(x2)*log(y1))/(log(x1)-log(x2)))}
 * </li>
 * </ol>
 */
public final class LogisticModelOverLogX extends _ModelBase {

  /** create */
  public LogisticModelOverLogX() {
    super();
  }

  /** {@inheritDoc} */
  @Override
  public final double value(final double x, final double[] parameters) {
    return _ModelBase._logisticModelOverLogXCompute(x, parameters[0],
        parameters[1], parameters[2]);
  }

  /** {@inheritDoc} */
  @Override
  public final void gradient(final double x, final double[] parameters,
      final double[] gradient) {
    _ModelBase._logisticModelOverLogXGradient(x, parameters[0],
        parameters[1], parameters[2], gradient);
  }

  /** {@inheritDoc} */
  @Override
  public final int getParameterCount() {
    return 3;
  }

  /** {@inheritDoc} */
  @Override
  public final IParameterGuesser createParameterGuesser(
      final IMatrix data) {
    return new __LogisticGuesser(data);
  }

  /** {@inheritDoc} */
  @Override
  public final void canonicalizeParameters(final double[] parameters) {
    _ModelBase._logisticModelOverLogXCanonicalizeParameters(parameters);
  }

  /** {@inheritDoc} */
  @Override
  public final void mathRender(final ITextOutput out,
      final IParameterRenderer renderer, final IMathRenderable x) {
    _ModelBase._logisticModelOverLogXMathRender(out, renderer, x);
  }

  /** {@inheritDoc} */
  @Override
  public final void mathRender(final IMath out,
      final IParameterRenderer renderer, final IMathRenderable x) {
    _ModelBase._logisticModelOverLogXMathRender(out, renderer, x);
  }

  /** A parameter guesser for the logistic models. */
  private static final class __LogisticGuesser
      extends SampleBasedParameterGuesser {

    /**
     * create the guesser
     *
     * @param data
     *          the data
     */
    __LogisticGuesser(final IMatrix data) {
      super(data, 3);
    }

    /**
     * compute the error of a given fitting
     *
     * @param x0
     *          the first x-coordinate
     * @param y0
     *          the first y-coordinate
     * @param x1
     *          the second x-coordinate
     * @param y1
     *          the second y-coordinate
     * @param x2
     *          the third x-coordinate
     * @param y2
     *          the third y-coordinate
     * @param a
     *          the first fitting parameter
     * @param b
     *          the second fitting parameter
     * @param c
     *          the third fitting parameter
     * @return the fitting error
     */
    private static final double __error(final double x0, final double y0,
        final double x1, final double y1, final double x2, final double y2,
        final double a, final double b, final double c) {
      return _ModelBase._add3(//
          __LogisticGuesser.__error(x0, y0, a, b, c), //
          __LogisticGuesser.__error(x1, y1, a, b, c), //
          __LogisticGuesser.__error(x2, y2, a, b, c));
    }

    /**
     * Compute the error of the fitting for a single point
     *
     * @param x
     *          the {@code x}-coordinate of the point
     * @param y
     *          the {@code y}-coordinate of the point
     * @param a
     *          the {@code a} value
     * @param b
     *          the {@code b} value
     * @param c
     *          the {@code c} value
     * @return the error
     */
    private static final double __error(final double x, final double y,
        final double a, final double b, final double c) {
      return Math.abs(//
          y - _ModelBase._logisticModelOverLogXCompute(x, a, b, c));
    }

    /**
     * Compute {@code b} from one point {@code (x,y)} and known {@code a}
     * and {@code c} values.
     *
     * @param x
     *          the {@code x}-coordinate of the point
     * @param y
     *          the {@code y}-coordinate of the point
     * @param a
     *          the {@code a} value
     * @param c
     *          the {@code c} value
     * @return the {@code b} value
     */
    private static final double __b_xyac(final double x, final double y,
        final double a, final double c) {
      final double b1, b2, b3, xc, xcy, e1, e2, e3;

      xc = _ModelBase._pow(x, c);
      xcy = (xc * y);

      b1 = ((a - y) / xcy);
      b2 = ((a / xcy) - (1d / xc));

      if (b1 == b2) {
        return b1;
      }

      e1 = __LogisticGuesser.__error(x, y, a, b1, c);
      e2 = __LogisticGuesser.__error(x, y, a, b2, c);
      if (MathUtils.isFinite(e1)) {
        if (MathUtils.isFinite(e2)) {
          if (e1 == e2) {
            b3 = (0.5d * (b1 + b2));
            if ((b3 != b2) && (b3 != b1) && ((b1 < b3) || (b2 < b3))
                && ((b3 < b1) || (b3 < b2))) {
              e3 = __LogisticGuesser.__error(x, y, a, b3, c);
              if (MathUtils.isFinite(e3) && (e3 <= e1) && (e3 <= e2)) {
                return b3;
              }
            }
          }
          return ((e1 < e2) ? b1 : b2);
        }
        return b1;
      }
      if (MathUtils.isFinite(e2)) {
        return b2;
      }
      return ((e1 < e2) ? b1 : b2);
    }

    /**
     * Compute {@code c} from one point {@code (x,y)} and known {@code a}
     * and {@code b} values.
     *
     * @param x
     *          the {@code x}-coordinate of the point
     * @param y
     *          the {@code y}-coordinate of the point
     * @param a
     *          the {@code a} value
     * @param b
     *          the {@code b} value
     * @return the {@code c} value
     */
    private static final double __c_xyab(final double x, final double y,
        final double a, final double b) {
      final double c1, c2, c3, lx, by, e1, e2, e3;

      lx = _ModelBase._log(x);
      if ((b <= (-1d)) && (0d < x) && (x < 1d) && (Math.abs(a) <= 0d)
          && (Math.abs(y) <= 0d)) {
        return Math.nextUp(Math.nextUp(//
            _ModelBase._log(-1d / b) / lx));
      }

      by = b * y;
      c1 = _ModelBase._log((a / by) - (1 / b)) / lx;
      c2 = _ModelBase._log((a - y) / by) / lx;

      if (c1 == c2) {
        return c1;
      }

      e1 = __LogisticGuesser.__error(x, y, a, b, c1);
      e2 = __LogisticGuesser.__error(x, y, a, b, c2);
      if (MathUtils.isFinite(e1)) {
        if (MathUtils.isFinite(e2)) {
          if (e1 == e2) {
            c3 = (0.5d * (c1 + c2));
            if ((c3 != c2) && (c3 != c1) && ((c1 < c3) || (c2 < c3))
                && ((c3 < c1) || (c3 < c2))) {
              e3 = __LogisticGuesser.__error(x, y, a, b, c3);
              if (MathUtils.isFinite(e3) && (e3 <= e1) && (e3 <= e2)) {
                return c3;
              }
            }
          }
          return ((e1 < e2) ? c1 : c2);
        }
        return c1;
      }
      if (MathUtils.isFinite(e2)) {
        return c2;
      }
      return ((e1 < e2) ? c1 : c2);
    }

    /**
     * Update a guess for {@code a}, {@code b}, and {@code c} by using
     * median results from all formulas
     *
     * @param x0
     *          the {@code x}-coordinate of the first point
     * @param y0
     *          the {@code y}-coordinate of the first point
     * @param x1
     *          the {@code x}-coordinate of the second point
     * @param y1
     *          the {@code y}-coordinate of the second point
     * @param x2
     *          the {@code x}-coordinate of the third point
     * @param y2
     *          the {@code y}-coordinate of the third point
     * @param maxY
     *          the maximum {@code y} coordinate
     * @param dest
     *          the destination array
     * @param bestError
     *          the best error so far
     * @return the new (or old) best error
     */
    private static final double __updateMed(final double x0,
        final double y0, final double x1, final double y1, final double x2,
        final double y2, final double maxY, final double[] dest,
        final double bestError) {
      double newA, newB, newC, error;
      boolean hasA, hasB, hasC, changed;

      newA = newB = newC = Double.NaN;
      hasA = hasB = hasC = false;

      changed = true;
      while (changed) {
        changed = false;

        if (!hasB) {
          // find B based on the existing or new A and C values
          newB = _ModelBase._med3(//
              _ModelBase._logisticModelOverLogX_b_x1y1x2y2a(x0, y0, x1, y1,
                  (hasA ? newA : dest[0])), //
              _ModelBase._logisticModelOverLogX_b_x1y1x2y2a(x1, y1, x2, y2,
                  (hasA ? newA : dest[0])), //
              _ModelBase._logisticModelOverLogX_b_x1y1x2y2a(x2, y2, x0, y0,
                  (hasA ? newA : dest[0])));

          if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
            changed = hasB = true;
          } else {
            newB = _ModelBase._med3(//
                __LogisticGuesser.__b_xyac(x0, y0, (hasA ? newA : dest[0]),
                    (hasC ? newC : dest[2])), //
                __LogisticGuesser.__b_xyac(x1, y1, (hasA ? newA : dest[0]),
                    (hasC ? newC : dest[2])), //
                __LogisticGuesser.__b_xyac(x2, y2, (hasA ? newA : dest[0]),
                    (hasC ? newC : dest[2])));

            if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
              changed = hasB = true;
            } else {
              newB = _ModelBase._med3(//
                  _ModelBase._logisticModelOverLogX_b_x1y1x2y2c(x0, y0, x1,
                      y1, (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_b_x1y1x2y2c(x1, y1, x2,
                      y2, (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_b_x1y1x2y2c(x2, y2, x0,
                      y0, (hasC ? newC : dest[2])));

              if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
                changed = hasB = true;
              }
            }
          }
        }

        if (!hasC) {
          // find C based on the existing or new A and B values
          newC = _ModelBase._med3(//
              __LogisticGuesser.__c_xyab(x0, y0, (hasA ? newA : dest[0]),
                  (hasB ? newB : dest[1])), //
              __LogisticGuesser.__c_xyab(x1, y1, (hasA ? newA : dest[0]),
                  (hasB ? newB : dest[1])), //
              __LogisticGuesser.__c_xyab(x2, y2, (hasA ? newA : dest[0]),
                  (hasB ? newB : dest[1])));

          if (_ModelBase._logisticModelOverLogXCheckC(newC)) {
            changed = hasC = true;
          }
        }

        if (!hasA) {
          findA: {
            // find A based on the existing or new B and C values
            if (hasB) {
              newA = _ModelBase._med3(//
                  _ModelBase._logisticModelOverLogX_a_xybc(x0, y0, newB,
                      (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_a_xybc(x1, y1, newB,
                      (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_a_xybc(x2, y2, newB,
                      (hasC ? newC : dest[2])));

              if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
                changed = hasA = true;
                break findA;
              }
            }

            newA = _ModelBase._med3(//
                _ModelBase._logisticModelOverLogX_a_x1y1x2y2c(x0, y0, x1,
                    y1, (hasC ? newC : dest[2])), //
                _ModelBase._logisticModelOverLogX_a_x1y1x2y2c(x1, y1, x2,
                    y2, (hasC ? newC : dest[2])), //
                _ModelBase._logisticModelOverLogX_a_x1y1x2y2c(x2, y2, x0,
                    y0, (hasC ? newC : dest[2])));//

            if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
              changed = hasA = true;
              break findA;
            }

            if (!hasB) {
              newA = _ModelBase._med3(//
                  _ModelBase._logisticModelOverLogX_a_xybc(x0, y0, dest[1],
                      (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_a_xybc(x1, y1, dest[1],
                      (hasC ? newC : dest[2])), //
                  _ModelBase._logisticModelOverLogX_a_xybc(x2, y2, dest[1],
                      (hasC ? newC : dest[2])));

              if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
                changed = hasA = true;
                break findA;
              }
            }

            if (!changed) {
              if (Math.abs(x0) <= 0d) {
                newA = y0;
                changed = hasA = true;
                break findA;
              }
              if (Math.abs(x1) <= 0d) {
                newA = y1;
                changed = hasA = true;
                break findA;
              }
              if (Math.abs(x2) <= 0d) {
                newA = y2;
                changed = hasA = true;
                break findA;
              }
            }
          }
        }

        if (hasA && hasB && hasC) {
          error = __LogisticGuesser.__error(x0, y0, x1, y1, x2, y2, newA,
              newB, newC);
          if (MathUtils.isFinite(error) && (error < bestError)) {
            dest[0] = newA;
            dest[1] = newB;
            dest[2] = newC;
            return error;
          }

          return bestError;
        }
      }

      return bestError;
    }

    /**
     * Update a guess for {@code a}, {@code b}, and {@code c} by using the
     * first two points for calculating the new values (and the last one
     * only in the error computation)
     *
     * @param x0
     *          the {@code x}-coordinate of the first point
     * @param y0
     *          the {@code y}-coordinate of the first point
     * @param x1
     *          the {@code x}-coordinate of the second point
     * @param y1
     *          the {@code y}-coordinate of the second point
     * @param x2
     *          the {@code x}-coordinate of the third point
     * @param y2
     *          the {@code y}-coordinate of the third point
     * @param maxY
     *          the maximum {@code y} coordinate
     * @param dest
     *          the destination array
     * @param bestError
     *          the best error so far
     * @return the new (or old) best error
     */
    private static final double __update(final double x0, final double y0,
        final double x1, final double y1, final double x2, final double y2,
        final double maxY, final double[] dest, final double bestError) {
      double newA, newB, newC, error;
      boolean hasA, hasB, hasC, changed;

      newA = newB = newC = Double.NaN;
      hasA = hasB = hasC = false;

      changed = true;
      while (changed) {
        changed = false;

        if (!hasB) {
          // find B based on the existing or new A and C values
          newB = _ModelBase._logisticModelOverLogX_b_x1y1x2y2a(x0, y0, x1,
              y1, (hasA ? newA : dest[0]));
          if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
            changed = hasB = true;
          } else {
            newB = __LogisticGuesser.__b_xyac(x0, y0,
                (hasA ? newA : dest[0]), (hasC ? newC : dest[2]));
            if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
              changed = hasB = true;
            } else {
              newB = _ModelBase._logisticModelOverLogX_b_x1y1x2y2c(x0, y0,
                  x1, y1, (hasC ? newC : dest[2]));
              if (_ModelBase._logisticModelOverLogXCheckB(newB)) {
                changed = hasB = true;
              }
            }
          }
        }

        if (!hasC) {
          // find C based on the existing or new A and B values
          newC = __LogisticGuesser.__c_xyab(x0, y0,
              (hasA ? newA : dest[0]), (hasB ? newB : dest[1]));
          if (_ModelBase._logisticModelOverLogXCheckC(newC)) {
            changed = hasC = true;
          }
        }

        if (!hasA) {
          findA: {
            // find A based on the existing or new B and C values
            if (hasB) {
              newA = _ModelBase._logisticModelOverLogX_a_xybc(x0, y0, newB,
                  (hasC ? newC : dest[2]));
              if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
                changed = hasA = true;
                break findA;
              }
            }

            newA = _ModelBase._logisticModelOverLogX_a_x1y1x2y2c(x0, y0,
                x1, y1, (hasC ? newC : dest[2]));
            if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
              changed = hasA = true;
              break findA;
            }

            if (!hasB) {
              newA = _ModelBase._logisticModelOverLogX_a_xybc(x0, y0,
                  dest[1], (hasC ? newC : dest[2]));
              if (_ModelBase._logisticModelOverLogXCheckA(newA, maxY)) {
                changed = hasA = true;
                break findA;
              }
            }

            if (!changed) {
              if (Math.abs(x0) <= 0d) {
                newA = y0;
                changed = hasA = true;
                break findA;
              }
              if (Math.abs(x1) <= 0d) {
                newA = y1;
                changed = hasA = true;
                break findA;
              }
              if (Math.abs(x2) <= 0d) {
                newA = y2;
                changed = hasA = true;
                break findA;
              }
            }
          }
        }

        if (hasA && hasB && hasC) {
          error = __LogisticGuesser.__error(x0, y0, x1, y1, x2, y2, newA,
              newB, newC);
          if (MathUtils.isFinite(error) && (error < bestError)) {
            dest[0] = newA;
            dest[1] = newB;
            dest[2] = newC;
            return error;
          }

          return bestError;
        }
      }

      return bestError;
    }

    /** {@inheritDoc} */
    @Override
    protected final boolean guess(final double[] points,
        final double[] dest, final Random random) {
      final double maxY, x0, y0, x1, y1, x2, y2;
      double oldError, newError;
      int steps;

      x0 = points[0];
      y0 = points[1];
      x1 = points[2];
      y1 = points[3];
      x2 = points[4];
      y2 = points[5];

      maxY = ((random.nextInt(5) > 0)//
          ? Math.max(y0, Math.max(y1, y2)) : this.m_maxY);
      steps = 100;
      newError = Double.POSITIVE_INFINITY;

      while ((--steps) > 0) {
        _ModelBase._logisticModelOverLogXFallback(maxY, random, dest);
        for (;;) {
          oldError = newError;

          newError = __LogisticGuesser.__update(x0, y0, x1, y1, x2, y2,
              maxY, dest, oldError);
          newError = __LogisticGuesser.__update(x0, y0, x2, y2, x1, y1,
              maxY, dest, newError);
          newError = __LogisticGuesser.__update(x1, y1, x0, y0, x2, y2,
              maxY, dest, newError);
          newError = __LogisticGuesser.__update(x1, y1, x2, y2, x0, y0,
              maxY, dest, newError);
          newError = __LogisticGuesser.__update(x2, y2, x0, y0, x1, y1,
              maxY, dest, newError);
          newError = __LogisticGuesser.__update(x2, y2, x1, y1, x0, y0,
              maxY, dest, newError);

          newError = __LogisticGuesser.__updateMed(x0, y0, x1, y1, x2, y2,
              maxY, dest, newError);

          if ((--steps) <= 0) {
            return MathUtils.isFinite(newError);
          }
          if (newError >= oldError) {
            if (MathUtils.isFinite(newError)) {
              return true;
            }
          }
        }
      }

      return false;
    }

    /** {@inheritDoc} */
    @Override
    protected final boolean fallback(final double[] points,
        final double[] dest, final Random random) {
      double maxY;
      int i;

      if (random.nextInt(10) <= 0) {
        maxY = this.m_maxX;
      } else {
        maxY = Double.NEGATIVE_INFINITY;
        for (i = (points.length - 1); i > 0; i -= 2) {
          maxY = Math.max(maxY, points[i]);
        }
      }

      _ModelBase._logisticModelOverLogXFallback(maxY, random, dest);
      return true;
    }

    /** {@inheritDoc} */
    @Override
    protected final void fallback(final double[] dest,
        final Random random) {
      _ModelBase._logisticModelOverLogXFallback(this.m_maxY, random, dest);
    }
  }
}