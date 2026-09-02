import os
import wave
import math
import struct
import random

RAW_DIR = "app/src/main/res/raw"
os.makedirs(RAW_DIR, exist_ok=True)
SAMPLE_RATE = 22050

def write_wav(filename, samples, sample_rate=SAMPLE_RATE):
    filepath = os.path.join(RAW_DIR, filename + ".wav")
    with wave.open(filepath, "w") as wav_file:
        wav_file.setnchannels(1) # Mono
        wav_file.setsampwidth(2) # 16-bit
        wav_file.setframerate(sample_rate)
        
        # Clip and pack
        packed_data = bytearray()
        for s in samples:
            val = max(-1.0, min(1.0, s))
            int_val = int(val * 32767.0)
            packed_data.extend(struct.pack("<h", int_val))
        wav_file.writeframes(packed_data)
    print(f"Generated {filename}.wav ({len(samples)/sample_rate:.2f}s)")

def make_tone(freq, duration_s, decay=True, wave_type="sine", attack_s=0.01):
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        if wave_type == "sine":
            val = math.sin(2.0 * math.pi * freq * t)
        elif wave_type == "square":
            val = 1.0 if math.sin(2.0 * math.pi * freq * t) > 0 else -1.0
        elif wave_type == "saw":
            val = 2.0 * (t * freq - math.floor(0.5 + t * freq))
        elif wave_type == "noise":
            val = random.uniform(-1.0, 1.0)
        else:
            val = math.sin(2.0 * math.pi * freq * t)
            
        # Envelope
        env = 1.0
        if t < attack_s:
            env = t / attack_s
        elif decay:
            progress = (t - attack_s) / max(0.001, (duration_s - attack_s))
            env = math.exp(-progress * 4.0)
        samples.append(val * env * 0.7)
    return samples

def make_sweep(freq_start, freq_end, duration_s, decay=True):
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = []
    phase = 0.0
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        progress = t / duration_s
        cur_freq = freq_start + (freq_end - freq_start) * progress
        phase += 2.0 * math.pi * cur_freq / SAMPLE_RATE
        val = math.sin(phase)
        
        env = math.exp(-progress * 3.5) if decay else 1.0
        samples.append(val * env * 0.7)
    return samples

def make_chord(freqs, duration_s, decay=True):
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = [0.0] * num_samples
    for f in freqs:
        tone = make_tone(f, duration_s, decay=decay, attack_s=0.02)
        for i in range(num_samples):
            samples[i] += tone[i] / len(freqs)
    return samples

def make_gunshot(duration_s=0.25, snap_freq=120, low_boost=True):
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        progress = t / duration_s
        # Initial transient crack
        noise = random.uniform(-1.0, 1.0)
        thud = math.sin(2.0 * math.pi * snap_freq * (1.0 - progress) * t) if low_boost else 0.0
        val = noise * 0.7 + thud * 0.6
        env = math.exp(-progress * 8.0)
        samples.append(val * env * 0.8)
    return samples

def make_ambient_loop(duration_s=4.0, base_freq=55.0, wind_intensity=0.4):
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = []
    for i in range(num_samples):
        t = i / SAMPLE_RATE
        # Low drone
        drone = math.sin(2.0 * math.pi * base_freq * t) * 0.3 + math.sin(2.0 * math.pi * (base_freq * 1.5) * t) * 0.15
        # Soft filtered noise
        noise = random.uniform(-1.0, 1.0) * wind_intensity * (0.5 + 0.5 * math.sin(2.0 * math.pi * 0.25 * t))
        val = (drone + noise) * 0.5
        # Smooth loop boundaries (fade in/out slightly at ends)
        fade_samples = int(SAMPLE_RATE * 0.2)
        env = 1.0
        if i < fade_samples:
            env = i / fade_samples
        elif i > num_samples - fade_samples:
            env = (num_samples - i) / fade_samples
        samples.append(val * env)
    return samples

