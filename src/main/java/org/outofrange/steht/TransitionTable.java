package org.outofrange.steht;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stores all transitions and optional actions for a state machine
 *
 * @param <S> the type of the states
 */
class TransitionTable<S> implements Cloneable {
	private final Table<S, S, List<Runnable>> table;

	private TransitionTable(S[] validStates) {
		final List<S> states = Arrays.asList(Objects.requireNonNull(validStates));
		table = ArrayTable.create(states, states);
	}

	private TransitionTable(TransitionTable<S> transitionTable) {
		this.table = ArrayTable.create(transitionTable.table);
	}

	static <T> TransitionTable<T> create(T[] states) {
		return new TransitionTable<>(states);
	}

	@Override
	public TransitionTable<S> clone() throws CloneNotSupportedException {
		super.clone();
		return new TransitionTable<>(this);
	}

	/**
	 * Returns all defined actions for the transition from {@code from} to {@code to}
	 *
	 * @param from the state where the transition starts
	 * @param to   the state where the transition ends
	 * @return all defined actions for the transition from {@code from} to {@code to}
	 */
	public List<Runnable> get(S from, S to) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);

		return table.get(from, to);
	}

	/**
	 * Adds a transition from {@code from} to {@code to}, with an optional {@code action}
	 * <p>
	 * This method may be called multiple times to add multiple actions for a single transition
	 *
	 * @param from   the state where the transition starts
	 * @param to     the state where the transition ends
	 * @param action an action, may be null
	 */
	public void addTransition(S from, S to, Runnable action) {
		Objects.requireNonNull(from);
		Objects.requireNonNull(to);

		List<Runnable> runnables = table.get(from, to);

		if (runnables == null) {
			runnables = new ArrayList<>();
			table.put(from, to, runnables);
		}

		if (action != null) {
			runnables.add(action);
		}
	}

	/**
	 * Returns all states that are reachable from {@code from}
	 *
	 * @param from the state where to look for reachable states
	 * @return all states that have a transition configured from {@code from}
	 */
	public Set<S> getReachableStates(S from) {
		Objects.requireNonNull(from);

		return table.row(from).entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect
				(Collectors.toSet());
	}

	@Override
	public String toString() {
		final StringJoiner joinNewline = new StringJoiner("\n");

		for (Map.Entry<S, Map<S, List<Runnable>>> rowEntry : table.rowMap().entrySet()) {
			final StringBuilder sb = new StringBuilder();
			sb.append(rowEntry.getKey()).append(": ");

			final StringJoiner joinBlank = new StringJoiner(" ");
			for (S column : getReachableStates(rowEntry.getKey())) {
				joinBlank.add(column.toString());
			}

			joinNewline.add(sb.append(joinBlank));
		}

		return joinNewline.toString();
	}
}
