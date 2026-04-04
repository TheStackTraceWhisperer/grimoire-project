package com.grimoire.domain.navigation.component;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PathTest {

    @Test
    void fromListWithTarget() {
        var waypoints = List.of(new Position(100, 100), new Position(200, 200), new Position(300, 300));

        var path = Path.fromList(waypoints, 42, 50L);

        assertThat(path.size()).isEqualTo(3);
        assertThat(path.currentIndex).isZero();
        assertThat(path.targetEntityId).isEqualTo(42);
        assertThat(path.lastCalculationTick).isEqualTo(50L);
    }

    @Test
    void fromListWithoutTarget() {
        var path = Path.fromList(List.of(new Position(1, 2)), 75L);

        assertThat(path.targetEntityId).isEqualTo(-1);
        assertThat(path.lastCalculationTick).isEqualTo(75L);
    }

    @Test
    void implementsComponent() {
        assertThat(Path.fromList(List.of(), 0L)).isInstanceOf(Component.class);
    }

    @Test
    void isEmptyOnEmptyList() {
        assertThat(Path.fromList(List.of(), 0L).isEmpty()).isTrue();
    }

    @Test
    void nullWaypointsNormalisedToEmpty() {
        var path = new Path(null, 0, -1, 0L);
        assertThat(path.isEmpty()).isTrue();
        assertThat(path.waypoints()).isNotNull().isEmpty();
    }

    @Test
    void isEmptyWhenIndexPastEnd() {
        var waypoints = List.of(new Position(1, 1));
        assertThat(new Path(waypoints, 1, -1, 0L).isEmpty()).isTrue();
    }

    @Test
    void getCurrentWaypoint() {
        var path = Path.fromList(List.of(new Position(10, 20), new Position(30, 40)), 0L);

        assertThat(path.getCurrentWaypoint()).isEqualTo(new Position(10, 20));
    }

    @Test
    void getCurrentWaypointOnEmptyPath() {
        assertThat(Path.fromList(List.of(), 0L).getCurrentWaypoint()).isNull();
    }

    @Test
    void advanceToNextWaypoint() {
        var waypoints = List.of(new Position(1, 1), new Position(2, 2), new Position(3, 3));
        var path = Path.fromList(waypoints, 0L);

        path.advanceToNextWaypoint();
        assertThat(path.getCurrentWaypoint()).isEqualTo(new Position(2, 2));
        assertThat(path.remainingWaypoints()).isEqualTo(2);

        path.advanceToNextWaypoint();
        assertThat(path.getCurrentWaypoint()).isEqualTo(new Position(3, 3));

        path.advanceToNextWaypoint();
        assertThat(path.isEmpty()).isTrue();
    }

    @Test
    void remainingWaypoints() {
        var path = Path.fromList(List.of(new Position(1, 1), new Position(2, 2)), 0L);

        assertThat(path.remainingWaypoints()).isEqualTo(2);
        path.advanceToNextWaypoint();
        assertThat(path.remainingWaypoints()).isEqualTo(1);

        assertThat(new Path(null, 0, -1, 0L).remainingWaypoints()).isZero();
    }

    @Test
    void size() {
        var path = Path.fromList(List.of(new Position(1, 1), new Position(2, 2)), 0L);

        assertThat(path.size()).isEqualTo(2);
        path.advanceToNextWaypoint();
        assertThat(path.size()).isEqualTo(2); // size unchanged

        assertThat(new Path(null, 0, -1, 0L).size()).isZero();
    }

    @Test
    void getLastWaypoint() {
        var path = Path.fromList(List.of(new Position(1, 1), new Position(9, 9)), 0L);

        assertThat(path.getLastWaypoint()).isEqualTo(new Position(9, 9));

        assertThat(Path.fromList(List.of(), 0L).getLastWaypoint()).isNull();
        assertThat(new Path(null, 0, -1, 0L).getLastWaypoint()).isNull();
    }
}
