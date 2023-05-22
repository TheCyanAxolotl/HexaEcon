package space.kiyoshi.hexaecon.api;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import space.kiyoshi.hexaecon.HexaEcon;
import space.kiyoshi.hexaecon.functions.TableFunctionMongo;
import space.kiyoshi.hexaecon.functions.TableFunctionRedis;
import space.kiyoshi.hexaecon.functions.TableFunctionSQL;
import space.kiyoshi.hexaecon.utils.GetConfig;
import space.kiyoshi.hexaecon.utils.NMSUtils;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class HexaEconAPI {
    public static void createBankAccount(Player player, Long value) throws SQLException {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        GetConfig.INSTANCE.generatePlayerConfigAmount(player, value);
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
    }

    public static void deleteBankAccount(Player player) throws SQLException {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        GetConfig.INSTANCE.deletePlayerConfig(player);
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
    }

    public static boolean hasBankAccount(Player player) {
        File playerFolder = new File(HexaEcon.Companion.getPlugin().getDataFolder(), "data/"+player.getName()+"");
        return playerFolder.exists();
    }

    public static void setMoney(Player player, Long value) throws SQLException {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        GetConfig.INSTANCE.deletePlayerConfig(player);
        GetConfig.INSTANCE.generatePlayerConfigAmount(player, value);
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
    }

    public static void addMoney(Player player, Long value) {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = GetConfig.INSTANCE.main().getString("DataBase.DataEconomyName");
        File data_names2_sqlite =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_SQLite.txt");
        File data_names2_mysql =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_MySQL.txt");
        File data_names2_mongodb =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_MongoDB.txt");
        File data_names2_redis =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_Redis.txt");
        FileConfiguration data_names_config2_sqlite =
                YamlConfiguration.loadConfiguration(data_names2_sqlite);
        FileConfiguration data_names_config2_mysql =
                YamlConfiguration.loadConfiguration(data_names2_mysql);
        FileConfiguration data_names_config2_mongodb =
                YamlConfiguration.loadConfiguration(data_names2_mongodb);
        FileConfiguration data_names_config2_redis =
                YamlConfiguration.loadConfiguration(data_names2_redis);
        File data_names_sqlite = new File(
                HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName()
                        + "_SQLite.txt"
        );
        File data_names_mysql = new File(
                HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName()
                        + "_MySQL.txt"
        );
        File data_names_mongodb = new File(
                HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName()
                        + "_MongoDB.txt"
        );
        File data_names_redis = new File(
                HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName()
                        + "_Redis.txt"
        );
        long somasqlite =
                data_names_config2_sqlite.getLong("data." + dataeconomyvalue) + value;
        long somamysql =
                data_names_config2_mysql.getLong("data." + dataeconomyvalue) + value;
        long somamongodb =
                data_names_config2_mongodb.getLong("data." + dataeconomyvalue) + value;
        long somaredis =
                data_names_config2_redis.getLong("data." + dataeconomyvalue) + value;
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
        } catch (Exception ignored) {}
        try {
            switch (databasetype) {
                case "h2" -> {
                    TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, somasqlite);
                }
                case "MongoDB" -> {
                    TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), somamongodb);
                }
                case "MySQL" -> {
                    TableFunctionSQL.INSTANCE.createTableAmount(player, somamysql);
                }
                case "Redis" -> {
                    TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), somaredis);
                }
            }
        } catch (Exception ignored) {}
        switch (databasetype) {
            case "h2" -> {
                data_names_config2_sqlite.set("data." + dataeconomyvalue, somasqlite);
            }
            case "MongoDB" -> {
                data_names_config2_mongodb.set("data." + dataeconomyvalue, somamongodb);
            }
            case "MySQL" -> {
                data_names_config2_mysql.set("data." + dataeconomyvalue, somamysql);
            }
            case "Redis" -> {
                data_names_config2_redis.set("data." + dataeconomyvalue, somaredis);
            }
        }
        try {
            switch (databasetype) {
                case "h2" -> {
                    data_names_config2_sqlite.save(data_names_sqlite);
                }
                case "MongoDB" -> {
                    data_names_config2_mongodb.save(data_names_mongodb);
                }
                case "MySQL" -> {
                    data_names_config2_mysql.save(data_names_mysql);
                }
                case "Redis" -> {
                    data_names_config2_redis.save(data_names_redis);
                }
            }
        } catch (Exception ignored) {}
        HexaEcon.Companion.getPlugin().reloadConfig();
    }

    public static void removeMoney(Player player, Long value) {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = GetConfig.INSTANCE.main().getString("DataBase.DataEconomyName");
        File data_names_sqlite =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_SQLite.txt");
        File data_names_mysql =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_MySQL.txt");
        File data_names_mongodb =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_MongoDB.txt");
        File data_names_redis =
                new File(HexaEcon.Companion.getPlugin().getDataFolder().toString() + "/data/"+player.getName()+"/" + player.getName() + "_Redis.txt");
        FileConfiguration data_names_config_sqlite =
                YamlConfiguration.loadConfiguration(data_names_sqlite);
        FileConfiguration data_names_config_mysql =
                YamlConfiguration.loadConfiguration(data_names_mysql);
        FileConfiguration data_names_config_mongodb =
                YamlConfiguration.loadConfiguration(data_names_mongodb);
        FileConfiguration data_names_config_redis =
                YamlConfiguration.loadConfiguration(data_names_redis);
        long somasqlite =
                data_names_config_sqlite.getLong("data." + dataeconomyvalue) - value;
        long somamysql =
                data_names_config_mysql.getLong("data." + dataeconomyvalue) - value;
        long somamongodb =
                data_names_config_mongodb.getLong("data." + data_names_config_mongodb) - value;
        long somaredis =
                data_names_config_redis.getLong("data." + dataeconomyvalue) - value;

        if (databasetype == "h2") {
            if (data_names_config_sqlite.getLong("data." + dataeconomyvalue) >= value){
                try {
                    switch (databasetype) {
                        case "h2" ->{
                            TableFunctionSQL.INSTANCE.dropTableSQLite(player);
                        }
                        case "MongoDB" ->{
                            TableFunctionMongo.INSTANCE.dropCollection(player.getName());
                        }
                        case "MySQL" ->{
                            TableFunctionSQL.INSTANCE.dropTable(player);
                        }
                        case "Redis" ->{
                            TableFunctionRedis.INSTANCE.dropTable(player.getName());
                        }
                    }
                } catch (Exception ignored) {}
                try {
                    switch (databasetype) {
                        case "h2" ->{
                            TableFunctionSQL.INSTANCE.createTableAmountSQLite(player, somasqlite);
                        }
                        case "MongoDB" ->{
                            TableFunctionMongo.INSTANCE.createCollectionAmount(player.getName(), somamongodb);
                        }
                        case "MySQL" ->{
                            TableFunctionSQL.INSTANCE.createTableAmount(player, somamysql);
                        }
                        case "Redis" ->{
                            TableFunctionRedis.INSTANCE.createTableAmount(player.getName(), somaredis);
                        }
                    }
                } catch (Exception ignored) {}
                switch (databasetype) {
                    case "h2" ->{
                        data_names_config_sqlite.set("data." + dataeconomyvalue, somasqlite);
                    }
                    case "MongoDB" ->{
                        data_names_config_mongodb.set("data." + dataeconomyvalue, somamongodb);
                    }
                    case "MySQL" ->{
                        data_names_config_mysql.set("data." + dataeconomyvalue, somamysql);
                    }
                    case "Redis" ->{
                        data_names_config_redis.set("data." + dataeconomyvalue, somaredis);
                    }
                }
                try {
                    switch (databasetype) {
                        case "h2" ->{
                            data_names_config_sqlite.save(data_names_sqlite);
                        }
                        case "MongoDB" ->{
                            data_names_config_mongodb.save(data_names_mongodb);
                        }
                        case "MySQL" ->{
                            data_names_config_mysql.save(data_names_mysql);
                        }
                        case "Redis" ->{
                            data_names_config_redis.save(data_names_redis);
                        }
                    }
                } catch (Exception ignored) {}
                HexaEcon.Companion.getPlugin().reloadConfig();
            }
        }
    }

    public static String getPlayerBalance(Player player) throws SQLException {
        String databasetype = GetConfig.INSTANCE.main().getString("DataBase.Type");
        String dataeconomyvalue = GetConfig.INSTANCE.main().getString("DataBase.DataEconomyName");
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
                TableFunctionRedis.selectAllFromCollectionAsStringRedis(player.getName()).toString().replace("[", "").replace("]", "");
            }
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
        Long value = Long.parseLong(balance);

        List<String> suffixes = Arrays.asList(
                "",
                "k",
                "m",
                "t",
                "q",
                "a",
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
        );

        int suffixIndex = (int) (Math.floor(Math.log10(value.doubleValue())) / 3);

        double formattedValue = suffixIndex >= 0 && suffixIndex < suffixes.size() ?
                value / Math.pow(10.0, suffixIndex * 3) : value.doubleValue();

        String formattedString = String.format("%.1f%s", formattedValue, suffixes.get(suffixIndex));

        return value == 0L ? "0" : formattedString;
    }
}
