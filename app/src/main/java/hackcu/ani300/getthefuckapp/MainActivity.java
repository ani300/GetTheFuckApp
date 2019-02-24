package hackcu.ani300.getthefuckapp;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.getthefuckapp.R;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.types.Track;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class MainActivity extends AppCompatActivity {

    private static final String CLIENT_ID = "4386dbf0a1a44da58fe44ee7119859f4";
    private static final String REDIRECT_URI = "getthefuckapp://callback";
    private static final int REQUEST_CODE = 1337;
    private SpotifyAppRemote mSpotifyAppRemote;
    private String mSpotifyToken = "";
    private final OkHttpClient client = new OkHttpClient();

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
        mSoothingList = new ArrayList<>();
        mHappyDayList = new ArrayList<>();
        mAnnoyingList = new ArrayList<>();

        // Request code will be used to verify if result comes from the login activity. Can be set to any integer.
        AuthenticationRequest.Builder builder =
                new AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI);

        builder.setScopes(new String[]{"streaming, app-remote-control"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }

    private void connected() {
        // Then we will write some more code here.
        // Play a playlist
        //mSpotifyAppRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        //mSpotifyAppRemote.getPlayerApi()
        //        .subscribeToPlayerState()
        //        .setEventCallback(playerState -> {
        //            final Track track = playerState.track;
        //            if (track != null) {
        //                Log.d("MainActivity", track.name + " by " + track.artist.name);
        //            }
        //        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Aaand we will finish off here.
        super.onStop();
        SpotifyAppRemote.disconnect(mSpotifyAppRemote);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);

            switch (response.getType()) {
                // Response was successful and contains auth token
                case TOKEN:
                    // Handle successful response
                    mSpotifyToken = response.getAccessToken();

                    // Update list of songs
                    updateSongLists();

                    // Now you can start interacting with App Remote
                    ConnectionParams connectionParams =
                            new ConnectionParams.Builder(CLIENT_ID)
                                    .setRedirectUri(REDIRECT_URI)
                                    .showAuthView(false)
                                    .build();

                    SpotifyAppRemote.connect(this, connectionParams,
                            new Connector.ConnectionListener() {

                                @Override
                                public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                                    mSpotifyAppRemote = spotifyAppRemote;
                                    Log.d("MainActivity", "Connected! Yay!");

                                    connected();
                                }

                                @Override
                                public void onFailure(Throwable throwable) {
                                    Log.e("MainActivity", throwable.getMessage(), throwable);
                                    // Something went wrong when attempting to connect! Handle errors here
                                }
                            });
                    break;

                // Auth flow returned an error
                case ERROR:
                    // Handle error response
                    Log.e("MainActivity", response.getError());
                    break;

                // Most likely auth flow was cancelled
                default:
                    // Handle other cases
            }
        }
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

        Integer seed = Integer.valueOf(view.getTag().toString());

        Random rand = new Random(seed);
        mSoothingSong = mSoothingList.get(rand.nextInt(mSoothingList.size()));
        mAnnoyingSong = mAnnoyingList.get(rand.nextInt(mAnnoyingList.size()));
        mHappyDaySong = mHappyDayList.get(rand.nextInt(mHappyDayList.size()));
    }

    public void addSongsToList(JsonArray songList, List<String> list) {
        for (JsonElement song : songList) {
            JsonObject jsonSong = song.getAsJsonObject().get("track").getAsJsonObject();
            String songId = jsonSong.get("id").getAsString();
            list.add(songId);
        }
    }

    public void updateSongLists() {
        Request soothingRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/albums/0yuk5dojU1E5oJuXouXAOF/tracks")
                .addHeader("Authorization", "Bearer " + mSpotifyToken)
                .build();

        client.newCall(soothingRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    JsonParser parser = new JsonParser();
                    JsonElement rootNode = parser.parse(responseBody.string());
                    JsonObject playlistObject = rootNode.getAsJsonObject();
                    JsonArray songlist = playlistObject.get("items").getAsJsonArray();
                    for (JsonElement song : songlist) {
                        JsonObject jsonSong = song.getAsJsonObject();
                        String songId = jsonSong.get("id").getAsString();
                        mSoothingList.add(songId);
                    }
                }
            }
        });


        Request happyRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/playlists/0dtV5bmnRGIuAQUefVYVQt/tracks")
                .addHeader("Authorization", "Bearer " + mSpotifyToken)
                .build();

        client.newCall(happyRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    JsonParser parser = new JsonParser();
                    JsonElement rootNode = parser.parse(responseBody.string());
                    JsonObject playlistObject = rootNode.getAsJsonObject();
                    JsonArray songlist = playlistObject.get("items").getAsJsonArray();
                    addSongsToList(songlist, mHappyDayList);
                }
            }
        });


        Request annoyingRequest = new Request.Builder()
                .url("https://api.spotify.com/v1/playlists/41VnU8yRfJ2NzTO2AG3kI7/tracks")
                .addHeader("Authorization", "Bearer " + mSpotifyToken)
                .build();

        client.newCall(annoyingRequest).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    JsonParser parser = new JsonParser();
                    JsonElement rootNode = parser.parse(responseBody.string());
                    JsonObject playlistObject = rootNode.getAsJsonObject();
                    JsonArray songlist = playlistObject.get("items").getAsJsonArray();
                    addSongsToList(songlist, mAnnoyingList);
                }
            }
        });
    }
}
