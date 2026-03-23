package com.example.basketballgame.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile LeaderboardDao _leaderboardDao;

  private volatile PlayerStatsDao _playerStatsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `leaderboard_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `mode` TEXT, `score` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `playerName` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `player_stats` (`id` INTEGER NOT NULL, `totalGames` INTEGER NOT NULL, `arcadeBest` INTEGER NOT NULL, `timedBest` INTEGER NOT NULL, `duelWins` INTEGER NOT NULL, `duelLosses` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '4c9cb0a8b429ce2349783584ca19fef4')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `leaderboard_entries`");
        db.execSQL("DROP TABLE IF EXISTS `player_stats`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsLeaderboardEntries = new HashMap<String, TableInfo.Column>(5);
        _columnsLeaderboardEntries.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaderboardEntries.put("mode", new TableInfo.Column("mode", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaderboardEntries.put("score", new TableInfo.Column("score", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaderboardEntries.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsLeaderboardEntries.put("playerName", new TableInfo.Column("playerName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysLeaderboardEntries = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesLeaderboardEntries = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoLeaderboardEntries = new TableInfo("leaderboard_entries", _columnsLeaderboardEntries, _foreignKeysLeaderboardEntries, _indicesLeaderboardEntries);
        final TableInfo _existingLeaderboardEntries = TableInfo.read(db, "leaderboard_entries");
        if (!_infoLeaderboardEntries.equals(_existingLeaderboardEntries)) {
          return new RoomOpenHelper.ValidationResult(false, "leaderboard_entries(com.example.basketballgame.data.LeaderboardEntry).\n"
                  + " Expected:\n" + _infoLeaderboardEntries + "\n"
                  + " Found:\n" + _existingLeaderboardEntries);
        }
        final HashMap<String, TableInfo.Column> _columnsPlayerStats = new HashMap<String, TableInfo.Column>(7);
        _columnsPlayerStats.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("totalGames", new TableInfo.Column("totalGames", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("arcadeBest", new TableInfo.Column("arcadeBest", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("timedBest", new TableInfo.Column("timedBest", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("duelWins", new TableInfo.Column("duelWins", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("duelLosses", new TableInfo.Column("duelLosses", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPlayerStats.put("updatedAt", new TableInfo.Column("updatedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPlayerStats = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPlayerStats = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPlayerStats = new TableInfo("player_stats", _columnsPlayerStats, _foreignKeysPlayerStats, _indicesPlayerStats);
        final TableInfo _existingPlayerStats = TableInfo.read(db, "player_stats");
        if (!_infoPlayerStats.equals(_existingPlayerStats)) {
          return new RoomOpenHelper.ValidationResult(false, "player_stats(com.example.basketballgame.data.PlayerStats).\n"
                  + " Expected:\n" + _infoPlayerStats + "\n"
                  + " Found:\n" + _existingPlayerStats);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "4c9cb0a8b429ce2349783584ca19fef4", "c7601de70413c4643bd9ba06480454a9");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "leaderboard_entries","player_stats");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `leaderboard_entries`");
      _db.execSQL("DELETE FROM `player_stats`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(LeaderboardDao.class, LeaderboardDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PlayerStatsDao.class, PlayerStatsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public LeaderboardDao leaderboardDao() {
    if (_leaderboardDao != null) {
      return _leaderboardDao;
    } else {
      synchronized(this) {
        if(_leaderboardDao == null) {
          _leaderboardDao = new LeaderboardDao_Impl(this);
        }
        return _leaderboardDao;
      }
    }
  }

  @Override
  public PlayerStatsDao playerStatsDao() {
    if (_playerStatsDao != null) {
      return _playerStatsDao;
    } else {
      synchronized(this) {
        if(_playerStatsDao == null) {
          _playerStatsDao = new PlayerStatsDao_Impl(this);
        }
        return _playerStatsDao;
      }
    }
  }
}
