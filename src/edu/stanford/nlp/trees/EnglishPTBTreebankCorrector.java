package edu.stanford.nlp.trees;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.ParseException;
import edu.stanford.nlp.trees.tregex.tsurgeon.Tsurgeon;
import edu.stanford.nlp.trees.tregex.tsurgeon.TsurgeonPattern;
import edu.stanford.nlp.util.Pair;


public class EnglishPTBTreebankCorrector implements TreebankTransformer {

  private static final boolean DEBUG = false;

  // ai is ai n't
  private static final String BE = 
    "/^(?i:am|is|are|was|were|be|being|been|'s|'m|'re|s|ai)$/";

  private static final String HAVE = 
    "/^(?i:has|have|had|having|'s|'ve|'d)$/";
 
  private static final String DO = 
    "/^(?i:do|did|does|doing|done)$/";
 
  /** List of be, have, and get auxiliary forms: things that you would expect
   *  to have a VBN complement if anything verbal.
   *  A few times the apostrophe is missing on "'s", so we have just "s".
   *  Could also add become, feel, seem, remain for some PTB cases, but
   *  maybe should reparse them?
   */ 
  private static final String BE_HAVE_GET = 
    "/^(?i:has|have|had|having|am|is|are|was|were|be|being|been|'s|'ve|'d|'m|'re|s|ai|get|gets|getting|got|gotten)$/";
 
  private static final String MODAL_WORD = 
    "/^(?i:should|would|wo|could|may|might|ca|can|dare|will|'ll|must|shall|sha|'d)$/";

  private static final String MODAL = 
    "[ < (__ < /^(?i:should|would|wo|could|may|might|ca|can|dare|will|'ll|must|shall|sha)$/) | < (MD < /^(?i:'d)$/) ]";

  /** Contexts that take VB complement.  This is a tregex subexpression
   *  that includes the tag.  Something to specify next to VP:
   *  "(@VP" + MODAL_DO_TO + ")".  na is gonna.  sha is shan't (though doesn't occur).
   *  Not modals that take TO: need, ought.  Or mighta.
   *  Note that "'d" can also be "had", so one needs to be careful on that!
   */
  private static final String MODAL_DO_TO = 
    "[ < (__ < /^(?i:do|did|does|doing|done|to|na|should|would|wo|could|may|might|ca|can|dare|will|'ll|must|shall|sha)$/) | < (MD < /^(?i:'d)$/) ]";


  /** Verbs that take bare VB complements.
   *  'say' is sort of special, these are semi-direct speech cases where there
   *  are no inverted quotes but imperatives follow.  Check for
   *  overgeneralization.
   */
  private static final String BARE_VP_VERB = 
    " < (__ < /^(?i:help|helps|helping|helped|make|makes|making|made|see|sees|saw|seen|seeing|hear|heard|hears|hearing|let|lets|letting)$/)";

  private static final String SAY_VERB = 
    " < (__ < /^(?i:say|says|said|saying)$/)";

  /** Fix all the English Penn Treebank errors, or at least some of them (!).
   */
  public MemoryTreebank transformTrees(Treebank tb) {
    List<Pair<TregexPattern,TsurgeonPattern>> ops = new ArrayList<Pair<TregexPattern,TsurgeonPattern>>();
    String line = null;
    try {
      BufferedReader br = getBufferedReader();
      List<TsurgeonPattern> tsp = new ArrayList<TsurgeonPattern>();
      while ((line = br.readLine()) != null) {
        if (DEBUG) System.err.print("Pattern is " + line);
        TregexPattern matchPattern = TregexPattern.compile(line);
        if (DEBUG) System.err.println(" [" + matchPattern + "]");
        tsp.clear();
        while (continuing(line = br.readLine())) {
          TsurgeonPattern p = Tsurgeon.parseOperation(line);
          if (DEBUG) System.err.println("Operation is " + line + " [" + p + "]");
          tsp.add(p);
        }
        if ( ! tsp.isEmpty()) {
          TsurgeonPattern tp = Tsurgeon.collectOperations(tsp);
          ops.add(new Pair<TregexPattern,TsurgeonPattern>(matchPattern, tp));
        }
      } // while not at end of file
    } catch (IOException ioe) {
      ioe.printStackTrace();
    } catch (ParseException pe) {
      throw new RuntimeException("EnglishPTBTreebankCorrector: tregex expression is ill-formed: " + line, pe);
    }
    MemoryTreebank mtb = new MemoryTreebank(tb.treeReaderFactory(),
                                            tb.encoding());
    for (Tree t : tb) {
      mtb.add(Tsurgeon.processPatternsOnTree(ops, t));
    }
    return mtb;
  }
  
  private static boolean continuing(String str) {
    return str != null && ! str.matches("\\s*");
  }

  private BufferedReader getBufferedReader() {
    return new BufferedReader(new StringReader(editStr));
  }

  private static final String editStr = 

    // Bung tree fixing
    
    // NOTE: if you add more of these, make sure to group the string additions
    // into chunks using parentheses, or the compiler will choke with a
    // StackOverflowError

    // Fix a bad parse in wsj_0415.mrg
    ("@VP=adj < (NP < (NP=ex < (NN < growth)) < CC=bad < (NP=bd < (VB < service)))\n" +
    "excise ex ex\n" +
    "delete bad\n" +
    "delete bd\n" +
    "adjoinF (VP VP@ (CC and) (VP (VB service) (NP (NN debt)))) adj\n" +
    "\n") +

    // sec 24
    ("@SBAR=home <1 /^-NONE-$/=emp <2 (@S < (@NP <1 (DT=bad < that|That) <-1 NNS))\n" +
    "delete emp\n" + 
    "relabel bad IN\n" +
    "move bad >1 home\n" +
    "\n") +

    // sec 22 bad parse!
    ("@NP < (@NP=gone < (NN < authority)) < (@PP=bad < (TO < to) < (NP=vp < (NN=newv < block) < (NNS=newnp < mergers)))\n" +
    "excise gone gone\n" +
    "adjoin (S (NP-SBJ (-NONE- *)) VP@) bad\n" +
    "relabel vp VP\n" +
    "relabel newv VB\n" +
    "adjoin (NP NN@) newnp\n" +
    "\n") +

    // POS tag fixing 

    // Ones specific to a phrasal category

    ("@NP < (/^``$/ < /^`$/) < (POS=bad < /^'$/)\n" +
    "relabel bad |''|\n" +
    "\n") +

    ("@NP < (IN=bad < /^(?:a|that|That)$/)\n" +
    "relabel bad DT\n" +
    "\n") +
    
    ("@NP < (IN=bad < /^(?:so|about)$/)\n" +
    "relabel bad RB\n" +
    "\n") +
    
    ("@NP < (IN=bad < /^(?:fiscal|next)$/)\n" +
    "relabel bad JJ\n" +
    "\n") +
    
    ("@NP < (RB=bad < /^(?:a|that|Some)$/)\n" +
    "relabel bad DT\n" +
    "\n") +
    
    ("@NP < (RB=bad < most $. DT)\n" +
    "relabel bad PDT\n" +
    "\n") +
    
    ("@NP < (RB=bad < /^(?:MORE)$/)\n" +
    "relabel bad JJR\n" +
    "\n") +
    
    ("@NP < (NN=bad < the)\n" + 
    "relabel bad DT\n" +
    "\n") +

