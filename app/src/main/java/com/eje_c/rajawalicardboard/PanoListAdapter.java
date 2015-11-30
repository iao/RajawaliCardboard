package com.eje_c.rajawalicardboard;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by iao on 07/10/15.
 */
public class PanoListAdapter extends BaseAdapter {

    private List<PanoSet> panoListChapterList;
    private PanoPicker panoPicker;
    private SimpleDateFormat sdf;
    private String paths;

    public PanoListAdapter(PanoPicker panoPicker) {
        paths = panoPicker.getString(R.string.zippath);
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.UK);
        panoListChapterList = new ArrayList<PanoSet>();
        scanDir();

        this.panoPicker = panoPicker;
    }

    public void refresh() {
        panoListChapterList.clear();
        scanDir();
    }

    public void scanDir() {
        File directory =  new File(paths);
        if(!directory.exists())
            directory.mkdir();
        File[] files = directory.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".zip");
            }
        });

        for(File file : files) {
            panoListChapterList.add(new PanoSet(file));
        }
        notifyDataSetChanged();
    }

    public void addAll(List<PanoSet> panoSets) {
        for (PanoSet panoSet: panoSets ) {
            add(panoSet);
        }
    }
    
    public void add(PanoSet panoSet) {
        int index;
        if((index = panoListChapterList.indexOf(panoSet)) != -1) {

            panoListChapterList.get(index).Combine(panoSet);
        } else {
            //Log.d(panoPicker.getLocalClassName(), "Download: " + panoSet.name);
            panoListChapterList.add(panoSet);
        }
    }

    @Override
    public int getCount() {
        return panoListChapterList.size();
    }

    @Override
    public PanoSet getItem(int position) {
        return panoListChapterList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PanoSet chapter = panoListChapterList.get(position);

        //Log.d(panoPicker.getLocalClassName(), chapter.name + " " + chapter.url);

        File dir = new File(panoPicker.getFilesDir().getPath() + "/" + chapter.getName());
        File zipfile = new File(paths + chapter.getName());
        boolean updateable = false;

        if(zipfile.exists()) {
            //Log.d(panoPicker.getLocalClassName(), chapter.getName() + " | Time: " + sdf.format(new Date(zipfile.lastModified())) + " " + sdf.format(new Date(chapter.mtime)) + " " + sdf.format(new Date((zipfile.lastModified() - chapter.mtime))));
            if((zipfile.lastModified() - chapter.mtime) < 0)
                updateable = true;
        }

        if(chapter.downloaded && dir.exists()) {

            if (convertView == null || convertView.findViewById(R.id.textView) == null) {
                LayoutInflater inflater = (LayoutInflater) panoPicker.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.row_layout, parent, false);
            }


            TextView chapterName = (TextView) convertView.findViewById(R.id.textView);
            Button unzip = (Button) convertView.findViewById(R.id.button);
            Button run = (Button) convertView.findViewById(R.id.button2);
            Button run1 = (Button) convertView.findViewById(R.id.button3);


            chapterName.setText(chapter.getName());

            unzip.setText("Cardboard");
            unzip.setTag(chapter.getName());
            unzip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File dir = new File(panoPicker.getFilesDir().getPath() + "/" + v.getTag().toString());
                    if (dir.exists()) {
                        Intent panoview = new Intent(panoPicker, MainActivity.class);
                        panoview.putExtra("dir", dir.getPath());
                        panoPicker.startActivity(panoview);
                    }
                }
            });

            run.setText("Run");
            run.setTag(chapter.getName());
            run.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File dir = new File(panoPicker.getFilesDir().getPath() + "/" + v.getTag().toString());
                    if (dir.exists()) {
                        Intent panoview = new Intent(panoPicker, MainActivity.class);
                        panoview.putExtra("dir", dir.getPath());
                        panoview.putExtra("cardboard", false);
                        panoPicker.startActivity(panoview);
                    }
                }
            });


            run1.setText("Redownload");


            if(updateable)
                run1.getBackground().setColorFilter(panoPicker.getResources().getColor(R.color.Green), PorterDuff.Mode.MULTIPLY);
            /*else
                run1.getBackground().setColorFilter(panoPicker.getResources().getColor(R.color.black), PorterDuff.Mode.MULTIPLY);*/
            run1.setTag(position);
            run1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PanoSet panoSet = panoListChapterList.get((Integer) v.getTag());
                    Log.d(panoPicker.getLocalClassName(), "Download: " + panoSet.url);
                    new DownloadTask(panoPicker, panoSet.url, new File(paths+panoSet.name), panoSet).execute();
                }
            });
        } else {
            if (convertView == null || convertView.findViewById(R.id.textView2) == null) {
                LayoutInflater inflater = (LayoutInflater) panoPicker.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.download_layout, parent, false);
            }


            TextView chapterName = (TextView) convertView.findViewById(R.id.textView2);
            Button download = (Button) convertView.findViewById(R.id.button4);

            chapterName.setText(chapter.getName());


            if(zipfile.exists()) {
                download.setText("Unzip");
                download.setTag(chapter.getName());
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        File zipfile = new File(paths + v.getTag().toString());
                        File dir = new File(panoPicker.getFilesDir().getPath() + "/" + v.getTag().toString());
                        if(dir.exists()) {
                            dir.delete();
                            dir.mkdir();
                        }
                        new UnzipTask(panoPicker, zipfile, dir).execute();
                    }
                });
            } else {
                download.setText("Download");
                download.setTag(position);
                download.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PanoSet panoSet = panoListChapterList.get((Integer) v.getTag());
                        Log.d(panoPicker.getLocalClassName(), "Download: " + panoSet.url);
                        new DownloadTask(panoPicker, panoSet.url, new File(paths + panoSet.name), panoSet).execute();
                    }
                });
            }
        }

        return convertView;
    }

    private class DownloadTask extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog dialog;
        private String urlString;
        private File file;
        private PanoSet panoSet;

        public DownloadTask(PanoPicker activity, String url, File file, PanoSet panoSet) {
            dialog = new ProgressDialog(activity);
            this.urlString = url;
            this.file = file;
            this.panoSet = panoSet;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Downloading, please wait.");
            dialog.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            int count;
            byte[] buffer = new byte[8192];

            try {
                URL url = new URL(urlString);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                FileOutputStream fout = new FileOutputStream(file);
                InputStream is = urlConnection.getInputStream();
                try {
                    while ((count = is.read(buffer)) != -1)
                        fout.write(buffer, 0, count);
                } finally {
                    fout.close();
                }
                panoSet.file = file;
                panoSet.downloaded = true;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;
        }

        protected void onPostExecute(Boolean result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            File zipfile = new File(paths + panoSet.name);
            File dir = new File(panoPicker.getFilesDir().getPath() + "/" + panoSet.name);
            if(dir.exists()) {
                dir.delete();
                dir.mkdir();
            }
            new UnzipTask(panoPicker, zipfile, dir).execute();
        }
    }

    private class UnzipTask extends AsyncTask <Void, Void, Void> {
        private ProgressDialog dialog;
        private File zipfile;
        private File dir;

        public UnzipTask(PanoPicker activity, File zipfile, File dir) {
            dialog = new ProgressDialog(activity);
            this.zipfile = zipfile;
            this.dir = dir;
        }

        @Override
        protected void onPreExecute() {
            dialog.setMessage("Unziping, please wait.");
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void result) {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
            PanoListAdapter.this.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                PanoPicker.unzip(zipfile, dir);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}
