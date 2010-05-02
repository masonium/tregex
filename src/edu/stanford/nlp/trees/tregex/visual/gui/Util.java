package edu.stanford.nlp.trees.tregex.visual.gui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class Util {

  public static Matcher isValidRegex(String possibleRegex) {
    try {
      return Pattern.compile( possibleRegex ).matcher( "" );
    } catch (PatternSyntaxException e) {
      return null;
    }
  }

  public static String escapeHTML(String s) {
    StringBuffer sb = new StringBuffer();
    int n = s.length();
    for (int i = 0; i < n; i++) {
      char c = s.charAt( i );
      switch (c) {
      case '<':
        sb.append( "&lt;" );
        break;
      case '>':
        sb.append( "&gt;" );
        break;
      case '&':
        sb.append( "&amp;" );
        break;
      case '"':
        sb.append( "&quot;" );
        break;
      default:
        sb.append( c );
        break;
      }
    }
    return sb.toString();
  }
}
