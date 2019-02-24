package hackcu.ani300.getthefuckapp;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getthefuckapp.R;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;

import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "4386dbf0a1a44da58fe44ee7119859f4";
    private static final String REDIRECT_URI = "getthefuckapp://callback";
    private SpotifyAppRemote mSpotifyAppRemote;

    private ArrayList<String> mSoothingList;
    private ArrayList<String> mHappyDayList;
    private ArrayList<String> mAnnoyingList;

    private String mSoothingSong;
    private String mAnnoyingSong;
    private String mHappyDaySong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // We will start writing our code here.

        // Set the connection parameters
        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build();

        SpotifyAppRemote.connect(this, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("MainActivity", "Connected! Yay!");

                        // Now you can start interacting with App Remote
                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("MainActivity", throwable.getMessage(), throwable);

                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

    }

    private void connected() {
        // Then we will write some more code here.
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        mSpotifyAppRemote.getPlayerApi()
                .subscribeToPlayerState()
                .setEventCallback(playerState -> {
                    final Track track = playerState.track;
                    if (track != null) {
                        Log.d("MainActivity", track.name + " by " + track.artist.name);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    public void setAlarmTime(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(),"timePicker");
    }

    public void processTimePickerResult(int hour, int minute) {
        String hourString = Integer.toString(hour);
        String minuteString = Integer.toString(minute);
        String timeMessage = hourString + ":" + minuteString;
        TextView alarmTime = findViewById(R.id.alarmTime);
        alarmTime.setText(timeMessage);
    }

    public void setMix(View view) {
        Toast toast = Toast.makeText(this, R.string.Mix_Chosen, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 150);
        toast.show();

        Random rand = new Random();
        mSoothingSong = mSoothingList.get(rand.nextInt(mSoothingList.size()));
        mAnnoyingSong = mAnnoyingList.get(rand.nextInt(mAnnoyingList.size()));
        mHappyDaySong = mHappyDayList.get(rand.nextInt(mHappyDayList.size()));
    }
}
