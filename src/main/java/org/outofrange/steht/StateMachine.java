package org.outofrange.steht;

import com.google.common.collect.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * An implementation of a state machine able to choose the shortest path between two states.
 *
 * @param <S> the Enum describing the possible states of this machine
 */
public class StateMachine<S> {
    private static final Logger log = LoggerFactory.getLogger(StateMachine.class);

    private final Table<S, S, List<Runnable>> transitions;

    private final S initialState;
    private S currentState;
    private int transitionsDone = 0;

    StateMachine(Table<S, S, List<Runnable>> transitions, S initialState) {
        this.transitions = Objects.requireNonNull(transitions);
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

                log.trace("Going to state " + state);
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
     * <p/>
     * Also, {@link StateMachine#getTransitionsDone()} will return 0 again after {@code reset}
     */
    public void reset() {
        currentState = initialState;
        transitionsDone = 0;
    }

    /**
     * Sets the state without doing any transitions or checks. Useful if the state was altered externally.
     * @param state the state to set
     */
    public void setState(S state) {
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
    private List<S> getShortestStatePathBetween(S from, S to) {
        final Set<S> reachableStates = getKeysWithoutValue(transitions.row(from));

        if (reachableStates.contains(to)) {
            final List<S> l = new ArrayList<>();
            l.add(from);
            l.add(to);
            return l;
        }

        List<S> shortestRoute = null;
        for (S reachableState : reachableStates) {
            final List<S> statesBetween = getShortestStatePathBetween(reachableState, to);
            if (statesBetween != null && (shortestRoute == null || statesBetween.size() < shortestRoute.size())) {
                shortestRoute = statesBetween;
            }
        }

        if (shortestRoute != null) {
            shortestRoute.add(0, from);
            return shortestRoute;
        } else {
            return null;
        }
    }

    private static <T> Set<T> getKeysWithoutValue(Map<T, ?> map) {
        return map.entrySet().stream().filter(e -> e.getValue() != null).map(Map.Entry::getKey).collect(Collectors
                .toSet());
    }
}