package com.hdweiss.morgand.synchronizer.git;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;

public class JGitConfigSessionFactory extends JschConfigSessionFactory {
    private final String username;
    private final String password;
    private final String keyLocation;

    public JGitConfigSessionFactory(String username, String password, String keyLocation) {
        super();
        this.username = username;
        this.password = password;
        this.keyLocation = keyLocation;
    }

    @Override
    protected void configure(OpenSshConfig.Host host, Session session) {
        session.setConfig("StrictHostKeyChecking", "no"); // TODO Find out how to enable strict host checking

        // TODO Delete me
        // String knownHostsLocation = "/sdcard/morg/known_hosts";
        // jSch.setKnownHosts(knownHostsLocation);

        CredentialsProvider provider = new JGitCredentialsProvider(username, password);
        session.setUserInfo(new CredentialsProviderUserInfo(session, provider));
    }

    @Override
    protected JSch getJSch(OpenSshConfig.Host hc, FS fs) throws JSchException {
        JSch jSch = super.getJSch(hc, fs);
        jSch.removeAllIdentity();
        if (!keyLocation.isEmpty())
            jSch.addIdentity(keyLocation, password);
        return jSch;
    }
}
