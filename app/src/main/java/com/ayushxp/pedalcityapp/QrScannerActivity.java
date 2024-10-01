package com.ayushxp.pedalcityapp;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.journeyapps.barcodescanner.CaptureActivity;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.CompoundBarcodeView;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class QrScannerActivity extends CaptureActivity {

    DecoratedBarcodeView barcodeView;
    private Button enterBicycleNumberButton;
    private ToggleButton flashlightToggleButton;
    Dialog dialog;
    EditText bicycle_number;
    Button submit_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        barcodeView = findViewById(R.id.barcode_scanner);


        enterBicycleNumberButton = findViewById(R.id.enter_bicycle_number_button);
        flashlightToggleButton = findViewById(R.id.flashlight_toggle_button);

        flashlightToggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Toggle flashlight
            if (isChecked) {
                barcodeView.setTorchOn();
                flashlightToggleButton.setForeground(getResources().getDrawable(R.drawable.baseline_flash_off_24));
            } else {
                barcodeView.setTorchOff();
                flashlightToggleButton.setForeground(getResources().getDrawable(R.drawable.baseline_flash_on_24));
            }
        });

        //dialog Enter Bicycle Number
        dialog = new Dialog(QrScannerActivity.this);
        dialog.setContentView(R.layout.bicycle_number_popup);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bicycle_number = dialog.findViewById(R.id.bicycle_number);
        submit_btn = dialog.findViewById(R.id.submit_btn);

        enterBicycleNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                bicycle_number.setText("");

                submit_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String bicycle_num = bicycle_number.getText().toString();
                        if (!bicycle_num.isEmpty()){


                            dialog.dismiss();
                        } else {
                            Toast.makeText(QrScannerActivity.this, "PleaseEnter bicycle number", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

    }

}