package cpen221.mp3.server;

import cpen221.mp3.event.ActuatorEvent;
import cpen221.mp3.event.Event;

import java.util.ArrayList;
import java.util.List;

import cpen221.mp3.server.*;

public class Filter {
    /**
     * Fields for this class
     **/
    private BooleanOperator boolOperator;
    private DoubleOperator doubleOperator;
    private boolean boolValue;
    private double doubleValue;
    private String field;
    private boolean isBasicFilter;
    private boolean isBooleanFilter;        // Filter for boolean values, otherwise is double filter
    private List<Filter> compositeFilters = new ArrayList<>();
    private List<Filter> basicFilters = new ArrayList<>();

    /**
     * Constructs a filter that compares the boolean (actuator) event value
     * to the given boolean value using the given BooleanOperator.
     * (X (BooleanOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A BooleanOperator can be one of the following:
     * <p>
     * BooleanOperator.EQUALS
     * BooleanOperator.NOT_EQUALS
     *
     * @param operator the BooleanOperator to use to compare the event value with the given value
     * @param value    the boolean value to match
     */
    public Filter(BooleanOperator operator, boolean value) {
        this.boolOperator = operator;
        this.boolValue = value;
        this.isBasicFilter = true;
        this.basicFilters.add(this);
        this.isBooleanFilter = true;
    }

    /**
     * Constructs a filter that compares a double field in events
     * with the given double value using the given DoubleOperator.
     * (X (DoubleOperator) value), where X is the event's value passed by satisfies or sift methods.
     * A DoubleOperator can be one of the following:
     * <p>
     * DoubleOperator.EQUALS
     * DoubleOperator.GREATER_THAN
     * DoubleOperator.LESS_THAN
     * DoubleOperator.GREATER_THAN_OR_EQUALS
     * DoubleOperator.LESS_THAN_OR_EQUALS
     * <p>
     * For non-double (boolean) value events, the satisfies method should return false.
     *
     * @param field    the field to match (event "value" or event "timestamp")
     * @param operator the DoubleOperator to use to compare the event value with the given value
     * @param value    the double value to match
     * @throws IllegalArgumentException if the given field is not "value" or "timestamp"
     */
    public Filter(String field, DoubleOperator operator, double value) throws IllegalArgumentException {

        if (!field.equals("value") && !field.equals("timestamp")) {
            throw new IllegalArgumentException();
        }
        this.basicFilters.add(this);
        this.doubleOperator = operator;
        this.field = field;
        this.doubleValue = value;
        this.isBasicFilter = true;
        this.isBooleanFilter = false;
    }

    /**
     * A filter can be composed of other filters.
     * in this case, the filter should satisfy all the filters in the list.
     * Constructs a complex filter composed of other filters.
     *
     * @param filters the list of filters to use in the composition
     */
    public Filter(List<Filter> filters) {
        this.compositeFilters.addAll(filters);
        decompose(getCompositeFilters());
        this.isBasicFilter = false;
        this.isBooleanFilter = true;
        for (Filter f : this.basicFilters) {
            if (!f.isBooleanFilter)
                this.isBooleanFilter = false;
        }
    }

    /**
     * Strips down every complex filter until they reach their basic form.
     * i.e. a basic Filter is a filter constructed with a BooleanOperator.
     *
     * @param complexFilter List of complex filters and basic filters.
     */
    private void decompose(List<Filter> complexFilter) {
        for (Filter f : complexFilter) {
            if (f.isBasic()) {
                this.basicFilters.add(f);
            } else {
                decompose(f.getCompositeFilters());
            }
        }
    }

    /**
     * @return true if the Filter is a basic filter, false otherwise.
     */
    private boolean isBasic() {
        return this.isBasicFilter;
    }

    /**
     * @return list of the Filter composed of complex Filters.
     */
    private List<Filter> getCompositeFilters() {
        return this.compositeFilters;
    }

