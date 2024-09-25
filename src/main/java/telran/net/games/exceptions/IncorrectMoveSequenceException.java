package telran.net.games.exceptions;

@SuppressWarnings("serial")
public class IncorrectMoveSequenceException extends IllegalArgumentException {
	public IncorrectMoveSequenceException() {
		super("Incorrect move sequence");
	}
}
