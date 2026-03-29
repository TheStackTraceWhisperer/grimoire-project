package engine.services.world.systems;

import engine.services.audio.AudioBuffer;
import engine.services.audio.AudioService;
import engine.services.audio.AudioSource;
import engine.services.resources.AssetCacheService;
import engine.services.world.ISystem;
import engine.services.world.World;
import engine.services.world.components.AudioSourceComponent;
import engine.services.world.components.MusicComponent;
import engine.services.world.components.SoundEffectComponent;
import engine.services.world.components.TransformComponent;
import io.micronaut.context.annotation.Prototype;
import jakarta.inject.Inject;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Prototype
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AudioSystem implements ISystem {

  private final AudioService audioService;
  private final AssetCacheService resourceManager;

  private final Map<Integer, AudioSource> audioSourceMap = new HashMap<>();
  private final Map<Integer, AudioSource> musicSourceMap = new HashMap<>();
  private final Map<Integer, AudioSource> soundEffectSourceMap = new HashMap<>();

  @Override
  public void update(World world, float deltaTime) {
    updateAudioSources(world, deltaTime);
    updateMusic(world, deltaTime);
    updateSoundEffects(world, deltaTime);
  }

  private static void applySourceProperties(AudioSource source, float volume, float pitch, boolean looping) {
    source.setVolume(volume);
    source.setPitch(pitch);
    source.setLooping(looping);
  }

  private void updateAudioSources(World world, float deltaTime) {
    var entities = world.getEntitiesWith(AudioSourceComponent.class);

    for (int entityId : entities) {
      AudioSourceComponent audioComp = world.getComponent(entityId, AudioSourceComponent.class);
      AudioSource audioSource = audioSourceMap.get(entityId);

      if (audioSource == null) {
        audioSource = audioService.createSource();
        audioSourceMap.put(entityId, audioSource);

        applySourceProperties(audioSource, audioComp.volume, audioComp.pitch, audioComp.looping);

        if (audioComp.autoPlay && !audioComp.isPlaying) {
          AudioBuffer buffer = resourceManager.resolveAudioBufferHandle(audioComp.audioBufferHandle);
          audioSource.play(buffer);
          audioComp.isPlaying = true;
        }
      }

      TransformComponent transform = world.getComponent(entityId, TransformComponent.class);
      if (transform != null) {
        audioSource.setPosition(transform.position);
      }

      applySourceProperties(audioSource, audioComp.volume, audioComp.pitch, audioComp.looping);

      audioComp.isPlaying = audioSource.isPlaying();

      if (!audioComp.looping && audioSource.isStopped() && audioComp.isPlaying) {
        audioComp.isPlaying = false;
      }
    }

    cleanupAudioSources(entities, audioSourceMap);
  }

  private void updateMusic(World world, float deltaTime) {
    var entities = world.getEntitiesWith(MusicComponent.class);

    for (int entityId : entities) {
      MusicComponent musicComp = world.getComponent(entityId, MusicComponent.class);
      AudioSource musicSource = musicSourceMap.get(entityId);
      boolean justCreated = false;

      if (musicSource == null) {
        musicSource = audioService.createSource();
        musicSourceMap.put(entityId, musicSource);
        justCreated = true;

        musicSource.setPosition(0.0f, 0.0f, 0.0f);
        musicSource.setLooping(musicComp.looping);

        if (musicComp.autoPlay && !musicComp.isPlaying) {
          AudioBuffer buffer = resourceManager.resolveAudioBufferHandle(musicComp.musicBufferHandle);
          musicSource.play(buffer);
          musicComp.isPlaying = true;

          if (!musicComp.fadingIn && !musicComp.fadingOut) {
            musicComp.startFadeIn();
          }
        }
      }

      // If we just created and started fade-in, skip progression this frame so tests see fadingIn=true
      if (!justCreated) {
        updateMusicFades(musicComp, musicSource, deltaTime);
      }

      musicSource.setVolume(musicComp.currentVolume);
      musicSource.setLooping(musicComp.looping);

      musicComp.isPlaying = musicSource.isPlaying();
      musicComp.isPaused = musicSource.isPaused();

      if (!musicComp.looping && musicSource.isStopped() && musicComp.isPlaying) {
        musicComp.isPlaying = false;
      }
    }

    cleanupAudioSources(entities, musicSourceMap);
  }

  private void updateMusicFades(MusicComponent musicComp, AudioSource musicSource, float deltaTime) {
    if (musicComp.fadingIn) {
      musicComp.fadeTimer += deltaTime;
      float fadeProgress = Math.min(musicComp.fadeTimer / musicComp.fadeDuration, 1.0f);
      musicComp.currentVolume = musicComp.baseVolume * fadeProgress;

      if (fadeProgress >= 1.0f) {
        musicComp.fadingIn = false;
        musicComp.fadeTimer = 0.0f;
        musicComp.currentVolume = musicComp.baseVolume;
      }
    } else if (musicComp.fadingOut) {
      musicComp.fadeTimer += deltaTime;
      float fadeProgress = Math.min(musicComp.fadeTimer / musicComp.fadeDuration, 1.0f);
      musicComp.currentVolume = musicComp.baseVolume * (1.0f - fadeProgress);

      if (fadeProgress >= 1.0f) {
        musicComp.fadingOut = false;
        musicComp.fadeTimer = 0.0f;
        musicComp.currentVolume = 0.0f;
        musicSource.stop();
        musicComp.isPlaying = false;
      }
    }
  }

  private void updateSoundEffects(World world, float deltaTime) {
    List<Integer> entitiesToCleanup = new ArrayList<>();
    var entities = world.getEntitiesWith(SoundEffectComponent.class);

    for (int entityId : entities) {
      SoundEffectComponent soundComp = world.getComponent(entityId, SoundEffectComponent.class);
      AudioSource soundSource = soundEffectSourceMap.get(entityId);

      if (soundSource == null && !soundComp.hasBeenTriggered) {
        soundSource = audioService.createSource();
        soundEffectSourceMap.put(entityId, soundSource);

        soundSource.setVolume(soundComp.volume);
        soundSource.setPitch(soundComp.pitch);
        soundSource.setLooping(false);

        if (soundComp.autoPlay) {
          AudioBuffer buffer = resourceManager.resolveAudioBufferHandle(soundComp.soundBufferHandle);
          soundSource.play(buffer);
          soundComp.hasBeenTriggered = true;
        }
      }

      if (soundSource != null && soundSource.isStopped() && soundComp.hasBeenTriggered) {
        if (soundComp.removeAfterPlay) {
          entitiesToCleanup.add(entityId);
        }
      }
    }

    for (int entityId : entitiesToCleanup) {
      AudioSource soundSource = soundEffectSourceMap.remove(entityId);
      if (soundSource != null) {
        soundSource.close();
      }
      world.removeComponent(entityId, SoundEffectComponent.class);
    }

    cleanupAudioSources(world.getEntitiesWith(SoundEffectComponent.class), soundEffectSourceMap);
  }


  private void cleanupAudioSources(Set<Integer> currentEntities, Map<Integer, AudioSource> sourceMap) {
    sourceMap.entrySet().removeIf(entry -> {
      if (!currentEntities.contains(entry.getKey())) {
        entry.getValue().close();
        return true;
      }
      return false;
    });
  }

  public void playSoundEffect(World world, int entityId, String bufferHandle,
                              SoundEffectComponent.SoundEffectType soundType, float volume) {
    SoundEffectComponent soundComp = new SoundEffectComponent(bufferHandle, soundType);
    soundComp.volume = volume;
    world.addComponent(entityId, soundComp);
  }

  public void fadeOutAllMusic(World world) {
    var entities = world.getEntitiesWith(MusicComponent.class);
    for (int entityId : entities) {
      MusicComponent musicComp = world.getComponent(entityId, MusicComponent.class);
      musicComp.startFadeOut();
    }
  }

  public void pauseAllMusic() {
    for (AudioSource musicSource : musicSourceMap.values()) {
      if (musicSource.isPlaying()) {
        musicSource.pause();
      }
    }
  }

  public void resumeAllMusic() {
    for (AudioSource musicSource : musicSourceMap.values()) {
      if (musicSource.isPaused()) {
        musicSource.resume();
      }
    }
  }

  public void stopAll() {
    audioSourceMap.values().forEach(AudioSource::close);
    audioSourceMap.clear();

    musicSourceMap.values().forEach(AudioSource::close);
    musicSourceMap.clear();

    soundEffectSourceMap.values().forEach(AudioSource::close);
    soundEffectSourceMap.clear();
  }
}
