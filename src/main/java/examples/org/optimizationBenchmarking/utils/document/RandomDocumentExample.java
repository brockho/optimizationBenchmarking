package examples.org.optimizationBenchmarking.utils.document;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.optimizationBenchmarking.utils.ErrorUtils;
import org.optimizationBenchmarking.utils.RandomUtils;
import org.optimizationBenchmarking.utils.bibliography.data.BibAuthorBuilder;
import org.optimizationBenchmarking.utils.bibliography.data.BibAuthorsBuilder;
import org.optimizationBenchmarking.utils.bibliography.data.BibDateBuilder;
import org.optimizationBenchmarking.utils.bibliography.data.Bibliography;
import org.optimizationBenchmarking.utils.bibliography.data.BibliographyBuilder;
import org.optimizationBenchmarking.utils.collections.lists.ArrayListView;
import org.optimizationBenchmarking.utils.comparison.EComparison;
import org.optimizationBenchmarking.utils.document.impl.EDocumentFormat;
import org.optimizationBenchmarking.utils.document.impl.latex.LaTeXDocumentClass;
import org.optimizationBenchmarking.utils.document.impl.xhtml10.XHTML10Driver;
import org.optimizationBenchmarking.utils.document.spec.ECitationMode;
import org.optimizationBenchmarking.utils.document.spec.EFigureSize;
import org.optimizationBenchmarking.utils.document.spec.ELabelType;
import org.optimizationBenchmarking.utils.document.spec.ICode;
import org.optimizationBenchmarking.utils.document.spec.IComplexText;
import org.optimizationBenchmarking.utils.document.spec.IDocument;
import org.optimizationBenchmarking.utils.document.spec.IDocumentBody;
import org.optimizationBenchmarking.utils.document.spec.IDocumentDriver;
import org.optimizationBenchmarking.utils.document.spec.IDocumentHeader;
import org.optimizationBenchmarking.utils.document.spec.IEquation;
import org.optimizationBenchmarking.utils.document.spec.IFigure;
import org.optimizationBenchmarking.utils.document.spec.IFigureSeries;
import org.optimizationBenchmarking.utils.document.spec.ILabel;
import org.optimizationBenchmarking.utils.document.spec.ILabeledObject;
import org.optimizationBenchmarking.utils.document.spec.IList;
import org.optimizationBenchmarking.utils.document.spec.IMath;
import org.optimizationBenchmarking.utils.document.spec.IPlainText;
import org.optimizationBenchmarking.utils.document.spec.ISection;
import org.optimizationBenchmarking.utils.document.spec.ISectionBody;
import org.optimizationBenchmarking.utils.document.spec.ISectionContainer;
import org.optimizationBenchmarking.utils.document.spec.IStructuredText;
import org.optimizationBenchmarking.utils.document.spec.ITable;
import org.optimizationBenchmarking.utils.document.spec.ITableRow;
import org.optimizationBenchmarking.utils.document.spec.ITableSection;
import org.optimizationBenchmarking.utils.document.spec.IText;
import org.optimizationBenchmarking.utils.document.spec.TableCellDef;
import org.optimizationBenchmarking.utils.graphics.EScreenSize;
import org.optimizationBenchmarking.utils.graphics.chart.spec.IChartDriver;
import org.optimizationBenchmarking.utils.graphics.graphic.EGraphicFormat;
import org.optimizationBenchmarking.utils.graphics.graphic.spec.Graphic;
import org.optimizationBenchmarking.utils.graphics.style.IStyle;
import org.optimizationBenchmarking.utils.graphics.style.StyleSet;
import org.optimizationBenchmarking.utils.graphics.style.color.ColorStyle;
import org.optimizationBenchmarking.utils.graphics.style.font.FontStyle;
import org.optimizationBenchmarking.utils.graphics.style.stroke.StrokeStyle;
import org.optimizationBenchmarking.utils.text.ESequenceMode;
import org.optimizationBenchmarking.utils.text.ETextCase;
import org.optimizationBenchmarking.utils.text.textOutput.ITextOutput;
import org.optimizationBenchmarking.utils.text.textOutput.MemoryTextOutput;

import examples.org.optimizationBenchmarking.LoremIpsum;
import examples.org.optimizationBenchmarking.utils.bibliography.data.RandomBibliography;
import examples.org.optimizationBenchmarking.utils.graphics.chart.LineChartExample;

/**
 * An example used to illustrate how documents can be created with the
 * document output API both serial and in parallel. The program takes two
 * parameters: the destination folder and the number of processors to use.
 * Several different document are used in parallel to generate documents.
 * The documents are entirely random, but based on the same seed, and
 * contains examples for all of the available primitives of the API. If
 * more than one processor is specified, then the creation of each of the
 * (nested) sections in a document is launched as a separate
 * {@link java.util.concurrent.RecursiveAction ForkJoinTask}. If only one
 * processor is specified, no parallelization will be performed and all
 * produced documents should look approximately the same, except for
 * necessary adaptation to the underlying output format.
 */
public class RandomDocumentExample extends DocumentExample {

  /** the document driver to use */
  public static final ArrayListView<IDocumentDriver> DRIVERS;

  static {
    final LinkedHashSet<IDocumentDriver> list;
    IDocumentDriver driver;

    list = new LinkedHashSet<>();

    driver = XHTML10Driver.getDefaultDriver();
    if ((driver != null) && (driver.canUse())) {
      list.add(driver);
    }

    driver = XHTML10Driver.getInstance(
        EGraphicFormat.JPEG.getDefaultDriver(),
        EScreenSize.WQXGA.getPageSize(120), null);
    if ((driver != null) && (driver.canUse())) {
      list.add(driver);
    }

    driver = XHTML10Driver.getInstance(
        EGraphicFormat.GIF.getDefaultDriver(),
        EScreenSize.SVGA.getPageSize(90),
        LaTeXDocumentClass.getDefaultFontPalette());
    if ((driver != null) && (driver.canUse())) {
      list.add(driver);
    }

    for (final EDocumentFormat format : EDocumentFormat.values()) {
      driver = format.getDefaultDriver();
      if ((driver != null) && (driver.canUse())) {
        list.add(driver);
      }
    }

    DRIVERS = ArrayListView.collectionToView(list, false);
  }

  /** the normal text */
  private static final int NORMAL_TEXT = 0;
  /** in braces */
  private static final int IN_BRACES = (RandomDocumentExample.NORMAL_TEXT + 1);
  /** in quotes */
  private static final int IN_QUOTES = (RandomDocumentExample.IN_BRACES + 1);
  /** with font */
  private static final int WITH_FONT = (RandomDocumentExample.IN_QUOTES + 1);
  /** with color */
  private static final int WITH_COLOR = (RandomDocumentExample.WITH_FONT + 1);
  /** section */
  private static final int SECTION = (RandomDocumentExample.WITH_COLOR + 1);
  /** enum */
  private static final int ENUM = (RandomDocumentExample.SECTION + 1);
  /** itemize */
  private static final int ITEMIZE = (RandomDocumentExample.ENUM + 1);
  /** figure */
  private static final int FIGURE = (RandomDocumentExample.ITEMIZE + 1);
  /** figure series */
  private static final int FIGURE_SERIES = (RandomDocumentExample.FIGURE + 1);
  /** table */
  private static final int TABLE = (RandomDocumentExample.FIGURE_SERIES + 1);
  /** equation */
  private static final int EQUATION = (RandomDocumentExample.TABLE + 1);
  /** inline math */
  private static final int INLINE_MATH = (RandomDocumentExample.EQUATION + 1);
  /** citation */
  private static final int CITATION = (RandomDocumentExample.INLINE_MATH + 1);
  /** code */
  private static final int CODE = (RandomDocumentExample.CITATION + 1);
  /** inline code */
  private static final int INLINE_CODE = (RandomDocumentExample.CODE + 1);
  /** emph */
  private static final int EMPH = (RandomDocumentExample.INLINE_CODE + 1);
  /** sub-script */
  private static final int SUBSCRIPT = (RandomDocumentExample.EMPH + 1);
  /** super-script */
  private static final int SUPERSCRIPT = (RandomDocumentExample.SUBSCRIPT + 1);
  /** ref */
  private static final int REFERENCE = (RandomDocumentExample.SUPERSCRIPT + 1);