    ("@NP < (CD=bad < the)\n" + 
    "relabel bad DT\n" +
    "\n") +

    ("@NP < (JJ=bad < the)\n" + 
    "relabel bad DT\n" +
    "\n") +

    ("@NP|NX < (NNP=bad < the)\n" + 
    "relabel bad DT\n" +
    "\n") +

    ("@NP=bad < (NNP=badder < Technically|Historically)\n" +
    "relabel bad ADVP\n" +
    "relabel badder RB\n" +
    "\n") +

    ("@NP < (RB=bad < /^(?:McNally)$/)\n" +
    "relabel bad NNP\n" +
    "\n") +
    
    ("@NP < (RB=bad < /^(?:vice|night|multifamily|hand|fist)$/)\n" +
    "relabel bad NN\n" +
    "\n") +
    
    ("@NP < (RP=bad < /^(?:whole)$/)\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@NP < (RP=bad < Howard) < (NN=badder < /^A\\.$/)\n" +
    "relabel bad NNP\n" +
    "relabel badder NNP\n" + 
    "\n") + 

    ("@NP < (JJ=bad < (First , (__ !> /^``$/ !> /^-LRB-$/ !> /^PRP\\$$/)) $. NNP)\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (JJ=bad < /^(?:U\\.S\\.|Sept\\.)$/)\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (JJ=bad <1 Sharp) !<2 __\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (JJ=bad < /^(?:mine)$/)\n" +
    "relabel bad NN\n" +                // noun usages (mining)
    "\n") +

    ("@NP <-1 (JJ=bad < firm)\n" +
    "relabel bad NN\n" +                // noun usages
    "\n") +

    ("@NP < (JJ=bad < /^(?:ours)$/)\n" +
    "relabel bad PRP\n" +
    "\n") +

    // make all uses of 'the House' (Congress) NNP, like most, and 
    // like all uses of 'the Senate'
    ("@NP <2 (NN=bad < House $- (DT < /^[Tt]he$/))\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (NNP=bad < Democrats|Republicans)\n" +
    "relabel bad NNPS\n" + 
    "\n") +
    
    ("@NP < (NNS=bad < Democrats|Republicans , __)\n" +
    "relabel bad NNPS\n" + 
    "\n") +
    
    ("@NP < (NN=bad < /^(?:Chapman|Ok|Oslo|Boeing|Jan\\.|Sept\\.|Oct\\.|Nov\\.|Dec\\.|Treasury)$/)\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (NN=bad < /^(?:members|bureaus|days|outfits|institutes|innings|write-offs|wines|trade-offs|tie-ins|thrips|1980s|1920s|receivables|earnings)$/)\n" +
    "relabel bad NNS\n" +
    "\n") +

    ("@NP < (NN=bad < /^(?:this)$/)\n" +
    "relabel bad DT\n" +
    "\n") +

    ("@NP < (/^:/=bad < /^(?:')$/)\n" +
    "relabel bad |''|\n" +
    "\n") + 

    ("@NP < (NNS=bad < /^(?:start-up|ground-handling|word-processing|T-shirt|co-pilot|sell-off)$/)\n" +
    "relabel bad NN\n" +
    "\n") +

    // not clear why Sens isn't NNPS
    ("@NP < (NNS=bad < /^(?:Sens\\.|Aichi|Asahi|Cincinnati|Hawaii|Pepsi)$/)\n" +
    "relabel bad NNP\n" +
    "\n") +

    // VBZ under @NP.  Strong rule ordering!

    // move up misplaced possessives
    ("@NP <1 (@NP=dest !< @NP . (__=wrong < /^\u0027s$/))\n" +
    "move wrong >-1 dest\n" +
    "\n") +

    // fix bung syntax in wsj_0295 and wsj_1142
    ("@S < (@NP=bad < PRP < (VBZ=bottom < /^'s$/)) < (@VP=adj < VBN|VBG)\n" + 
    "adjoin (VP (VBZ 's) VP@) adj\n" +
    "delete bottom\n" +
    "\n") +

    ("@S < (NP-SBJ < (PRP < I)) < (VP < (VB=bad < think) < SBAR)\n" +
    "relabel bad VBP\n" +
    "\n") +
    

    // and an extra weird NP in wsj_0446 and wsj_1101
    ("@VP < (@NP=bad < (VBZ < kills|blames) < @NP)\n" +
    "excise bad bad\n" +
    "\n") + 

    // then turn all 's VBZ ino a POS
    // except for Everything's a Dollar Inc. !
    ("@NP < (VBZ=bad < /^'s$/) !< (NNP < Everything)\n" +
    "relabel bad POS\n" +
    "\n") +

    // and then turn all other VBZ that aren't the 's into NNS. 
    // 100% precision now!
    // CHECK IF CAN DELETE kills HERE NOW!
    ("@NP|NX < (VBZ=bad !< /^(?:'s|kills)/)\n" +
    "relabel bad NNS\n" +
    "\n") +

    // but turn POS under NP to PRP if it is the 's of let's
    ("@NP < (POS=bad < /^'s$/) > (@S > (@VP < (VB < let)))\n" +
    "relabel bad PRP\n" +
    "\n") +

    // VBP under NP.  First fix one that should be a verb!  NP is wrong

    ("@NP=bad < (VBP < are) > (@VP > (@S < NP-SBJ))\n" +
    "excise bad bad\n" +
    "\n") +

    ("@NP < (VBP=bad < charge)\n" +
    "relabel bad NN\n" +
    "\n") +

    ("@NP < (VBP=bad < the)\n" +
    "relabel bad DT\n" +
    "\n") +

    ("@NP < (VBP=bad < we)\n" +
    "relabel bad PRP\n" +
    "\n") +

    ("@NP < (VBP=bad < /^[A-Z]/)\n" +
    "relabel bad NNP\n" +
    "\n") +

    // VBN under NP 

    ("@NP < (VBN=bad < Applied !$ __)\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (VBG=bad < preferred)\n" +
    "relabel bad VBN\n" +
    "\n") + 

    ("@NP < (VB=bad < The)\n" +
    "relabel bad DT\n" +
    "\n") + 

    ("@NP < (VB=bad < allowed)\n" +
    "relabel bad VBD\n" +
    "\n") + 

    ("@NP <-1 (JJR=bad < cleaner)\n" +
    "relabel bad NN\n" +
    "\n") + 

    ("@NP < (VB=bad < /^(?:Nov\\.|Jan\\.|Dec\\.|Tandy|Release|Orkem|McDonald|Citicorp|Anne)$/)\n" +
    "relabel bad NNP\n" +
    "\n") + 

    ("@NP < (VB=bad < /^(?:short|key|many|last|further)$/)\n" +
    "relabel bad JJ\n" +
    "\n") + 

    ("@NP < (VB=bad < lower)\n" +
    "relabel bad JJR\n" +
    "\n") + 

    ("@NP < (VB=bad < /^(?:spill|watch|review|risk|realestate|love|experience|control|Transport|mind|term|program|gender|audit|blame|stock|run|group|affect|rent|show|accord|change|finish|work|schedule|influence|school|freight|growth|travel|call|autograph|demand|abuse|return|defeat|pressure|bank|notice|tax|ooze|network|concern|pit|contract|cash|help|lunch|combat|pot|care|date|Streetspeak|face|effect|worry)$/)\n" +
    "relabel bad NN\n" +
    "\n") + 

