package edu.stanford.nlp.trees.international.arabic;

import java.io.Reader;
import java.io.Serializable;

import edu.stanford.nlp.trees.*;
import edu.stanford.nlp.util.Filter;


/** Reads ArabicTreebank trees.  See {@link ArabicTreeNormalizer} for the
 *  meaning of the constructor parameters.
 *
 *  @author Roger Levy
 *  @author Christopher Manning
 */
public class ArabicTreeReaderFactory implements TreeReaderFactory, Serializable {

  private static final long serialVersionUID = 1973767605277873017L;

  private boolean retainNPTmp;
  private boolean retainNPSbj;
  private boolean retainPRD;
  private boolean changeNoLabels;
  private boolean mapPrepositions;
  private boolean filterX;
  private boolean noNormalization;
  
  //WSGDEBUG Under test
  private boolean retainPPClr;

  public ArabicTreeReaderFactory() {
    this(false, false, false, false, false, false, false, false);
  }

  public ArabicTreeReaderFactory(boolean retainNPTmp, boolean retainPRD,
      boolean changeNoLabels, boolean filterX,
      boolean mapPrepositions, boolean retainNPSbj,
      boolean noNormalization, boolean retainPPClr) {
    this.retainNPTmp = retainNPTmp;
    this.retainPRD = retainPRD;
    this.changeNoLabels = changeNoLabels;
    this.filterX = filterX;
    this.mapPrepositions = mapPrepositions;
    this.retainNPSbj = retainNPSbj;
    this.noNormalization = noNormalization;
    this.retainPPClr = retainPPClr;
  }

  public TreeReader newTreeReader(Reader in) {
    TreeReader tr = null;
    if(noNormalization) {
      tr = new PennTreeReader(in, new LabeledScoredTreeFactory(), new TreeNormalizer(), new ArabicTreebankTokenizer(in));
    } else
      tr = new PennTreeReader(in, new LabeledScoredTreeFactory(), new ArabicTreeNormalizer(retainNPTmp,retainPRD,changeNoLabels, mapPrepositions,retainNPSbj, retainPPClr), new ArabicTreebankTokenizer(in));

    if (filterX)
      tr = new FilteringTreeReader(tr, new XFilter());

    return tr;
  }


  static class XFilter implements Filter<Tree> {

    private static final long serialVersionUID = -4522060160716318895L;

    public XFilter() {}

    public boolean accept(Tree t) {
      return ! (t.numChildren() == 1 && "X".equals(t.firstChild().value()));
    }

  }


  public static class ArabicXFilteringTreeReaderFactory extends ArabicTreeReaderFactory {

    private static final long serialVersionUID = -2028631608727725548L;

    public ArabicXFilteringTreeReaderFactory() {
      super(false, false, false, true, false, false, false, false);
    }

  }


  public static class ArabicRawTreeReaderFactory extends ArabicTreeReaderFactory {

    private static final long serialVersionUID = -5693371540982097793L;

    public ArabicRawTreeReaderFactory() {
      super(false, false, true, false, false, false, false, false);
    }

    public ArabicRawTreeReaderFactory(boolean noNormalization) {
      super(false, false, true, false, false, false, noNormalization, false);
    }
  }

}
