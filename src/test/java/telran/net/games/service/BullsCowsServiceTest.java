package telran.net.games.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.*;

import telran.net.games.BullsCowsTestPersistenceUnitInfo;
import telran.net.games.entities.*;
import telran.net.games.exceptions.*;
import telran.net.games.repo.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BullsCowsServiceTest {
	private static final int N_DIGITS = 4;
	static BullsCowsRepository repository;
	static BullsCowsService bcService;
	static {
		HashMap<String, Object> hibernateProperties = new HashMap<>();
		hibernateProperties.put("hibernate.hbm2ddl.auto", "create-drop");
		repository = new BullsCowsRepositoryJpa(new BullsCowsTestPersistenceUnitInfo(), hibernateProperties);
		BullsCowsGameRunner bcRunner = new BullsCowsGameRunner(N_DIGITS);
		bcService = new BullsCowsServiceImpl(repository, bcRunner);

	}
	static Long gameId;
	static String gamerUsername = "gamer1";
	static String gamerUsername2 = "gamer2";
	static String gamerUsername3 = "gamer3";

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
		assertNotNull(gamerUsername);
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
	/**
	 * starts game returns list of gamers (user names) exceptions:
	 * GameNotFoundException GameAlreadyStartedException NoGamerInGameException
	 */
	void startGameTest() {
		Long gameId2 = bcService.createGame();
		assertThrowsExactly(NoGamerInGameException.class, () -> bcService.startGame(gameId2));
		bcService.gamerJoinGame(gameId2, gamerUsername);
		bcService.startGame(gameId2);
		assertTrue(repository.isGameStarted(gameId2));
		assertThrowsExactly(GameAlreadyStartedException.class, () -> bcService.startGame(gameId2));
	}

	// TODO Tests
	// access the to be guessed sequence -
	// ((BullsCowsServiceImpl)bcService).getSequence(gameId)
//	
//    @Test
//    @Order(5)
//    void moveProcessingTest() {
//    	
//    }
//    
//    @Test
//    @Order(6)
//    void gameOverTest() {
//    	
//    }
}
