package com.hdweiss.morgand.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

import com.hdweiss.morgand.R;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.KeyPair;
import com.lamerman.FileDialog;
import com.lamerman.SelectionMode;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class KeySettingActivity extends Activity {

    public final static String KeyfileName = "keyfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
        //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "_rsa" });
        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String passphrase = prefs.getString("git_password", "");
            if (CopyKeyToStorage(filePath, passphrase)) {
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("git_key_info", GetKeyprint(GetInternalKeyPath(), passphrase));
                edit.putString("git_key_path", GetInternalKeyPath());
                edit.commit();
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
        }
        finish();
    }

    private String GetKeyprint(String keyfilePath, String passphrase) {
        try {
            KeyPair keyPair = KeyPair.load(new JSch(), keyfilePath);
            if (!passphrase.isEmpty() && keyPair.isEncrypted())
                keyPair.decrypt(passphrase);
            else if (passphrase.isEmpty() && keyPair.isEncrypted()) {
                Toast.makeText(this, R.string.error_key_need_pass, Toast.LENGTH_LONG).show();
                return "";
            }
            String fingerprint = keyPair.getFingerPrint();
            keyPair.dispose();
            return fingerprint;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private boolean CopyKeyToStorage(String filePath, String passphrase) {
        String fingerprint = GetKeyprint(filePath, passphrase);
        if (TextUtils.isEmpty(fingerprint)) {
            Toast.makeText(this, R.string.error_key_file_info, Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            FileInputStream fis = new FileInputStream(filePath);
            FileOutputStream fos = openFileOutput(KeyfileName, MODE_PRIVATE);

            byte[] buffer = new byte[1444];
            int byteread = 0;
            int bytesum = 0;
            while ((byteread = fis.read(buffer)) != -1) {
                bytesum += byteread;
                fos.write(buffer, 0, byteread);
            }
            fos.close();
            fis.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getText(R.string.error_key_file_copy), Toast.LENGTH_LONG);
        }

        return false;
    }

    private String GetInternalKeyPath() {
        return getFilesDir() + "/" + KeyfileName;
    }
}
