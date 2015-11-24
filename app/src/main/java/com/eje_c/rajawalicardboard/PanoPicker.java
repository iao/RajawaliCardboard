package com.eje_c.rajawalicardboard;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by iao on 06/10/15.
 */
public class PanoPicker extends Activity implements View.OnClickListener {

    public PanoListAdapter panoListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.pano_picker);

        panoListAdapter = new PanoListAdapter(this);
        ListView panoList = (ListView) findViewById(R.id.mainListView);
        panoList.setAdapter(panoListAdapter);

        new ProgressTask("http://openvirtualworlds.org/panodemo/panolist.php").execute();
        //new ProgressTask("http://138.251.213.207/panodemo/panolist.php").execute();
    }

    public void refresh(View v) {
        panoListAdapter.refresh();
        new ProgressTask("http://openvirtualworlds.org/panodemo/panolist.php").execute();
        //new ProgressTask("http://138.251.213.207/panodemo/panolist.php").execute();
    }

    @Override
    public void onClick(View v) {
        File zipfile = new File("/sdcard/PhotoTour/" + v.getTag().toString());
        File dir = new File(getFilesDir().getPath() + "/" + v.getTag().toString());
        try {
            unzip(zipfile, dir);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Intent panoview = new Intent(this, MainActivity.class);
        panoview.putExtra("dir", dir.getPath());
        startActivity(panoview);
    }

    public static void unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zis = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));
        try {
            ZipEntry ze;
            int count;
            byte[] buffer = new byte[8192];
            while ((ze = zis.getNextEntry()) != null) {
                File file = new File(targetDirectory, ze.getName());
                File dir = ze.isDirectory() ? file : file.getParentFile();
                if (!dir.isDirectory() && !dir.mkdirs())
                    throw new FileNotFoundException("Failed to ensure directory: " +
                            dir.getAbsolutePath());
                if (ze.isDirectory())
                    continue;
                FileOutputStream fout = new FileOutputStream(file);
                try {
                    while ((count = zis.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
            /* if time should be restored as well
            long time = ze.getTime();
            if (time > 0)
                file.setLastModified(time);
            */
            }
        } finally {
            zis.close();
        }
    }

    private class ProgressTask extends AsyncTask<String, Void, Boolean> {

        private String url;
        private ArrayList<PanoSet> panoSets;
        private boolean added;

        public ProgressTask(String url) {
            this.url = url;
            panoSets = new ArrayList<>();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            JSONArray json;
            try {
                json = JSONParser.getJSONFromUrl(url);

            added = false;
            for (int i = 0; i < json.length(); i++) {
                try {

                    JSONObject c = json.getJSONObject(i);
                    String name = c.getString("name");
                    String url = c.getString("url").replace(" ", "%20");
                    panoSets.add(new PanoSet(url, name));
                    added = true;
                    Log.d(PanoPicker.this.getLocalClassName(), "Name: "+name+" URL: "+url);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(added)
                panoListAdapter.addAll(panoSets);
            panoListAdapter.notifyDataSetChanged();
        }
    }

}