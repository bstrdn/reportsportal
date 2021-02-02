package ru.bstrdn.report.gate.configurations;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class GateUsersDataSource implements DataSource {

    private String connUrl;

    public GateUsersDataSource(String connUrl) {
        this.connUrl = connUrl;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(connUrl + ";jackcessOpener=ru.bstrdn.report.gate.configurations.CryptCodecOpener");
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return DriverManager.getConnection(connUrl + ";jackcessOpener=ru.bstrdn.report.gate.configurations.CryptCodecOpener", username, password);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
