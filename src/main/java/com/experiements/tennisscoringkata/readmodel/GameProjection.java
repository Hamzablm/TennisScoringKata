package com.experiements.tennisscoringkata.readmodel;

import com.experiements.tennisscoringkata.event.Event;
import com.experiements.tennisscoringkata.event.PointWon;
import com.experiements.tennisscoringkata.model.Player;

public final class GameProjection {
  // numeric score buckets before deuce
  private static final int[] SCORE_BUCKETS = {0, 15, 30, 40};

  private int indexA = 0;
  private int indexB = 0;

  private boolean deuce = false;
  private Player advantage = null;
  private Player winner = null;

  public void apply(Event e) {
    if (winner != null) return; // already finished , ignore further events
    if (e instanceof PointWon(Player player)) {
      handleBall(player);
    }
  }

  private void handleBall(Player p) {
    if (deuce) {
      if (advantage == null) {
        advantage = p; // first point after deuce gives advantage
      } else if (advantage == p) {
        winner = p; // same player converts advantage → game
      } else {
        advantage = null; // opposite player cancels advantage → back to deuce
      }
      return;
    }

    // Normal scoring phase (before deuce)
    advanceScoreForOnePlayer(p == Player.A);
  }

  private void advanceScoreForOnePlayer(boolean isA) {
    int idxSelf = isA ? indexA : indexB;
    int idxOther = isA ? indexB : indexA;

    if (idxSelf < 3) {          // 0→15→30→40
      idxSelf++;
      if (isA) indexA = idxSelf;
      else indexB = idxSelf;

      // if we just made it 40-40, mark deuce now
      if (idxSelf == 3 && idxOther == 3) {
        deuce = true;
      }
      return;
    }

    // self already at 40
    if (idxOther < 3) {
      winner = isA ? Player.A : Player.B;  // opponent < 40 → win
    }
  }

  public String render() {
    if (winner != null) {
      return "Player " + winner + " wins the game";
    }
    if (advantage != null) {
      return "Advantage Player " + advantage + "\n";
    }
    if (deuce) {
      return "Deuce" + "\n";
    }
    return "Player A : " + SCORE_BUCKETS[indexA] + " / Player B : " + SCORE_BUCKETS[indexB] + "\n";
  }

  public Player winner() {
    return winner;
  }
}
