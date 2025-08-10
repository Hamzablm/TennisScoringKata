package com.experiements.tennisscoringkata.readmodel;

import static org.junit.jupiter.api.Assertions.*;

import com.experiements.tennisscoringkata.event.PointWon;
import com.experiements.tennisscoringkata.model.Player;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GameProjectionTest {

  @Test
  @DisplayName("pre-deuce scoring buckets progress 0→15→30→40")
  void buckets_progress() {
    GameProjection gp = new GameProjection();

    gp.apply(new PointWon(Player.A));
    assertEquals("Player A : 15 / Player B : 0\n", gp.render());

    gp.apply(new PointWon(Player.A));
    assertEquals("Player A : 30 / Player B : 0\n", gp.render());

    gp.apply(new PointWon(Player.A));
    assertEquals("Player A : 40 / Player B : 0\n", gp.render());
  }

  @Test
  @DisplayName("winner is set and further events are ignored")
  void winner_then_ignore_rest() {
    GameProjection gp = new GameProjection();
    gp.apply(new PointWon(Player.A));
    gp.apply(new PointWon(Player.A));
    gp.apply(new PointWon(Player.A));
    gp.apply(new PointWon(Player.A)); // A wins

    assertEquals("Player A wins the game", gp.render());
    assertEquals(Player.A, gp.winner());

    // these should be ignored by the projection (idempotent after win)
    gp.apply(new PointWon(Player.B));
    gp.apply(new PointWon(Player.B));

    assertEquals("Player A wins the game", gp.render());
    assertEquals(Player.A, gp.winner());
  }

  @Test
  @DisplayName("deuce then advantage toggling")
  void deuce_and_advantage_flow() {
    GameProjection gp = new GameProjection();

    // reach 40–40 → must say "Deuce"
    gp.apply(new PointWon(Player.A)); // 15–0
    gp.apply(new PointWon(Player.B)); // 15–15
    gp.apply(new PointWon(Player.A)); // 30–15
    gp.apply(new PointWon(Player.B)); // 30–30
    gp.apply(new PointWon(Player.A)); // 40–30
    gp.apply(new PointWon(Player.B)); // Deuce

    assertEquals("Deuce\n", gp.render(), "at 40–40 the render should be 'Deuce'");

    // A takes advantage
    gp.apply(new PointWon(Player.A));
    assertEquals("Advantage Player A\n", gp.render());

    // B cancels → deuce
    gp.apply(new PointWon(Player.B));
    assertEquals("Deuce\n", gp.render());

    // B takes advantage then wins
    gp.apply(new PointWon(Player.B));
    assertEquals("Advantage Player B\n", gp.render());
    gp.apply(new PointWon(Player.B));
    assertEquals("Player B wins the game", gp.render());
    assertEquals(Player.B, gp.winner());
  }
}