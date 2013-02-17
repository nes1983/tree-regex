/*
 * dk.brics.automaton
 * 
 * Copyright (c) 2001-2011 Anders Moeller All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met: 1. Redistributions of source code must retain the
 * above copyright notice, this list of conditions and the following disclaimer. 2. Redistributions
 * in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package ch.unibe.scg.regex;


// NES DID NOT USE BECAUSE IS NOT STANDARD REGEXP SYNTAX.

/**
 * Regular Expression extension to <code>Automaton</code>.
 * <p>
 * Regular expressions are built from the following abstract syntax:
 * <p>
 * <table border=0>
 * <tr>
 * <td><i>regexp</i></td>
 * <td>::=</td>
 * <td><i>unionexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>unionexp</i></td>
 * <td>::=</td>
 * <td><i>interexp</i>&nbsp;<tt><b>|</b></tt>&nbsp;<i>unionexp</i></td>
 * <td>(union)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>interexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>interexp</i></td>
 * <td>::=</td>
 * <td><i>concatexp</i>&nbsp;<tt><b>&amp;</b></tt>&nbsp;<i>interexp</i></td>
 * <td>(intersection)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>concatexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>concatexp</i></td>
 * <td>::=</td>
 * <td><i>repeatexp</i>&nbsp;<i>concatexp</i></td>
 * <td>(concatenation)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>repeatexp</i></td>
 * <td>::=</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>?</b></tt></td>
 * <td>(zero or one occurrence)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>*</b></tt></td>
 * <td>(zero or more occurrences)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>+</b></tt></td>
 * <td>(one or more occurrences)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>}</b></tt></td>
 * <td>(<tt><i>n</i></tt> occurrences)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,}</b></tt></td>
 * <td>(<tt><i>n</i></tt> or more occurrences)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>repeatexp</i>&nbsp;<tt><b>{</b><i>n</i><b>,</b><i>m</i><b>}</b></tt></td>
 * <td>(<tt><i>n</i></tt> to <tt><i>m</i></tt> occurrences, including both)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>complexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>complexp</i></td>
 * <td>::=</td>
 * <td><tt><b>~</b></tt>&nbsp;<i>complexp</i></td>
 * <td>(complement)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>charclassexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>charclassexp</i></td>
 * <td>::=</td>
 * <td><tt><b>[</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td>
 * <td>(character class)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>[^</b></tt>&nbsp;<i>charclasses</i>&nbsp;<tt><b>]</b></tt></td>
 * <td>(negated character class)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>simpleexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>charclasses</i></td>
 * <td>::=</td>
 * <td><i>charclass</i>&nbsp;<i>charclasses</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>charclass</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>charclass</i></td>
 * <td>::=</td>
 * <td><i>charexp</i>&nbsp;<tt><b>-</b></tt>&nbsp;<i>charexp</i></td>
 * <td>(character range, including end-points)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><i>charexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * 
 * <tr>
 * <td><i>simpleexp</i></td>
 * <td>::=</td>
 * <td><i>charexp</i></td>
 * <td></td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>.</b></tt></td>
 * <td>(any single character)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>#</b></tt></td>
 * <td>(the empty language)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>@</b></tt></td>
 * <td>(any string)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>"</b></tt>&nbsp;&lt;Unicode string without double-quotes&gt;&nbsp; <tt><b>"</b></tt></td>
 * <td>(a string)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>(</b></tt>&nbsp;<tt><b>)</b></tt></td>
 * <td>(the empty string)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>(</b></tt>&nbsp;<i>unionexp</i>&nbsp;<tt><b>)</b></tt></td>
 * <td>(precedence override)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>&lt;</b></tt>&nbsp;&lt;identifier&gt;&nbsp;<tt><b>&gt;</b></tt></td>
 * <td>(named automaton)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>&lt;</b><i>n</i>-<i>m</i><b>&gt;</b></tt></td>
 * <td>(numerical interval)</td>
 * <td><small>[OPTIONAL]</small></td>
 * </tr>
 * 
 * <tr>
 * <td><i>charexp</i></td>
 * <td>::=</td>
 * <td>&lt;Unicode character&gt;</td>
 * <td>(a single non-reserved character)</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td></td>
 * <td>|</td>
 * <td><tt><b>\</b></tt>&nbsp;&lt;Unicode character&gt;&nbsp;</td>
 * <td>(a single character)</td>
 * <td></td>
 * </tr>
 * </table>
 * <p>
 * The productions marked <small>[OPTIONAL]</small> are only allowed if specified by the syntax
 * flags passed to the <code>RegExp</code> constructor. The reserved characters used in the
 * (enabled) syntax must be escaped with backslash (<tt><b>\</b></tt>) or double-quotes (
 * <tt><b>"..."</b></tt>). (In contrast to other regexp syntaxes, this is required also in character
 * classes.) Be aware that dash (<tt><b>-</b></tt>) has a special meaning in <i>charclass</i>
 * expressions. An identifier is a string not containing right angle bracket (<tt><b>&gt;</b></tt>)
 * or dash (<tt><b>-</b></tt>). Numerical intervals are specified by non-negative decimal integers
 * and include both end points, and if <tt><i>n</i></tt> and <tt><i>m</i></tt> have the same number
 * of digits, then the conforming strings must have that length (i.e. prefixed by 0's).
 * 
 * @author Anders M&oslash;ller &lt;<a href="mailto:amoeller@cs.au.dk">amoeller@cs.au.dk</a>&gt;
 * */
