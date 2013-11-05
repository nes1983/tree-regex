package ch.unibe.scg.regex;

import java.util.NoSuchElementException;
import java.util.regex.MatchResult;

/**
 * The result of a match operation.
 *
 * <p>A MatchResultTree contains query methods for hierarchical regex results.
 * The match boundaries, groups and group boundaries can be seen but
 * not modified through a {@code MatchResultTree}. Access is thread-safe.
 *
 * @see MatchResultTree#getRoot {@code getRoot} to get access to the hierarchy.
 */
public interface MatchResultTree extends MatchResult {
	/**
	 * @return the {@link TreeNode} of the match of the whole string or throws
	 *         {@link NoSuchElementException} if no match was found.
	 */
	public TreeNode getRoot();

	/**
	 * A {@code TreeNode} is part of the tree structure that is a regex match.
	 * The topmost group is the whole matching string. Its children are the
	 * capture groups on the topmost layer, etc. Each {@code TreeNode} represents
	 * exactly one match for the capture group of {@link TreeNode#getGroup()}.
	 *
	 * <pre>
	 *   Regex:  "(((a+)b)+c)+"
	 *   String: aa b c aaa b c
	 *   Index:  01 2 3 456 7 8
	 *   Tree:   |/ / / |_/ / /
	 *           o / /  o  / /
	 *           |/ /   |_/ /
	 *           o /    o  /
	 *           |/     |_/
	 *           o      o
	 *           |_____/
	 *           o
	 * </pre>
	 * But groups can have different sub-groups on the same layer:
	 *
	 * <pre>
   *   Regex:  "((a+)|(b)|(c))+"
   *   String: aa b  c
   *   Index:  01 2  3
   *   Tree:   |/ |  |
   *           o  o  o
   *            \_|_/
   *              o
   * </pre>
	 */
	public interface TreeNode {
		/**
		 * @return 	all sub-matches of this group. This can be different
		 * 			groups if this group contains more than one group.
		 */
		public Iterable<TreeNode> getChildren();

		/** @return match result for the sub-pattern. */
		@Override
		public String toString();

		/**
		 * @return Number of the group in the original regex. This is basically n
		 * if the group is designated with the <i>n</i>th opening paren. 0 is the matching of
		 * the whole string.
		 *
		 *
		 * <pre>
		 * Regex:     ( ( a+ ) b+) (c+)
		 *            1 2          3
		 * String:    "aaabbccc"
		 * Group 0:   "aaabbccc"
		 * Group 1:   "aaabb"
		 * Group 2:   "aaa"
		 * Group 3:   "ccc"
		 * </pre>
		 */
		public int getGroup();

		// TODO:
		// /** @return {@code TreeNode} of group containing this group or null if this is the root. */
		// public TreeNode getParent();
	}
}
