package org.outofrange.steht;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for creating enum based StateMachines.
 * <p>
 * To create a builder, call the static factory method {@link StateMachineBuilder#create(Class)}
 * <p>
 * Configuration is fluently done using {@link StateMachineBuilder#from(Object)} and
 * {@link TransitionFrom#to(Object, Runnable)}.
 * <p>
 * Example usage:
 * <pre>
 *     StateMachineBuilder&lt;SomeEnum&gt; builder = StateMachineBuilder.create(SomeEnum.class);
 *
 *     builder.from(SomeEnum.A).to(SomeEnum.B)
 *     .from(SomeEnum.B).to(SomeEnum.C).to(SomeEnum.D)
 *     .from(SomeEnum.A).to(SomeEnum.C, () -&gt; System.out.println(&quot;Transition to C&quot;);
 *
 *     StateMachine&lt;SomeEnum<&gt; stateMachine = builder.startAt(SomeEnum.A);
 * </pre>
 *
 * @param <S> the used Enum
 */
public class StateMachineBuilder<S> {
    private final Table<S, S, List<Runnable>> transitions;

    private StateMachineBuilder(S[] validStates) {
        final List<S> valueList = Arrays.asList(validStates);
        transitions = ArrayTable.create(valueList, valueList);
    }

    /**
     * Creates a new builder using an enum class and all their declared constants
     *
     * @param enumClass an enum class
     * @return a new builder to configure possible transitions
     */
    public static <T extends Enum<T>> StateMachineBuilder<T> create(Class<T> enumClass) {
        return new StateMachineBuilder<>(enumClass.getEnumConstants());
    }

    /**
     * Creates a new builder using the given states
     *
     * @param states possible states for this state machine builder
     * @return a new builder to configure possible transitions
     */
    public static <T> StateMachineBuilder<T> create(T[] states) {
        return new StateMachineBuilder<>(states);
    }

    public TransitionFrom from(S state) {
        return new TransitionFrom(transitions.row(state));
    }

    /**
     * Creates a new {@link StateMachine} using the current configuration
     *
     * @param initialState the starting state of the state machine
     * @return a new StateMachine
     */
    public StateMachine<S> startAt(S initialState) {
        return new StateMachine<>(transitions, initialState);
    }

    public class TransitionFrom {
        private final Map<S, List<Runnable>> transitionsTo;

        private TransitionFrom(Map<S, List<Runnable>> transitionsTo) {
            this.transitionsTo = transitionsTo;
        }

        /**
         * Creates a new transition to {@code state}, executing the transition {@code transition} when
         * switching the state to it
         *
         * @param state      the state to create the transition to
         * @param transition a functional interface which should be executed when transitioning to {@code state}
         */
        public TransitionTo to(S state, Runnable transition) {
            List<Runnable> runnables = transitionsTo.get(state);

            if (runnables == null) {
                runnables = new ArrayList<>();
                transitionsTo.put(state, runnables);
            }

            runnables.add(transition);

            return new TransitionTo(transitionsTo);
        }

        /**
         * Creates a new transition to {@code state} without an action
         *
         * @param state the state to create the transition to
         */
        public TransitionTo to(S state) {
            List<Runnable> runnables = transitionsTo.get(state);

            if (runnables == null) {
                runnables = new ArrayList<>();
                transitionsTo.put(state, runnables);
            }

            return new TransitionTo(transitionsTo);
        }

        public class TransitionTo extends TransitionFrom {
            private TransitionTo(Map<S, List<Runnable>> transitionsTo) {
                super(transitionsTo);
            }

            /**
             * @see StateMachineBuilder#from(Object)
             */
            public TransitionFrom from(S state) {
                return StateMachineBuilder.this.from(state);
            }

            /**
             * @see StateMachineBuilder#startAt(Object)
             */
            public StateMachine<S> startAt(S initialState) {
                return StateMachineBuilder.this.startAt(initialState);
            }
        }
    }
}