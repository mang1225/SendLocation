package com.ricky.sendlocation.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.ricky.sendlocation.R;

public class MainActivity extends AppCompatActivity {

  private static final int REQUEST_LOCATION = 0x1;
  private Button locationBtn;
  private SimpleDraweeView previewView;
  private TextView showText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    locationBtn = (Button) findViewById(R.id.btn_location);
    locationBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        startActivityForResult(new Intent(MainActivity.this, MapPickerActivity.class), REQUEST_LOCATION);
      }
    });
    previewView = (SimpleDraweeView) findViewById(R.id.image);
    GenericDraweeHierarchyBuilder builder =
        new GenericDraweeHierarchyBuilder(MainActivity.this.getResources());

    GenericDraweeHierarchy hierarchy = builder
        .setFadeDuration(200)
        .setRoundingParams(new RoundingParams()
            .setCornersRadius(10)
            .setRoundingMethod(RoundingParams.RoundingMethod.BITMAP_ONLY))
        .build();
    previewView.setHierarchy(hierarchy);
    showText = (TextView) findViewById(R.id.tv_show);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
    if (resultCode == RESULT_OK) {
      if (requestCode == REQUEST_LOCATION) {
        Double longitude = data.getDoubleExtra("longitude", 0);
        Double latitude = data.getDoubleExtra("latitude", 0);
        String street = data.getStringExtra("street");
        String place = data.getStringExtra("place");

        showText.setText("地址：" + street);

        String imageUrl =
            "http://api.map.baidu.com/staticimage?center=" + longitude + "," + latitude + "&zoom=15&size=200x100&scale=2&markers=" + longitude + "," + latitude + "&markerStyles=m,0xFF0000";
        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(imageUrl))
            .setResizeOptions(new ResizeOptions(previewView.getLayoutParams().width,
                previewView.getLayoutParams().height))
            .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
            .setOldController(previewView.getController())
            .setImageRequest(request)
            .build();
        previewView.setController(controller);
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }
}
