package automaton.core;

import automaton.core.TransitionTable.RealCaptureGroup;

interface Tag extends Comparable<Tag> {
	static class Marker implements Tag {
		final String name;

		Marker(final String name) {
			this.name = name;
		}

		public int compareTo(final Tag o) {
			return 0; // XXX unsupported?
		}

		public int getGroup() {
			return 0;
		}

		@Override
		public boolean isEndTag() {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isStartTag() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return name;
		}

	}

	static abstract class RealTag implements Tag {
		static class EndTag extends RealTag {

			EndTag(final CaptureGroup captureGroup) {
				super(captureGroup);
			}

			@Override
			public boolean isEndTag() {
				return true;
			}

			@Override
			public boolean isStartTag() {
				return false;
			}

			@Override
			public String toString() {
				return "➁" + getGroup();
			}
		}

		static class StartTag extends RealTag {

			StartTag(final CaptureGroup captureGroup) {
				super(captureGroup);
			}

			@Override
			public boolean isEndTag() {
				return false;
			}

			@Override
			public boolean isStartTag() {
				return true;
			}

			@Override
			public String toString() {
				return "➀" + getGroup();
			}
		}

		public static Tag makeEndTag(final RealCaptureGroup cg) {
			return new EndTag(cg);
		}

		public static Tag makeStartTag(final RealCaptureGroup cg) {
			return new StartTag(cg);
		}

		final CaptureGroup captureGroup;

		RealTag(final CaptureGroup captureGroup) {
			super();
			this.captureGroup = captureGroup;
		}

		@Override
		public int compareTo(final Tag o) {
			return Integer.valueOf(getGroup()).compareTo(o.getGroup());
		}

		@Override
		public int getGroup() {
			return captureGroup.getNumber();
		}

		public abstract boolean isEndTag();

		public abstract boolean isStartTag();
	}

	public final Tag ENTIRE_MATCH = new Marker("ENTIRE_MATCH");

	public final Tag NONE = new Marker("NONE");

	public int getGroup();

	public boolean isEndTag();

	public boolean isStartTag();
}