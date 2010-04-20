package edu.stanford.nlp.trees;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.io.FileSequentialCollection;
import edu.stanford.nlp.io.RuntimeIOException;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.util.Filter;
import edu.stanford.nlp.util.Timing;
import edu.stanford.nlp.stats.TwoDimensionalCounter;
import edu.stanford.nlp.stats.Counter;


/**
 * A <code>DiskTreebank</code> is a <code>Collection</code> of
 * <code>Tree</code>s.
 * A <code>DiskTreebank</code> object stores merely the information to
 * get at a corpus of trees that is stored on disk.  Access is usually
 * via apply()'ing a TreeVisitor to each Tree in the Treebank or by using
 * an iterator() to get an iteration over the Trees.
 * <p/>
 * If the root Label of the Tree objects built by the TreeReader
 * implements HasIndex, then the filename and index of the tree in
 * a corpus will be inserted as they are read in.
 *
 * @author Christopher Manning
 */
public final class DiskTreebank extends Treebank {

  private static final boolean PRINT_FILENAMES = false;

  private final ArrayList<File> filePaths = new ArrayList<File>();
  private final ArrayList<FileFilter> fileFilters = new ArrayList<FileFilter>();

  /**
   * Maintains as a class variable the <code>File</code> from which
   * trees are currently being read.
   */
  private File currentFile; // = null;


  /**
   * Create a new DiskTreebank.
   * The trees are made with a <code>LabeledScoredTreeReaderFactory</code>.
   * <p/>
   * <i>Compatibility note: Until Sep 2004, this used to create a Treebank
   * with a SimpleTreeReaderFactory, but this was changed as the old
   * default wasn't very useful, especially to naive users.</i>
   */
  public DiskTreebank() {
    this(new LabeledScoredTreeReaderFactory());
  }

  /**
   * Create a new treebank, set the encoding for file access.
   *
   * @param encoding The charset encoding to use for treebank file decoding
   */
  public DiskTreebank(String encoding) {
    this(new LabeledScoredTreeReaderFactory(), encoding);
  }

  /**
   * Create a new DiskTreebank.
   *
   * @param trf the factory class to be called to create a new
   *            <code>TreeReader</code>
   */
  public DiskTreebank(TreeReaderFactory trf) {
    super(trf);
  }

  /**
   * Create a new DiskTreebank.
   *
   * @param trf      the factory class to be called to create a new
   *                 <code>TreeReader</code>
   * @param encoding The charset encoding to use for treebank file decoding
   */
  public DiskTreebank(TreeReaderFactory trf, String encoding) {
    super(trf, encoding);
  }

  /**
   * Create a new Treebank.
   * The trees are made with a <code>LabeledScoredTreeReaderFactory</code>.
   * <p/>
   * <i>Compatibility note: Until Sep 2004, this used to create a Treebank
   * with a SimpleTreeReaderFactory, but this was changed as the old
   * default wasn't very useful, especially to naive users.</i>
   *
   * @param initialCapacity The initial size of the underlying Collection.
   *                        For a <code>DiskTreebank</code>, this parameter is ignored.
   */
  public DiskTreebank(int initialCapacity) {
    this(initialCapacity, new LabeledScoredTreeReaderFactory());
  }

  /**
   * Create a new Treebank.
   *
   * @param initialCapacity The initial size of the underlying Collection,
   *                        For a <code>DiskTreebank</code>, this parameter is ignored.
   * @param trf             the factory class to be called to create a new
   *                        <code>TreeReader</code>
   */
  @SuppressWarnings({"UnusedDeclaration"})
  public DiskTreebank(int initialCapacity, TreeReaderFactory trf) {
    this(trf);
  }


  /**
   * Empty a <code>Treebank</code>.
   */
  @Override
  public void clear() {
    filePaths.clear();
    fileFilters.clear();
  }

  /**
   * Load trees from given directory.  This version just records
   * the paths to be processed, and actually processes them at apply time.
   *
   * @param path file or directory to load from
   * @param filt a FilenameFilter of files to load
   */
  @Override
  public void loadPath(File path, FileFilter filt) {
    filePaths.add(path);
    fileFilters.add(filt);
  }

  /**
   * Applies the TreeVisitor to to all trees in the Treebank.
   *
   * @param tp A class that can process trees.
   */
  @Override
  public void apply(final TreeVisitor tp) {
    for (Tree t : this) {
      tp.visitTree(t);
    }
  }

