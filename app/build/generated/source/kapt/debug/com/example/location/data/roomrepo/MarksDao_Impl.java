package com.example.location.data.roomrepo;

import android.database.Cursor;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class MarksDao_Impl implements MarksDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<MarkEntity> __insertionAdapterOfMarkEntity;

  private final SharedSQLiteStatement __preparedStmtOfDeleteMark;

  private final SharedSQLiteStatement __preparedStmtOfRemoveAllMarks;

  public MarksDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfMarkEntity = new EntityInsertionAdapter<MarkEntity>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `marksTable` (`id`,`coordinateLong`,`coordinateLat`,`photoFileName`) VALUES (?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, MarkEntity value) {
        stmt.bindLong(1, value.getId());
        stmt.bindDouble(2, value.getCoordinateLong());
        stmt.bindDouble(3, value.getCoordinateLat());
        if (value.getPhotoFileName() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getPhotoFileName());
        }
      }
    };
    this.__preparedStmtOfDeleteMark = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM marksTable WHERE id=?";
        return _query;
      }
    };
    this.__preparedStmtOfRemoveAllMarks = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM marksTable";
        return _query;
      }
    };
  }

  @Override
  public Object insertMark(final MarkEntity cityInfoWeather,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfMarkEntity.insert(cityInfoWeather);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteMark(final int id, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteMark.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, id);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfDeleteMark.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object removeAllMarks(final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfRemoveAllMarks.acquire();
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfRemoveAllMarks.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Flow<List<MarkEntity>> getAllMarks() {
    final String _sql = "SELECT * FROM marksTable ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"marksTable"}, new Callable<List<MarkEntity>>() {
      @Override
      public List<MarkEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCoordinateLong = CursorUtil.getColumnIndexOrThrow(_cursor, "coordinateLong");
          final int _cursorIndexOfCoordinateLat = CursorUtil.getColumnIndexOrThrow(_cursor, "coordinateLat");
          final int _cursorIndexOfPhotoFileName = CursorUtil.getColumnIndexOrThrow(_cursor, "photoFileName");
          final List<MarkEntity> _result = new ArrayList<MarkEntity>(_cursor.getCount());
          while(_cursor.moveToNext()) {
            final MarkEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final double _tmpCoordinateLong;
            _tmpCoordinateLong = _cursor.getDouble(_cursorIndexOfCoordinateLong);
            final double _tmpCoordinateLat;
            _tmpCoordinateLat = _cursor.getDouble(_cursorIndexOfCoordinateLat);
            final String _tmpPhotoFileName;
            if (_cursor.isNull(_cursorIndexOfPhotoFileName)) {
              _tmpPhotoFileName = null;
            } else {
              _tmpPhotoFileName = _cursor.getString(_cursorIndexOfPhotoFileName);
            }
            _item = new MarkEntity(_tmpId,_tmpCoordinateLong,_tmpCoordinateLat,_tmpPhotoFileName);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
