package it.chiarani.meteotrentinoapp.views;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import it.chiarani.meteotrentinoapp.R;
import it.chiarani.meteotrentinoapp.api.API_locality;
import it.chiarani.meteotrentinoapp.api.API_locality_response;
import it.chiarani.meteotrentinoapp.database.entity.LocalityEntity;
import it.chiarani.meteotrentinoapp.databinding.ActivityChooseLocationBinding;
import it.chiarani.meteotrentinoapp.models.Locality;
import it.chiarani.meteotrentinoapp.repositories.LocalityRepository;

public class ChooseLocationActivity extends SampleActivity implements API_locality_response {

  // #region private fields
  private static final String CHOOSELOCATIONACTIVITY_TAG = "CHOOSELOCATIONACTIVITY";
  private ActivityChooseLocationBinding binding;
  private String[] all_locs;
  // #endregion

  @Override
  protected int getLayoutID() {
    return R.layout.activity_choose_location;
  }

  @Override
  protected void setActivityBinding() {
    binding = DataBindingUtil.setContentView(this, getLayoutID());
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Log.d( CHOOSELOCATIONACTIVITY_TAG, "Start choose location actiity");

    LocalityRepository repository = new LocalityRepository(this.getApplication());

    repository.getAll().observe(this, entries -> {
      if(entries.size() == 0) {
        // Launch async task for get locality
        new API_locality(getApplication(), this, this::processFinish).execute();
      }
      else
      {
        all_locs = listTostring(entries);

        if(all_locs == null) {
          // TODO: place in @String
          Toast.makeText(this, "Impossibile scaricare le località. riprova.", Toast.LENGTH_LONG).show();
          return;
        }

        // create adapter with all localities
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, all_locs);

        // set adapter to autocomplete text
        binding.chooseLocationAutoCompleteTxt.setAdapter(adapter);
      }
    });

    // set next button handler
    binding.chooseLocationBtnNext.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {

        String user_location = binding.chooseLocationAutoCompleteTxt.getText().toString();

        if(user_location.isEmpty() || user_location == null || all_locs == null || all_locs.length == 0) {
          Toast.makeText(v.getContext(), "Inserire una località per continuare!", Toast.LENGTH_LONG).show();
          return;
        }

        for(String l : all_locs){
          if(l.toLowerCase().equals(user_location.toLowerCase()))
          {
            // launch main activity
            Intent myIntent = new Intent(ChooseLocationActivity.this, MainActivity.class);
            myIntent.putExtra("POSITION", binding.chooseLocationAutoCompleteTxt.getText().toString());
            startActivity(myIntent);
          }
        }
        Toast.makeText(v.getContext(), "Località non valida!", Toast.LENGTH_LONG).show();
      }
    });

    /**
     * Skip activity after choose the item
     */
    binding.chooseLocationAutoCompleteTxt.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        Intent myIntent = new Intent(ChooseLocationActivity.this, MainActivity.class);
        myIntent.putExtra("POSITION", binding.chooseLocationAutoCompleteTxt.getText().toString());
        startActivity(myIntent);
      }
    });
  }

  /**
   * Called after API termination
   */
  @Override
  public void processFinish() {
    LocalityRepository repository = new LocalityRepository(this.getApplication());

    repository.getAll().observe(this, entries -> {
      all_locs = listTostring(entries);

      if(all_locs == null) {
        // TODO: place in @String
        Toast.makeText(this, "Impossibile scaricare le località. riprova.", Toast.LENGTH_LONG).show();
        return;
      }

      // create adapter with all localities
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, all_locs);

      // set adapter to autocomplete text
      binding.chooseLocationAutoCompleteTxt.setAdapter(adapter);
    });
  }

  /**
   * Convert ArrayList to String[] containing all localities
   * @param data ArrayList to convert
   * @return String[] with locality
   */
  private String[] listTostring(List<LocalityEntity> data) {
    if(data.isEmpty())
      return null;

    String[] tmp = new String[data.size()];

    int i = 0;
    for(Locality loc : data) {
      tmp[i++] = loc.getLoc();
    }

    return tmp;
  }

  @Override
  public void onBackPressed() {
    // do nothing
  }
}
