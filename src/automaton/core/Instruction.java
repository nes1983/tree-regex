package automaton.core;

import automaton.instructions.Context;

interface Instruction {
	static class CopyInstruction implements Instruction {
		// private final MapItem from, to;

		public static CopyInstruction make(final MapItem from, final MapItem to) {
			throw null;
		}

		CopyInstruction(final MapItem from, final MapItem to) {

		}

		@Override
		public void execute(final Context context, final int pos) {
			throw null;
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
