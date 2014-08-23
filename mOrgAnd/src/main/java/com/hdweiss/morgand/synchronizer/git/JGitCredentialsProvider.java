package com.hdweiss.morgand.synchronizer.git;

import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;

public class JGitCredentialsProvider extends CredentialsProvider {
    private String username;
    private String password;

    public JGitCredentialsProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

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
