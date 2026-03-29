package com.grimoire.application.core.ecs;

import com.grimoire.domain.core.component.Component;
import com.grimoire.domain.core.component.Position;
import com.grimoire.domain.core.component.Stats;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PrefabTest {

    @Test
    void nameIsPreserved() {
        var prefab = new Prefab("goblin");

        assertThat(prefab.getName()).isEqualTo("goblin");
    }

    @Test
    void addComponentIsFluent() {
        var prefab = new Prefab("test")
                .addComponent(new Position(1, 2))
                .addComponent(new Stats(10, 10, 5, 5));

        assertThat(prefab.getComponentTemplates()).hasSize(2);
    }

    @Test
    void getComponentTemplatesReturnsDefensiveCopy() {
        var prefab = new Prefab("test")
                .addComponent(new Position(1, 2));

        List<Component> templates = prefab.getComponentTemplates();
        templates.clear();

        assertThat(prefab.getComponentTemplates()).hasSize(1);
    }

    @Test
    void emptyPrefabHasNoTemplates() {
        var prefab = new Prefab("empty");

        assertThat(prefab.getComponentTemplates()).isEmpty();
    }

    @Test
    void componentTemplatesPreserveOrder() {
        var pos = new Position(1, 2);
        var stats = new Stats(10, 10, 5, 5);

        var prefab = new Prefab("ordered")
                .addComponent(pos)
                .addComponent(stats);

        assertThat(prefab.getComponentTemplates()).containsExactly(pos, stats);
    }
}
