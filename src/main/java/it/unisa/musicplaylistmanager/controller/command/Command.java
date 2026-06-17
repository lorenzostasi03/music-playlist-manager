package it.unisa.musicplaylistmanager.controller.command;

public interface Command {
	void execute();
	void undo();
}
