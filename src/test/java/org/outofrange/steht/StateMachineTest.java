package org.outofrange.steht;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.outofrange.steht.StateMachineTest.TestStates.*;

public class StateMachineTest {
	private StateMachineBuilder<TestStates> b;
	private List<Object> register;

	@Before
	public void prepare() {
		b = StateMachineBuilder.with(TestStates.class);
		register = new ArrayList<>();
	}

	@Test
	public void actionOfOneTransitionIsExecuted() {
		b.from(A).to(B, () -> register.add(1)).startAt(A).go(B);

		assertTrue(register.contains(1));
	}

	@Test
	public void twoActionsOfOneTransitionIsExecuted() {
		b.from(A).to(B, () -> register.add(1)).to(B, () -> register.add(2)).startAt(A).go(B);

		assertTrue(register.indexOf(1) == 0);
		assertTrue(register.indexOf(2) == 1);
	}

	@Test
	public void actionsOnShortestPathAreExecuted() {
		b.from(A).to(B, () -> register.add(1)).to(B, () -> register.add(2))
				.from(B).to(C, () -> register.add(3)).from(C).to(D).from(D).to(E, () -> register.add(4))
				.from(A).to(F, () -> register.add(9))
				.startAt(A).go(E);

		assertTrue(register.indexOf(1) == 0);
		assertTrue(register.indexOf(2) == 1);
		assertTrue(register.indexOf(3) == 2);
		assertTrue(register.indexOf(4) == 3);
		assertFalse(register.contains(9));
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
	public void resetWorksAndChangesTransitionCounter() {
		StateMachine<TestStates> fsm = b.from(A).to(B).from(A).to(C).startAt(A);

		assertEquals(fsm.getCurrentState(), A);
		assertEquals(fsm.getTransitionsDone(), 0);

		fsm.go(B);
		assertEquals(fsm.getCurrentState(), B);
		assertEquals(fsm.getTransitionsDone(), 1);

		fsm.reset();
		fsm.go(C);
		assertEquals(fsm.getCurrentState(), C);
		assertEquals(fsm.getTransitionsDone(), 1);
	}

	@Test
	public void setStateDoesntChangeTransitionCounter() {
		StateMachine<TestStates> fsm = b.from(A).to(B).from(A).to(C).startAt(A);

		assertEquals(fsm.getCurrentState(), A);
		assertEquals(fsm.getTransitionsDone(), 0);

		fsm.go(B);
		assertEquals(fsm.getCurrentState(), B);
		assertEquals(fsm.getTransitionsDone(), 1);

		fsm.setState(A);
		fsm.go(C);
		assertEquals(fsm.getCurrentState(), C);
		assertEquals(fsm.getTransitionsDone(), 2);
	}

	@Test
	public void creatingWithExplicitStates() {
		StateMachineBuilder<TestStates> builder = StateMachineBuilder.with(new TestStates[]{A, B, C});

		assertGoWorks(builder.from(A).to(B).from(B).to(C), A, C, 2);
	}

	@Test(expected = IllegalArgumentException.class)
	public void exceptionWhenConfiguringNonValidState() {
		StateMachineBuilder.with(new TestStates[]{A, B, C}).from(D).to(E);
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