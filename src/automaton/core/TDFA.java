package automaton.core;

import java.util.List;

import automaton.core.TransitionTable.TDFATransitionTable;

class TDFA {
	final List<Instruction> initializer;
	final TransitionTable.TDFATransitionTable table;

	public TDFA(final TDFATransitionTable table, final List<Instruction> initializer) {
		this.table = table;
		this.initializer = initializer;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(table.toString());
		sb.append('\n');
		sb.append(initializer);
		return sb.toString();
	}

}
