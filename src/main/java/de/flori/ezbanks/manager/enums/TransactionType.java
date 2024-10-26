package de.flori.ezbanks.manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    ADD_MONEY("<green><bold>→<reset>", "§a§l→"),
    REMOVE_MONEY("<red><bold>←<reset>", "§c§l←"),
    TRANSFER_IN("<green><bold>→<reset>", "§a§l→"),
    TRANSFER_OUT("<red><bold>←<reset>", "§c§l←");

    private final String displayName;
    private final String legacyDisplayName;

}
