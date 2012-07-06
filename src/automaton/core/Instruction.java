package automaton.core;

import java.util.LinkedHashMap;
import java.util.Map;

interface Instruction {
	public static class Context {
	}

	static class CopyInstruction implements Instruction {

		public static Instruction make(final int fromTag, final int fromPos,
				final int toTag, final int toPos) {
			return new CopyInstruction(fromTag, fromPos, toTag, toPos);
		}

		final int fromTag, fromPos, toTag, toPos;

		public CopyInstruction(final int fromTag, final int fromPos, final int toTag,
				final int toPos) {
			this.fromTag = fromTag;
			this.fromPos = fromPos;
			this.toTag = toTag;
			this.toPos = toPos;
		}

		public void execute(final Context context, final int pos) {
			throw null;
		}

		@Override
		public String toString() {
			return "" + fromTag + "," + fromPos + " <- " + toTag + "," + toPos;
		}

	}

	/**
	 * Not threadsafe!
	 */
	class InstructionMaker {

		public static InstructionMaker get() {
			return new InstructionMaker();
		}

		int id = -1;

		Map<Tag, Integer> tagIds = new LinkedHashMap<>();

		int lookup(final Tag tag) {
			final Integer id = tagIds.get(tag);
			if (id != null) {
				return id;
			}
			final int ret = nextId();
			tagIds.put(tag, ret);
			return ret;

		}

		public int nextId() {
			return ++id;
		}

		public Instruction reorder(final MapItem from, final MapItem to) {
			return CopyInstruction.make(lookup(from.getTag()), from.getPos(),
					lookup(to.getTag()), to.getPos());
		}

		public Instruction storePos(final MapItem mapItem) {
			return SetInstruction.make(lookup(mapItem.getTag()), mapItem.getPos());
		}
	}

	static class SetInstruction implements Instruction {

		static SetInstruction make(final int tag, final int pos) {
			return new SetInstruction(tag, pos);
		}

		final int tag, pos;

		public SetInstruction(final int tag, final int pos) {
			this.tag = tag;
			this.pos = pos;
		}

		public void execute(final Context context, final int pos) {
			throw null;
		}

		@Override
		public String toString() {
			return "" + tag + "," + pos + "<- pos";
		}
	}

	public void execute(Context context, int pos);
}
