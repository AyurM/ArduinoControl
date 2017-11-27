package ru.ayurmar.arduinocontrol.db;


import android.database.Cursor;
import android.database.CursorWrapper;

import ru.ayurmar.arduinocontrol.interfaces.model.IWidget;
import ru.ayurmar.arduinocontrol.model.BlynkWidget;
import ru.ayurmar.arduinocontrol.db.DbSchema.WidgetTable;
import ru.ayurmar.arduinocontrol.model.WidgetType;

class WidgetCursorWrapper extends CursorWrapper {

    WidgetCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public IWidget getWidget() {
        String uuid = getString(getColumnIndex(WidgetTable.Columns.UUID));
        String name = getString(getColumnIndex(WidgetTable.Columns.NAME));
        String pin = getString(getColumnIndex(WidgetTable.Columns.PIN));
        String value = getString(getColumnIndex(WidgetTable.Columns.VALUE));
        long date = getLong(getColumnIndex(WidgetTable.Columns.DATE));
        String type = getString(getColumnIndex(WidgetTable.Columns.TYPE));

        return new BlynkWidget(name, pin, value, WidgetType.valueOf(type),
                uuid, date);
    }
}