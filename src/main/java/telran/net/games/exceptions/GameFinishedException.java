package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class GameFinishedException extends IllegalStateException {
	public GameFinishedException() {
		super("Not is already finished");
	}
}
