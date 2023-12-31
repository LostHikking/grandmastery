package io.deeplay.grandmastery;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.deeplay.grandmastery.core.UI;
import io.deeplay.grandmastery.domain.ChessType;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.domain.GameMode;
import io.deeplay.grandmastery.domain.GameState;
import io.deeplay.grandmastery.dto.ErrorConnectionBotFarm;
import io.deeplay.grandmastery.dto.ResultGame;
import io.deeplay.grandmastery.dto.SendListBots;
import io.deeplay.grandmastery.dto.StartGameRequest;
import io.deeplay.grandmastery.dto.StartGameResponse;
import io.deeplay.grandmastery.exceptions.QueryException;
import io.deeplay.grandmastery.ui.ConsoleUi;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

public class ClientTest {
  private static final int PORT = 8080;
  private final UI mockUi = mock(UI.class);
  private Client client;
  private ServerSocket server;
  private ListAppender<ILoggingEvent> listAppender;

  private void setupLogCheck() {
    Logger clientLogger = (Logger) LoggerFactory.getLogger(ClientController.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    clientLogger.addAppender(listAppender);
  }

  private void runTestServer() throws IOException {
    server = new ServerSocket(PORT);
  }

  /**
   * Закрывает тестовый сервер.
   *
   * @throws IOException ошибка при закрытии.
   */
  @AfterEach
  public void closeTestServer() throws IOException {
    if (server != null && !server.isClosed()) {
      server.close();
    }
  }

  @Test
  public void successfulConnectionTest() throws Exception {
    setupLogCheck();
    runTestServer();
    client = new Client(mockUi);

    List<ILoggingEvent> logs = listAppender.list;
    Assertions.assertAll(
        () -> assertEquals("Соединение с сервером установлено.", logs.get(0).getMessage()),
        () -> assertFalse(client.reconnect));
  }

  @Test
  public void failedConnectionTest() throws Exception {
    setupLogCheck();
    Thread connectThread =
        new Thread(
            () -> {
              try {
                client = new Client(mockUi);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            });
    connectThread.start();
    Thread.sleep(2000);
    runTestServer();
    connectThread.join();

    List<ILoggingEvent> logs = listAppender.list;
    Assertions.assertAll(
        () -> assertFalse(client.reconnect),
        () -> assertEquals("localhost", client.host),
        () -> assertEquals(8080, client.port),
        () ->
            assertEquals(
                "Сервер недоступен. Попробуем снова через некоторое время...",
                logs.get(0).getMessage()),
        () -> assertEquals("Соединение с сервером установлено.", logs.get(1).getMessage()));
  }

  @Test
  public void reconnectTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    client.reconnect();

    Assertions.assertAll(
        () -> assertTrue(client.reconnect), () -> assertFalse(client.clientController.isClosed()));
  }

  @Test
  public void runBotVsBotTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    ResultGame resultGame = new ResultGame(GameState.WHITE_WIN, List.of(""));
    ClientController mockClientController = mock(ClientController.class);

    when(mockClientController.selectMode()).thenReturn(GameMode.BOT_VS_BOT, GameMode.BOT_VS_BOT);
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC, ChessType.CLASSIC);
    when(mockClientController.selectBot(any())).thenReturn("Bot");
    when(mockClientController.query(any())).thenReturn(resultGame, resultGame);
    when(mockClientController.startNewGame()).thenReturn(true, false);

