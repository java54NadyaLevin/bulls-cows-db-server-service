package telran.net.games.service;


import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.*;

import telran.net.games.BullsCowsTestPersistenceUnitInfo;
import telran.net.games.entities.*;
import telran.net.games.exceptions.*;
import telran.net.games.model.MoveData;
import telran.net.games.repo.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BullsCowsServiceTest {
	private static final int N_DIGITS = 4;
	static BullsCowsRepository repository;
	static BullsCowsService bcService;
	static BullsCowsGameRunner bcRunner;
	static {
		HashMap<String, Object> hibernateProperties = new HashMap<>();
		hibernateProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		repository = new BullsCowsRepositoryJpa(new BullsCowsTestPersistenceUnitInfo(), hibernateProperties);
		bcRunner = new BullsCowsGameRunner(N_DIGITS);
		bcService = new BullsCowsServiceImpl(repository, bcRunner);

	}
	static Long gameId;
	static String gamerUsername = "gamer1";
	static String gamerUsername2 = "gamer2";
	static String gamerUsername3 = "gamer3";
	static String gamerUsername4 = "gamer4";

	@Test
	@Order(1)
	void createGameTest() {
		gameId = bcService.createGame();
		Game game = repository.getGame(gameId);
		assertNotNull(game);
		assertNull(game.getDate());
		assertFalse(game.isfinished());
	}

	@Test
	@Order(2)
	void registerGamerTest() {
		bcService.registerGamer(gamerUsername, LocalDate.of(2000, 1, 1));
		Gamer gamer = repository.getGamer(gamerUsername);
		assertNotNull(gamer);
		assertThrowsExactly(GamerAlreadyExistsdException.class,
				() -> repository.createNewGamer(gamerUsername, LocalDate.of(2000, 1, 1)));
	}

	@Test
	@Order(3)
	void gamerJoinGameTest() {
		assertThrowsExactly(GamerNotFoundException.class, () -> bcService.gamerJoinGame(gameId, "gamer2"));
		assertThrowsExactly(GameNotFoundException.class, () -> bcService.gamerJoinGame(22, gamerUsername));
		bcService.gamerJoinGame(gameId, gamerUsername);
		List<String> gamers = repository.getGameGamers(gameId);
		assertEquals(1, gamers.size());
		assertEquals(gamerUsername, gamers.get(0));
	}

	@Test
	@Order(4)

	void startGameTest() {
		Long gameId2 = bcService.createGame();
		assertThrowsExactly(NoGamerInGameException.class, () -> bcService.startGame(gameId2));
		bcService.gamerJoinGame(gameId2, gamerUsername);
		bcService.startGame(gameId2);
		assertTrue(repository.isGameStarted(gameId2));
		assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.startGame(gameId2));
	}

	@Test
	@Order(5)
	void moveProcessingPositiveTest() {
		Long gameId2 = bcService.createGame();
		bcService.gamerJoinGame(gameId2, gamerUsername);
		bcService.startGame(gameId2);
		List<MoveData> moves = bcService.moveProcessing(((BullsCowsServiceImpl) bcService).getSequence(gameId2),
				gameId2, gamerUsername);
		assertEquals(N_DIGITS, moves.getLast().bulls());
		assertTrue(repository.isGameFinished(gameId2));
		assertTrue(repository.isWinner(gameId2, gamerUsername));

	}
	
	@Test
	@Order(6)
	void moveProcessingNegative() {
		Long gameId2 = bcService.createGame();
		bcService.gamerJoinGame(gameId2, gamerUsername);
		assertThrowsExactly(GameNotStartedException.class,
				() -> bcService.moveProcessing(bcRunner.getRandomSequence(), gameId2, gamerUsername));
		bcService.startGame(gameId2);
		assertThrowsExactly(IncorrectMoveSequenceException.class,
				() -> bcService.moveProcessing("12345", gameId2, gamerUsername));
		assertThrowsExactly(IncorrectMoveSequenceException.class,
				() -> bcService.moveProcessing("1233", gameId2, gamerUsername));
		assertThrowsExactly(GameNotFoundException.class, () -> bcService.moveProcessing("1754", 22, gamerUsername));
		assertThrowsExactly(GamerNotFoundException.class, () -> bcService.moveProcessing("1354", gameId, "noGamer"));
	}

	@Test
	@Order(7)
	void getGameGamersTest() {
		assertThrowsExactly(GameNotFoundException.class,
				() -> bcService.getGameGamers(22));
		Long gameId2 = bcService.createGame();
		String[] gamers = {gamerUsername2, gamerUsername3, gamerUsername4};
		List<String> expected = new ArrayList<>();
		for(String e : gamers) {
			bcService.registerGamer(e,  LocalDate.of(2000, 1, 1));
			bcService.gamerJoinGame(gameId2, e);
			expected.add(e);
		}
		assertEquals(expected, bcService.getGameGamers(gameId2));
		
	}
}
