package de.flori.ezbanks.manager.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TransactionType {

    ADD_MONEY("§a§l→"),
    REMOVE_MONEY("§c§l←"),
    TRANSFER_IN("§a§l→"),
    TRANSFER_OUT("§c§l←");

    private final String displayName;

}