    ("@NP <1 (NNP=bad < Officials|Cartoonists|Prices)\n" +
    "relabel bad NNS\n" +
    "\n") + 

    ("@NP=badder < (NNP=bad < Currently)\n" +
    "relabel bad RB\n" +
    "relabel badder ADVP-TMP\n" +
    "\n") + 

    ("@NP < (PRP=bad < US & $. __)\n" +
    "relabel bad NNP\n" +
    "\n") + 

    ("@NP < (PRP=bad < her & $. __)\n" +
    "relabel bad |PRP$|\n" +
    "\n") + 

    ("@NP <1 (PRP=bad < his) !<2 __\n" +
    "relabel bad |PRP$|\n" +
    "\n") + 

    ("@NP < (/^VB/=bad < won)\n" +
    "relabel bad NN\n" +
    "\n") +

    ("VBD=bad [ > @NP | > (@ADJP < CC|CONJP > @NP) ]\n" +
    "relabel bad VBN\n" +
    "\n") +

    ("@NP < (NN=bad < Time) < (NNP < Warner)\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("@NP < (MD=bad < Can|May)\n" +
    "relabel bad NNP\n" +
    "\n") +

    // there are a number of cases of 'the the' -- errors in sources or
    // mistakes in Perl scripts?  
    // and one case of a/DT half/DT
    ("@NP <1 (DT=bad !< the $. (DT !< half))\n" +
    "relabel bad PDT\n" +
    "\n") +

    // filling out NP with PP modifier.  This rule should precede
    ("@NP=place < (@NP <1 DT !<2 __ $. (JJ=bad $. (NN=badder $. PP)))\n" +
    "move bad >-1 place\n" +
    "move badder >-1 place\n" +
    "\n") +

    // filling out NP with PP modifier.  This rule should follow
    ("@NP=place < (@NP <1 DT|JJ !<2 __ $. (NN=bad $. PP))\n" +
    "move bad >-1 place\n" +
    "\n") +

    // NEWSPAPERS
    ("@NP < (NNPS=bad < NEWSPAPERS ! $ /^NN/)\n" +
    "relabel bad NNS\n" +
    "\n") +

    ("@NP < (@NP < (NNPS=bad < CERTIFICATES)) < (PP < (IN < OF) < (NP < (__ < DEPOSIT)))\n" +
    "relabel bad NNS\n" +
    "\n") +

    ("@NP < (@NP < (__ < CERTIFICATES)) < (PP < (IN < OF) < (NP < (NNPS=bad < DEPOSIT)))\n" +
    "relabel bad NN\n" +
    "\n") +

    ("@NP < (NNP=bad < DISCOUNT) < (NNP=badder < RATE)\n" +
    "relabel bad NN\n" +
    "relabel bad NN\n" +
    "\n") +

    ("@NP < (JJ=bad < DISCOUNT) < (NN < RATE)\n" +
    "relabel bad NN\n" +
    "relabel bad NN\n" +
    "\n") +

    ("@NP < (__ < chief $. (NN=bad < executive $. (NN < officer)))\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@NP < (NN=bad < chief $. (__ < executive $. (NN < officer)))\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@NP <: (NNP=bad < /^'s$/)\n" + 
    "relabel bad PRP\n" +
    "\n") +

    "NP-TMP=bad < (NN < Leisure)\n" +
    "relabel bad NP\n" +
    "\n"+

    // chief executive officer
    ("@NP < (NN < officer $- (NN=bad < executive $- (__ < chief)))\n" + 
    "relabel bad JJ\n" +
    "\n") +

    // chief executive officer
    ("@NP < (NN < officer $- (JJ < executive $- (NN=bad < chief)))\n" + 
    "relabel bad JJ\n" +
    "\n") +

    // the infamous "Ad Notes"
    ("@NP < (NNP=bad < Ad) < (/^NN/ < Notes)\n" +
    "relabel bad NN\n" +
    "\n") +

    // the infamous "Ad Notes"
    ("@NP < (/^NN/ < Ad) < (NNPS=bad < Notes)\n" +
    "relabel bad NNS\n" +
    "\n") +

    // the infamous "Ad Notes"
    ("NP=bad < (/^NN/ < Ad) < (/^NN/ < Notes)\n" +
    "relabel bad NP-HLN\n" +
    "\n") +
    
    // equally infamous "Who's News:"
    ("@SBAR=bad < (@WHNP < (WP < WHO)) < (S=badder < (VP < (@NP < (NN < NEWS))))\n" +
    "relabel bad SBARQ-HLN\n" + 
    "relabel badder SQ\n" + 
    "\n") +

    // equally infamous "Who's News:"
    ("@SBARQ < (@WHNP < (WP < WHO)) < (SQ < (VP < (@NP < (NNP=bad < NEWS))))\n" +
    "relabel bad NN\n" + 
    "\n") +

    // equally infamous "Who's News:"
    ("@SBARQ < (@WHNP < (WP < WHO)) < (S=bad < (VP < (@NP < (NN < NEWS))))\n" +
    "relabel bad SQ\n" + 
    "\n") +

    ("@WHNP < @WHNP=bad < @WHPP\n" +
    "relabel bad NP\n" +
    "\n") +

    // How much always has 'much' as a JJ
    ("/^WH/ < (WRB < /^(?i:how)$/) < (__=bad < (much !> JJ))\n" +
    "relabel bad JJ\n" +
    "\n") +

    // the VP is for extraposed relatives....
    ("@WHNP|WHADVP < (VBP|DT|IN=bad < /^(?i:that)$/) [ > (@SBAR > @NP|VP) | > (@SBAR > (@SBAR < /^(?:CC|CONJP|,)$/ > @NP|VP)) ]\n" +
    "relabel bad WDT\n" +
    "\n") + 


    ("@UCP < (RB=bad < multifamily)\n" +
    "relabel bad NN\n" +
    "\n") + 


    ("@PRT < (RBR=bad < in)\n" +
    "relabel bad RP\n" +
    "\n") + 

    ("@PRT < (NNP=bad < up)\n" +
    "relabel bad RP\n" +
    "\n") + 


    // PP parent

    ("@PP < (RP=bad < through) < @NP\n" +
    "relabel bad IN\n" +
    "\n") + 

    ("@PP < (RP|NN=bad < in) < @NP\n" +
    "relabel bad IN\n" +
    "\n") + 

    ("@PP < (RB=bad < for|For|after|After|past|Past|under|Under)\n" +
    "relabel bad IN\n" +
    "\n") + 

    ("@PP < (JJ=bad < if)\n" +
    "relabel bad IN\n" +
    "\n") + 


    // VP parent

    ("@VP=bad < (IN < past) < @NP\n" +
    "relabel bad PP\n" +
    "\n") + 

    ("@VP < (RB=bad < back $. (PRT < (RP < down))) > (@SINV|SQ|VP < MD)\n" +
    "relabel bad VB\n" +
    "\n") +

    ("@VP < (IN=bad < complicated) > @S\n" + 
    "relabel bad VBD\n" +
    "\n") + 

    ("@VP < (IN=bad < near) > @VP\n" + 
    "relabel bad VB\n" +
    "\n") + 

