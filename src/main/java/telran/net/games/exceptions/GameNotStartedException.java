package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class GameNotStartedException extends IllegalStateException {
	public GameNotStartedException() {
		super("The game was not started ");
	}
}
