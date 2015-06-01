package org.outofrange.steht;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.outofrange.steht.StateMachineTest.TestStates.*;

public class StateMachineTest {
	private StateMachineBuilder<TestStates> b;

	@Before
	public void prepare() {
		b = StateMachineBuilder.create(TestStates.class);
	}

	@Test
	public void goToDirectlyConnectedStateShouldWork() {
		assertGoWorks(b.from(A).to(B), A, B, 1);
	}

	@Test
	public void goOverOneHopShouldWork() {
		assertGoWorks(b.from(A).to(B).from(B).to(C), A, C, 2);
	}

	@Test
	public void goOverTwoHopsShouldWork() {
		assertGoWorks(b.from(A).to(B).from(B).to(C).from(C).to(D), A, D, 3);
	}

	@Test
	public void noHopWhenNoTransition() {
		StateMachine<TestStates> fsm = b.from(A).to(B).from(B).to(C).startAt(A);

		assertEquals(fsm.getCurrentState(), A);
		assertEquals(fsm.getTransitionsDone(), 0);

		try {
			fsm.go(D);
		} catch (IllegalStateException e) {
			// that's okay
		}

		assertEquals(fsm.getCurrentState(), A);
		assertEquals(fsm.getTransitionsDone(), 0);
	}

	@Test
	public void goingShortestPathWhenShortestPathWasConfiguredFirst() {
		assertGoWorks(b.from(A).to(B).from(B).to(F).from(B).to(C).from(C).to(D).from(D).to(E).from(E).to(F), A, F, 2);
	}

	@Test
	public void goingShortestPathWhenShortestPathWasConfiguredLast() {
		assertGoWorks(b.from(A).to(B).from(B).to(C).from(C).to(D).from(D).to(E).from(E).to(F).from(B).to(F), A, F, 2);
	}

	@Test(expected = IllegalStateException.class)
	public void exceptionWhenNoTransition() {
		assertGoWorks(b.from(A).to(B).from(B).to(C).from(C).to(D), A, E, 0);
	}

	private static void assertGoWorks(StateMachineBuilder<TestStates>.TransitionFrom.TransitionTo builder, TestStates
			from, TestStates to, int expectedTransitions) {
		StateMachine<TestStates> fsm = builder.startAt(from);

		assertEquals(fsm.getCurrentState(), from);
		assertEquals(fsm.getTransitionsDone(), 0);

		fsm.go(to);

		assertEquals(fsm.getCurrentState(), to);
		assertEquals(fsm.getTransitionsDone(), expectedTransitions);
	}

	enum TestStates {
		A, B, C, D, E, F
	}
}