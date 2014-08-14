package com.hdweiss.morgand.synchronizer.git;


import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

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


        session.setConfig("User", username);
        session.setPassword(password);

//        session.setUserInfo(new MyUserInfo()); // TODO Delete me

        try {
            JSch jSch = getJSch(host, FS.DETECTED);
            jSch.addIdentity(keyLocation); // TODO Test keyfile with passphrase

            // TODO Delete me
//            String knownHostsLocation = "/sdcard/morg/known_hosts";
//            jSch.setKnownHosts(knownHostsLocation);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }


    public class MyUserInfo implements UserInfo, UIKeyboardInteractive {
        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public boolean promptPassphrase(String arg0) {
            return false;
        }

        @Override
        public boolean promptPassword(String arg0) {
            return false;
        }

        @Override
        public boolean promptYesNo(String arg0) {
            return true;
        }

        @Override
        public void showMessage(String arg0) {
        }

        @Override
        public String[] promptKeyboardInteractive(String arg0, String arg1,
                                                  String arg2, String[] arg3, boolean[] arg4) {
            return null;
        }
    }
}
