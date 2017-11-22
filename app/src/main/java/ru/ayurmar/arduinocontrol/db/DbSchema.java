package ru.ayurmar.arduinocontrol.db;


class DbSchema {

     static final class WidgetTable{
         static final String NAME = "widgets";

         static final class Columns{
             static final String UUID = "uuid";
             static final String NAME = "name";
             static final String PIN = "pin";
             static final String VALUE = "value";
             static final String DATE = "date";
             static final String TYPE = "type";
        }
    }
}
