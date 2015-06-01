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
public class TransitionTable<S> {
	private final Table<S, S, List<Runnable>> table;

	private TransitionTable(S[] validStates) {
		final List<S> states = Arrays.asList(Objects.requireNonNull(validStates));
		table = ArrayTable.create(states, states);
	}

	static <T> TransitionTable<T> create(T[] states) {
		return new TransitionTable<>(states);
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
}