  /**
   * Return the <code>File</code> from which trees are currently being
   * read by an Iterator or <code>apply()</code> and passed to a
   * <code>TreePprocessor</code>.
   * <p/>
   * This is useful if one wants to map the original file and
   * directory structure over to a set of modified trees.  New code
   * might prefer to build trees with labels that implement
   * HasIndex.
   *
   * @return the file that trees are currently being read from, or
   *         <code>null</code> if no file is currently open
   */
  public File getCurrentFile() {
    return currentFile;
  }


  private class DiskTreebankIterator implements Iterator<Tree> {

    private int fileUpto; // = 0 (will start on index array 0)
    Iterator<File> fileIterator;
    private TreeReader tr;
    private Tree storedTree;  // null means iterator is exhausted (or not yet constructed)

    private DiskTreebankIterator() {
      storedTree = primeNextTree();
    }

    private Tree primeNextTree() {
      Tree nextTree = null;
      int fpsize = filePaths.size();
      while (nextTree == null && fileUpto <= fpsize) {
        if (tr == null && (fileIterator == null || ! fileIterator.hasNext())) {
          if (fileUpto < fpsize) {
            FileSequentialCollection fsc = new FileSequentialCollection(Collections.singletonList(filePaths.get(fileUpto)), fileFilters.get(fileUpto));
            fileIterator = fsc.iterator();
          }
          // else we're finished, but increment anyway so we leave outermost loop
          fileUpto++;
        }
        while (nextTree == null && (tr != null || (fileIterator != null && fileIterator.hasNext()))) {
          try {
            while (nextTree == null && (tr != null || (fileIterator != null && fileIterator.hasNext()))) {
              if (tr != null) {
                nextTree = tr.readTree();
                if (nextTree == null) {
                  tr.close();
                  tr = null;
                }
              }
              if (nextTree == null && (fileIterator != null && fileIterator.hasNext())) {
                currentFile = fileIterator.next();
                // maybe print file name to stdout to get some feedback
                if (PRINT_FILENAMES) {
                  System.err.println(currentFile);
                }
                tr = treeReaderFactory().newTreeReader(new BufferedReader(new InputStreamReader(new FileInputStream(currentFile), encoding())));
              }
            }
          } catch (IOException e) {
            throw new RuntimeIOException("primeNextTree IO Exception in file " + currentFile, e);
          }
        }
      }
      if (nextTree == null) {
        currentFile = null;
      }
      return nextTree;
    }


    /**
     * Returns true if the iteration has more elements.
     */
    public boolean hasNext() {
      return storedTree != null;
    }

    /**
     * Returns the next element in the iteration.
     */
    public Tree next() {
      if (storedTree == null) {
        throw new NoSuchElementException();
      }
      Tree ret = storedTree;
      storedTree = primeNextTree();
      return ret;
    }

    /**
     * Not supported
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

  } // end class DiskTreebankIterator


  /**
   * Return an Iterator over Trees in the Treebank.  This is implemented
   * by building per-file MemoryTreebanks for the files in the
   * DiskTreebank.  As such, it isn't as efficient as using
   * <code>apply()</code>.
   */
  @Override
  public Iterator<Tree> iterator() {
    return new DiskTreebankIterator();
  }


  /**
   * Loads treebank and prints it.
   * All files below the designated <code>filePath</code> within the given
   * number range if any are loaded.  You can normalize the trees or not
   * (English-specific) and print trees one per line up to a certain length
   * (for EVALB).
   * <p>
   * Usage: <code>
   * java edu.stanford.nlp.trees.DiskTreebank [-maxLength n|-normalize|-treeReaderFactory class] filePath [numberRanges]
   * </code>
   *
   * @param args Array of command-line arguments
   * @throws IOException If there is a treebank file access problem
   */
  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      System.err.println("This main method will let you variously manipulate and view a treebank.");
      System.err.println("Usage: java DiskTreebank [-flags]* treebankPath fileRanges");
      System.err.println("Useful flags include:");
      System.err.println("\t-maxLength n\t-suffix ext\t-treeReaderFactory class");
      System.err.println("\t-pennPrint\t-encoding enc\t-tlp class\t-sentenceLengths");
      System.err.println("\t-summary\t-decimate\t-yield\t-correct\t-punct");
      return;
    }
    int i = 0;
    int maxLength = -1;
    boolean normalized = false;
    boolean decimate = false;
    boolean pennPrintTrees = false;
    boolean correct = false;
    boolean summary = false;
    boolean timing = false;
    boolean yield = false;
    boolean punct = false;
    boolean sentenceLengths = false;
    boolean countTaggings = false;
    String decimatePrefix = null;
    String encoding = TreebankLanguagePack.DEFAULT_ENCODING;
    String suffix = Treebank.DEFAULT_TREE_FILE_SUFFIX;
    TreeReaderFactory trf = null;
    TreebankLanguagePack tlp = null;

