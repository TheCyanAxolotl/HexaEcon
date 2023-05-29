/*
 * Copyright (c) 2023. KiyoshiDevelopment
 *   All rights reserved.
 */

package space.kiyoshi.hexaecon.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import space.kiyoshi.hexaecon.HexaEcon;
import space.kiyoshi.hexaecon.functions.TableFunctionMongo;
import space.kiyoshi.hexaecon.functions.TableFunctionRedis;
import space.kiyoshi.hexaecon.functions.TableFunctionSQL;
import space.kiyoshi.hexaecon.utils.DataManager;
import space.kiyoshi.hexaecon.utils.NMSUtils;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

@SuppressWarnings("ALL")
public class HexaEconAPI {
    public static void createBankAccount(Player player, Long value) throws SQLException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        DataManager.INSTANCE.generatePlayerConfigAmount(player, value);
        try {
            switch (databasetype) {
                case "h2" -> {
                    TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, value);
                }
                case "MongoDB" -> {
                    TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), value);
                }
                case "MySQL" -> {
                    TableFunctionSQL.INSTANCE.createTableAmount(player, value);
                }
                case "Redis" -> {
                    TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteBankAccount(Player player) throws SQLException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        DataManager.INSTANCE.deletePlayerConfig(player);
        try {
            switch (databasetype) {
                case "h2" -> {
                    TableFunctionSQL.INSTANCE.dropTableSQLite(player);
                }
                case "MongoDB" -> {
                    TableFunctionMongo.INSTANCE.dropCollection(player.getName());
                }
                case "MySQL" -> {
                    TableFunctionSQL.INSTANCE.dropTable(player);
                }
                case "Redis" -> {
                    TableFunctionRedis.INSTANCE.dropTable(player.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean hasBankAccount(Player player) {
        File playerFolder = new File(HexaEcon.Companion.getPlugin().getDataFolder(), "data/"+player.getName()+"");
        return playerFolder.exists();
    }

    public static void setMoney(Player player, Long value) throws SQLException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        DataManager.INSTANCE.deletePlayerConfig(player);
        DataManager.INSTANCE.generatePlayerConfigAmount(player, value);
        try {
            switch (databasetype) {
                case "h2" -> {
                    TableFunctionSQL.INSTANCE.dropTableSQLite(player);
                    TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, value);
                }
                case "MongoDB" -> {
                    TableFunctionMongo.INSTANCE.dropCollection(player.getName());
                    TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), value);
                }
                case "MySQL" -> {
                    TableFunctionSQL.INSTANCE.dropTable(player);
                    TableFunctionSQL.INSTANCE.createTableAmount(player, value);
                }
                case "Redis" -> {
                    TableFunctionRedis.INSTANCE.dropTable(player.getName());
                    TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), value);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addMoney(Player player, Long value) {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = DataManager.INSTANCE.main().getString("DataBase.DataEconomyName");
        File dataFolder = HexaEcon.Companion.getPlugin().getDataFolder();
        File dataFileSqlite = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_SQLite.txt");
        File dataFileMysql = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_MySQL.txt");
        File dataFileMongodb = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_MongoDB.txt");
        File dataFileRedis = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_Redis.txt");
        FileConfiguration dataConfigSqlite = YamlConfiguration.loadConfiguration(dataFileSqlite);
        FileConfiguration dataConfigMysql = YamlConfiguration.loadConfiguration(dataFileMysql);
        FileConfiguration dataConfigMongodb = YamlConfiguration.loadConfiguration(dataFileMongodb);
        FileConfiguration dataConfigRedis = YamlConfiguration.loadConfiguration(dataFileRedis);
        long currentAmountSqlite = dataConfigSqlite.getLong("data." + dataeconomyvalue, 0);
        long currentAmountMysql = dataConfigMysql.getLong("data." + dataeconomyvalue, 0);
        long currentAmountMongodb = dataConfigMongodb.getLong("data." + dataeconomyvalue, 0);
        long currentAmountRedis = dataConfigRedis.getLong("data." + dataeconomyvalue, 0);
        long newAmountSqlite = currentAmountSqlite + value;
        long newAmountMysql = currentAmountMysql + value;
        long newAmountMongodb = currentAmountMongodb + value;
        long newAmountRedis = currentAmountRedis + value;
        try {
            switch (databasetype) {
                case "h2":
                    TableFunctionSQL.INSTANCE.dropTableSQLite(player);
                    TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, newAmountSqlite);
                    dataConfigSqlite.set("data." + dataeconomyvalue, newAmountSqlite);
                    dataConfigSqlite.save(dataFileSqlite);
                    break;
                case "MongoDB":
                    TableFunctionMongo.INSTANCE.dropCollection(player.getName());
                    TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), newAmountMongodb);
                    dataConfigMongodb.set("data." + dataeconomyvalue, newAmountMongodb);
                    dataConfigMongodb.save(dataFileMongodb);
                    break;
                case "MySQL":
                    TableFunctionSQL.INSTANCE.dropTable(player);
                    TableFunctionSQL.INSTANCE.createTableAmount(player, newAmountMysql);
                    dataConfigMysql.set("data." + dataeconomyvalue, newAmountMysql);
                    dataConfigMysql.save(dataFileMysql);
                    break;
                case "Redis":
                    TableFunctionRedis.INSTANCE.dropTable(player.getName());
                    TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), newAmountRedis);
                    dataConfigRedis.set("data." + dataeconomyvalue, newAmountRedis);
                    dataConfigRedis.save(dataFileRedis);
                    break;
            }
            HexaEcon.Companion.getPlugin().reloadConfig();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void removeMoney(Player player, Long value) throws IOException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = DataManager.INSTANCE.main().getString("DataBase.DataEconomyName");
        File dataFolder = HexaEcon.Companion.getPlugin().getDataFolder();
        File dataFileSqlite = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_SQLite.txt");
        File dataFileMysql = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_MySQL.txt");
        File dataFileMongodb = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_MongoDB.txt");
        File dataFileRedis = new File(dataFolder, "/data/" + player.getName() + "/" + player.getName() + "_Redis.txt");
        FileConfiguration dataConfigSqlite = YamlConfiguration.loadConfiguration(dataFileSqlite);
        FileConfiguration dataConfigMysql = YamlConfiguration.loadConfiguration(dataFileMysql);
        FileConfiguration dataConfigMongodb = YamlConfiguration.loadConfiguration(dataFileMongodb);
        FileConfiguration dataConfigRedis = YamlConfiguration.loadConfiguration(dataFileRedis);
        long currentAmountSqlite = dataConfigSqlite.getLong("data." + dataeconomyvalue, 0);
        long currentAmountMysql = dataConfigMysql.getLong("data." + dataeconomyvalue, 0);
        long currentAmountMongodb = dataConfigMongodb.getLong("data." + dataeconomyvalue, 0);
        long currentAmountRedis = dataConfigRedis.getLong("data." + dataeconomyvalue, 0);
        long newAmountSqlite = currentAmountSqlite - value;
        long newAmountMysql = currentAmountMysql - value;
        long newAmountMongodb = currentAmountMongodb - value;
        long newAmountRedis = currentAmountRedis - value;
        try {
            switch (databasetype) {
                case "h2":
                    if (newAmountSqlite >= 0) {
                        TableFunctionSQL.INSTANCE.dropTableSQLite(player);
                        TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, newAmountSqlite);
                        dataConfigSqlite.set("data." + dataeconomyvalue, newAmountSqlite);
                        dataConfigSqlite.save(dataFileSqlite);
                    }
                    break;
                case "MongoDB":
                    if (newAmountMongodb >= 0) {
                        TableFunctionMongo.INSTANCE.dropCollection(player.getName());
                        TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), newAmountMongodb);
                        dataConfigMongodb.set("data." + dataeconomyvalue, newAmountMongodb);
                        dataConfigMongodb.save(dataFileMongodb);
                    }
                    break;
                case "MySQL":
                    if (newAmountMysql >= 0) {
                        TableFunctionSQL.INSTANCE.dropTable(player);
                        TableFunctionSQL.INSTANCE.createTableAmount(player, newAmountMysql);
                        dataConfigMysql.set("data." + dataeconomyvalue, newAmountMysql);
                        dataConfigMysql.save(dataFileMysql);
                    }
                    break;
                case "Redis":
                    if (newAmountRedis >= 0) {
                        TableFunctionRedis.INSTANCE.dropTable(player.getName());
                        TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), newAmountRedis);
                        dataConfigRedis.set("data." + dataeconomyvalue, newAmountRedis);
                        dataConfigRedis.save(dataFileRedis);
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        HexaEcon.Companion.getPlugin().reloadConfig();
    }


    public static String getPlayerBalance(Player player) throws SQLException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = DataManager.INSTANCE.main().getString("DataBase.DataEconomyName");
        try {
            switch (databasetype) {
                case "h2" -> {
                    return TableFunctionSQL.selectAllFromTableAsStringSQLite(player.getName()).toString().replace("[", "").replace("]", "");
                }
                case "MongoDB" -> {
                    return TableFunctionMongo.INSTANCE.selectAllFromCollectionAsString(player.getName()).toString().replace("[", "").replace("]", "");
                }
                case "MySQL" -> {
                    Statement stmt = HexaEcon.Companion.getMySQLManager().getConnection().createStatement();
                    String SQL = "SELECT * FROM " + player.getName();
                    ResultSet rs = stmt.executeQuery(SQL);
                    rs.next();
                    String value = String.valueOf(rs.getLong(dataeconomyvalue));
                    rs.close();
                    stmt.close();
                    return value;
                }
                case "Redis" -> {
                    return TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.getName()).toString().replace("[", "").replace("]", "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Balance";
    }

    public static String getFormattedPlayerBalance(Player player) throws SQLException {
        String databasetype = DataManager.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = DataManager.INSTANCE.main().getString("DataBase.DataEconomyName");
        try {
            switch (databasetype) {
                case "h2" -> {
                    return formatBalance(TableFunctionSQL.selectAllFromTableAsStringSQLite(player.getName()).toString().replace("[", "").replace("]", ""));
                }
                case "MongoDB" -> {
                    return formatBalance(TableFunctionMongo.INSTANCE.selectAllFromCollectionAsString(player.getName()).toString().replace("[", "").replace("]", ""));
                }
                case "MySQL" -> {
                    Statement stmt = HexaEcon.Companion.getMySQLManager().getConnection().createStatement();
                    String SQL = "SELECT * FROM " + player.getName();
                    ResultSet rs = stmt.executeQuery(SQL);
                    rs.next();
                    String value = String.valueOf(rs.getLong(dataeconomyvalue));
                    rs.close();
                    stmt.close();
                    return formatBalance(value);
                }
                case "Redis" -> {
                    return formatBalance(TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.getName()).toString().replace("[", "").replace("]", ""));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Balance";
    }

    public static boolean hasPlayerEnoughMoney(Player player, Long amount) throws SQLException {
        String playerBalance = getPlayerBalance(player);
        if (playerBalance != null) {
            try {
                long balance = Long.parseLong(playerBalance);
                return balance >= amount;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean canUseTexturePack() {
        return NMSUtils.INSTANCE.checkServerVersionUp(NMSUtils.INSTANCE.getCleanServerVersion());
    }


    public static String formatBalance(String balance) {
        if (balance.equals("0")) {
            return "0";
        }

        String pattern = DataManager.INSTANCE.main().getString("Economy.Pattern");
        Long value = Long.parseLong(balance);

        String[] suffixes = {
                "",
                "k",
                "m",
                "b",
                "t",
                "q",
                "aa",
                "ab",
                "ac",
                "ad",
                "ae",
                "af",
                "ag",
                "ah",
                "ai",
                "aj",
                "ak",
                "al",
                "am",
                "an",
                "ao",
                "ap",
                "aq",
                "ar",
                "as",
                "at",
                "au",
                "av",
                "aw",
                "ax",
                "ay",
                "az"
        };

        int suffixIndex = (int) (Math.floor(Math.log10(value.doubleValue())) / 3);
        double formattedValue = suffixIndex < suffixes.length ? value / Math.pow(10.0, suffixIndex * 3) : value.doubleValue();
        String suffix = suffixes[Math.min(suffixIndex, suffixes.length - 1)];

        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setGroupingSeparator('.');
        decimalFormatSymbols.setDecimalSeparator(',');

        DecimalFormat decimalFormat = new DecimalFormat(pattern, decimalFormatSymbols);

        String formattedBalance = decimalFormat.format(formattedValue);

        return formattedBalance + suffix;
    }


}