class RegExp {

  enum Kind {
    REGEXP_ANYCHAR, REGEXP_ANYSTRING, REGEXP_AUTOMATON, REGEXP_CHAR, REGEXP_CHAR_RANGE, REGEXP_COMPLEMENT, REGEXP_CONCATENATION, REGEXP_EMPTY, REGEXP_INTERSECTION, REGEXP_INTERVAL, REGEXP_OPTIONAL, REGEXP_REPEAT, REGEXP_REPEAT_MIN, REGEXP_REPEAT_MINMAX, REGEXP_STRING, REGEXP_UNION
  }

  static RegExp makeAnyChar() {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_ANYCHAR;
    return r;
  }

  static RegExp makeAnyString() {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_ANYSTRING;
    return r;
  }

  static RegExp makeChar(final char c) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_CHAR;
    r.c = c;
    return r;
  }

  static RegExp makeCharRange(final char from, final char to) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_CHAR_RANGE;
    r.from = from;
    r.to = to;
    return r;
  }

  static RegExp makeComplement(final RegExp exp) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_COMPLEMENT;
    r.exp1 = exp;
    return r;
  }

  static RegExp makeConcatenation(final RegExp exp1, final RegExp exp2) {
    if ((exp1.kind == Kind.REGEXP_CHAR || exp1.kind == Kind.REGEXP_STRING)
        && (exp2.kind == Kind.REGEXP_CHAR || exp2.kind == Kind.REGEXP_STRING)) {
      return makeString(exp1, exp2);
    }
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_CONCATENATION;
    if (exp1.kind == Kind.REGEXP_CONCATENATION
        && (exp1.exp2.kind == Kind.REGEXP_CHAR || exp1.exp2.kind == Kind.REGEXP_STRING)
        && (exp2.kind == Kind.REGEXP_CHAR || exp2.kind == Kind.REGEXP_STRING)) {
      r.exp1 = exp1.exp1;
      r.exp2 = makeString(exp1.exp2, exp2);
    } else if ((exp1.kind == Kind.REGEXP_CHAR || exp1.kind == Kind.REGEXP_STRING)
        && exp2.kind == Kind.REGEXP_CONCATENATION
        && (exp2.exp1.kind == Kind.REGEXP_CHAR || exp2.exp1.kind == Kind.REGEXP_STRING)) {
      r.exp1 = makeString(exp1, exp2.exp1);
      r.exp2 = exp2.exp2;
    } else {
      r.exp1 = exp1;
      r.exp2 = exp2;
    }
    return r;
  }

  static RegExp makeEmpty() {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_EMPTY;
    return r;
  }

  static RegExp makeIntersection(final RegExp exp1, final RegExp exp2) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_INTERSECTION;
    r.exp1 = exp1;
    r.exp2 = exp2;
    return r;
  }

  static RegExp makeInterval(final int min, final int max, final int digits) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_INTERVAL;
    r.min = min;
    r.max = max;
    r.digits = digits;
    return r;
  }

  static RegExp makeOptional(final RegExp exp) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_OPTIONAL;
    r.exp1 = exp;
    return r;
  }

  static RegExp makeRepeat(final RegExp exp) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_REPEAT;
    r.exp1 = exp;
    return r;
  }

  static RegExp makeRepeat(final RegExp exp, final int min) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_REPEAT_MIN;
    r.exp1 = exp;
    r.min = min;
    return r;
  }

  static RegExp makeRepeat(final RegExp exp, final int min, final int max) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_REPEAT_MINMAX;
    r.exp1 = exp;
    r.min = min;
    r.max = max;
    return r;
  }

  static private RegExp makeString(final RegExp exp1, final RegExp exp2) {
    final StringBuilder b = new StringBuilder();
    if (exp1.kind == Kind.REGEXP_STRING) {
      b.append(exp1.s);
    } else {
      b.append(exp1.c);
    }
    if (exp2.kind == Kind.REGEXP_STRING) {
      b.append(exp2.s);
    } else {
      b.append(exp2.c);
    }
    return makeString(b.toString());
  }

  static RegExp makeString(final String s) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_STRING;
    r.s = s;
    return r;
  }

  static RegExp makeUnion(final RegExp exp1, final RegExp exp2) {
    final RegExp r = new RegExp();
    r.kind = Kind.REGEXP_UNION;
    r.exp1 = exp1;
    r.exp2 = exp2;
    return r;
  }

  String b;

  char c;

  RegExp exp1, exp2;

  char from, to;

  Kind kind;

  int min, max, digits;

  int pos;

  String s;

  RegExp() {}

  /**
   * Constructs new <code>RegExp</code> from a string.
   * 
   * @param s regexp string
   * @param syntax_flags boolean 'or' of optional syntax constructs to be enabled
   * @exception IllegalArgumentException if an error occured while parsing the regular expression
   */
  public RegExp(final String s) throws IllegalArgumentException {
    b = s;
    RegExp e;
    if (s.length() == 0) {
      e = makeString("");
    } else {
      e = parseUnionExp();
      if (pos < b.length()) {
        throw new IllegalArgumentException("end-of-string expected at position " + pos);
      }
    }
    kind = e.kind;
    exp1 = e.exp1;
    exp2 = e.exp2;
    this.s = e.s;
    c = e.c;
    min = e.min;
    max = e.max;
    digits = e.digits;
    from = e.from;
    to = e.to;
    b = null;
  }

  private boolean match(final char c) {
    if (pos >= b.length()) {
      return false;
    }
    if (b.charAt(pos) == c) {
      pos++;
      return true;
    }
    return false;
  }

  private boolean more() {
    return pos < b.length();
  }

  private char next() throws IllegalArgumentException {
    if (!more()) {
      throw new IllegalArgumentException("unexpected end-of-string");
    }
    return b.charAt(pos++);
  }

  RegExp parseCharClass() throws IllegalArgumentException {
    final char c = parseCharExp();
    if (match('-')) {
      if (peek("]")) {
        return makeUnion(makeChar(c), makeChar('-'));
      } else {
        return makeCharRange(c, parseCharExp());
      }
    } else {
      return makeChar(c);
    }
  }

  RegExp parseCharClasses() throws IllegalArgumentException {
    RegExp e = parseCharClass();
    while (more() && !peek("]")) {
      e = makeUnion(e, parseCharClass());
    }
    return e;
  }

  final RegExp parseCharClassExp() throws IllegalArgumentException {
    if (match('[')) {
      boolean negate = false;
      if (match('^')) {
        negate = true;
      }
      RegExp e = parseCharClasses();
      if (negate) {
        e = makeIntersection(makeAnyChar(), makeComplement(e));
      }
      if (!match(']')) {
        throw new IllegalArgumentException("expected ']' at position " + pos);
      }
      return e;
    } else {
      return parseSimpleExp();
    }
  }

  char parseCharExp() throws IllegalArgumentException {
    match('\\');
    return next();
  }

  final RegExp parseComplExp() throws IllegalArgumentException {
    if (match('~')) {
      return makeComplement(parseComplExp());
    } else {
      return parseCharClassExp();
    }
  }

  RegExp parseConcatExp() throws IllegalArgumentException {
    RegExp e = parseRepeatExp();
    if (more() && !peek(")|") && (!peek("&"))) {
      e = makeConcatenation(e, parseConcatExp());
    }
    return e;
  }

  RegExp parseInterExp() throws IllegalArgumentException {
    RegExp e = parseConcatExp();
    if (match('&')) {
      e = makeIntersection(e, parseInterExp());
    }
    return e;
  }

  RegExp parseRepeatExp() throws IllegalArgumentException {
    RegExp e = parseComplExp();
    while (peek("?*+{")) {
      if (match('?')) {
        e = makeOptional(e);
      } else if (match('*')) {
        e = makeRepeat(e);
      } else if (match('+')) {
        e = makeRepeat(e, 1);
      } else if (match('{')) {
        int start = pos;
        while (peek("0123456789")) {
          next();
        }
        if (start == pos) {
          throw new IllegalArgumentException("integer expected at position " + pos);
        }
        final int n = Integer.parseInt(b.substring(start, pos));
        int m = -1;
        if (match(',')) {
          start = pos;
          while (peek("0123456789")) {
            next();
          }
          if (start != pos) {
            m = Integer.parseInt(b.substring(start, pos));
          }
        } else {
          m = n;
        }
        if (!match('}')) {
          throw new IllegalArgumentException("expected '}' at position " + pos);
        }
        if (m == -1) {
          e = makeRepeat(e, n);
        } else {
          e = makeRepeat(e, n, m);
        }
      }
    }
    return e;
  }

  RegExp parseSimpleExp() throws IllegalArgumentException {
    if (match('.')) {
      return makeAnyChar();
    } else if (match('"')) {
      final int start = pos;
      while (more() && !peek("\"")) {
        next();
      }
      if (!match('"')) {
        throw new IllegalArgumentException("expected '\"' at position " + pos);
      }
      return makeString(b.substring(start, pos - 1));
    } else if (match('(')) {
      if (match(')')) {
        return makeString("");
      }
      final RegExp e = parseUnionExp();
      if (!match(')')) {
        throw new IllegalArgumentException("expected ')' at position " + pos);
      }
      return e;
    } else {
      return makeChar(parseCharExp());
    }
  }

  final RegExp parseUnionExp() throws IllegalArgumentException {
    RegExp e = parseInterExp();
    if (match('|')) {
      e = makeUnion(e, parseUnionExp());
    }
    return e;
  }

  private boolean peek(final String s) {
    return more() && s.indexOf(b.charAt(pos)) != -1;
  }

  /**
   * Constructs string from parsed regular expression.
   */
  @Override
  public String toString() {
    return toStringBuilder(new StringBuilder()).toString();
  }

  StringBuilder toStringBuilder(final StringBuilder b) {
    switch (kind) {
      case REGEXP_UNION:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append("|");
        exp2.toStringBuilder(b);
        b.append(")");
        break;
      case REGEXP_CONCATENATION:
        exp1.toStringBuilder(b);
        exp2.toStringBuilder(b);
        break;
      case REGEXP_INTERSECTION:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append("&");
        exp2.toStringBuilder(b);
        b.append(")");
        break;
      case REGEXP_OPTIONAL:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append(")?");
        break;
      case REGEXP_REPEAT:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append(")*");
        break;
      case REGEXP_REPEAT_MIN:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append("){").append(min).append(",}");
        break;
      case REGEXP_REPEAT_MINMAX:
        b.append("(");
        exp1.toStringBuilder(b);
        b.append("){").append(min).append(",").append(max).append("}");
        break;
      case REGEXP_COMPLEMENT:
        b.append("~(");
        exp1.toStringBuilder(b);
        b.append(")");
        break;
      case REGEXP_CHAR:
        b.append("\\").append(c);
        break;
      case REGEXP_CHAR_RANGE:
        b.append("[\\").append(from).append("-\\").append(to).append("]");
        break;
      case REGEXP_ANYCHAR:
        b.append(".");
        break;
      case REGEXP_EMPTY:
        b.append("#");
        break;
      case REGEXP_STRING:
        b.append("\"").append(s).append("\"");
        break;
      case REGEXP_ANYSTRING:
        b.append("@");
        break;
      case REGEXP_AUTOMATON:
        b.append("<").append(s).append(">");
        break;
      case REGEXP_INTERVAL:
        final String s1 = Integer.toString(min);
        final String s2 = Integer.toString(max);
        b.append("<");
        if (digits > 0) {
          for (int i = s1.length(); i < digits; i++) {
            b.append('0');
          }
        }
        b.append(s1).append("-");
        if (digits > 0) {
          for (int i = s2.length(); i < digits; i++) {
            b.append('0');
          }
        }
        b.append(s2).append(">");
        break;
    }
    return b;
  }
}
