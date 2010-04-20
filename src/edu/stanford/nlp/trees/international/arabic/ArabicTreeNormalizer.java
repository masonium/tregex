package edu.stanford.nlp.trees.international.arabic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.BobChrisTreeNormalizer;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeFactory;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.Filter;

/**
 * A first-version tree normalizer for the Arabic Penn Treebank.
 * Just like BobChrisTreeNormalizer but:
 * <ul>
 * <li> Adds a ROOT node to the top of every tree
 * <li> Strips all the interesting stuff off of the POS tags.
 * <li> Can keep NP-TMP annotations (retainNPTmp parameter)
 * <li> Can keep whatever annotations there are on verbs that are sisters
 *           to predicatively marked (-PRD) elements (markPRDverb parameter)
 *           [Chris Nov 2006: I'm a bit unsure on that one!]
 * <li> Can keep categories unchanged, i.e., not mapped to basic categories
 *           (changeNoLabels parameter)
 * <li> Counts pronoun deletions ("nullp" and "_") as empty; filters
 * </ul>
 *
 * @author Roger Levy
 * @author Anna Rafferty
 */
public class ArabicTreeNormalizer extends BobChrisTreeNormalizer {

  /**
   *
   */
  private static final long serialVersionUID = -1592231121068698494L;
  private boolean retainNPTmp;
  private boolean retainNPSbj;
  private boolean markPRDverb;
  private boolean collapse3LetterPrepositionVariants;
  private boolean changeNoLabels;
  private final Pattern prdPattern = Pattern.compile("^[A-Z]+-PRD");
  private TregexPattern prdVerbPattern;
  private TregexPattern npSbjPattern;


  //WSGDEBUG
  private boolean retainPPClr;

  public ArabicTreeNormalizer(boolean retainNPTmp, boolean markPRDverb,
                              boolean changeNoLabels, boolean collapsePreps,
                              boolean retainNPSbj, boolean retainPPClr) {
    super(new ArabicTreebankLanguagePack());
    this.retainNPTmp = retainNPTmp;
    this.retainNPSbj = retainNPSbj;
    this.markPRDverb = markPRDverb;
    this.changeNoLabels = changeNoLabels;
    this.collapse3LetterPrepositionVariants = collapsePreps;

    this.retainPPClr = retainPPClr;

    try {
      prdVerbPattern  = TregexPattern.compile("/^V[^P]/ > VP $ /-PRD$/=prd");

      //This pattern is used to remove all NP subjects that *do not* occur
      //in verb-initial clauses
      npSbjPattern = TregexPattern.compile("/^NP-SBJ/ !> @VP");

    } catch(ParseException e) {
      System.out.println(e);
      throw new RuntimeException();
    }
    // override default EmptyFilter
    emptyFilter = new ArabicEmptyFilter();
  }

  public ArabicTreeNormalizer(boolean retainNPTmp, boolean markPRDverb,
      boolean changeNoLabels, boolean collapsePreps) {
    this(retainNPTmp, markPRDverb, changeNoLabels, collapsePreps,false, false);
  }

  public ArabicTreeNormalizer(boolean retainNPTmp, boolean markPRDverb,
                              boolean changeNoLabels) {
    this(retainNPTmp, markPRDverb, changeNoLabels, false);
  }

  public ArabicTreeNormalizer(boolean retainNPTmp, boolean markPRDverb) {
    this(retainNPTmp,markPRDverb,false);
  }

  public ArabicTreeNormalizer(boolean retainNPTmp) {
    this(retainNPTmp,false);
  }

  public ArabicTreeNormalizer() {
    this(false);
  }


