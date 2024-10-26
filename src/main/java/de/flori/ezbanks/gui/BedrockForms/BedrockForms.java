package de.flori.ezbanks.gui.BedrockForms;

import de.flori.ezbanks.EZBanks;
import de.flori.ezbanks.manager.enums.TransactionType;
import de.flori.ezbanks.manager.impl.BankAccount;
import de.flori.ezbanks.utils.MessageUtils;
import de.flori.ezbanks.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.geysermc.cumulus.form.CustomForm;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.floodgate.api.player.FloodgatePlayer;

import java.util.Objects;


public class BedrockForms {

        public static void sendLoginForm(BankAccount account, FloodgatePlayer player){

            final int pin = account.getPin();
            player.sendForm(
                    CustomForm.builder()
                            .title("§aLogin to your Account")
                            .label("§aLogin to the Bank account with the id: §6" + account.getBankId())
                            .input("Code", "1234")
                            .validResultHandler(result -> {
                                if (Objects.equals(result.next(), String.valueOf(pin))) {
                                    sendBankAccountForm(account, player);
                                }else{
                                    sendErrorForm("§cThe PIN you entered is incorrect.", player);
                                }
                            }));

        }

        public static void sendBankAccountForm (BankAccount account, FloodgatePlayer player){
            player.sendForm(
                    SimpleForm.builder()
                            .title("§6Bank")
                            .content(
                                    "§bOwner: §a" + Bukkit.getOfflinePlayer(account.getOwnerUuid()).getName() + "\n" +
                                            "§bID: §6" + account.getBankId() + "\n" +
                                            "§bBalance: §6" + account.getBalance() + EZBanks.getInstance().getConfigManager().getSymbol()
                            )
                            .button("§aBank Transactions")
                            .button("§eDeposit")
                            .button("§bWithdraw")
                            .button("§6Transfer")
                            .validResultHandler(result -> {

                                if (result.clickedButtonId() == 0) {
                                    sendTransactionForm(account, player);
                                }
                                if (result.clickedButtonId() == 1) {
                                    sendDeposidForm(account, player);
                                }
                                if (result.clickedButtonId() == 2) {
                                    sendWithdrawForm(account, player);
                                }
                                if (result.clickedButtonId() == 3) {
                                    sendTransferForm(account, player);
                                }

                            })
            );
        }

        public static void sendTransactionForm(BankAccount account, FloodgatePlayer player){

            final StringBuilder builder = new StringBuilder();
            account.getTransactions().reversed().forEach(transaction -> {
                builder.append(transaction.getType().getLegacyDisplayName()).append(" §6").append(transaction.getAmount()).append(EZBanks.getInstance().getConfigManager().getSymbol()).append("§b ").append(Bukkit.getOfflinePlayer(transaction.getPlayer()).getName()).append(" §7").append(Utils.DATE_AND_TIME_FORMAT.format(transaction.getTimestamp())).append('\n');
            });

            player.sendForm(
                    SimpleForm.builder()
                            .title("§6Transaction Log")
                            .content(builder.toString())
                            .button("§cBack")
                            .validResultHandler(result -> {
                                if (result.clickedButtonId() == 0) {
                                    sendBankAccountForm(account, player);
                                }
                            })
            );


        }