  /** all elements */
  private static final int ALL_LENGTH = (RandomDocumentExample.REFERENCE + 1);

  /** the table cell defs */
  private static final TableCellDef[] CELLS = { TableCellDef.CENTER,
      TableCellDef.RIGHT, TableCellDef.LEFT,
      TableCellDef.VERTICAL_SEPARATOR };

  /** the comparison */
  private static final EComparison[] COMP = EComparison.values();

  /** the modes */
  private static final ECitationMode[] CITES = ECitationMode.values();
  /** the sequence modes */
  private static final ESequenceMode[] SEQUENCE = ESequenceMode.values();

  /** the label types */
  private static final ELabelType[] LABEL_TYPES = ELabelType.values();

  /** the seed to use */
  private static final long SEED = new Random().nextLong();

  /** the maximum code depth: {@value} */
  private static final int CODE_MAX_DEPTH = 8;
  /** the spaces */
  private static final char[] SPACES = new char[RandomDocumentExample.CODE_MAX_DEPTH << 1];
  static {
    Arrays.fill(RandomDocumentExample.SPACES, ' ');
  }

  /**
   * The maximum number of times all {@code while}s together will be
   * allowed to loop &ndash; {@value} &ndash; in order to prevent endless
   * loops (which should not occur anyway) or very long running example
   * documents.
   */
  private static final int MAX_ITERATIONS = 5000;

  /** the log */
  private final PrintStream m_log;

  /** the randomizer */
  private final Random m_rand;

  /** the fonts */
  private FontStyle[] m_fonts;

  /** the colors */
  private ColorStyle[] m_colors;

  /** the strokes */
  private StrokeStyle[] m_strokes;

  /** the maximum achieved section depth */
  private volatile int m_maxSectionDepth;

  /** the elements which have been done */
  private final boolean[] m_done;

  /** the labels */
  private final ArrayList<ILabel> m_labels;

  /** the figure counter */
  private volatile long m_figureCounter;

  /** the bibliography */
  private Bibliography m_bib;

  /** the allocated labels */
  private final ArrayList<ILabel>[] m_allocatedLabels;

  /** the total number of function calls */
  private int m_totalIterations;

