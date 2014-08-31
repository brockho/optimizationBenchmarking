package org.optimizationBenchmarking.utils.graphics.drivers.freeHEP;

import org.freehep.graphicsio.emf.EMFGraphics2D;
import org.optimizationBenchmarking.utils.graphics.GraphicID;
import org.optimizationBenchmarking.utils.graphics.IGraphicListener;

/**
 * the internal <a
 * href="http://en.wikipedia.org/wiki/Windows_Metafile">EMF</a> graphic
 * based on the <a
 * href="http://java.freehep.org/vectorgraphics">FreeHEP</a> library
 */
final class _FreeHEPEMFGraphic extends
    _FreeHEPAbstractVectorGraphicsProxy<EMFGraphics2D> {

  /**
   * instantiate
   * 
   * @param graphic
   *          the graphic to use
   * @param id
   *          the graphic id identifying this graphic and the path under
   *          which the contents of the graphic are stored
   * @param listener
   *          the object to notify when we are closed, or {@code null} if
   *          none needs to be notified
   * @param w
   *          the width
   * @param h
   *          the height
   */
  _FreeHEPEMFGraphic(final EMFGraphics2D graphic, final GraphicID id,
      final IGraphicListener listener, final int w, final int h) {
    super(graphic, id, listener, w, h);
  }

  /** {@inheritDoc} */
  @Override
  protected final void onClose() {
    try {
      this.m_out.endExport();
    } finally {
      super.onClose();
    }
  }
}