    ("@VP < (IN=bad < like|post) > (@SQ|VP < /^(?:VB|MD)/)\n" + 
    "relabel bad VB\n" +
    "\n") + 

    ("@VP < (IN=bad < like|post) [ > @S | > (@VP < CC|CONJP > @S) ]\n" + 
    "relabel bad VBP\n" +
    "\n") + 

    ("@VP < (/^VBD?$/=ins < take|sold) < (IN=bad < off)\n" +
    "delete bad\n" +
    "insert (PRT (RP off)) $- ins\n" +
    "\n") + 

    // NNS under VP.  Easy.

    ("@VP < NNS=bad\n" +
    "relabel bad VBZ\n" +
    "\n") + 

    // ordered rules for NN under VP

    // first fix the ones ending in -ing
    ("@VP < (NN=bad < /.{2}ing$/)\n" + 
    "relabel bad VBG\n" + 
    "\n") + 

    ("@VP < (NN=bad [ < set|beat|bid|redone|reset|hurt|underwritten|overrun | < /.{2}[^e]ed$/ ]) [ > (@VP < (/^VB/ < " + BE_HAVE_GET + ")) | > (@NP < @NP)]\n" + 
    "relabel bad VBN\n" + 
    "\n") + 

    ("@VP < (NN=bad < agreed|set|rebounded|fell) [ > @S | > (@VP < @CC|CONJP > @S) ]\n" + 
    "relabel bad VBD\n" + 
    "\n") + 

    // contexts where an untensed verb should occur
    // an imperative is VB!  The 'say' cases are direct speech imperatives
    ("@VP !< /^VB/ < (NN|NNP|JJ=bad !< /...(?i:ing)$/) [ > (@VP|SINV|SQ " + MODAL_DO_TO + ") | > (@VP|SINV|UCP|SQ < CC|CONJP > (@VP|SINV|UCP|SQ " + MODAL_DO_TO + ")) | > (@S > (@VP " + BARE_VP_VERB + ")) | > (@VP " + BARE_VP_VERB + ") | > (@S < (/^NP-SBJ/ < /^-NONE-$/) > (@VP " + SAY_VERB + ")) ]\n" + 
    "relabel bad VB\n" + 
    "\n") + 

    // file should maybe be VB - subjunctive (look at higher verb? wsj_0979)
    ("@VP !< /^VB/ < (NN=bad [ [ !< /s$/ & !< /e[dn]$/ & !< /ing$/ ] | < stress ] ) [ > @S | > (@VP < @CONJP|CC > @S) ]\n" + 
    "relabel bad VBP\n" + 
    "\n") + 

    // not just anything ending in s.  Definitely not if -ss, but also 'focus'
    ("@VP < (NN=bad < institutes) > @S\n" + 
    "relabel bad VBZ\n" + 
    "\n") + 

    ("@VP < (VBP=bad !< /...(?i:ing)$/) [ > (@VP|SQ|SINV " + MODAL_DO_TO + ") | > (@VP|UCP|SQ|SINV < CC|CONJP > (@VP|UCP|SQ|SINV " + MODAL_DO_TO + ")) | > (@S > (@VP " + BARE_VP_VERB + ")) | > (@VP " + BARE_VP_VERB + ") | > (@S < (/^NP-SBJ/ < /^-NONE-$/) > (@VP " + SAY_VERB + ")) ]\n" + 
    "relabel bad VB\n" +
    "\n") +

    // Don't allow SINV for VBN as get participial clauses
    // unless the SINV has a modal under it
    // Allow VBN in complement of SEE or HEAR (but not LET or MAKE)
    ("@VP < (VBN=bad !< /...(?i:ing)$/ !< /...(?i:ed)$/) [ > (@VP|SQ " + MODAL_DO_TO + ") | > (@VP|SQ < CC|CONJP !< /^VB/ > (@VP|SQ " + MODAL_DO_TO + ")) | > (@SINV " + MODAL + ") | > (@SINV < CC|CONJP !< /^VB/ > (@SINV " + MODAL + ")) ]\n" + 
    "relabel bad VB\n" +
    "\n") +

    // for transitive, it can't be VBN under see/hear
    ("@VP < (VBN=bad !< /...(?i:ing)$/) < (NP !< /^-NONE-$/) [ > (@S > (@VP " + BARE_VP_VERB + ")) | > (@VP " + BARE_VP_VERB + ") | > (@S < (/^NP-SBJ/ < /^-NONE-$/) > (@VP " + SAY_VERB + ")) ]\n" + 
    "relabel bad VB\n" +
    "\n") +

    ("@VP < (NN=bad < relocate) > (@VP < @CONJP > (@VP < MD))\n" + 
    "relabel bad VB\n" + 
    "\n") + 

    ("@VP < (NN=bad < might|will)\n" + 
    "relabel bad MD\n" + 
    "\n") + 

    // ordered rules for NNP under VP

    // it's VBD for SINV
    ("@VP < (NNP=bad < /...(?i:ed)$/) [ > (SINV !> /TTL/) | > (@VP < @CONJP|CC > (SINV !> /TTL/)) ]\n" + 
    "relabel bad VBD\n" + 
    "\n") + 

    // it's VBN not VBD because the one or two there are headline-speak
    ("@VP < (NNP=bad < /...(?i:ed)$/) [ > (S|S-HLN|S-ADV !> /TTL/) | > (@VP < @CONJP|CC > (S|S-HLN|S-ADV !> /TTL/)) | > (@VP < (__ < " + BE_HAVE_GET + ")) ]\n" + 
    "relabel bad VBN\n" + 
    "\n") + 

    // it's VBN not VBD because the one or two there are headline-speak
    ("@VP < (NNP=bad < Got|Gotten) [ > (S|S-HLN|S-ADV !> /TTL/) | > (@VP < @CONJP|CC > (S|S-HLN|S-ADV !> /TTL/)) | > (@VP < (__ < " + BE_HAVE_GET + ")) ]\n" + 
    "relabel bad VBN\n" + 
    "\n") + 

    // ordered rules for NNP under VP  (2 dots as want to get "Adds")
    ("@VP < (NNP=bad < /..[^Ss](?i:s)$/) [ > (S|S-HLN|SINV !> /TTL/) | > (@VP < @CONJP|CC > (S|SINV|S-HLN !> /TTL/)) ]\n" + 
    "relabel bad VBZ\n" + 
    "\n") + 

    // ordered rules for NNP under VP
    ("@VP !< /^VB/ < (NNP=bad < /(?i:ing)$/)\n" + 
    "relabel bad VBG\n" + 
    "\n") + 

    // ordered rules for NNP under VP
    ("@VP < NNP=bad [ > (S|S-HLN !> /TTL/) | > (@VP < @CONJP|CC > (S|S-HLN !> /TTL/)) ]\n" + 
    "relabel bad VBP\n" + 
    "\n") + 

    // this one is in a title....
    ("@VP < (NNP=bad < are) [ > @S | > (@VP < @CONJP|CC > @S) ]\n" + 
    "relabel bad VBP\n" + 
    "\n") + 

    /* ----------------------------
       tregex3 -f -w '@VP < NN' | & less

       // unresolved: some bid, ask cases
  
       // so far only done 1-1000 inclusive

            } else if (word.equals("reconfirm")) {
              cat = changeBaseCat(cat, "VB");
            } else if (word.equals("rebounded")) {
              cat = changeBaseCat(cat, "VBD");
            }
    // still need to do NNP > VP rules as well!
       --------------------- */

