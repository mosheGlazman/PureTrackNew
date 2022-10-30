package com.supercom.puretrack.ui.dialog;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.util.Base64;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.supercom.puretrack.database.DatabaseAccess;
import com.supercom.puretrack.data.source.local.table_managers.TableEventsManager;
import com.supercom.puretrack.model.database.entities.EntityOffenderPhoto;
import com.supercom.puretrack.data.source.local.table.TableEventConfig;
import com.supercom.puretrack.data.R;

import java.io.ByteArrayOutputStream;

public class PhotoOnDemandDialog extends DialogFragment {

    //Request code
    public final int ON_DEMAND_CAMERA_REQUEST_CODE = 188;

    //Arguments
    public static final String REQUEST_ID_KEY = "requestId";

    //Views
    private Button sendButton;
    private Button cancelButton;
    private ImageView offenderSelfieImageView;
    private Button retryButton;

    //Variables
    private String imageAsBase64String;
    private int requestId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_photo_on_demand, container, true);
        getDialog().requestWindowFeature(STYLE_NO_TITLE);
        setCancelable(false);
        initViews(view);
        initAndroidCamera();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void initViews(View view) {
        sendButton = view.findViewById(R.id.dialog_photo_on_demand_send);
        cancelButton = view.findViewById(R.id.dialog_photo_on_demand_cancel);
        offenderSelfieImageView = view.findViewById(R.id.dialog_photo_on_demand_image);
        retryButton = view.findViewById(R.id.dialog_photo_on_demand_retry);
        requestId = getArguments().getInt(REQUEST_ID_KEY);

        initListeners();
    }

    private void initListeners() {
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (imageAsBase64String == null) {
                    Toast.makeText(getActivity(), getString(R.string.dialog_photo_on_demand_error_sending_image), Toast.LENGTH_SHORT).show();
                    return;
                }
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.photoTest, -1, -1);
                int lastEventLogIdByEventType = DatabaseAccess.getInstance().tableEventLog.getLastEventLogIdByEventType(TableEventConfig.EventTypes.photoTest);
                EntityOffenderPhoto recordOffenderPhoto = new EntityOffenderPhoto(imageAsBase64String, String.valueOf(requestId), lastEventLogIdByEventType);
                DatabaseAccess.getInstance().tableOffenderPhoto.insertRecord(recordOffenderPhoto);
                Toast.makeText(getActivity(), getString(R.string.dialog_photo_on_demand_photo_saved), Toast.LENGTH_SHORT).show();
                dismiss();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableEventsManager.sharedInstance().addEventToLog(TableEventConfig.EventTypes.photoCanceledByTheOffender, -1, -1);
                dismiss();
            }
        });
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initAndroidCamera();
            }
        });
    }

    private void initAndroidCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("camerafacing", "front");
        takePictureIntent.putExtra("previous_mode", "front");
        takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
        takePictureIntent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
        takePictureIntent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        startActivityForResult(takePictureIntent, ON_DEMAND_CAMERA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ON_DEMAND_CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            offenderSelfieImageView.setImageBitmap(imageBitmap);
            getBase64StringFromBitmapAndSaveToLocal(imageBitmap);
        }
    }

    private void getBase64StringFromBitmapAndSaveToLocal(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        imageAsBase64String = Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}
