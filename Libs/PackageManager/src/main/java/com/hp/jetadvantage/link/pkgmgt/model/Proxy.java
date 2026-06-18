package com.hp.jetadvantage.link.pkgmgt.model;

public class Proxy {
    private String server;
    private int port;
    private String username;
    private String userpasswd;

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserpasswd() {
        return userpasswd;
    }

    public void setUserpasswd(String userpasswd) {
        this.userpasswd = userpasswd;
    }

    @Override
    public String toString() {
        return "Proxy{" +
                "server='" + server + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", userpasswd='" + userpasswd + '\'' +
                '}';
    }
}
