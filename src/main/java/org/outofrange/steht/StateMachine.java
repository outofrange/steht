package org.outofrange.steht;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * An implementation of a state machine able to choose the shortest path between two states.
 *
 * @param <S> the Enum describing the possible states of this machine
 */
public class StateMachine<S> {
	private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

	private final TransitionTable<S> transitions;

	private final S initialState;
	private S currentState;
	private int transitionsDone = 0;

	StateMachine(TransitionTable<S> transitions, S initialState) {
		try {
			this.transitions = Objects.requireNonNull(transitions).clone();
		} catch (CloneNotSupportedException e) {
			throw new UnsupportedOperationException("Can't clone transitions table", e);
		}

		this.initialState = currentState = Objects.requireNonNull(initialState);
	}

	/**
	 * Tries to look for the shortest path from {@code currentState} to {@code state} and executing all registered
	 * transition actions.
	 *
	 * @param state the state to go to
	 * @return this
	 * @throws IllegalArgumentException if there is no path to {@code state}
	 */
	public StateMachine<S> go(S state) {
		if (currentState != state) {
			final List<Runnable> runnables = transitions.get(currentState, state);

			if (runnables != null) {
				// there's a direct path

				log.trace("Going to state {}", state);
				runnables.forEach(Runnable::run);

				currentState = state;
				transitionsDone++;
			} else {
				// check if there is a path
				List<S> intermediaryStates = getShortestStatePathBetween(currentState, state);

				if (intermediaryStates != null) {
					// the first item is the same as currentState, but since we ignore going to the current state,
					// we don't have to strip it
					intermediaryStates.forEach(this::go);
				} else {
					throw new IllegalStateException("There is no valid transition!");
				}
			}
		}

		return this;
	}

	/**
	 * Returns the current state the machine is in
	 *
	 * @return the current state of the machine
	 */
	public S getCurrentState() {
		return currentState;
	}

	/**
	 * Returns how many transitions were done by this machine.
	 * <p>
	 * Most used for debugging purpouses.
	 *
	 * @return an integer greater or equal to 0, describing how many transitions were done
	 */
	public int getTransitionsDone() {
		return transitionsDone;
	}

	/**
	 * Resets the current state to the state the machine was created with, without doing any transitions.
	 * <p>
	 * Also, {@link StateMachine#getTransitionsDone()} will return 0 again after {@code reset}
	 */
	public void reset() {
		log.trace("Resetting state machine");
		setState(initialState);
		transitionsDone = 0;
	}

	/**
	 * Sets the state without doing any transitions or checks. Useful if the state was altered externally.
	 *
	 * @param state the state to set
	 */
	public void setState(S state) {
		log.trace("Setting state to {} without doing any transitions", state);
		currentState = state;
	}

	/**
	 * Looks for the shortest available state path between the states {@code from} and {@code to}
	 * <p>
	 * Given the transitions {@code A -&gt; B -&gt; C -&gt; D -&gt; E}, a call to
	 * {@code getShortestStatePathBetween(B, D)} will return the list {@code [B, C, D]}
	 *
	 * @param from the state to start looking
	 * @param to   the state to find a path to
	 * @return either a list describing the shortest path from {@code from} to {@code to} (including themselves),
	 * or null if no path could be found
	 */
	List<S> getShortestStatePathBetween(S from, S to) {
		// in the transition table, on state is reachable from another state, if there the intersection in the table
		// is != null
		final Set<S> reachableStates = transitions.getReachableStates(from);

		// is the target state B directly reachable from this state, A?
		// then it's A -> B
		if (reachableStates.contains(to)) {
			final List<S> l = new ArrayList<>();
			l.add(from);
			l.add(to);
			return l;
		}

		// if it isn't, we have to look for the shortest path
		List<S> shortestPath = null;
		for (S reachableState : reachableStates) {
			final List<S> statesBetween = getShortestStatePathBetween(reachableState, to);
			if (statesBetween != null && (shortestPath == null || statesBetween.size() < shortestPath.size())) {
				shortestPath = statesBetween;
			}
		}

		if (shortestPath != null) {
			// that's the shortest path from one of our directly reachable state, so we have to add ourself
			shortestPath.add(0, from);
			return shortestPath;
		} else {
			return null;
		}
	}
}