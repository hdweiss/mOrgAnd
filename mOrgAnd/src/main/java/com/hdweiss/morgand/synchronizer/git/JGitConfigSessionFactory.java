package com.hdweiss.morgand.synchronizer.git;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProviderUserInfo;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.transport.URIish;
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

        CredentialsProvider provider = new JGitCredentialsProvider();
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

    public class JGitCredentialsProvider extends CredentialsProvider {

        @Override
        public boolean isInteractive() {
            return false;
        }

        @Override // never gets called
        public boolean supports(CredentialItem... items) {
            return true;
        }

        @Override
        public boolean get(URIish uri, CredentialItem... items)
                throws UnsupportedCredentialItem {
            for (CredentialItem item : items) {
                if (item instanceof CredentialItem.Username) {
                    ((CredentialItem.Username) item).setValue(username);
                } else if (item instanceof CredentialItem.Password) {
                    ((CredentialItem.Password) item).setValue(password.toCharArray());
                } else if (item instanceof CredentialItem.StringType) {
                    ((CredentialItem.StringType) item).setValue(password);
                } else if (item instanceof CredentialItem.InformationalMessage) {
                    throw new UnsupportedCredentialItem(uri, "Not supported");
                } else if (item instanceof CredentialItem.YesNoType) {
                    // TODO handle strict host key checking here
                    throw new UnsupportedCredentialItem(uri, "Not supported");
                } else {
                    throw new UnsupportedCredentialItem(uri, "Not supported");
                }
            }
            return true;
        }
    }
}
