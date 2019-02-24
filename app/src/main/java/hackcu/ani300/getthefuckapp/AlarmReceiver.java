package hackcu.ani300.getthefuckapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.PlayerApi;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.CallResult;
import com.spotify.protocol.types.Empty;
import com.spotify.protocol.types.Track;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CLIENT_ID = "4386dbf0a1a44da58fe44ee7119859f4";
    private static final String REDIRECT_URI = "getthefuckapp://callback";

    private SpotifyAppRemote mSpotifyAppRemote;

    private String mSong;
    private Integer mStartingTime;

    @Override
    public void onReceive(Context context, Intent intent) {
        String spotify_token = intent.getStringExtra("spotify_token");
        mSong = intent.getStringExtra("song");
        mStartingTime = intent.getIntExtra("starting_time", 0);

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(false)
                        .build();

        SpotifyAppRemote.connect(context, connectionParams,
                new Connector.ConnectionListener() {

                    @Override
                    public void onConnected(SpotifyAppRemote spotifyAppRemote) {
                        mSpotifyAppRemote = spotifyAppRemote;
                        Log.d("AlarmReceiver", "Connected! Yay!");

                        connected();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        Log.e("AlarmReceiver", throwable.getMessage(), throwable);
                        // Something went wrong when attempting to connect! Handle errors here
                    }
                });

        Toast.makeText(context, "Alarm! Wake up! Wake up!", Toast.LENGTH_LONG).show();
    }

    private void connected() {
        // Then we will write some more code here.
        // Play a playlist
        mSpotifyAppRemote.getPlayerApi().play("spotify:track:" + mSong, PlayerApi.StreamType.ALARM)
                .setResultCallback(new CallResult.ResultCallback<Empty>()
                {
                    @Override
                    public void onResult(Empty empty) {
                        mSpotifyAppRemote.getPlayerApi().seekTo(mStartingTime);
                    }
                }
        );

    }
}
