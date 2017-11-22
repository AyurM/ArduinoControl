package ru.ayurmar.arduinocontrol.db;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.reactivex.Completable;
import io.reactivex.Single;
import ru.ayurmar.arduinocontrol.interfaces.model.IDbHelper;
import ru.ayurmar.arduinocontrol.db.DbSchema.WidgetTable;
import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;

public class SQLiteDbHelper extends SQLiteOpenHelper implements IDbHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "widgets.db";
    private final SQLiteDatabase mDatabase;

    public SQLiteDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        mDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + WidgetTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                WidgetTable.Columns.UUID + ", " +
                WidgetTable.Columns.NAME + ", " +
                WidgetTable.Columns.PIN + ", " +
                WidgetTable.Columns.VALUE + ", " +
                WidgetTable.Columns.DATE + ", " +
                WidgetTable.Columns.TYPE + ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    @Override
    public Completable addWidget(IWidget widget){
        return Completable.fromAction(() -> {
            ContentValues values = getContentValues(widget);
            mDatabase.insert(WidgetTable.NAME, null, values);
        });
    }

    @Override
    public Completable addWidgetList(List<IWidget> widgets){
        return Completable.fromAction(() -> {
            for(IWidget widget : widgets){
                ContentValues values = getContentValues(widget);
                mDatabase.insert(WidgetTable.NAME, null, values);
            }
        });
    }

    @Override
    public Completable deleteWidget(IWidget widget){
        return Completable.fromAction(() -> mDatabase.delete(WidgetTable.NAME,
                WidgetTable.Columns.UUID + " = ?",
                new String[] { widget.getId().toString() }));
    }

    @Override
    public Completable updateWidget(IWidget widget){
        return Completable.fromAction(() -> {
            String id = widget.getId().toString();
            ContentValues values = getContentValues(widget);
            mDatabase.update(WidgetTable.NAME, values,
                    WidgetTable.Columns.UUID + " = ?",
                    new String[] {id});
        });
    }

    @Override
    public Single<List<IWidget>> loadWidgetList(){
        return Single.fromCallable(() -> getWidgetsFromDb());
    }

    @Override
    public Single<IWidget> loadWidget(UUID id){
        return Single.fromCallable(() -> getWidgetFromDb(id));
    }

    private static ContentValues getContentValues(IWidget widget){
        ContentValues values = new ContentValues();
        values.put(WidgetTable.Columns.UUID, widget.getId().toString());
        values.put(WidgetTable.Columns.NAME, widget.getName());
        values.put(WidgetTable.Columns.PIN, widget.getPin());
        values.put(WidgetTable.Columns.VALUE, widget.getValue());
        values.put(WidgetTable.Columns.DATE, widget.getLastUpdateTime().getTime());
        values.put(WidgetTable.Columns.TYPE, widget.getWidgetType().name());
        return values;
    }

    private WidgetCursorWrapper queryWidgets(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                WidgetTable.NAME,
                null, // Columns - null выбирает все столбцы
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );
        return new WidgetCursorWrapper(cursor);
    }

    private List<IWidget> getWidgetsFromDb(){
        List<IWidget> widgets = new ArrayList<>();
        WidgetCursorWrapper cursorWrapper = queryWidgets(null, null);

        cursorWrapper.moveToFirst();
        while (!cursorWrapper.isAfterLast()) {
            widgets.add(cursorWrapper.getWidget());
            cursorWrapper.moveToNext();
        }
        cursorWrapper.close();
        return widgets;
    }

    private IWidget getWidgetFromDb(UUID id){
        List<IWidget> movies = new ArrayList<>();
        WidgetCursorWrapper cursorWrapper =
                queryWidgets(WidgetTable.Columns.UUID + " = ?",
                new String[] {id.toString()});

        cursorWrapper.moveToFirst();
        while (!cursorWrapper.isAfterLast()) {
            movies.add(cursorWrapper.getWidget());
            cursorWrapper.moveToNext();
        }
        cursorWrapper.close();
        return movies.get(0);
    }
}