    while (i < args.length && args[i].startsWith("-")) {
      if (args[i].equals("-maxLength") && i + 1 < args.length) {
        maxLength = Integer.parseInt(args[i+1]);
        i += 2;
      } else if (args[i].equals("-normalized")) {
        normalized = true;
        i += 1;
      } else if (args[i].equalsIgnoreCase("-tlp")) {
        try {
          final Object o = Class.forName(args[i+1]).newInstance();
          tlp = (TreebankLanguagePack) o;
          trf = tlp.treeReaderFactory();
        } catch (Exception e) {
          System.err.println("Couldn't instantiate as TreebankLangParserParams: " + args[i+1]);
          return;
        }
        i += 2;
      } else if (args[i].equals("-treeReaderFactory") || args[i].equals("-trf")) {
        try {
          final Object o = Class.forName(args[i+1]).newInstance();
          trf = (TreeReaderFactory) o;
        } catch (Exception e) {
          System.err.println("Couldn't instantiate as TreeReaderFactory: " + args[i+1]);
          return;
        }
        i += 2;
      } else if (args[i].equals("-suffix")) {
        suffix = args[i+1];
        i += 2;
      } else if (args[i].equals("-decimate")) {
        decimate = true;
        decimatePrefix = args[i+1];
        i += 2;
      } else if (args[i].equals("-encoding")) {
        encoding = args[i+1];
        i += 2;
      } else if (args[i].equals("-correct")) {
        correct = true;
        i += 1;
      } else if (args[i].equals("-summary")) {
        summary = true;
        i += 1;
      } else if (args[i].equals("-yield")) {
        yield = true;
        i += 1;
      } else if (args[i].equals("-punct")) {
        punct = true;
        i += 1;
      } else if (args[i].equals("-pennPrint")) {
        pennPrintTrees = true;
        i++;
      } else if (args[i].equals("-timing")) {
        timing = true;
        i++;
      } else if (args[i].equals("-countTaggings")) {
        countTaggings = true;
        i++;
      } else if (args[i].equals("-sentenceLengths")) {
        sentenceLengths = true;
        i++;
      } else {
        System.err.println("Unknown option: " + args[i]);
        i++;
      }
    }
    Treebank treebank;
    if (trf == null) {
      trf = new TreeReaderFactory() {
          public TreeReader newTreeReader(Reader in) {
            return new PennTreeReader(in, new LabeledScoredTreeFactory());
          }
        };
    }
    if (normalized) {
      treebank = new DiskTreebank();
    } else {
      treebank = new DiskTreebank(trf, encoding);
    }

