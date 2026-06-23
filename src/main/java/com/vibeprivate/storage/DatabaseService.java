package com.vibeprivate.storage;

import com.vibeprivate.config.ConfigService;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.Properties;

public final class DatabaseService {
    public enum Dialect {
        SQLITE,
        MYSQL
    }

    private final JavaPlugin plugin;
    private final ConfigService configService;
    private final DatabaseStatements statements = new DatabaseStatements(this);
    private Connection connection;
    private Dialect dialect = Dialect.SQLITE;

    public DatabaseService(JavaPlugin plugin, ConfigService configService) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.configService = Objects.requireNonNull(configService, "configService");
    }

    public void open() {
        String type = configService.getDatabaseType();
        dialect = type.equals("mysql") ? Dialect.MYSQL : Dialect.SQLITE;

        try {
            if (dialect == Dialect.MYSQL) {
                openMySql();
            } else {
                openSqlite();
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Failed to open " + dialect.name().toLowerCase() + " database.", exception);
        }
    }

    public void migrate() {
        new DatabaseSchema(this).migrate();
    }

    public Connection getConnection() {
        if (connection == null) {
            throw new IllegalStateException("Database is not open.");
        }

        return connection;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public boolean isMySql() {
        return dialect == Dialect.MYSQL;
    }

    public String upsertRegionSql() {
        return statements.upsertRegionSql();
    }

    public String upsertDefaultFlagSql() {
        return statements.upsertDefaultFlagSql();
    }

    public String upsertMemberSql() {
        return statements.upsertMemberSql();
    }

    public String upsertMemberFlagSql() {
        return statements.upsertMemberFlagSql();
    }

    public String upsertDepositSql() {
        return statements.upsertDepositSql();
    }

    public String upsertProtectedChunkSql() {
        return statements.upsertProtectedChunkSql();
    }

    public String upsertRegionHomeSql() {
        return statements.upsertRegionHomeSql();
    }

    public void close() {
        if (connection == null) {
            return;
        }

        try {
            connection.close();
        } catch (SQLException exception) {
            plugin.getLogger().warning("Failed to close database: " + exception.getMessage());
        } finally {
            connection = null;
        }
    }

    private void openSqlite() throws SQLException {
        File databaseFile = new File(plugin.getDataFolder(), configService.getDatabaseFile());
        File parent = databaseFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile.getAbsolutePath());
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("PRAGMA synchronous = NORMAL");
        }
    }

    private void openMySql() throws SQLException {
        String parameters = configService.getMySqlParameters();
        String suffix = parameters == null || parameters.isBlank() ? "" : "?" + stripQuestionMark(parameters.trim());
        String url = "jdbc:mysql://" + configService.getMySqlHost() + ":" + configService.getMySqlPort()
                + "/" + configService.getMySqlDatabase() + suffix;

        Properties properties = new Properties();
        properties.setProperty("user", configService.getMySqlUsername());
        properties.setProperty("password", configService.getMySqlPassword());
        connection = DriverManager.getConnection(url, properties);
    }

    private String stripQuestionMark(String parameters) {
        return parameters.startsWith("?") ? parameters.substring(1) : parameters;
    }
}
