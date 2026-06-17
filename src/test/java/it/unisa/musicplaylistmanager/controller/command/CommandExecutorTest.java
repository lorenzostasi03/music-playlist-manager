package it.unisa.musicplaylistmanager.controller.command;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test della classe {@link CommandExecutor}.
 */
class CommandExecutorTest {

	private CommandExecutor executor;

	@BeforeEach
	void setUp() throws Exception {
		resetSingleton();
		executor = CommandExecutor.getInstance();
	}

	@Test
	void getInstanceRestituisceSempreLaStessaIstanza() {
		CommandExecutor secondInstance = CommandExecutor.getInstance();

		assertSame(executor, secondInstance);
	}

	@Test
	void inizialmenteNonSonoPresentiComandiDaAnnullare() {
		assertFalse(executor.canUndoProperty().get());
	}

	@Test
	void executeConComandoNullLanciaEccezione() {
		IllegalArgumentException exception = assertThrows(
				IllegalArgumentException.class,
				() -> executor.execute(null));

		assertEquals(
				"Il comando non può essere null!",
				exception.getMessage());
	}

	@Test
	void executeEsegueIlComando() {
		TestCommand command = new TestCommand("primo");

		executor.execute(command);

		assertAll(
				() -> assertEquals(1, command.getExecuteCount()),
				() -> assertEquals(0, command.getUndoCount()));
	}

	@Test
	void executeRendeDisponibileUndo() {
		TestCommand command = new TestCommand("primo");

		executor.execute(command);

		assertTrue(executor.canUndoProperty().get());
	}

	@Test
	void undoAnnullaUltimoComandoEseguito() {
		TestCommand command = new TestCommand("primo");

		executor.execute(command);
		executor.undo();

		assertAll(
				() -> assertEquals(1, command.getExecuteCount()),
				() -> assertEquals(1, command.getUndoCount()),
				() -> assertFalse(executor.canUndoProperty().get()));
	}

	@Test
	void undoRispettaOrdineLifo() {
		List<String> operations = new ArrayList<>();

		TestCommand firstCommand = new TestCommand("primo", operations);
		TestCommand secondCommand = new TestCommand("secondo", operations);

		executor.execute(firstCommand);
		executor.execute(secondCommand);

		executor.undo();
		executor.undo();

		assertEquals(
				List.of(
						"execute-primo",
						"execute-secondo",
						"undo-secondo",
						"undo-primo"),
				operations);
	}

	@Test
	void undoDiUnSoloComandoMantieneDisponibileUnAltroUndo() {
		TestCommand firstCommand = new TestCommand("primo");
		TestCommand secondCommand = new TestCommand("secondo");

		executor.execute(firstCommand);
		executor.execute(secondCommand);

		executor.undo();

		assertAll(
				() -> assertEquals(0, firstCommand.getUndoCount()),
				() -> assertEquals(1, secondCommand.getUndoCount()),
				() -> assertTrue(executor.canUndoProperty().get()));
	}

	@Test
	void undoDiTuttiIComandiDisabilitaUndo() {
		TestCommand firstCommand = new TestCommand("primo");
		TestCommand secondCommand = new TestCommand("secondo");

		executor.execute(firstCommand);
		executor.execute(secondCommand);

		executor.undo();
		executor.undo();

		assertFalse(executor.canUndoProperty().get());
	}

	@Test
	void undoSuStackVuotoNonGeneraEccezione() {
		assertDoesNotThrow(executor::undo);

		assertFalse(executor.canUndoProperty().get());
	}

	@Test
	void comandoAnnullatoNonVieneAnnullatoUnaSecondaVolta() {
		TestCommand command = new TestCommand("primo");

		executor.execute(command);
		executor.undo();
		executor.undo();

		assertEquals(1, command.getUndoCount());
	}

	/**
	 * Ripristina il singleton per garantire l'indipendenza tra i test.
	 */
	private void resetSingleton() throws Exception {
		Field instanceField = CommandExecutor.class.getDeclaredField("instance");
		instanceField.setAccessible(true);
		instanceField.set(null, null);
	}

	/**
	 * Comando minimale utilizzato per verificare il comportamento del command
	 * executor.
	 */
	private static class TestCommand implements Command {

		private final String name;
		private final List<String> operations;

		private int executeCount;
		private int undoCount;

		TestCommand(String name) {
			this(name, new ArrayList<>());
		}

		TestCommand(String name, List<String> operations) {
			this.name = name;
			this.operations = operations;
		}

		@Override
		public void execute() {
			executeCount++;
			operations.add("execute-" + name);
		}

		@Override
		public void undo() {
			undoCount++;
			operations.add("undo-" + name);
		}

		int getExecuteCount() {
			return executeCount;
		}

		int getUndoCount() {
			return undoCount;
		}
	}
}