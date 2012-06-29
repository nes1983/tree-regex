package automaton.core;

import automaton.core.TransitionTable.TDFATransitionTable;

class TDFA {
	final TransitionTable.TDFATransitionTable table;

	public TDFA(final TDFATransitionTable table) {
		super();
		this.table = table;
	}

	@Override
	public String toString() {
		return table.toString();
	}

}