    final PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, encoding), true);

    if (i + 1 < args.length ) {
      treebank.loadPath(args[i], new NumberRangesFileFilter(args[i+1], true));
    } else {
      treebank.loadPath(args[i], suffix, true);
    }
    // System.err.println("Loaded " + treebank.size() + " trees from " + args[i]);

    if (summary) {
      System.out.println(treebank.textualSummary());
    }
    if (sentenceLengths) {
      sentenceLengths(treebank, args[i], ((i+1)<args.length ? args[i+1]: null), pw);
    }

    if (punct) {
      printPunct(treebank, tlp, pw);
    }

    if (correct) {
      treebank = new EnglishPTBTreebankCorrector().transformTrees(treebank);
    }

    if (pennPrintTrees) {
      treebank.apply(new TreeVisitor() {
          public void visitTree(Tree tree) {
            tree.pennPrint(pw);
            pw.println();
          }
        });
    }

    if (countTaggings) {
      countTaggings(treebank, pw);
    }

    if (yield) {
      treebank.apply(new TreeVisitor() {
          public void visitTree(Tree tree) {
            pw.println(tree.yield().toString());
          }
        });
    }

    if (decimate) {
      Writer w1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decimatePrefix + "-train.txt"), encoding));
      Writer w2 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decimatePrefix + "-dev.txt"), encoding));
      Writer w3 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(decimatePrefix + "-test.txt"), encoding));
      treebank.decimate(w1, w2, w3);
    } else if (maxLength >= 0) {
      for (Tree t : treebank) {
        if (t.yield().length() <= maxLength) {
          System.out.println(t);
        }
      }
    } else if (timing) {
      runTiming(treebank);
    }
  } // end main()


  private static void printPunct(Treebank treebank, TreebankLanguagePack tlp, PrintWriter pw) {
    if (tlp == null) {
      System.err.println("The -punct option requires you to specify -tlp");
    } else {
      Filter<String> punctTagFilter = tlp.punctuationTagAcceptFilter();
      for (Tree t : treebank) {
        List<TaggedWord> tws = t.taggedYield();
        for (TaggedWord tw : tws) {
          if (punctTagFilter.accept(tw.tag())) {
            pw.println(tw);
          }
        }
      }
    }
  }

  private static void countTaggings(Treebank tb, final PrintWriter pw) {
    final TwoDimensionalCounter<String,String> wtc = new TwoDimensionalCounter<String,String>();
    tb.apply(new TreeVisitor() {
        public void visitTree(Tree tree) {
          List<TaggedWord> tags = tree.taggedYield();
          for (TaggedWord tag : tags)
            wtc.incrementCount(tag.word(), tag.tag());
        }
      });
    for (String key : wtc.firstKeySet()) {
      pw.print(key);
      pw.print('\t');
      Counter<String> ctr = wtc.getCounter(key);
      for (String k2 : ctr.keySet()) {
        pw.print(k2 + '\t' + ctr.getCount(k2) + '\t');
      }
      pw.println();
    }
  }


  private static void runTiming(Treebank treebank) {
    System.out.println();
    Timing.startTime();
    int num = 0;
    for (Tree t : treebank) {
      num += t.yield().length();
    }
    Timing.endTime("traversing corpus, counting words with iterator");
    System.err.println("There were " + num + " words in the treebank.");

    treebank.apply(new TreeVisitor() {
        int num = 0;
        public void visitTree(final Tree t) {
          num += t.yield().length();
        }
      });
    System.err.println();
    Timing.endTime("traversing corpus, counting words with TreeVisitor");
    System.err.println("There were " + num + " words in the treebank.");

    System.err.println();
    Timing.startTime();
    System.err.println("This treebank contains " + treebank.size() + " trees.");
    Timing.endTime("size of corpus");
  }


  public static void sentenceLengths(Treebank treebank, String name, String range,
                                     PrintWriter pw) {
    final int maxleng = 150;
    int[] lengthCounts = new int[maxleng+2];
    int numSents = 0;
    int longestSeen = 0;
    int totalWords = 0;
    String longSent = "";
    double median = 0.0;
    NumberFormat nf = new DecimalFormat("0.0");
    boolean foundMedian = false;

    for (Tree t : treebank) {
      numSents++;
      int len = t.yield().length();
      if (len <= maxleng) {
        lengthCounts[len]++;
      } else {
        lengthCounts[maxleng+1]++;
      }
      totalWords += len;
      if (len > longestSeen) {
        longestSeen = len;
        longSent = t.toString();
      }
    }
    System.out.print("Files " + name + ' ');
    if (range != null) {
      System.out.print(range + ' ');
    }
    System.out.println("consists of " + numSents + " sentences");
    int runningTotal = 0;
    for (int i = 0; i <= maxleng; i++) {
      runningTotal += lengthCounts[i];
      System.out.println("  " + lengthCounts[i] + " of length " + i +
              " (running total: " + runningTotal + ')');
      if ( ! foundMedian && runningTotal > numSents / 2) {
        if (numSents % 2 == 0 && runningTotal == numSents / 2 + 1) {
          // right on the boundary
          int j = i - 1;
          while (j > 0 && lengthCounts[j] == 0) {
            j--;
          }
          median = ((double) i + j) / 2;
        } else {
          median =  i;
        }
        foundMedian = true;
      }
    }
    if (lengthCounts[maxleng+1] > 0) {
      runningTotal += lengthCounts[maxleng+1];
      System.out.println("  " + lengthCounts[maxleng+1] +
              " of length " + (maxleng+1) + " to " + longestSeen +
              " (running total: " + runningTotal + ')');
    }
    System.out.println("Average length: " +
            nf.format(((double) totalWords) / numSents) + "; median length: " +
            nf.format(median));
    System.out.println("Longest sentence is of length: " + longestSeen);
    pw.println(longSent);
  }

}