    /*
          } else if (baseCat.equals("NNP")) {
            if (word.equals("GRAB")) {
              cat = changeBaseCat(cat, "VBP");
            } else if (word.equals("mature")) {
              cat = changeBaseCat(cat, "VB");
            } else if (word.equals("Face")) {
              cat = changeBaseCat(cat, "VBP");
            } else if (word.equals("are")) {
              cat = changeBaseCat(cat, "VBP");
            } else if (word.equals("say")) {
              cat = changeBaseCat(cat, "VBP");
            } else if (word.equals("Added")) {
              cat = changeBaseCat(cat, "VBD");
            } else if (word.equals("Adds")) {
              cat = changeBaseCat(cat, "VBZ");
            } else if (word.equals("BRACED")) {
              cat = changeBaseCat(cat, "VBD");
            } else if (word.equals("REQUIRED")) {
              cat = changeBaseCat(cat, "VBN");
            } else if (word.equals("REVIEW")) {
              cat = changeBaseCat(cat, "VB");
            } else if (word.equals("code-named")) {
              cat = changeBaseCat(cat, "VBN");
            } else if (word.equals("Printed")) {
              cat = changeBaseCat(cat, "VBN");
            } else if (word.equals("Rated")) {
              cat = changeBaseCat(cat, "VBN");
            } else if (word.equals("FALTERS")) {
              cat = changeBaseCat(cat, "VBZ");
            } else if (word.equals("Got")) {
              cat = changeBaseCat(cat, "VBN");
            } else if (word.equals("Adds")) {
              cat = changeBaseCat(cat, "VBZ");
            }
    */

    // Modal fixes: of 'd tagged as VBD and others tagged VBP

    // first fix one where the complement verb tag is wrong
    ("@VP < (VBD < /^(?i:'d)$/) < (@VP < (VB=badder < seen))\n" +
    "relabel badder VBN\n" + 
    "\n") + 

    ("@VP < (VBP=bad < " + MODAL_WORD + ") < (@VP < VB)\n" +
    "relabel bad MD\n" + 
    "\n") + 

    ("@VP < (VBP=bad < /^(?i:'d)$/) < (@VP < VB)\n" +
    "relabel bad MD\n" + 
    "\n") + 


    // this must be ordered before the have/be auxiliary complement fix!
    ("@VP < POS=bad\n" +
    "relabel bad VBZ\n" +
    "\n") +

    ("@VP < (VBD=bad < heaves)\n" +
    "relabel bad VBZ\n" +
    "\n") +

    ("@VP < (VB=bad < /.{2}[^e]ed$/) > @S\n" +
    "relabel bad VBD\n" +
    "\n") +

    ("@VP < (VB=bad < /^(?i:.{2,}[^e]ed|reset|run|become|hit|remade|gone|rid|put|hurt|become)$/) [ > (@VP < (/^VB/ < " + BE_HAVE_GET + ")) | > (@NP < @NP) ]\n" + 
    "relabel bad VBN\n" + 
    "\n") + 

    ("@VP < (VBN=bad < has)\n" +
    "relabel bad VBZ\n" +
    "\n") +

    ("@VP < (VBN=bad < grew|fell|had) [ > @S | > (@VP < CONJP|CC > @S)]\n" +
    "relabel bad VBD\n" +
    "\n") +

    // Doing the VBN to VBD is too general because of auxiliary deletion in
    // contracted constructions, headlines, etc.
    // Nevertheless, there are quite a few cases....
    // Try ones with objects??
    // "@VP < VBN=bad [ > @S | > (@VP < CONJP|CC > @S) ]\n" + 
    // "relabel bad VBN\n" +
    // "\n" +
    // Below are three patterns that doesn't overgeneralize and still gets a
    // lot of the cases: note that they don't use @ so as to not match NP-TMP
    // or S-HLN, and checks for non-NULL constituents

    // must be finite in clause with overt NPsubj and NPobj
    ("@VP < VBN=bad < (NP !< /^-NONE-$/) [ > (S < (@NP !< /^-NONE-$/)) | > (@VP < CONJP|CC > (S < (@NP !< /^-NONE-$/))) ]\n" +
    "relabel bad VBD\n" +
    "\n") +

    // root main clauses with overt subj must be finite
    ("@VP < VBN=bad [ > (S < (@NP !< /^-NONE-$/) > (__ !> __)) | > (@VP < CONJP|CC > (S < (@NP !< /^-NONE-$/) > (__ !> __))) ]\n" +
    "relabel bad VBD\n" +
    "\n") +

    // that clauses (or that-less that clauses) with overt subjects are finite
    ("@SBAR [ < (/^-NONE-$/ < /^0$/) | < (IN < that) ] < (@S < (NP-SBJ !< /^-NONE-$/) < (@VP < VBN=bad))\n" +
    "relabel bad VBD\n" +
    "\n") +

    // similar but more limited corrections of VB to VBP
    // have to beware getting VB in imperatives, subjunctives, etc.
    // root main clauses with overt subj must be finite
    ("@VP < VB=bad [ > (S < (NP-SBJ !< /^-NONE-$/) > (__ !> __)) | > (@VP < CONJP|CC > (S < (NP-SBJ !< /^-NONE-$/) > (__ !> __))) ]\n" +
    "relabel bad VBP\n" +
    "\n") +

    // Should have a finite VBP not a VB in a finite relative clause
    ("@NP < @NP < (@SBAR < @WHNP < (@S < (VP < VB=bad)))\n" +
    "relabel bad VBP\n" +
    "\n") +

    ("@VP < CONJP|CC <1 (VBP $.. VB=bad)\n" +
    "relabel bad VBP\n" +
    "\n") +

    ("@VP < (VBP=bad < has)\n" + 
    "relabel bad VBZ\n" +
    "\n") +

    // JJ under VP rewrites (see also above for BARE_VP_VERB generalizations)

    ("@VP < (JJ=bad < own|elaborate) [ > @S | > (@VP < CONJP|CC > @S)]\n" +
    "relabel bad VBP\n" +
    "\n") +

    ("@VP < (JJ=bad < /..ing$/) < (@S < (@NP !< /^-NONE-$/) < (VP < TO))\n" +
    "relabel bad VBG\n" +
    "\n") +

    ("@VP < (JJ=bad < /...ed$/) [ > @S | > (@VP < CONJP|CC > @S)]\n" +
    "relabel bad VBD\n" +
    "\n") +

    ("@VP < (JJ=bad < pressured|known) [ > (@VP < (__ < " + BE + ")) | > (@VP < CONJP|CC > (@VP < (__ < " + BE + "))) ]\n" +
    "relabel bad VBN\n" +
    "\n") +

    ("@VP < (JJ=bad < /..ed$/) > (@VP < (__ < " + HAVE + "))\n" + 
    "relabel bad VBN\n" +
    "\n") +

    ("@VP < (JJ=bad < /.{2}ing$/) [ > @S | > (@VP < CONJP|CC > @S)]\n" +
    "relabel bad VBG\n" +
    "\n") +

    ("@VP < (JJ=bad < to)\n" +
    "relabel bad TO\n" +
    "\n") +