def make_music_loop(duration_s=6.0, chord_sequence=None):
    if chord_sequence is None:
        chord_sequence = [[220, 261.6, 329.6], [196, 246.9, 293.7]]
    num_samples = int(SAMPLE_RATE * duration_s)
    samples = [0.0] * num_samples
    sub_dur = duration_s / len(chord_sequence)
    for c_idx, chord in enumerate(chord_sequence):
        start_idx = int(c_idx * sub_dur * SAMPLE_RATE)
        end_idx = min(num_samples, int((c_idx + 1) * sub_dur * SAMPLE_RATE))
        chord_samples = make_chord(chord, sub_dur, decay=False)
        for i in range(end_idx - start_idx):
            if start_idx + i < num_samples and i < len(chord_samples):
                # Gentle bell envelope
                t_sub = i / SAMPLE_RATE
                env = 0.5 + 0.5 * math.sin(math.pi * (i / (end_idx - start_idx)))
                samples[start_idx + i] += chord_samples[i] * env * 0.6
    return samples

def main():
    print("Generating audio assets for Frontier Settlement...")
    
    # 1. UI Sounds
    write_wav("sfx_ui_click", make_tone(800, 0.04, wave_type="sine", attack_s=0.005))
    write_wav("sfx_ui_confirm", make_sweep(520, 784, 0.12))
    write_wav("sfx_ui_cancel", make_sweep(650, 400, 0.10))
    write_wav("sfx_ui_tab", make_tone(600, 0.03, attack_s=0.003))
    write_wav("sfx_ui_toggle", make_tone(950, 0.025, wave_type="square", attack_s=0.002))
    write_wav("sfx_ui_error", make_tone(150, 0.18, wave_type="saw", attack_s=0.01))
    write_wav("sfx_ui_warning", make_sweep(440, 330, 0.22))

    # 2. Settlement & Workshop
    write_wav("sfx_resource_gain", make_sweep(440, 880, 0.18))
    write_wav("sfx_daily_report", make_chord([523.25, 659.25], 0.35))
    write_wav("sfx_new_day", make_chord([440, 554.37, 659.25, 880], 0.7))
    write_wav("sfx_building_construct", make_sweep(120, 60, 0.3))
    write_wav("sfx_building_upgrade", make_chord([392, 493.88, 587.33], 0.45))
    write_wav("sfx_craft", make_tone(480, 0.15, wave_type="square"))
    write_wav("sfx_repair", make_tone(350, 0.15, wave_type="saw"))
    write_wav("sfx_research_complete", make_chord([587.33, 739.99, 880], 0.5))

    # 3. Trade, Quests, Reputation
    write_wav("sfx_trade_buy", make_sweep(700, 1100, 0.14))
    write_wav("sfx_trade_sell", make_sweep(1100, 800, 0.14))
    write_wav("sfx_reputation_up", make_chord([440, 554.37, 659.25], 0.4))
    write_wav("sfx_quest_accept", make_chord([330, 493.88], 0.25))
    write_wav("sfx_quest_objective", make_chord([493.88, 659.25], 0.3))
    write_wav("sfx_quest_complete", make_chord([440, 554.37, 659.25, 880], 0.6))
    write_wav("sfx_quest_failed", make_sweep(350, 180, 0.4))

    # 4. Map & Travel
    write_wav("sfx_location_discovered", make_chord([330, 392, 493.88, 659.25], 0.5))
    write_wav("sfx_travel_step", make_tone(90, 0.08, wave_type="noise"))
    write_wav("sfx_vehicle_engine", make_tone(65, 0.35, wave_type="saw"))

    # 5. Events & Loot
    write_wav("sfx_event_reveal", make_chord([293.66, 369.99, 440], 0.4))
    write_wav("sfx_event_positive", make_chord([440, 554.37, 659.25], 0.45))
    write_wav("sfx_event_negative", make_sweep(300, 150, 0.35))
    write_wav("sfx_loot_reveal", make_chord([392, 493.88, 587.33], 0.35))
    write_wav("sfx_loot_pick", make_tone(650, 0.06))
    write_wav("sfx_loot_rare", make_chord([523.25, 659.25, 783.99, 1046.5], 0.65))
    write_wav("sfx_loot_take_all", make_chord([440, 659.25], 0.25))

    # 6. Combat SFX & Variants
    write_wav("sfx_combat_melee_01", make_sweep(300, 80, 0.14))
    write_wav("sfx_combat_melee_02", make_sweep(350, 90, 0.13))
    write_wav("sfx_combat_melee_03", make_sweep(280, 70, 0.15))
    write_wav("sfx_combat_pistol_01", make_gunshot(0.18, 140))
    write_wav("sfx_combat_pistol_02", make_gunshot(0.19, 150))
    write_wav("sfx_combat_rifle_01", make_gunshot(0.24, 110))
    write_wav("sfx_combat_rifle_02", make_gunshot(0.26, 115))
    write_wav("sfx_combat_shotgun", make_gunshot(0.35, 80))
    write_wav("sfx_combat_heavy", make_gunshot(0.45, 55))
    write_wav("sfx_combat_hit_01", make_tone(180, 0.12, wave_type="saw"))
    write_wav("sfx_combat_hit_02", make_tone(160, 0.13, wave_type="saw"))
    write_wav("sfx_combat_hit_03", make_tone(200, 0.11, wave_type="saw"))
    write_wav("sfx_combat_miss", make_sweep(600, 200, 0.12))
    write_wav("sfx_combat_block", make_tone(900, 0.08, wave_type="square"))
    write_wav("sfx_combat_heal", make_sweep(400, 800, 0.3))
    write_wav("sfx_combat_buff", make_sweep(300, 600, 0.25))
    write_wav("sfx_combat_debuff", make_sweep(600, 300, 0.25))
    write_wav("sfx_combat_status_expire", make_tone(450, 0.15))
    write_wav("sfx_combat_turn_player", make_tone(550, 0.08))
    write_wav("sfx_combat_turn_enemy", make_tone(250, 0.08))
    write_wav("sfx_combat_victory", make_chord([392, 493.88, 587.33, 783.99], 0.75))
    write_wav("sfx_combat_defeat", make_chord([220, 261.63, 311.13], 0.75))

    # 7. Ambient Loops
    write_wav("amb_settlement_day", make_ambient_loop(3.0, base_freq=65.0, wind_intensity=0.3))
    write_wav("amb_settlement_night", make_ambient_loop(3.0, base_freq=45.0, wind_intensity=0.2))
    write_wav("amb_ruins", make_ambient_loop(3.0, base_freq=50.0, wind_intensity=0.5))
    write_wav("amb_industrial", make_ambient_loop(3.0, base_freq=75.0, wind_intensity=0.35))
    write_wav("amb_forest", make_ambient_loop(3.0, base_freq=55.0, wind_intensity=0.4))
    write_wav("amb_road", make_ambient_loop(3.0, base_freq=60.0, wind_intensity=0.45))
    write_wav("amb_storm", make_ambient_loop(3.0, base_freq=40.0, wind_intensity=0.85))
    write_wav("amb_fallback", make_ambient_loop(2.0, base_freq=50.0, wind_intensity=0.2))

    # 8. Music Tracks
    write_wav("music_settlement_01", make_music_loop(4.0, [[220, 277.18, 329.63], [196, 246.94, 293.66]]))
    write_wav("music_world_map_01", make_music_loop(4.0, [[164.81, 220, 261.63], [146.83, 196, 246.94]]))
    write_wav("music_exploration_01", make_music_loop(4.0, [[130.81, 164.81, 196], [110, 146.83, 174.61]]))
    write_wav("music_combat_01", make_music_loop(3.0, [[110, 138.59, 164.81], [98, 123.47, 146.83]]))
    write_wav("music_victory_01", make_music_loop(3.0, [[261.63, 329.63, 392, 523.25]]))
    write_wav("music_defeat_01", make_music_loop(3.0, [[174.61, 207.65, 261.63]]))
    write_wav("music_main_menu_01", make_music_loop(4.0, [[146.83, 174.61, 220], [130.81, 164.81, 196]]))
    write_wav("music_fallback", make_music_loop(2.0, [[220, 277.18, 329.63]]))
    write_wav("sfx_fallback", make_tone(440, 0.1))

    print("All audio assets generated successfully in", RAW_DIR)

if __name__ == "__main__":
    main()
