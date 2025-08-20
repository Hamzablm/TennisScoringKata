package com.experiements.tennisscoringkata;

import com.experiements.tennisscoringkata.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TennisGameTest {

  /* helper: play a sequence and capture the score after each ball */
  private static List<String> playAndCollect(String sequence) {
    TennisGame game = new TennisGame();
    List<String> out = new ArrayList<>();
    for (char c : sequence.toCharArray()) {
      Player p = switch (c) {
        case 'A' -> Player.A;
        case 'B' -> Player.B;
        default -> throw new IllegalArgumentException("Only A or B allowed – found: " + c);
      };
      game.play(p);
      out.add(game.score());
      if (game.finished()) break;
    }
    return out;
  }

  @Test
  @DisplayName("spec example: ABABAA")
  void example_ababaa() {
    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 15 / Player B : 15\n",
            "Player A : 30 / Player B : 15\n",
            "Player A : 30 / Player B : 30\n",
            "Player A : 40 / Player B : 30\n",
            "Player A wins the game"
    );
    assertEquals(expected, playAndCollect("ABABAA"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"AAAA", "AAAAXXXAAAA"}) // trailing garbage should be ignored after win
  @DisplayName("straight win for A, trailing input ignored")
  void straight_win_A(String seq) {
    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 30 / Player B : 0\n",
            "Player A : 40 / Player B : 0\n",
            "Player A wins the game"
    );
    assertEquals(expected, playAndCollect(seq));
  }

  @Test
  @DisplayName("straight win for B")
  void straight_win_B() {
    var expected = List.of(
            "Player A : 0 / Player B : 15\n",
            "Player A : 0 / Player B : 30\n",
            "Player A : 0 / Player B : 40\n",
            "Player B wins the game"
    );
    assertEquals(expected, playAndCollect("BBBB"));
  }

  @Test
  @DisplayName("reaches deuce at 40–40")
  void reaches_deuce() {
    // by spec: once both hit 40, render should say "Deuce"
    // current implementation prints "Player A : 40 / Player B : 40" first, then "Deuce" on the *next* ball.
    // this test will FAIL with current code, revealing the off-by-one.
    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 15 / Player B : 15\n",
            "Player A : 30 / Player B : 15\n",
            "Player A : 30 / Player B : 30\n",
            "Player A : 40 / Player B : 30\n",
            "Deuce\n"
    );
    assertEquals(expected, playAndCollect("ABABAB"));
  }

  @Test
  @DisplayName("deuce → advantage A → A wins")
  void deuce_advantageA_then_winA() {
    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 15 / Player B : 15\n",
            "Player A : 30 / Player B : 15\n",
            "Player A : 30 / Player B : 30\n",
            "Player A : 40 / Player B : 30\n",
            "Deuce\n",
            "Advantage Player A\n",
            "Player A wins the game"
    );
    assertEquals(expected, playAndCollect("ABABABAA"));
  }

  @Test
  @DisplayName("deuce → advantage A → back to deuce → advantage B → B wins")
  void deuce_advantage_swap_then_B_wins() {
    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 15 / Player B : 15\n",
            "Player A : 30 / Player B : 15\n",
            "Player A : 30 / Player B : 30\n",
            "Player A : 40 / Player B : 30\n",
            "Deuce\n",
            "Advantage Player A\n",
            "Deuce\n",
            "Advantage Player B\n",
            "Player B wins the game"
    );
    assertEquals(expected, playAndCollect("ABABABABBB"));
  }

  @Test
  @DisplayName("integration: main prints exactly the expected lines for ABABAA")
  void main_prints_example() {
    String[] args = {"ABABAA"};
    PrintStream orig = System.out;
    var baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {
      TennisGame.main(args);
    } finally {
      System.setOut(orig);
    }

    var output = baos.toString(StandardCharsets.UTF_8);

    var expected = List.of(
            "Player A : 15 / Player B : 0\n",
            "Player A : 15 / Player B : 15\n",
            "Player A : 30 / Player B : 15\n",
            "Player A : 30 / Player B : 30\n",
            "Player A : 40 / Player B : 30\n",
            "Player A wins the game\n"
    );
    assertEquals(String.join("", expected), output);
  }

  @Test
  @DisplayName("main rejects any character outside {A,B}")
  void main_rejects_invalid_input() {
    IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> TennisGame.main(new String[]{"ABX"}));
    assertTrue(ex.getMessage().contains("Only A or B allowed"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "AAAA",           // exact win
          "AAAABBB",        // valid trailing A/B
          "AAAAXXX",        // invalid trailing chars – should be ignored because loop breaks
          "AAAAxxxx",       // mixed-case trailing noise
          "AAAA+++BBB"      // more trailing noise
  })
  @DisplayName("main(): ignores trailing chars after A wins (via finished())")
  void main_ignores_trailing_after_A_win(String seq) {
    String[] args = {seq};
    PrintStream orig = System.out;
    var baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {
      TennisGame.main(args);
    } finally {
      System.setOut(orig);
    }

    var output = baos.toString(StandardCharsets.UTF_8);

    var expected = String.join("",
            "Player A : 15 / Player B : 0\n",
            "Player A : 30 / Player B : 0\n",
            "Player A : 40 / Player B : 0\n",
            "Player A wins the game\n"
    );
    assertEquals(expected, output, "main should stop processing once finished() returns true");
  }

  @ParameterizedTest
  @ValueSource(strings = {
          "BBBB",           // exact win
          "BBBBABA",        // valid trailing A/B
          "BBBB###",        // invalid trailing chars , should be ignored
          "bbbbXXXX"        // mixed-case + noise
  })
  @DisplayName("main(): ignores trailing chars after B wins (via finished())")
  void main_ignores_trailing_after_B_win(String seq) {
    String[] args = {seq};
    PrintStream orig = System.out;
    var baos = new ByteArrayOutputStream();
    System.setOut(new PrintStream(baos));

    try {
      TennisGame.main(args);
    } finally {
      System.setOut(orig);
    }

    var output = baos.toString(StandardCharsets.UTF_8);

    var expected = String.join("",
            "Player A : 0 / Player B : 15\n",
            "Player A : 0 / Player B : 30\n",
            "Player A : 0 / Player B : 40\n",
            "Player B wins the game\n"
    );
    assertEquals(expected, output, "main should stop processing once finished() returns true");
  }
}
