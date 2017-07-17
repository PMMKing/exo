package com.haolb.client.utils.inject;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;


import com.haolb.client.app.MainApplication;
import com.haolb.client.utils.QArrays;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Injector {

    private static final Class<?>[] HALT_CLASSES = { Activity.class, Fragment.class, View.class, Object.class };

    public static void inject(Object container) {
        inject(container, container, false);
    }

    public static void inject(Object container, boolean ignore) {
        inject(container, container, ignore);
    }

    public static void inject(Object container, Object parent) {
        inject(container, parent, false);
    }

    public static void inject(Object container, Object parent, boolean ignore) {
        Class<?> clazz = container.getClass();
        while (!QArrays.contains(HALT_CLASSES, clazz)) {
            Field[] fields = clazz.getDeclaredFields();
            clazz = clazz.getSuperclass();
            for (Field field : fields) {
                if (field.isAnnotationPresent(From.class)) {
                    From from = field.getAnnotation(From.class);
                    try {
                        int id = from.value();
                        field.setAccessible(true);
                        View view = inflateView(parent, id);
                        if (!ignore && !from.canBeNull() && view == null) {
                            throwIdException(id);
                        } else if (view != null) {
                            field.set(container, view);
                        }
                    } catch (Exception e) {
                        throw new InjectException(e.getMessage(), e);
                    }
                } else if (field.isAnnotationPresent(FromArray.class)) {
                    FromArray fromArray = field.getAnnotation(FromArray.class);
                    try {
                        int[] ids = fromArray.value();
                        field.setAccessible(true);
                        Class<?> componentType = field.getType().getComponentType();
                        if (componentType != null) {
                            Object grown = Array.newInstance(componentType, ids.length);
                            ArrayList<View> tempArray = new ArrayList<View>(ids.length);
                            View view;
                            for (int id : ids) {
                                view = inflateView(parent, id);
                                if (!ignore && !fromArray.canBeNull() && view == null) {
                                    throwIdException(id);
                                }
                                tempArray.add(view);
                            }
                            System.arraycopy(tempArray.toArray(), 0, grown, 0, ids.length);
                            field.set(container, grown);
                        }
                    } catch (Exception e) {
                        throw new InjectException(e.getMessage(), e);
                    }
                }
            }

        }
    }

    private static void throwIdException(int id) {
        throw new InjectException("field '" + MainApplication.getInstance().getResources().getResourceName(id) + ":"
                + Integer.toHexString(id) + "' not injected! Check your settings or layout xml id value!");
    }

    private static View inflateView(Object parent, int id) {
        View view;
        view = null;
        if (parent instanceof Activity) {
            view = ((Activity) parent).findViewById(id);
        } else if (parent instanceof Fragment) {
            view = ((Fragment) parent).getView().findViewById(id);
        } else if (parent instanceof View) {
            view = ((View) parent).findViewById(id);
        }
        return view;
    }

    private static void setViewClickListener(View view, Object parent) {
        try {
            if (parent instanceof OnClickListener) {
                view.setOnClickListener((OnClickListener) parent);
            }
        } catch (Exception e) {
            throw new InjectException("setViewClickListener error", e);
        }
    }

    private static void setViewLongClickListener(View view, Object parent) {
        try {
            if (parent instanceof OnLongClickListener) {
                view.setOnLongClickListener((OnLongClickListener) parent);
            }
        } catch (Exception e) {
            throw new InjectException("setViewLongClickListener error", e);
        }
    }

    private static void setItemClickListener(View view, Object parent) {
        try {
            if (view instanceof AbsListView && parent instanceof OnItemClickListener) {
                ((AbsListView) view).setOnItemClickListener((OnItemClickListener) parent);
            }
        } catch (Exception e) {
            throw new InjectException("setItemClickListener error", e);
        }
    }

    private static void setItemLongClickListener(View view, Object parent) {
        try {
            if (view instanceof AbsListView && parent instanceof OnItemLongClickListener) {
                ((AbsListView) view).setOnItemLongClickListener((OnItemLongClickListener) parent);
            }
        } catch (Exception e) {
            throw new InjectException("setItemLongClickListener error", e);
        }
    }

}