    /**
     * Returns true if the given event satisfies the filter criteria.
     *
     * @param event the event to check
     * @return true if the event satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(Event event) {
        for (Filter f : this.basicFilters) {
            if (f.boolOperator != null) {
                switch (f.boolOperator) {
                    case EQUALS: {
                        if (event.getValueBoolean() != f.boolValue) {
                            return false;
                        }
                        break;
                    }
                    case NOT_EQUALS: {
                        if (event.getValueBoolean() == f.boolValue) {
                            return false;
                        }
                        break;
                    }
                    default:
                        return false;
                }
            } else {
                switch (f.doubleOperator) {
                    case EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() != f.doubleValue) {
                            return false;
                        } else if (field.equals("timestamp") && event.getTimeStamp() != f.doubleValue) {
                            return false;
                        }
                        break;
                    }
                    case GREATER_THAN: {
                        if (f.field.equals("value") && event.getValueDouble() <= f.doubleValue) {
                            return false;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() <= f.doubleValue) {
                            return false;
                        }
                        break;
                    }
                    case GREATER_THAN_OR_EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() < f.doubleValue) {
                            return false;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() < f.doubleValue) {
                            return false;
                        }
                        break;
                    }
                    case LESS_THAN: {
                        if (f.field.equals("value") && event.getValueDouble() >= f.doubleValue) {
                            return false;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() >= f.doubleValue) {
                            return false;
                        }
                        break;
                    }
                    case LESS_THAN_OR_EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() > f.doubleValue) {
                            return false;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() > f.doubleValue) {
                            return false;
                        }
                        break;
                    }
                    default:
                        return false;
                }
            }

        }
        return true;
    }

    /**
     * Returns true if the given list of events satisfies the filter criteria.
     *
     * @param events the list of events to check
     * @return true if every event in the list satisfies the filter criteria, false otherwise
     */
    public boolean satisfies(List<Event> events) {
        for (Filter f : this.basicFilters) {
            if (f.boolOperator != null) {
                switch (f.boolOperator) {
                    case EQUALS: {
                        for (Event e : events) {
                            if (e.getValueBoolean() != f.boolValue) {
                                return false;
                            }
                        }
                        break;
                    }
                    case NOT_EQUALS: {
                        for (Event e : events) {
                            if (e.getValueBoolean() == f.boolValue) {
                                return false;
                            }
                        }
                        break;
                    }
                    default:
                        return false;
                }
            } else {
                switch (f.doubleOperator) {
                    case EQUALS: {
                        for (Event e : events) {
                            if (e.getValueDouble() != f.doubleValue && f.field.equals("value")) {
                                return false;
                            }
                            if (e.getTimeStamp() != f.doubleValue && f.field.equals("timestamp")) {
                                return false;
                            }
                        }
                        break;
                    }
                    case GREATER_THAN: {
                        for (Event e : events) {
                            if (e.getValueDouble() <= f.doubleValue && f.field.equals("value")) {
                                return false;
                            }
                            if (e.getTimeStamp() <= f.doubleValue && f.field.equals("timestamp")) {
                                return false;
                            }
                        }
                        break;
                    }
                    case GREATER_THAN_OR_EQUALS: {
                        for (Event e : events) {
                            if (e.getValueDouble() < f.doubleValue && f.field.equals("value")) {
                                return false;
                            }
                            if (e.getTimeStamp() < f.doubleValue && f.field.equals("timestamp")) {
                                return false;
                            }
                        }
                        break;
                    }
                    case LESS_THAN: {
                        for (Event e : events) {
                            if (e.getValueDouble() >= f.doubleValue && f.field.equals("value")) {
                                return false;
                            }
                            if (e.getTimeStamp() >= f.doubleValue && f.field.equals("timestamp")) {
                                return false;
                            }
                        }
                        break;
                    }
                    case LESS_THAN_OR_EQUALS: {
                        for (Event e : events) {
                            if (e.getValueDouble() > f.doubleValue && f.field.equals("value")) {
                                return false;
                            }
                            if (e.getTimeStamp() > f.doubleValue && f.field.equals("timestamp")) {
                                return false;
                            }
                        }
                        break;
                    }
                    default:
                        return false;
                }
            }
        }
        // if everything runs this return statement should be hit
        return true;
    }

