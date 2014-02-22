package com.hdweiss.amorg.settings;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.hdweiss.amorg.R;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
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
        intent.putExtra(FileDialog.START_PATH, "/sdcard");
        intent.putExtra(FileDialog.CAN_SELECT_DIR, false);
        intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "_rsa" });
        intent.putExtra(FileDialog.SELECTION_MODE, SelectionMode.MODE_OPEN);

        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
            CopyKeyToStorage(filePath);

            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
            edit.putString("git_key_info", GetKeyprint());
            edit.putString("git_key_path", GetKeyPath());
            edit.commit();

        } else if (resultCode == Activity.RESULT_CANCELED) {
        }
        finish();
    }

    private String GetKeyprint() {
        try {
            String keyfilePath = GetKeyPath();
            KeyPair keyPair = KeyPair.load(new JSch(), keyfilePath);
            String fingerprint = keyPair.getFingerPrint();
            keyPair.dispose();
            return fingerprint;
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void CopyKeyToStorage(String filePath) {
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
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, getText(R.string.err), Toast.LENGTH_LONG);
        }
    }

    private String GetKeyPath() {
        return getFilesDir() + "/" + KeyfileName;
    }
}
