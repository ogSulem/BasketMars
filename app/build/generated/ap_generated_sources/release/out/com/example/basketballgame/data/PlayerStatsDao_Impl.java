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
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class PlayerStatsDao_Impl implements PlayerStatsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PlayerStats> __insertionAdapterOfPlayerStats;

  public PlayerStatsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPlayerStats = new EntityInsertionAdapter<PlayerStats>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `player_stats` (`id`,`totalGames`,`arcadeBest`,`timedBest`,`duelWins`,`duelLosses`,`updatedAt`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final PlayerStats entity) {
        statement.bindLong(1, entity.id);
        statement.bindLong(2, entity.totalGames);
        statement.bindLong(3, entity.arcadeBest);
        statement.bindLong(4, entity.timedBest);
        statement.bindLong(5, entity.duelWins);
        statement.bindLong(6, entity.duelLosses);
        statement.bindLong(7, entity.updatedAt);
      }
    };
  }

  @Override
  public void save(final PlayerStats stats) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfPlayerStats.insert(stats);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public PlayerStats getStats() {
    final String _sql = "SELECT * FROM player_stats WHERE id = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfTotalGames = CursorUtil.getColumnIndexOrThrow(_cursor, "totalGames");
      final int _cursorIndexOfArcadeBest = CursorUtil.getColumnIndexOrThrow(_cursor, "arcadeBest");
      final int _cursorIndexOfTimedBest = CursorUtil.getColumnIndexOrThrow(_cursor, "timedBest");
      final int _cursorIndexOfDuelWins = CursorUtil.getColumnIndexOrThrow(_cursor, "duelWins");
      final int _cursorIndexOfDuelLosses = CursorUtil.getColumnIndexOrThrow(_cursor, "duelLosses");
      final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
      final PlayerStats _result;
      if (_cursor.moveToFirst()) {
        _result = new PlayerStats();
        _result.id = _cursor.getLong(_cursorIndexOfId);
        _result.totalGames = _cursor.getInt(_cursorIndexOfTotalGames);
        _result.arcadeBest = _cursor.getInt(_cursorIndexOfArcadeBest);
        _result.timedBest = _cursor.getInt(_cursorIndexOfTimedBest);
        _result.duelWins = _cursor.getInt(_cursorIndexOfDuelWins);
        _result.duelLosses = _cursor.getInt(_cursorIndexOfDuelLosses);
        _result.updatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
      } else {
        _result = null;
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
