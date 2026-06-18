package com.hp.jetadvantage.link.pkgmgt.hpkutil.model;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root (name = "networkCredentials")
public class NetworkCredentials {

    @Element (required = false)
    private String userName;

    @Element (required = false)
    private String password;

    @Element (required = false)
    private String domain;

    public String getDomain() { return domain; }

    public void setDomain(String domain) { this.domain = domain; }

    public String getUserName() { return userName; }

    public void setUserName(String userName) { this.userName = userName; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public boolean isEmpty(){
        if((userName == null || userName.isEmpty()) &&
            (password == null || password.isEmpty()) &&
            (domain == null || domain.isEmpty())){
            return true;
        } else return false;
    }
}
