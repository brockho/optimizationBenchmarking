package org.optimizationBenchmarking.utils.graphics.graphic.impl.freeHEP;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.graphics.GraphicUtils;
import org.optimizationBenchmarking.utils.graphics.PhysicalDimension;
import org.optimizationBenchmarking.utils.graphics.graphic.EGraphicFormat;
import org.optimizationBenchmarking.utils.graphics.graphic.impl.abstr.AbstractGraphicDriver;
import org.optimizationBenchmarking.utils.graphics.graphic.spec.Graphic;
import org.optimizationBenchmarking.utils.io.paths.PathUtils;
import org.optimizationBenchmarking.utils.math.units.ELength;
import org.optimizationBenchmarking.utils.reflection.ReflectionUtils;
import org.optimizationBenchmarking.utils.tools.spec.IFileProducerListener;

/**
 * A driver which creates <a
 * href="http://en.wikipedia.org/wiki/Portable_Document_Format">PDF</a>
 * graphics. This driver uses a very dirty hack to enforce custom page
 * sizes in the pdf output: It temporarily changes the size of the
 * {@link org.freehep.graphicsio.PageConstants#INTERNATIONAL} page&hellip;
 * All using FreeHEP should thus synchronize on
 * <code>{@link org.freehep.graphicsio.PageConstants}.class</code> during
 * all code accessing page sizes. This driver has some problems, see, e.g.,
 * {@link examples.org.optimizationBenchmarking.utils.graphics.GraphicsExample}
 * .
 */
public class FreeHEPPDFGraphicDriver extends AbstractGraphicDriver {
  /** the properties */
  private final Map<Object, Object> m_props;

  /** the correct dimension to use */
  static Dimension s_correctDim;

  /** the dimension to temporarily destroy */
  static Dimension s_messWith;

  /** the hidden constructor */
  FreeHEPPDFGraphicDriver() {
    super(EGraphicFormat.PDF);
    Map<Object, Object> o;

    try {
      o = FreeHEPPDFGraphicDriver.__initialize();
    } catch (final Throwable t) {
      o = null;
    }
    this.m_props = o;
  }

