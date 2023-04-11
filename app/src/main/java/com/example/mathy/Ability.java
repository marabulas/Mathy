package com.example.mathy;

import android.graphics.drawable.Drawable;

public class Ability {
    int abilityCost;
    int abilityLevel;

    double abilityProbability;
    String abilityDescription;

    String abilityName;

    int image;

    public int getImage() {
        return image;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public Ability(String name, int level, String abilityDescription, int abilityCost, double abilityProbability, int image) {
        this.abilityName = name;
        this.abilityCost = abilityCost;
        this.abilityLevel = level;
        this.abilityDescription = abilityDescription;
        this.abilityProbability = abilityProbability;
        this.image = image;
    }

    public double getAbilityProbability() {
        return abilityProbability;
    }

    public void setAbilityProbability(int abilityProbability) {
        this.abilityProbability = abilityProbability;
    }

    public String getAbilityName() {
        return abilityName;
    }

    public void setAbilityName(String abilityName) {
        this.abilityName = abilityName;
    }

    public int getAbilityCost() {
        return abilityCost;
    }

    public void setAbilityCost(int abilityCost) {
        this.abilityCost = abilityCost;
    }

    public int getAbilityLevel() {
        return abilityLevel;
    }

    public void setAbilityLevel(int abilityLevel) {
        this.abilityLevel = abilityLevel;
    }

    public String getAbilityDescription() {
        return abilityDescription;
    }

    public void setAbilityDescription(String abilityDescription) {
        this.abilityDescription = abilityDescription;
    }

    public void runnable() {

    }
}