    ("@VP|S < (JJ=bad < all|ALL|All)\n" +
    "relabel bad RB\n" +
    "\n") +

    // VBN after have/be auxiliaries
    ("@VP < VBD=bad [ > (@VP < (/^VB/ < " + BE_HAVE_GET + ")) | > (@VP < CONJP|CC > (@VP < (/^VB/ < " + BE_HAVE_GET + "))) | > (@NP < @NP) ]\n" + 
    "relabel bad VBN\n" +
    "\n") +

    // Asked, Warned, etc. at start of sentence
    ("@VP < (VBD=bad < /[A-Z]/) > S-ADV\n" +
    "relabel bad VBN\n" +
    "\n") +

    ("@VP < (/^VB/ $. (IN=bad < up|off))\n" +
    "adjoin (PRT (RP@)) bad\n" +
    "\n") +

    // remove the two (!) unsure phrasal categories: ADVP|PRT, PRT|ADVP
    ("@VP < /^VB/ < (/^(?:ADVP|PRT)\\|(?:ADVP|PRT)$/=bad < (RB|NN=badder < back))\n" +
    "relabel bad PRT\n" +
    "relabel badder RP\n" +
    "\n") +

    // make other instances of 'win back' particle verbs
    ("@VP < (/^VB/ < win|wins|winning|won) < (@ADVP=bad < (RB=badder < back))\n" +
    "relabel bad PRT\n" +
    "relabel badder RP\n" +
    "\n") +

    // TODO: do other instances of put back.  Worth looking at Treebank II -PUT

    ("@VP < (PDT=bad < all)\n" +
    "relabel bad RB\n" +
    "\n") +

    ("@VP < (PRT < (VBP=bad < down))\n" +
    "relabel bad RP\n" +
    "\n") +

    ("@VP < (PRT=bad < (RBS < best))\n" +
    "relabel bad ADVP\n" +
    "\n") +

    ("@VP <1 (VB=bad < plea) <2 (NN=badder < bargain) > (@VP" + MODAL_DO_TO + ")\n" +
    "relabel bad NN\n" +
    "relabel badder VB\n" +
    "\n") +

    ("@ADJP < UH=bad\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@ADJP < (JJ=bad < more)\n" +
    "relabel bad JJR\n" +
    "\n") +

    ("@ADJP < (NN=bad < firm|due|permissible)\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@ADJP < (NNS=bad < due)\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@ADJP < (NNP=bad < READY)\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@ADJP < (RB=bad < free|clear|tight|sure|particular|due)\n" +
    "relabel bad JJ\n" +
    "\n") +
    // consider also "hard" -- many but not all cases should be JJ

    ("@ADJP < (RB=bad < likely) > @VP\n" +
    "relabel bad JJ\n" +
    "\n") +
    // reconsider also cases not under VP, but some looked more complex....

    ("@ADJP < (VB=bad < /^(?i:stock|No\\.)$/)\n" +
    "relabel bad NN\n" +
    "\n") +

    ("@ADJP < (VBP=bad < fit|close)\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("@ADJP < (VB=bad < secure|keen|quiet)\n" +
    "relabel bad JJ\n" +
    "\n") +


    ("@QP < (IN|JJ|RBR|RP=bad < about)\n" +
    "relabel bad RB\n" +
    "\n") +

    ("@QP < (JJ=bad < as)\n" +
    "relabel bad RB\n" +
    "\n") +
    // look at 'as' examples, but don't want to maul 'as much as X' idiom

    ("@QP < (JJ|JJS=bad < more|less)\n" +
    "relabel bad JJR\n" +
    "\n") +
    // need to work out whether to relabel all the RBR ones similarly....

    ("@QP < (RP=bad < up $. (TO <to))\n" +
    "relabel bad IN\n" +
    "\n") +
    // there's some IN/RB variability for this up ... what's correct/why?

    ("@ADVP < EX=bad\n" +
    "relabel bad RB\n" +
    "\n") +

    ("@ADVP < (NN=bad < that)\n" +
    "relabel bad DT\n" +
    "\n") +

    ("@ADVP < (NNP=bad [ < /.{2}ly$/ | < Overall | < Systemwide ])\n" +
    "relabel bad RB\n" +
    "\n") +

    ("@ADVP < (RP=bad < around|before)\n" +
    "relabel bad RB\n" +
    "\n") +

    ("@ADVP=bad <1 PRT !<2 __\n" +
    "excise bad bad\n" +
    "\n") +

    // special for let
    ("@ADVP < (VBD=bad < let) < (RB=badder < alone)\n" +
    "relabel bad VB\n" +
    "relabel badder JJ\n" +
    "\n") +

    // in general must be non finite
    ("@ADVP < VBD=bad\n" +
    "relabel bad VBN\n" +
    "\n") +


    ("@SBAR < (DT|WDT|NN|NNP|RB=bad < that|because|while|Though|Whether)\n" +
    "relabel bad IN\n" +
    "\n") +

    ("@SQ < VB=bad\n" +
    "relabel bad VBP\n" + 
    "\n") +

    ("@SQ < (NNS=bad $. NP-SBJ)\n" +
    "relabel bad VBZ\n" + 
    "\n") +

    ("@SQ < (NNP=bad < Does)\n" +
    "relabel bad VBZ\n" + 
    "\n") +

    ("@SQ < (NNP=bad < Should)\n" +
    "relabel bad MD\n" + 
    "\n") +

    ("@X < (JJS=bad < more|less)\n" +
    "relabel bad JJR\n" +
    "\n") +


    ("@INTJ < (NNP=bad < UH|HUH)\n" +
    "relabel bad UH\n" +
    "\n") +

    
    // non-phrasally rooted POS tag corrections

    ("JJ=bad < /^%$/\n" +
    "relabel bad NN\n" +
    "\n") +

    ("NN|NNP|JJ|IN=bad < and\n" +
    "relabel bad CC\n" +
    "\n") +

    ("VB=bad < even\n" +
    "relabel bad RB\n" +
    "\n") +

    // bad comma tags section

    ("/^,$/=bad < /^2$/\n" +
    "relabel bad CD\n" +
    "\n") +

    ("/^,$/=bad < an\n" +
    "relabel bad DT\n" +
    "\n") +

    ("/^,$/=bad < Wa\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("/^,$/=bad < section\n" +
    "relabel bad NN\n" +
    "\n") +

    ("/^,$/=bad < underwriters\n" +
    "relabel bad NNS\n" +
    "\n") +


    ("CD=bad < high-risk\n" +
    "relabel bad JJ\n" +
    "\n") +

    ("RB|RP|NN=bad < for|at\n" +
    "relabel bad IN\n" +
    "\n") +

