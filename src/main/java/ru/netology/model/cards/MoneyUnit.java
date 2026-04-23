package ru.netology.model.cards;

public enum MoneyUnit {
    KOPECKS(0),
    RUBLES(2);

    private final int scale;

    MoneyUnit(int scale) {
        this.scale = scale;
    }

    public int getScale() {
        return scale;
    }
}