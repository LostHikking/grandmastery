package io.deeplay.grandmastery;

import io.deeplay.grandmastery.core.GameHistory;
import io.deeplay.grandmastery.core.Move;
import io.deeplay.grandmastery.core.Position;
import io.deeplay.grandmastery.domain.ChessType;
import io.deeplay.grandmastery.domain.Color;
import io.deeplay.grandmastery.dto.CreateMoveFarmResponse;
import io.deeplay.grandmastery.exceptions.QueryException;
import io.deeplay.grandmastery.service.ConversationService;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class FarmPlayerTest {
  private FarmPlayer farmPlayer;

  @BeforeEach
  void init() throws IOException {
    var in = Mockito.mock(BufferedReader.class);
    Mockito.when(in.readLine())
        .thenReturn(
            ConversationService.serialize(
                new CreateMoveFarmResponse(
                    new Move(Position.fromString("e2"), Position.fromString("e4"), null))));

    var out = Mockito.mock(BufferedWriter.class);
    farmPlayer = new FarmPlayer("farm", Color.WHITE, ChessType.CLASSIC);
    farmPlayer.setIn(in);
    farmPlayer.setOut(out);
    farmPlayer.setGameHistory(new GameHistory());
  }

  @Test
  void answerDrawTest() {
    Assertions.assertFalse(farmPlayer.answerDraw());
  }

  @Test
  void sendTest() {
    Assertions.assertDoesNotThrow(() -> farmPlayer.send("some_json"));
  }

  @Test
  void queryTest() throws QueryException {
    var dto = farmPlayer.query(new CreateMoveFarmResponse(), CreateMoveFarmResponse.class);
    Assertions.assertEquals(
        new CreateMoveFarmResponse(
            new Move(Position.fromString("e2"), Position.fromString("e4"), null)),
        dto);
  }

  @Test
  void createMoveTest() {
    var move = farmPlayer.createMove();
    Assertions.assertEquals(
        new Move(Position.fromString("e2"), Position.fromString("e4"), null), move);
  }
}