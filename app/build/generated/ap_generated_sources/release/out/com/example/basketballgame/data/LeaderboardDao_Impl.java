package com.example.basketballgame.data;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class LeaderboardDao_Impl implements LeaderboardDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<LeaderboardEntry> __insertionAdapterOfLeaderboardEntry;

  public LeaderboardDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfLeaderboardEntry = new EntityInsertionAdapter<LeaderboardEntry>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `leaderboard_entries` (`id`,`mode`,`score`,`timestamp`,`playerName`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final LeaderboardEntry entity) {
        statement.bindLong(1, entity.id);
        if (entity.mode == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.mode);
        }
        statement.bindLong(3, entity.score);
        statement.bindLong(4, entity.timestamp);
        if (entity.playerName == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.playerName);
        }
      }
    };
  }

  @Override
  public long insert(final LeaderboardEntry entry) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      final long _result = __insertionAdapterOfLeaderboardEntry.insertAndReturnId(entry);
      __db.setTransactionSuccessful();
      return _result;
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<LeaderboardEntry> getTop(final String mode, final int limit) {
    final String _sql = "SELECT * FROM leaderboard_entries WHERE mode = ? ORDER BY score DESC, timestamp ASC LIMIT ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    if (mode == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, mode);
    }
    _argIndex = 2;
    _statement.bindLong(_argIndex, limit);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfMode = CursorUtil.getColumnIndexOrThrow(_cursor, "mode");
      final int _cursorIndexOfScore = CursorUtil.getColumnIndexOrThrow(_cursor, "score");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final int _cursorIndexOfPlayerName = CursorUtil.getColumnIndexOrThrow(_cursor, "playerName");
      final List<LeaderboardEntry> _result = new ArrayList<LeaderboardEntry>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final LeaderboardEntry _item;
        _item = new LeaderboardEntry();
        _item.id = _cursor.getLong(_cursorIndexOfId);
        if (_cursor.isNull(_cursorIndexOfMode)) {
          _item.mode = null;
        } else {
          _item.mode = _cursor.getString(_cursorIndexOfMode);
        }
        _item.score = _cursor.getInt(_cursorIndexOfScore);
        _item.timestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        if (_cursor.isNull(_cursorIndexOfPlayerName)) {
          _item.playerName = null;
        } else {
          _item.playerName = _cursor.getString(_cursorIndexOfPlayerName);
        }
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