  @Override
  public String normalizeNonterminal(String category) {
    if (changeNoLabels) {
      return category;
    } else if (retainNPTmp && category != null && category.startsWith("NP-TMP")) {
      return "NP-TMP";
    } else if (retainNPSbj && category != null && category.startsWith("NP-SBJ")) {
      return "NP-SBJ";
    } else if (retainPPClr && category != null && category.startsWith("PP-CLR")) {
      return "PP-CLR";
    } else if (markPRDverb && category != null && prdPattern.matcher(category).matches()) {
      return category;
    } else {
      // otherwise, return the basicCategory (and turn null to ROOTarg0)
      return super.normalizeNonterminal(category);
    }
  }

  /** Miscellany:
   * <ul>
   * <li> Escapes out "/" and "*" tokens (this is ugly, should be fixed!)
   * </ul>
   * todo: cdm 2009: Is this really needed for Arabic??
   */
  @Override
  public String normalizeTerminal(String leaf) {
    if(changeNoLabels)
      return leaf;
    if(escape && escapeCharacters.contains(leaf))
      return "\\" + leaf;
    return super.normalizeTerminal(leaf);
  }

  private static final boolean escape = false;
  private static final Collection<String> escapeCharacters = Arrays.asList("/","*");


  /** This one extends the one in BobChrisTreeNormalizer to also delete
   *  empty pronouns.
   */
  public static class ArabicEmptyFilter implements Filter<Tree> {

    private static final long serialVersionUID = 7417844982953945964L;

    /** Doesn't accept nodes that only cover an empty. */
    public boolean accept(Tree t) {
      Tree[] kids = t.children();
      Label l = t.label();
      // Delete empty/trace nodes
      if ((l != null) && "-NONE-".equals(l.value()) &&
          !t.isLeaf() && kids.length == 1 && kids[0].isLeaf()) {
        return false;
      }
      //Delete pronoun deletions
      if ((l != null) && ("PRP".equals(l.value()) && kids.length == 1 && kids[0].isLeaf())) {
        //check if its kid is a pronoun deletion
        Label kidLabel = kids[0].label();
        if(kidLabel != null && ("nullp".equals(l.value()) || "_".equals(l.value()) || "\u005F".equals(l.value())))
          return false;
      }

      return true;
    }
      //    private static final long serialVersionUID = 1L;

  } // end class EmptyFilter


  private boolean warnedPrepositions = false;

  private void warnIfFirstTimePrep() {
    if ( ! warnedPrepositions) {
      warnedPrepositions = true;
      System.err.println("ATBNormalizer: mapping preposition forms: Ely to ElY; <ly, AlY, Aly to <lY; ldy to ldY; Hty to HtY");
    }
  }

  private void do3LetterPrepositionVariants(Tree t) {
    // t is a leaf
    // normalize preposition forms that vary to "underlying" forms
    // this will only be effective if the same normalization happens on input text
    // the second set of conditions mirrors the first half but in utf8 not buckwalter
    if (t.value().equals("Ely")) {
      warnIfFirstTimePrep();
      t.label().setValue("ElY");  // preposition meaning "on" should consistently be ElY
    } else if (t.value().equals("<ly") || t.firstChild().value().equals("Aly") || t.firstChild().value().equals("AlY")) {
      warnIfFirstTimePrep();
      t.label().setValue("<lY");
    } else if (t.value().equals("ldy")) {
      warnIfFirstTimePrep();
      t.label().setValue("ldY"); // preposition meaning "at", "having" is underlyingly "ldY"
    } else  if (t.value().equals("Hty")) {
      warnIfFirstTimePrep();
      t.label().setValue("HtY"); // preposition meaning "at", "having" is underlyingly "ldY"
    } else if (t.value().equals("علي")) {
      warnIfFirstTimePrep();
      t.label().setValue("على");  // preposition meaning "on" should consistently be ElY
    } else if (t.value().equals("إلي") || t.firstChild().value().equals("الي") || t.firstChild().value().equals("الى")) {
      warnIfFirstTimePrep();
      t.label().setValue("إلى");
    } else if (t.value().equals("لدي")) {
      warnIfFirstTimePrep();
      t.label().setValue("لدى"); // preposition meaning "at", "having" is underlyingly "ldY"
    } else  if (t.value().equals("حتي")) {
      warnIfFirstTimePrep();
      t.label().setValue("حتى"); // preposition meaning "at", "having" is underlyingly "ldY"
    }
  }