    client.clientController = mockClientController;
    Assertions.assertAll(
        () -> assertDoesNotThrow(client::run), () -> assertFalse(client.reconnect));
  }

  @Test
  public void runHumanVsBotErrorTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    ClientController mockClientController = mock(ClientController.class);
    StartGameResponse startGameResponse =
        new StartGameResponse("rp____PRnp____PNbp____PBqp____PQkp____PKbp____PBnp____PNrp____PR");

    when(mockClientController.query(any())).thenReturn(startGameResponse);
    when(mockClientController.getUi()).thenReturn(mockUi);
    when(mockClientController.getJsonFromServer())
        .thenReturn("{\"type\":\"ResultGame\",\"gameState\":\"BLACK_WIN\",\"boards\":[\"\"]}");
    when(mockClientController.selectMode()).thenReturn(GameMode.HUMAN_VS_BOT);
    when(mockClientController.selectColor()).thenReturn(Color.WHITE);
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC);
    when(mockClientController.selectBot(any())).thenReturn(null, "Bot");
    when(mockClientController.startNewGame()).thenReturn(false);

    client.clientController = mockClientController;
    Assertions.assertAll(
        () -> assertDoesNotThrow(client::run),
        () -> assertFalse(client.reconnect),
        () -> verify(mockClientController, times(2)).close());
  }

  @Test
  public void errorBotVsBotTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    StartGameRequest startGameRequest = new StartGameRequest();
    ClientController mockClientController = mock(ClientController.class);

    when(mockClientController.selectMode()).thenReturn(GameMode.BOT_VS_BOT);
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC, ChessType.CLASSIC);
    when(mockClientController.selectBot(any())).thenReturn("Bot");
    when(mockClientController.query(any())).thenReturn(startGameRequest);
    when(mockClientController.startNewGame()).thenReturn(false);

    client.clientController = mockClientController;
    Assertions.assertAll(
        () -> assertThrows(IllegalStateException.class, client::run),
        () -> verify(mockClientController, times(1)).close());
  }

  @Test
  public void errorBotVsBotUnavailableBotFarmTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    ClientController mockClientController = mock(ClientController.class);

    when(mockClientController.selectMode()).thenReturn(GameMode.BOT_VS_BOT);
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC);
    when(mockClientController.selectBot(any())).thenReturn(null, "bot", null, "Bot");
    when(mockClientController.query(any()))
        .thenReturn(new ResultGame(GameState.WHITE_WIN, List.of("")));
    when(mockClientController.startNewGame()).thenReturn(false);

    client.clientController = mockClientController;
    Assertions.assertAll(
        () -> assertDoesNotThrow(client::run),
        () -> verify(mockClientController, times(3)).close());
  }

  @Test
  public void selectBotTest() throws IOException {
    ClientController clientController = new ClientController(mockUi);
    ClientDao mockDao = mock(ClientDao.class);
    when(mockDao.query(any())).thenReturn(new SendListBots());
    when(mockUi.selectBot(any(), any())).thenReturn("bot");

    clientController.clientDao = mockDao;
    assertEquals("bot", clientController.selectBot(Color.WHITE));
  }

  @Test
  public void errorSelectBotTest() throws IOException {
    ClientController clientController = new ClientController(mockUi);
    ClientDao mockDao = mock(ClientDao.class);
    when(mockDao.query(any())).thenReturn(new ErrorConnectionBotFarm());

    clientController.clientDao = mockDao;
    assertNull(clientController.selectBot(Color.WHITE));
  }

  @Test
  public void runGameWithHumanTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);

    ClientController mockClientController = mock(ClientController.class);
    StartGameResponse startGameResponse =
        new StartGameResponse("rp____PRnp____PNbp____PBqp____PQkp____PKbp____PBnp____PNrp____PR");

    when(mockClientController.selectMode()).thenReturn(GameMode.HUMAN_VS_HUMAN);
    when(mockClientController.startNewGame()).thenReturn(false);
    when(mockClientController.getUi()).thenReturn(mockUi);
    when(mockClientController.selectColor()).thenReturn(Color.WHITE);
    when(mockClientController.inputPlayerName(Color.WHITE)).thenReturn("Player");
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC);
    when(mockClientController.query(any())).thenReturn(startGameResponse);
    when(mockClientController.getJsonFromServer()).thenReturn("{\"type\":\"WaitMove\"}");
    when(mockClientController.getJsonFromServer())
        .thenAnswer(
            invocation -> {
              Thread.sleep(2000);
              return "{\"type\":\"ResultGame\",\"gameState\":\"BLACK_WIN\",\"boards\":[\"\"]}";
            });

    when(mockUi.inputMove(anyString())).thenReturn("e2e4");
    client.clientController = mockClientController;

    Assertions.assertAll(
        () -> assertDoesNotThrow(client::run),
        () -> assertFalse(client.reconnect),
        () -> assertEquals(mockUi, client.clientController.getUi()),
        () -> assertTrue(client.player.isGameOver(), "Player game over"),
        () -> assertTrue(client.player.getGameHistory().isGameOver(), "History game over"),
        () -> verify(mockClientController, times(1)).close());
  }

  @Test
  public void disconnectServerTest() throws Exception {
    runTestServer();
    client = new Client(mockUi);
    Logger clientLogger = (Logger) LoggerFactory.getLogger(Client.class);
    clientLogger.setLevel(Level.OFF);

    ClientController mockClientController = mock(ClientController.class);
    StartGameResponse startGameResponse =
        new StartGameResponse("rp____PRnp____PNbp____PBqp____PQkp____PKbp____PBnp____PNrp____PR");

    when(mockClientController.selectMode()).thenReturn(GameMode.HUMAN_VS_HUMAN);
    when(mockClientController.startNewGame()).thenReturn(false);
    when(mockClientController.getUi()).thenReturn(mockUi);
    when(mockClientController.selectColor()).thenReturn(Color.WHITE);
    when(mockClientController.inputPlayerName(Color.WHITE)).thenReturn("Player");
    when(mockClientController.selectChessType()).thenReturn(ChessType.CLASSIC);
    when(mockClientController.query(any()))
        .thenThrow(QueryException.class)
        .thenReturn(startGameResponse);
    when(mockClientController.getJsonFromServer()).thenReturn("{\"type\":\"WaitMove\"}");
    when(mockClientController.getJsonFromServer())
        .thenAnswer(
            invocation -> {
              Thread.sleep(2000);
              return "{\"type\":\"ResultGame\",\"gameState\":\"BLACK_WIN\",\"boards\":[\"\"]}";
            });

    when(mockUi.inputMove(anyString())).thenReturn("e2e4");
    client.clientController = mockClientController;

    Assertions.assertAll(
        () -> assertDoesNotThrow(client::run),
        () -> assertFalse(client.reconnect),
        () -> assertEquals(mockUi, client.clientController.getUi()),
        () -> assertTrue(client.player.isGameOver(), "Player game over"),
        () -> assertTrue(client.player.getGameHistory().isGameOver(), "History game over"),
        () -> verify(mockClientController, times(2)).close());
  }

  @Test
  public void createUiTest() {
    assertTrue(Client.createUi("tui") instanceof ConsoleUi);
  }

  @Test
  public void tryCreateUnknownUiTest() {
    assertThrows(IllegalArgumentException.class, () -> Client.createUi("ababa"));
  }
}
