package Sound;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundManager {

    private static final SoundManager INSTANCE = new SoundManager();
    public static SoundManager getInstance() { return INSTANCE; }

    private float sfxVolume   = 0.8f;
    private float musicVolume = 0.7f;

    private Clip bgmClip;

    private SoundManager() {}

    // ── BGM ──────────────────────────────────────────

    public void playBGM(String resourcePath) {
        stopBGM();
        new Thread(() -> {
            try {
                URL url = SoundManager.class.getClassLoader()
                        .getResource(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
                if (url == null) {
                    System.out.println("⚠️ BGM not found: " + resourcePath);
                    return;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                bgmClip = AudioSystem.getClip();
                bgmClip.open(audioIn);
                applyVolume(bgmClip, musicVolume);
                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgmClip.start();
            } catch (Exception e) {
                System.out.println("❌ BGM error: " + e.getMessage());
            }
        }).start();
    }

    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
        bgmClip = null;
    }

    // ── SFX ──────────────────────────────────────────

    public void playSFX(String resourcePath) {
        new Thread(() -> {
            try {
                URL url = SoundManager.class.getClassLoader()
                        .getResource(resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath);
                if (url == null) {
                    System.out.println("⚠️ SFX not found: " + resourcePath);
                    return;
                }
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                applyVolume(clip, sfxVolume);
                clip.start();
            } catch (Exception e) {
                System.out.println("❌ SFX error: " + e.getMessage());
            }
        }).start();
    }

    // ── Volume control ────────────────────────────────

    public void setSfxVolume(float volume) {
        this.sfxVolume = clamp(volume);
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = clamp(volume);
        if (bgmClip != null && bgmClip.isOpen()) applyVolume(bgmClip, musicVolume);
    }

    public float getSfxVolume()   { return sfxVolume; }
    public float getMusicVolume() { return musicVolume; }

    // ── Helper ────────────────────────────────────────

    private void applyVolume(Clip clip, float volume) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float dB = volume == 0f ? -80f : (float)(Math.log10(volume) * 20);
            gain.setValue(Math.max(gain.getMinimum(), Math.min(dB, gain.getMaximum())));
        }
    }

    private float clamp(float v) { return Math.max(0f, Math.min(1f, v)); }
}