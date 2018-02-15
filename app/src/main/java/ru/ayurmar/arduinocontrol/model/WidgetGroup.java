package ru.ayurmar.arduinocontrol.model;

import java.util.ArrayList;
import java.util.List;

public class WidgetGroup {
    private List<AlarmWidget> mAlarmWidgets;
    private List<InfoWidget> mInfoWidgets;
    private List<SwitchWidget> mSwitchWidgets;

    public WidgetGroup(){
        mAlarmWidgets = new ArrayList<>();
        mInfoWidgets = new ArrayList<>();
        mSwitchWidgets = new ArrayList<>();
    }

    public void addAlarmWidget(AlarmWidget widget){
        mAlarmWidgets.add(widget);
    }

    public void addInfoWidget(InfoWidget widget){
        mInfoWidgets.add(widget);
    }

    public void addSwitchWidget(SwitchWidget widget){
        mSwitchWidgets.add(widget);
    }

    public void updateWidget(FarhomeWidget widget){
        List<? extends FarhomeWidget> list = new ArrayList<>();
        if(widget instanceof AlarmWidget){
            list = mAlarmWidgets;
        } else if(widget instanceof InfoWidget){
            list = mInfoWidgets;
        } else if(widget instanceof SwitchWidget){
            list = mSwitchWidgets;
        }
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).getDbkey().equals(widget.getDbkey())){
                if(widget instanceof AlarmWidget){
                    mAlarmWidgets.set(i, (AlarmWidget) widget);
                } else if(widget instanceof InfoWidget){
                    mInfoWidgets.set(i, (InfoWidget) widget);
                } else if(widget instanceof SwitchWidget){
                    mSwitchWidgets.set(i, (SwitchWidget) widget);
                }
            }
        }
    }

    public int getAlarmWidgetsCount(){
        return mAlarmWidgets.size();
    }

    public int getInfoWidgetsCount(){
        return mInfoWidgets.size();
    }

    public int getSwitchWidgetsCount(){
        return mSwitchWidgets.size();
    }

    public int getWidgetsCount(){
        return (getAlarmWidgetsCount() + getInfoWidgetsCount() + getSwitchWidgetsCount());
    }

    public List<AlarmWidget> getAlarmWidgets() {
        return mAlarmWidgets;
    }

    public void setAlarmWidgets(List<AlarmWidget> alarmWidgets) {
        mAlarmWidgets = alarmWidgets;
    }

    public List<InfoWidget> getInfoWidgets() {
        return mInfoWidgets;
    }

    public void setInfoWidgets(List<InfoWidget> infoWidgets) {
        mInfoWidgets = infoWidgets;
    }

    public List<SwitchWidget> getSwitchWidgets() {
        return mSwitchWidgets;
    }

    public List<FarhomeWidget> getWidgets(){
        List<FarhomeWidget> result = new ArrayList<>();
        result.addAll(mAlarmWidgets);
        result.addAll(mInfoWidgets);
        result.addAll(mSwitchWidgets);
        return result;
    }

    public boolean contains(FarhomeWidget widget){
        return getWidgets().contains(widget);
    }

    public void setSwitchWidgets(List<SwitchWidget> switchWidgets) {
        mSwitchWidgets = switchWidgets;
    }

    public void clear(){
        mAlarmWidgets.clear();
        mInfoWidgets.clear();
        mSwitchWidgets.clear();
    }
}
