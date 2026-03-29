package engine.services.audio;


import engine.services.world.components.SoundEffectComponent;

/** Game-specific sound effect categories. */
public enum GameSoundEffectType implements SoundEffectComponent.SoundEffectType {
  // UI
  UI_BUTTON_CLICK,
  UI_BUTTON_HOVER,
  UI_MENU_OPEN,
  UI_MENU_CLOSE,
  UI_ERROR,

  // Player
  PLAYER_JUMP,
  PLAYER_LAND,
  PLAYER_FOOTSTEP,
  PLAYER_DAMAGE,
  PLAYER_HEAL,

  // Combat
  WEAPON_SWING,
  WEAPON_HIT,
  PROJECTILE_FIRE,
  PROJECTILE_HIT,

  // Environment
  ITEM_PICKUP,
  DOOR_OPEN,
  DOOR_CLOSE,
  SWITCH_ACTIVATE,

  // Enemy
  ENEMY_DAMAGE,
  ENEMY_DEATH,
  ENEMY_ATTACK
}
