package net.osmand.plus.quickaction;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.osmand.plus.OsmandSettings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rosty on 12/27/16.
 */

public class QuickActionRegistry {

    public interface QuickActionUpdatesListener{

        void onActionsUpdated();
    }

    private final QuickActionFactory factory;
    private final OsmandSettings settings;

    private final List<QuickAction> quickActions;
    private final Map<String, Boolean> fabStateMap;

    private QuickActionUpdatesListener updatesListener;

    public QuickActionRegistry(OsmandSettings settings) {

        this.factory = new QuickActionFactory();
        this.settings = settings;

        quickActions = factory.parseActiveActionsList(settings.QUICK_ACTION_LIST.get());
        fabStateMap = getQuickActionFabStateMapFromJson(settings.QUICK_ACTION.get());
    }

    public void setUpdatesListener(QuickActionUpdatesListener updatesListener) {
        this.updatesListener = updatesListener;
    }

    public void notifyUpdates() {
        if (updatesListener != null) updatesListener.onActionsUpdated();
    }

    public List<QuickAction> getQuickActions() {

        ArrayList<QuickAction> actions = new ArrayList<>();
        actions.addAll(quickActions);

        return actions;
    }

    public void addQuickAction(QuickAction action){

        quickActions.add(action);

        settings.QUICK_ACTION_LIST.set(factory.quickActionListToString(quickActions));
    }

    public void deleteQuickAction(QuickAction action){

        int index = quickActions.indexOf(action);

        if (index >= 0) quickActions.remove(index);

        settings.QUICK_ACTION_LIST.set(factory.quickActionListToString(quickActions));
    }

    public void deleteQuickAction(int id){

        int index = -1;

        for (QuickAction action: quickActions){

            if (action.id == id)
                index = quickActions.indexOf(action);
        }

        if (index >= 0) quickActions.remove(index);

        settings.QUICK_ACTION_LIST.set(factory.quickActionListToString(quickActions));
    }

    public void updateQuickAction(QuickAction action){

        int index = quickActions.indexOf(action);

        if (index >= 0) quickActions.set(index, action);

        settings.QUICK_ACTION_LIST.set(factory.quickActionListToString(quickActions));
    }

    public void updateQuickActions(List<QuickAction>  quickActions){

        this.quickActions.clear();
        this.quickActions.addAll(quickActions);

        settings.QUICK_ACTION_LIST.set(factory.quickActionListToString(this.quickActions));
    }

    public QuickAction getQuickAction(long id){

        for (QuickAction action: quickActions){

            if (action.id == id) return action;
        }

        return null;
    }

    public boolean isNameUnique(QuickAction action, Context context){


        for (QuickAction a: quickActions){

            if (action.id != a.id) {

                if (action.getName(context).equals(a.getName(context)))
                    return false;
            }
        }

        return true;
    }

    public QuickAction generateUniqueName(QuickAction action, Context context) {

        int number = 0;
        String name = action.getName(context);

        while (true) {

            number++;

            action.setName(name + " (" + number + ")");

            if (isNameUnique(action, context)) return action;
        }
    }

    public boolean isQuickActionOn() {
        Boolean result = fabStateMap.get(settings.APPLICATION_MODE.get().getStringKey());
        return result != null && result;
    }

    public void setQuickActionFabState(boolean isOn) {
        fabStateMap.put(settings.APPLICATION_MODE.get().getStringKey(), isOn);
        settings.QUICK_ACTION.set(new Gson().toJson(fabStateMap));
    }

    private Map<String, Boolean> getQuickActionFabStateMapFromJson(String json) {
        Type type = new TypeToken<HashMap<String, Boolean>>() {
        }.getType();
        HashMap<String, Boolean> quickActions = new Gson().fromJson(json, type);

        return quickActions != null ? quickActions : new HashMap<String, Boolean>();
    }
}