        public static void sendDeposidForm(BankAccount account, FloodgatePlayer player){

            player.sendForm(
                    CustomForm.builder()
                            .title("§6Deposit")
                            .label("§aDeposit a amount of Money to your bank account")
                            .input("Amount", "100")
                            .validResultHandler(result -> {

                                final String amount = result.next();

                                assert amount != null;
                                if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                    sendErrorForm("§cPlease enter a correct amount!", player);
                                    return;
                                }
                                final int amountInt = Integer.parseInt(amount);
                                final double balance = EZBanks.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getJavaUniqueId()));

                                if (amountInt < 0.1) {
                                    sendErrorForm("§cPlease enter a correct amount!", player);
                                    return;
                                } else {
                                    if (amountInt > balance) {
                                        sendErrorForm("§cYou don't have enough money in your inventory!", player);
                                        return;
                                    }
                                }

                                EZBanks.getInstance().getBankManager().addBalance(account, amountInt);
                                EZBanks.getInstance().getEconomy().withdrawPlayer(Bukkit.getOfflinePlayer(player.getJavaUniqueId()), amountInt);

                                sendSuccresForm("§aYou have successfully deposited §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol(), player, account);

                                EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.ADD_MONEY, amountInt, player.getJavaUniqueId());


                            })
            );
        }

    public static void sendWithdrawForm(BankAccount account, FloodgatePlayer player){

        player.sendForm(
                CustomForm.builder()
                        .title("§6Withdraw")
                        .label("§aWithdraw a amount of Money from your bank account")
                        .input("Amount", "100")
                        .validResultHandler(result -> {

                            final String amount = result.next();

                            assert amount != null;
                            if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                sendErrorForm("§cPlease enter a correct amount!", player);
                                return;
                            }
                            final int amountInt = Integer.parseInt(amount);
                            final double balance = EZBanks.getInstance().getEconomy().getBalance(Bukkit.getOfflinePlayer(player.getJavaUniqueId()));

                            if (amountInt < 0.1) {
                                sendErrorForm("§cPlease enter a correct amount!", player);
                                return;
                            } else {
                                if (amountInt > balance) {
                                    sendErrorForm("§cYou don't have enough money in your bank account!", player);
                                    return;
                                }
                            }

                            EZBanks.getInstance().getBankManager().removeBalance(account, amountInt);
                            EZBanks.getInstance().getEconomy().depositPlayer(Bukkit.getOfflinePlayer(player.getJavaUniqueId()), amountInt);

                            sendSuccresForm("§aYou have successfully withdrawn §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol(), player, account);

                            EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.REMOVE_MONEY, amountInt, player.getJavaUniqueId());


                        })
        );
    }

    public static void sendTransferForm(BankAccount account, FloodgatePlayer player){

        player.sendForm(
                CustomForm.builder()
                        .title("§6Transfer-ID")
                        .label("§aEnter the bankID of the recipient")
                        .input("ID", "7dsdf256ds")
                        .validResultHandler(result -> {

                            final String targetID = result.next();

                            assert targetID != null;
                            if (targetID.isEmpty() ) {
                                sendErrorForm("§cPlease enter a correct bank id!", player);
                                return;
                            }

                            final BankAccount targetAccount = EZBanks.getInstance().getBankManager().getBankAccount(targetID);

                            if(targetAccount == null){
                                sendErrorForm("§cBank account not found!", player);
                                return;
                            }

                            if(Objects.equals(targetAccount.getBankId(), EZBanks.getInstance().getBankManager().getBankAccount(player.getJavaUniqueId()).getBankId())){
                                sendErrorForm("§cYou cant send money to your own account!", player);
                                return;
                            }

                            player.sendForm(
                                    CustomForm.builder()
                                            .title("§6Transfer-Amount")
                                            .label("§aEnter amount to transfer")
                                            .input("Amount", "100")
                                            .validResultHandler(result1 -> {
                                                final String amount = result1.next();

                                                assert amount != null;
                                                if (amount.isEmpty() || !MessageUtils.isValidInteger(amount)) {
                                                    sendErrorForm("§cPlease enter a correct amount!", player);
                                                    return;
                                                }
                                                final int amountInt = Integer.parseInt(amount);
                                                final double balance = account.getBalance();

                                                if (amountInt < 0.1) {
                                                    sendErrorForm("§cPlease enter a correct amount!", player);
                                                } else {
                                                    if (amountInt > balance) {
                                                        sendErrorForm("§cYou don't have enough money in your bank account!", player);

                                                    }
                                            }
                                                final Player targetPlayer = Bukkit.getPlayer(targetAccount.getOwnerUuid());
                                                if (targetPlayer != null) {
                                                    targetPlayer.sendMessage(Component.text(EZBanks.getPrefix() + "§aYou have received a bank transfer from §b" + player.getJavaUsername() + "§a. Amount: §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol()));
                                                }

                                                EZBanks.getInstance().getBankManager().removeBalance(account, amountInt);
                                                EZBanks.getInstance().getBankManager().addBalance(targetAccount, amountInt);
                                                EZBanks.getInstance().getBankManager().addTransaction(account, TransactionType.TRANSFER_OUT, amountInt, player.getJavaUniqueId());
                                                assert targetPlayer != null;
                                                EZBanks.getInstance().getBankManager().addTransaction(targetAccount, TransactionType.TRANSFER_IN, amountInt, targetPlayer.getUniqueId());
                                                sendSuccresForm("§aYou have successfully transferred §6" + amount + EZBanks.getInstance().getConfigManager().getSymbol() + " §ato §b" + Bukkit.getOfflinePlayer(targetAccount.getOwnerUuid()).getName(), player, account);


                        })

                            );
                        }));
        }

        public static void sendSuccresForm (String content, FloodgatePlayer player, BankAccount account){
            player.sendForm(
                    SimpleForm.builder()
                            .title("§a✓")
                            .content(content)
                            .button("§cBack")
                            .validResultHandler(result -> {
                                if (result.clickedButtonId() == 0) {
                                    sendBankAccountForm(account, player);
                                }
                            })
            );
        }

        public static void sendErrorForm(String content, FloodgatePlayer player){
            player.sendForm(
                    SimpleForm.builder()
                            .title("§c✕")
                            .content(content)
            );
        }

    public static void sendChangePinForm(BankAccount account, FloodgatePlayer player){
        player.sendForm(
                CustomForm.builder()
                        .title("§aChange your card PIN")
                        .label("§cType new PIN ")
                        .input("New PIN", "1234")
                        .validResultHandler(result -> {
                            final String newPin = result.next();

                            assert newPin != null;
                            if (newPin.isEmpty()) {
                                sendErrorForm("§cPlease enter a correct PIN!", player);
                                return;
                            }
                            if (!MessageUtils.isValidInteger(newPin)) {
                                sendErrorForm("§cThe PIN must be a number.", player);
                                return;
                            }
                            if (newPin.length() != 4) {
                                sendErrorForm("§cThe PIN must be exactly 4 digits long.", player);
                                return;
                            }

                            EZBanks.getInstance().getBankManager().setNewPin(account, Integer.parseInt(newPin));
                            sendSuccresForm("§aYou have successfully changed the PIN to: §6" + newPin, player, account);
                        })
        );
    }



}