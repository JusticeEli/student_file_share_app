package com.justice.studentfileshareapp;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

import java.text.SimpleDateFormat;
import java.util.Objects;

public class MainAdapter extends FirestoreRecyclerAdapter<FileData, MainAdapter.ViewHolder> {
    private Context context;

    public MainAdapter(Context context, @NonNull FirestoreRecyclerOptions<FileData> options) {
        super(options);
        this.context = context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull FileData model) {
       String date=new SimpleDateFormat("dd-MM-yyyy").format(model.getUploadDate());

        holder.fileNameTxtView.setText(model.getFileName());
         holder.uploadDateTxtView.setText(date);

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);

        return new ViewHolder(view);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView fileNameTxtView;
        private TextView uploadDateTxtView;

        private View itemView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
            fileNameTxtView = itemView.findViewById(R.id.fileNameTextView);
            uploadDateTxtView = itemView.findViewById(R.id.uploadDateTxtView);
            setOnClickListeners();

        }

        private void setOnClickListeners() {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    downloadFile(getSnapshots().get(getAdapterPosition()));


                }
            });

        }

    }


    public void downloadFile(FileData fileData) {
        ((MainActivity) context).progressBar.setVisibility(View.VISIBLE);
        new BackgroundAsync().execute(fileData);

    }

    private void DownloadSuccess() {
    }


    class BackgroundAsync extends AsyncTask<FileData, Void, Void> {

        @Override
        protected Void doInBackground(FileData... fileData) {
            String DownloadUrl = fileData[0].getFileUrl();
            DownloadManager.Request request1 = new DownloadManager.Request(Uri.parse(DownloadUrl));
            request1.setDescription("Sample Music File");   //appears the same in Notification bar while downloading
            request1.setTitle("File1.mp3");
            request1.setVisibleInDownloadsUi(false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request1.allowScanningByMediaScanner();
                request1.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
            }
            request1.setDestinationInExternalFilesDir(context, Environment.getExternalStorageDirectory().getAbsolutePath(), "/" + fileData[0].getFileName());

            DownloadManager manager1 = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Objects.requireNonNull(manager1).enqueue(request1);
            if (DownloadManager.STATUS_SUCCESSFUL == 8) {
                DownloadSuccess();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((MainActivity) context).progressBar.setVisibility(View.GONE);
            Toast.makeText(context, "Download Success", Toast.LENGTH_SHORT).show();
        }
    }

}