    /**
     * Returns a new event if it satisfies the filter criteria.
     * If the given event does not satisfy the filter criteria, then this method should return null.
     *
     * @param event the event to sift
     * @return a new event if it satisfies the filter criteria, null otherwise
     */
    public Event sift(Event event) {
        for (Filter f : this.basicFilters) {
            if (f.boolOperator != null) {
                switch (f.boolOperator) {
                    case EQUALS: {
                        if (event.getValueBoolean() != f.boolValue) {
                            return null;
                        }
                        break;
                    }
                    case NOT_EQUALS: {
                        if (event.getValueBoolean() == f.boolValue) {
                            return null;
                        }
                        break;
                    }
                    default:
                        return null;
                }
            } else {
                switch (f.doubleOperator) {
                    case EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() != f.doubleValue) {
                            return null;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() != f.doubleValue) {
                            return null;
                        }
                        break;
                    }
                    case GREATER_THAN: {
                        if (f.field.equals("value") && event.getValueDouble() <= f.doubleValue) {
                            return null;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() <= f.doubleValue) {
                            return null;
                        }
                        break;
                    }
                    case GREATER_THAN_OR_EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() < f.doubleValue) {
                            return null;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() < f.doubleValue) {
                            return null;
                        }
                        break;
                    }
                    case LESS_THAN: {
                        if (f.field.equals("value") && event.getValueDouble() >= f.doubleValue) {
                            return null;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() >= f.doubleValue) {
                            return null;
                        }
                        break;
                    }
                    case LESS_THAN_OR_EQUALS: {
                        if (f.field.equals("value") && event.getValueDouble() > f.doubleValue) {
                            return null;
                        } else if (f.field.equals("timestamp") && event.getTimeStamp() > f.doubleValue) {
                            return null;
                        }
                        break;
                    }
                    default:
                        return null;
                }
            }

        }
        return event;
    }

    /**
     * Returns a list of events that contains only the events in the given list that satisfy the filter criteria.
     * If no events in the given list satisfy the filter criteria, then this method should return an empty list.
     *
     * @param events the list of events to sift
     * @return a list of events that contains only the events in the given list that satisfy the filter criteria
     * or an empty list if no events in the given list satisfy the filter criteria
     */
    public List<Event> sift(List<Event> events) {

        List<Event> eventsPassed = new ArrayList<>();
        List<Boolean> eventsFail = new ArrayList<>();
        for (int i = 0; i < events.size(); i++) {
            eventsFail.add(true);
        }
        int count = 0;
        for (Filter f : this.basicFilters) {
            if (f.boolOperator != null) {
                switch (f.boolOperator) {
                    case EQUALS: {
                        for (Event e : events) {
                            if (e.getValueBoolean() != f.boolValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                    case NOT_EQUALS: {
                        for (Event e : events) {
                            if (e.getValueBoolean() == f.boolValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                }
            } else {
                switch (f.doubleOperator) {
                    case EQUALS: {
                        for (Event e : events) {
                            if (f.field.equals("value") && e.getValueDouble() != f.doubleValue) {
                                eventsFail.set(count, false);
                            } else if (f.field.equals("timestamp") && e.getTimeStamp() != f.doubleValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                    case GREATER_THAN: {
                        for (Event e : events) {
                            if (f.field.equals("value") && e.getValueDouble() <= f.doubleValue) {
                                eventsFail.set(count, false);
                            } else if (f.field.equals("timestamp") && e.getTimeStamp() <= f.doubleValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                    case GREATER_THAN_OR_EQUALS: {
                        for (Event e : events) {
                            if (f.field.equals("value") && e.getValueDouble() < f.doubleValue) {
                                eventsFail.set(count, false);
                            } else if (f.field.equals("timestamp") && e.getTimeStamp() < f.doubleValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                    case LESS_THAN: {
                        for (Event e : events) {
                            if (f.field.equals("value") && e.getValueDouble() >= f.doubleValue) {
                                eventsFail.set(count, false);
                            } else if (f.field.equals("timestamp") && e.getTimeStamp() >= f.doubleValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                    case LESS_THAN_OR_EQUALS: {
                        for (Event e : events) {
                            if (f.field.equals("value") && e.getValueDouble() > f.doubleValue) {
                                eventsFail.set(count, false);
                            } else if (f.field.equals("timestamp") && e.getTimeStamp() > f.doubleValue) {
                                eventsFail.set(count, false);
                            }
                        }
                        break;
                    }
                }
            }
            count++;
        }
        int count2 = 0;
        for (boolean b : eventsFail) {
            if (b) {
                eventsPassed.add(events.get(count2));
            }
            count2++;
        }
        return eventsPassed;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Filter:");
        for (Filter filter : this.basicFilters) {
            if (filter.isBooleanFilter) {
                switch (filter.boolOperator) {
                    case EQUALS:
                        stringBuilder.append( "==x" + filter.boolValue+"&");
                        break;
                    case NOT_EQUALS:
                        stringBuilder.append( "!=x" + filter.boolValue+"&");
                        break;
                    default:
                }
            } else {
                switch (filter.doubleOperator) {
                    case EQUALS: stringBuilder.append("=x" + filter.doubleValue +"x" + filter.field + "&"); break;
                    case LESS_THAN: stringBuilder.append("<x" + filter.doubleValue +"x" + filter.field + "&"); break;
                    case GREATER_THAN: stringBuilder.append(">x" + filter.doubleValue +"x" + filter.field + "&"); break;
                    case LESS_THAN_OR_EQUALS: stringBuilder.append("<=x" + filter.doubleValue +"x" + filter.field + "&"); break;
                    case GREATER_THAN_OR_EQUALS: stringBuilder.append(">=x" + filter.doubleValue +"x" + filter.field + "&"); break;
                    default:
                }
            }
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return stringBuilder.toString();
    }


    public static void main(String[] args) {
        Filter sensorValueFilter = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        System.out.println(sensorValueFilter.toString());
        Filter f1 = new Filter("value", DoubleOperator.GREATER_THAN_OR_EQUALS, 23);
        Filter f2 = new Filter("timestamp", DoubleOperator.LESS_THAN, 1);
        List<Filter> filterList = new ArrayList<>();
        filterList.add(f1);
        filterList.add(f2);
        Filter complexFilter = new Filter(filterList);
        //System.out.println(complexFilter.toString());
        /*System.out.println(Filter.toFilters("Filter:>=x1000.0xtimestamp"));

        Event sentEvent = new ActuatorEvent(System.currentTimeMillis(), 1, 1, "Switch", true);
        Filter f3 = new Filter("timestamp", GREATER_THAN_OR_EQUALS, 1000);
        System.out.println(f3.satisfies(sentEvent));*/
        String filterStr = complexFilter.toString();
        List<Filter> list = new ArrayList<>();
        String[] filters = (filterStr.split(":")[1]).split("&");

        for (String s : filters)
            System.out.println(s);
        System.out.println(filters.length);
        for (int i = 0 ; i < filters.length; i++) {
            String [] split = filters[i].split("x");
            String quantifier = split[0];
            String value = split[1];
            if (quantifier.equals("=")) list.add(new Filter(split[2], DoubleOperator.EQUALS, Double.valueOf(value)));
            else if (quantifier.equals("<")) list.add(new Filter(split[2], DoubleOperator.LESS_THAN, Double.valueOf(value)));
            else if (quantifier.equals(">")) list.add(new Filter(split[2], DoubleOperator.GREATER_THAN, Double.valueOf(value)));
            else if (quantifier.equals("<=")) list.add(new Filter(split[2], DoubleOperator.LESS_THAN_OR_EQUALS, Double.valueOf(value)));
            else if (quantifier.equals(">=")) list.add(new Filter(split[2], DoubleOperator.GREATER_THAN_OR_EQUALS, Double.valueOf(value)));
            else if (quantifier.equals("==")) list.add(new Filter(BooleanOperator.EQUALS, Boolean.valueOf(value)));
            else list.add(new Filter(BooleanOperator.NOT_EQUALS, Boolean.valueOf(value)));

        }
        System.out.println(list);
    }


    public static List<Filter> toFilters(String filterStr) {
        List<Filter> list = new ArrayList<>();
        String[] filters = (filterStr.split(":")[1]).split("&");

        for (int i = 0 ; i < filters.length; i++) {
            String [] split = filters[i].split("x");
            String quantifier = split[0];
            String value = split[1];
            if (quantifier.equals("=")) list.add(new Filter(split[2], DoubleOperator.EQUALS, Double.valueOf(value)));
            else if (quantifier.equals("<")) list.add(new Filter(split[2], DoubleOperator.LESS_THAN, Double.valueOf(value)));
            else if (quantifier.equals(">")) list.add(new Filter(split[2], DoubleOperator.GREATER_THAN, Double.valueOf(value)));
            else if (quantifier.equals("<=")) list.add(new Filter(split[2], DoubleOperator.LESS_THAN_OR_EQUALS, Double.valueOf(value)));
            else if (quantifier.equals(">=")) list.add(new Filter(split[2], DoubleOperator.GREATER_THAN_OR_EQUALS, Double.valueOf(value)));
            else if (quantifier.equals("==")) list.add(new Filter(BooleanOperator.EQUALS, Boolean.valueOf(value)));
            else list.add(new Filter(BooleanOperator.NOT_EQUALS, Boolean.valueOf(value)));

        }
        return list;
    }
}