  @Override
  public Tree normalizeWholeTree(Tree tree, TreeFactory tf) {
    tree = tree.prune(emptyFilter, tf).spliceOut(aOverAFilter, tf);

    for (Tree t : tree) {
      if (t.isPreTerminal()) {
        if (collapse3LetterPrepositionVariants) {
          // CDM Nov 2006: It's not clear to me that this is actually an error
          // since ElY can change to Ely with pronoun objects (Ryding p.29)
          // but it seems a reasonable normalization to do
          if ((t.value().equals("PREP") || t.value().equals("IN"))) {
            do3LetterPrepositionVariants(t.firstChild());
          }
        }
        if (t.label().value() == null || t.label().value().equals("")) {
          System.err.println("ATBNormalizer ERROR: missing tag: " + t);
        }
        if (t.label().value().equals("NO_FUNC")) {
          if (t.firstChild().label().value().equals(".") || t.firstChild().label().value().equals("\"")) {
            System.err.println("ArabicTreeNormalizer: changing NO_FUNC tag to PUNC: " + t);
            t.label().setValue("PUNC");
          }
        }
      }
      if (t.isPreTerminal() || t.isLeaf()) {
        continue;
      }
      // there are some nodes "/" missing preterminals.  We'll splice in a tag for these.
      int nk = t.numChildren();
      List<Tree> newKids = new ArrayList<Tree>(nk);
      for (int j = 0; j < nk; j++) {
        Tree child = t.getChild(j);
        if (child.isLeaf()) {
          System.err.println("ArabicTreeNormalizer: missing tag for " + child.label().value() + ", splicing in DUMMYTAG, in tree " + t);
          newKids.add(tf.newTreeNode("DUMMYTAG", Collections.singletonList(child)));
        } else {
          newKids.add(child);
        }
      }
      t.setChildren(newKids);
    }
    // special global coding for moving PRD annotation from constituent to verb tag.
    if (markPRDverb) {
      TregexMatcher m = prdVerbPattern.matcher(tree);
      Tree match = null;
      while (m.find()) {
        if (m.getMatch() != match) {
          match = m.getMatch();
          match.label().setValue(match.label().value() + "-PRDverb");
          Tree prd = m.getNode("prd");
          prd.label().setValue(super.normalizeNonterminal(prd.label().value()));
        }
      }
    }

    //Mark *only* subjects in verb-initial clauses
    if(retainNPSbj) {
      TregexMatcher m = npSbjPattern.matcher(tree);
      while (m.find()) {
        Tree match = m.getMatch();
        match.label().setValue("NP");
      }
    }

//    if (normalizeConj && tree.isPreTerminal() && tree.children()[0].label().value().equals("w") && wrongConjPattern.matcher(tree.label().value()).matches()) {
//      System.err.print("ATBNormalizer ERROR: bad CC remapped tree " + tree + " to ");
//      tree.label().setValue("CC");
//      System.err.println(tree);
//    }
    if (tree.isPreTerminal()) {
      // The whole tree is a bare tag: bad!
      String val = tree.label().value();
      if (val.equals("CC") || val.equals("PUNC") || val.equals("CONJ")) {
        System.err.println("ATBNormalizer ERROR: bare tagged word: " + tree +
                           " being wrapped in FRAG");
        tree = tf.newTreeNode("FRAG", Collections.singletonList(tree));
      } else {
        System.err.println("ATBNormalizer ERROR: bare tagged word: " + tree +
                           ": fix it!!");
      }
    }
    if (! tree.label().value().equals("ROOT")) {
      tree = tf.newTreeNode("ROOT", Collections.singletonList(tree));
    }
    return tree;
  }

//  private static final Pattern wrongConjPattern = Pattern.compile("NNP|NO_FUNC|NOFUNC|IN");

}