    ("NN=bad [ < /^.\\.$/ | < Lorillard ]\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("JJS=bad < StatesWest\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("JJR=bad < Richter|Gartner\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("IN|JJ|NN|NNP=bad < /^[Aa][Nn][Dd]$/\n" +
    "relabel bad CC\n" +
    "\n") +

    // This is for 'ago' as adverb
    // TODO: reconsider this -- I think Pullum argues for IN
    // "IN=bad < ago\n" +
    // "relabel bad RB\n" +
    // "\n" +

    // The next several are for 'ago' as postposition IN 
    // NP is marked -ADV at least once... (strip it?)
    ("/^(?:ADVP|ADVP-TMP.*)$/=badder < (RB=bad < ago) < @NP\n" +
    "relabel bad IN\n" +
    "relabel badder PP-TMP\n" +
    "\n") +

    ("ADV-TMP=bad < (ADVP|NP=badder < (IN < ago) < @NP)\n" +
    "relabel bad PP-TMP\n" +
    "relabel badder PP\n" +
    "\n") +

    // TODO: for cases embedded in ADV-TMP: "a year ago, when X was snamed pres":
    // higher one PP-TMP.
    ("/^(?:ADVP|NP)/=badder < (IN < ago) < @NP\n" +
    "relabel badder PP-TMP\n" +
    "\n") +

    ("@NP=badder < (RB=bad < ago) < @NP\n" +
    "relabel bad IN\n" +
    "relabel badder PP-TMP\n" +
    "\n") +

    ("/^ADVP-TMP/=badder < (RB < not|Not|so|So) < (RB=jj < long) < (RB=bad < ago)\n" +
    "adjoin (PP-TMP NP@ (IN ago)) badder\n" +
    "delete bad\n" +
    "relabel jj JJ\n" +
    "\n") +

    ("/^ADVP-TMP/=badder <1 (RB|JJ=jj < long) <2 (RB=bad < ago)\n" +
    "adjoin (PP-TMP NP@ (IN ago)) badder\n" +
    "delete bad\n" +
    "relabel jj JJ\n" +
    "\n") +

    ("ADJP=badder <1 (NN=baddest < Year) <2 (RB=bad < ago)\n" +
    "adjoin (NP NN@) baddest\n" +
    "relabel badder PP-TMP\n" +
    "relabel bad IN\n" +
    "\n") +

    ("@NP=bad <1 NP <2 (ADVP-TMP=badder < (RB=baddest < ago))\n" +
    "relabel bad PP-TMP\n" +
    "excise badder badder\n" +
    "relabel baddest IN\n" +
    "\n") +

    ("/^NP/=badder < DT|CD < NN|NNS < (RB|IN=bad < ago)\n" +
    "adjoin (PP-TMP NP@ (IN ago)) badder\n" +
    "delete bad\n" +
    "\n") +

    // TODO: Provisional rule for this tree; should be revised!
    //    (ADVP-TMP
    //     (ADVP
    //      (ADVP (RB As) (RB long))
    //      (PP (IN as)
    //       (NP (DT a) (NN decade))))
    //     (IN ago))
    ("ADVP-TMP=badder < (IN < ago) < @ADVP\n" +
    "relabel badder PP-TMP\n" +
    "\n") +
    
    // TODO: this suggests a posposing in the above sentence! Do as movement?
    //    (ADVP-TMP
    //     (ADVP (RB as) (JJ long) (RB ago))
    //     (PP (IN as)
    //      (NP (JJ early) (CD 1988))))


    // "RB=bad < ago\n" +
    // "relabel bad IN\n" +
    // "\n" +



    /*
      (ADVP-TMP
        (ADVP
          (NP (CD 25) (NNS years))
          (RB ago))
        (, ,)
        (SBAR
          (WHADVP-1 (WRB when))

            (NP-TMP
              (ADVP
                (NP (DT a) (NN year))
                (RB ago))
              (, ,)
              (SBAR
                (WHADVP-1 (WRB when))

        (ADVP
          (NP (DT a) (NN year))
          (RB ago))))

                    (ADVP-TMP
                      (ADVP
                        (NP (DT a) (NN year))
                        (RB ago))
                      (, ,)
                      (SBAR
                        (WHADVP-1 (WRB when))
                        (S

                (PP-DIR (IN from)
                  (ADVP
                    (NP (DT a) (NN year))
                    (RB ago)))))))))

            (NP (DT the) (NN use))
            (PP (IN of)
              (NP (NNS incentives)))
            (UCP-TMP
              (NP (DT this) (NN year))
              (CC and)
              (RB not)
              (ADVP
                (NP (DT a) (NN year))
                (RB ago)))))

              (ADVP-TMP (RB so) (RB long) (RB ago))))

          (PP-DIR (IN from)
            (NP-TMP
              (NP (DT a) (NN year))
              (IN ago))))))

              (NP-TMP (DT a) (NN year) (IN ago)))))))

                  (PP-TMP (IN from)
                    (ADVP
                      (NP (DT a) (NN year))
                      (IN ago)))

          (ADVP-TMP
            (ADVP
              (NP (CD five) (NNS years))
              (IN ago))
            (, ,)
            (SBAR
              (WHPP-2 (IN at)
                (WHNP (WDT which) (NN time)))

        (ADVP
          (NP
            (QP (JJR more) (IN than) (CD 30))
            (NNS years))
          (IN ago))
        (, ,)
        (SBAR
          (WHADVP (WRB when))

    (NP-SBJ-1
      (ADJP (NN Year) (RB ago))
      (NN figure))

                (PP-TMP (IN until)
                  (ADVP
                    (NP (CD three) (NNS years))
                    (RB ago))))))))

        (PP-TMP (IN from)
          (ADVP
            (NP (CD six) (NNS years))
            (RB ago)))))

          (PP (IN of)
            (NP
              (NP (RB about) (CD two) (NNS decades))
              (ADVP-TMP (RB ago)))))))

              (PP-DIR (IN from)
                (ADVP
                  (NP (DT a) (NN year))
                  (RB ago))))))))

        (NP-TMP
          (NP (DT a) (NN year))
          (RB ago))))

                (PP-TMP (IN from)
                  (ADVP
                    (NP (DT a) (NN year))
                    (IN ago)))))))))

            (NP
              (ADVP
                (NP (CD six) (NNS months))
                (IN ago))
              (, ,)
              (SBAR
                (WHADVP-1 (WRB when))

      (PP-TMP (IN from)
        (ADVP
          (NP (DT a) (NN year))
          (IN ago)))

                      (NP-TMP
                        (ADVP
                          (NP (DT a) (NN year))
                          (IN ago))
                        (, ,)
                        (SBAR
                          (WHADVP-2 (WRB when))

                      (PP-TMP (IN until)
                        (NP (DT a) (JJ few) (NNS years) (IN ago)))

    (ADVP-TMP (RB long) (RB ago))
    */






    ("RB=bad < newsweekly\n" +
    "relabel bad NN\n" +
    "\n") +

    ("NN=bad < PaineWebber\n" +
    "relabel bad NNP\n" +
    "\n") +

    ("NNP=bad < Though\n" +
    "relabel bad IN\n" +
    "\n") +

    // phrase structure stuff / phrasal categories

    // at best/least
    ("@PP < (IN < /^(?i:at)$/ $. RBS=adj)\n" +
    "adjoin (NP RBS@) adj\n" +
    "\n") +

    // Wh-phrases

    // treee in wsj_1447. 
    ("@SBAR < (/^WP\\$$/=bad $. (@WHNP=dest < NN))\n" +
    "move bad >1 dest\n" +
    "\n") + 

    // tree in wsj_2155
    ("@SBAR=sbar < (/^WP\\$$/=wrong $. (S=ins < NP-SBJ=fix < VP))\n" +
    "move wrong >1 fix\n" +
    "relabel fix WHNP\n" +
    "move fix >1 sbar\n" +
    "insert (NP-SBJ (-NONE- *T*)) >1 ins\n" +
    "\n") +

    // tree in wsj_1457
    ("@SBAR <1 @WHNP <2 @S=loc <3 @VP=bad !<4 __\n" +
    "move bad >-1 loc\n" +
    "\n") +

    ("@WHNP < (@WHADVP=bad < (WRB < /^(?i:how)$/) < (JJ < many|much)) < NN|NNS|NNP|NNPS\n" +
    "relabel bad WHADJP\n" +
    "\n") + 

    ("@WHNP < (@NP=bad < /^(?:WP\\$|WDT|WRB)$/ $.. @PP|PRN|NP)\n" +
    "relabel bad WHNP\n" +
    "\n") + 

    ("@WHNP < (@WHADVP=bad < (WRB < /^(?i:how)$/)) < (JJ < much)\n" +
    "excise bad bad\n" +
    "\n") + 

    ("@WHNP < @NP < (@PP=bad < (@WHNP < WDT|WP))\n" +
    "relabel bad WHPP\n" +
    "\n") + 

    ("@WHNP < @NP < (@PP=bad < (@NP=badder < WDT))\n" +
    "relabel bad WHPP\n" +
    "relabel badder WHNP\n" +
    "\n") + 

    
    // ordered PP with noun directly in it - fix wrong size NP
    ("@PP <1 (IN|TO $. (@NP=place < ADJP)) <-1 NN|NNS|NNP|NNPS=word\n" +
    "move word >-1 place\n" +
    "\n") +

    // ordered PP with noun directly in it - fix missing NP
    ("@PP=head <1 IN|TO=prep <-1 NN|NNS|NNP|NNPS\n" +
    "adjoinH (PP NP@) head\n" + 
    "move prep >1 head\n" +
    "\n") +

    ("@S < (@SBAR < (SBAR < SINV $. (CC $. (SBAR=adj < VBD < S))))\n" +
    "adjoin (SBAR SINV@) adj\n" +
    "\n") +

    ("@SINV < (NP-SBJ=subj $. (@VP=base $.. (@VP=say < (/^VB/ < say|says|said))))\n" +
    "adjoinH (S VP@) base\n" +
    "move subj >1 base\n" +
    "relabel base S-1\n" +
    "insert (S (-NONE- *T*-1)) >-1 say\n" +
    "\n") +

    ("NP-SBJ=bad < NP-TMP !< NP < PP-TMP\n" +
    "excise bad bad\n" +
    "\n") + 

    ("@S < NP-SBJ < (NP=bad < (/^NN/ < Tuesday|yesterday|Yesterday))\n" +
    "relabel bad NP-TMP\n" +
    "\n") +

    ("@S=bad < (VBP $. (NP-SBJ $. VP))\n" +
    "relabel bad SINV\n" +
    "\n") +

    // these two rules show why an "insert above" operation would be nice....
    ("@S < (/^NP-SBJ/ $. (VBP|VBZ=aux $. @VP=adj))\n" +
    "adjoinH (VP VP@) adj\n" +
    "move aux >1 adj\n" +
    "\n") +

    ("@S < (/^NP-SBJ/ $. (VBP|VBZ=aux $. @ADJP=adj))\n" +
    "adjoinH (VP ADJP@) adj\n" +
    "move aux >1 adj\n" +
    "\n") +

    ("@PP=adj < (IN=prep $. JJ)\n" +
    "adjoinH (PP NP@) adj\n" +
    "move prep >1 adj\n" +
    "\n") +

    ("@SBARQ <1 @WHADVP <2 VBZ=bad <3 RB=badder <4 @SQ=loc\n" +
    "move badder >1 loc\n" +
    "move bad >1 loc\n" + 
    "\n") + 

    ("@SBARQ <2 @WHADVP <3 MD=bad <4 @SQ=loc\n" +
    "move bad >1 loc\n" + 
    "\n") + 

    ("@SBARQ <2 @WHNP <3 VBZ=bad <4 @SQ=loc\n" +
    "move bad >1 loc\n" + 
    "\n") + 

    // tree in wsj_0755
    ("@VP=adj < (VBN=bad < been) < (JJ < unable) !< CC|CONJP\n" +
    "adjoin (VP (VBN been) ADJP@) adj\n" +
    "delete bad\n" +
    "\n") +

    // fix a bad tree in wsj_1623
    ("@S < (@NP < (NNS=bad < runs)) < (VP=home !< /^VB/ < (IN=badder < up))\n" +
    "relabel bad VBZ\n" +
    "relabel badder RP\n" +
    "move bad >1 home\n" +
    "adjoin (PRT RP@) badder\n" +
    "\n") +

    ("@VP=top < (MD=bottom < will $. /^VB/)\n" +
    "adjoin (VP (MD will) VP@) top\n" +
    "delete bottom\n" +
    "\n") +
    
    ("@VP < (MD=bad < /^'d$/)\n" +
    "relabel bad VBD\n" +
    "\n") +

    ("@S < (TO=bottom < to $. (@VP=top < /^VB/))\n" +
    "adjoin (VP (TO to) VP@) top\n" +
    "delete bottom\n" +
    "\n") +
    
    ("@S < /^NP-SBJ/ < (VP < (VBD < " + DO + ") < (@NP=bad < (NN=badder < work)))\n" +
    "relabel bad VP\n" +
    "relabel badder VB\n" +
    "\n") +

    ("@SBARQ !< @SQ !< /^-NONE-$/ !< @SBARQ < (@S|SINV=bad < VBP|VBZ|MD|VBD)\n" +
    "relabel bad SQ\n" +
    "\n") +

    ("@SBARQ < (@SINV=bad < (__ < would))\n" +
    "relabel bad SQ\n" +
    "\n") +

    // NP over NP with nothing else.
    ("@NP=top < @NP=bottom !<2 __\n" +
    "excise bottom bottom\n" +
    "\n") +

    ("NP-SBJ=bad < (RB < Earlier)\n" +
    "relabel bad NP-TMP\n" +
    "\n") +

    ("@NP < (RB=bad < late|early) < (NN < trading) > (@PP < (IN < In|in))\n" + 
    "relabel bad JJ\n" +
    "\n") +

    // most complements of help take an S complement (ECM analysis), 
    // but some with null subjects don't, just a VP.  Fix them so they do.
    ("@VP < (/^VB/ < help|helps|helped|helping|start|started|starts|starting|begin|begins|began|beginning) < (@VP=site < VB)\n" +
    "adjoin (S (NP-SBJ (-NONE- *)) VP@) site\n" +
    "\n") +

    "";


    // not yet done
    // 94 NP < (RB < much) -- many are determinatives (JJ?)
    // lots of weird stuff in NP < RB !!

    // Lots of PP < RP that should be consistentized somehow!

    // Don Blaheta
    // Markus Dickinson (does he cite Blaheta??)
    // Ratnaparkhi tagger paper
    // Singer et al. POS tagging paper
    // Huddleston and Pullum analyze 'ago' as IN.  Check Treebank guidelines.

    // turn off markCC in definition of goodFactored.  It just doesn't help.

    // Naiwen Xue et al. for CTB discuss similar style of rules and tgrep error
    // detection and correction phase.

}

// Salomon Brothers: is Brothers NNPS or NNP?
// Securities (146 NNP vs. 160 NNPS).
