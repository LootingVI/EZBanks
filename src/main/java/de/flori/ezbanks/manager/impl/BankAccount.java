package de.flori.ezbanks.manager.impl;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class BankAccount {

    private String bankId;
    private UUID ownerUuid;
    private int pin;

    private double balance = 0;
    private boolean suspended = false;

    private List<Transaction> transactions = Lists.newArrayList();

}
