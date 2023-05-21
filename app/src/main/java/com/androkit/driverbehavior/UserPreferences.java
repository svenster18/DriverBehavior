package com.androkit.driverbehavior;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

public class UserPreferences {

    private final Preferences.Key<String> USER_ID_KEY = PreferencesKeys.stringKey("user_id");
    private final Preferences.Key<Integer> STREAM_ID_KEY = PreferencesKeys.intKey("stream_id");
    private final RxDataStore<Preferences> dataStore;

    private UserPreferences(RxDataStore<Preferences> dataStore) {
        this.dataStore = dataStore;
    }

    private static volatile UserPreferences INSTANCE;

    static UserPreferences getInstance(final RxDataStore<Preferences> dataStore) {
        if (INSTANCE == null) {
            synchronized (UserPreferences.class) {
                if (INSTANCE == null) {
                    INSTANCE = new UserPreferences(dataStore);
                }
            }
        }
        return INSTANCE;
    }

    public Flowable<String> getUserId() {
        return dataStore.data().map(preferences -> {
                    if (preferences.get(USER_ID_KEY) != null) {
                        return preferences.get(USER_ID_KEY);
                    } else {
                        return "";
                    }
                }
        );
    }

    public void saveUserId(String userId) {
        dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(USER_ID_KEY, userId);
            return Single.just(mutablePreferences);
        });
    }

    public Flowable<Integer> getStreamId() {
        return dataStore.data().map(preferences -> {
                    if (preferences.get(STREAM_ID_KEY) != null) {
                        return preferences.get(STREAM_ID_KEY);
                    } else {
                        return 0;
                    }
                }
        );
    }

    public void saveStreamId(int streamId) {
        dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            mutablePreferences.set(STREAM_ID_KEY, streamId);
            return Single.just(mutablePreferences);
        });
    }

}