  /**
   * try to initialize
   * 
   * @return the properties
   * @throws ClassNotFoundException
   *           if a necessary class could not be loaded
   */
  private static final Map<Object, Object> __initialize()
      throws ClassNotFoundException {

    ReflectionUtils.ensureClassesAreLoaded(
        "org.freehep.graphics2d.TagString", //$NON-NLS-1$
        "org.freehep.graphics2d.font.FontUtilities", //$NON-NLS-1$
        "org.freehep.graphics2d.font.Lookup", //$NON-NLS-1$
        "org.freehep.graphicsio.AbstractVectorGraphicsIO", //$NON-NLS-1$
        "org.freehep.graphicsio.FontConstants", //$NON-NLS-1$
        "org.freehep.graphicsio.ImageConstants", //$NON-NLS-1$
        //"org.freehep.graphicsio.ImageGraphics2D", //$NON-NLS-1$
        "org.freehep.graphicsio.InfoConstants", //$NON-NLS-1$
        "org.freehep.graphicsio.MultiPageDocument", //$NON-NLS-1$
        "org.freehep.graphicsio.PageConstants", //$NON-NLS-1$
        "org.freehep.util.UserProperties", //$NON-NLS-1$
        "org.freehep.graphics2d.font.FontEncoder", //$NON-NLS-1$
        "org.freehep.graphics2d.font.FontUtilities", //$NON-NLS-1$
        "org.freehep.util.images.ImageUtilities" //$NON-NLS-1$
    );

    final org.freehep.util.UserProperties props = new org.freehep.util.UserProperties();

    org.freehep.graphicsio.pdf.PDFGraphics2D.setClipEnabled(true);

    props.putAll(org.freehep.graphicsio.pdf.PDFGraphics2D
        .getDefaultProperties());
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.EMBED_FONTS, true);
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.EMBED_FONTS_AS,
        org.freehep.graphicsio.FontConstants.EMBED_FONTS_TYPE3);
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.BACKGROUND_COLOR,
        Color.WHITE);
    props.setProperty(org.freehep.graphicsio.pdf.PDFGraphics2D.COMPRESS,
        true);
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.FIT_TO_PAGE, false);
    props.setProperty(org.freehep.graphicsio.pdf.PDFGraphics2D.VERSION,//
        org.freehep.graphicsio.pdf.PDFGraphics2D.VERSION6);
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.ORIENTATION,
        org.freehep.graphicsio.PageConstants.PORTRAIT);
    props.setProperty(
        org.freehep.graphicsio.pdf.PDFGraphics2D.PAGE_MARGINS,
        "0, 0, 0, 0"); //$NON-NLS-1$
    props.setProperty(org.freehep.graphicsio.pdf.PDFGraphics2D.PAGE_SIZE,
        org.freehep.graphicsio.PageConstants.INTERNATIONAL);

    synchronized (org.freehep.graphicsio.PageConstants.class) {
      FreeHEPPDFGraphicDriver.s_messWith = org.freehep.graphicsio.PageConstants
          .getSize(org.freehep.graphicsio.PageConstants.INTERNATIONAL);
      FreeHEPPDFGraphicDriver.s_correctDim = new Dimension(
          FreeHEPPDFGraphicDriver.s_messWith.width,
          FreeHEPPDFGraphicDriver.s_messWith.height);
    }

    return props;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean canUse() {
    return (this.m_props != null);
  }

  /**
   * get the instance of the FreeHEP PDF driver
   * 
   * @return the instance of the FreeHEP PDF driver
   */
  public static final FreeHEPPDFGraphicDriver getInstance() {
    return __FreeHEPPDFGraphicDriverLoader.INSTANCE;
  }

  /** {@inheritDoc} */
  @Override
  public final String toString() {
    return "FreeHEP-based PDF Driver"; //$NON-NLS-1$
  }

  /** {@inheritDoc} */
  @Override
  protected final Graphic createGraphic(final Logger logger,
      final IFileProducerListener listener, final Path basePath,
      final String mainDocumentNameSuggestion, final PhysicalDimension size) {
    final org.freehep.util.UserProperties up;
    final org.freehep.graphicsio.pdf.PDFGraphics2D g;
    final double wd, hd;
    final Dimension dim;
    final ELength sizeUnit;
    final Path path;

    up = new org.freehep.util.UserProperties();
    up.putAll(this.m_props);

    sizeUnit = size.getUnit();
    wd = sizeUnit.convertTo(size.getWidth(), ELength.POINT);
    hd = sizeUnit.convertTo(size.getHeight(), ELength.POINT);
    dim = new Dimension();
    if ((wd <= 0d) || (wd >= Integer.MAX_VALUE) || (hd <= 0d)
        || (hd >= Integer.MAX_VALUE)
        || ((dim.width = ((int) (0.5d + wd))) <= 0)
        || ((dim.height = ((int) (0.5d + hd))) <= 0)) {
      throw new IllegalArgumentException("Invalid size " + size + //$NON-NLS-1$
          " translated to " + dim);//$NON-NLS-1$
    }

    path = this.makePath(basePath, mainDocumentNameSuggestion);
    synchronized (org.freehep.graphicsio.PageConstants.class) {
      FreeHEPPDFGraphicDriver.s_messWith.setSize(dim);
      try {
        synchronized (org.freehep.graphicsio.pdf.PDFGraphics2D.class) {
          org.freehep.graphicsio.pdf.PDFGraphics2D.setClipEnabled(true);

          g = new org.freehep.graphicsio.pdf.PDFGraphics2D(
              PathUtils.openOutputStream(path), dim);
          g.setProperties(up);
          g.setMultiPage(false);
          GraphicUtils.setDefaultRenderingHints(g);
          g.startExport();
          g.setClip(0, 0, dim.width, dim.height);
        }
        GraphicUtils.setDefaultRenderingHints(g);
      } finally {
        FreeHEPPDFGraphicDriver.s_messWith
            .setSize(FreeHEPPDFGraphicDriver.s_correctDim);
      }
    }

    return new _FreeHEPPDFGraphic(g, logger, listener, path, dim.width,
        dim.height);
  }

  /** the loader */
  static final class __FreeHEPPDFGraphicDriverLoader {
    /**
     * the globally shared instance of the <a
     * href="http://en.wikipedia.org/wiki/Portable_Document_Format">PDF</a>
     * graphic driver
     */
    static final FreeHEPPDFGraphicDriver INSTANCE = new FreeHEPPDFGraphicDriver();

  }
}
