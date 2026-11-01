package br.com.filastock.data

val MATERIALS = listOf("PLA", "PLA+", "PLA Silk", "PETG", "ABS", "ASA", "TPU", "PA", "PC")

data class ColorPreset(val name: String, val argb: Long)

val COLOR_PRESETS = listOf(
    ColorPreset("Preto", 0xFF1B1B1B),
    ColorPreset("Branco", 0xFFF5F5F5),
    ColorPreset("Cinza", 0xFF9E9E9E),
    ColorPreset("Vermelho", 0xFFE53935),
    ColorPreset("Laranja", 0xFFFB8C00),
    ColorPreset("Amarelo", 0xFFFDD835),
    ColorPreset("Verde", 0xFF43A047),
    ColorPreset("Azul", 0xFF1E88E5),
    ColorPreset("Roxo", 0xFF8E24AA),
    ColorPreset("Rosa", 0xFFEC407A),
    ColorPreset("Marrom", 0xFF6D4C41),
    ColorPreset("Transparente", 0xFFD6EEF2),
)