  /**
   * run the example: there are problems with the pdf output
   * 
   * @param args
   *          the arguments
   * @throws Throwable
   *           if something fails
   */
  public static final void main(final String[] args) throws Throwable {
    final Path dir;
    final ForkJoinPool pool;
    final int proc;
    final Logger log;
    RandomDocumentExample de;
    String last, cur;
    int i;

    log = DocumentExample._getLogger();

    findProcs: {
      if ((args != null) && (args.length > 0)) {
        dir = Paths.get(args[0]);
        if (args.length > 1) {
          proc = Math.max(1, Math.min(Integer.parseInt(args[1]), Runtime
              .getRuntime().availableProcessors()));
          break findProcs;
        }
      } else {
        dir = Files.createTempDirectory("document"); //$NON-NLS-1$
      }
      proc = 1;
    }
    synchronized (System.out) {
      System.out.print("Begin creating documents to folder ");//$NON-NLS-1$
      System.out.print(dir);
      System.out.print(" using ");//$NON-NLS-1$
      System.out.print(proc);
      System.out.println(" processor(s).");//$NON-NLS-1$
    }

    i = 0;
    cur = null;
    if (proc > 1) {
      pool = new ForkJoinPool(proc,
          ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    } else {
      pool = null;
    }

    for (final IDocumentDriver driver : RandomDocumentExample.DRIVERS) {//
      last = cur;
      cur = driver.getClass().getSimpleName();

      if (!(cur.equals(last))) {
        i = 0;
      }
      i++;

      de = new RandomDocumentExample(driver.use()
          .setBasePath(dir.resolve((("random/" + cur) + '_') + i))//$NON-NLS-1$
          .setMainDocumentNameSuggestion("report")//$NON-NLS-1$
          .setFileProducerListener(new FinishedPrinter(driver))
          .setLogger(log).create(), null, System.out);

      if (pool != null) {
        pool.execute(de);
      } else {
        de.run();
      }
    }

    if (pool != null) {
      pool.shutdown();
      pool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
    }
    synchronized (System.out) {
      System.out.print("Finished creating documents to folder ");//$NON-NLS-1$
      System.out.print(dir);
      System.out.print(" using ");//$NON-NLS-1$
      System.out.print(proc);
      System.out.println(" processor(s).");//$NON-NLS-1$
    }
  }

  /**
   * mark a thing as done
   * 
   * @param index
   *          the index
   */
  private final void __done(final int index) {
    this.m_done[index] = true;

  }

  /**
   * note a used label
   * 
   * @param e
   *          the labeled element
   */
  private final void __useLabel(final ILabeledObject e) {
    final ILabel l;
    if (e != null) {
      l = e.getLabel();
      if (l != null) {
        synchronized (this.m_labels) {
          this.m_labels.add(l);
        }
      }
    }
  }

  /**
   * pop a label
   * 
   * @return the label, or {@code null} if the label list is empty
   */
  private final ILabel __getLabel() {
    final int s, i;
    synchronized (this.m_labels) {
      s = this.m_labels.size();
      if (s <= 0) {
        return null;
      }
      i = this.m_rand.nextInt(s);
      if (s > 10) {
        return this.m_labels.remove(i);
      }
      return this.m_labels.get(i);
    }
  }

  /**
   * note the maximum section depth
   * 
   * @param i
   *          the value
   */
  private synchronized final void __maxSecDepth(final int i) {
    if (i > this.m_maxSectionDepth) {
      this.m_maxSectionDepth = i;
    }
  }

  /**
   * get the next figure
   * 
   * @return the next figure
   */
  private synchronized final long __nextFigure() {
    return (this.m_figureCounter++);
  }

  /**
   * create the header
   * 
   * @param header
   *          the header
   */
  private final void __createHeader(final IDocumentHeader header) {

    RandomDocumentExample._createRandomHeader(header, this.m_doc
        .getClass().getSimpleName(), this.m_rand);
  }

  /**
   * create a random header
   * 
   * @param header
   *          the header
   * @param clazz
   *          the driver class
   * @param rand
   *          the randomizer
   */
  static final void _createRandomHeader(final IDocumentHeader header,
      final String clazz, final Random rand) {
    boolean first;

    try (final IPlainText t = header.title()) {
      t.append("Report Created by Driver "); //$NON-NLS-1$
      t.append(clazz);
    }

    try (final BibAuthorsBuilder babs = header.authors()) {
      try (final BibAuthorBuilder bab = babs.author()) {
        bab.setFamilyName("Weise");//$NON-NLS-1$
        bab.setPersonalName("Thomas");//$NON-NLS-1$
      }
      try (final BibAuthorBuilder bab = babs.author()) {
        bab.setFamilyName("Other");//$NON-NLS-1$
        bab.setPersonalName("Someone");//$NON-NLS-1$
      }
    }

    try (final BibDateBuilder bd = header.date()) {
      bd.fromNow();
    }

    first = true;
    try (final IPlainText t = header.summary()) {
      do {
        if (first) {
          first = false;
        } else {
          t.append(' ');
        }
        LoremIpsum.appendLoremIpsum(t, rand);
      } while (rand.nextInt(5) > 0);
    }
  }

  /**
   * check if examples for any possible thing are included
   * 
   * @return the things
   */
  private final boolean __hasAll() {
    for (final boolean b : this.m_done) {
      if (!b) {
        return false;
      }
    }
    return true;
  }

  /**
   * Create a section in a section container
   * 
   * @param sc
   *          the section container
   * @param sectionDepth
   *          the section depth
   */
  final void _createSection(final ISectionContainer sc,
      final int sectionDepth) {
    Throwable error;
    boolean hasSection, hasText, needsText, needsNewLine;
    int cur, last;
    ArrayList<Future<Void>> ra;
    _SectionTask st;

    hasText = hasSection = false;

    needsText = true;
    needsNewLine = hasText = hasSection = false;

    error = null;
    this.__done(RandomDocumentExample.SECTION);
    try {

      this.__maxSecDepth(sectionDepth + 1);
      cur = last = (-1);
      ra = null;

      try (final ISection section = sc.section(this
          .__getLabel(ELabelType.SECTION))) {
        this.__useLabel(section);

        try (final IPlainText title = section.title()) {
          LoremIpsum.appendLoremIpsum(title, this.m_rand, 8);
        }

        try (final ISectionBody body = section.body()) {

          main: do {

            if (hasSection) {
              cur = 0;
            } else {
              if (needsText) {
                cur = ((this.m_rand.nextInt(3) > 0) ? 8 : 0);
              } else {
                do {
                  cur = this.m_rand.nextInt(8);
                } while ((cur == last)// never have two same
                    || ((cur == 2) && (last == 3))
                    || ((cur == 3) && (last == 2)));
              }
            }
            last = cur;

            switch (cur) {
              case 0: {
                if (sectionDepth > 4) {
                  continue main;
                }
                hasSection = true;

                st = new _SectionTask(this, body, (sectionDepth + 1));

                if (ForkJoinTask.inForkJoinPool()) {
                  if (ra == null) {
                    ra = new ArrayList<>();
                  }
                  ra.add(st.fork());
                } else {
                  try {
                    st.compute();
                  } catch (final Throwable tt) {
                    error = ErrorUtils.aggregateError(error, tt);
                    break main;
                  }
                }
                needsNewLine = true;
                needsText = false;
                break;
              }
              case 1: {
                try {
                  this.__createList(body, 0);
                  needsNewLine = false;
                  needsText = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              case 2: {
                try {
                  this.__createFigure(body);
                  needsText = needsNewLine = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              case 3: {
                try {
                  this.__createFigureSeries(body);
                  needsText = needsNewLine = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              case 4: {
                try {
                  this.__createTable(body);
                  needsText = needsNewLine = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              case 5: {
                try {
                  this.__createEquation(body);
                  needsText = needsNewLine = false;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              case 6: {
                try {
                  this.__createCode(body);
                  needsText = needsNewLine = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              default: {
                try {
                  this.__text(body, 0, needsNewLine);
                  needsText = false;
                  needsNewLine = hasText = true;
                  break;
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
            }
          } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
              && ((!(hasText || hasSection)) || this.m_rand.nextBoolean()));

          if (ra != null) {
            looper: for (final Future<Void> f : ra) {
              try {
                f.get();
              } catch (final Throwable tt) {
                error = ErrorUtils.aggregateError(error, tt);
                break looper;
              }
            }
          }
        }
      }
    } catch (final Throwable t) {
      error = ErrorUtils.aggregateError(error, t);
    }
    if (error != null) {
      ErrorUtils.throwAsRuntimeException(error);
    }
  }

  /**
   * print some text
   * 
   * @param out
   *          the text output
   * @param depth
   *          the depth
   * @param needsNewLine
   *          needs a new line?
   */
  private final void __text(final ITextOutput out, final int depth,
      final boolean needsNewLine) {
    boolean first, must, needs;
    IStyle s;
    ArrayList<ILabel> labels;
    ILabel l;
    Throwable error;

    first = true;
    must = needsNewLine;
    needs = false;
    labels = null;
    error = null;

    this.__done(RandomDocumentExample.NORMAL_TEXT);
    try {
      main: do {
        spacer: {
          if (first) {
            if ((depth <= 0) && must) {
              out.appendLineBreak();
            }
            break spacer;
          }
          if (this.m_rand.nextBoolean()) {
            out.append(' ');
          } else {
            out.appendLineBreak();
          }
        }
        first = false;

        LoremIpsum.appendLoremIpsum(out, this.m_rand);

        if ((out instanceof IPlainText) && (depth < 10)) {

          switch (this.m_rand.nextInt(//
              (out instanceof IComplexText) ? 11 : 2)) {
            case 0: {
              out.append(' ');
              this.__done(RandomDocumentExample.IN_BRACES);
              try (final IPlainText t = ((IPlainText) out).inBraces()) {
                try {
                  this.__text(t, (depth + 1), this.m_rand.nextBoolean());
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 1: {
              out.append(' ');
              this.__done(RandomDocumentExample.IN_QUOTES);
              try (final IPlainText t = ((IPlainText) out).inQuotes()) {
                try {
                  this.__text(t, (depth + 1), this.m_rand.nextBoolean());
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 2: {
              out.append(' ');
              this.__done(RandomDocumentExample.WITH_FONT);
              try (final IPlainText t = ((IComplexText) out)
                  .style((s = this.m_fonts[this.m_rand
                      .nextInt(this.m_fonts.length)]))) {
                try {
                  t.append("In ");//$NON-NLS-1$
                  s.appendDescription(ETextCase.IN_SENTENCE, t, false);
                  t.append(" font: "); //$NON-NLS-1$
                  this.__text(t, (depth + 1), this.m_rand.nextBoolean());
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 3: {
              out.append(' ');
              this.__done(RandomDocumentExample.WITH_COLOR);
              try (final IPlainText t = ((IComplexText) out)
                  .style((s = this.m_colors[this.m_rand
                      .nextInt(this.m_colors.length)]))) {
                try {
                  t.append("In ");//$NON-NLS-1$
                  s.appendDescription(ETextCase.IN_SENTENCE, t, false);
                  t.append(" color: "); //$NON-NLS-1$
                  this.__text(t, (depth + 1), this.m_rand.nextBoolean());
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 4: {
              out.append(' ');
              this.__done(RandomDocumentExample.EMPH);
              try (final IPlainText t = ((IComplexText) out).emphasize()) {
                try {
                  t.append("Something Emphasized: "); //$NON-NLS-1$
                  LoremIpsum.appendLoremIpsum(t, this.m_rand, 5);
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 5: {
              out.append(' ');
              this.__done(RandomDocumentExample.INLINE_CODE);
              try (final IPlainText t = ((IComplexText) out).inlineCode()) {
                try {
                  t.append("Inline Code: "); //$NON-NLS-1$
                  LoremIpsum.appendLoremIpsum(t, this.m_rand, 5);
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 6: {
              this.__done(RandomDocumentExample.SUBSCRIPT);
              out.append(" Sub: "); //$NON-NLS-1$
              try (final IPlainText t = ((IComplexText) out).subscript()) {
                try {
                  LoremIpsum.appendLoremIpsum(t, this.m_rand, 2);
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }
            case 7: {
              this.__done(RandomDocumentExample.SUPERSCRIPT);
              out.append(" Super: "); //$NON-NLS-1$
              try (final IPlainText t = ((IComplexText) out).superscript()) {
                try {

                  LoremIpsum.appendLoremIpsum(t, this.m_rand, 2);
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
              break;
            }

            case 8: {
              this.__done(RandomDocumentExample.INLINE_MATH);
              out.append(" Inline equation: ");//$NON-NLS-1$
              try {
                this.__createInlineMath(((IComplexText) out));
              } catch (final Throwable tt) {
                error = ErrorUtils.aggregateError(error, tt);
                break main;
              }
              break;
            }

            case 9: {
              this.__done(RandomDocumentExample.REFERENCE);
              try {
                if (labels == null) {
                  labels = new ArrayList<>();
                }
                do {
                  l = this.__getLabel();
                  if (l == null) {
                    break;
                  }
                  if (labels.contains(l)) {
                    continue;
                  }
                  labels.add(l);
                } while (this.m_rand.nextBoolean());

                if (labels.size() > 0) {
                  out.append(" And here we reference ");//$NON-NLS-1$

                  ((IComplexText) out)
                      .reference(
                          ETextCase.IN_SENTENCE,
                          RandomDocumentExample.SEQUENCE[this.m_rand
                              .nextInt(RandomDocumentExample.SEQUENCE.length)],
                          labels.toArray(new ILabel[labels.size()]));
                  labels.clear();
                  out.append('.');
                }
              } catch (final Throwable tt) {
                error = ErrorUtils.aggregateError(error, tt);
                break main;
              }
              break;
            }

            case 10: {
              this.__done(RandomDocumentExample.CITATION);
              try {
                out.append(" And here we cite ");//$NON-NLS-1$

                try (BibliographyBuilder bb = ((IComplexText) out).cite(
                    RandomDocumentExample.CITES[this.m_rand
                        .nextInt(RandomDocumentExample.CITES.length)],
                    ETextCase.IN_SENTENCE,
                    RandomDocumentExample.SEQUENCE[this.m_rand
                        .nextInt(RandomDocumentExample.SEQUENCE.length)])) {

                  do {
                    bb.add(this.m_bib.get(this.m_rand.nextInt(this.m_bib
                        .size())));
                  } while (this.m_rand.nextBoolean());
                }
                out.append('.');
              } catch (final Throwable tt) {
                error = ErrorUtils.aggregateError(error, tt);
                break main;
              }
              break;
            }
            default: {
              throw new IllegalStateException();
            }
          }

          needs = true;
        }

        if (needs && (this.m_rand.nextBoolean())) {
          try {
            out.append(' ');
            LoremIpsum.appendLoremIpsum(out, this.m_rand);
            this.__done(RandomDocumentExample.NORMAL_TEXT);
          } catch (final Throwable tt) {
            error = ErrorUtils.aggregateError(error, tt);
            break main;
          }
        }

        first = false;
      } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
          && ((this.m_rand.nextInt(depth + 2) <= 0)));
    } catch (final Throwable a) {
      error = ErrorUtils.aggregateError(error, a);
    }
    if (error != null) {
      ErrorUtils.throwAsRuntimeException(error);
    }
  }

  /**
   * create a list
   * 
   * @param sb
   *          the section body
   * @param listDepth
   *          the list depth
   */
  private final void __createList(final IStructuredText sb,
      final int listDepth) {
    final boolean b;
    int ic;

    b = this.m_rand.nextBoolean();

    if (listDepth <= 0) {
      if (b) {
        this.__done(RandomDocumentExample.ENUM);
      } else {
        this.__done(RandomDocumentExample.ITEMIZE);
      }
    }

    try (final IList list = (b ? sb.enumeration() : sb.itemization())) {
      ic = 0;
      do {
        try (final IStructuredText item = list.item()) {
          if (this.m_rand.nextInt(10) <= 0) {
            this.__text(item, 0, false);
          } else {
            LoremIpsum.appendLoremIpsum(item, this.m_rand);
          }
          if ((listDepth <= 4)
              && (this.m_rand.nextInt(listDepth + 5) <= 0)) {
            this.__createList(item, (listDepth + 1));
          }
        }
      } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
          && (((++ic) <= 2) || (this.m_rand.nextBoolean())));

    }
  }

  /**
   * create the body
   * 
   * @param body
   *          the body
   */
  private final void __createBody(final IDocumentBody body) {
    this.m_maxSectionDepth = 0;

    Arrays.fill(this.m_done, false);
    do {
      this._createSection(body, 0);
    } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && ((!(this.__hasAll())) || this.__hasUnusedAllocateLabels()
            || (this.m_maxSectionDepth < 3) || (this.m_rand.nextInt(3) > 0)));
  }

  /**
   * create the footer
   * 
   * @param footer
   *          the footer
   */
  private final void __createFooter(final IDocumentBody footer) {
    this.m_maxSectionDepth = 0;

    Arrays.fill(this.m_done, false);
    do {
      this._createSection(footer, 0);
    } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && (this.m_rand.nextBoolean()));
  }

  /**
   * create
   * 
   * @param doc
   *          the document
   * @param r
   *          the randomizer
   * @param log
   *          the log stream
   */
  @SuppressWarnings("unchecked")
  public RandomDocumentExample(final IDocument doc, final Random r,
      final PrintStream log) {
    super(doc);
    int i;

    if (r == null) {
      this.m_rand = new Random();
      this.m_rand.setSeed(RandomDocumentExample.SEED);
    } else {
      this.m_rand = r;
    }
    this.m_done = new boolean[RandomDocumentExample.ALL_LENGTH];
    this.m_labels = new ArrayList<>();

    this.m_allocatedLabels = new ArrayList[i = RandomDocumentExample.LABEL_TYPES.length];
    for (; (--i) >= 0;) {
      this.m_allocatedLabels[i] = new ArrayList<>();
    }
    this.m_log = log;
  }

  /** create the bibliography */
  private final void __createBib() {

    this.m_bib = new RandomBibliography(this.m_rand).createBibliography();
  }

  /** pre-allocate some labels */
  private final void __preAllocateLabels() {
    int i;
    ILabel l;
    ArrayList<ILabel> a;

    do {
      i = this.m_rand.nextInt(RandomDocumentExample.LABEL_TYPES.length);
      l = this.m_doc.createLabel(RandomDocumentExample.LABEL_TYPES[i]);

      a = this.m_allocatedLabels[i];
      synchronized (a) {
        a.add(l);
      }

      a = this.m_labels;
      synchronized (a) {
        a.add(l);
      }
    } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && (this.m_rand.nextInt(20) > 0));
  }

  /**
   * are some pre-allocated, but unused labels left?
   * 
   * @return {@code true} if some labels are left
   */
  private final boolean __hasUnusedAllocateLabels() {
    for (final ArrayList<ILabel> l : this.m_allocatedLabels) {
      synchronized (l) {
        if (l.isEmpty()) {
          continue;
        }
        return true;
      }
    }

    return false;
  }

  /**
   * get a label
   * 
   * @param type
   *          the label type
   * @return the label (or AUTO)
   */
  private final ILabel __getLabel(final ELabelType type) {
    final ArrayList<ILabel> l;
    final int s;

    l = this.m_allocatedLabels[type.ordinal()];
    synchronized (l) {
      s = l.size();
      if (s > 0) {
        return l.remove(this.m_rand.nextInt(s));
      }
    }

    return ELabelType.AUTO;
  }

  /** {@inheritDoc} */
  @Override
  public final void run() {
    Throwable error;

    error = null;
    main: {
      try {
        try {
          if (this.m_log != null) {
            synchronized (this.m_log) {
              this.m_log.print("Begin creating document "); //$NON-NLS-1$
              this.m_log.println(this.m_doc);
            }
          }

          this.m_totalIterations = 0;
          this.m_maxSectionDepth = 0;
          this.m_labels.clear();
          for (final ArrayList<ILabel> l : this.m_allocatedLabels) {
            l.clear();
          }
          this.m_figureCounter = 0L;

          Arrays.fill(this.m_done, false);

          try {
            this.__loadStyles();
          } catch (final Throwable tt) {
            error = ErrorUtils.aggregateError(error, tt);
            break main;
          }
          try {
            this.__createBib();
          } catch (final Throwable tt) {
            error = ErrorUtils.aggregateError(error, tt);
            break main;
          }
          try {
            this.__preAllocateLabels();
          } catch (final Throwable tt) {
            error = ErrorUtils.aggregateError(error, tt);
            break main;
          }

          try (final IDocumentHeader header = this.m_doc.header()) {
            try {
              this.__createHeader(header);
            } catch (final Throwable tt) {
              error = ErrorUtils.aggregateError(error, tt);
              break main;
            }
          } finally {
            try (final IDocumentBody body = this.m_doc.body()) {
              try {
                this.m_totalIterations = 0;
                this.__createBody(body);
              } catch (final Throwable tt) {
                error = ErrorUtils.aggregateError(error, tt);
                break main;
              }
            } finally {
              try (final IDocumentBody footer = this.m_doc.footer()) {
                try {
                  this.m_totalIterations = 0;
                  this.__createFooter(footer);
                } catch (final Throwable tt) {
                  error = ErrorUtils.aggregateError(error, tt);
                  break main;
                }
              }
            }
          }
        } finally {
          try {
            try {
              this.m_doc.close();
            } catch (final Throwable tt) {
              error = ErrorUtils.aggregateError(error, tt);
              break main;
            }
          } finally {
            if (this.m_log != null) {
              synchronized (this.m_log) {
                this.m_log.print("Finished creating document "); //$NON-NLS-1$
                this.m_log.println(this.m_doc);
              }
            }
          }
        }
      } catch (final Throwable ttt) {
        error = ErrorUtils.aggregateError(error, ttt);
      }
    }

    if (error != null) {
      ErrorUtils.throwAsRuntimeException(error);
    }
  }

  /** load the styles */
  private final void __loadStyles() {
    final StyleSet ss;
    final ArrayList<Object> list;

    ss = this.m_doc.getStyles();
    list = new ArrayList<>();

    findFonts: for (;;) {
      final List<FontStyle> s = ss.allocateFonts(1);
      if (s == null) {
        break findFonts;
      }
      list.addAll(s);
    }
    list.add(ss.getCodeFont());
    list.add(ss.getDefaultFont());
    list.add(ss.getEmphFont());
    this.m_fonts = list.toArray(new FontStyle[list.size()]);

    list.clear();
    findColors: for (;;) {
      final List<ColorStyle> s = ss.allocateColors(1);
      if (s == null) {
        break findColors;
      }
      list.addAll(s);
    }
    list.add(ss.getBlack());
    this.m_colors = list.toArray(new ColorStyle[list.size()]);

    list.clear();
    findStrokes: for (;;) {
      final List<StrokeStyle> s = ss.allocateStrokes(1);
      if (s == null) {
        break findStrokes;
      }
      list.addAll(s);
    }
    list.add(ss.getDefaultStroke());
    list.add(ss.getThickStroke());
    list.add(ss.getThinStroke());
    this.m_strokes = list.toArray(new StrokeStyle[list.size()]);
  }

  /**
   * make the variable declaration
   * 
   * @param body
   *          the dest
   * @param vars
   *          the number of variables
   * @param r
   *          the random number generator
   */
  private static final void __makeVars(final IText body, final int vars,
      final Random r) {
    int i, last, cur;

    last = Integer.MIN_VALUE;

    for (i = 0; i < vars; i++) {
      if ((i == 0) || (r.nextInt(6) == 0)) {
        if (i != 0) {
          do {
            cur = r.nextInt(10) - 5;
          } while (cur == last);
          body.append(cur);
          last = cur;

          body.append(';');
          body.appendLineBreak();
        }
        body.append("int ");//$NON-NLS-1$
      }
      body.append((char) ('a' + i));
      body.append(' ');
      body.append('=');
      body.append(' ');
    }
    do {
      cur = r.nextInt(10) - 5;
    } while (cur == last);
    body.append(cur);
    body.append(';');
  }

  /**
   * make the variable declaration
   * 
   * @param body
   *          the destination
   * @param depth
   *          the depth
   */
  private static final void __indentLine(final IText body, final int depth) {
    body.appendLineBreak();
    body.append(RandomDocumentExample.SPACES, 0, (depth << 1));
  }

  /**
   * create a code
   * 
   * @param sb
   *          the section body
   */
  private final void __createCode(final ISectionBody sb) {
    final Random r;
    int i, depth, vars, a, b, c, d, lc;
    boolean needsContent;

    r = this.m_rand;
    this.__done(RandomDocumentExample.CODE);
    try (final ICode code = sb.code(this.__getLabel(ELabelType.CODE),
        r.nextBoolean())) {
      this.__useLabel(code);
      try (final IPlainText cap = code.caption()) {
        LoremIpsum.appendLoremIpsum(cap, this.m_rand, r.nextInt(10) + 10);
      }

      try (final IText body = code.body()) {
        depth = lc = 0;
        vars = (r.nextInt(22) + 5);
        RandomDocumentExample.__makeVars(body, vars, r);
        needsContent = false;

        do {
          lc++;
          switch (r.nextInt(//
              (lc >= 50) ? 1 : //
                  (depth < RandomDocumentExample.CODE_MAX_DEPTH) ? 11 : 8)) {

            case 0:
            case 1:
            case 2:
            case 3: {
              if ((!needsContent) && (depth > 0)) {
                depth--;
                RandomDocumentExample.__indentLine(body, depth);
                body.append('}');
                break;
              }
            }

            case 4: {
              RandomDocumentExample.__indentLine(body, depth);
              needsContent = false;
              a = r.nextInt(vars);
              do {
                b = r.nextInt(vars);
              } while ((vars > 1) && (a == b));
              do {
                c = r.nextInt(vars);
              } while ((vars > 2) && ((a == c) || (b == c)));
              body.append((char) ('a' + a));
              body.append(' ');
              body.append('=');
              body.append(' ');
              body.append((char) ('a' + b));
              body.append(' ');
              switch (r.nextInt(10)) {
                case 0: {
                  body.append('+');
                  break;
                }
                case 1: {
                  body.append('-');
                  break;
                }
                case 2: {
                  body.append('*');
                  break;
                }
                case 3: {
                  body.append("mod"); //$NON-NLS-1$
                  break;
                }
                case 4: {
                  body.append('<');
                  body.append('<');
                  break;
                }
                case 5: {
                  body.append('>');
                  body.append('>');
                  break;
                }
                case 6: {
                  body.append('|');
                  break;
                }
                case 7: {
                  body.append('&');
                  break;
                }
                case 8: {
                  body.append('^');
                  break;
                }
                default: {
                  body.append('/');
                  break;
                }
              }
              body.append(' ');
              body.append((char) ('a' + c));
              body.append(';');
              break;
            }

            case 5: {
              RandomDocumentExample.__indentLine(body, depth);
              needsContent = false;
              a = r.nextInt(vars);
              do {
                b = r.nextInt(vars);
              } while ((vars > 1) && (a == b));
              do {
                c = r.nextInt(vars);
              } while ((vars > 2) && ((a == c) || (b == c)));
              do {
                d = r.nextInt(vars);
              } while ((vars > 3) && ((a == d) || (b == d) || (c == d)));

              body.append((char) ('a' + a));
              body.append(' ');
              body.append('=');
              body.append(' ');
              body.append('(');
              body.append('(');
              body.append((char) ('a' + b));
              body.append(' ');
              switch (r.nextInt(4)) {
                case 0: {
                  body.append('=');
                  break;
                }
                case 1: {
                  body.append('<');
                  break;
                }
                case 2: {
                  body.append('>');
                  break;
                }
                default: {
                  body.append('!');
                  break;
                }
              }
              body.append('=');
              body.append(' ');
              body.append((char) ('a' + c));
              body.append(')');
              body.append(' ');
              body.append('?');
              body.append(' ');
              body.append((char) ('a' + d));
              body.append(' ');
              body.append(':');
              body.append(' ');
              body.append((char) ('a' + (r.nextBoolean() ? b : c)));
              body.append(')');
              body.append(';');
              break;
            }

            case 6: {
              RandomDocumentExample.__indentLine(body, depth);
              needsContent = false;
              body.append("System.out.println("); //$NON-NLS-1$
              body.append((char) ('a' + r.nextInt(vars)));
              body.append(')');
              body.append(';');
              break;
            }

            case 7: {
              body.appendLineBreak();
              body.append('/');
              body.append('*');
              body.append(' ');
              LoremIpsum.appendLoremIpsum(body, r, 10);
              body.append(' ');
              body.append('*');
              body.append('/');
              break;
            }

            case 8: {
              RandomDocumentExample.__indentLine(body, depth);
              needsContent = true;
              a = r.nextInt(vars);
              do {
                b = r.nextInt(vars);
              } while ((vars > 1) && (a == b));
              body.append("if("); //$NON-NLS-1$
              body.append((char) ('a' + a));
              body.append(' ');
              body.append(r.nextBoolean() ? '<' : '>');
              body.append(' ');
              body.append((char) ('a' + b));
              body.append(')');
              body.append(' ');
              body.append('{');
              depth++;
              break;
            }

            default: {
              RandomDocumentExample.__indentLine(body, depth);
              needsContent = true;
              a = r.nextInt(vars);
              do {
                b = r.nextInt(vars);
              } while ((vars > 1) && (a == b));
              do {
                c = r.nextInt(vars);
              } while ((vars > 2) && (((a == c) || (b == c))));
              body.append("for("); //$NON-NLS-1$
              body.append((char) ('a' + a));
              body.append(' ');
              body.append('=');
              body.append(' ');
              body.append((char) ('a' + b));
              body.append(';');
              body.append(' ');
              body.append((char) ('a' + a));
              body.append(' ');

              i = r.nextInt(5) - 2;
              switch (i) {
                case -2: {
                  body.append('<');
                  body.append('=');
                  break;
                }
                case -1: {
                  body.append('<');
                  break;
                }
                case 0: {
                  body.append('!');
                  body.append('=');
                  break;
                }
                case 1: {
                  body.append('>');
                  body.append('=');
                  break;
                }
                default: {
                  body.append('>');
                  break;
                }
              }

              body.append(' ');
              body.append((char) ('a' + c));
              body.append(';');
              body.append(' ');
              body.append((char) ('a' + a));
              if (i <= 0) {
                body.append('+');
                body.append('+');
              } else {
                body.append('-');
                body.append('-');
              }
              body.append(')');
              body.append(' ');
              body.append('{');
              depth++;
              break;
            }

          }
        } while ((lc < 100)
            && ((depth > 0) || ((lc < 50) && (r.nextInt(4) > 0))));
      }
    }
  }

  /**
   * create a table
   * 
   * @param sb
   *          the section body
   */
  private final void __createTable(final ISectionBody sb) {
    final ArrayList<TableCellDef> def, pureDef;
    final TableCellDef[] pureDefs;
    TableCellDef d;
    int min;

    this.__done(RandomDocumentExample.TABLE);

    def = new ArrayList<>();
    pureDef = new ArrayList<>();
    min = (this.m_rand.nextInt(3) + 1);

    do {
      d = RandomDocumentExample.CELLS[this.m_rand
          .nextInt(RandomDocumentExample.CELLS.length)];
      def.add(d);
      if (d != TableCellDef.VERTICAL_SEPARATOR) {
        pureDef.add(d);
      }
    } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && ((pureDef.size() < min) || this.m_rand.nextBoolean()));
    pureDefs = pureDef.toArray(new TableCellDef[pureDef.size()]);

    try (final ITable tab = sb.table(this.__getLabel(ELabelType.TABLE),
        this.m_rand.nextBoolean(),
        def.toArray(new TableCellDef[def.size()]))) {
      this.__useLabel(tab);
      try (final IPlainText cap = tab.caption()) {
        LoremIpsum.appendLoremIpsum(cap, this.m_rand,
            this.m_rand.nextInt(10) + 10);
      }
      try (final ITableSection s = tab.header()) {
        this.__makeTableSection(s, pureDefs, 1, 3);
      }
      try (final ITableSection s = tab.body()) {
        this.__makeTableSection(s, pureDefs, 2, 1000);
      }
      try (final ITableSection s = tab.footer()) {
        this.__makeTableSection(s, pureDefs, 0, 3);
      }
    }
  }

  /**
   * make a table section
   * 
   * @param sec
   *          the section
   * @param cells
   *          the cells
   * @param minRows
   *          the minimum number of rows
   * @param maxRows
   *          the maximum number of rows
   */
  private final void __makeTableSection(final ITableSection sec,
      final TableCellDef[] cells, final int minRows, final int maxRows) {
    int rows, neededRows, i, maxX, maxY;
    int[] blocked;
    TableCellDef d;

    neededRows = minRows;

    rows = 0;
    blocked = new int[cells.length];
    while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && ((rows < maxRows) && ((rows < neededRows) || (this.m_rand
            .nextInt(4) > 0)))) {
      try (final ITableRow row = sec.row()) {
        rows++;
        for (i = 0; i < cells.length; i++) {

          if (blocked[i] < rows) {
            if (this.m_rand.nextInt(7) <= 0) {
              findMaxX: for (maxX = i; (++maxX) < cells.length;) {
                if (blocked[maxX] >= rows) {
                  break findMaxX;
                }
              }

              maxX = ((i + this.m_rand.nextInt(maxX - i)) + 1);
            } else {
              maxX = (i + 1);
            }

            if (this.m_rand.nextInt(7) <= 0) {
              maxY = (rows + 1 + this.m_rand.nextInt(Math.min(5,
                  ((maxRows - rows) + 1))));
            } else {
              maxY = (rows + 1);
            }

            Arrays.fill(blocked, i, maxX, (maxY - 1));
            neededRows = Math.max(neededRows, (maxY - 1));

            if ((maxX <= (i + 1)) && (maxY <= (rows + 1))) {
              try (final IPlainText cell = row.cell()) {
                cell.append(RandomUtils.longToObject(
                    this.m_rand.nextLong(), true));
              }
            } else {
              do {
                d = RandomDocumentExample.CELLS[this.m_rand
                    .nextInt(RandomDocumentExample.CELLS.length)];
              } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
                  && ((d == TableCellDef.VERTICAL_SEPARATOR)));
              try (final IPlainText cell = row.cell((maxX - i),
                  (maxY - rows), d)) {
                if (this.m_rand.nextBoolean()) {
                  cell.append(d);
                } else {
                  cell.append(RandomUtils.longToObject(
                      this.m_rand.nextLong(), true));
                }
              }
            }
          }
        }
      }
    }
  }

  /**
   * create a figure
   * 
   * @param sb
   *          the section body
   */
  private final void __createFigure(final ISectionBody sb) {
    final EFigureSize[] s;
    final EFigureSize v;

    s = EFigureSize.values();

    this.__done(RandomDocumentExample.FIGURE);
    try (final IFigure fig = sb.figure(this.__getLabel(ELabelType.FIGURE),
        (v = s[this.m_rand.nextInt(s.length)]),
        RandomUtils.longToString(null, this.__nextFigure()))) {
      this.__fillFigure(fig, v);
    }
  }

  /**
   * create a figure series
   * 
   * @param sb
   *          the section body
   */
  private final void __createFigureSeries(final ISectionBody sb) {
    final EFigureSize[] s;
    final EFigureSize v;
    final int c;
    int i;

    s = EFigureSize.values();

    v = s[this.m_rand.nextInt(s.length)];
    c = v.getNX();
    this.__done(RandomDocumentExample.FIGURE_SERIES);
    try (final IFigureSeries fs = sb.figureSeries(
        this.__getLabel(ELabelType.FIGURE), v,
        RandomUtils.longToString(null, this.__nextFigure()))) {
      this.__useLabel(fs);
      try (final IPlainText caption = fs.caption()) {
        caption.append("A figure series with figures of size "); //$NON-NLS-1$
        caption.append(v.toString());
        caption.append(':');
        caption.append(' ');
        LoremIpsum.appendLoremIpsum(caption, this.m_rand);
      }
      for (i = (1 + this.m_rand.nextInt(10 * c)); (--i) >= 0;) {
        try (final IFigure fig = fs.figure(
            this.__getLabel(ELabelType.SUBFIGURE),
            RandomUtils.longToString(null, this.__nextFigure()))) {
          this.__fillFigure(fig, null);
        }
      }
    }
  }

  /**
   * get a point
   * 
   * @param r
   *          the rectangle
   * @return the point
   */
  private final Point2D __randomPoint(final Rectangle2D r) {
    return new Point2D.Double(
        //
        (r.getX() + (this.m_rand.nextDouble() * r.getWidth())),
        (r.getY() + (this.m_rand.nextDouble() * r.getHeight())));
  }

  /**
   * get a random polygon
   * 
   * @param r
   *          the rectangle
   * @return the point
   */
  private final double[][] __randomPolygon(final Rectangle2D r) {
    final double[][] data;
    double x, y;
    int dirX, dirY;
    Point2D p;
    int i;

    data = new double[2][this.m_rand.nextInt(100) + 3];
    p = this.__randomPoint(r);
    data[0][0] = p.getX();
    data[1][0] = p.getY();

    dirX = dirY = 0;
    for (i = 1; i < data[0].length; i++) {

      if (this.m_rand.nextInt(10) <= 0) {
        p = this.__randomPoint(r);
        data[0][i] = p.getX();
        data[1][i] = p.getY();
      } else {
        do {
          dirX += (this.m_rand.nextBoolean() ? (1) : (-1));
          dirY += (this.m_rand.nextBoolean() ? (1) : (-1));
        } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
            && ((dirX == 0) && (dirY == 0)));

        data[0][i] = data[0][i - 1];

        x = (this.m_rand.nextDouble() * 0.05d * r.getWidth());
        if (dirX > 0) {
          data[0][i] += x;
        } else {
          if (dirX < 0) {
            data[0][i] -= x;
          }
        }

        data[1][i] = data[1][i - 1];
        y = (this.m_rand.nextDouble() * 0.05d * r.getHeight());
        if (dirX > 0) {
          data[1][i] += y;
        } else {
          if (dirX < 0) {
            data[1][i] -= y;
          }
        }
      }

    }

    return data;
  }

  /**
   * fill a figure
   * 
   * @param fig
   *          the figure
   * @param v
   *          the figure size
   */
  private final void __fillFigure(final IFigure fig, final EFigureSize v) {
    this.__useLabel(fig);

    try (final IPlainText cap = fig.caption()) {
      if (v != null) {
        cap.append("A figure of size "); //$NON-NLS-1$
        cap.append(v.toString());
        cap.append(':');
        cap.append(' ');
      }
      LoremIpsum.appendLoremIpsum(cap, this.m_rand, 20);
    }

    try (final Graphic g = fig.body()) {
      switch (this.m_rand.nextInt(2)) {
        case 0: {
          LineChartExample.randomLineChart(this.m_rand, g,
              this.m_doc.getStyles(), this.__randomChartDriver());
          break;
        }
        default: {
          this.__randomGraphic(g);
          break;
        }
      }
    }
  }

  /**
   * select a random chart driver
   * 
   * @return the chart driver
   */
  private final IChartDriver __randomChartDriver() {
    return LineChartExample.DRIVERS.get(this.m_rand
        .nextInt(LineChartExample.DRIVERS.size()));
  }

  /**
   * Fill a graphic with random contents
   * 
   * @param g
   *          the graphic
   */
  private final void __randomGraphic(final Graphic g) {
    final Rectangle2D r;
    final ArrayList<AffineTransform> at;
    int k, e, i, limit;
    Point2D a, b;
    double[][] d;
    MemoryTextOutput mo;
    FontStyle fs;

    at = new ArrayList<>();
    e = 500;
    mo = new MemoryTextOutput();
    fs = null;
    at.add(g.getTransform());

    r = g.getBounds();
    k = 0;
    limit = (RandomDocumentExample.MAX_ITERATIONS - (this.m_totalIterations++));
    do {
      switch (this.m_rand.nextInt(15)) {
        case 0: {
          i = this.m_fonts.length;
          if (i > 0) {
            g.setFont((fs = this.m_fonts[this.m_rand.nextInt(i)])
                .getFont());
            break;
          }
        }
        case 1: {
          i = this.m_colors.length;
          if (i > 0) {
            g.setColor(this.m_colors[this.m_rand.nextInt(i)]);
            break;
          }
        }
        case 2: {
          i = this.m_strokes.length;
          if (i > 0) {
            g.setStroke(this.m_strokes[this.m_rand.nextInt(i)]);
            break;
          }
        }
        case 3: {
          trans: for (;;) {
            try {
              switch (this.m_rand.nextInt(3)) {
                case 0: {
                  g.shear((-1d + (2d * this.m_rand.nextDouble())),
                      (-1d + (2d * this.m_rand.nextDouble())));
                  break;
                }
                case 1: {
                  g.rotate(Math.PI
                      * (-1d + (2d * this.m_rand.nextDouble())));
                  break;
                }
                default: {
                  g.scale(//
                      (1d + ((this.m_rand.nextDouble() * 0.1d) - 0.05d)),//
                      (1d + ((this.m_rand.nextDouble() * 0.1d) - 0.05d)));
                }
              }
              at.add(g.getTransform());
              break trans;
            } catch (final Throwable tt) {
              //
            }
          }
          break;
        }
        case 4: {
          if (at.size() > 1) {
            at.remove(at.size() - 1);
            g.setTransform(at.get(at.size() - 1));
          }
          break;
        }

        case 5: {
          a = this.__randomPoint(r);
          b = this.__randomPoint(r);
          g.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
          k++;
          break;
        }
        case 6: {
          a = this.__randomPoint(r);
          b = this.__randomPoint(r);
          g.drawRect(a.getX(), a.getY(), (b.getX() - a.getX()),
              (b.getY() - a.getY()));
          k++;
          break;
        }
        case 7: {
          a = this.__randomPoint(r);
          b = this.__randomPoint(r);
          g.fillRect(a.getX(), a.getY(), (b.getX() - a.getX()),
              (b.getY() - a.getY()));
          k++;
          break;
        }
        case 8: {
          a = this.__randomPoint(r);
          b = this.__randomPoint(r);
          g.drawArc(a.getX(), a.getY(), (b.getX() - a.getX()),
              (b.getY() - a.getY()),
              (Math.PI * 2d * this.m_rand.nextDouble()),
              (Math.PI * 2d * this.m_rand.nextDouble()));
          k++;
          break;
        }
        case 9: {
          a = this.__randomPoint(r);
          b = this.__randomPoint(r);
          g.fillArc(a.getX(), a.getY(), (b.getX() - a.getX()),
              (b.getY() - a.getY()),
              (Math.PI * 2d * this.m_rand.nextDouble()),
              (Math.PI * 2d * this.m_rand.nextDouble()));
          k++;
          break;
        }
        case 10: {
          d = this.__randomPolygon(r);
          g.drawPolygon(d[0], d[1], d[0].length);
          k++;
          break;
        }
        case 11: {
          d = this.__randomPolygon(r);
          g.fillPolygon(d[0], d[1], d[0].length);
          k++;
          break;
        }
        case 12: {
          d = this.__randomPolygon(r);
          g.drawPolyline(d[0], d[1], d[0].length);
          k++;
          break;
        }

        default: {
          a = this.__randomPoint(r);
          mo.clear();
          if ((fs != null) && (this.m_rand.nextBoolean())) {
            fs.appendDescription(ETextCase.AT_SENTENCE_START, mo, false);
            mo.append(' ');
          }
          LoremIpsum.appendLoremIpsum(mo, this.m_rand,
              this.m_rand.nextInt(10) + 1);
          g.drawString(mo.toString(), a.getX(), a.getY());
          mo.clear();
          k++;
        }
      }
    } while (((limit--) > 0) && ((k < e) || (this.m_rand.nextInt(10) > 0)));
  }

  /**
   * create an equation
   * 
   * @param sb
   *          the section body
   */
  private final void __createEquation(final ISectionBody sb) {

    try (final IEquation equ = sb.equation(this
        .__getLabel(ELabelType.EQUATION))) {
      this.__done(RandomDocumentExample.EQUATION);
      this.__useLabel(equ);
      this.__fillMath(equ, 1, 1, 5);
    }
  }

  /**
   * create an inline math
   * 
   * @param sb
   *          the text
   */
  private final void __createInlineMath(final IComplexText sb) {
    try (final IMath equ = sb.inlineMath()) {

      this.__done(RandomDocumentExample.INLINE_MATH);
      this.__fillMath(equ, 1, 1, 4);
    }
  }

  /**
   * fill a mathematics context
   * 
   * @param math
   *          the maths context
   * @param minArgs
   *          the minimum arguments
   * @param maxArgs
   *          the maximum arguments
   * @param depth
   *          the depth
   */
  private final void __fillMath(final IMath math, final int minArgs,
      final int maxArgs, final int depth) {
    int args;

    args = 0;

    do {
      switch (this.m_rand.nextInt((depth <= 0) ? 3 : 23)) {

        case 0: {
          try (final IText text = math.name()) {
            text.append("id"); //$NON-NLS-1$
            text.append(this.m_rand.nextInt(10));
          }
          break;
        }

        case 1: {
          try (final IText text = math.number()) {
            text.append(this.m_rand.nextInt(201) - 100);
          }
          break;
        }

        case 2: {
          try (final IText text = math.text()) {
            text.append("text_"); //$NON-NLS-1$
            text.append(this.m_rand.nextInt(10));
          }
          break;
        }

        case 3: {
          try (final IMath nm = math.add()) {
            this.__fillMath(nm, 2, 20, (depth - 1));
          }
          break;
        }

        case 4: {
          try (final IMath nm = math.sub()) {
            this.__fillMath(nm, 2, 20, (depth - 1));
          }
          break;
        }

        case 5: {
          try (final IMath nm = math.mul()) {
            this.__fillMath(nm, 2, 20, (depth - 1));
          }
          break;
        }

        case 6: {
          try (final IMath nm = math.div()) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 7: {
          try (final IMath nm = math.divInline()) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 8: {
          try (final IMath nm = math.mod()) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 9: {
          try (final IMath nm = math.log()) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 10: {
          try (final IMath nm = math.ln()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 11: {
          try (final IMath nm = math.ld()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 12: {
          try (final IMath nm = math.lg()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 13: {
          try (final IMath nm = math.sqrt()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 14: {
          try (final IMath nm = math.root()) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 15: {
          try (final IMath nm = math
              .compare(RandomDocumentExample.COMP[this.m_rand
                  .nextInt(RandomDocumentExample.COMP.length)])) {
            this.__fillMath(nm, 2, 2, (depth - 1));
          }
          break;
        }

        case 16: {
          try (final IMath nm = math.inBraces()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 17: {
          try (final IMath nm = math.negate()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 18: {
          try (final IMath nm = math.abs()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 19: {
          try (final IMath nm = math.factorial()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 20: {
          try (final IMath nm = math.sin()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 21: {
          try (final IMath nm = math.cos()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        case 22: {
          try (final IMath nm = math.tan()) {
            this.__fillMath(nm, 1, 1, (depth - 1));
          }
          break;
        }

        default: {
          throw new IllegalStateException();
        }
      }

      args++;
    } while (((this.m_totalIterations++) < RandomDocumentExample.MAX_ITERATIONS)
        && ((args < minArgs) || ((args < maxArgs) && this.m_rand
            .nextBoolean())));

  }
}
