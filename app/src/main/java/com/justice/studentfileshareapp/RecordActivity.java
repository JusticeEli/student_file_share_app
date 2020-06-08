package com.justice.studentfileshareapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecordActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private Chronometer chronometer;
    private ImageView recordImageView;
    private boolean recording = false;

    private File file;
    private FileData fileData = new FileData();

    private MediaRecorder recorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        initWidgets();
        setOnClickListeners();
    }

    private void setOnClickListeners() {

        recordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recording) {
                    recording = false;

                    stopRecording();
                } else {
                    recording = true;

                    startRecording();

                }
            }
        });

    }

    private void stopRecording() {
        chronometer.stop();
        recorder.stop();
        recorder.reset();
        recorder.release();

        recordImageView.setImageResource(R.mipmap.stop_recording);
    }

    private void startRecording() {
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        recordImageView.setImageResource(R.mipmap.start_recording);
        try {
            file = File.createTempFile(System.currentTimeMillis()+"","3gp");
        } catch (Exception e) {
            e.printStackTrace();
        }
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(file.getAbsolutePath());
        try {
            recorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        recorder.start(); // Recording is now started
    }

    private void initWidgets() {
        progressBar = findViewById(R.id.progressBar);
        chronometer = findViewById(R.id.chronometer);
        recordImageView = findViewById(R.id.recordImageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.record_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.uploadMenu) {
            Toast.makeText(this, "uploading", Toast.LENGTH_SHORT).show();
            uploadRecordedFile();
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadRecordedFile() {
        if (recorder == null) {
            return;
        }
        stopRecording();
        uploadFile(Uri.fromFile(file));

    }

    private void uploadFile(Uri uri) {
        progressBar.setVisibility(View.VISIBLE);
        String fileName = System.currentTimeMillis() + "";
        fileData.setFileName(fileName);
        final StorageReference ref = FirebaseStorage.getInstance().getReference("files").child(fileName + ".jpg");

        UploadTask uploadTask = ref.putFile(uri);


        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    fileData.setFileUrl(downloadUri.toString());
                    Toast.makeText(RecordActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();
                    saveFileMetadataInFirebase();

                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(RecordActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);
            }
        });

    }

    private void saveFileMetadataInFirebase() {
        Map<String, Object> map = new HashMap<>();
        map.put("fileUrl", fileData.getFileUrl());
        map.put("fileName", fileData.getFileName());
        map.put("uploadDate", FieldValue.serverTimestamp());

        progressBar.setVisibility(View.VISIBLE);
        FirebaseFirestore.getInstance().collection("files").add(map).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(RecordActivity.this, "metadata uploaded successfully", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(RecordActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.GONE);

            }
        });
    }

    public void uploadFile(View view) {
        Toast.makeText(this, "uploading", Toast.LENGTH_SHORT).show();
        uploadRecordedFile();

    }

}
