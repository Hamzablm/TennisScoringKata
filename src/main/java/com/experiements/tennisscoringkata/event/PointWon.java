package com.experiements.tennisscoringkata.event;

import com.experiements.tennisscoringkata.model.Player;

public record PointWon(Player player) implements Event {
}
