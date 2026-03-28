package engine.game;


import engine.services.world.components.ColliderComponent;

/** Game-specific collider categories. */
public enum GameColliderType implements ColliderComponent.ColliderType {
    PLAYER,
    WALL,
    ENEMY,
    ITEM,
    PROJECTILE
}
