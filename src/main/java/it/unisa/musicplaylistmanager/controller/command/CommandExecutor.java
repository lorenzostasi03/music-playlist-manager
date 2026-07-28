package it.unisa.musicplaylistmanager.controller.command;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.ArrayDeque;
import java.util.Deque;

public class CommandExecutor {

    private static CommandExecutor instance;

    private final Deque<Command> stack;
    private final BooleanProperty canUndo;

    private CommandExecutor() {
        stack = new ArrayDeque<>();
        canUndo = new SimpleBooleanProperty(false);
    }

    public void execute(Command cmd) {
        if (cmd == null)
            throw new IllegalArgumentException("Il comando non può essere null!");

        cmd.execute();
        stack.addFirst(cmd);

        updateCanUndo();
    }

    public void undo() {
        if (stack.isEmpty()) return;

        Command last = stack.peekFirst();

        last.undo();

        stack.removeFirst();

        updateCanUndo();
    }

    private void updateCanUndo() {
        canUndo.set(!stack.isEmpty());
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public static CommandExecutor getInstance() {
        if (instance == null) instance = new CommandExecutor();
        return instance;
    }
}
