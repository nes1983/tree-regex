package automaton.core;

interface Instruction {
	public static class Context {
	}

	static class CopyInstruction implements Instruction {
		public static CopyInstruction make(final MapItem from, final MapItem to) {
			throw null;
		}

		MapItem from, to;

		CopyInstruction(final MapItem from, final MapItem to) {
			throw null;
		}

		@Override
		public void execute(final Context context, final int pos) {
			throw null;
		}

		MapItem getFrom() {
			return from;
		}

	}

	public enum InstructionMaker {
		SINGLETON;

		public static InstructionMaker get() {
			return SINGLETON;
		}

		public Instruction reorder(final MapItem from, final MapItem to) {
			return CopyInstruction.make(from, to);
		}

		public Instruction storePos(final MapItem mapItem) {
			return SetInstruction.make(mapItem);
		}
	}

	static class SetInstruction implements Instruction {

		static SetInstruction make(final MapItem mapItem) {
			throw null;
		}

		SetInstruction(final MapItem mapItem) {

		}

		@Override
		public void execute(final Context context, final int pos) {
			throw null;
		}

	}

	public void execute(Context context, int pos);
}
